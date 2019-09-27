package com.quickcamel.winnow.palindromes.service.parser;

@FunctionalInterface
public interface PalindromeParser {

    PalindromeSolution parse(String text);
}
