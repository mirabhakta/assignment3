package model;

import java.io.FileInputStream;
import java.io.InputStream;
import java.time.Duration;
import java.util.Optional;
import java.util.Properties;
import java.net.http.HttpClient;

// Class that manages configuration and HTTP setup for connecting to Gemini API

public final class APIClient {
    private static APIClient INSTANCE;

    private final HttpClient http;
    private final String apiKey;
    private final String model;
    private final String baseUrl;
    private final int maxTokens;
    private final long minIntervalMs;

    // Loads API credentials and settings from environment or config.properties

    private APIClient() {
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();

        Properties p = new Properties();

        // Tries to load from classpath first, then falls back to /resources
        try (InputStream in = APIClient.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (in != null) p.load(in);
        } catch (Exception ignored) {}

        if (p.isEmpty()) {
            try (InputStream in = new FileInputStream("resources/config.properties.example")) {
                p.load(in);
            } catch (Exception ignored) {}
        }

        // Loads values from config file or environment variables
        this.apiKey = Optional.ofNullable(System.getenv("GEMINI_API_KEY"))
                .orElse(p.getProperty("GEMINI_API_KEY", "")).trim();
        this.model = p.getProperty("GEMINI_MODEL", "gemini-2.5-flash").trim();
        this.baseUrl = p.getProperty("GEMINI_BASE_URL", "https://generativelanguage.googleapis.com").trim();
        this.maxTokens = Integer.parseInt(p.getProperty("GEMINI_MAX_TOKENS", "1024").trim());
        this.minIntervalMs = Long.parseLong(p.getProperty("REQUEST_MIN_INTERVAL_MS", "1200").trim());

        if (apiKey.isEmpty()) {
            throw new IllegalStateException("Missing GEMINI_API_KEY (env var or resources/config.properties).");
        }
    }

    public static synchronized APIClient getInstance() {
        if (INSTANCE == null) INSTANCE = new APIClient();
        return INSTANCE;
    }

    // Getters for configuration and HTTP client
    public HttpClient http() { return http; }
    public String apiKey() { return apiKey; }
    public String model() { return model; }
    public String baseUrl() { return baseUrl; }
    public int defaultMaxTokens() { return maxTokens; }
    public long minIntervalMs() { return minIntervalMs; }
}
