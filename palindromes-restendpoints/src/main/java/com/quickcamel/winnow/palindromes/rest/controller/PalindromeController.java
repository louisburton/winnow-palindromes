package com.quickcamel.winnow.palindromes.rest.controller;

import com.quickcamel.winnow.palindromes.rest.dto.PalindromeTaskOutput;
import com.quickcamel.winnow.palindromes.rest.task.NotFoundException;
import com.quickcamel.winnow.palindromes.rest.task.PalindromeTaskManager;
import com.quickcamel.winnow.palindromes.rest.dto.PalindromeTaskSubmission;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("palindrome")
public class PalindromeController {

    private PalindromeTaskManager palindromeTaskManager;
    private MessageSourceAccessor messageSourceAccessor;

    public PalindromeController(PalindromeTaskManager palindromeTaskManager, MessageSourceAccessor messageSourceAccessor) {
        this.palindromeTaskManager = palindromeTaskManager;
        this.messageSourceAccessor = messageSourceAccessor;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<PalindromeTaskOutput> createPalindromeTask(@RequestBody PalindromeTaskSubmission submission) {
        PalindromeTaskOutput taskOutput = palindromeTaskManager.submit(submission.getProblem());

        return ResponseEntity
                // return location to better meet RESTful expectations,
                // and avoid need to parse response object to poll for solution
                .created(getTaskLocation(taskOutput))
                // return entity as per spec TODO - clarify ambiguity around REST contract
                .body(taskOutput);
    }

    private URI getTaskLocation(PalindromeTaskOutput taskOutput) {
        return ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{task}")
                .buildAndExpand(taskOutput.getTask())
                .toUri();
    }

    @GetMapping(path = "/{task}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public PalindromeTaskOutput getPalindromeTaskOutput(@PathVariable(name = "task") String taskId) {
        try {
            return palindromeTaskManager.status(taskId);
        }
        catch (NotFoundException e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, messageSourceAccessor.getMessage("task.not.found", new Object[] {taskId}));

        }
    }
}
