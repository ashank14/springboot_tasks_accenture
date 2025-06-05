package spring_tasks.spring_project.Models;

import org.springframework.cglib.core.Local;

import java.time.LocalDate;

public class Book {

    public int id;
    public String title;
    public String author;
    public LocalDate publishedDate;

    public Book(){}

    public Book(int id, String title, String author, LocalDate publishedDate){
        this.id=id;
        this.title=title;
        this.author=author;
        this.publishedDate=publishedDate;
    }
}
