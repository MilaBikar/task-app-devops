package com.example.taskservice.controller;

import com.example.taskservice.dto.TaskDTO;
import com.example.taskservice.model.Task;
import com.example.taskservice.repository.TaskRepository;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("INTEGRATION TESTS - TaskController")
class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
    }

    @Test
    @DisplayName("Should create task via POST /api/tasks")
    void shouldCreateTaskViaAPI() throws Exception {
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setTitle("Integration Test Task");
        taskDTO.setDescription("Test Description");
        taskDTO.setStatus(Task.TaskStatus.TODO);
        taskDTO.setUserId(1L);

        String taskJson = objectMapper.writeValueAsString(taskDTO);

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(taskJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Integration Test Task"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());

        Task savedTask = taskRepository.findAll().get(0);
        assertEquals("Integration Test Task", savedTask.getTitle());
        assertEquals(1L, savedTask.getUserId());
    }

    @Test
    @DisplayName("Should return 400 when title is blank")
    void shouldReturn400WhenTitleBlank() throws Exception {
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setTitle("");
        taskDTO.setDescription("Description");
        taskDTO.setStatus(Task.TaskStatus.TODO);
        taskDTO.setUserId(1L);

        String taskJson = objectMapper.writeValueAsString(taskDTO);

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(taskJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should get all tasks via GET /api/tasks")
    void shouldGetAllTasks() throws Exception {
        Task task1 = new Task();
        task1.setTitle("Task 1");
        task1.setDescription("Description 1");
        task1.setStatus(Task.TaskStatus.TODO);
        task1.setUserId(1L);
        taskRepository.save(task1);

        Task task2 = new Task();
        task2.setTitle("Task 2");
        task2.setDescription("Description 2");
        task2.setStatus(Task.TaskStatus.IN_PROGRESS);
        task2.setUserId(2L);
        taskRepository.save(task2);

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title").value("Task 1"))
                .andExpect(jsonPath("$[1].title").value("Task 2"));
    }

    @Test
    @DisplayName("Should get task by ID via GET /api/tasks/{id}")
    void shouldGetTaskById() throws Exception {
        Task task = new Task();
        task.setTitle("Find Me");
        task.setDescription("Description");
        task.setStatus(Task.TaskStatus.TODO);
        task.setUserId(1L);
        Task savedTask = taskRepository.save(task);

        mockMvc.perform(get("/api/tasks/" + savedTask.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedTask.getId()))
                .andExpect(jsonPath("$.title").value("Find Me"))
                .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    @DisplayName("Should return 404 when task not found")
    void shouldReturn404WhenTaskNotFound() throws Exception {
        mockMvc.perform(get("/api/tasks/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("Task not found")));
    }

    @Test
    @DisplayName("Should get tasks by user ID via GET /api/tasks/user/{userId}")
    void shouldGetTasksByUserId() throws Exception {
        Task task1 = new Task();
        task1.setTitle("User 1 Task 1");
        task1.setStatus(Task.TaskStatus.TODO);
        task1.setUserId(1L);
        taskRepository.save(task1);

        Task task2 = new Task();
        task2.setTitle("User 1 Task 2");
        task2.setStatus(Task.TaskStatus.IN_PROGRESS);
        task2.setUserId(1L);
        taskRepository.save(task2);

        Task task3 = new Task();
        task3.setTitle("User 2 Task");
        task3.setStatus(Task.TaskStatus.TODO);
        task3.setUserId(2L);
        taskRepository.save(task3);

        mockMvc.perform(get("/api/tasks/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].userId").value(1))
                .andExpect(jsonPath("$[1].userId").value(1));
    }

    @Test
    @DisplayName("Should get tasks by status via GET /api/tasks/status/{status}")
    void shouldGetTasksByStatus() throws Exception {
        Task todoTask = new Task();
        todoTask.setTitle("TODO Task");
        todoTask.setStatus(Task.TaskStatus.TODO);
        todoTask.setUserId(1L);
        taskRepository.save(todoTask);

        Task doneTask = new Task();
        doneTask.setTitle("DONE Task");
        doneTask.setStatus(Task.TaskStatus.DONE);
        doneTask.setUserId(1L);
        taskRepository.save(doneTask);

        mockMvc.perform(get("/api/tasks/status/TODO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("TODO"))
                .andExpect(jsonPath("$[0].title").value("TODO Task"));
    }

    @Test
    @DisplayName("Should update task via PUT /api/tasks/{id}")
    void shouldUpdateTask() throws Exception {
        Task task = new Task();
        task.setTitle("Old Title");
        task.setDescription("Old Description");
        task.setStatus(Task.TaskStatus.TODO);
        task.setUserId(1L);
        Task savedTask = taskRepository.save(task);

        TaskDTO updateDTO = new TaskDTO();
        updateDTO.setTitle("New Title");
        updateDTO.setDescription("New Description");
        updateDTO.setStatus(Task.TaskStatus.DONE);
        updateDTO.setUserId(1L);

        String updateJson = objectMapper.writeValueAsString(updateDTO);

        mockMvc.perform(put("/api/tasks/" + savedTask.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Title"))
                .andExpect(jsonPath("$.description").value("New Description"))
                .andExpect(jsonPath("$.status").value("DONE"));

        Task updatedTask = taskRepository.findById(savedTask.getId()).orElseThrow();
        assertEquals("New Title", updatedTask.getTitle());
        assertEquals(Task.TaskStatus.DONE, updatedTask.getStatus());
    }

    @Test
    @DisplayName("Should delete task via DELETE /api/tasks/{id}")
    void shouldDeleteTask() throws Exception {
        Task task = new Task();
        task.setTitle("To Delete");
        task.setStatus(Task.TaskStatus.TODO);
        task.setUserId(1L);
        Task savedTask = taskRepository.save(task);

        mockMvc.perform(delete("/api/tasks/" + savedTask.getId()))
                .andExpect(status().isNoContent());

        assertFalse(taskRepository.existsById(savedTask.getId()));
    }

    @Test
    @DisplayName("Should create task with default TODO status when not provided")
    void shouldCreateTaskWithDefaultStatus() throws Exception {
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setTitle("Task Without Status");
        taskDTO.setDescription("Description");
        taskDTO.setUserId(1L);

        String taskJson = objectMapper.writeValueAsString(taskDTO);

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(taskJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("TODO"));
    }
}