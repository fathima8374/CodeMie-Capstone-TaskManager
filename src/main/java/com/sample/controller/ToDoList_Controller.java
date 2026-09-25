package com.sample.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sample.entity.ToDoList;
import com.sample.service.ToDoList_Service;

@RestController
@RequestMapping("/todo")
public class ToDoList_Controller {

	@Autowired
	private ToDoList_Service todoService;
	
	@GetMapping("/readall")
	public List<ToDoList> getAllToDoList(){
		return todoService.getAll();
	}
	
	@GetMapping("/read/{id}")
	public ToDoList getById(@PathVariable int id) {
		return todoService.getById(id);	
	}
	
	@PostMapping
	public ToDoList add(@RequestBody ToDoList todo) {
	    System.out.println("TITLE = " + todo.gettitle());
	    System.out.println("COMPLETED = " + todo.isCompleted());
	    return todoService.addTask(todo);
	}

	@PutMapping("/update/{id}")
	public ToDoList update(@PathVariable int id,@RequestBody ToDoList todo) {
		return todoService.updateTask(id, todo);
	}
	
	@DeleteMapping("/delete/{id}")
	public String delete(@PathVariable int id) {
		return todoService.deleteTask(id) ? "Deleted" : "Not Found";
		
	}
}



















