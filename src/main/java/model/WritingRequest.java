package model;

// Represents a single request sent to the Gemini API
// Holds all user input and AI configuration parameters

public class WritingRequest {
    private final String userText; // user's input text
    private final String systemPrompt; // system's instructions guiding tone/style
    private final double temperature; // controls randomness
    private final double topP; // token sampling diversity
    private final double presencePenalty; // reduces repetition in output
    private final int maxTokens; // max tokens allowed in the response

    // Initializes a new writing request with all require parameters.
    public WritingRequest(String userText, String systemPrompt, double temperature,
                          double topP, double presencePenalty, int maxTokens) {
        this.userText = userText;
        this.systemPrompt = systemPrompt;
        this.temperature = temperature;
        this.topP = topP;
        this.presencePenalty = presencePenalty;
        this.maxTokens = maxTokens;
    }

    // Getters for all requests fields
    public String getUserText() { return userText; }
    public String getSystemPrompt() { return systemPrompt; }
    public double getTemperature() { return temperature; }
    public double getTopP() { return topP; }
    public double getPresencePenalty() { return presencePenalty; }
    public int getMaxTokens() { return maxTokens; }
}
