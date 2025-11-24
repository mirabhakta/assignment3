package service;

import model.*;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Handles communication with Gemini API
// Builds requests, sends them, and parses responses into WritingResponse objects

public class APIService {
    private final HttpClient http;
    private final String apiKey;
    private final String modelName;
    private final String baseUrl;
    private final long minIntervalMs;

    // Rate limiting
    private final Object rateLock = new Object();
    private long lastCallEpochMs = 0L;

    public APIService() {
        APIClient c = APIClient.getInstance();
        this.http = c.http();
        this.apiKey = c.apiKey();
        this.modelName = c.model();
        this.baseUrl = c.baseUrl();
        this.minIntervalMs = c.minIntervalMs();
    }

    // for unit tests
    public APIService(HttpClient http, String apiKey, String modelName, String baseUrl, long minIntervalMs) {
        this.http = Objects.requireNonNull(http);
        this.apiKey = apiKey;
        this.modelName = modelName;
        this.baseUrl = baseUrl;
        this.minIntervalMs = minIntervalMs;
    }

    // Sends a text-generation request to Gemini and returns the parsed response
    public WritingResponse generateText(WritingRequest req) {
        enforceRateLimit();
        try {
            // Build request URL
            String url = baseUrl + "/v1/models/" + modelName
                    + ":generateContent?key=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8);

            // Prepare HTTP request body
            String bodyJson = buildGeminiJson(req);

            // Build and send request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(60))
                    .header("Content-Type", "application/json; charset=utf-8")
                    .POST(HttpRequest.BodyPublishers.ofString(bodyJson, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> resp = http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            // Throw error
            if (resp.statusCode() / 100 != 2) {
                throw new APIException(resp.statusCode(), "Gemini error " + resp.statusCode() + ": " + resp.body());
            }
            // Parse and return response
            return parseGeminiResponse(resp.body());
        } catch (APIException e) {
            throw e;
        } catch (Exception e) {
            throw new APIException("Network error: " + e.getMessage());
        }
    }

    // Rate limiting
    private void enforceRateLimit() {
        synchronized (rateLock) {
            long now = System.currentTimeMillis();
            long wait = (lastCallEpochMs + minIntervalMs) - now;
            if (wait > 0) {
                try { Thread.sleep(wait); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            }
            lastCallEpochMs = System.currentTimeMillis();
        }
    }

    // Escapes special character for JSON string building
    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }

    // Builds the request body sent to Gemini
    private String buildGeminiJson(WritingRequest r) {
        String sys = escape(r.getSystemPrompt());
        String usr = escape(r.getUserText());
        String merged = sys + "\\n\\n" + usr;

        return "{"
                + "\"contents\":[{\"role\":\"user\",\"parts\":[{\"text\":\"" + merged + "\"}]}],"
                + "\"generationConfig\":{"
                + "\"candidateCount\":1,"
                + "\"temperature\":" + r.getTemperature() + ","
                + "\"topP\":" + r.getTopP() + ","
                + "\"maxOutputTokens\":" + r.getMaxTokens()
                + "}"
                + "}";
    }

    // Parses Gemini's JSON response into a WritingResponse object
    private WritingResponse parseGeminiResponse(String json) {
        if (json == null || json.isBlank()) {
            throw new APIException("Empty response from API");
        }
        if (json.contains("\"error\"")) {
            throw new APIException("API returned error JSON: " + json);
        }

        String text = findFirst(
                json,
                "\"candidates\"\\s*:\\s*\\[\\s*\\{[^}]*?\"content\"\\s*:\\s*\\{[^}]*?\"parts\"\\s*:\\s*\\[\\s*\\{\\s*\"text\"\\s*:\\s*\"(.*?)\"",
                Pattern.DOTALL);

        // fallbacks
        if (text == null) {
            text = findFirst(json, "\"parts\"\\s*:\\s*\\[\\s*\\{[^}]*?\"text\"\\s*:\\s*\"(.*?)\"", Pattern.DOTALL);
        }
        if (text == null) {
            text = findFirst(json, "\"text\"\\s*:\\s*\"(.*?)\"", Pattern.DOTALL);
        }

        String finish = findFirst(json, "\"finishReason\"\\s*:\\s*\"(.*?)\"", 0);
        if (finish == null) finish = "";

        // token metadata
        int pt = parseInt(json, "\"promptTokenCount\"\\s*:\\s*(\\d+)");
        int ct = parseInt(json, "\"candidatesTokenCount\"\\s*:\\s*(\\d+)");
        int tt = parseInt(json, "\"totalTokenCount\"\\s*:\\s*(\\d+)");
        if (pt == 0 || ct == 0 || tt == 0) {
            pt = Math.max(pt, parseInt(json, "\"inputTokenCount\"\\s*:\\s*(\\d+)"));
            ct = Math.max(ct, parseInt(json, "\"outputTokenCount\"\\s*:\\s*(\\d+)"));
            if (tt == 0 && (pt > 0 || ct > 0)) tt = pt + ct;
        }

        // safety
        if (text == null || text.isEmpty()) {
            String block = findFirst(json, "\"blockReason\"\\s*:\\s*\"(.*?)\"", 0);
            if (block != null) {
                return new WritingResponse("[Response blocked by safety: " + block + "]", finish, pt, ct, tt);
            }
            if ("MAX_TOKENS".equals(finish)) {
                return new WritingResponse(
                        "[Model stopped early (MAX_TOKENS) and returned no text. Increase GEMINI_MAX_OUTPUT_TOKENS in config.]",
                        finish, pt, ct, tt);
            }
            return new WritingResponse("[Model returned no text in candidates.]", finish, pt, ct, tt);
        }

        return new WritingResponse(unescape(text), finish, pt, ct, tt);
    }
    // unescapes characters
    private static String unescape(String s) {
        return s.replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    private static String findFirst(String text, String regex, int flags) {
        Pattern p = Pattern.compile(regex, flags);
        Matcher m = p.matcher(text);
        if (m.find()) return m.group(1);
        return null;
    }

    // Parses integer value from the JSON text
    private static int parseInt(String text, String regex) {
        String v = findFirst(text, regex, 0);
        if (v == null) return 0;
        try { return Integer.parseInt(v); } catch (NumberFormatException e) { return 0; }
    }
}
