package com.quickcamel.winnow.palindromes.service.parser;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ManacherPalindromeParserTest {

    private PalindromeParser palindromeParser = new ManacherPalindromeParser();

    @Test
    void shouldFindAnna() {
        PalindromeSolution solution = palindromeParser.parse("I am Anna");
        // example shows lower case
        assertThat(solution.getLargestPalindrome()).isEqualTo("anna");
        assertThat(solution.getLargestPalindromeLength()).isEqualTo(4);
    }

    @Test
    void shouldFindLongest() {
        PalindromeSolution solution = palindromeParser.parse("Madam Anna");
        assertThat(solution.getLargestPalindrome()).isEqualTo("madam");
        assertThat(solution.getLargestPalindromeLength()).isEqualTo(5);
    }

    // TODO clarify requirement
    @Test
    void shouldFindAllSubstrings() {
        PalindromeSolution solution = palindromeParser.parse("I am Madam Anna");
        assertThat(solution.getLargestPalindrome()).isEqualTo(" madam ");
        assertThat(solution.getLargestPalindromeLength()).isEqualTo(7);
    }

    @Test
    void shouldFindNonAlphabetPalindromes() {
        PalindromeSolution solution = palindromeParser.parse("Ma'am, I am Annabelle!");
        assertThat(solution.getLargestPalindrome()).isEqualTo("ma'am");
        assertThat(solution.getLargestPalindromeLength()).isEqualTo(5);
    }
}