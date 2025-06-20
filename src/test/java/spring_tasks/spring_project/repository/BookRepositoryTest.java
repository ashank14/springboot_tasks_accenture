package spring_tasks.spring_project.repository;


import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import spring_tasks.spring_project.models.Book;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
class BookRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("testdb")
            .withUsername("postgres")
            .withPassword("password");

    @Autowired
    private BookRepository bookRepository;

    @DynamicPropertySource
    static void overrideDatasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");

    }

    @BeforeAll
    static void checkContainerRunning() {
        assertTrue(postgresContainer.isRunning());
    }

    @Test
    void testSaveAndFindBooks() {
        Book book1 = new Book("book1", "Ashank Sethi", LocalDate.of(2025, 6, 17));
        Book book2 = new Book("book2", "Ashank Sethi", LocalDate.of(2025, 6, 18));

        bookRepository.save(book1);
        bookRepository.save(book2);

        List<Book> books = bookRepository.findAll();

        assertEquals(2, books.size());
        assertEquals("book1", books.get(0).getTitle());
        assertEquals("book2", books.get(1).getTitle());
    }


    @Test
    void testGetByIdAndDeleteBook() {
        Book book = new Book("Test Book", "Author", LocalDate.now());
        Book savedBook = bookRepository.save(book);

        assertTrue(bookRepository.existsById(savedBook.getId()));

        bookRepository.deleteById(savedBook.getId());

        assertFalse(bookRepository.existsById(savedBook.getId()));
    }

    @Test
    void testUpdateBook() {
        Book book = new Book("Old Title", "Old Author", LocalDate.of(2020, 1, 1));
        Book savedBook = bookRepository.save(book);

        // Update the book
        savedBook.setTitle("Updated Title");
        savedBook.setAuthor("Updated Author");
        savedBook.setPublishedDate(LocalDate.of(2024, 6, 17));
        Book updatedBook = bookRepository.save(savedBook);

        // Fetch again and verify
        Optional<Book> fetchedBook = bookRepository.findById(updatedBook.getId());
        assertTrue(fetchedBook.isPresent());

        Book result = fetchedBook.get();
        assertEquals("Updated Title", result.getTitle());
        assertEquals("Updated Author", result.getAuthor());
        assertEquals(LocalDate.of(2024, 6, 17), result.getPublishedDate());
    }

}
