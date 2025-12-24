package com.catalogforge.skill;

import com.catalogforge.model.Skill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Assembles skills into a combined prompt.
 * Handles dependency resolution, priority ordering, and style/format skill inclusion.
 */
@Component
public class SkillAssembler {

    private static final Logger log = LoggerFactory.getLogger(SkillAssembler.class);
    private static final String MASTER_SKILL = "MASTER_SKILL";

    /**
     * Assembles skills into a combined prompt string.
     * 
     * @param skills Map of all available skills
     * @param requestedSkills List of skill names to include
     * @param style Optional style skill name (e.g., "STYLE_MODERN")
     * @param format Optional format skill name (e.g., "FORMAT_A4")
     * @return Combined prompt string
     */
    public String assemble(Map<String, Skill> skills, List<String> requestedSkills, 
                          String style, String format) {
        
        Set<String> toInclude = new LinkedHashSet<>();
        
        // 1. Always include MASTER_SKILL first
        if (skills.containsKey(MASTER_SKILL)) {
            toInclude.add(MASTER_SKILL);
        }
        
        // 2. Add requested skills with their dependencies
        for (String skillName : requestedSkills) {
            addWithDependencies(skills, skillName, toInclude, new HashSet<>());
        }
        
        // 3. Add style skill if specified
        if (style != null && !style.isBlank()) {
            String styleSkill = normalizeStyleName(style);
            addWithDependencies(skills, styleSkill, toInclude, new HashSet<>());
        }
        
        // 4. Add format skill if specified
        if (format != null && !format.isBlank()) {
            String formatSkill = normalizeFormatName(format);
            addWithDependencies(skills, formatSkill, toInclude, new HashSet<>());
        }
        
        // 5. Sort by priority (MASTER first, then by priority, then alphabetically)
        List<Skill> orderedSkills = toInclude.stream()
                .map(skills::get)
                .filter(Objects::nonNull)
                .sorted(Comparator
                        .comparingInt(Skill::priority)
                        .thenComparing(Skill::name))
                .toList();
        
        // 6. Combine contents
        StringBuilder combined = new StringBuilder();
        for (Skill skill : orderedSkills) {
            if (!combined.isEmpty()) {
                combined.append("\n\n---\n\n");
            }
            combined.append("# ").append(skill.name()).append("\n\n");
            combined.append(skill.content());
        }
        
        log.debug("Assembled {} skills: {}", orderedSkills.size(), 
                orderedSkills.stream().map(Skill::name).toList());
        
        return combined.toString();
    }

    /**
     * Recursively adds a skill and its dependencies to the include set.
     */
    void addWithDependencies(Map<String, Skill> skills, String skillName, 
                            Set<String> toInclude, Set<String> visited) {
        if (skillName == null || visited.contains(skillName)) {
            return;
        }
        
        visited.add(skillName);
        Skill skill = skills.get(skillName);
        
        if (skill == null) {
            log.warn("Skill not found: {}", skillName);
            return;
        }
        
        // Add dependencies first
        for (String dep : skill.dependencies()) {
            addWithDependencies(skills, dep, toInclude, visited);
        }
        
        // Then add the skill itself
        toInclude.add(skillName);
    }

    /**
     * Normalizes style name to skill name format.
     * e.g., "modern" -> "STYLE_MODERN"
     */
    String normalizeStyleName(String style) {
        String upper = style.toUpperCase(Locale.ROOT);
        if (upper.startsWith("STYLE_")) {
            return upper;
        }
        return "STYLE_" + upper;
    }

    /**
     * Normalizes format name to skill name format.
     * e.g., "A4" -> "FORMAT_A4"
     */
    String normalizeFormatName(String format) {
        String upper = format.toUpperCase(Locale.ROOT);
        if (upper.startsWith("FORMAT_")) {
            return upper;
        }
        return "FORMAT_" + upper;
    }

    /**
     * Gets the ordered list of skill names that would be included.
     * Useful for debugging and testing.
     */
    public List<String> getOrderedSkillNames(Map<String, Skill> skills, List<String> requestedSkills,
                                             String style, String format) {
        Set<String> toInclude = new LinkedHashSet<>();
        
        if (skills.containsKey(MASTER_SKILL)) {
            toInclude.add(MASTER_SKILL);
        }
        
        for (String skillName : requestedSkills) {
            addWithDependencies(skills, skillName, toInclude, new HashSet<>());
        }
        
        if (style != null && !style.isBlank()) {
            addWithDependencies(skills, normalizeStyleName(style), toInclude, new HashSet<>());
        }
        
        if (format != null && !format.isBlank()) {
            addWithDependencies(skills, normalizeFormatName(format), toInclude, new HashSet<>());
        }
        
        return toInclude.stream()
                .map(skills::get)
                .filter(Objects::nonNull)
                .sorted(Comparator
                        .comparingInt(Skill::priority)
                        .thenComparing(Skill::name))
                .map(Skill::name)
                .toList();
    }
}
