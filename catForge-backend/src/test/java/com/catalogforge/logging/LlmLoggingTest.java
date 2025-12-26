package com.catalogforge.logging;

import com.catalogforge.util.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based and unit tests for LLM logging system.
 */
class LlmLoggingTest {

    @Nested
    @DisplayName("Property 14: LLM Log Correlation")
    class LlmLogCorrelationTests {

        @Property(tries = 50)
        @Label("Request and response should share the same requestId")
        void requestAndResponseShouldShareRequestId(
                @ForAll("validRequestId") String requestId,
                @ForAll("modelName") String model
        ) {
            LlmLogEntry request = LlmLogEntry.request(requestId, model, "/v1/generate", "Test prompt", "Full test prompt content");
            LlmLogEntry response = LlmLogEntry.successResponse(requestId, model, 100, 50, 1000, "Response", "Full response content");
            
            assertThat(request.requestId()).isEqualTo(response.requestId());
            assertThat(request.direction()).isEqualTo(LlmLogEntry.Direction.REQUEST);
            assertThat(response.direction()).isEqualTo(LlmLogEntry.Direction.RESPONSE);
        }

        @Property(tries = 50)
        @Label("Error responses should preserve requestId")
        void errorResponsesShouldPreserveRequestId(
                @ForAll("validRequestId") String requestId,
                @ForAll("modelName") String model,
                @ForAll @StringLength(min = 1, max = 200) String errorMessage
        ) {
            LlmLogEntry request = LlmLogEntry.request(requestId, model, "/v1/generate", "Test", "Full test prompt");
            LlmLogEntry error = LlmLogEntry.errorResponse(requestId, model, 500, errorMessage);
            
            assertThat(request.requestId()).isEqualTo(error.requestId());
            assertThat(error.status()).isEqualTo(LlmLogEntry.Status.ERROR);
            assertThat(error.errorMessage()).isEqualTo(errorMessage);
        }

        @Property(tries = 30)
        @Label("Timeout responses should have TIMEOUT status")
        void timeoutResponsesShouldHaveTimeoutStatus(
                @ForAll("validRequestId") String requestId,
                @ForAll("modelName") String model,
                @ForAll @LongRange(min = 1000, max = 120000) long durationMs
        ) {
            LlmLogEntry timeout = LlmLogEntry.timeoutResponse(requestId, model, durationMs);
            
            assertThat(timeout.status()).isEqualTo(LlmLogEntry.Status.TIMEOUT);
            assertThat(timeout.durationMs()).isEqualTo(durationMs);
            assertThat(timeout.errorMessage()).contains("timeout");
        }

        @Provide
        Arbitrary<String> validRequestId() {
            return Arbitraries.create(() -> UUID.randomUUID().toString());
        }

        @Provide
        Arbitrary<String> modelName() {
            return Arbitraries.of(
                    "gemini-2.0-flash", "gemini-2.5-flash", 
                    "gemini-2.5-pro", "gemini-2.5-pro-vision"
            );
        }
    }

    @Nested
    @DisplayName("Property 15: Log File Format")
    class LogFileFormatTests {

        @TempDir
        Path tempDir;

        @Property(tries = 20)
        @Label("Log entries should serialize to valid JSON lines")
        void logEntriesShouldSerializeToValidJsonLines(
                @ForAll("validRequestId") String requestId,
                @ForAll("modelName") String model,
                @ForAll @IntRange(min = 1, max = 10000) int inputTokens,
                @ForAll @IntRange(min = 1, max = 5000) int outputTokens
        ) {
            LlmLogEntry entry = LlmLogEntry.successResponse(
                    requestId, model, inputTokens, outputTokens, 1000, "Test response", "Full test response content"
            );
            
            String json = JsonUtils.toJson(entry);
            
            // Should be valid JSON
            assertThat(json).isNotBlank();
            assertThat(json).doesNotContain("\n"); // Single line
            
            // Should be parseable back
            Map<String, Object> parsed = JsonUtils.fromJson(json, new TypeReference<>() {});
            assertThat(parsed).containsKey("requestId");
            assertThat(parsed.get("requestId")).isEqualTo(requestId);
        }

        @Test
        @DisplayName("LlmLogWriter should create daily log files")
        void llmLogWriterShouldCreateDailyLogFiles() throws IOException {
            LlmLogWriter writer = new LlmLogWriter(tempDir.toString());
            
            String requestId = UUID.randomUUID().toString();
            LlmLogEntry entry = LlmLogEntry.request(requestId, "gemini-2.0-flash", "/v1/generate", "Test", "Full test prompt");
            
            writer.write(entry);
            writer.shutdown();
            
            // Check file was created with correct name
            String expectedFileName = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "_llm.jsonl";
            Path logFile = tempDir.resolve(expectedFileName);
            
            assertThat(Files.exists(logFile)).isTrue();
            
            // Check content is valid JSONL
            List<String> lines = Files.readAllLines(logFile);
            assertThat(lines).hasSize(1);
            
            Map<String, Object> parsed = JsonUtils.fromJson(lines.get(0), new TypeReference<>() {});
            assertThat(parsed.get("requestId")).isEqualTo(requestId);
        }

        @Test
        @DisplayName("Multiple entries should append to same file")
        void multipleEntriesShouldAppendToSameFile() throws IOException {
            LlmLogWriter writer = new LlmLogWriter(tempDir.toString());
            
            // Write multiple entries
            for (int i = 0; i < 5; i++) {
                String requestId = UUID.randomUUID().toString();
                LlmLogEntry entry = LlmLogEntry.request(requestId, "gemini-2.0-flash", "/v1/generate", "Test " + i, "Full test prompt " + i);
                writer.write(entry);
            }
            writer.shutdown();
            
            // Check all entries are in the file
            Path logFile = writer.getCurrentLogFile();
            List<String> lines = Files.readAllLines(logFile);
            
            assertThat(lines).hasSize(5);
            
            // Each line should be valid JSON
            for (String line : lines) {
                Map<String, Object> parsed = JsonUtils.fromJson(line, new TypeReference<>() {});
                assertThat(parsed).containsKey("requestId");
                assertThat(parsed).containsKey("timestamp");
                assertThat(parsed).containsKey("direction");
            }
        }

        @Provide
        Arbitrary<String> validRequestId() {
            return Arbitraries.create(() -> UUID.randomUUID().toString());
        }

        @Provide
        Arbitrary<String> modelName() {
            return Arbitraries.of("gemini-2.0-flash", "gemini-2.5-flash", "gemini-2.5-pro");
        }
    }

    @Nested
    @DisplayName("LlmLogEntry Unit Tests")
    class LlmLogEntryUnitTests {

        @Test
        @DisplayName("Request entry should have correct direction")
        void requestEntryShouldHaveCorrectDirection() {
            LlmLogEntry entry = LlmLogEntry.request("req-1", "gemini-2.0-flash", "/v1/generate", "Hello", "Full hello prompt");
            
            assertThat(entry.direction()).isEqualTo(LlmLogEntry.Direction.REQUEST);
            assertThat(entry.status()).isNull();
            assertThat(entry.promptSummary()).isEqualTo("Hello");
            assertThat(entry.promptFull()).isEqualTo("Full hello prompt");
        }

        @Test
        @DisplayName("Success response should have token counts")
        void successResponseShouldHaveTokenCounts() {
            LlmLogEntry entry = LlmLogEntry.successResponse("req-1", "gemini-2.0-flash", 100, 50, 1500, "Response text", "Full response text content");
            
            assertThat(entry.direction()).isEqualTo(LlmLogEntry.Direction.RESPONSE);
            assertThat(entry.status()).isEqualTo(LlmLogEntry.Status.SUCCESS);
            assertThat(entry.inputTokens()).isEqualTo(100);
            assertThat(entry.outputTokens()).isEqualTo(50);
            assertThat(entry.durationMs()).isEqualTo(1500);
            assertThat(entry.responseSummary()).isEqualTo("Response text");
            assertThat(entry.responseFull()).isEqualTo("Full response text content");
        }

        @Test
        @DisplayName("Error response should have error message")
        void errorResponseShouldHaveErrorMessage() {
            LlmLogEntry entry = LlmLogEntry.errorResponse("req-1", "gemini-2.0-flash", 500, "API rate limit exceeded");
            
            assertThat(entry.status()).isEqualTo(LlmLogEntry.Status.ERROR);
            assertThat(entry.errorMessage()).isEqualTo("API rate limit exceeded");
        }

        @Test
        @DisplayName("Timestamp should be set automatically")
        void timestampShouldBeSetAutomatically() {
            Instant before = Instant.now();
            LlmLogEntry entry = LlmLogEntry.request("req-1", "gemini-2.0-flash", "/v1/generate", "Test", "Full test prompt");
            Instant after = Instant.now();
            
            assertThat(entry.timestamp()).isAfterOrEqualTo(before);
            assertThat(entry.timestamp()).isBeforeOrEqualTo(after);
        }

        @Test
        @DisplayName("withMetadata should create new entry with metadata")
        void withMetadataShouldCreateNewEntryWithMetadata() {
            LlmLogEntry original = LlmLogEntry.request("req-1", "gemini-2.0-flash", "/v1/generate", "Test", "Full test prompt");
            LlmLogEntry withMeta = original.withMetadata(Map.of("pipelineId", "pipe-123", "step", "layout"));
            
            assertThat(withMeta.requestId()).isEqualTo(original.requestId());
            assertThat(withMeta.metadata()).containsEntry("pipelineId", "pipe-123");
            assertThat(original.metadata()).isNull(); // Original unchanged
        }
    }

    @Nested
    @DisplayName("LlmInteractionLogger Unit Tests")
    class LlmInteractionLoggerUnitTests {

        @TempDir
        Path tempDir;

        private LlmInteractionLogger logger;
        private LlmLogWriter writer;

        @BeforeEach
        void setUp() {
            writer = new LlmLogWriter(tempDir.toString());
            logger = new LlmInteractionLogger(writer);
        }

        @Test
        @DisplayName("generateRequestId should return unique UUIDs")
        void generateRequestIdShouldReturnUniqueUuids() {
            String id1 = logger.generateRequestId();
            String id2 = logger.generateRequestId();
            
            assertThat(id1).isNotEqualTo(id2);
            assertThat(id1).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
        }

        @Test
        @DisplayName("logRequest and logResponse should correlate")
        void logRequestAndLogResponseShouldCorrelate() throws IOException {
            String requestId = logger.generateRequestId();
            
            logger.logRequest(requestId, "gemini-2.0-flash", "/v1/generate", "Test prompt");
            
            // Simulate some processing time
            try { Thread.sleep(10); } catch (InterruptedException ignored) {}
            
            logger.logResponse(requestId, "gemini-2.0-flash", "Response text", 100, 50);
            
            writer.shutdown();
            
            // Verify log file
            List<String> lines = Files.readAllLines(writer.getCurrentLogFile());
            assertThat(lines).hasSize(2);
            
            Map<String, Object> request = JsonUtils.fromJson(lines.get(0), new TypeReference<>() {});
            Map<String, Object> response = JsonUtils.fromJson(lines.get(1), new TypeReference<>() {});
            
            assertThat(request.get("requestId")).isEqualTo(requestId);
            assertThat(response.get("requestId")).isEqualTo(requestId);
            assertThat(request.get("direction")).isEqualTo("REQUEST");
            assertThat(response.get("direction")).isEqualTo("RESPONSE");
        }

        @Test
        @DisplayName("logError should record error details")
        void logErrorShouldRecordErrorDetails() throws IOException {
            String requestId = logger.generateRequestId();
            
            logger.logRequest(requestId, "gemini-2.0-flash", "/v1/generate", "Test");
            logger.logError(requestId, "gemini-2.0-flash", new RuntimeException("Connection failed"));
            
            writer.shutdown();
            
            List<String> lines = Files.readAllLines(writer.getCurrentLogFile());
            Map<String, Object> error = JsonUtils.fromJson(lines.get(1), new TypeReference<>() {});
            
            assertThat(error.get("status")).isEqualTo("ERROR");
            assertThat(error.get("errorMessage")).isEqualTo("Connection failed");
        }
    }
}
