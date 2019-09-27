package com.quickcamel.winnow.palindromes.rest.dto;

public class Problem {

    private String text;

    public String getText() {
        return text;
    }

    public Problem withText(final String text) {
        this.text = text;
        return this;
    }

    @Override
    public String toString() {
        return "Problem{" +
                "text='" + text + '\'' +
                '}';
    }
}
