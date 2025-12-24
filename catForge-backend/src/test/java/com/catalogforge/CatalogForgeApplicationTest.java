package com.catalogforge;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test to verify the application context loads successfully.
 */
@SpringBootTest
@ActiveProfiles("test")
@Tag("integration")
class CatalogForgeApplicationTest {

    @Test
    void contextLoads() {
        // Application context should load without errors
    }
}
