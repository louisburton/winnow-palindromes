package com.quickcamel.winnow.palindromes.service.task;

@FunctionalInterface
public interface PalindromeTaskProcessor {

    void processPalindromeTask(String taskId);
}
