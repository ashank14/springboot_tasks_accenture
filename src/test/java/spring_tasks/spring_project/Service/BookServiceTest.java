package spring_tasks.spring_project.Service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import spring_tasks.spring_project.DTO.BookRequestDTO;
import spring_tasks.spring_project.DTO.BookResponseDTO;
import spring_tasks.spring_project.Models.Book;
import spring_tasks.spring_project.service.BookService;

import javax.swing.text.html.Option;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BookServiceTest {

    @Autowired
    private BookService bookService;

    @Test
    void testCreateAndFindBook() {
        BookRequestDTO book = new BookRequestDTO("JUnit Book", "Ashank", LocalDate.now());
        BookResponseDTO saved = bookService.addBook(book);

        assertEquals("JUnit Book", saved.title);

        Optional<BookResponseDTO> fetched = bookService.getBookById(saved.id);
        assertTrue(fetched.isPresent());
        BookResponseDTO b=fetched.get();
        assertEquals("Ashank",b.author);
    }

}