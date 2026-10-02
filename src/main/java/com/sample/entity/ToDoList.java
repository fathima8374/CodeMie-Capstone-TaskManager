package com.sample.entity;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;

public class ToDoList {

    private int tId;

    @NotBlank(message = "title is required")
    private String title;
    private boolean completed;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    public int gettId() {
        return tId;
    }

    public void settId(int tId) {
        this.tId = tId;
    }

    public String gettitle() {
        return title;
    }

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

    public ToDoList() {
    }

    public ToDoList(String title, boolean completed) {
        this.title = title;
        this.completed = completed;
    }
}
