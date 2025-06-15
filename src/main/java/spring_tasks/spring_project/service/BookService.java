package spring_tasks.spring_project.service;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import spring_tasks.spring_project.dto.*;
import spring_tasks.spring_project.kafka.producer.KafkaProducerService;
import spring_tasks.spring_project.models.Book;
import spring_tasks.spring_project.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class BookService {

    private static final Logger logger=LoggerFactory.getLogger(BookService.class);

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private KafkaProducerService notificationProducer;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${google.api.base-url}")
    private String baseURL;

    @Value("${google.api.key}")
    private String googleApiKey;

    // Get all books
    public List<BookResponseDTO> getAllBooks() {
        logger.info("getting books");
        return bookRepository.findAll().stream().map(book -> new BookResponseDTO(book.getId(), book.getTitle(), book.getAuthor(), book.getPublishedDate())).collect(Collectors.toList());
    }

    // Get book by ID
    public Optional<BookResponseDTO> getBookById(int id) {
        logger.info("getting book with id:{}",id);
        return bookRepository.findById(id).map(book -> new BookResponseDTO(book.getId(), book.getTitle(), book.getAuthor(), book.getPublishedDate()));
    }

    // Add a new book
    public BookResponseDTO addBook(BookRequestDTO bookDTO) {
        logger.info("Adding new book");
        Book newBook = new Book();
        newBook.setTitle(bookDTO.title());
        newBook.setAuthor(bookDTO.author());
        newBook.setPublishedDate(bookDTO.publishedDate());

        Book savedBook = bookRepository.save(newBook);
        String notificationMessage = "New Book Added: " + savedBook.getTitle() + " by " + savedBook.getAuthor();
        try {
            notificationProducer.sendNotification(notificationMessage);
        } catch (Exception e) {
            logger.warn("Could not send Kafka notification: {}", e.getMessage());
        }

        return new BookResponseDTO(savedBook.getId(), savedBook.getTitle(), savedBook.getAuthor(), savedBook.getPublishedDate());
    }

    // Update a book
    public Optional<BookResponseDTO> updateBook(int id, BookRequestDTO updatedBook) {
        logger.info("Updating details of book with id:{}",id);
        return bookRepository.findById(id).map(book -> {
            book.setTitle(updatedBook.title());
            book.setAuthor(updatedBook.author());
            book.setPublishedDate(updatedBook.publishedDate());
            bookRepository.save(book);


            return new BookResponseDTO(
                    book.getId(), book.getTitle(), book.getAuthor(), book.getPublishedDate());
        });


    }

    // Delete a book
    public boolean deleteBook(int id) {
        logger.info("Deleting book with id:{}",id);
        if (!bookRepository.existsById(id)) {
            return false;
        }
        bookRepository.deleteById(id);
        return true;
    }

    @CircuitBreaker(name = "googleApiBreaker", fallbackMethod = "searchBooksFallback")
    @Retry(name = "googleApiRetry", fallbackMethod = "searchBooksFallback")
    public List<GoogleApiResponseDTO> searchBooks(String title) {
        logger.info("Fetching from API");

        String url = baseURL + "?q=intitle:" + title + "&key=" + googleApiKey;

        logger.info(url);

        ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
        JsonNode jsonNode = response.getBody();

        if (jsonNode == null || !jsonNode.has("items") || jsonNode.get("items").isEmpty()) {
            return List.of();
        }

        List<GoogleApiResponseDTO> books = new ArrayList<>();
        jsonNode.get("items").forEach(item -> {
            JsonNode volumeInfo = item.get("volumeInfo");
            logger.info(String.valueOf(volumeInfo));

            List<String> authors = volumeInfo.has("authors") ?
                    StreamSupport.stream(volumeInfo.get("authors").spliterator(), false)
                            .map(JsonNode::asText)
                            .collect(Collectors.toList()) :
                    List.of("Unknown");

            logger.info("date");
            DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                    .appendPattern("yyyy[-MM[-dd]]")
                    .parseDefaulting(ChronoField.MONTH_OF_YEAR, 1)
                    .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
                    .toFormatter();
            LocalDate publishedDate = LocalDate.parse(volumeInfo.get("publishedDate").asText(), formatter);

            logger.info("returning books");
            books.add(new GoogleApiResponseDTO(
                    item.get("id").asText(),
                    volumeInfo.get("title").asText(),
                    authors,
                    publishedDate
            ));
        });

        return books;
    }

    public List<GoogleApiResponseDTO> searchBooksFallback(String title, Throwable t) {
        GoogleApiResponseDTO placeholder = new GoogleApiResponseDTO(
                "N/A",
                "Google Books API is currently unavailable",
                List.of("N/A"),
                null
        );

        return List.of(placeholder);
    }

    @CircuitBreaker(name = "googleApiBreaker", fallbackMethod = "addViaApiFallback")
    @Retry(name = "googleApiRetry", fallbackMethod = "addViaApiFallback")
    public Book addViaApi(GoogleApiRequestDTO bookId) {

        String id=bookId.id();
        logger.info("Getting book via API with id: {}", id);

        String cleanId = id.replaceAll("^\"|\"$", "");
        String url = baseURL+"/"+ cleanId + "?key=" + googleApiKey;

        ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
        JsonNode jsonNode = response.getBody();

        if (jsonNode == null || !jsonNode.has("volumeInfo")) {
            throw new NoSuchElementException("No book found for id: " + cleanId);
        }

        JsonNode volumeInfo = jsonNode.get("volumeInfo");
        logger.info("VolumeInfo: {}", volumeInfo);

        List<String> authors = volumeInfo.has("authors") ?
                StreamSupport.stream(volumeInfo.get("authors").spliterator(), false)
                        .map(JsonNode::asText)
                        .collect(Collectors.toList()) :
                List.of("Unknown");

        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendPattern("yyyy[-MM[-dd]]")
                .parseDefaulting(ChronoField.MONTH_OF_YEAR, 1)
                .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
                .toFormatter();
        LocalDate publishedDate = LocalDate.parse(volumeInfo.get("publishedDate").asText(), formatter);

        GoogleApiResponseDTO googleResponse = new GoogleApiResponseDTO(
                jsonNode.get("id").asText(),
                volumeInfo.get("title").asText(),
                authors,
                publishedDate
        );

        Book book = new Book(
                googleResponse.title(),
                googleResponse.author().get(0),
                googleResponse.publishedDate()
        );

        String notificationMessage = "New Book Added: " + book.getTitle() + " by " + book.getAuthor();
        try {
            notificationProducer.sendNotification(notificationMessage);
        } catch (Exception e) {
            logger.info("Could not send Kafka notification: {}", e.getMessage());
        }
        return bookRepository.save(book);
    }

    public Book addViaApiFallback(GoogleApiRequestDTO id, Throwable t) {
        logger.error("Fallback triggered: {}", t.getMessage());
        Book placeholder = new Book(
                "Google Books API is currently unavailable",
                "no author",
                null
        );

        return placeholder;
    }


}
