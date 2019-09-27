package com.quickcamel.winnow.palindromes.service.parser;

public class PalindromeSolution {

    private Integer largestPalindromeLength;
    private String largestPalindrome;

    public PalindromeSolution(Integer largestPalindromeLength, String largestPalindrome) {
        this.largestPalindromeLength = largestPalindromeLength;
        this.largestPalindrome = largestPalindrome;
    }

    public Integer getLargestPalindromeLength() {
        return largestPalindromeLength;
    }

    public String getLargestPalindrome() {
        return largestPalindrome;
    }
}
