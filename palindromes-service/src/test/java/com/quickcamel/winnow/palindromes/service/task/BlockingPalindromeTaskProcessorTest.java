package com.quickcamel.winnow.palindromes.service.task;

import com.quickcamel.winnow.palindromes.entities.PalindromeTaskEntity;
import com.quickcamel.winnow.palindromes.repositories.PalindromeTaskRepository;
import com.quickcamel.winnow.palindromes.service.parser.PalindromeParser;
import com.quickcamel.winnow.palindromes.service.parser.PalindromeSolution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlockingPalindromeTaskProcessorTest {

    @Mock
    private PalindromeParser parser;
    @Mock
    private PalindromeTaskRepository repository;

    private BlockingPalindromeTaskProcessor blockingProcessor;

    @BeforeEach
    void setup() {
        blockingProcessor = new BlockingPalindromeTaskProcessor(parser, repository);
    }

    @Test
    void shouldDoNothingWhenTaskNotFound() {
        when(repository.findById(anyString())).thenReturn(Optional.empty());

        blockingProcessor.processPalindromeTask("de54b2bd-dfb3-43b9-9dc9-07b6326ff023");

        verify(repository, never()).save(any());
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldProgressSubmittedTaskToStartedBeforeParsing() {
        String taskId = "de54b2bd-dfb3-43b9-9dc9-07b6326ff023";
        when(repository.findById(anyString())).thenReturn(
                Optional.of(new PalindromeTaskEntity()
                        .withTask(taskId)
                        .withStatus(PalindromeTaskEntity.Status.SUBMITTED)
                        .withSubmitted(1L)
                        .withText("test")),
                Optional.of(new PalindromeTaskEntity()
                        .withTask(taskId)
                        .withStatus(PalindromeTaskEntity.Status.STARTED)
                        .withSubmitted(1L)
                        .withStarted(2L)
                        .withText("test"))
        );

        when(parser.parse(anyString()))
                .thenReturn(new PalindromeSolution(0, null));

        blockingProcessor.processPalindromeTask(taskId);

        InOrder inOrder = inOrder(repository, parser);
        inOrder.verify(repository).save(argThat(argument ->
                argument.getStatus().equals(PalindromeTaskEntity.Status.STARTED)
                        && argument.getStarted() != null));
        inOrder.verify(parser).parse(anyString());
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldProgressStartedTaskToCompletedAfterParsing() {
        String taskId = "de54b2bd-dfb3-43b9-9dc9-07b6326ff023";
        when(repository.findById(anyString())).thenReturn(
                Optional.of(new PalindromeTaskEntity()
                        .withTask(taskId)
                        .withStatus(PalindromeTaskEntity.Status.SUBMITTED)
                        .withSubmitted(1L)
                        .withText("test")),
                Optional.of(new PalindromeTaskEntity()
                        .withTask(taskId)
                        .withStatus(PalindromeTaskEntity.Status.STARTED)
                        .withSubmitted(1L)
                        .withStarted(2L)
                        .withText("test"))
        );

        when(parser.parse(anyString()))
                .thenReturn(new PalindromeSolution(0, null));

        blockingProcessor.processPalindromeTask(taskId);

        InOrder inOrder = inOrder(repository, parser);
        inOrder.verify(parser).parse(anyString());
        inOrder.verify(repository).save(argThat(argument ->
                argument.getStatus().equals(PalindromeTaskEntity.Status.COMPLETED)
                        && argument.getCompleted() != null));
    }

    @Test
    void shouldNotDoAnythingToCompletedTask() {
        String taskId = "de54b2bd-dfb3-43b9-9dc9-07b6326ff023";
        when(repository.findById(anyString())).thenReturn(
                Optional.of(new PalindromeTaskEntity()
                        .withTask(taskId)
                        .withStatus(PalindromeTaskEntity.Status.COMPLETED)
                        .withCompleted(1L))
        );

        blockingProcessor.processPalindromeTask(taskId);

        verify(parser, never()).parse(any());
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldNotOverwriteResultOfTaskCompletedMidParsing() {
        String taskId = "de54b2bd-dfb3-43b9-9dc9-07b6326ff023";
        when(repository.findById(anyString())).thenReturn(
                Optional.of(new PalindromeTaskEntity()
                        .withTask(taskId)
                        .withStatus(PalindromeTaskEntity.Status.STARTED)
                        .withStarted(1L)
                        .withText("test")),
                Optional.of(new PalindromeTaskEntity()
                        .withTask(taskId)
                        .withStatus(PalindromeTaskEntity.Status.COMPLETED)
                        .withCompleted(1L)
                        .withLargestPalindromeLength(0))
        );

        blockingProcessor.processPalindromeTask(taskId);

        verify(repository, never()).save(any());
    }
}