package spring_tasks.spring_project.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;
import spring_tasks.spring_project.DTO.BookRequestDTO;
import spring_tasks.spring_project.DTO.BookResponseDTO;
import spring_tasks.spring_project.service.BookService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class BookControllerTest {

    private final MockMvc mockMvc;
    private final BookService bookService;
    private final ObjectMapper objectMapper;

    BookControllerTest(MockMvc mockMvc, BookService bookService, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.bookService = bookService;
        this.objectMapper = objectMapper;
    }

    @Test
    void testGetAllBooks() throws Exception {
        List<BookResponseDTO> books = List.of(
                new BookResponseDTO(1, "Book 1", "Author", LocalDate.now())
        );
        when(bookService.getAllBooks()).thenReturn(books);

        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Book 1"));
    }

    @Test
    void testGetBookById() throws Exception {
        BookResponseDTO book = new BookResponseDTO(1, "Book 1", "Author", LocalDate.now());
        when(bookService.getBookById(1)).thenReturn(Optional.of(book));

        mockMvc.perform(get("/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Book 1"));
    }

    @Test
    void testAddBook() throws Exception {
        BookRequestDTO bookRequest = new BookRequestDTO("New Book", "Author", LocalDate.now());
        BookResponseDTO bookResponse = new BookResponseDTO(1, "New Book", "Author", LocalDate.now());

        when(bookService.addBook(any(BookRequestDTO.class))).thenReturn(bookResponse);

        mockMvc.perform(post("/books")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Book"));
    }

    @Test
    void testUpdateBook() throws Exception {
        BookRequestDTO updateRequest = new BookRequestDTO("Updated Book", "Author", LocalDate.now());
        BookResponseDTO updatedResponse = new BookResponseDTO(1, "Updated Book", "Author", LocalDate.now());

        when(bookService.updateBook(eq(1), any(BookRequestDTO.class))).thenReturn(Optional.of(updatedResponse));

        mockMvc.perform(put("/books/1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Book"));
    }

    @Test
    void testDeleteBook() throws Exception {
        when(bookService.deleteBook(1)).thenReturn(true);

        mockMvc.perform(delete("/books/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Book removed"));
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        BookService bookService() {
            return mock(BookService.class);
        }
    }
}