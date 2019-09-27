package com.quickcamel.winnow.palindromes.rest.task;

import com.quickcamel.winnow.palindromes.entities.PalindromeTaskEntity;

@FunctionalInterface
public interface SubmissionObserver {
    void newTaskPersisted(PalindromeTaskEntity taskEntity);
}
