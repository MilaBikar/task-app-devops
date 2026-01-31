package com.example.taskservice.dto;

import com.example.taskservice.model.Task;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskDTO {

    private Long id;

    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 200, message = "Title must be between 1 and 200 characters")
    private String title;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    //@NotNull(message = "Status is required")
    private Task.TaskStatus status;

    @NotNull(message = "User ID is required")
    private Long userId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}