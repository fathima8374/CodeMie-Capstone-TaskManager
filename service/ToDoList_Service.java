package com.sample.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.sample.entity.ToDoList;

@Service
public class ToDoList_Service {
	
	private Map<Integer, ToDoList> tasks = new HashMap<>();
    private int idCounter = 1;
	
    public List<ToDoList> getAll(){
		return new ArrayList<>(tasks.values());	
    }
    
    public ToDoList getById(int id) {
		return tasks.get(id);
    }
    
    public ToDoList addTask(ToDoList todo) {
    	todo.settId(idCounter++);
    	tasks.put(todo.gettId(),todo);
    	return todo;	
    }
    
    public ToDoList updateTask(int id, ToDoList todo) {
		if(!tasks.containsKey(id)) return null;
		todo.settId(id);
    	tasks.put(id, todo);
    	return todo;	
    }

    public boolean deleteTask(int id) {
		return tasks.remove(id) != null;	
    }
}























