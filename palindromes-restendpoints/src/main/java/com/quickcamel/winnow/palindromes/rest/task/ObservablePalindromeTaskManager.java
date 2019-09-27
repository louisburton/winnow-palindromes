package com.quickcamel.winnow.palindromes.rest.task;

import com.quickcamel.winnow.palindromes.entities.PalindromeTaskEntity;
import com.quickcamel.winnow.palindromes.repositories.PalindromeTaskRepository;
import com.quickcamel.winnow.palindromes.rest.dto.PalindromeTaskOutput;
import com.quickcamel.winnow.palindromes.rest.dto.Problem;
import com.quickcamel.winnow.palindromes.rest.dto.Solution;
import com.quickcamel.winnow.palindromes.rest.dto.Timestamps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class ObservablePalindromeTaskManager implements PalindromeTaskManager {

    private static final Logger logger = LoggerFactory.getLogger(ObservablePalindromeTaskManager.class);

    private Collection<SubmissionObserver> submissionObservers;
    private PalindromeTaskRepository palindromeTaskRepository;

    ObservablePalindromeTaskManager(Collection<SubmissionObserver> submissionObservers,
                                    PalindromeTaskRepository palindromeTaskRepository) {
        this.submissionObservers = submissionObservers;
        this.palindromeTaskRepository = palindromeTaskRepository;
    }

    @Override
    public PalindromeTaskOutput submit(Problem problem) {
        logger.debug("Submitted {}", problem);

        PalindromeTaskEntity persistedTask = persistPalindromeTask(problem);

        submissionObservers.forEach(submissionObserver -> submissionObserver.newTaskPersisted(persistedTask));

        return adaptEntityToDTO(persistedTask);
    }

    private PalindromeTaskEntity persistPalindromeTask(Problem problem) {
        PalindromeTaskEntity task = new PalindromeTaskEntity()
                .withText(problem.getText())
                .withSubmitted(System.currentTimeMillis())
                .withStatus(PalindromeTaskEntity.Status.SUBMITTED);
        task = palindromeTaskRepository.save(task);
        logger.debug("Persisted the following record : {}", task);
        return task;
    }

    private PalindromeTaskOutput adaptEntityToDTO(PalindromeTaskEntity taskEntity) {
        PalindromeTaskOutput taskOutput = new PalindromeTaskOutput()
                .withTask(taskEntity.getTask())
                .withStatus(taskEntity.getStatus().name().toLowerCase())
                .withTimestamps(
                        new Timestamps()
                                .withSubmitted(taskEntity.getSubmitted())
                                .withStarted(taskEntity.getStarted())
                                .withCompleted(taskEntity.getCompleted()))
                .withProblem(
                        new Problem().
                                withText(taskEntity.getText()));
        if (taskEntity.getLargestPalindrome() != null) {
            taskOutput.withSolution(
                    new Solution()
                            .withLargestPalindromeLength(taskEntity.getLargestPalindromeLength())
                            .withLargestPalindrome(taskEntity.getLargestPalindrome()));
        }
        return taskOutput;
    }

    @Override
    public PalindromeTaskOutput status(String taskId) throws NotFoundException {
        PalindromeTaskEntity task = palindromeTaskRepository.findById(taskId)
                .orElseThrow(NotFoundException::new);
        logger.debug("Retrieved the following record : {}", task);
        return adaptEntityToDTO(task);
    }
}