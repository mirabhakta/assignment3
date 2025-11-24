package model.strategy;

// Configuration values and behaviors for the "Academic" writing mode

public class AcademicStrategy implements WritingStrategy {
    @Override public String systemPrompt() {
        return "You are an academic writing assistant. Maintain formal tone, cautious claims, and logical structure. "
             + "Explain reasoning, define terms, and avoid rhetorical flourish.";
    }
    @Override public double temperature() { return 0.3; } // Controls the randomness of AI output.
    @Override public double topP() { return 0.85; }
    // The diversity of token sampling, set to 0.85 to encourage well-worded and formal responses
    @Override public double presencePenalty() { return 0.0; }
    // Controls how much the AI avoids repeating ideas or topics.
    // Set to 0 since academic text should maintain consistency and depth.
}
