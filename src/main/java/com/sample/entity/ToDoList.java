package com.sample.entity;

public class ToDoList {
	
	private int tId;
	private String title;
	private boolean completed;
	
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
	public ToDoList() {
	}
	public ToDoList(String title, boolean completed) {
		this.title = title;
		this.completed = completed;
	}
}
