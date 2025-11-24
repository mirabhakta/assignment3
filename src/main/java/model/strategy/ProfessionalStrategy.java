package model.strategy;

// Configuration values and behaviors for the "Professional" writing mode

public class ProfessionalStrategy implements WritingStrategy {
    @Override public String systemPrompt() {
        return "You are a professional writing assistant. Use clear, concise business language. "
             + "Prefer active voice, bullet points when appropriate, and concrete next steps.";
    }
    @Override public double temperature() { return 0.5; } // AI's creativity, 0.5 = balanced
    @Override public double topP() { return 0.9; }
    // How much AI explores different phr. options, 0.9 = moderate variety
    @Override public double presencePenalty() { return 0.0; }
    // How much the AI avoids repeating ideas or topics.
    // Set to 0.0 because professional writing should be concise and clear.
}
