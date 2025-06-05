package spring_tasks.spring_project.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import spring_tasks.spring_project.Models.Book;

import org.springframework.beans.factory.annotation.Autowired;
import spring_tasks.spring_project.repository.BookRepository;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequestMapping("/books")
public class BookController {

    @Autowired
    private BookRepository bookRepository;

    // GET all books
    @GetMapping
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    // GET a book by ID
    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable int id) {
        Optional<Book> book = bookRepository.findById(id);
        return book.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST a new book
    @PostMapping
    public ResponseEntity<Book> addBook(@RequestBody Book book) {
        if (book.id>0 && bookRepository.existsById(book.id)) {
            return ResponseEntity.status(409).build();
        }
        Book savedBook = bookRepository.save(book);
        return ResponseEntity.status(201).body(savedBook);
    }

    // PUT update a book
    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable int id,@RequestBody Book updatedBook) {
        Optional<Book> bookOptional = bookRepository.findById(id);
        if (bookOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Book book=bookOptional.get();
        book.title=updatedBook.title;
        book.author=updatedBook.author;
        book.publishedDate=updatedBook.publishedDate;

        bookRepository.save(book);
        return ResponseEntity.ok(book);
    }

    // DELETE a book by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBook(@PathVariable int id) {
        if (!bookRepository.existsById(id)) {
            return ResponseEntity.status(404).body("Book not found");
        }
        bookRepository.deleteById(id);
        return ResponseEntity.ok("Book removed");
    }

    //Initial Code without H2 database(using an arraylist as an in memory database)
    /*In-memory Database
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

    @DeleteMapping("/{id}")
    public String deleteBook(@PathVariable int id){
        boolean deleted=books.removeIf(book->book.id==id);
        return deleted?"Deleted":"id not found";

    }

    @PostMapping
    public ResponseEntity<?> addBook(@RequestBody Book book){
        boolean exists=books.stream().anyMatch(b->b.id==book.id);

        if(exists){
            return ResponseEntity.status(409).body("Book already exists");
        }

        books.add(book);

        return ResponseEntity.status(201).body(books);

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
    }*/


}
