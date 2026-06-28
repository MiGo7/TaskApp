package com.abnamro.taskapp.controller;

import com.abnamro.taskapp.dto.TaskRequest;
import com.abnamro.taskapp.dto.TaskResponse;
import com.abnamro.taskapp.exception.TaskNotFoundException;
import com.abnamro.taskapp.model.TaskPriority;
import com.abnamro.taskapp.model.TaskStatus;
import com.abnamro.taskapp.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for TaskController using @WebMvcTest.
 */
@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    private static final UUID TASK_ID = UUID.randomUUID();

    private TaskResponse sampleResponse() {
        return new TaskResponse(
                TASK_ID, "Sample task", "Sample description",
                TaskStatus.OPEN, TaskPriority.HIGH, Instant.now(), Instant.now());
    }

    @Test
    void createTask_withValidPayload_returns201() throws Exception {
        when(taskService.createTask(any(TaskRequest.class))).thenReturn(sampleResponse());

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Sample task",
                                  "description": "Sample description",
                                  "status": "OPEN",
                                  "priority": "HIGH"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/tasks/" + TASK_ID))
                .andExpect(jsonPath("$.id").value(TASK_ID.toString()))
                .andExpect(jsonPath("$.title").value("Sample task"));
    }

    @Test
    void createTask_withTitleTooShort_returns400WithFieldError() throws Exception {
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Hi",
                                  "status": "OPEN",
                                  "priority": "HIGH"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.title").exists());
    }

    @Test
    void createTask_withMissingPriority_returns400WithFieldError() throws Exception {
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Valid title",
                                  "status": "OPEN"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.priority").exists());
    }

    @Test
    void createTask_withInvalidEnumValue_returns400() throws Exception {
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Valid title",
                                  "status": "OPEN",
                                  "priority": "URGENT"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("URGENT")));
    }


    @Test
    void getTaskById_whenExists_returns200() throws Exception {
        when(taskService.getTaskById(TASK_ID)).thenReturn(sampleResponse());

        mockMvc.perform(get("/tasks/{id}", TASK_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TASK_ID.toString()));
    }

    @Test
    void getTaskById_whenMissing_returns404() throws Exception {
        when(taskService.getTaskById(TASK_ID)).thenThrow(new TaskNotFoundException(TASK_ID));

        mockMvc.perform(get("/tasks/{id}", TASK_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString(TASK_ID.toString())));
    }

    @Test
    void getTaskById_invalidUuid_returns400() throws Exception {
        mockMvc.perform(get("/tasks/{id}", "invalid-uuid"))
                .andExpect(status().isBadRequest());
    }

    //filtering tests for GET /tasks endpoint
    @Test
    void getTasks_withNoParams_returns200() throws Exception {
        when(taskService.getTasks(null, null)).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getTasks_withStatusFilter_returns200() throws Exception {
        when(taskService.getTasks(eq(TaskStatus.OPEN), eq(null))).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/tasks").param("status", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getTasks_withNoMatches_returns200() throws Exception {
        when(taskService.getTasks(eq(TaskStatus.DONE), eq(null))).thenReturn(List.of());

        mockMvc.perform(get("/tasks").param("status", "DONE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }


    @Test
    void updateTask_withValidPayload_returns200() throws Exception {
        when(taskService.updateTask(eq(TASK_ID), any(TaskRequest.class))).thenReturn(sampleResponse());

        mockMvc.perform(put("/tasks/{id}", TASK_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Sample task",
                                  "description": "Sample description",
                                  "status": "OPEN",
                                  "priority": "HIGH"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TASK_ID.toString()));
    }

    @Test
    void updateTask_whenMissingTask_returns404() throws Exception {
        when(taskService.updateTask(eq(TASK_ID), any(TaskRequest.class)))
                .thenThrow(new TaskNotFoundException(TASK_ID));

        mockMvc.perform(put("/tasks/{id}", TASK_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Sample task",
                                  "status": "OPEN",
                                  "priority": "HIGH"
                                }
                                """))
                .andExpect(status().isNotFound());
    }


    @Test
    void deleteTask_whenExists_returns204() throws Exception {
        mockMvc.perform(delete("/tasks/{id}", TASK_ID))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteTask_whenMissingTask_returns404() throws Exception {
        org.mockito.Mockito.doThrow(new TaskNotFoundException(TASK_ID))
                .when(taskService).deleteTask(TASK_ID);

        mockMvc.perform(delete("/tasks/{id}", TASK_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTaskById_whenServiceThrowsUnexpectedException_returns500() throws Exception {
        when(taskService.getTaskById(TASK_ID))
                .thenThrow(new RuntimeException("some internal SQL detail that must not leak"));

        mockMvc.perform(get("/tasks/{id}", TASK_ID))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }
}