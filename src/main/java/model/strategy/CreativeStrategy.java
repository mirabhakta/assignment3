package model.strategy;

// Configuration values and behaviors for the "Creative" writing mode

public class CreativeStrategy implements WritingStrategy {
    @Override public String systemPrompt() {
        return "You are a creative writing assistant. Emphasize vivid imagery, novel ideas, and narrative flow. "
             + "Avoid using a corporate tone. Keep it engaging and original.";
    }
    @Override public double temperature() { return 0.9; }
    // controls how random/imaginative the AI's output is, higher temp = more creative
    @Override public double topP() { return 0.95; }
    // How diverse the word choice is, 0.95 = more range of creative word selection
    @Override public double presencePenalty() { return 0.2; }
    // Small penalty for repetition, creativity should use more originality
}
