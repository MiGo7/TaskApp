package com.abnamro.taskapp.service;

import com.abnamro.taskapp.dto.TaskRequest;
import com.abnamro.taskapp.dto.TaskResponse;
import com.abnamro.taskapp.model.TaskPriority;
import com.abnamro.taskapp.model.TaskStatus;

import java.util.List;
import java.util.UUID;

/**
 * Service Interface for Task management.
 */
public interface TaskService {

    TaskResponse createTask(TaskRequest request);

    TaskResponse getTaskById(UUID id);

    List<TaskResponse> getTasks(TaskStatus status, TaskPriority priority);

    TaskResponse updateTask(UUID id, TaskRequest request);

    void deleteTask(UUID id);
}