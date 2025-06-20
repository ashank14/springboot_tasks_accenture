package spring_tasks.spring_project.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import spring_tasks.spring_project.dto.*;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import spring_tasks.spring_project.models.Book;
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

    @GetMapping("/search")
    public List<GoogleApiResponseDTO> callApi(@RequestParam String title){
        return bookService.searchBooks(title);
    }

    @PostMapping("/addViaAPI")
    public Book addViaAPI(@Valid @RequestBody GoogleApiRequestDTO id){
        return bookService.addViaApi(id);
    }

}
