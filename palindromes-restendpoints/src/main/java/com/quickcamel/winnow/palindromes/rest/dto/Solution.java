package com.quickcamel.winnow.palindromes.rest.dto;

public class Solution {

    private Integer largestPalindromeLength;
    private String largestPalindrome;

    public Integer getLargestPalindromeLength() {
        return largestPalindromeLength;
    }

    public String getLargestPalindrome() {
        return largestPalindrome;
    }

    public Solution withLargestPalindromeLength(final Integer largestPalindromeLength) {
        this.largestPalindromeLength = largestPalindromeLength;
        return this;
    }

    public Solution withLargestPalindrome(final String largestPalindrome) {
        this.largestPalindrome = largestPalindrome;
        return this;
    }

    @Override
    public String toString() {
        return "Solution{" +
                "largestPalindromeLength=" + largestPalindromeLength +
                ", largestPalindrome='" + largestPalindrome + '\'' +
                '}';
    }
}
