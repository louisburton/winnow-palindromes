package com.quickcamel.winnow.palindromes.service.task;

import com.quickcamel.winnow.palindromes.entities.PalindromeTaskEntity;
import com.quickcamel.winnow.palindromes.repositories.PalindromeTaskRepository;
import com.quickcamel.winnow.palindromes.service.parser.PalindromeParser;
import com.quickcamel.winnow.palindromes.service.parser.PalindromeSolution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BlockingPalindromeTaskProcessor implements PalindromeTaskProcessor {

    private static final Logger logger = LoggerFactory.getLogger(BlockingPalindromeTaskProcessor.class);

    private PalindromeParser parser;
    private PalindromeTaskRepository repository;

    public BlockingPalindromeTaskProcessor(PalindromeParser parser, PalindromeTaskRepository repository) {
        this.parser = parser;
        this.repository = repository;
    }

    @Override
    public void processPalindromeTask(String taskId) {
        logger.debug("Processing task {}", taskId);
        Optional<PalindromeTaskEntity> dbTask = repository.findById(taskId);
        if (dbTask.isEmpty()) {
            logger.error("Task details not found : {}", taskId);
        } else {
            PalindromeTaskEntity entity = dbTask.get();
            logger.debug("Task details found : {}", entity);
            processEntity(entity);
        }
    }

    private void processEntity(PalindromeTaskEntity entity) {
        String taskId = entity.getTask();
        if (submittedButNotStarted(entity)) {
            persistStartTask(entity);
        }
        if (startedButNotCompleted(entity)) {
            logger.debug("Parsing task {}", taskId);
            PalindromeSolution solution = parser.parse(entity.getText());
            long completedTime = System.currentTimeMillis();
            entity = repository.findById(taskId).orElseThrow();
            if (startedButNotCompleted(entity)) {
                persistTaskCompleted(entity, solution, completedTime);
            }
        }
    }

    private void persistStartTask(PalindromeTaskEntity entity) {
        logger.debug("Starting task {}", entity.getTask());
        entity.setStatus(PalindromeTaskEntity.Status.STARTED);
        entity.setStarted(System.currentTimeMillis());
        repository.save(entity);
    }

    private void persistTaskCompleted(PalindromeTaskEntity entity, PalindromeSolution solution, long completedTime) {
        logger.debug("Completing task {}", entity.getTask());
        entity.setStatus(PalindromeTaskEntity.Status.COMPLETED);
        entity.setCompleted(completedTime);
        if (solution.getLargestPalindromeLength() > 0) {
            entity.setLargestPalindrome(solution.getLargestPalindrome());
            entity.setLargestPalindromeLength(solution.getLargestPalindromeLength());
        }
        repository.save(entity);
    }

    private boolean submittedButNotStarted(PalindromeTaskEntity entity) {
        return entity.getStarted() == null && entity.getStatus().equals(PalindromeTaskEntity.Status.SUBMITTED);
    }

    private boolean startedButNotCompleted(PalindromeTaskEntity entity) {
        return entity.getCompleted() == null && entity.getStatus().equals(PalindromeTaskEntity.Status.STARTED);
    }
}
