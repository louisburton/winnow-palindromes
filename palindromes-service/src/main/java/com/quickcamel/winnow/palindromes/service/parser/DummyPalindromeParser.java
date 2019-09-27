package com.quickcamel.winnow.palindromes.service.parser;

import org.springframework.stereotype.Component;

@Component
public class DummyPalindromeParser implements PalindromeParser {

    @Override
    public PalindromeSolution parse(String text) {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new PalindromeSolution(4, "Anna");
    }
}
