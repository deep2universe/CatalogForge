package com.catalogforge.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Tag;

import net.jqwik.api.ForAll;
import net.jqwik.api.Label;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;

/**
 * Property-based tests for CSS validation.
 * 
 * Property 17: CSS Validation
 * For any CSS input:
 * - Unbalanced brackets are detected as invalid
 * - Print units (mm, cm, pt, in) are accepted as valid
 * 
 * Validates: Requirements 12.4, 12.5
 */
@Tag("property")
@Tag("unit")
class CssValidatorPropertyTest {

    @Property(tries = 100)
    @Label("Property 17: CSS Validation - balanced brackets valid")
    void balancedBracketsAreValid(@ForAll @IntRange(min = 1, max = 5) int depth) {
        StringBuilder css = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            css.append(".class").append(i).append(" { ");
        }
        css.append("color: red;");
        for (int i = 0; i < depth; i++) {
            css.append(" }");
        }
        
        var result = CssValidator.validate(css.toString());
        
        assertThat(result.isValid()).isTrue();
    }

    @Property(tries = 100)
    @Label("Property 17: CSS Validation - unbalanced brackets invalid")
    void unbalancedBracketsAreInvalid(@ForAll @IntRange(min = 1, max = 5) int extraOpen) {
        StringBuilder css = new StringBuilder();
        css.append(".page { width: 210mm;");
        for (int i = 0; i < extraOpen; i++) {
            css.append(" {");
        }
        // Missing closing brackets
        
        var result = CssValidator.validate(css.toString());
        
        assertThat(result.isValid()).isFalse();
        assertThat(result.errors()).isNotEmpty();
    }

    @Property(tries = 50)
    @Label("Property 17: CSS Validation - print units accepted (mm)")
    void printUnitsMmAccepted(@ForAll @IntRange(min = 1, max = 1000) int value) {
        String css = ".page { width: " + value + "mm; }";
        
        var result = CssValidator.validate(css);
        
        assertThat(result.isValid()).isTrue();
        assertThat(CssValidator.containsPrintUnits(value + "mm")).isTrue();
    }

    @Property(tries = 50)
    @Label("Property 17: CSS Validation - print units accepted (cm)")
    void printUnitsCmAccepted(@ForAll @IntRange(min = 1, max = 100) int value) {
        String css = ".page { height: " + value + "cm; }";
        
        var result = CssValidator.validate(css);
        
        assertThat(result.isValid()).isTrue();
        assertThat(CssValidator.containsPrintUnits(value + "cm")).isTrue();
    }

    @Property(tries = 50)
    @Label("Property 17: CSS Validation - print units accepted (pt)")
    void printUnitsPtAccepted(@ForAll @IntRange(min = 1, max = 1000) int value) {
        String css = ".text { font-size: " + value + "pt; }";
        
        var result = CssValidator.validate(css);
        
        assertThat(result.isValid()).isTrue();
        assertThat(CssValidator.containsPrintUnits(value + "pt")).isTrue();
    }

    @Property(tries = 50)
    @Label("Property 17: CSS Validation - balanced parentheses valid")
    void balancedParenthesesAreValid(@ForAll @IntRange(min = 1, max = 3) int depth) {
        StringBuilder css = new StringBuilder(".page { background: ");
        for (int i = 0; i < depth; i++) {
            css.append("rgb(");
        }
        css.append("255, 0, 0");
        for (int i = 0; i < depth; i++) {
            css.append(")");
        }
        css.append("; }");
        
        var result = CssValidator.validate(css.toString());
        
        assertThat(result.isValid()).isTrue();
    }
}
