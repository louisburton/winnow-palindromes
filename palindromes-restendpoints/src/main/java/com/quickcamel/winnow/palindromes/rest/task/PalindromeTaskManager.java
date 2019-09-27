package com.quickcamel.winnow.palindromes.rest.task;

import com.quickcamel.winnow.palindromes.rest.dto.PalindromeTaskOutput;
import com.quickcamel.winnow.palindromes.rest.dto.Problem;

public interface PalindromeTaskManager {

    PalindromeTaskOutput submit(Problem submission);

    PalindromeTaskOutput status(String taskId) throws NotFoundException;
}