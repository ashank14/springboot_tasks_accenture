package spring_tasks.spring_project.DTO;

import java.time.LocalDate;

public class BookResponseDTO {
    public int id;
    public String title;
    public String author;
    public LocalDate publishedDate;

    public BookResponseDTO(int id, String title, String author, LocalDate publishedDate){

        this.id=id;
        this.title=title;
        this.author=author;
        this.publishedDate=publishedDate;
    }
}
