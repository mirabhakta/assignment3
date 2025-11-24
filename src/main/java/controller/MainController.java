package controller;

import model.*;
import service.APIService;

import javax.swing.*;

/* coordinates interactions between the view and the model.
    Handles user requests & triggers API calls asynchronously.
    Also updates session model with the results or errors.
*/

public class MainController {
    private final SessionModel session;
    // Handles actual API calls to the Gemini API
    private final APIService api;

    public MainController(SessionModel session) {
        this.session = session;
        this.api = new APIService();
    }

    // For j-unit tests
    public MainController(SessionModel session, APIService apiService) {
        this.session = session;
        this.api = apiService;
    }

    // Called when the user presses "Enter" or submits text in the editor panel
    // Builds a WritingRequest
    public void onGenerate(String input, WritingMode mode) {
        session.setErrorMessage("");
        session.setMode(mode);
        session.setLoading(true);

        // Performs the API call in a background threat so the UI remains responsive.
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private String resultText;
            private String error;

            @Override protected Void doInBackground() {
                try {
                    // Builds the API request based on the selected writing mode
                    int maxTokens = APIClient.getInstance().defaultMaxTokens();
                    WritingRequest req = RequestFactory.build(mode, input, maxTokens); //  Makes the call to Gemini API
                    WritingResponse resp = api.generateText(req);
                    resultText = resp.getText();
                } catch (Exception ex) {
                    error = ex.getMessage();
                    // Captures any exceptions or API errors for display in the UI
                }
                return null;
            }

            @Override protected void done() {
                // Marks loading as complete
                session.setLoading(false);
                if (error != null && !error.isBlank()) { // Logic of error handling
                    session.setErrorMessage(error);
                } else {
                    session.setResponseText(resultText == null ? "" : resultText.trim());
                }
            }
        };
        worker.execute(); // background worker
    }
    public SessionModel getSession() { return session; } //
}
