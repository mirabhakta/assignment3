package service;

import model.APIException;
import model.WritingRequest;
import model.WritingResponse;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;

class APIServiceTest {

    // Helpers

    private static WritingRequest sampleReq() {
        return new WritingRequest(
                "User text here",
                "System prompt here",
                0.3,
                0.85,
                0.0,
                256
        );
    }

    // Fake HttpClient so we can unit test without real network calls
    private static final class FakeHttpClient extends HttpClient {
        private int status = 200;
        private String body = "{}";
        private HttpRequest lastRequest;

        FakeHttpClient respond(int status, String body) {
            this.status = status;
            this.body = body;
            return this;
        }

        HttpRequest lastRequest() {
            return lastRequest;
        }

        @Override
        public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) {
            this.lastRequest = request;

            @SuppressWarnings("unchecked")
            T castBody = (T) body; // our tests only use BodyHandlers.ofString(...)
            return new FakeHttpResponse<>(request, status, castBody);
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) {
            return CompletableFuture.completedFuture(send(request, responseBodyHandler));
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(
                HttpRequest request,
                HttpResponse.BodyHandler<T> responseBodyHandler,
                HttpResponse.PushPromiseHandler<T> pushPromiseHandler
        ) {
            return CompletableFuture.completedFuture(send(request, responseBodyHandler));
        }

        // Not used by our tests, but required by abstract class
        @Override public Optional<CookieHandler> cookieHandler() { return Optional.empty(); }
        @Override public Optional<Duration> connectTimeout() { return Optional.empty(); }
        @Override public Redirect followRedirects() { return Redirect.NEVER; }
        @Override public Optional<ProxySelector> proxy() { return Optional.empty(); }
        @Override public SSLContext sslContext() { return null; }
        @Override public SSLParameters sslParameters() { return new SSLParameters(); }
        @Override public Optional<java.net.Authenticator> authenticator() { return Optional.empty(); }
        @Override public Version version() { return Version.HTTP_1_1; }
        @Override public Optional<Executor> executor() { return Optional.empty(); }
    }

    private static final class FakeHttpResponse<T> implements HttpResponse<T> {
        private final HttpRequest request;
        private final int status;
        private final T body;

        FakeHttpResponse(HttpRequest request, int status, T body) {
            this.request = request;
            this.status = status;
            this.body = body;
        }

        @Override public int statusCode() {return status; }
        @Override public HttpRequest request() {return request; }
        @Override public Optional<HttpResponse<T>> previousResponse() { return Optional.empty(); }
        @Override public HttpHeaders headers() {return HttpHeaders.of(java.util.Map.of(), (a, b) -> true); }
        @Override public T body() { return body; }
        @Override public Optional<javax.net.ssl.SSLSession> sslSession() {return Optional.empty(); }
        @Override public URI uri() {return request.uri(); }
        @Override public HttpClient.Version version() {return HttpClient.Version.HTTP_1_1;}
    }

    // Tests

    @Test
    void generateText_success_parsesTextFinishAndTokens() {
        FakeHttpClient http = new FakeHttpClient().respond(
                200,
                "{"
                        + "\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"Hello world\"}]},\"finishReason\":\"STOP\"}],"
                        + "\"usageMetadata\":{\"promptTokenCount\":5,\"candidatesTokenCount\":7,\"totalTokenCount\":12}"
                        + "}"
        );

        APIService svc = new APIService(http, "k", "m", "https://example.com", 0);
        WritingResponse r = svc.generateText(sampleReq());

        assertEquals("Hello world", r.getText());
        assertEquals("STOP", r.getFinishReason());
        assertEquals(5, r.getPromptTokens());
        assertEquals(7, r.getCompletionTokens());
        assertEquals(12, r.getTotalTokens());
    }

    @Test
    void generateText_non2xx_throwsAPIExceptionWithStatus() {
        FakeHttpClient http = new FakeHttpClient().respond(401, "{\"error\":\"nope\"}");

        APIService svc = new APIService(http, "k", "m", "https://example.com", 0);

        APIException ex = assertThrows(APIException.class, () -> svc.generateText(sampleReq()));
        assertEquals(401, ex.getStatus()); // APIException stores status in getStatus() :contentReference[oaicite:2]{index=2}
        assertTrue(ex.getMessage().contains("Gemini error 401"));
    }

    @Test
    void generateText_blankBody_throwsEmptyResponse() {
        FakeHttpClient http = new FakeHttpClient().respond(200, "   ");

        APIService svc = new APIService(http, "k", "m", "https://example.com", 0);

        APIException ex = assertThrows(APIException.class, () -> svc.generateText(sampleReq()));
        assertTrue(ex.getMessage().contains("Empty response from API"));
    }

    @Test
    void generateText_errorJson_throwsApiReturnedErrorJson() {
        FakeHttpClient http = new FakeHttpClient().respond(200, "{\"error\":{\"message\":\"bad\"}}");

        APIService svc = new APIService(http, "k", "m", "https://example.com", 0);

        APIException ex = assertThrows(APIException.class, () -> svc.generateText(sampleReq()));
        assertTrue(ex.getMessage().startsWith("API returned error JSON:"));
    }

    @Test
    void generateText_safetyBlock_returnsBlockedMessage() {
        FakeHttpClient http = new FakeHttpClient().respond(
                200,
                "{"
                        + "\"candidates\":[{\"finishReason\":\"SAFETY\"}],"
                        + "\"blockReason\":\"SAFETY\","
                        + "\"usageMetadata\":{\"promptTokenCount\":1,\"candidatesTokenCount\":1,\"totalTokenCount\":2}"
                        + "}"
        );

        APIService svc = new APIService(http, "k", "m", "https://example.com", 0);
        WritingResponse r = svc.generateText(sampleReq());

        assertTrue(r.getText().contains("Response blocked by safety"));
        assertEquals("SAFETY", r.getFinishReason());
    }

    @Test
    void generateText_maxTokensNoText_returnsHelpfulMessage() {
        FakeHttpClient http = new FakeHttpClient().respond(
                200,
                "{"
                        + "\"candidates\":[{\"finishReason\":\"MAX_TOKENS\"}],"
                        + "\"usageMetadata\":{\"promptTokenCount\":2,\"candidatesTokenCount\":2,\"totalTokenCount\":4}"
                        + "}"
        );

        APIService svc = new APIService(http, "k", "m", "https://example.com", 0);
        WritingResponse r = svc.generateText(sampleReq());

        assertTrue(r.getText().contains("MAX_TOKENS"));
        assertEquals("MAX_TOKENS", r.getFinishReason());
    }

    @Test
    void generateText_buildsRequestUrlWithModelAndEncodedKey() {
        FakeHttpClient http = new FakeHttpClient().respond(
                200,
                "{"
                        + "\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"OK\"}]},\"finishReason\":\"STOP\"}]"
                        + "}"
        );

        String apiKey = "a b"; // should get encoded to a+b
        APIService svc = new APIService(http, apiKey, "gemini-2.5-flash", "https://generativelanguage.googleapis.com", 0);

        svc.generateText(sampleReq());

        String url = http.lastRequest().uri().toString();
        assertTrue(url.contains("/v1/models/gemini-2.5-flash:generateContent?key="));
        assertTrue(url.contains("a+b"));
    }
}

