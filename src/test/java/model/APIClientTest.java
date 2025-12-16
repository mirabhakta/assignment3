package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Singleton APIClient
 */
class APIClientTest {

    @Test
    void testSingletonPattern() {
        APIClient instance1 = APIClient.getInstance();
        APIClient instance2 = APIClient.getInstance();

        assertSame(instance1, instance2, "Both instances should be the same object");
    }

    @Test
    void testGetApiUrl() {
        APIClient client = APIClient.getInstance();
        assertNotNull(client.baseUrl(), "API URL should not be null");
        assertFalse(client.baseUrl().isBlank(), "Base URL should not be blank");
        assertTrue(client.baseUrl().startsWith("https://"),
                "API URL should start with https://");
    }

    @Test
    void testGetApiKey() {
        APIClient client = APIClient.getInstance();
        assertNotNull(client.apiKey(), "API key should not be null");
        assertFalse(client.apiKey().isEmpty(), "API key should not be empty");
    }
}
