package com.abnamro.taskapp.dto;

import com.abnamro.taskapp.model.TaskPriority;
import com.abnamro.taskapp.model.TaskStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Response returned by the API.
 */
public record TaskResponse(
        UUID id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        Instant createdAt,
        Instant updatedAt
) {
}