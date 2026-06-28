package com.abnamro.taskapp.service;

import com.abnamro.taskapp.dto.TaskRequest;
import com.abnamro.taskapp.dto.TaskResponse;
import com.abnamro.taskapp.exception.TaskNotFoundException;
import com.abnamro.taskapp.mapper.TaskMapper;
import com.abnamro.taskapp.model.Task;
import com.abnamro.taskapp.model.TaskPriority;
import com.abnamro.taskapp.model.TaskStatus;
import com.abnamro.taskapp.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TaskServiceImpl using Mockito to mock dependencies.
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskServiceImpl taskService;

    private UUID taskId;
    private Task existingTask;

    @BeforeEach
    void setUp() {
        taskId = UUID.randomUUID();
        existingTask = Task.builder()
                .id(taskId)
                .title("Existing task")
                .description("Existing description")
                .status(TaskStatus.OPEN)
                .priority(TaskPriority.MEDIUM)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    void createTask_savesAndReturnsMappedResponse() {
        TaskRequest request = new TaskRequest("New task", "desc", TaskStatus.OPEN, TaskPriority.HIGH);
        Task unsavedEntity = Task.builder().title("New task").build();
        Task savedEntity = Task.builder().id(taskId).title("New task").build();
        TaskResponse expectedResponse = new TaskResponse(
                taskId, "New task", "desc", TaskStatus.OPEN, TaskPriority.HIGH, Instant.now(), Instant.now());

        when(taskMapper.toEntity(request)).thenReturn(unsavedEntity);
        when(taskRepository.save(unsavedEntity)).thenReturn(savedEntity);
        when(taskMapper.toResponse(savedEntity)).thenReturn(expectedResponse);

        TaskResponse result = taskService.createTask(request);

        assertThat(result).isEqualTo(expectedResponse);
        verify(taskRepository).save(unsavedEntity);
    }


    @Test
    void getTaskById_returnsMappedResponse() {
        TaskResponse expectedResponse = new TaskResponse(
                taskId, "Existing task", "Existing description",
                TaskStatus.OPEN, TaskPriority.MEDIUM, Instant.now(), Instant.now());

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(taskMapper.toResponse(existingTask)).thenReturn(expectedResponse);

        TaskResponse result = taskService.getTaskById(taskId);

        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    void getTaskById_throwsTaskNotFoundException() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTaskById(taskId))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining(taskId.toString());

        verify(taskMapper, never()).toResponse(any());
    }

    @Test
    void getTasks_withNoFilters() {
        when(taskRepository.findAll()).thenReturn(List.of(existingTask));
        when(taskMapper.toResponse(existingTask)).thenReturn(mock(TaskResponse.class));

        List<TaskResponse> result = taskService.getTasks(null, null);

        assertThat(result).hasSize(1);
        verify(taskRepository).findAll();
        verify(taskRepository, never()).findByStatus(any());
        verify(taskRepository, never()).findByPriority(any());
        verify(taskRepository, never()).findByStatusAndPriority(any(), any());
    }

    @Test
    void getTasks_withStatusOnly() {
        when(taskRepository.findByStatus(TaskStatus.OPEN)).thenReturn(List.of(existingTask));
        when(taskMapper.toResponse(existingTask)).thenReturn(mock(TaskResponse.class));

        List<TaskResponse> result = taskService.getTasks(TaskStatus.OPEN, null);

        assertThat(result).hasSize(1);
        verify(taskRepository).findByStatus(TaskStatus.OPEN);
        verify(taskRepository, never()).findAll();
    }

    @Test
    void getTasks_withPriorityOnly() {
        when(taskRepository.findByPriority(TaskPriority.HIGH)).thenReturn(List.of(existingTask));
        when(taskMapper.toResponse(existingTask)).thenReturn(mock(TaskResponse.class));

        List<TaskResponse> result = taskService.getTasks(null, TaskPriority.HIGH);

        assertThat(result).hasSize(1);
        verify(taskRepository).findByPriority(TaskPriority.HIGH);
    }

    @Test
    void getTasks_withBothFilters() {
        when(taskRepository.findByStatusAndPriority(TaskStatus.OPEN, TaskPriority.HIGH))
                .thenReturn(List.of(existingTask));
        when(taskMapper.toResponse(existingTask)).thenReturn(mock(TaskResponse.class));

        List<TaskResponse> result = taskService.getTasks(TaskStatus.OPEN, TaskPriority.HIGH);

        assertThat(result).hasSize(1);
        verify(taskRepository).findByStatusAndPriority(TaskStatus.OPEN, TaskPriority.HIGH);
    }

    @Test
    void getTasks_withNoMatches() {
        when(taskRepository.findByStatus(TaskStatus.DONE)).thenReturn(List.of());

        List<TaskResponse> result = taskService.getTasks(TaskStatus.DONE, null);

        assertThat(result).isEmpty();
    }


    @Test
    void updateTask_taskExists() {
        TaskRequest request = new TaskRequest(
                "Updated title", "Updated description", TaskStatus.DONE, TaskPriority.LOW);
        TaskResponse expectedResponse = new TaskResponse(
                taskId, "Updated title", "Updated description",
                TaskStatus.DONE, TaskPriority.LOW, Instant.now(), Instant.now());

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(existingTask)).thenReturn(existingTask);
        when(taskMapper.toResponse(existingTask)).thenReturn(expectedResponse);

        TaskResponse result = taskService.updateTask(taskId, request);

        assertThat(existingTask.getTitle()).isEqualTo("Updated title");
        assertThat(existingTask.getDescription()).isEqualTo("Updated description");
        assertThat(existingTask.getStatus()).isEqualTo(TaskStatus.DONE);
        assertThat(existingTask.getPriority()).isEqualTo(TaskPriority.LOW);
        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    void updateTask_whenMissingTask() {
        TaskRequest request = new TaskRequest("Title", null, TaskStatus.OPEN, TaskPriority.LOW);
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.updateTask(taskId, request))
                .isInstanceOf(TaskNotFoundException.class);

        verify(taskRepository, never()).save(any());
    }


    @Test
    void deleteTask_taskExists() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));

        taskService.deleteTask(taskId);

        verify(taskRepository).delete(existingTask);
    }

    @Test
    void deleteTask_whenMissing() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.deleteTask(taskId))
                .isInstanceOf(TaskNotFoundException.class);

        verify(taskRepository, never()).delete(any());
    }
}