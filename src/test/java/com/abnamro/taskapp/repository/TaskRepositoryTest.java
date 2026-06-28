package com.abnamro.taskapp.repository;

import com.abnamro.taskapp.model.Task;
import com.abnamro.taskapp.model.TaskPriority;
import com.abnamro.taskapp.model.TaskStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for TaskRepository using @DataJpaTest.
 */
@DataJpaTest
@ActiveProfiles("dev")
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    private Task save(String title, TaskStatus status, TaskPriority priority) {
        Task task = Task.builder()
                .title(title)
                .status(status)
                .priority(priority)
                .build();
        return taskRepository.save(task);
    }

    @Test
    void save_persist() {
        Task saved = save("New task", TaskStatus.OPEN, TaskPriority.LOW);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void findById_whenExists_returnsTask() {
        Task saved = save("Findable task", TaskStatus.OPEN, TaskPriority.MEDIUM);

        Optional<Task> result = taskRepository.findById(saved.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Findable task");
    }

    @Test
    void findById_whenMissing_returnsEmpty() {
        Optional<Task> result = taskRepository.findById(UUID.randomUUID());

        assertThat(result).isEmpty();
    }

    @Test
    void findByStatus_returnsOnlyMatchingTasks() {
        save("Open task 1", TaskStatus.OPEN, TaskPriority.LOW);
        save("Open task 2", TaskStatus.OPEN, TaskPriority.HIGH);
        save("Done task", TaskStatus.DONE, TaskPriority.LOW);

        List<Task> result = taskRepository.findByStatus(TaskStatus.OPEN);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(t -> t.getStatus() == TaskStatus.OPEN);
    }

    @Test
    void findByPriority_returnsOnlyMatchingTasks() {
        save("High prio 1", TaskStatus.OPEN, TaskPriority.HIGH);
        save("High prio 2", TaskStatus.IN_PROGRESS, TaskPriority.HIGH);
        save("Low prio", TaskStatus.OPEN, TaskPriority.LOW);

        List<Task> result = taskRepository.findByPriority(TaskPriority.HIGH);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(t -> t.getPriority() == TaskPriority.HIGH);
    }

    @Test
    void findByStatusAndPriority_returnsOnlyTasksMatchingBoth() {
        Task target = save("Target task", TaskStatus.OPEN, TaskPriority.HIGH);
        save("Wrong status", TaskStatus.DONE, TaskPriority.HIGH);
        save("Wrong priority", TaskStatus.OPEN, TaskPriority.LOW);

        List<Task> result = taskRepository.findByStatusAndPriority(TaskStatus.OPEN, TaskPriority.HIGH);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(target.getId());
    }

    @Test
    void findByStatus_withNoMatches_returnsEmptyList() {
        save("Some task", TaskStatus.OPEN, TaskPriority.LOW);

        List<Task> result = taskRepository.findByStatus(TaskStatus.DONE);

        assertThat(result).isEmpty();
    }

    @Test
    void delete_removesTaskFromDatabase() {
        Task saved = save("Temporary task", TaskStatus.OPEN, TaskPriority.LOW);
        UUID id = saved.getId();

        taskRepository.delete(saved);

        assertThat(taskRepository.findById(id)).isEmpty();
    }

    @Test
    void onUpdate_changesUpdatedAtButNotCreatedAt() throws InterruptedException {
        Task saved = save("Task to update", TaskStatus.OPEN, TaskPriority.LOW);
        var originalCreatedAt = saved.getCreatedAt();
        var originalUpdatedAt = saved.getUpdatedAt();

        // Sleep for a short duration to ensure the updatedAt timestamp will be different.
        Thread.sleep(5);

        saved.setStatus(TaskStatus.DONE);
        Task updated = taskRepository.saveAndFlush(saved);

        assertThat(updated.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(updated.getUpdatedAt()).isAfter(originalUpdatedAt);
    }
}