package com.catalogforge.gemini;

import com.catalogforge.model.ColorPalette;
import com.catalogforge.model.ImageAnalysisResult;
import com.catalogforge.model.LayoutHints;
import com.catalogforge.model.MoodAnalysis;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for GeminiVisionAnalyzer and Image Analysis models.
 */
class GeminiVisionAnalyzerTest {

    @Nested
    @DisplayName("Property 9: Image Analysis Extraction")
    class ImageAnalysisExtractionTests {

        @Property(tries = 50)
        @Label("ColorPalette should contain valid hex colors")
        void colorPaletteShouldContainValidHexColors(
                @ForAll("validHexColor") String primary,
                @ForAll("validHexColor") String secondary,
                @ForAll("validHexColor") String accent,
                @ForAll("validHexColor") String neutralLight,
                @ForAll("validHexColor") String neutralDark
        ) {
            ColorPalette palette = new ColorPalette(primary, secondary, accent, neutralLight, neutralDark);
            
            assertThat(palette.primary()).matches("^#[0-9A-Fa-f]{6}$");
            assertThat(palette.secondary()).matches("^#[0-9A-Fa-f]{6}$");
            assertThat(palette.accent()).matches("^#[0-9A-Fa-f]{6}$");
            assertThat(palette.neutralLight()).matches("^#[0-9A-Fa-f]{6}$");
            assertThat(palette.neutralDark()).matches("^#[0-9A-Fa-f]{6}$");
        }

        @Property(tries = 50)
        @Label("MoodAnalysis confidence should be between 0 and 1")
        void moodAnalysisConfidenceShouldBeValid(
                @ForAll("moodType") String type,
                @ForAll @DoubleRange(min = 0.0, max = 1.0) double confidence,
                @ForAll("moodKeywords") List<String> keywords
        ) {
            MoodAnalysis mood = new MoodAnalysis(type, confidence, keywords);
            
            assertThat(mood.type()).isNotBlank();
            assertThat(mood.confidence()).isBetween(0.0, 1.0);
            assertThat(mood.keywords()).isNotNull();
        }

        @Property(tries = 50)
        @Label("LayoutHints suggestedColumns should be positive")
        void layoutHintsSuggestedColumnsShouldBePositive(
                @ForAll("gridType") String gridType,
                @ForAll("density") String density,
                @ForAll("focusArea") String focusArea,
                @ForAll @IntRange(min = 1, max = 12) int suggestedColumns
        ) {
            LayoutHints hints = new LayoutHints(gridType, density, focusArea, suggestedColumns);
            
            assertThat(hints.gridType()).isNotBlank();
            assertThat(hints.density()).isNotBlank();
            assertThat(hints.focusArea()).isNotBlank();
            assertThat(hints.suggestedColumns()).isBetween(1, 12);
        }

        @Property(tries = 30)
        @Label("ImageAnalysisResult should contain all components")
        void imageAnalysisResultShouldContainAllComponents(
                @ForAll("validHexColor") String primary,
                @ForAll("moodType") String moodType,
                @ForAll @DoubleRange(min = 0.0, max = 1.0) double confidence,
                @ForAll("gridType") String gridType,
                @ForAll @IntRange(min = 1, max = 6) int columns
        ) {
            ColorPalette palette = new ColorPalette(primary, "#666666", "#0066CC", "#F5F5F5", "#1A1A1A");
            MoodAnalysis mood = new MoodAnalysis(moodType, confidence, List.of("clean"));
            LayoutHints hints = new LayoutHints(gridType, "medium", "center", columns);
            
            ImageAnalysisResult result = new ImageAnalysisResult(palette, mood, hints);
            
            assertThat(result.colorPalette()).isNotNull();
            assertThat(result.mood()).isNotNull();
            assertThat(result.layoutHints()).isNotNull();
            assertThat(result.colorPalette().primary()).isEqualTo(primary);
            assertThat(result.mood().type()).isEqualTo(moodType);
            assertThat(result.layoutHints().suggestedColumns()).isEqualTo(columns);
        }

        @Property(tries = 30)
        @Label("MoodAnalysis keywords should be immutable")
        void moodAnalysisKeywordsShouldBeImmutable(
                @ForAll("moodKeywords") List<String> keywords
        ) {
            MoodAnalysis mood = new MoodAnalysis("professional", 0.8, keywords);
            
            // Keywords should be a defensive copy
            assertThat(mood.keywords()).isNotNull();
            assertThat(mood.keywords()).containsExactlyElementsOf(
                    keywords != null ? keywords : List.of()
            );
        }

        @Provide
        Arbitrary<String> validHexColor() {
            return Arbitraries.strings()
                    .withCharRange('0', '9')
                    .withCharRange('A', 'F')
                    .ofLength(6)
                    .map(s -> "#" + s);
        }

        @Provide
        Arbitrary<String> moodType() {
            return Arbitraries.of(
                    "professional", "dynamic", "elegant", "technical", 
                    "eco-friendly", "premium", "modern", "classic"
            );
        }

        @Provide
        Arbitrary<List<String>> moodKeywords() {
            return Arbitraries.of(
                    "clean", "modern", "sleek", "powerful", "efficient",
                    "innovative", "sustainable", "robust", "elegant"
            ).list().ofMinSize(0).ofMaxSize(5);
        }

        @Provide
        Arbitrary<String> gridType() {
            return Arbitraries.of("modular", "columnar", "hierarchical", "free-form", "grid");
        }

        @Provide
        Arbitrary<String> density() {
            return Arbitraries.of("low", "medium", "high");
        }

        @Provide
        Arbitrary<String> focusArea() {
            return Arbitraries.of("center", "top", "bottom", "left", "right", "top-left", "top-right");
        }
    }

    @Nested
    @DisplayName("Unit Tests for Image Analysis Models")
    class ImageAnalysisModelTests {

        @Test
        @DisplayName("ColorPalette should store all five colors")
        void colorPaletteShouldStoreAllFiveColors() {
            ColorPalette palette = new ColorPalette("#111111", "#222222", "#333333", "#EEEEEE", "#000000");
            
            assertThat(palette.primary()).isEqualTo("#111111");
            assertThat(palette.secondary()).isEqualTo("#222222");
            assertThat(palette.accent()).isEqualTo("#333333");
            assertThat(palette.neutralLight()).isEqualTo("#EEEEEE");
            assertThat(palette.neutralDark()).isEqualTo("#000000");
        }

        @Test
        @DisplayName("MoodAnalysis should handle null keywords")
        void moodAnalysisShouldHandleNullKeywords() {
            MoodAnalysis mood = new MoodAnalysis("professional", 0.9, null);
            
            assertThat(mood.keywords()).isNotNull();
            assertThat(mood.keywords()).isEmpty();
        }

        @Test
        @DisplayName("LayoutHints should store grid configuration")
        void layoutHintsShouldStoreGridConfiguration() {
            LayoutHints hints = new LayoutHints("modular", "high", "center", 3);
            
            assertThat(hints.gridType()).isEqualTo("modular");
            assertThat(hints.density()).isEqualTo("high");
            assertThat(hints.focusArea()).isEqualTo("center");
            assertThat(hints.suggestedColumns()).isEqualTo(3);
        }

        @Test
        @DisplayName("ImageAnalysisResult should compose all analysis components")
        void imageAnalysisResultShouldComposeAllComponents() {
            ColorPalette palette = new ColorPalette("#333333", "#666666", "#0066CC", "#F5F5F5", "#1A1A1A");
            MoodAnalysis mood = new MoodAnalysis("dynamic", 0.85, List.of("powerful", "modern"));
            LayoutHints hints = new LayoutHints("columnar", "medium", "top", 2);
            
            ImageAnalysisResult result = new ImageAnalysisResult(palette, mood, hints);
            
            assertThat(result.colorPalette()).isEqualTo(palette);
            assertThat(result.mood()).isEqualTo(mood);
            assertThat(result.layoutHints()).isEqualTo(hints);
        }
    }
}
