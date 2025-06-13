package spring_tasks.spring_project.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record BookRequestDTO(

        @NotNull(message = "{title.required}")
        @Size(min = 1, max = 100, message = "{title.size}")
        String title,

        @NotNull(message = "{author.required}")
        @Size(min = 1, max = 100, message = "{author.size}")
        String author,

        @NotNull(message = "{publishedDate.required}")
        @PastOrPresent(message = "{publishedDate.pastOrPresent}")
        LocalDate publishedDate

) {}
