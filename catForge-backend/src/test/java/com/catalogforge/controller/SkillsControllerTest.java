package com.catalogforge.controller;

import com.catalogforge.model.Skill;
import com.catalogforge.service.SkillsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for SkillsController.
 */
@WebMvcTest(SkillsController.class)
class SkillsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SkillsService skillsService;

    @Nested
    @DisplayName("GET /api/v1/skills")
    class GetAllSkillsTests {

        @Test
        @DisplayName("should return all skills")
        void shouldReturnAllSkills() throws Exception {
            List<Skill> skills = List.of(
                    new Skill("MASTER_SKILL", "core", "content", List.of(), 0),
                    new Skill("TYPOGRAPHY", "core", "content", List.of("MASTER_SKILL"), 15)
            );
            when(skillsService.getAllSkills()).thenReturn(skills);

            mockMvc.perform(get("/api/v1/skills"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].name", is("MASTER_SKILL")))
                    .andExpect(jsonPath("$[1].name", is("TYPOGRAPHY")));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/skills/categories")
    class GetCategoriesTests {

        @Test
        @DisplayName("should return all categories")
        void shouldReturnAllCategories() throws Exception {
            Set<String> categories = Set.of("core", "styles", "formats");
            when(skillsService.getAllCategories()).thenReturn(categories);

            mockMvc.perform(get("/api/v1/skills/categories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(3)))
                    .andExpect(jsonPath("$", containsInAnyOrder("core", "styles", "formats")));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/skills/{category}")
    class GetSkillsByCategoryTests {

        @Test
        @DisplayName("should return skills by category")
        void shouldReturnSkillsByCategory() throws Exception {
            List<Skill> coreSkills = List.of(
                    new Skill("MASTER_SKILL", "core", "content", List.of(), 0),
                    new Skill("LAYOUT_PRINCIPLES", "core", "content", List.of("MASTER_SKILL"), 10)
            );
            when(skillsService.getSkillsByCategory("core")).thenReturn(coreSkills);

            mockMvc.perform(get("/api/v1/skills/core"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].category", is("core")));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/skills/prompts/examples")
    class GetExamplePromptsTests {

        @Test
        @DisplayName("should return example prompts")
        void shouldReturnExamplePrompts() throws Exception {
            mockMvc.perform(get("/api/v1/skills/prompts/examples"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(3)))
                    .andExpect(jsonPath("$[0].id", is("product-page-modern")))
                    .andExpect(jsonPath("$[0].title", notNullValue()))
                    .andExpect(jsonPath("$[0].prompt", notNullValue()))
                    .andExpect(jsonPath("$[0].skills", isA(List.class)))
                    .andExpect(jsonPath("$[0].style", notNullValue()))
                    .andExpect(jsonPath("$[0].format", notNullValue()));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/skills/reload")
    class ReloadSkillsTests {

        @Test
        @DisplayName("should reload skills")
        void shouldReloadSkills() throws Exception {
            mockMvc.perform(post("/api/v1/skills/reload"))
                    .andExpect(status().isOk());

            verify(skillsService).reloadSkills();
        }
    }
}
