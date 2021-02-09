package com.todo.ui;

import java.io.Serializable;

import com.todo.app.backend.ToDoTask;


/**
 * @author amran hossain
 *
 */

public class ToDoTaskModifiedEvent implements Serializable {

    private final ToDoTask toDoTask;

    public ToDoTaskModifiedEvent(ToDoTask p) {
        this.toDoTask = p;
    }

	public ToDoTask getToDoTask() {
		return toDoTask;
	}

 
    
}
