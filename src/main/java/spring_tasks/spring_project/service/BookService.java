package spring_tasks.spring_project.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import spring_tasks.spring_project.DTO.*;

import spring_tasks.spring_project.Models.Book;

import org.springframework.beans.factory.annotation.Autowired;
import spring_tasks.spring_project.repository.BookRepository;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookService {
    @Autowired
    private BookRepository bookRepository;

    //get books
    public List<BookResponseDTO> getAllBooks() {
        return bookRepository.findAll().stream().map(book->new BookResponseDTO(book.id,book.title,book.author,book.publishedDate)).collect(Collectors.toList());
    }

    //get by id
    public Optional<BookResponseDTO> getBookById(int id) {
        return bookRepository.findById(id).map(book->new BookResponseDTO(book.id,book.title,book.author,book.publishedDate));
    }

    //add a book
    public Optional<BookResponseDTO> addBook(BookRequestDTO book) {
        Book newbook=new Book();
        newbook.title=book.title;
        newbook.author=book.author;
        newbook.publishedDate=book.publishedDate;

        Book savedBook=bookRepository.save(newbook);

        return Optional.of(new BookResponseDTO(savedBook.id, savedBook.title, savedBook.author, savedBook.publishedDate));

    }

    //update book
    public Optional<BookResponseDTO> updateBook(int id,BookRequestDTO updatedBook) {
        return bookRepository.findById(id).map(book->{
            book.title=updatedBook.title;
            book.author=updatedBook.author;
            book.publishedDate=updatedBook.publishedDate;
            bookRepository.save(book);
            return new BookResponseDTO(book.id,book.title,book.author,book.publishedDate);
        });

    }

    //delete book
    public boolean deleteBook(int id){
        if(!bookRepository.existsById(id)){
            return false;
        }
        bookRepository.deleteById(id);
        return true;
    }

}
