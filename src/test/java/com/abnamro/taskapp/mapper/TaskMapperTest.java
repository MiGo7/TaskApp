package com.abnamro.taskapp.mapper;

import com.abnamro.taskapp.dto.TaskRequest;
import com.abnamro.taskapp.dto.TaskResponse;
import com.abnamro.taskapp.model.Task;
import com.abnamro.taskapp.model.TaskPriority;
import com.abnamro.taskapp.model.TaskStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for TaskMapper, verifying that it correctly maps between TaskRequest, Task, and TaskResponse.
 */
class TaskMapperTest {

    private final TaskMapper mapper = new TaskMapper();

    @Test
    void toEntity_mapsAllRequestFieldsOntoNewEntity() {
        TaskRequest request = new TaskRequest(
                "Write report", "Quarterly summary", TaskStatus.OPEN, TaskPriority.HIGH);

        Task result = mapper.toEntity(request);

        assertThat(result.getTitle()).isEqualTo("Write report");
        assertThat(result.getDescription()).isEqualTo("Quarterly summary");
        assertThat(result.getStatus()).isEqualTo(TaskStatus.OPEN);
        assertThat(result.getPriority()).isEqualTo(TaskPriority.HIGH);
    }

    @Test
    void toEntity_neverSetsTimeFields() {

        TaskRequest request = new TaskRequest("Title", null, TaskStatus.OPEN, TaskPriority.LOW);

        Task result = mapper.toEntity(request);

        assertThat(result.getId()).isNull();
        assertThat(result.getCreatedAt()).isNull();
        assertThat(result.getUpdatedAt()).isNull();
    }

    @Test
    void toEntity_withNullDescription() {
        TaskRequest request = new TaskRequest("Title only", null, TaskStatus.OPEN, TaskPriority.LOW);

        Task result = mapper.toEntity(request);

        assertThat(result.getDescription()).isNull();
    }

    @Test
    void toResponse_mapsAllEntityFields() {
        UUID id = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-01-01T10:00:00Z");
        Instant updatedAt = Instant.parse("2026-01-02T11:30:00Z");

        Task task = Task.builder()
                .id(id)
                .title("Existing task")
                .description("Some description")
                .status(TaskStatus.IN_PROGRESS)
                .priority(TaskPriority.MEDIUM)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        TaskResponse result = mapper.toResponse(task);

        assertThat(result.id()).isEqualTo(id);
        assertThat(result.title()).isEqualTo("Existing task");
        assertThat(result.description()).isEqualTo("Some description");
        assertThat(result.status()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(result.priority()).isEqualTo(TaskPriority.MEDIUM);
        assertThat(result.createdAt()).isEqualTo(createdAt);
        assertThat(result.updatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void toResponse_withNullDescription() {
        Task task = Task.builder()
                .id(UUID.randomUUID())
                .title("No description task")
                .description(null)
                .status(TaskStatus.OPEN)
                .priority(TaskPriority.LOW)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        TaskResponse result = mapper.toResponse(task);

        assertThat(result.description()).isNull();
    }
}