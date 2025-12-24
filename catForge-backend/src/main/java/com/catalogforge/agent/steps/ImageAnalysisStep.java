package com.catalogforge.agent.steps;

import com.catalogforge.agent.AgentContext;
import com.catalogforge.agent.AgentStep;
import com.catalogforge.gemini.GeminiVisionAnalyzer;
import com.catalogforge.model.ColorPalette;
import com.catalogforge.model.ImageAnalysisResult;
import com.catalogforge.model.LayoutHints;
import com.catalogforge.model.MoodAnalysis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Analyzes reference images using Gemini Vision.
 * Extracts color palette, mood, and layout hints.
 */
@Component
public class ImageAnalysisStep implements AgentStep {

    private static final Logger log = LoggerFactory.getLogger(ImageAnalysisStep.class);

    private final GeminiVisionAnalyzer visionAnalyzer;

    public ImageAnalysisStep(GeminiVisionAnalyzer visionAnalyzer) {
        this.visionAnalyzer = visionAnalyzer;
    }

    @Override
    public AgentContext execute(AgentContext context) {
        if (!context.hasImage()) {
            log.debug("No image to analyze, skipping ImageAnalysisStep");
            return context;
        }
        
        log.debug("Analyzing image for pipeline: {}", context.pipelineId());
        
        try {
            ImageAnalysisResult result = visionAnalyzer.analyzeImage(
                    context.imageBase64(),
                    context.imageMimeType()
            );
            
            log.debug("Image analysis completed: mood={}, colors extracted", 
                    result.mood().type());
            
            return context.withImageAnalysis(result);
            
        } catch (Exception e) {
            log.warn("Image analysis failed, using placeholder: {}", e.getMessage());
            return context.withImageAnalysis(createPlaceholderAnalysis());
        }
    }

    private ImageAnalysisResult createPlaceholderAnalysis() {
        return new ImageAnalysisResult(
                new ColorPalette("#333333", "#666666", "#0066CC", "#F5F5F5", "#1A1A1A"),
                new MoodAnalysis("professional", 0.7, List.of("clean", "modern")),
                new LayoutHints("modular", "medium", "center", 2)
        );
    }

    @Override
    public String name() {
        return "ImageAnalysis";
    }
}
