package com.abnamro.taskapp.mapper;

import com.abnamro.taskapp.dto.TaskRequest;
import com.abnamro.taskapp.dto.TaskResponse;
import com.abnamro.taskapp.model.Task;
import org.springframework.stereotype.Component;

/**
 * Converts between the Task entity and its DTOs.
 *
 */
@Component
public class TaskMapper {

    public Task toEntity(TaskRequest request) {
        return Task.builder()
                .title(request.title())
                .description(request.description())
                .status(request.status())
                .priority(request.priority())
                .build();
    }

    public TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}