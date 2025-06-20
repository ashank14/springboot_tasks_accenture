package spring_tasks.spring_project.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;
import spring_tasks.spring_project.dto.BookRequestDTO;
import spring_tasks.spring_project.dto.BookResponseDTO;
import spring_tasks.spring_project.dto.GoogleApiRequestDTO;
import spring_tasks.spring_project.dto.GoogleApiResponseDTO;
import spring_tasks.spring_project.kafka.producer.KafkaProducerService;
import spring_tasks.spring_project.models.Book;
import spring_tasks.spring_project.repository.BookRepository;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(SpringExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;
    @Mock
    private MeterRegistry meterRegistry;
    @Mock
    private  Counter booksAddedCounter;
    @Mock
    private RestTemplate restTemplate;


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
        // Mock meter counter
        Counter mockCounter = mock(Counter.class);
        when(meterRegistry.counter("books.added.count")).thenReturn(mockCounter);

        BookResponseDTO result = bookService.addBook(requestDTO);

        assertEquals("New Title", result.title());
        assertEquals(1, result.id());
        verify(bookRepository, times(1)).save(any(Book.class));
        verify(mockCounter, times(1)).increment();

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
    @Test
    void testUpdateBookSuccess() {
        Book existingBook = new Book("Old Title", "Old Author", LocalDate.now());
        existingBook.setId(1);
        BookRequestDTO updatedBook = new BookRequestDTO("New Title", "New Author", LocalDate.now());
        when(bookRepository.findById(1)).thenReturn(Optional.of(existingBook));
        when(bookRepository.save(any(Book.class))).thenReturn(existingBook);
        Optional<BookResponseDTO> result = bookService.updateBook(1, updatedBook);
        assertTrue(result.isPresent());
        assertEquals("New Title", result.get().title());
        verify(bookRepository, times(1)).save(existingBook);
    }
    @Test
    void testSearchBooksFallback() {
        List<GoogleApiResponseDTO> result = bookService.searchBooksFallback("Any Title", new RuntimeException("API down"));
        assertEquals(1, result.size());
        assertEquals("Google Books API is currently unavailable", result.get(0).title());
    }

    @Test
    void testAddViaApiFallback() {
        Book fallbackBook = bookService.addViaApiFallback(new GoogleApiRequestDTO("id"), new RuntimeException("API down"));
        assertEquals("Google Books API is currently unavailable", fallbackBook.getTitle());
    }
    @Test
    void testUpdateBookNotFound() {
        when(bookRepository.findById(99)).thenReturn(Optional.empty());
        Optional<BookResponseDTO> result = bookService.updateBook(99, new BookRequestDTO("Title", "Author", LocalDate.now()));
        assertFalse(result.isPresent());
    }


    @Test
    void testSearchBooks() {
        // Create sample JSON response
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jsonNode = mapper.createObjectNode();
        ArrayNode itemsArray = mapper.createArrayNode();

        // Single book item
        ObjectNode itemNode = mapper.createObjectNode();
        itemNode.put("id", "test-id");

        ObjectNode volumeInfo = mapper.createObjectNode();
        volumeInfo.put("title", "Test Book");
        volumeInfo.put("publishedDate", "2023");
        ArrayNode authorsArray = mapper.createArrayNode();
        authorsArray.add("Author 1");
        volumeInfo.set("authors", authorsArray);

        itemNode.set("volumeInfo", volumeInfo);
        itemsArray.add(itemNode);

        jsonNode.set("items", itemsArray);

        ResponseEntity<JsonNode> responseEntity = new ResponseEntity<>(jsonNode, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(JsonNode.class))).thenReturn(responseEntity);

        List<GoogleApiResponseDTO> result = bookService.searchBooks("Test Book");

        // Assertions
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Book", result.get(0).title());
        assertEquals("Author 1", result.get(0).author().get(0));
        assertEquals("test-id", result.get(0).id());
    }


    @Test
    void testAddViaApiBookNotFound() {
        JsonNode jsonNode = mock(JsonNode.class);
        when(jsonNode.has("volumeInfo")).thenReturn(false);

        ResponseEntity<JsonNode> responseEntity = new ResponseEntity<>(jsonNode, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(JsonNode.class))).thenReturn(responseEntity);

        GoogleApiRequestDTO request = new GoogleApiRequestDTO("some-id");
        assertThrows(NoSuchElementException.class, () -> bookService.addViaApi(request));
    }
    @Test
    void testAddBookKafkaFailure() {
        BookRequestDTO requestDTO = new BookRequestDTO("Title", "Author", LocalDate.now());
        Book book = new Book("Title", "Author", LocalDate.now());
        book.setId(1);

        when(bookRepository.save(any(Book.class))).thenReturn(book);
        when(meterRegistry.counter("books.added.count")).thenReturn(mock(Counter.class));

        doThrow(new RuntimeException("Kafka down")).when(kafkaProducerService).sendNotification(anyString());

        BookResponseDTO result = bookService.addBook(requestDTO);
        assertEquals("Title", result.title());
        verify(kafkaProducerService, times(1)).sendNotification(anyString());
    }

    @Test
    void testSearchBooksEmptyResponse() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jsonNode = mapper.createObjectNode(); // no 'items'
        ResponseEntity<JsonNode> responseEntity = new ResponseEntity<>(jsonNode, HttpStatus.OK);

        when(restTemplate.getForEntity(anyString(), eq(JsonNode.class))).thenReturn(responseEntity);

        List<GoogleApiResponseDTO> result = bookService.searchBooks("Test");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    @Test
    void testAddViaApiMissingPublishedDate() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jsonNode = mapper.createObjectNode();
        jsonNode.put("id", "book-id");

        ObjectNode volumeInfo = mapper.createObjectNode();
        volumeInfo.put("title", "Title");
        ArrayNode authorsArray = mapper.createArrayNode();
        authorsArray.add("Author");
        volumeInfo.set("authors", authorsArray);

        jsonNode.set("volumeInfo", volumeInfo);

        ResponseEntity<JsonNode> responseEntity = new ResponseEntity<>(jsonNode, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(JsonNode.class))).thenReturn(responseEntity);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            bookService.addViaApi(new GoogleApiRequestDTO("book-id"));
        });

        // Optional: check message if you like
        assertTrue(ex.getMessage().contains("Text '' could not be parsed")
                || ex instanceof NullPointerException
                || ex instanceof DateTimeParseException);
    }





}