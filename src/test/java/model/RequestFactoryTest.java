package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RequestFactoryTest {

    @Test
    void build_creative_usesCreativeParams() {
        WritingRequest req = RequestFactory.build(WritingMode.CREATIVE, "Hello", 200);
        assertTrue(req.getSystemPrompt().toLowerCase().contains("creative"));
        assertTrue(req.getTemperature() > 0.8);
        assertEquals(200, req.getMaxTokens());
    }

    @Test
    void build_professional_temperatureLower() {
        WritingRequest req = RequestFactory.build(WritingMode.PROFESSIONAL, "Hello", 300);
        assertTrue(req.getTemperature() <= 0.5);
    }

    @Test
    void build_academic_formalTone() {
        WritingRequest req = RequestFactory.build(WritingMode.ACADEMIC, "Hello", 400);
        assertTrue(req.getSystemPrompt().toLowerCase().contains("academic"));
        assertTrue(req.getTemperature() <= 0.3);
    }
}
