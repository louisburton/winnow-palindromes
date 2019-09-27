package com.quickcamel.winnow.palindromes.rest.task;

import com.quickcamel.winnow.palindromes.entities.PalindromeTaskEntity;
import com.quickcamel.winnow.palindromes.repositories.PalindromeTaskRepository;
import com.quickcamel.winnow.palindromes.rest.dto.Problem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ObservablePalindromeTaskManagerTest {

    @Mock
    private SubmissionObserver submissionObserver;

    @Mock
    private PalindromeTaskRepository repository;
    private ObservablePalindromeTaskManager observablePalindromeTaskManager;

    @BeforeEach
    void setup() {
        when(repository.save(any()))
                .thenReturn(new PalindromeTaskEntity().withStatus(PalindromeTaskEntity.Status.SUBMITTED));
        observablePalindromeTaskManager =
                new ObservablePalindromeTaskManager(Collections.singletonList(submissionObserver), repository);
    }

    @Test
    void shouldPersistSubmittedProblem() {
        String text = "Can't claim to be a wordsmith and misspell palindrome in the same sentence!";

        assertThat(observablePalindromeTaskManager.submit(new Problem().withText(text)))
                .hasFieldOrPropertyWithValue("status", "submitted");

        verify(repository).save(argThat(argument -> argument.getText().equals(text)));
    }

    @Test
    void shouldSendTaskToObservers() {
        observablePalindromeTaskManager.submit(new Problem().withText("hi"));

        verify(submissionObserver).newTaskPersisted(argThat(argument -> argument.getStatus().equals(PalindromeTaskEntity.Status.SUBMITTED)));
    }
}