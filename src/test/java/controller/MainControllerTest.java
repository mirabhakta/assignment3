package controller;

import model.SessionModel;
import model.WritingMode;
import model.WritingRequest;
import model.WritingResponse;
import org.junit.jupiter.api.Test;
import service.APIService;

import javax.swing.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

// Fake service to avoid real HTTP
class FakeService extends APIService {
    @Override
    public WritingResponse generateText(WritingRequest r) {
        try {
            Thread.sleep(50);
        } catch (InterruptedException ignored) {
        }
        return new WritingResponse("ok-" + r.getUserText(), "stop", 1, 1, 2);
    }
}

public class MainControllerTest {

    @Test
    void generateRunsAsyncAndUpdatesModel() throws Exception {
        SessionModel m = new SessionModel();
        MainController c = new MainController(m) {
            { // replace real API with fake via reflection-less approach
                try {
                    var f = MainController.class.getDeclaredField("api");
                    f.setAccessible(true);
                    f.set(this, new FakeService());
                } catch (Exception ignored) {
                }
            }
        };

        CountDownLatch done = new CountDownLatch(1);
        m.addPropertyChangeListener(e -> {
            if ("responseText".equals(e.getPropertyName()))
                done.countDown();
        });

        // Run on EDT to mimic real button click
        SwingUtilities.invokeAndWait(() -> c.onGenerate("Mira", WritingMode.PROFESSIONAL));

        assertTrue(done.await(2, TimeUnit.SECONDS), "Response should be set asynchronously");
        assertEquals("ok-Mira", m.getResponseText());
        assertFalse(m.isLoading());
    }
}
