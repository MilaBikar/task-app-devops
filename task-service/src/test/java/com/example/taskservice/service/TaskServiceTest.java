package com.example.taskservice.service;

import com.example.taskservice.dto.TaskDTO;
import com.example.taskservice.event.TaskEvent;
import com.example.taskservice.messaging.TaskEventPublisher;
import com.example.taskservice.model.Task;
import com.example.taskservice.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UNIT TESTS - TaskService")
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskEventPublisher taskEventPublisher;

    @InjectMocks
    private TaskService taskService;

    private TaskDTO testTaskDTO;
    private Task testTask;

    @BeforeEach
    void setUp() {
        testTaskDTO = new TaskDTO();
        testTaskDTO.setTitle("Test Task");
        testTaskDTO.setDescription("Test Description");
        testTaskDTO.setStatus(Task.TaskStatus.TODO);
        testTaskDTO.setUserId(1L);

        testTask = new Task();
        testTask.setId(1L);
        testTask.setTitle("Test Task");
        testTask.setDescription("Test Description");
        testTask.setStatus(Task.TaskStatus.TODO);
        testTask.setUserId(1L);
    }

    @Test
    @DisplayName("Should create task successfully")
    void shouldCreateTaskSuccessfully() {
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        TaskDTO result = taskService.createTask(testTaskDTO);

        assertNotNull(result);
        assertEquals("Test Task", result.getTitle());
        assertEquals("Test Description", result.getDescription());
        assertEquals(Task.TaskStatus.TODO, result.getStatus());
        assertEquals(1L, result.getUserId());

        verify(taskRepository, times(1)).save(any(Task.class));
        verify(taskEventPublisher, times(1)).publishTaskEvent(any(TaskEvent.class));
    }

    @Test
    @DisplayName("Should set default status to TODO when not provided")
    void shouldSetDefaultStatusToTodo() {
        testTaskDTO.setStatus(null);
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        TaskDTO result = taskService.createTask(testTaskDTO);

        assertNotNull(result);
        verify(taskRepository, times(1)).save(any(Task.class));
        verify(taskEventPublisher, times(1)).publishTaskEvent(any(TaskEvent.class));
    }

    @Test
    @DisplayName("Should get task by ID successfully")
    void shouldGetTaskByIdSuccessfully() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        TaskDTO result = taskService.getTaskById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Task", result.getTitle());
        verify(taskRepository, times(1)).findById(1L);
        verifyNoInteractions(taskEventPublisher);
    }

    @Test
    @DisplayName("Should throw exception when task not found by ID")
    void shouldThrowExceptionWhenTaskNotFoundById() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        TaskService.ResourceNotFoundException exception = assertThrows(
                TaskService.ResourceNotFoundException.class,
                () -> taskService.getTaskById(999L)
        );

        assertEquals("Task not found with id: 999", exception.getMessage());
        verifyNoInteractions(taskEventPublisher);
    }

    @Test
    @DisplayName("Should get all tasks successfully")
    void shouldGetAllTasksSuccessfully() {
        Task task2 = new Task();
        task2.setId(2L);
        task2.setTitle("Task 2");
        task2.setStatus(Task.TaskStatus.IN_PROGRESS);

        when(taskRepository.findAll()).thenReturn(Arrays.asList(testTask, task2));

        List<TaskDTO> result = taskService.getAllTasks();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Test Task", result.get(0).getTitle());
        assertEquals("Task 2", result.get(1).getTitle());
        verify(taskRepository, times(1)).findAll();
        verifyNoInteractions(taskEventPublisher);
    }

    @Test
    @DisplayName("Should get tasks by user ID successfully")
    void shouldGetTasksByUserIdSuccessfully() {
        when(taskRepository.findByUserId(1L)).thenReturn(Arrays.asList(testTask));

        List<TaskDTO> result = taskService.getTasksByUserId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getUserId());
        verify(taskRepository, times(1)).findByUserId(1L);
        verifyNoInteractions(taskEventPublisher);
    }

    @Test
    @DisplayName("Should get tasks by status successfully")
    void shouldGetTasksByStatusSuccessfully() {
        when(taskRepository.findByStatus(Task.TaskStatus.TODO))
                .thenReturn(Arrays.asList(testTask));

        List<TaskDTO> result = taskService.getTasksByStatus(Task.TaskStatus.TODO);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Task.TaskStatus.TODO, result.get(0).getStatus());
        verify(taskRepository, times(1)).findByStatus(Task.TaskStatus.TODO);
        verifyNoInteractions(taskEventPublisher);
    }

    @Test
    @DisplayName("Should update task successfully")
    void shouldUpdateTaskSuccessfully() {
        TaskDTO updateDTO = new TaskDTO();
        updateDTO.setTitle("Updated Task");
        updateDTO.setDescription("Updated Description");
        updateDTO.setStatus(Task.TaskStatus.DONE);
        updateDTO.setUserId(1L);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        TaskDTO result = taskService.updateTask(1L, updateDTO);

        assertNotNull(result);
        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).save(any(Task.class));
        verify(taskEventPublisher, times(1)).publishTaskEvent(any(TaskEvent.class));
    }

    @Test
    @DisplayName("Should delete task successfully")
    void shouldDeleteTaskSuccessfully() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        doNothing().when(taskRepository).deleteById(1L);

        taskService.deleteTask(1L);

        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).deleteById(1L);
        verify(taskEventPublisher, times(1)).publishTaskEvent(any(TaskEvent.class));
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent task")
    void shouldThrowExceptionWhenDeletingNonExistentTask() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        TaskService.ResourceNotFoundException exception = assertThrows(
                TaskService.ResourceNotFoundException.class,
                () -> taskService.deleteTask(999L)
        );

        assertEquals("Task not found with id: 999", exception.getMessage());
        verify(taskRepository, never()).deleteById(anyLong());
        verifyNoInteractions(taskEventPublisher);
    }

    @Test
    @DisplayName("Should handle null description")
    void shouldHandleNullDescription() {
        testTaskDTO.setDescription(null);
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        TaskDTO result = taskService.createTask(testTaskDTO);

        assertNotNull(result);
        verify(taskRepository, times(1)).save(any(Task.class));
        verify(taskEventPublisher, times(1)).publishTaskEvent(any(TaskEvent.class));
    }

    @Test
    @DisplayName("Should publish TASK_CREATED event with correct data")
    void shouldPublishCorrectEventOnCreate() {
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        taskService.createTask(testTaskDTO);

        verify(taskEventPublisher).publishTaskEvent(argThat(event ->
                event.getEventType().equals("TASK_CREATED") &&
                        event.getTaskId().equals(1L) &&
                        event.getUserId().equals(1L) &&
                        event.getTitle().equals("Test Task")
        ));
    }

    @Test
    @DisplayName("Should publish TASK_DELETED event with correct data")
    void shouldPublishCorrectEventOnDelete() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        doNothing().when(taskRepository).deleteById(1L);

        taskService.deleteTask(1L);

        verify(taskEventPublisher).publishTaskEvent(argThat(event ->
                event.getEventType().equals("TASK_DELETED") &&
                        event.getTaskId().equals(1L) &&
                        event.getUserId().equals(1L)
        ));
    }
}