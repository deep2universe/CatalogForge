package com.catalogforge.logging;

import com.catalogforge.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Writes LLM log entries to daily JSONL files.
 * Thread-safe implementation with automatic date-based file rotation.
 */
@Component
public class LlmLogWriter {

    private static final Logger log = LoggerFactory.getLogger(LlmLogWriter.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String FILE_SUFFIX = "_llm.jsonl";

    private final Path logDirectory;
    private final ReentrantLock lock = new ReentrantLock();
    
    private BufferedWriter currentWriter;
    private LocalDate currentDate;
    private Path currentFilePath;

    public LlmLogWriter(@Value("${catalogforge.logging.llm-dir:logs/llm}") String logDir) {
        this.logDirectory = Path.of(logDir);
        initializeDirectory();
    }

    private void initializeDirectory() {
        try {
            Files.createDirectories(logDirectory);
            log.info("LLM log directory initialized: {}", logDirectory.toAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to create LLM log directory: {}", logDirectory, e);
        }
    }

    /**
     * Writes a log entry to the current day's JSONL file.
     * 
     * @param entry The log entry to write
     */
    public void write(LlmLogEntry entry) {
        lock.lock();
        try {
            ensureWriterForToday();
            if (currentWriter != null) {
                String json = JsonUtils.toJson(entry);
                currentWriter.write(json);
                currentWriter.newLine();
                currentWriter.flush();
            }
        } catch (IOException e) {
            log.error("Failed to write LLM log entry: {}", entry.requestId(), e);
        } finally {
            lock.unlock();
        }
    }

    private void ensureWriterForToday() throws IOException {
        LocalDate today = LocalDate.now();
        
        if (currentWriter == null || !today.equals(currentDate)) {
            closeCurrentWriter();
            currentDate = today;
            currentFilePath = logDirectory.resolve(today.format(DATE_FORMAT) + FILE_SUFFIX);
            currentWriter = Files.newBufferedWriter(
                    currentFilePath,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
            log.debug("Opened new LLM log file: {}", currentFilePath);
        }
    }

    private void closeCurrentWriter() {
        if (currentWriter != null) {
            try {
                currentWriter.close();
                log.debug("Closed LLM log file: {}", currentFilePath);
            } catch (IOException e) {
                log.error("Failed to close LLM log file", e);
            }
            currentWriter = null;
        }
    }

    /**
     * Returns the path to today's log file.
     */
    public Path getCurrentLogFile() {
        return logDirectory.resolve(LocalDate.now().format(DATE_FORMAT) + FILE_SUFFIX);
    }

    /**
     * Returns the log directory path.
     */
    public Path getLogDirectory() {
        return logDirectory;
    }

    @PreDestroy
    public void shutdown() {
        lock.lock();
        try {
            closeCurrentWriter();
        } finally {
            lock.unlock();
        }
    }
}
