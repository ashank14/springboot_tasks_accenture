package spring_tasks.spring_project.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class BookRequestDTO {

    @NotNull(message = "Title required")
    @Size(min=1,max=100,message = "Title must be between 1 to 100 characters")
    public String title;
    @NotNull(message = "Author is required")
    @Size(min=1,max=100,message = "Author must be between 1 to 100 characters")
    public String author;
    @NotNull(message = "Published date is required")
    public LocalDate publishedDate;

    public BookRequestDTO(String title, String author, LocalDate publishedDate){
        this.title=title;
        this.author=author;
        this.publishedDate=publishedDate;
    }
}

