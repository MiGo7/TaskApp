package com.abnamro.taskapp.service;

import com.abnamro.taskapp.dto.TaskRequest;
import com.abnamro.taskapp.dto.TaskResponse;
import com.abnamro.taskapp.exception.TaskNotFoundException;
import com.abnamro.taskapp.mapper.TaskMapper;
import com.abnamro.taskapp.model.Task;
import com.abnamro.taskapp.model.TaskPriority;
import com.abnamro.taskapp.model.TaskStatus;
import com.abnamro.taskapp.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * TaskService for CRUD operations.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    @Override
    @Transactional
    public TaskResponse createTask(TaskRequest request) {
        Task task = taskMapper.toEntity(request);
        Task saved = taskRepository.save(task);
        return taskMapper.toResponse(saved);
    }

    @Override
    public TaskResponse getTaskById(UUID id) {
        Task task = findTaskOrThrow(id);
        return taskMapper.toResponse(task);
    }

    @Override
    public List<TaskResponse> getTasks(TaskStatus status, TaskPriority priority) {
        List<Task> tasks;

        if (status != null && priority != null) {
            tasks = taskRepository.findByStatusAndPriority(status, priority);
        } else if (status != null) {
            tasks = taskRepository.findByStatus(status);
        } else if (priority != null) {
            tasks = taskRepository.findByPriority(priority);
        } else {
            tasks = taskRepository.findAll();
        }

        return tasks.stream()
                .map(taskMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public TaskResponse updateTask(UUID id, TaskRequest request) {
        Task taskindb = findTaskOrThrow(id);

        taskindb.setTitle(request.title());
        taskindb.setDescription(request.description());
        taskindb.setStatus(request.status());
        taskindb.setPriority(request.priority());

        Task saved = taskRepository.save(taskindb);
        return taskMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteTask(UUID id) {
        Task existing = findTaskOrThrow(id);
        taskRepository.delete(existing);
    }

    private Task findTaskOrThrow(UUID id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }
}