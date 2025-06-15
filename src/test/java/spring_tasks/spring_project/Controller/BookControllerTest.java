package spring_tasks.spring_project.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;

import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import spring_tasks.spring_project.dto.BookRequestDTO;
import spring_tasks.spring_project.dto.BookResponseDTO;
import spring_tasks.spring_project.dto.GoogleApiRequestDTO;
import spring_tasks.spring_project.dto.GoogleApiResponseDTO;
import spring_tasks.spring_project.models.Book;
import spring_tasks.spring_project.service.BookService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "google.api.key=mock-key",
        "google.api.base-url=https://mock-api.com"
})
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

    @Test
    void testSearchBooks() throws Exception {
        List<GoogleApiResponseDTO> searchResults = List.of(
                new GoogleApiResponseDTO("id1", "API Book", List.of("Author"), LocalDate.now())
        );

        when(bookService.searchBooks("API Book")).thenReturn(searchResults);

        mockMvc.perform(get("/books/search")
                        .param("title", "API Book"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("API Book"))
                .andExpect(jsonPath("$[0].id").value("id1"));
    }

    @Test
    void testSearchBooksFallback() throws Exception {
        // Simulate fallback by returning the fallback response directly
        List<GoogleApiResponseDTO> fallbackResult = List.of(
                new GoogleApiResponseDTO("N/A", "Google Books API is currently unavailable", List.of("N/A"), null)
        );

        when(bookService.searchBooks("Unavailable")).thenReturn(fallbackResult);

        mockMvc.perform(get("/books/search")
                        .param("title", "Unavailable"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Google Books API is currently unavailable"));
    }

    @Test
    void testAddViaApi() throws Exception {
        Book mockBook = new Book("API Book", "Author", LocalDate.now());
        mockBook.setId(1);
        GoogleApiRequestDTO id=new GoogleApiRequestDTO("id1");
        String jsonRequest = objectMapper.writeValueAsString(id);

        when(bookService.addViaApi(id)).thenReturn(mockBook);

        mockMvc.perform(post("/books/addViaAPI")
                        .contentType("application/json")
                        .content(jsonRequest))  // since your controller expects a JSON string body
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("API Book"))
                .andExpect(jsonPath("$.author").value("Author"));
    }

    @Test
    void testAddViaApiFallback() throws Exception {
        Book fallbackBook = new Book("Google Books API is currently unavailable", "no author", null);
        GoogleApiRequestDTO id=new GoogleApiRequestDTO("id1");
        String jsonRequest = objectMapper.writeValueAsString(id);
        when(bookService.addViaApi(id)).thenReturn(fallbackBook);

        mockMvc.perform(post("/books/addViaAPI")
                        .contentType("application/json")
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Google Books API is currently unavailable"))
                .andExpect(jsonPath("$.author").value("no author"));
    }



    @TestConfiguration
    static class TestConfig {
        @Bean
        BookService bookService() {
            return mock(BookService.class);
        }
    }
}