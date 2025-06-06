package spring_tasks.spring_project.service;

import org.springframework.stereotype.Service;
import spring_tasks.spring_project.DTO.*;
import spring_tasks.spring_project.Models.Book;
import spring_tasks.spring_project.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    // Get all books
    public List<BookResponseDTO> getAllBooks() {
        return bookRepository.findAll().stream().map(book -> new BookResponseDTO(book.getId(), book.getTitle(), book.getAuthor(), book.getPublishedDate())).collect(Collectors.toList());
    }

    // Get book by ID
    public Optional<BookResponseDTO> getBookById(int id) {
        return bookRepository.findById(id).map(book -> new BookResponseDTO(book.getId(), book.getTitle(), book.getAuthor(), book.getPublishedDate()));
    }

    // Add a new book
    public BookResponseDTO addBook(BookRequestDTO bookDTO) {
        Book newBook = new Book();
        newBook.setTitle(bookDTO.getTitle());
        newBook.setAuthor(bookDTO.getAuthor());
        newBook.setPublishedDate(bookDTO.getPublishedDate());

        Book savedBook = bookRepository.save(newBook);
        return new BookResponseDTO(savedBook.getId(), savedBook.getTitle(), savedBook.getAuthor(), savedBook.getPublishedDate());
    }

    // Update a book
    public Optional<BookResponseDTO> updateBook(int id, BookRequestDTO updatedBook) {
        return bookRepository.findById(id).map(book -> {
            book.setTitle(updatedBook.getTitle());
            book.setAuthor(updatedBook.getAuthor());
            book.setPublishedDate(updatedBook.getPublishedDate());
            bookRepository.save(book);
            return new BookResponseDTO(
                    book.getId(), book.getTitle(), book.getAuthor(), book.getPublishedDate());
        });
    }

    // Delete a book
    public boolean deleteBook(int id) {
        if (!bookRepository.existsById(id)) {
            return false;
        }
        bookRepository.deleteById(id);
        return true;
    }
}
