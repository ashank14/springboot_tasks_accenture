package spring_tasks.spring_project.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import spring_tasks.spring_project.dto.*;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import spring_tasks.spring_project.service.BookService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RestController
@RequestMapping("/books")
public class BookController {

    private static final Logger logger=LoggerFactory.getLogger(BookController.class);

    @Autowired
    private BookService bookService;

    // GET all books
    @GetMapping
    public List<BookResponseDTO> getAllBooks() {
        logger.info("GET /books");
        return bookService.getAllBooks();
    }


    //GET a book by ID
    @GetMapping("/{id}")
    public ResponseEntity<BookResponseDTO> getBookById(@PathVariable int id) {
        logger.info("GET /books/{id}");
        Optional<BookResponseDTO> book=bookService.getBookById(id);
        return book.map(ResponseEntity::ok).orElseThrow(()->new NoSuchElementException("Book with ID "+ id+" not found"));
    }

    //POST a new book
    @PostMapping
    public ResponseEntity<BookResponseDTO> addBook(@Valid @RequestBody BookRequestDTO book) {
        logger.info("POST /books");
        BookResponseDTO savedBook=bookService.addBook(book);
        return ResponseEntity.status(201).body(savedBook);
    }

    // PUT update a book
    @PutMapping("/{id}")
    public ResponseEntity<BookResponseDTO> updateBook(@PathVariable int id,@Valid @RequestBody BookRequestDTO updatedBook) {
        logger.info("PUT /books/{id}");
        Optional<BookResponseDTO>book=bookService.updateBook(id,updatedBook);
        return book.map(ResponseEntity::ok).orElseThrow(()->new NoSuchElementException("Book with ID "+ id+" not found"));
    }

    // DELETE a book by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBook(@PathVariable int id) {
        logger.info("DELETE /books/{id}");
        boolean deleted= bookService.deleteBook(id);
        if(deleted){
            return ResponseEntity.ok("Book removed");
        }
        throw new NoSuchElementException("Book with ID "+ id+" not found");
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
