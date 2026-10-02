package com.sample.entity;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.OptBoolean;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public class ToDoList {

    private int tId;

    @NotBlank(message = "title is required")
    private String title;
    private boolean completed;

    @JsonFormat(pattern = "uuuu-MM-dd", lenient = OptBoolean.FALSE)
    private LocalDate dueDate;

    @JsonProperty("tId")
    public int gettId() {
        return tId;
    }

    @JsonProperty("tId")
    public void settId(int tId) {
        this.tId = tId;
    }

    @JsonProperty("title")
    public String gettitle() {
        return title;
    }

    @JsonProperty("title")
    public void settitle(String title) {
        this.title = title;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    @JsonCreator
    public ToDoList() {
    }

    public ToDoList(String title, boolean completed) {
        this.title = title;
        this.completed = completed;
    }
}
