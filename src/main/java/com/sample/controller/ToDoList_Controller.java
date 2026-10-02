package com.sample.controller;

import jakarta.validation.Valid;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.sample.error.NotFoundException;
import com.sample.service.ToDoList_Service;

@RestController
@RequestMapping("/todo")
public class ToDoList_Controller {

	@Autowired
	private ToDoList_Service todoService;
	
	
GETMapping("/readall")
	public List<ToDoList> getAllToDoList(){
		return todoService.getAll();
	}
	
	
GETMapping("/read/{id}")
		public ToDoList getById(@PathVariable int id) {
		ToDoList task = todoService.getById(id);
		if (task == null) {
				throw new NotFoundException("Task with id " + id + " not found");
		}
		return task;	
	}
	
		@PostMapping
		public ResponseEntity<ToDoList> add(@Valid @RequestBody ToDoList todo) {
		    ToDoList created = todoService.addTask(todo);
		    return ResponseEntity.status(HttpStatuS.CREATED).body(created);
		}

		@PutMapping("/update/{id}")
		public ToDoList update(@PathVariable int id,@Valid @RequestBody ToDoList todo) {
			ToDoList updated = todoService.updateTask(id, todo);
			if (updated == null) {
				throw new NotFoundException("Task with id " + id + " not found");
		}
			return updated;
	}
	
		@DeleteMapping("/delete/{id}")
		public ResponseEntity<Void> delete(@PathVariable int id) {
			boolean deleted = todoService.deleteTask(id);
			if (!deleted) {
				throw new NotFoundException("Task with id " + id + " not found");
			}
			return ResponseEntity.no Content().build();
		}
}
