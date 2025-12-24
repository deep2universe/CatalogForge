package com.catalogforge.gemini;

import java.util.List;

/**
 * Response structure from Gemini API.
 */
public record GeminiResponse(
    List<Candidate> candidates,
    UsageMetadata usageMetadata,
    String modelVersion
) {
    
    public record Candidate(
        Content content,
        String finishReason,
        Integer index,
        List<SafetyRating> safetyRatings
    ) {}
    
    public record Content(
        List<Part> parts,
        String role
    ) {}
    
    public record Part(
        String text
    ) {}
    
    public record SafetyRating(
        String category,
        String probability
    ) {}
    
    public record UsageMetadata(
        Integer promptTokenCount,
        Integer candidatesTokenCount,
        Integer totalTokenCount
    ) {}

    /**
     * Extracts the text content from the first candidate.
     * @return The generated text or null if not available
     */
    public String getText() {
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }
        Candidate first = candidates.get(0);
        if (first.content() == null || first.content().parts() == null || first.content().parts().isEmpty()) {
            return null;
        }
        return first.content().parts().get(0).text();
    }

    /**
     * Checks if the response was successful.
     */
    public boolean isSuccess() {
        return candidates != null && !candidates.isEmpty() 
                && candidates.get(0).finishReason() != null
                && ("STOP".equals(candidates.get(0).finishReason()) 
                    || "MAX_TOKENS".equals(candidates.get(0).finishReason()));
    }

    /**
     * Gets the finish reason from the first candidate.
     */
    public String getFinishReason() {
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }
        return candidates.get(0).finishReason();
    }

    /**
     * Gets the total token count.
     */
    public int getTotalTokens() {
        return usageMetadata != null && usageMetadata.totalTokenCount() != null 
                ? usageMetadata.totalTokenCount() 
                : 0;
    }
}
