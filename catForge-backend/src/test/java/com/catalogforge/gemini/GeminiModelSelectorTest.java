package com.catalogforge.gemini;

import net.jqwik.api.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for GeminiModelSelector including property-based tests.
 * 
 * Property Tests validate:
 * - Property 12: Gemini Model Selection (Requirements 8.2, 8.3, 8.4)
 */
class GeminiModelSelectorTest {

    @Nested
    @DisplayName("Property 12: Gemini Model Selection")
    class GeminiModelSelectionTests {

        @Test
        @DisplayName("Simple layout should use flash model")
        void simpleLayoutShouldUseFlashModel() {
            String model = GeminiModelSelector.selectModel(false, false);
            assertThat(model).isEqualTo(GeminiModelSelector.MODEL_FLASH);
        }

        @Test
        @DisplayName("Complex layout should use pro model")
        void complexLayoutShouldUseProModel() {
            String model = GeminiModelSelector.selectModel(true, false);
            assertThat(model).isEqualTo(GeminiModelSelector.MODEL_PRO);
        }

        @Test
        @DisplayName("Image analysis should use vision model")
        void imageAnalysisShouldUseVisionModel() {
            String model = GeminiModelSelector.selectModel(false, true);
            assertThat(model).isEqualTo(GeminiModelSelector.MODEL_VISION);
        }

        @Test
        @DisplayName("Image with complex layout should prioritize vision model")
        void imageWithComplexLayoutShouldPrioritizeVision() {
            String model = GeminiModelSelector.selectModel(true, true);
            assertThat(model).isEqualTo(GeminiModelSelector.MODEL_VISION);
        }

        @Property
        @Label("Model selection should always return a valid model")
        void modelSelectionShouldAlwaysReturnValidModel(
                @ForAll boolean isComplex,
                @ForAll boolean hasImage) {
            
            String model = GeminiModelSelector.selectModel(isComplex, hasImage);
            
            assertThat(model).isNotNull();
            assertThat(model).isNotBlank();
            assertThat(model).isIn(
                    GeminiModelSelector.MODEL_FLASH,
                    GeminiModelSelector.MODEL_PRO,
                    GeminiModelSelector.MODEL_VISION
            );
        }

        @Property
        @Label("Image requests should always use vision-capable model")
        void imageRequestsShouldUseVisionModel(@ForAll boolean isComplex) {
            String model = GeminiModelSelector.selectModel(isComplex, true);
            assertThat(model).isEqualTo(GeminiModelSelector.MODEL_VISION);
        }
    }

    @Nested
    @DisplayName("Convenience Methods")
    class ConvenienceMethodsTests {

        @Test
        @DisplayName("forSimpleLayout should return flash model")
        void forSimpleLayoutShouldReturnFlash() {
            assertThat(GeminiModelSelector.forSimpleLayout())
                    .isEqualTo(GeminiModelSelector.MODEL_FLASH);
        }

        @Test
        @DisplayName("forComplexLayout should return pro model")
        void forComplexLayoutShouldReturnPro() {
            assertThat(GeminiModelSelector.forComplexLayout())
                    .isEqualTo(GeminiModelSelector.MODEL_PRO);
        }

        @Test
        @DisplayName("forImageAnalysis should return vision model")
        void forImageAnalysisShouldReturnVision() {
            assertThat(GeminiModelSelector.forImageAnalysis())
                    .isEqualTo(GeminiModelSelector.MODEL_VISION);
        }
    }
}
