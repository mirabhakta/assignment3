package model;

import model.strategy.AcademicStrategy;
import model.strategy.CreativeStrategy;
import model.strategy.ProfessionalStrategy;
import model.strategy.WritingStrategy;

// Factory class that builds WritingRequest objects based on the selected WritingMode

public class RequestFactory {

    public static WritingRequest build(WritingMode mode, String userText, int defaultMaxTokens) {
        // Chooses the appropriate writing strategy based on the selected mode
        WritingStrategy strategy = switch (mode) {
            case CREATIVE -> new CreativeStrategy();
            case PROFESSIONAL -> new ProfessionalStrategy();
            case ACADEMIC -> new AcademicStrategy();
        };

        // Builds the request using the chosen strategy's parameters
        return new WritingRequest(
                userText,
                strategy.systemPrompt(),
                strategy.temperature(),
                strategy.topP(),
                strategy.presencePenalty(),
                defaultMaxTokens
        );
    }
}