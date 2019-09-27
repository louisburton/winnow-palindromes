package com.quickcamel.winnow.palindromes.rest.dto;

public class Timestamps {

    private Long submitted;
    private Long started;
    private Long completed;

    public Long getSubmitted() {
        return submitted;
    }

    public Long getStarted() {
        return started;
    }

    public Long getCompleted() {
        return completed;
    }

    public Timestamps withSubmitted(final Long submitted) {
        this.submitted = submitted;
        return this;
    }

    public Timestamps withStarted(final Long started) {
        this.started = started;
        return this;
    }

    public Timestamps withCompleted(final Long completed) {
        this.completed = completed;
        return this;
    }


    @Override
    public String toString() {
        return "Timestamps{" +
                "submitted=" + submitted +
                ", started=" + started +
                ", completed=" + completed +
                '}';
    }
}
