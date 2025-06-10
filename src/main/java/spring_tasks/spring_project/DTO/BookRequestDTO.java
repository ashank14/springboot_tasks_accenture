package spring_tasks.spring_project.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record BookRequestDTO (

    @NotNull(message = "Title required")
    @Size(min=1, max=100, message = "Title must be between 1 to 100 characters")
    String title,

    @NotNull(message = "Author is required")
    @Size(min=1, max=100, message = "Author must be between 1 to 100 characters")
    String author,

    @NotNull(message = "Published date is required")
    @PastOrPresent(message= "Published Date must be in the past or present")
    LocalDate publishedDate
    ){}

