package spring_tasks.spring_project.Models;

import org.springframework.cglib.core.Local;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int id;
    public String title;
    public String author;
    public LocalDate publishedDate;

    public Book(){}

    public Book( String title, String author, LocalDate publishedDate){
        this.title=title;
        this.author=author;
        this.publishedDate=publishedDate;
    }
}
