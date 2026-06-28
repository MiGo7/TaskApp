package com.abnamro.taskapp.integration;

import com.abnamro.taskapp.dto.TaskRequest;
import com.abnamro.taskapp.model.TaskPriority;
import com.abnamro.taskapp.model.TaskStatus;
import com.abnamro.taskapp.repository.TaskRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test for full flow.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class TaskIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @AfterEach
    void cleanUp() {
        taskRepository.deleteAll();
    }

    @Test
    void fullLifecycle_createReadUpdateDelete() throws Exception {

        String createPayload = """
                {
                  "title": "Integration test task",
                  "description": "Created through the full real stack",
                  "status": "OPEN",
                  "priority": "MEDIUM"
                }
                """;

        String createResponseJson = mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Integration test task"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andReturn().getResponse().getContentAsString();

        UUID createdId = UUID.fromString(
                objectMapper.readTree(createResponseJson).get("id").asText());

        assertThat(taskRepository.findById(createdId)).isPresent();

        mockMvc.perform(get("/tasks/{id}", createdId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Integration test task"));

        String updatePayload = """
                {
                  "title": "Integration test task",
                  "description": "Now updated",
                  "status": "DONE",
                  "priority": "HIGH"
                }
                """;

        mockMvc.perform(put("/tasks/{id}", createdId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"))
                .andExpect(jsonPath("$.priority").value("HIGH"));

        var updated = taskRepository.findById(createdId).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(TaskStatus.DONE);
        assertThat(updated.getPriority()).isEqualTo(TaskPriority.HIGH);

        mockMvc.perform(delete("/tasks/{id}", createdId))
                .andExpect(status().isNoContent());

        assertThat(taskRepository.findById(createdId)).isEmpty();

        mockMvc.perform(get("/tasks/{id}", createdId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testFiltering() throws Exception {
        createTask("Open high", TaskStatus.OPEN, TaskPriority.HIGH);
        createTask("Open low", TaskStatus.OPEN, TaskPriority.LOW);
        createTask("Done high", TaskStatus.DONE, TaskPriority.HIGH);

        mockMvc.perform(get("/tasks").param("status", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(get("/tasks").param("priority", "HIGH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(get("/tasks").param("status", "OPEN").param("priority", "HIGH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Open high"));

        mockMvc.perform(get("/tasks").param("status", "DONE").param("priority", "LOW"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void createTask_withInvalidPayload() throws Exception {
        String invalidPayload = """
                {
                  "title": "Hi",
                  "status": "OPEN",
                  "priority": "LOW"
                }
                """;

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.title").exists());


        assertThat(taskRepository.findAll()).isEmpty();
    }

    private void createTask(String title, TaskStatus status, TaskPriority priority) throws Exception {
        String payload = objectMapper.writeValueAsString(new TaskRequest(
                title, null, status, priority));
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());
    }
}