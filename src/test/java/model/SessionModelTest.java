package model;

import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.jupiter.api.Assertions.*;

public class SessionModelTest {

    @Test
    void propertyChangeFiresOnLoading() {
        SessionModel m = new SessionModel();
        AtomicBoolean fired = new AtomicBoolean(false);
        m.addPropertyChangeListener(e -> {
            if ("loading".equals(e.getPropertyName()))
                fired.set(true);
        });
        m.setLoading(true);
        assertTrue(fired.get());
    }

    @Test
    void responseTextUpdates() {
        SessionModel m = new SessionModel();
        m.setResponseText("ok");
        assertEquals("ok", m.getResponseText());
    }
}
