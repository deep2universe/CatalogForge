package com.catalogforge.model;

import java.util.List;

/**
 * Represents a skill (prompt template) for layout generation.
 * Skills are loaded from markdown files in resources/skills/.
 */
public record Skill(
    String name,
    String category,
    String content,
    List<String> dependencies,
    int priority
) {
    public Skill {
        dependencies = dependencies != null ? List.copyOf(dependencies) : List.of();
    }

    /**
     * Default priority for skills without explicit priority.
     */
    public static final int DEFAULT_PRIORITY = 100;

    /**
     * Priority for the master skill (always first).
     */
    public static final int MASTER_PRIORITY = 0;
}
