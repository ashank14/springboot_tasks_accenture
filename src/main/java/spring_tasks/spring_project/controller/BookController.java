package spring_tasks.spring_project.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import spring_tasks.spring_project.Models.Book;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

@RestController
@RequestMapping("/books")
public class BookController {

    //In-memory Database
    private List<Book> books=new ArrayList<>();

    public BookController(){
        this.books.add(new Book(1,"book1","author1",LocalDate.of(2025,6,5)));
        this.books.add(new Book(2,"book2","author2",LocalDate.of(2025,6,1)));
    }


    @GetMapping
    public List<Book> getAll(){
        return books;
    }

    @GetMapping("/{id}")
    public Book getBook(@PathVariable int id){
        for(Book b:books){
            if(b.id==id){
                return b;
            }
        }
        return null;
    }

    @DeleteMapping("delete/{id}")
    public String deleteBook(@PathVariable int id){
        boolean deleted=books.removeIf(book->book.id==id);
        return deleted?"Deleted":"id not found";

    }

    @PostMapping("/addBook")
    public ResponseEntity<?> addBook(@RequestBody Book book){
        boolean exists=books.stream().anyMatch(b->b.id==book.id);

        if(exists){
            return ResponseEntity.status(409).body("Book already exists");
        }

        books.add(book);

        return ResponseEntity.status(200).body(books);

    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBook(@PathVariable int id,@RequestBody Book newbook){

        for(Book book:books){
            if(book.id==id){
                book.title=newbook.title;
                book.author=newbook.author;
                book.publishedDate=newbook.publishedDate;

                return ResponseEntity.ok(book);
            }
        }
        return ResponseEntity.status(404).body("Book not found");
    }

}
