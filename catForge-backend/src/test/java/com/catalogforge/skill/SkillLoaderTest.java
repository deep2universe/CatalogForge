package com.catalogforge.skill;

import com.catalogforge.model.Skill;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for SkillLoader.
 */
class SkillLoaderTest {

    private SkillLoader loader;

    @BeforeEach
    void setUp() {
        loader = new SkillLoader();
    }

    @Nested
    @DisplayName("Skill Loading Tests")
    class SkillLoadingTests {

        @Test
        @DisplayName("should load skills from resources")
        void shouldLoadSkillsFromResources() {
            Map<String, Skill> skills = loader.loadAllSkills();
            
            assertThat(skills).isNotEmpty();
            assertThat(skills).containsKey("MASTER_SKILL");
        }

        @Test
        @DisplayName("should load MASTER_SKILL with priority 0")
        void shouldLoadMasterSkillWithPriorityZero() {
            Map<String, Skill> skills = loader.loadAllSkills();
            
            Skill master = skills.get("MASTER_SKILL");
            assertThat(master).isNotNull();
            assertThat(master.priority()).isEqualTo(Skill.MASTER_PRIORITY);
        }

        @Test
        @DisplayName("should extract category from path")
        void shouldExtractCategoryFromPath() {
            Map<String, Skill> skills = loader.loadAllSkills();
            
            Skill master = skills.get("MASTER_SKILL");
            assertThat(master.category()).isEqualTo("core");
            
            if (skills.containsKey("STYLE_MODERN")) {
                Skill style = skills.get("STYLE_MODERN");
                assertThat(style.category()).isEqualTo("styles");
            }
        }

        @Test
        @DisplayName("should parse dependencies from metadata")
        void shouldParseDependencies() {
            Map<String, Skill> skills = loader.loadAllSkills();
            
            Skill layout = skills.get("LAYOUT_PRINCIPLES");
            if (layout != null) {
                assertThat(layout.dependencies()).contains("MASTER_SKILL");
            }
        }
    }

    @Nested
    @DisplayName("Metadata Parsing Tests")
    class MetadataParsingTests {

        @Test
        @DisplayName("should parse dependencies list")
        void shouldParseDependenciesList() {
            String metadata = "dependencies: [DEP1, DEP2, DEP3]";
            List<String> deps = loader.parseDependencies(metadata);
            
            assertThat(deps).containsExactly("DEP1", "DEP2", "DEP3");
        }

        @Test
        @DisplayName("should handle empty dependencies")
        void shouldHandleEmptyDependencies() {
            String metadata = "dependencies: []";
            List<String> deps = loader.parseDependencies(metadata);
            
            assertThat(deps).isEmpty();
        }

        @Test
        @DisplayName("should parse priority")
        void shouldParsePriority() {
            String metadata = "priority: 42";
            int priority = loader.parsePriority(metadata);
            
            assertThat(priority).isEqualTo(42);
        }

        @Test
        @DisplayName("should return default priority when not specified")
        void shouldReturnDefaultPriority() {
            String metadata = "other: value";
            int priority = loader.parsePriority(metadata);
            
            assertThat(priority).isEqualTo(Skill.DEFAULT_PRIORITY);
        }

        @Test
        @DisplayName("should extract name from filename")
        void shouldExtractNameFromFilename() {
            assertThat(loader.extractName("MASTER_SKILL.md")).isEqualTo("MASTER_SKILL");
            assertThat(loader.extractName("STYLE_MODERN.md")).isEqualTo("STYLE_MODERN");
        }
    }
}
