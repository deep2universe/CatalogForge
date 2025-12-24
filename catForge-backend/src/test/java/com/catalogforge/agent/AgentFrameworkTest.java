package com.catalogforge.agent;

import com.catalogforge.model.*;
import com.catalogforge.model.request.LayoutOptions;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based and unit tests for Agent Framework.
 */
class AgentFrameworkTest {

    @Nested
    @DisplayName("Property 6: Pipeline Selection Logic")
    class PipelineSelectionTests {

        @Property(tries = 50)
        @Label("Simple options should select LinearPipeline")
        void simpleOptionsShouldSelectLinearPipeline(
                @ForAll("simpleStyle") String style,
                @ForAll("pageFormat") String format
        ) {
            LayoutOptions options = new LayoutOptions(format, style, 1, true, false);
            
            AgentContext context = AgentContext.forTextGeneration(
                    List.of(createTestProduct()),
                    options,
                    "Create a simple layout"
            );
            
            // Simple options = not multi-variant
            assertThat(context.isMultiVariant()).isFalse();
        }

        @Property(tries = 30)
        @Label("Multi-variant options should be detected")
        void multiVariantOptionsShouldBeDetected(
                @ForAll @IntRange(min = 2, max = 5) int variantCount
        ) {
            LayoutOptions options = new LayoutOptions("A4", "modern", variantCount, true, false);
            
            AgentContext context = AgentContext.forTextGeneration(
                    List.of(createTestProduct()),
                    options,
                    "Create multiple variants"
            );
            
            assertThat(context.isMultiVariant()).isTrue();
        }

        @Property(tries = 30)
        @Label("Image context should be detected")
        void imageContextShouldBeDetected(
                @ForAll @StringLength(min = 100, max = 500) String imageBase64
        ) {
            LayoutOptions options = new LayoutOptions("A4", "modern", 1, true, false);
            
            AgentContext context = AgentContext.forImageGeneration(
                    List.of(createTestProduct()),
                    options,
                    "Match this style",
                    imageBase64,
                    "image/jpeg"
            );
            
            assertThat(context.hasImage()).isTrue();
        }

        @Provide
        Arbitrary<String> simpleStyle() {
            return Arbitraries.of("modern", "technical", "premium", "eco", "dynamic");
        }

        @Provide
        Arbitrary<String> pageFormat() {
            return Arbitraries.of("A4", "A5", "DL", "A6", "square");
        }
    }

    @Nested
    @DisplayName("AgentContext Unit Tests")
    class AgentContextUnitTests {

        @Test
        @DisplayName("forTextGeneration should create valid context")
        void forTextGenerationShouldCreateValidContext() {
            Product product = createTestProduct();
            LayoutOptions options = new LayoutOptions("A4", "modern", 1, true, false);
            
            AgentContext context = AgentContext.forTextGeneration(
                    List.of(product),
                    options,
                    "Create a product page"
            );
            
            assertThat(context.pipelineId()).isNotBlank();
            assertThat(context.requestId()).isNotBlank();
            assertThat(context.products()).hasSize(1);
            assertThat(context.options()).isEqualTo(options);
            assertThat(context.userPrompt()).isEqualTo("Create a product page");
            assertThat(context.hasImage()).isFalse();
            assertThat(context.isValid()).isTrue();
        }

        @Test
        @DisplayName("forImageGeneration should include image data")
        void forImageGenerationShouldIncludeImageData() {
            Product product = createTestProduct();
            LayoutOptions options = new LayoutOptions("A4", "modern", 1, true, false);
            
            AgentContext context = AgentContext.forImageGeneration(
                    List.of(product),
                    options,
                    "Match this style",
                    "base64data",
                    "image/png"
            );
            
            assertThat(context.hasImage()).isTrue();
            assertThat(context.imageBase64()).isEqualTo("base64data");
            assertThat(context.imageMimeType()).isEqualTo("image/png");
        }

        @Test
        @DisplayName("withAssembledPrompt should return new context")
        void withAssembledPromptShouldReturnNewContext() {
            AgentContext original = AgentContext.forTextGeneration(
                    List.of(createTestProduct()),
                    null,
                    "Test"
            );
            
            AgentContext updated = original.withAssembledPrompt("Assembled prompt content");
            
            assertThat(updated.assembledPrompt()).isEqualTo("Assembled prompt content");
            assertThat(original.assembledPrompt()).isNull(); // Original unchanged
            assertThat(updated.pipelineId()).isEqualTo(original.pipelineId());
        }

        @Test
        @DisplayName("withValidationErrors should track errors")
        void withValidationErrorsShouldTrackErrors() {
            AgentContext context = AgentContext.forTextGeneration(
                    List.of(createTestProduct()),
                    null,
                    "Test"
            );
            
            assertThat(context.isValid()).isTrue();
            
            AgentContext withErrors = context.withValidationErrors(List.of("Error 1", "Error 2"));
            
            assertThat(withErrors.isValid()).isFalse();
            assertThat(withErrors.validationErrors()).containsExactly("Error 1", "Error 2");
        }

        @Test
        @DisplayName("withIncrementedRetry should increment counter")
        void withIncrementedRetryShouldIncrementCounter() {
            AgentContext context = AgentContext.forTextGeneration(
                    List.of(createTestProduct()),
                    null,
                    "Test"
            );
            
            assertThat(context.retryCount()).isEqualTo(0);
            
            AgentContext retry1 = context.withIncrementedRetry();
            AgentContext retry2 = retry1.withIncrementedRetry();
            
            assertThat(retry1.retryCount()).isEqualTo(1);
            assertThat(retry2.retryCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("withMetadata should add metadata")
        void withMetadataShouldAddMetadata() {
            AgentContext context = AgentContext.forTextGeneration(
                    List.of(createTestProduct()),
                    null,
                    "Test"
            );
            
            AgentContext withMeta = context
                    .withMetadata("key1", "value1")
                    .withMetadata("key2", 42);
            
            assertThat(withMeta.metadata()).containsEntry("key1", "value1");
            assertThat(withMeta.metadata()).containsEntry("key2", 42);
        }

        @Test
        @DisplayName("withImageAnalysis should set analysis result")
        void withImageAnalysisShouldSetAnalysisResult() {
            AgentContext context = AgentContext.forImageGeneration(
                    List.of(createTestProduct()),
                    null,
                    "Test",
                    "base64",
                    "image/jpeg"
            );
            
            ImageAnalysisResult analysis = new ImageAnalysisResult(
                    new ColorPalette("#111", "#222", "#333", "#EEE", "#000"),
                    new MoodAnalysis("professional", 0.9, List.of("clean")),
                    new LayoutHints("modular", "medium", "center", 2)
            );
            
            AgentContext withAnalysis = context.withImageAnalysis(analysis);
            
            assertThat(withAnalysis.imageAnalysis()).isEqualTo(analysis);
            assertThat(context.imageAnalysis()).isNull(); // Original unchanged
        }
    }

    private static Product createTestProduct() {
        return new Product(
                1L,
                "Test Product",
                "Short desc",
                "Description",
                "Long description",
                "Test Category",
                "Test Series",
                new TechnicalData(Map.of("power", "500kW")),
                List.of("Highlight 1"),
                "http://example.com/image.jpg",
                99999
        );
    }
}
