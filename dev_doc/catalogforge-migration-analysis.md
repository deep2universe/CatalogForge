# CatalogForge Backend - Migrationsanalyse
## Java 21 → 25 & Spring Boot 3.4.1 → 4.0.1

**Analysedatum:** 26. Dezember 2025  
**Analysiert von:** Claude AI (Anthropic)  
**Ziel-Stack:** Java 25 LTS + Spring Boot 4.0.1 + Gradle 9.x

---

## Inhaltsverzeichnis

1. [Executive Summary](#1-executive-summary)
2. [Aktuelle Architektur](#2-aktuelle-architektur)
3. [Code-Inventar](#3-code-inventar)
4. [Kritische Breaking Changes](#4-kritische-breaking-changes)
5. [Detaillierte Code-Analyse](#5-detaillierte-code-analyse)
6. [Migrationsaufwand pro Bereich](#6-migrationsaufwand-pro-bereich)
7. [Internet-Recherche Dokumentation](#7-internet-recherche-dokumentation)
8. [Migrations-Roadmap](#8-migrations-roadmap)
9. [Risikobewertung](#9-risikobewertung)
10. [Empfehlungen](#10-empfehlungen)

---

## 1. Executive Summary

### Gesamtbewertung: 🟡 MODERATER AUFWAND

| Bereich | Aufwand | Komplexität | Risiko |
|---------|---------|-------------|--------|
| Jackson 3.x Migration | **Hoch** | Mittel | Niedrig |
| Test-Framework Anpassungen | **Mittel** | Niedrig | Niedrig |
| Starter-Umbenennung | **Niedrig** | Niedrig | Niedrig |
| Java 25 Kompatibilität | **Niedrig** | Niedrig | Mittel |
| Gradle 9.x Update | **Niedrig** | Niedrig | Niedrig |

### Geschätzter Gesamtaufwand

| Phase | Dauer | Entwickler |
|-------|-------|------------|
| Vorbereitung & Analyse | 2-3 Tage | 1 |
| Jackson 3 Migration | 3-5 Tage | 1-2 |
| Test-Anpassungen | 2-3 Tage | 1 |
| Gradle & Starter Updates | 1 Tag | 1 |
| Integration & Testing | 3-5 Tage | 1-2 |
| **Gesamt** | **11-17 Tage** | **1-2** |

### Positive Befunde

✅ **ConfigurationProperties als Records** - Alle 6 Properties-Klassen nutzen bereits Java Records, was perfekt mit Spring Boot 4.0 kompatibel ist.

✅ **Keine public fields in @ConfigurationProperties** - Die verwendeten Records haben keine public field binding Probleme.

✅ **WebFlux bereits integriert** - WebClient-Nutzung ist bereits vorhanden und bleibt funktional.

✅ **Saubere Projektstruktur** - Klare Trennung von Schichten erleichtert die Migration.

---

## 2. Aktuelle Architektur

### Tech-Stack (Ist-Zustand)

```
┌─────────────────────────────────────────────────────────────┐
│ Java 21 LTS + Spring Boot 3.4.1 + Gradle 8.11.1             │
├─────────────────────────────────────────────────────────────┤
│ Dependencies:                                                │
│ • spring-boot-starter-web                                    │
│ • spring-boot-starter-webflux                                │
│ • spring-boot-starter-validation                             │
│ • com.fasterxml.jackson.core:jackson-databind                │
│ • com.fasterxml.jackson.datatype:jackson-datatype-jsr310     │
├─────────────────────────────────────────────────────────────┤
│ Test Dependencies:                                           │
│ • spring-boot-starter-test                                   │
│ • junit-jupiter:5.11.3                                       │
│ • mockito-core:5.14.2                                        │
│ • jqwik:1.9.2 (Property-Based Testing)                       │
│ • assertj-core:3.26.3                                        │
└─────────────────────────────────────────────────────────────┘
```

### Ziel-Stack

```
┌─────────────────────────────────────────────────────────────┐
│ Java 25 LTS + Spring Boot 4.0.1 + Gradle 9.x                │
├─────────────────────────────────────────────────────────────┤
│ Dependencies (NEU):                                          │
│ • spring-boot-starter-webmvc          (umbenannt)           │
│ • spring-boot-starter-webflux         (unverändert)         │
│ • spring-boot-starter-validation      (unverändert)         │
│ • spring-boot-starter-jackson         (NEU - modular)       │
│ • tools.jackson.datatype:jackson-datatype-jsr310 (PACKAGE!) │
├─────────────────────────────────────────────────────────────┤
│ Test Dependencies (NEU):                                     │
│ • spring-boot-starter-webmvc-test     (NEU - modular)       │
│ • spring-boot-starter-webflux-test    (NEU - modular)       │
│ • spring-boot-starter-validation-test (NEU - modular)       │
│ • junit-jupiter:6.x                   (optional)            │
└─────────────────────────────────────────────────────────────┘
```

---

## 3. Code-Inventar

### Betroffene Dateien nach Kategorie

#### Jackson-Imports (KRITISCH - 8 Dateien)

| Datei | Betroffene Imports | Aufwand |
|-------|-------------------|---------|
| `JsonUtils.java` | `com.fasterxml.jackson.core.*`, `databind.*` | Hoch |
| `LayoutGenerationStep.java` | `TypeReference` | Niedrig |
| `ProductService.java` | `TypeReference` | Niedrig |
| `SkillLoader.java` | `TypeReference` | Niedrig |
| `PuppeteerBridge.java` | `TypeReference` | Niedrig |
| `TechnicalData.java` | `@JsonCreator`, `@JsonValue` | **Keine Änderung!** |
| `GeminiRequest.java` | Implizit via Jackson | Niedrig |
| `LlmLoggingTest.java` | `TypeReference` | Niedrig |

#### Test-Annotations (MITTEL - 3 Dateien)

| Datei | Betroffene Annotations | Aufwand |
|-------|----------------------|---------|
| `ProductControllerTest.java` | `@WebMvcTest`, `@MockBean` | Mittel |
| `SkillsControllerTest.java` | `@WebMvcTest`, `@MockBean` | Mittel |
| `CatalogForgeApplicationTest.java` | `@SpringBootTest` | Niedrig |

#### Property-Based Tests mit jqwik (ÜBERWACHUNG - 6 Dateien)

| Datei | Status |
|-------|--------|
| `AgentFrameworkTest.java` | ⚠️ sun.misc.Unsafe Abhängigkeit prüfen |
| `ErrorResponsePropertyTest.java` | ⚠️ sun.misc.Unsafe Abhängigkeit prüfen |
| `ColorUtilsPropertyTest.java` | ⚠️ sun.misc.Unsafe Abhängigkeit prüfen |
| `CssValidatorPropertyTest.java` | ⚠️ sun.misc.Unsafe Abhängigkeit prüfen |
| `HtmlSanitizerPropertyTest.java` | ⚠️ sun.misc.Unsafe Abhängigkeit prüfen |
| `LlmLoggingTest.java` | ⚠️ sun.misc.Unsafe Abhängigkeit prüfen |

#### Configuration Properties (KEINE ÄNDERUNG - 6 Dateien)

| Datei | Record-basiert? | Status |
|-------|----------------|--------|
| `GeminiProperties.java` | ✅ Ja | Kompatibel |
| `ImageProperties.java` | ✅ Ja | Kompatibel |
| `LayoutProperties.java` | ✅ Ja | Kompatibel |
| `LoggingProperties.java` | ✅ Ja | Kompatibel |
| `PuppeteerProperties.java` | ✅ Ja | Kompatibel |
| `SkillsProperties.java` | ✅ Ja | Kompatibel |

---

## 4. Kritische Breaking Changes

### 4.1 Jackson 3.x Package-Umstellung

**Quelle:** [Jackson 3 Migration Guide](https://github.com/FasterXML/jackson/blob/main/jackson3/MIGRATING_TO_JACKSON_3.md)

> "Replace com.fasterxml.jackson. with tools.jackson. everywhere"
> — Jackson Team, Oktober 2025

#### Import-Änderungen

```java
// ❌ VORHER (Jackson 2.x)
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

// ✅ NACHHER (Jackson 3.x)
import tools.jackson.core.JacksonException;  // Umbenannt!
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;  // oder JsonMapper
import tools.jackson.databind.SerializationFeature;
import tools.jackson.datatype.jsr310.JavaTimeModule;
```

#### AUSNAHME: Jackson Annotations bleiben unverändert!

```java
// ✅ Diese Imports bleiben GLEICH - KEINE ÄNDERUNG NÖTIG!
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonProperty;
```

**Wichtig für CatalogForge:** Die Datei `TechnicalData.java` verwendet `@JsonCreator` und `@JsonValue` - diese müssen **NICHT** geändert werden!

#### Jackson 3 Default-Änderungen

| Feature | Jackson 2.x | Jackson 3.x |
|---------|-------------|-------------|
| `FAIL_ON_UNKNOWN_PROPERTIES` | `true` | **`false`** ✅ |
| `FAIL_ON_TRAILING_TOKENS` | `false` | **`true`** |
| `USE_STD_BEAN_NAMING` | `false` | **`true`** |

**Für CatalogForge:** Die `application.yml` setzt bereits:
```yaml
spring:
  jackson:
    deserialization:
      fail-on-unknown-properties: false  # Entspricht Jackson 3 Default
```

### 4.2 Spring Boot 4.0 Starter-Modularisierung

**Quelle:** [Spring Boot 4.0 Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide)

> "Spring Boot 4.0 has a new modular design and now ships smaller focused modules rather than several large jars."
> — Spring Team, Oktober 2025

#### Starter-Umbenennungen

| Alt (3.4.x) | Neu (4.0.x) | CatalogForge betroffen? |
|-------------|-------------|------------------------|
| `spring-boot-starter-web` | `spring-boot-starter-webmvc` | ✅ Ja |
| `spring-boot-starter-test` | + `spring-boot-starter-webmvc-test` | ✅ Ja |
| N/A | `spring-boot-starter-jackson` | ✅ Neu hinzufügen |

#### Test-Starter (NEU ERFORDERLICH)

```kotlin
// build.gradle.kts - VORHER
testImplementation("org.springframework.boot:spring-boot-starter-test")

// build.gradle.kts - NACHHER
testImplementation("org.springframework.boot:spring-boot-starter-test")
testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
testImplementation("org.springframework.boot:spring-boot-starter-webflux-test")
testImplementation("org.springframework.boot:spring-boot-starter-validation-test")
```

### 4.3 Test-Framework Änderungen

**Quelle:** [Spring Boot Testing Changes](https://rieckpil.de/whats-new-for-testing-in-spring-boot-4-0-and-spring-framework-7/)

#### @MockBean → @MockitoBean

```java
// ❌ VORHER (Spring Boot 3.x)
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(ProductController.class)
class ProductControllerTest {
    @MockBean
    private ProductService productService;
}

// ✅ NACHHER (Spring Boot 4.x)
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(ProductController.class)
class ProductControllerTest {
    @MockitoBean
    private ProductService productService;
}
```

#### @SpringBootTest + MockMVC

```java
// ❌ VORHER (Spring Boot 3.x) - funktionierte automatisch
@SpringBootTest
class ApplicationTests {
    @Autowired
    private MockMvc mockMvc;  // War automatisch konfiguriert
}

// ✅ NACHHER (Spring Boot 4.x) - explizit erforderlich
@SpringBootTest
@AutoConfigureMockMvc  // NEU: Muss explizit hinzugefügt werden!
class ApplicationTests {
    @Autowired
    private MockMvc mockMvc;
}
```

### 4.4 Java 25 sun.misc.Unsafe Deprecation

**Quelle:** [JEP 471](https://openjdk.org/jeps/471)

> "Phase 2 (JDK 25 or earlier): Runtime warnings will be introduced whenever the deprecated methods are used."

#### Betroffene Libraries im CatalogForge

| Library | Version | Unsafe-Nutzung | Workaround |
|---------|---------|----------------|------------|
| jqwik | 1.9.2 | ⚠️ Möglich | JVM Flag |
| Mockito | 5.14.2 | ⚠️ ByteBuddy | JVM Flag |
| Jackson | 2.x/3.x | ⚠️ Afterburner | Deaktivieren |
| Netty (via WebFlux) | 4.x | ⚠️ Ja | JVM Flag |

#### Erforderliches JVM Flag

```kotlin
// build.gradle.kts
tasks.withType<Test> {
    jvmArgs(
        "-XX:+EnableDynamicAgentLoading",      // Für Mockito
        "--sun-misc-unsafe-memory-access=allow" // Für Unsafe-Nutzung
    )
}

tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
    jvmArgs = listOf("--sun-misc-unsafe-memory-access=allow")
}
```

---

## 5. Detaillierte Code-Analyse

### 5.1 JsonUtils.java - Hauptanpassungspunkt

**Pfad:** `src/main/java/com/catalogforge/util/JsonUtils.java`

```java
// AKTUELLER CODE (Zeile 6959-7052)
package com.catalogforge.util;

import com.fasterxml.jackson.core.JsonProcessingException;      // ❌ Ändern
import com.fasterxml.jackson.core.type.TypeReference;           // ❌ Ändern
import com.fasterxml.jackson.databind.DeserializationFeature;   // ❌ Ändern
import com.fasterxml.jackson.databind.ObjectMapper;             // ❌ Ändern
import com.fasterxml.jackson.databind.SerializationFeature;     // ❌ Ändern
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;    // ❌ Ändern

public final class JsonUtils {

    private static final ObjectMapper MAPPER = createObjectMapper();

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return mapper;
    }
    // ...
}
```

**MIGRATION:**

```java
// MIGRIERTER CODE
package com.catalogforge.util;

import tools.jackson.core.JacksonException;                     // ✅ Neu
import tools.jackson.core.type.TypeReference;                   // ✅ Neu
import tools.jackson.databind.DeserializationFeature;           // ✅ Neu
import tools.jackson.databind.ObjectMapper;                     // ✅ Neu
import tools.jackson.databind.SerializationFeature;             // ✅ Neu
import tools.jackson.datatype.jsr310.JavaTimeModule;            // ✅ Neu

// Alternative: Nutzung des neuen JsonMapper (immutable)
import tools.jackson.databind.json.JsonMapper;

public final class JsonUtils {

    private static final ObjectMapper MAPPER = createObjectMapper();

    private static ObjectMapper createObjectMapper() {
        // Option A: Klassischer Weg (weiterhin funktional)
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return mapper;
        
        // Option B: Moderner Weg mit JsonMapper (empfohlen für neue Projekte)
        // return JsonMapper.builder()
        //     .addModule(new JavaTimeModule())
        //     .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        //     .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        //     .build();
    }
    // ...
}
```

### 5.2 TechnicalData.java - KEINE ÄNDERUNG NÖTIG

**Pfad:** `src/main/java/com/catalogforge/model/TechnicalData.java`

```java
// AKTUELLER CODE - BLEIBT UNVERÄNDERT
package com.catalogforge.model;

import com.fasterxml.jackson.annotation.JsonCreator;  // ✅ Bleibt gleich!
import com.fasterxml.jackson.annotation.JsonValue;    // ✅ Bleibt gleich!

import java.util.Map;

public record TechnicalData(Map<String, String> specifications) {
    
    @JsonCreator
    public static TechnicalData fromMap(Map<String, String> specs) {
        return new TechnicalData(specs);
    }

    @JsonValue
    public Map<String, String> specifications() {
        return specifications;
    }
}
```

**ERKLÄRUNG:** Jackson 3.x nutzt die gleichen Annotations aus `com.fasterxml.jackson.annotation` wie Jackson 2.x, um Abwärtskompatibilität zu gewährleisten.

### 5.3 Test-Klassen Migration

#### ProductControllerTest.java

```java
// ❌ VORHER
package com.catalogforge.controller;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;  // DEPRECATED

@WebMvcTest(ProductController.class)
class ProductControllerTest {
    
    @MockBean
    private ProductService productService;
    // ...
}

// ✅ NACHHER
package com.catalogforge.controller;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;  // NEU

@WebMvcTest(ProductController.class)
class ProductControllerTest {
    
    @MockitoBean  // Umbenannt
    private ProductService productService;
    // ...
}
```

#### SkillsControllerTest.java

```java
// ❌ VORHER
@WebMvcTest(SkillsController.class)
class SkillsControllerTest {
    @MockBean
    private SkillsService skillsService;
}

// ✅ NACHHER
@WebMvcTest(SkillsController.class)
class SkillsControllerTest {
    @MockitoBean
    private SkillsService skillsService;
}
```

### 5.4 build.gradle.kts Migration

```kotlin
// ❌ VORHER (Spring Boot 3.4.1)
plugins {
    java
    id("org.springframework.boot") version "3.4.1"
    id("io.spring.dependency-management") version "1.1.7"
    jacoco
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    
    // JSON Processing
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    
    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.mockito:mockito-core:5.14.2")
    testImplementation("net.jqwik:jqwik:1.9.2")
}
```

```kotlin
// ✅ NACHHER (Spring Boot 4.0.1)
plugins {
    java
    id("org.springframework.boot") version "4.0.1"
    id("io.spring.dependency-management") version "1.1.7"
    jacoco
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)  // Aktualisiert
    }
}

dependencies {
    // Spring Boot - Modularisiert
    implementation("org.springframework.boot:spring-boot-starter-webmvc")  // Umbenannt!
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-jackson")  // NEU!
    
    // JSON Processing - Jackson 3.x (wird via Starter gezogen)
    // NICHT MEHR NÖTIG: implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("tools.jackson.datatype:jackson-datatype-jsr310")  // Neues Package!
    
    // Configuration
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    
    // Testing - mit modularen Test-Startern
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")  // NEU!
    testImplementation("org.springframework.boot:spring-boot-starter-webflux-test") // NEU!
    testImplementation("org.springframework.boot:spring-boot-starter-validation-test") // NEU!
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.3")
    testImplementation("org.mockito:mockito-core:5.14.2")
    testImplementation("org.mockito:mockito-junit-jupiter:5.14.2")  // NEU für @ExtendWith
    testImplementation("org.assertj:assertj-core:3.26.3")
    testImplementation("net.jqwik:jqwik:1.9.2")
    testImplementation("org.skyscreamer:jsonassert:1.5.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform {
        includeEngines("junit-jupiter", "jqwik")
    }
    // JVM Flags für Java 25 Kompatibilität
    jvmArgs(
        "-XX:+EnableDynamicAgentLoading",
        "--sun-misc-unsafe-memory-access=allow"
    )
}
```

### 5.5 gradle-wrapper.properties Update

```properties
# ❌ VORHER
distributionUrl=https\://services.gradle.org/distributions/gradle-8.11.1-bin.zip

# ✅ NACHHER
distributionUrl=https\://services.gradle.org/distributions/gradle-9.1.1-bin.zip
```

### 5.6 application.yml Anpassungen

```yaml
# ❌ VORHER (Spring Boot 3.4.x)
spring:
  jackson:
    serialization:
      write-dates-as-timestamps: false
    deserialization:
      fail-on-unknown-properties: false

# ✅ NACHHER (Spring Boot 4.0.x) - Optional für Jackson 2 Kompatibilität
spring:
  jackson:
    use-jackson2-defaults: true  # Optional: Für Übergangsphase
    json:  # Neuer Prefix!
      read:
        fail-on-unknown-properties: false
      write:
        dates-as-timestamps: false
```

---

## 6. Migrationsaufwand pro Bereich

### 6.1 Jackson 3.x Migration

| Aufgabe | Dateien | Geschätzter Aufwand | Komplexität |
|---------|---------|---------------------|-------------|
| Import-Änderungen in JsonUtils.java | 1 | 30 Min | Niedrig |
| Import-Änderungen in Steps/Services | 4 | 1 Std | Niedrig |
| Import-Änderungen in Tests | 1 | 15 Min | Niedrig |
| Dependency-Updates in build.gradle.kts | 1 | 30 Min | Niedrig |
| application.yml Anpassung | 1 | 15 Min | Niedrig |
| **Gesamt Jackson** | **8** | **~3 Std** | **Niedrig** |

**Automatisierungsmöglichkeit:** OpenRewrite Recipe `org.openrewrite.java.jackson.UpgradeJackson_2_3` kann 90% der Änderungen automatisieren.

### 6.2 Test-Framework Migration

| Aufgabe | Dateien | Geschätzter Aufwand | Komplexität |
|---------|---------|---------------------|-------------|
| @MockBean → @MockitoBean | 2 | 30 Min | Niedrig |
| Test-Starter hinzufügen | 1 | 15 Min | Niedrig |
| JVM Flags für Tests | 1 | 15 Min | Niedrig |
| Test-Durchlauf & Fixes | - | 2-4 Std | Mittel |
| **Gesamt Tests** | **4** | **~4-5 Std** | **Mittel** |

### 6.3 Starter-Umbenennung

| Aufgabe | Dateien | Geschätzter Aufwand | Komplexität |
|---------|---------|---------------------|-------------|
| web → webmvc | 1 | 5 Min | Niedrig |
| jackson starter hinzufügen | 1 | 5 Min | Niedrig |
| Test-Starter hinzufügen | 1 | 10 Min | Niedrig |
| **Gesamt Starters** | **1** | **~20 Min** | **Niedrig** |

### 6.4 Java 25 & Gradle 9

| Aufgabe | Dateien | Geschätzter Aufwand | Komplexität |
|---------|---------|---------------------|-------------|
| Gradle Wrapper Update | 1 | 5 Min | Niedrig |
| Java Toolchain auf 25 | 1 | 5 Min | Niedrig |
| JVM Flags konfigurieren | 1 | 15 Min | Niedrig |
| Dockerfile aktualisieren | 1 | 15 Min | Niedrig |
| **Gesamt Infrastructure** | **4** | **~40 Min** | **Niedrig** |

### Gesamtübersicht

| Bereich | Aufwand | Risiko |
|---------|---------|--------|
| Jackson 3.x Migration | 3 Stunden | 🟢 Niedrig |
| Test-Framework | 4-5 Stunden | 🟡 Mittel |
| Starter-Umbenennung | 20 Minuten | 🟢 Niedrig |
| Java 25 & Gradle 9 | 40 Minuten | 🟢 Niedrig |
| Integration & QA | 8-16 Stunden | 🟡 Mittel |
| **GESAMT** | **16-26 Stunden** | **🟡 Moderat** |

---

## 7. Internet-Recherche Dokumentation

### 7.1 Spring Boot 4.0 Migration Guide (Offizielle Quelle)

**URL:** https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide

**Relevante Erkenntnisse:**

1. **@MockBean Deprecation bestätigt:**
   > "Spring Boot's @MockBean and @SpyBean support has been deprecated in this release, in favor of @MockitoBean and @MockitoSpyBean support."

2. **MockMVC nicht mehr automatisch:**
   > "Using the @SpringBootTest annotation will no longer provide any MockMVC support. If you want to use MockMVC in your tests you should now add an @AutoConfigureMockMvc annotation."

3. **Starter-Umbenennung:**
   > "Several starter POMs have been renamed to provide better alignment with their corresponding module."

### 7.2 Jackson 3 Migration Guide (Offizielle Quelle)

**URL:** https://github.com/FasterXML/jackson/blob/main/jackson3/MIGRATING_TO_JACKSON_3.md

**Relevante Erkenntnisse:**

1. **Package-Änderung:**
   > "New Maven group-id and Java package: tools.jackson (2.x used com.fasterxml.jackson)"

2. **Annotations-Ausnahme:**
   > "Exception: jackson-annotations: 2.x version still used with 3.x, so no group-id/Java package change"

3. **Java 17 Minimum:**
   > "Baseline JDK raised to Java 17, from Java 8 in Jackson 2.x"

### 7.3 Spring Jackson 3 Integration (Offizielle Quelle)

**URL:** https://spring.io/blog/2025/10/07/introducing-jackson-3-support-in-spring/

**Relevante Erkenntnisse:**

1. **Koexistenz möglich:**
   > "When you add spring-boot-starter-webmvc to a Spring Boot 4 project, you'll get both Jackson 2 and Jackson 3 on your classpath."

2. **Annotations kompatibel:**
   > "This means your existing @JsonView, @JsonFormat, and other annotations work with both Jackson 2 and 3."

3. **Übergangsphase:**
   > "Jackson 2 auto-configuration is kept for some limited time in a deprecated form, only to help for a gradual migration."

### 7.4 Spring Boot 4 Modularisierung (Offizielle Quelle)

**URL:** https://spring.io/blog/2025/10/28/modularizing-spring-boot/

**Relevante Erkenntnisse:**

1. **Classic Starters für Übergang:**
   > "Spring Boot 4 retains Classic Starter POMs. These 'classic starters' bundle all the (modular) auto-configuration modules without their transitive dependencies."

2. **Test-Starter erforderlich:**
   > "Each of the main starters that you use has a corresponding test starter that you'll also want to add."

### 7.5 JEP 471 - Unsafe Deprecation (Offizielle Quelle)

**URL:** https://openjdk.org/jeps/471

**Relevante Erkenntnisse:**

1. **Phasenplan:**
   > "Phase 1 (JDK 23): All memory-access methods will be deprecated"
   > "Phase 2 (JDK 25 or earlier): Runtime warnings will be introduced"

2. **Workaround:**
   > "--sun-misc-unsafe-memory-access={allow|warn|debug|deny}"

### 7.6 Java 25 LTS Release (Offizielle Quelle)

**URL:** https://openjdk.org/projects/jdk/25/

**Relevante Erkenntnisse:**

1. **Release-Datum:** 16. September 2025

2. **18 JEPs enthalten**, darunter:
   - JEP 513: Flexible Constructor Bodies
   - JEP 519: Compact Object Headers
   - JEP 506: Scoped Values (finalisiert)

### 7.7 OpenRewrite Recipes (Automatisierung)

**URL:** https://docs.openrewrite.org/recipes/java/jackson/upgradejackson_2_3

**Relevante Erkenntnisse:**

1. **Automatische Migration verfügbar:**
   > "This recipe handles package changes (com.fasterxml.jackson -> tools.jackson), dependency updates, core class renames, exception renames, and method renames."

**URL:** https://docs.openrewrite.org/recipes/java/spring/boot4/replacemockbeanandspybean

2. **@MockBean Ersetzung:**
   > "Replaces @MockBean and @SpyBean annotations with @MockitoBean and @MockitoSpyBean."

---

## 8. Migrations-Roadmap

### Phase 1: Vorbereitung (Tag 1)

```
□ Feature-Branch erstellen: feature/java25-springboot4-migration
□ Aktuelle Tests ausführen und Baseline dokumentieren
□ Backup der funktionierenden Konfiguration
□ OpenRewrite Plugin installieren:
  
  plugins {
      id("org.openrewrite.rewrite") version "7.0.0"
  }
  
  rewrite {
      activeRecipe("org.openrewrite.java.jackson.UpgradeJackson_2_3")
      activeRecipe("org.openrewrite.java.spring.boot4.ReplaceMockBeanAndSpyBean")
  }
```

### Phase 2: Gradle & Java Update (Tag 1-2)

```bash
# Gradle Wrapper updaten
./gradlew wrapper --gradle-version=9.1.1

# Java Toolchain auf 25 setzen
# In build.gradle.kts:
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}
```

### Phase 3: Spring Boot 4.0 Update (Tag 2-3)

```kotlin
// build.gradle.kts
plugins {
    id("org.springframework.boot") version "4.0.1"
}

dependencies {
    // Starter-Umbenennung
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-jackson")
    
    // Test-Starter
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux-test")
}
```

### Phase 4: Jackson 3.x Migration (Tag 3-4)

```bash
# OpenRewrite ausführen
./gradlew rewriteRun

# Manuelle Nacharbeiten in JsonUtils.java
# Imports prüfen und anpassen
```

### Phase 5: Test-Migration (Tag 4-5)

```bash
# OpenRewrite für Tests
./gradlew rewriteRun -Drewrite.activeRecipe=org.openrewrite.java.spring.boot4.ReplaceMockBeanAndSpyBean

# Manuelle Prüfung:
# - @MockBean → @MockitoBean
# - Import-Pfade aktualisieren
```

### Phase 6: Integration & QA (Tag 6-8)

```bash
# Alle Tests ausführen
./gradlew test

# Property-Based Tests separat
./gradlew propertyTest

# Integration Tests
./gradlew integrationTest

# Manuelle API-Tests durchführen
# PDF-Generierung testen
# Gemini-Integration testen
```

### Phase 7: Finalisierung (Tag 9-10)

```
□ Dockerfile auf Java 25 aktualisieren
□ devcontainer.json aktualisieren  
□ README.md aktualisieren
□ Code Review durchführen
□ Merge in main Branch
```

---

## 9. Risikobewertung

### Risikomatrix

| Risiko | Wahrscheinlichkeit | Auswirkung | Mitigationsstrategie |
|--------|-------------------|------------|---------------------|
| Jackson 3 Inkompatibilitäten | Niedrig | Mittel | use-jackson2-defaults: true |
| jqwik Unsafe-Probleme | Mittel | Niedrig | JVM Flags setzen |
| Mockito Dynamic Agents | Mittel | Niedrig | -XX:+EnableDynamicAgentLoading |
| Test-Kontext-Probleme | Mittel | Mittel | Schrittweise Migration |
| WebClient-Änderungen | Niedrig | Niedrig | WebFlux Starter unverändert |
| PDF-Generierung (Puppeteer) | Sehr Niedrig | Niedrig | Node.js unabhängig |

### Rollback-Strategie

1. **Git-basiert:**
   ```bash
   git checkout main -- build.gradle.kts
   git checkout main -- gradle/wrapper/gradle-wrapper.properties
   ```

2. **Classic Starters als Fallback:**
   ```kotlin
   // Falls modulare Starters Probleme machen:
   implementation("org.springframework.boot:spring-boot-starter-classic")
   testImplementation("org.springframework.boot:spring-boot-starter-test-classic")
   ```

3. **Jackson 2 Fallback:**
   ```yaml
   spring:
     jackson:
       use-jackson2-defaults: true
   ```

---

## 10. Empfehlungen

### Sofortige Maßnahmen

1. ✅ **OpenRewrite integrieren** - Automatisiert 80% der mechanischen Änderungen

2. ✅ **Feature Branch erstellen** - Parallele Entwicklung ohne Risiko für main

3. ✅ **JVM Flags vorbereiten** - Unsafe-Warnungen frühzeitig adressieren

### Best Practices für die Migration

1. **Schrittweise vorgehen:**
   - Erst Gradle/Java
   - Dann Spring Boot Starters
   - Dann Jackson
   - Zuletzt Tests

2. **Tests als Validierung:**
   - Nach jedem Schritt alle Tests ausführen
   - Property-Based Tests sind besonders wertvoll für Regression

3. **Dokumentation mitführen:**
   - Breaking Changes dokumentieren
   - Learnings für zukünftige Migrationen festhalten

### Langfristige Empfehlungen

1. **Dependency-Updates automatisieren:**
   - Dependabot oder Renovate einrichten
   - Regelmäßige Minor-Updates

2. **Jackson 3 Best Practices:**
   - Immutable JsonMapper statt ObjectMapper erwägen
   - JSpecify null-safety Annotations nutzen

3. **Test-Modernisierung:**
   - Testcontainers 2.0 für Integration Tests
   - RestTestClient statt TestRestTemplate (für neue Tests)

---

## Anhang: Vollständige Datei-Checkliste

### Zu ändernde Dateien

- [ ] `build.gradle.kts`
- [ ] `gradle/wrapper/gradle-wrapper.properties`
- [ ] `src/main/java/com/catalogforge/util/JsonUtils.java`
- [ ] `src/main/java/com/catalogforge/agent/steps/LayoutGenerationStep.java`
- [ ] `src/main/java/com/catalogforge/pdf/PuppeteerBridge.java`
- [ ] `src/main/java/com/catalogforge/service/ProductService.java`
- [ ] `src/main/java/com/catalogforge/skill/SkillLoader.java`
- [ ] `src/test/java/com/catalogforge/controller/ProductControllerTest.java`
- [ ] `src/test/java/com/catalogforge/controller/SkillsControllerTest.java`
- [ ] `src/test/java/com/catalogforge/logging/LlmLoggingTest.java`
- [ ] `src/main/resources/application.yml`
- [ ] `.devcontainer/Dockerfile`

### Unveränderte Dateien (bereits kompatibel)

- [x] `src/main/java/com/catalogforge/model/TechnicalData.java` (Jackson Annotations bleiben gleich)
- [x] `src/main/java/com/catalogforge/config/properties/*.java` (Records sind kompatibel)
- [x] `src/main/java/com/catalogforge/gemini/GeminiClient.java` (WebClient unverändert)
- [x] `scripts/pdf-generator.js` (Node.js unabhängig)

---

*Dokument erstellt am 26. Dezember 2025*  
*Basierend auf CatalogForge Backend Repository und aktueller Spring Boot 4.0.1 / Jackson 3.x Dokumentation*
