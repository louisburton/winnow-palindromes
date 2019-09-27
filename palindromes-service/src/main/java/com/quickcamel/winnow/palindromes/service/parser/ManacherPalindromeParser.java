package com.quickcamel.winnow.palindromes.service.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * This algorithm is mostly lifted from:
 * https://algs4.cs.princeton.edu/53substring/Manacher.java.html
 */
@Component
@Primary
public class ManacherPalindromeParser implements PalindromeParser {
    private static final Logger logger = LoggerFactory.getLogger(ManacherPalindromeParser.class);

    // Transform s into t.
    // For example, if s = "abba", then t = "$#a#b#b#a#@"
    // the # are interleaved to avoid even/odd-length palindromes uniformly
    // $ and @ are prepended and appended to each end to avoid bounds checking
    private char[] preprocess(String text) {
        char[] t = new char[text.length() * 2 + 3];
        t[0] = '$';
        t[text.length() * 2 + 2] = '@';
        for (int i = 0; i < text.length(); i++) {
            t[2 * i + 1] = '#';
            t[2 * i + 2] = text.charAt(i);
        }
        t[text.length() * 2 + 1] = '#';
        return t;
    }

    // longest palindromic substring
    private String longestPalindromicSubstring(String text, int[] centreIndexedPalindromeLengths) {
        int length = 0;   // length of longest palindromic substring
        int center = 0;   // center of longest palindromic substring
        for (int i = 1; i < centreIndexedPalindromeLengths.length - 1; i++) {
            if (centreIndexedPalindromeLengths[i] > length) {
                length = centreIndexedPalindromeLengths[i];
                center = i;
            }
        }
        return text.substring((center - 1 - length) / 2, (center - 1 + length) / 2);
    }

    @Override
    public PalindromeSolution parse(String text) {
        text = text.toLowerCase();
        int[] p;  // p[i] = length of longest palindromic substring of t, centered at i
        char[] t = preprocess(text);  // transformed string
        p = new int[t.length];

        int center = 0, right = 0;
        for (int i = 1; i < t.length - 1; i++) {
            int mirror = 2 * center - i;

            if (right > i)
                p[i] = Math.min(right - i, p[mirror]);

            // attempt to expand palindrome centered at i
            while (t[i + (1 + p[i])] == t[i - (1 + p[i])])
                p[i]++;

            // if palindrome centered at i expands past right,
            // adjust center based on expanded palindrome.
            if (i + p[i] > right) {
                center = i;
                right = i + p[i];
            }
        }
        if (logger.isTraceEnabled()) {
            logger.trace(Arrays.toString(t));
            logger.trace(Arrays.toString(p));
        }
        String longestPalindrome = longestPalindromicSubstring(text, p);
        return new PalindromeSolution(longestPalindrome.length(), longestPalindrome);
    }
}