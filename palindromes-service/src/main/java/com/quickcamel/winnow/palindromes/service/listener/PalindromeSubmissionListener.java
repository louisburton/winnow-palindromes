package com.quickcamel.winnow.palindromes.service.listener;

import com.quickcamel.winnow.palindromes.service.task.PalindromeTaskProcessor;
import org.springframework.cloud.aws.messaging.config.annotation.NotificationMessage;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.stereotype.Component;

@Component
public class PalindromeSubmissionListener {

    private PalindromeTaskProcessor taskProcessor;

    public PalindromeSubmissionListener(PalindromeTaskProcessor taskProcessor) {
        this.taskProcessor = taskProcessor;
    }

    @SuppressWarnings("unused")
    @SqsListener("${palindrome.service.queue:palindrome-service-queue}")
    public void inputRetrieve(@NotificationMessage String taskId) {
        taskProcessor.processPalindromeTask(taskId);
    }
}
