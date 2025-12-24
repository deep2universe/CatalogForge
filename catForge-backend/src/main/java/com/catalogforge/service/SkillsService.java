package com.catalogforge.service;

import com.catalogforge.model.Skill;
import com.catalogforge.skill.SkillAssembler;
import com.catalogforge.skill.SkillLoader;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing and assembling skills.
 * Provides caching and high-level operations for skill management.
 */
@Service
public class SkillsService {

    private static final Logger log = LoggerFactory.getLogger(SkillsService.class);

    private final SkillLoader skillLoader;
    private final SkillAssembler skillAssembler;
    
    private volatile Map<String, Skill> skillCache = Map.of();

    public SkillsService(SkillLoader skillLoader, SkillAssembler skillAssembler) {
        this.skillLoader = skillLoader;
        this.skillAssembler = skillAssembler;
    }

    @PostConstruct
    public void init() {
        reloadSkills();
    }

    /**
     * Reloads all skills from disk.
     */
    public void reloadSkills() {
        skillCache = skillLoader.loadAllSkills();
        log.info("Skills cache refreshed with {} skills", skillCache.size());
    }

    /**
     * Returns all loaded skills.
     */
    public Collection<Skill> getAllSkills() {
        return Collections.unmodifiableCollection(skillCache.values());
    }

    /**
     * Returns a skill by name.
     */
    public Optional<Skill> getSkill(String name) {
        return Optional.ofNullable(skillCache.get(name));
    }

    /**
     * Returns all skills in a specific category.
     */
    public List<Skill> getSkillsByCategory(String category) {
        return skillCache.values().stream()
                .filter(s -> s.category().equalsIgnoreCase(category))
                .sorted(Comparator.comparing(Skill::name))
                .toList();
    }

    /**
     * Returns all unique categories.
     */
    public Set<String> getAllCategories() {
        return skillCache.values().stream()
                .map(Skill::category)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    /**
     * Assembles a prompt from the specified skills.
     * 
     * @param skillNames List of skill names to include
     * @param style Optional style (e.g., "modern", "technical")
     * @param format Optional format (e.g., "A4", "DL")
     * @return Assembled prompt string
     */
    public String assemblePrompt(List<String> skillNames, String style, String format) {
        return skillAssembler.assemble(skillCache, skillNames, style, format);
    }

    /**
     * Returns the ordered list of skill names that would be included in assembly.
     */
    public List<String> getAssemblyOrder(List<String> skillNames, String style, String format) {
        return skillAssembler.getOrderedSkillNames(skillCache, skillNames, style, format);
    }

    /**
     * Returns the total number of loaded skills.
     */
    public int getSkillCount() {
        return skillCache.size();
    }
}
