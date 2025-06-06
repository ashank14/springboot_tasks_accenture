package spring_tasks.spring_project.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class BookRequestDTO {

    @NotNull(message = "Title required")
    @Size(min=1, max=100, message = "Title must be between 1 to 100 characters")
    private String title;

    @NotNull(message = "Author is required")
    @Size(min=1, max=100, message = "Author must be between 1 to 100 characters")
    private String author;

    @NotNull(message = "Published date is required")
    private LocalDate publishedDate;

    public BookRequestDTO() {}

    public BookRequestDTO(String title, String author, LocalDate publishedDate){
        this.title = title;
        this.author = author;
        this.publishedDate = publishedDate;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }
    public void setAuthor(String author) {
        this.author = author;
    }

    public LocalDate getPublishedDate() {
        return publishedDate;
    }
    public void setPublishedDate(LocalDate publishedDate) {
        this.publishedDate = publishedDate;
    }
}