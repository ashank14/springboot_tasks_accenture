package spring_tasks.spring_project.dto;

import jakarta.validation.constraints.NotNull;

public record GoogleApiRequestDTO(@NotNull(message = "id is required") String id){}

