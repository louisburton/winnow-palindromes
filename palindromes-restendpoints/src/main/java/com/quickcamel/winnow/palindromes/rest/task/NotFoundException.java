package com.quickcamel.winnow.palindromes.rest.task;

public class NotFoundException extends Exception {

    public NotFoundException() {
        super();
    }

    public NotFoundException(Exception sourceException) {
        super(sourceException);
    }
}
