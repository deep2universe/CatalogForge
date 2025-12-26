# CatalogForge Backend Dockerfile
# Multi-stage build: Gradle build + Runtime with Java 21, Node.js, Chromium

# =============================================================================
# Stage 1: Build the Spring Boot application
# =============================================================================
FROM --platform=$BUILDPLATFORM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# Copy Gradle wrapper and build files
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY gradle.properties .

# Make gradlew executable
RUN chmod +x gradlew

# Download dependencies (cached layer)
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src src

# Build the application (skip tests for faster build)
RUN ./gradlew bootJar --no-daemon -x test

# =============================================================================
# Stage 2: Runtime image with Java, Node.js, and Playwright
# =============================================================================
FROM mcr.microsoft.com/playwright:v1.49.1-jammy

# Install Java 21 JRE
RUN apt-get update && apt-get install -y \
    curl \
    gnupg \
    ca-certificates \
    wget \
    && wget -qO - https://packages.adoptium.net/artifactory/api/gpg/key/public | gpg --dearmor -o /usr/share/keyrings/adoptium.gpg \
    && echo "deb [signed-by=/usr/share/keyrings/adoptium.gpg] https://packages.adoptium.net/artifactory/deb jammy main" > /etc/apt/sources.list.d/adoptium.list \
    && apt-get update && apt-get install -y temurin-21-jre \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

ENV JAVA_HOME=/usr/lib/jvm/temurin-21-jre

WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Copy PDF generator scripts
COPY scripts scripts

# Install Node.js dependencies for PDF generator (skip Chromium download)
WORKDIR /app/scripts
RUN npm install --omit=dev
WORKDIR /app

# Create directories for logs and temp files
RUN mkdir -p /app/logs/llm /app/logs/application /tmp/catalogforge/pdf

# Create non-root user for security
RUN groupadd -r catalogforge && useradd -r -g catalogforge catalogforge
RUN chown -R catalogforge:catalogforge /app /tmp/catalogforge
USER catalogforge

# Expose port
EXPOSE 8080

# Health check - use simple endpoint instead of actuator
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/api/v1/products/categories || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
