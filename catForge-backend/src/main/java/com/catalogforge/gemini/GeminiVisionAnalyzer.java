package com.catalogforge.gemini;

import com.catalogforge.model.ColorPalette;
import com.catalogforge.model.ImageAnalysisResult;
import com.catalogforge.model.LayoutHints;
import com.catalogforge.model.MoodAnalysis;
import com.catalogforge.util.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Analyzes images using Gemini Vision to extract color palettes, mood, and layout hints.
 */
@Component
public class GeminiVisionAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(GeminiVisionAnalyzer.class);

    private final GeminiClient geminiClient;

    public GeminiVisionAnalyzer(GeminiClient geminiClient) {
        this.geminiClient = geminiClient;
    }

    /**
     * Analyzes an image and extracts design-relevant information.
     * 
     * @param imageBase64 Base64-encoded image data
     * @param mimeType Image MIME type (e.g., "image/jpeg")
     * @return Analysis result with color palette, mood, and layout hints
     */
    public ImageAnalysisResult analyzeImage(String imageBase64, String mimeType) {
        log.debug("Analyzing image: mimeType={}", mimeType);

        GeminiRequest request = GeminiRequest.builder()
                .systemInstruction(ANALYSIS_SYSTEM_PROMPT)
                .userPrompt(ANALYSIS_USER_PROMPT)
                .image(imageBase64, mimeType)
                .responseSchema(ANALYSIS_SCHEMA)
                .temperature(0.3)
                .build();

        GeminiResponse response = geminiClient.generate(
                GeminiModelSelector.forImageAnalysis(), 
                request
        );

        return parseAnalysisResponse(response);
    }

    private ImageAnalysisResult parseAnalysisResponse(GeminiResponse response) {
        String json = response.getText();
        if (json == null || json.isBlank()) {
            log.warn("Empty response from vision analysis");
            return createPlaceholderResult();
        }

        try {
            Map<String, Object> data = JsonUtils.fromJson(json, new TypeReference<>() {});
            
            ColorPalette colorPalette = parseColorPalette(data);
            MoodAnalysis moodAnalysis = parseMoodAnalysis(data);
            LayoutHints layoutHints = parseLayoutHints(data);

            return new ImageAnalysisResult(colorPalette, moodAnalysis, layoutHints);
            
        } catch (Exception e) {
            log.error("Failed to parse vision analysis response", e);
            return createPlaceholderResult();
        }
    }

    @SuppressWarnings("unchecked")
    private ColorPalette parseColorPalette(Map<String, Object> data) {
        Map<String, Object> palette = (Map<String, Object>) data.get("colorPalette");
        if (palette == null) {
            return new ColorPalette("#333333", "#666666", "#0066CC", "#F5F5F5", "#1A1A1A");
        }
        
        String primary = (String) palette.getOrDefault("primary", "#333333");
        String secondary = (String) palette.getOrDefault("secondary", "#666666");
        String accent = (String) palette.getOrDefault("accent", "#0066CC");
        String neutralLight = (String) palette.getOrDefault("neutralLight", "#F5F5F5");
        String neutralDark = (String) palette.getOrDefault("neutralDark", "#1A1A1A");
        
        return new ColorPalette(primary, secondary, accent, neutralLight, neutralDark);
    }

    @SuppressWarnings("unchecked")
    private MoodAnalysis parseMoodAnalysis(Map<String, Object> data) {
        Map<String, Object> mood = (Map<String, Object>) data.get("moodAnalysis");
        if (mood == null) {
            return new MoodAnalysis("professional", 0.8, List.of("clean", "modern"));
        }
        
        String type = (String) mood.getOrDefault("type", "professional");
        double confidence = mood.containsKey("confidence") 
                ? ((Number) mood.get("confidence")).doubleValue() 
                : 0.8;
        List<String> keywords = (List<String>) mood.getOrDefault("keywords", List.of());
        
        return new MoodAnalysis(type, confidence, keywords);
    }

    @SuppressWarnings("unchecked")
    private LayoutHints parseLayoutHints(Map<String, Object> data) {
        Map<String, Object> hints = (Map<String, Object>) data.get("layoutHints");
        if (hints == null) {
            return new LayoutHints("modular", "medium", "center", 2);
        }
        
        String gridType = (String) hints.getOrDefault("gridType", "modular");
        String density = (String) hints.getOrDefault("density", "medium");
        String focusArea = (String) hints.getOrDefault("focusArea", "center");
        int suggestedColumns = hints.containsKey("suggestedColumns") 
                ? ((Number) hints.get("suggestedColumns")).intValue() 
                : 2;
        
        return new LayoutHints(gridType, density, focusArea, suggestedColumns);
    }

    private ImageAnalysisResult createPlaceholderResult() {
        return new ImageAnalysisResult(
                new ColorPalette("#333333", "#666666", "#0066CC", "#F5F5F5", "#1A1A1A"),
                new MoodAnalysis("professional", 0.8, List.of("clean", "modern")),
                new LayoutHints("modular", "medium", "center", 2)
        );
    }

    private static final String ANALYSIS_SYSTEM_PROMPT = """
        You are an expert image analyst for design purposes.
        Analyze images to extract color palettes, mood/atmosphere, and layout suggestions.
        Always respond with valid JSON matching the specified schema.
        """;

    private static final String ANALYSIS_USER_PROMPT = """
        Analyze this image and extract:
        1. Color palette (primary, secondary, background, accent colors as hex codes)
        2. Mood analysis (overall mood, energy level, descriptive keywords)
        3. Layout hints (focal point position, orientation, text presence, complexity)
        """;

    private static final Map<String, Object> ANALYSIS_SCHEMA = Map.of(
            "type", "object",
            "properties", Map.of(
                    "colorPalette", Map.of(
                            "type", "object",
                            "properties", Map.of(
                                    "primary", Map.of("type", "string"),
                                    "secondary", Map.of("type", "string"),
                                    "accent", Map.of("type", "string"),
                                    "neutralLight", Map.of("type", "string"),
                                    "neutralDark", Map.of("type", "string")
                            )
                    ),
                    "moodAnalysis", Map.of(
                            "type", "object",
                            "properties", Map.of(
                                    "type", Map.of("type", "string"),
                                    "confidence", Map.of("type", "number"),
                                    "keywords", Map.of("type", "array", "items", Map.of("type", "string"))
                            )
                    ),
                    "layoutHints", Map.of(
                            "type", "object",
                            "properties", Map.of(
                                    "gridType", Map.of("type", "string"),
                                    "density", Map.of("type", "string"),
                                    "focusArea", Map.of("type", "string"),
                                    "suggestedColumns", Map.of("type", "integer")
                            )
                    )
            )
    );
}
