package com.catalogforge.skill;

import com.catalogforge.exception.SkillLoadException;
import com.catalogforge.model.Skill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Loads skill files from resources/skills/ directory.
 * Parses metadata headers and extracts skill content.
 */
@Component
public class SkillLoader {

    private static final Logger log = LoggerFactory.getLogger(SkillLoader.class);
    private static final String SKILLS_PATTERN = "classpath:skills/**/*.md";
    
    // Metadata patterns
    private static final Pattern METADATA_BLOCK = Pattern.compile("^---\\s*\\n(.*?)\\n---\\s*\\n", Pattern.DOTALL);
    private static final Pattern DEPENDENCIES_PATTERN = Pattern.compile("dependencies:\\s*\\[([^\\]]*)]");
    private static final Pattern PRIORITY_PATTERN = Pattern.compile("priority:\\s*(\\d+)");

    private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    /**
     * Loads all skills from the skills directory.
     * @return Map of skill name to Skill object
     */
    public Map<String, Skill> loadAllSkills() {
        Map<String, Skill> skills = new HashMap<>();
        
        try {
            Resource[] resources = resolver.getResources(SKILLS_PATTERN);
            
            for (Resource resource : resources) {
                try {
                    Skill skill = loadSkill(resource);
                    skills.put(skill.name(), skill);
                    log.debug("Loaded skill: {} (category: {}, priority: {})", 
                            skill.name(), skill.category(), skill.priority());
                } catch (Exception e) {
                    log.warn("Failed to load skill from {}: {}", resource.getFilename(), e.getMessage());
                }
            }
            
            log.info("Loaded {} skills from {}", skills.size(), SKILLS_PATTERN);
            
        } catch (IOException e) {
            throw new SkillLoadException("Failed to scan skills directory", e);
        }
        
        return skills;
    }

    /**
     * Loads a single skill from a resource.
     */
    Skill loadSkill(Resource resource) throws IOException {
        String filename = resource.getFilename();
        if (filename == null) {
            throw new SkillLoadException("Resource has no filename");
        }
        
        String content = resource.getContentAsString(StandardCharsets.UTF_8);
        String name = extractName(filename);
        String category = extractCategory(resource);
        
        // Parse metadata
        List<String> dependencies = List.of();
        int priority = Skill.DEFAULT_PRIORITY;
        String skillContent = content;
        
        Matcher metaMatcher = METADATA_BLOCK.matcher(content);
        if (metaMatcher.find()) {
            String metadata = metaMatcher.group(1);
            dependencies = parseDependencies(metadata);
            priority = parsePriority(metadata);
            skillContent = content.substring(metaMatcher.end());
        }
        
        // Master skill always has priority 0
        if ("MASTER_SKILL".equals(name)) {
            priority = Skill.MASTER_PRIORITY;
        }
        
        return new Skill(name, category, skillContent.trim(), dependencies, priority);
    }

    /**
     * Extracts skill name from filename (removes .md extension).
     */
    String extractName(String filename) {
        return filename.endsWith(".md") 
                ? filename.substring(0, filename.length() - 3) 
                : filename;
    }

    /**
     * Extracts category from resource path.
     * e.g., "skills/core/LAYOUT.md" -> "core"
     */
    String extractCategory(Resource resource) throws IOException {
        String path = resource.getURL().getPath();
        
        // Find "skills/" in path and extract next segment
        int skillsIdx = path.indexOf("skills/");
        if (skillsIdx >= 0) {
            String afterSkills = path.substring(skillsIdx + 7);
            int slashIdx = afterSkills.indexOf('/');
            if (slashIdx > 0) {
                return afterSkills.substring(0, slashIdx);
            }
        }
        
        return "default";
    }

    /**
     * Parses dependencies from metadata block.
     * Format: dependencies: [DEP1, DEP2]
     */
    List<String> parseDependencies(String metadata) {
        Matcher matcher = DEPENDENCIES_PATTERN.matcher(metadata);
        if (matcher.find()) {
            String deps = matcher.group(1).trim();
            if (deps.isEmpty()) {
                return List.of();
            }
            return Arrays.stream(deps.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
        }
        return List.of();
    }

    /**
     * Parses priority from metadata block.
     * Format: priority: 10
     */
    int parsePriority(String metadata) {
        Matcher matcher = PRIORITY_PATTERN.matcher(metadata);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return Skill.DEFAULT_PRIORITY;
    }
}
