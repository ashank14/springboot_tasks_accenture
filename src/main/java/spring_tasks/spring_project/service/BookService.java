package spring_tasks.spring_project.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import spring_tasks.spring_project.dto.*;
import spring_tasks.spring_project.models.Book;
import spring_tasks.spring_project.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
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

    private final WebClient webClient;
    @Autowired
    public BookService(WebClient webClient) {
        this.webClient = webClient;
    }


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



    public Mono<List<GoogleApiResponseDTO>> searchBooks(String title) {
        logger.info("fetching from api");
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/")
                        .queryParam("q", "intitle:" + title)
                        .queryParam("key", "AIzaSyD6_cE-b63l2ULS769mir0ySpknbihJwhI")
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)  // Read response as JSON
                .map(jsonNode -> {
                    if (!jsonNode.has("items") || jsonNode.get("items").isEmpty()) {
                        throw new NoSuchElementException("No books found for title: " + title);
                    }
                    logger.info("mapping");
                    List<GoogleApiResponseDTO> books = new ArrayList<>();
                    jsonNode.get("items").forEach(item -> {
                        JsonNode volumeInfo = item.get("volumeInfo");
                        logger.info(String.valueOf(volumeInfo));
                        books.add(new GoogleApiResponseDTO(
                                item.get("id").asText(),
                                volumeInfo.get("title").asText(),
                                volumeInfo.has("authors") ? StreamSupport.stream(
                                                volumeInfo.get("authors").spliterator(), false)
                                        .map(JsonNode::asText)
                                        .collect(Collectors.toList()) : List.of("Unknown"),
                                volumeInfo.has("publishedDate") ?LocalDate.parse(volumeInfo.get("publishedDate").asText()) : null
                        ));
                    });
                    return books;
                });
    }

    public Book addViaAPI(String id){
            // Clean up the ID by removing any extraneous double quotes.
            String cleanId = id.replaceAll("^\"|\"$", "");
            logger.info("Getting book via API with id: {}", cleanId);

            // 1. Call the external API to get the JSON response.
            Mono<JsonNode> jsonResponseMono = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/{id}")
                            .queryParam("key", "AIzaSyD6_cE-b63l2ULS769mir0ySpknbihJwhI")
                            .build(cleanId))
                    .retrieve()
                    .bodyToMono(JsonNode.class);

            // 2. Map the JSON response to GoogleApiResponseDTO.
            Mono<GoogleApiResponseDTO> googleApiResponseMono = jsonResponseMono.map(jsonNode -> {
                JsonNode volumeInfo = jsonNode.get("volumeInfo");
                String title = volumeInfo.get("title").asText();
                logger.info("VolumeInfo: {}", volumeInfo);

                List<String> authors = volumeInfo.has("authors")
                        ? StreamSupport.stream(volumeInfo.get("authors").spliterator(), false)
                        .map(JsonNode::asText)
                        .collect(Collectors.toList())
                        : List.of("Unknown");

                LocalDate publishedDate = volumeInfo.has("publishedDate")
                        ? LocalDate.parse(volumeInfo.get("publishedDate").asText())
                        : null;

                logger.info("Returning GoogleApiResponseDTO");
                return new GoogleApiResponseDTO(
                        jsonNode.get("id").asText(),
                        title,
                        authors,
                        publishedDate
                );
            });

            // 3. Map the GoogleApiResponseDTO to BookRequestDTO.
            Mono<BookRequestDTO> bookRequestDTOMono = googleApiResponseMono.map(googleResponse ->
                    new BookRequestDTO(
                            googleResponse.title(),
                            googleResponse.author().get(0),  // Using the first author for simplicity
                            googleResponse.publishedDate()
                    )
            );

            // 4. Finally, create a Book domain object from the BookRequestDTO.
            Mono<Book> bookMono = bookRequestDTOMono.map(bookRequestDTO ->
                    new Book(
                            bookRequestDTO.title(),
                            bookRequestDTO.author(),
                            bookRequestDTO.publishedDate()
                    )
            );
            //Add to database
            return bookRepository.save(bookMono.block());

    }



}
