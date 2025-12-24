package com.catalogforge.gemini;

import net.jqwik.api.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for GeminiRequest including property-based tests.
 * 
 * Property Tests validate:
 * - Property 11: Gemini Request Structure (Requirements 8.1, 8.5)
 */
class GeminiRequestTest {

    @Nested
    @DisplayName("Property 11: Gemini Request Structure")
    class GeminiRequestStructureTests {

        @Test
        @DisplayName("Builder should create valid request with user prompt")
        void builderShouldCreateValidRequest() {
            GeminiRequest request = GeminiRequest.builder()
                    .userPrompt("Generate a product layout")
                    .build();

            assertThat(request.contents()).hasSize(1);
            assertThat(request.contents().get(0).role()).isEqualTo("user");
            assertThat(request.contents().get(0).parts()).hasSize(1);
            assertThat(request.contents().get(0).parts().get(0).text())
                    .isEqualTo("Generate a product layout");
        }

        @Test
        @DisplayName("Builder should include system instruction when provided")
        void builderShouldIncludeSystemInstruction() {
            GeminiRequest request = GeminiRequest.builder()
                    .userPrompt("Generate layout")
                    .systemInstruction("You are a layout designer")
                    .build();

            assertThat(request.systemInstruction()).isNotNull();
            assertThat(request.systemInstruction().parts()).hasSize(1);
            assertThat(request.systemInstruction().parts().get(0).text())
                    .isEqualTo("You are a layout designer");
        }

        @Test
        @DisplayName("Builder should include generation config")
        void builderShouldIncludeGenerationConfig() {
            GeminiRequest request = GeminiRequest.builder()
                    .userPrompt("Generate layout")
                    .temperature(0.5)
                    .maxOutputTokens(4096)
                    .responseMimeType("application/json")
                    .build();

            assertThat(request.generationConfig()).isNotNull();
            assertThat(request.generationConfig().temperature()).isEqualTo(0.5);
            assertThat(request.generationConfig().maxOutputTokens()).isEqualTo(4096);
            assertThat(request.generationConfig().responseMimeType()).isEqualTo("application/json");
        }

        @Test
        @DisplayName("Builder should include response schema when provided")
        void builderShouldIncludeResponseSchema() {
            Map<String, Object> schema = Map.of(
                    "type", "object",
                    "properties", Map.of("html", Map.of("type", "string"))
            );

            GeminiRequest request = GeminiRequest.builder()
                    .userPrompt("Generate layout")
                    .responseSchema(schema)
                    .build();

            assertThat(request.generationConfig().responseSchema()).isEqualTo(schema);
        }

        @Test
        @DisplayName("Builder should include image data when provided")
        void builderShouldIncludeImageData() {
            GeminiRequest request = GeminiRequest.builder()
                    .userPrompt("Analyze this image")
                    .image("base64data", "image/jpeg")
                    .build();

            assertThat(request.contents().get(0).parts()).hasSize(2);
            
            // First part should be image
            GeminiRequest.Part imagePart = request.contents().get(0).parts().get(0);
            assertThat(imagePart.inlineData()).isNotNull();
            assertThat(imagePart.inlineData().mimeType()).isEqualTo("image/jpeg");
            assertThat(imagePart.inlineData().data()).isEqualTo("base64data");
            
            // Second part should be text
            GeminiRequest.Part textPart = request.contents().get(0).parts().get(1);
            assertThat(textPart.text()).isEqualTo("Analyze this image");
        }

        @Property
        @Label("Request should always have contents")
        void requestShouldAlwaysHaveContents(
                @ForAll("userPrompts") String prompt) {
            
            GeminiRequest request = GeminiRequest.builder()
                    .userPrompt(prompt)
                    .build();

            assertThat(request.contents()).isNotNull();
            assertThat(request.contents()).isNotEmpty();
        }

        @Provide
        Arbitrary<String> userPrompts() {
            return Arbitraries.strings()
                    .withCharRange('a', 'z')
                    .ofMinLength(1)
                    .ofMaxLength(100);
        }
    }

    @Nested
    @DisplayName("Part Factory Methods")
    class PartFactoryMethodsTests {

        @Test
        @DisplayName("Part.text should create text part")
        void partTextShouldCreateTextPart() {
            GeminiRequest.Part part = GeminiRequest.Part.text("Hello");
            
            assertThat(part.text()).isEqualTo("Hello");
            assertThat(part.inlineData()).isNull();
        }

        @Test
        @DisplayName("Part.image should create image part")
        void partImageShouldCreateImagePart() {
            GeminiRequest.Part part = GeminiRequest.Part.image("image/png", "base64data");
            
            assertThat(part.text()).isNull();
            assertThat(part.inlineData()).isNotNull();
            assertThat(part.inlineData().mimeType()).isEqualTo("image/png");
            assertThat(part.inlineData().data()).isEqualTo("base64data");
        }
    }
}
