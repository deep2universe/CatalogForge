package com.catalogforge.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import net.jqwik.api.ForAll;
import net.jqwik.api.Label;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.CharRange;
import net.jqwik.api.constraints.StringLength;

/**
 * Property-based tests for color validation and contrast calculation.
 * 
 * Property 18: Color Validation and Contrast
 * For any string:
 * - Valid hex codes (#RGB, #RRGGBB) return isValid=true
 * - Invalid formats return isValid=false
 * For any two valid colors, WCAG contrast ratio is calculated correctly
 * (verified against known values: #000/#FFF = 21.0).
 * 
 * Validates: Requirements 12.6, 12.7
 */
@Tag("property")
@Tag("unit")
class ColorUtilsPropertyTest {

    @Property(tries = 100)
    @Label("Property 18: Color Validation - valid 6-digit hex codes")
    void valid6DigitHexCodesAccepted(
            @ForAll @CharRange(from = '0', to = '9') char c1,
            @ForAll @CharRange(from = 'a', to = 'f') char c2,
            @ForAll @CharRange(from = 'A', to = 'F') char c3,
            @ForAll @CharRange(from = '0', to = '9') char c4,
            @ForAll @CharRange(from = 'a', to = 'f') char c5,
            @ForAll @CharRange(from = '0', to = '9') char c6) {
        
        String color = "#" + c1 + c2 + c3 + c4 + c5 + c6;
        
        assertThat(ColorUtils.isValidHexColor(color)).isTrue();
    }

    @Property(tries = 100)
    @Label("Property 18: Color Validation - valid 3-digit hex codes")
    void valid3DigitHexCodesAccepted(
            @ForAll @CharRange(from = '0', to = '9') char c1,
            @ForAll @CharRange(from = 'a', to = 'f') char c2,
            @ForAll @CharRange(from = 'A', to = 'F') char c3) {
        
        String color = "#" + c1 + c2 + c3;
        
        assertThat(ColorUtils.isValidHexColor(color)).isTrue();
    }

    @Property(tries = 100)
    @Label("Property 18: Color Validation - invalid formats rejected")
    void invalidFormatsRejected(@ForAll @StringLength(min = 1, max = 10) String random) {
        // Skip if it accidentally matches valid format
        if (random.matches("#[0-9A-Fa-f]{3}") || random.matches("#[0-9A-Fa-f]{6}")) {
            return;
        }
        
        assertThat(ColorUtils.isValidHexColor(random)).isFalse();
    }

    @Test
    @Tag("unit")
    @Label("Property 18: Color Contrast - black/white = 21.0")
    void blackWhiteContrastIs21() {
        double contrast = ColorUtils.calculateContrastRatio("#000000", "#FFFFFF");
        
        assertThat(contrast).isCloseTo(21.0, within(0.1));
    }

    @Test
    @Tag("unit")
    @Label("Property 18: Color Contrast - same color = 1.0")
    void sameColorContrastIs1() {
        double contrast = ColorUtils.calculateContrastRatio("#FF0000", "#FF0000");
        
        assertThat(contrast).isCloseTo(1.0, within(0.01));
    }

    @Test
    @Tag("unit")
    @Label("Property 18: Color Contrast - 3-digit hex works")
    void threeDigitHexWorks() {
        // #FFF should equal #FFFFFF
        double contrast1 = ColorUtils.calculateContrastRatio("#000", "#FFF");
        double contrast2 = ColorUtils.calculateContrastRatio("#000000", "#FFFFFF");
        
        assertThat(contrast1).isCloseTo(contrast2, within(0.01));
    }

    @Property(tries = 50)
    @Label("Property 18: Color Contrast - ratio always between 1 and 21")
    void contrastRatioInValidRange(
            @ForAll @CharRange(from = '0', to = '9') char r1,
            @ForAll @CharRange(from = '0', to = '9') char g1,
            @ForAll @CharRange(from = '0', to = '9') char b1,
            @ForAll @CharRange(from = '0', to = '9') char r2,
            @ForAll @CharRange(from = '0', to = '9') char g2,
            @ForAll @CharRange(from = '0', to = '9') char b2) {
        
        String color1 = "#" + r1 + r1 + g1 + g1 + b1 + b1;
        String color2 = "#" + r2 + r2 + g2 + g2 + b2 + b2;
        
        double contrast = ColorUtils.calculateContrastRatio(color1, color2);
        
        assertThat(contrast).isBetween(1.0, 21.0);
    }

    @Property(tries = 50)
    @Label("Property 18: Color Contrast - symmetric")
    void contrastRatioIsSymmetric(
            @ForAll @CharRange(from = '0', to = '9') char c1,
            @ForAll @CharRange(from = '0', to = '9') char c2) {
        
        String color1 = "#" + c1 + c1 + c1 + c1 + c1 + c1;
        String color2 = "#" + c2 + c2 + c2 + c2 + c2 + c2;
        
        double contrast1 = ColorUtils.calculateContrastRatio(color1, color2);
        double contrast2 = ColorUtils.calculateContrastRatio(color2, color1);
        
        assertThat(contrast1).isCloseTo(contrast2, within(0.001));
    }
}
