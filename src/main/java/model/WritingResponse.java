package model;

// Represents the result returned by the Gemini API
// Contains the generated text and token usage details

public class WritingResponse {
    private final String text; // generated text response
    private final String finishReason; // why the generation stopped
    private final int promptTokens; // tokens used for the input
    private final int completionTokens; // tokens used for the output
    private final int totalTokens; // total tokens consumed in the request

    // Creates a new response with generated text and token statistics
    public WritingResponse(String text, String finishReason, int promptTokens, int completionTokens, int totalTokens) {
        this.text = text;
        this.finishReason = finishReason;
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.totalTokens = totalTokens;
    }

    // Getters for response data and usage metrics
    public String getText() { return text; }
    public String getFinishReason() { return finishReason; }
    public int getPromptTokens() { return promptTokens; }
    public int getCompletionTokens() { return completionTokens; }
    public int getTotalTokens() { return totalTokens; }
}
