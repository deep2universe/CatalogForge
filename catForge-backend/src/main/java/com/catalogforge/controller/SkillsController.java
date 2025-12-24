package com.catalogforge.controller;

import com.catalogforge.model.Skill;
import com.catalogforge.service.SkillsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * REST controller for skills operations.
 * Provides endpoints for listing and retrieving skills.
 */
@RestController
@RequestMapping("/api/v1/skills")
public class SkillsController {

    private final SkillsService skillsService;

    public SkillsController(SkillsService skillsService) {
        this.skillsService = skillsService;
    }

    /**
     * GET /api/v1/skills
     * Returns all loaded skills.
     */
    @GetMapping
    public ResponseEntity<Collection<Skill>> getAllSkills() {
        return ResponseEntity.ok(skillsService.getAllSkills());
    }

    /**
     * GET /api/v1/skills/categories
     * Returns all skill categories.
     */
    @GetMapping("/categories")
    public ResponseEntity<Set<String>> getCategories() {
        return ResponseEntity.ok(skillsService.getAllCategories());
    }

    /**
     * GET /api/v1/skills/{category}
     * Returns all skills in a specific category.
     */
    @GetMapping("/{category}")
    public ResponseEntity<List<Skill>> getSkillsByCategory(@PathVariable String category) {
        List<Skill> skills = skillsService.getSkillsByCategory(category);
        return ResponseEntity.ok(skills);
    }

    /**
     * GET /api/v1/skills/prompts/examples
     * Returns example prompts for different use cases.
     */
    @GetMapping("/prompts/examples")
    public ResponseEntity<List<ExamplePrompt>> getExamplePrompts() {
        List<ExamplePrompt> examples = List.of(
                new ExamplePrompt(
                        "product-page-modern",
                        "Moderne Produktseite",
                        "Erstelle eine moderne Produktseite für den Mercedes-Benz Actros L mit technischen Daten und Highlights.",
                        List.of("LAYOUT_PRINCIPLES", "TYPOGRAPHY"),
                        "modern",
                        "A4"
                ),
                new ExamplePrompt(
                        "flyer-technical",
                        "Technischer Flyer",
                        "Erstelle einen technischen Flyer mit Fokus auf Spezifikationen und Leistungsdaten.",
                        List.of("LAYOUT_PRINCIPLES"),
                        "technical",
                        "DL"
                ),
                new ExamplePrompt(
                        "catalog-overview",
                        "Katalog-Übersicht",
                        "Erstelle eine Übersichtsseite mit mehreren Produkten der Actros-Baureihe.",
                        List.of("LAYOUT_PRINCIPLES", "TYPOGRAPHY"),
                        "modern",
                        "A4"
                )
        );
        return ResponseEntity.ok(examples);
    }

    /**
     * POST /api/v1/skills/reload
     * Reloads all skills from disk.
     */
    @PostMapping("/reload")
    public ResponseEntity<Void> reloadSkills() {
        skillsService.reloadSkills();
        return ResponseEntity.ok().build();
    }

    /**
     * Example prompt DTO for the examples endpoint.
     */
    public record ExamplePrompt(
            String id,
            String title,
            String prompt,
            List<String> skills,
            String style,
            String format
    ) {}
}
