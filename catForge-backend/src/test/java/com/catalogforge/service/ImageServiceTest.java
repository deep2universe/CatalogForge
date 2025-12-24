package com.catalogforge.service;

import com.catalogforge.exception.ImageUploadException;
import com.catalogforge.exception.ResourceNotFoundException;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.*;

/**
 * Property-based and unit tests for ImageService.
 */
class ImageServiceTest {

    private ImageService imageService;

    @BeforeEach
    void setUp() {
        imageService = new ImageService("http://localhost:8080");
    }

    @Nested
    @DisplayName("Property 10: Image Upload Round-Trip")
    class ImageUploadRoundTripTests {

        @Property(tries = 30)
        @Label("Uploaded image should be retrievable")
        void uploadedImageShouldBeRetrievable(
                @ForAll @ByteRange(min = 1, max = 127) byte[] data,
                @ForAll("validMimeType") String mimeType
        ) {
            // Ensure minimum data size
            if (data.length < 10) return;
            
            String base64 = Base64.getEncoder().encodeToString(data);
            
            String imageId = imageService.upload(base64, mimeType, "test.jpg");
            
            assertThat(imageId).isNotBlank();
            assertThat(imageService.exists(imageId)).isTrue();
            
            ImageService.StoredImage retrieved = imageService.getImage(imageId);
            assertThat(retrieved.data()).isEqualTo(data);
            assertThat(retrieved.mimeType()).isEqualTo(mimeType);
        }

        @Property(tries = 20)
        @Label("Generated URL should contain image ID")
        void generatedUrlShouldContainImageId(
                @ForAll @ByteRange(min = 1, max = 127) byte[] data
        ) {
            if (data.length < 10) return;
            
            String base64 = Base64.getEncoder().encodeToString(data);
            String imageId = imageService.upload(base64, "image/jpeg", "test.jpg");
            
            String url = imageService.generateUrl(imageId);
            
            assertThat(url).contains(imageId);
            assertThat(url).startsWith("http://localhost:8080/api/v1/images/");
        }

        @Property(tries = 20)
        @Label("Deleted image should not be retrievable")
        void deletedImageShouldNotBeRetrievable(
                @ForAll @ByteRange(min = 1, max = 127) byte[] data
        ) {
            if (data.length < 10) return;
            
            String base64 = Base64.getEncoder().encodeToString(data);
            String imageId = imageService.upload(base64, "image/png", "test.png");
            
            assertThat(imageService.exists(imageId)).isTrue();
            
            imageService.delete(imageId);
            
            assertThat(imageService.exists(imageId)).isFalse();
            assertThatThrownBy(() -> imageService.getImage(imageId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Provide
        Arbitrary<String> validMimeType() {
            return Arbitraries.of("image/jpeg", "image/png", "image/webp");
        }
    }

    @Nested
    @DisplayName("Image Validation Tests")
    class ImageValidationTests {

        @Test
        @DisplayName("Should reject invalid MIME types")
        void shouldRejectInvalidMimeTypes() {
            String base64 = Base64.getEncoder().encodeToString(new byte[100]);
            
            assertThatThrownBy(() -> imageService.upload(base64, "image/gif", "test.gif"))
                    .isInstanceOf(ImageUploadException.class)
                    .hasMessageContaining("Invalid image type");
            
            assertThatThrownBy(() -> imageService.upload(base64, "application/pdf", "test.pdf"))
                    .isInstanceOf(ImageUploadException.class);
        }

        @Test
        @DisplayName("Should accept valid MIME types")
        void shouldAcceptValidMimeTypes() {
            String base64 = Base64.getEncoder().encodeToString(new byte[100]);
            
            assertThatCode(() -> imageService.upload(base64, "image/jpeg", "test.jpg"))
                    .doesNotThrowAnyException();
            
            assertThatCode(() -> imageService.upload(base64, "image/png", "test.png"))
                    .doesNotThrowAnyException();
            
            assertThatCode(() -> imageService.upload(base64, "image/webp", "test.webp"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should generate unique IDs for each upload")
        void shouldGenerateUniqueIds() {
            String base64 = Base64.getEncoder().encodeToString(new byte[100]);
            
            String id1 = imageService.upload(base64, "image/jpeg", "test1.jpg");
            String id2 = imageService.upload(base64, "image/jpeg", "test2.jpg");
            String id3 = imageService.upload(base64, "image/jpeg", "test3.jpg");
            
            assertThat(id1).isNotEqualTo(id2);
            assertThat(id2).isNotEqualTo(id3);
            assertThat(id1).isNotEqualTo(id3);
        }
    }

    @Nested
    @DisplayName("Image Retrieval Tests")
    class ImageRetrievalTests {

        @Test
        @DisplayName("Should return image as Base64")
        void shouldReturnImageAsBase64() {
            byte[] originalData = "test image data".getBytes();
            String base64 = Base64.getEncoder().encodeToString(originalData);
            
            String imageId = imageService.upload(base64, "image/jpeg", "test.jpg");
            String retrievedBase64 = imageService.getImageBase64(imageId);
            
            assertThat(retrievedBase64).isEqualTo(base64);
        }

        @Test
        @DisplayName("Should throw for non-existent image")
        void shouldThrowForNonExistentImage() {
            assertThatThrownBy(() -> imageService.getImage("non-existent-id"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should track image count")
        void shouldTrackImageCount() {
            String base64 = Base64.getEncoder().encodeToString(new byte[100]);
            
            int initialCount = imageService.getImageCount();
            
            imageService.upload(base64, "image/jpeg", "test1.jpg");
            imageService.upload(base64, "image/png", "test2.png");
            
            assertThat(imageService.getImageCount()).isEqualTo(initialCount + 2);
        }
    }
}
