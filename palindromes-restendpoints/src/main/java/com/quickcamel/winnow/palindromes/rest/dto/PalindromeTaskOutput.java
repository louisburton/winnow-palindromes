package com.quickcamel.winnow.palindromes.rest.dto;

public class PalindromeTaskOutput {

    private String task;

    private String status;

    private Timestamps timestamps;
    private Problem problem;
    private Solution solution;

    public String getTask() {
        return task;
    }

    public String getStatus() {
        return status;
    }

    public Timestamps getTimestamps() {
        return timestamps;
    }

    public Problem getProblem() {
        return problem;
    }

    public Solution getSolution() {
        return solution;
    }

    public PalindromeTaskOutput withTask(final String task) {
        this.task = task;
        return this;
    }

    public PalindromeTaskOutput withStatus(final String status) {
        this.status = status;
        return this;
    }

    public PalindromeTaskOutput withTimestamps(final Timestamps timestamps) {
        this.timestamps = timestamps;
        return this;
    }

    public PalindromeTaskOutput withProblem(final Problem problem) {
        this.problem = problem;
        return this;
    }

    public PalindromeTaskOutput withSolution(final Solution solution) {
        this.solution = solution;
        return this;
    }


    @Override
    public String toString() {
        return "PalindromeTask{" +
                "task='" + task + '\'' +
                ", status=" + status +
                ", timestamps=" + timestamps +
                ", problem=" + problem +
                ", solution=" + solution +
                '}';
    }
}

