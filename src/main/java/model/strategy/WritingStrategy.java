package model.strategy;

// Defines the structure for the different strategies used in the writing modes

public interface WritingStrategy {
    String systemPrompt(); // Sets the AI's "personality"
    double temperature();
    // Controls the randomness of the AI's output. Higher value = more diverse, lower value = more focused
    double topP();
    // Specifies the sampling diversity.
    // Higher topP = more variety in word choice, lower topP = focuses on most probable words
    double presencePenalty();
    // Controls how much the Ai avoids repeating ideas/phrases
    // Positive values = encourages novelty, zero = consistent output
}
