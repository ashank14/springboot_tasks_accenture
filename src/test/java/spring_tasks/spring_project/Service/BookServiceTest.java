package spring_tasks.spring_project.Service;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import spring_tasks.spring_project.dto.BookRequestDTO;
import spring_tasks.spring_project.dto.BookResponseDTO;
import spring_tasks.spring_project.kafka.producer.KafkaProducerService;
import spring_tasks.spring_project.models.Book;
import spring_tasks.spring_project.repository.BookRepository;
import spring_tasks.spring_project.service.BookService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


 @TestPropertySource(properties = {
        "google.api.key=mock-key",
        "google.api.base-url=https://mock-api.com"
})
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private KafkaProducerService kafkaProducerService;
    @InjectMocks
    private BookService bookService;


    public BookServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllBooks() {
        List<Book> books = Arrays.asList(
                new Book("Title 1", "Author 1", LocalDate.now()),
                new Book("Title 2", "Author 2", LocalDate.now())
        );

        when(bookRepository.findAll()).thenReturn(books);

        List<BookResponseDTO> result = bookService.getAllBooks();

        assertEquals(2, result.size());
        verify(bookRepository, times(1)).findAll();
    }

    @Test
    void testGetBookById() {
        Book book = new Book("Title", "Author", LocalDate.now());
        book.setId(1);

        when(bookRepository.findById(1)).thenReturn(Optional.of(book));

        Optional<BookResponseDTO> result = bookService.getBookById(1);

        assertTrue(result.isPresent());
        assertEquals("Title", result.get().title());
    }

    @Test
    void testAddBook() {
        BookRequestDTO requestDTO = new BookRequestDTO("New Title", "New Author", LocalDate.now());
        Book book = new Book("New Title", "New Author", LocalDate.now());
        book.setId(1);

        when(bookRepository.save(any(Book.class))).thenReturn(book);

        BookResponseDTO result = bookService.addBook(requestDTO);

        assertEquals("New Title", result.title());
        assertEquals(1, result.id());
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void testDeleteBookSuccess() {
        when(bookRepository.existsById(1)).thenReturn(true);

        boolean result = bookService.deleteBook(1);

        assertTrue(result);
        verify(bookRepository, times(1)).deleteById(1);
    }

    @Test
    void testDeleteBookNotFound() {
        when(bookRepository.existsById(1)).thenReturn(false);

        boolean result = bookService.deleteBook(1);

        assertFalse(result);
        verify(bookRepository, never()).deleteById(1);
    }
}