package com.todo.app.backend;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
/**
 * @author amran hossain
 *
 */

public interface ToDoRepository extends JpaRepository<ToDoTask, Long> {

	List<ToDoTask> findByItemNameLikeIgnoreCase(String itemName, Pageable pageable);
	
    List<ToDoTask> findAllBy(Pageable pageable);
    
    List<ToDoTask> findByItemNameLikeIgnoreCase(String nameFilter);
    
    long countByItemNameLikeIgnoreCase(String itemName);
}
