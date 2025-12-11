package com.sample.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sample.entity.ToDoList;

public interface ToDoListRepository extends JpaRepository<ToDoList, Integer> {
	

}
