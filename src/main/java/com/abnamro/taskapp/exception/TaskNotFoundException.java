package com.abnamro.taskapp.exception;

import java.util.UUID;

/**
 * Thrown when a Task with a given id does not exist.
 *
 */
public class TaskNotFoundException extends RuntimeException {

    public TaskNotFoundException(UUID id) {
        super("Task not found with id: " + id);
    }
}