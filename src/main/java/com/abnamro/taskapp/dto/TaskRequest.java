package com.abnamro.taskapp.dto;

import com.abnamro.taskapp.model.TaskPriority;
import com.abnamro.taskapp.model.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Payload for creating or updating a Task.
 */
public record TaskRequest(

        @NotBlank(message = "title must not be blank")
        @Size(min = 3, max = 255, message = "title must be between 3 and 255 characters")
        String title,

        @Size(max = 2000, message = "description must not exceed 2000 characters")
        String description,

        @NotNull(message = "status must not be null")
        TaskStatus status,

        @NotNull(message = "priority must not be null")
        TaskPriority priority
) {
}