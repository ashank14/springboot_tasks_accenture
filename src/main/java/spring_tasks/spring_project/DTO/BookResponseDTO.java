package spring_tasks.spring_project.DTO;

import java.time.LocalDate;

public class BookResponseDTO {

    private int id;
    private String title;
    private String author;
    private LocalDate publishedDate;

    public BookResponseDTO() {}

    public BookResponseDTO(int id, String title, String author, LocalDate publishedDate){
        this.id = id;
        this.title = title;
        this.author = author;
        this.publishedDate = publishedDate;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
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
