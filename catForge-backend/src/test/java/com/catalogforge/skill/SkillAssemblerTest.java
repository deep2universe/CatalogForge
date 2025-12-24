package com.catalogforge.skill;

import com.catalogforge.model.Skill;
import net.jqwik.api.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for SkillAssembler including property-based tests.
 * 
 * Property Tests validate:
 * - Property 7: Skill Assembly Ordering (Requirements 6.1, 6.2, 6.3, 6.4)
 * - Property 8: Style and Format Skill Inclusion (Requirements 2.3, 2.4, 6.5, 6.6)
 */
class SkillAssemblerTest {

    private SkillAssembler assembler;
    private Map<String, Skill> testSkills;

    @BeforeEach
    void setUp() {
        assembler = new SkillAssembler();
        testSkills = createTestSkills();
    }

    private Map<String, Skill> createTestSkills() {
        Map<String, Skill> skills = new HashMap<>();
        
        skills.put("MASTER_SKILL", new Skill(
                "MASTER_SKILL", "core", "Master skill content", List.of(), Skill.MASTER_PRIORITY));
        
        skills.put("LAYOUT_PRINCIPLES", new Skill(
                "LAYOUT_PRINCIPLES", "core", "Layout principles content", 
                List.of("MASTER_SKILL"), 10));
        
        skills.put("TYPOGRAPHY", new Skill(
                "TYPOGRAPHY", "core", "Typography content", 
                List.of("MASTER_SKILL"), 15));
        
        skills.put("STYLE_MODERN", new Skill(
                "STYLE_MODERN", "styles", "Modern style content", 
                List.of("LAYOUT_PRINCIPLES", "TYPOGRAPHY"), 50));
        
        skills.put("STYLE_TECHNICAL", new Skill(
                "STYLE_TECHNICAL", "styles", "Technical style content", 
                List.of("LAYOUT_PRINCIPLES"), 50));
        
        skills.put("FORMAT_A4", new Skill(
                "FORMAT_A4", "formats", "A4 format content", List.of(), 60));
        
        skills.put("FORMAT_DL", new Skill(
                "FORMAT_DL", "formats", "DL format content", List.of(), 60));
        
        return skills;
    }

    @Nested
    @DisplayName("Property 7: Skill Assembly Ordering")
    class SkillAssemblyOrderingTests {

        @Test
        @DisplayName("MASTER_SKILL should always be first")
        void masterSkillShouldBeFirst() {
            List<String> order = assembler.getOrderedSkillNames(
                    testSkills, List.of("TYPOGRAPHY"), null, null);
            
            assertThat(order).isNotEmpty();
            assertThat(order.get(0)).isEqualTo("MASTER_SKILL");
        }

        @Test
        @DisplayName("Dependencies should come before dependent skills")
        void dependenciesShouldComeBefore() {
            List<String> order = assembler.getOrderedSkillNames(
                    testSkills, List.of("STYLE_MODERN"), null, null);
            
            int masterIdx = order.indexOf("MASTER_SKILL");
            int layoutIdx = order.indexOf("LAYOUT_PRINCIPLES");
            int typographyIdx = order.indexOf("TYPOGRAPHY");
            int styleIdx = order.indexOf("STYLE_MODERN");
            
            assertThat(masterIdx).isLessThan(layoutIdx);
            assertThat(masterIdx).isLessThan(typographyIdx);
            assertThat(layoutIdx).isLessThan(styleIdx);
            assertThat(typographyIdx).isLessThan(styleIdx);
        }

        @Test
        @DisplayName("Skills should be sorted by priority")
        void skillsShouldBeSortedByPriority() {
            List<String> order = assembler.getOrderedSkillNames(
                    testSkills, List.of("STYLE_MODERN", "FORMAT_A4"), null, null);
            
            // Verify priority order: MASTER(0) < LAYOUT(10) < TYPOGRAPHY(15) < STYLE(50) < FORMAT(60)
            int masterIdx = order.indexOf("MASTER_SKILL");
            int layoutIdx = order.indexOf("LAYOUT_PRINCIPLES");
            int typographyIdx = order.indexOf("TYPOGRAPHY");
            int styleIdx = order.indexOf("STYLE_MODERN");
            int formatIdx = order.indexOf("FORMAT_A4");
            
            assertThat(masterIdx).isLessThan(layoutIdx);
            assertThat(layoutIdx).isLessThan(typographyIdx);
            assertThat(typographyIdx).isLessThan(styleIdx);
            assertThat(styleIdx).isLessThan(formatIdx);
        }

        @Property
        @Label("Assembly should include all dependencies recursively")
        void assemblyShouldIncludeAllDependencies() {
            // STYLE_MODERN depends on LAYOUT_PRINCIPLES and TYPOGRAPHY
            // LAYOUT_PRINCIPLES depends on MASTER_SKILL
            // TYPOGRAPHY depends on MASTER_SKILL
            List<String> order = assembler.getOrderedSkillNames(
                    testSkills, List.of("STYLE_MODERN"), null, null);
            
            assertThat(order).contains("MASTER_SKILL", "LAYOUT_PRINCIPLES", "TYPOGRAPHY", "STYLE_MODERN");
        }

        @Property
        @Label("No duplicate skills in assembly")
        void noDuplicateSkillsInAssembly() {
            List<String> order = assembler.getOrderedSkillNames(
                    testSkills, List.of("STYLE_MODERN", "LAYOUT_PRINCIPLES", "TYPOGRAPHY"), null, null);
            
            assertThat(order).doesNotHaveDuplicates();
        }
    }

    @Nested
    @DisplayName("Property 8: Style and Format Skill Inclusion")
    class StyleFormatInclusionTests {

        @Test
        @DisplayName("Style skill should be included when specified")
        void styleSkillShouldBeIncluded() {
            List<String> order = assembler.getOrderedSkillNames(
                    testSkills, List.of(), "modern", null);
            
            assertThat(order).contains("STYLE_MODERN");
        }

        @Test
        @DisplayName("Format skill should be included when specified")
        void formatSkillShouldBeIncluded() {
            List<String> order = assembler.getOrderedSkillNames(
                    testSkills, List.of(), null, "A4");
            
            assertThat(order).contains("FORMAT_A4");
        }

        @Test
        @DisplayName("Both style and format should be included")
        void bothStyleAndFormatShouldBeIncluded() {
            List<String> order = assembler.getOrderedSkillNames(
                    testSkills, List.of(), "technical", "DL");
            
            assertThat(order).contains("STYLE_TECHNICAL", "FORMAT_DL");
        }

        @Test
        @DisplayName("Style name normalization should work")
        void styleNameNormalizationShouldWork() {
            assertThat(assembler.normalizeStyleName("modern")).isEqualTo("STYLE_MODERN");
            assertThat(assembler.normalizeStyleName("MODERN")).isEqualTo("STYLE_MODERN");
            assertThat(assembler.normalizeStyleName("STYLE_MODERN")).isEqualTo("STYLE_MODERN");
        }

        @Test
        @DisplayName("Format name normalization should work")
        void formatNameNormalizationShouldWork() {
            assertThat(assembler.normalizeFormatName("a4")).isEqualTo("FORMAT_A4");
            assertThat(assembler.normalizeFormatName("A4")).isEqualTo("FORMAT_A4");
            assertThat(assembler.normalizeFormatName("FORMAT_A4")).isEqualTo("FORMAT_A4");
        }

        @Property
        @Label("Style dependencies should be resolved")
        void styleDependenciesShouldBeResolved() {
            // STYLE_MODERN depends on LAYOUT_PRINCIPLES and TYPOGRAPHY
            List<String> order = assembler.getOrderedSkillNames(
                    testSkills, List.of(), "modern", null);
            
            assertThat(order).contains("LAYOUT_PRINCIPLES", "TYPOGRAPHY");
        }
    }

    @Nested
    @DisplayName("Assembly Content Tests")
    class AssemblyContentTests {

        @Test
        @DisplayName("Assembled content should contain skill headers")
        void assembledContentShouldContainHeaders() {
            String content = assembler.assemble(
                    testSkills, List.of("TYPOGRAPHY"), null, null);
            
            assertThat(content).contains("# MASTER_SKILL");
            assertThat(content).contains("# TYPOGRAPHY");
        }

        @Test
        @DisplayName("Assembled content should contain skill content")
        void assembledContentShouldContainContent() {
            String content = assembler.assemble(
                    testSkills, List.of("TYPOGRAPHY"), null, null);
            
            assertThat(content).contains("Master skill content");
            assertThat(content).contains("Typography content");
        }

        @Test
        @DisplayName("Skills should be separated by dividers")
        void skillsShouldBeSeparatedByDividers() {
            String content = assembler.assemble(
                    testSkills, List.of("TYPOGRAPHY"), null, null);
            
            assertThat(content).contains("---");
        }
    }
}
