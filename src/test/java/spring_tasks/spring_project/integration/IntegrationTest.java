package spring_tasks.spring_project.integration;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.fasterxml.jackson.databind.ObjectMapper;
import spring_tasks.spring_project.dto.BookRequestDTO;
import spring_tasks.spring_project.dto.GoogleApiRequestDTO;
import spring_tasks.spring_project.dto.GoogleApiResponseDTO;
import spring_tasks.spring_project.models.Book;
import spring_tasks.spring_project.repository.BookRepository;

import java.time.LocalDate;
import java.util.List;

import  org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder.*;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;



@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc(addFilters = false)
@Testcontainers
@TestPropertySource(properties = {
        "google.api.key=mock-key",
        "google.api.base-url=http://localhost:8089/"
})
public class IntegrationTest {
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



    @Autowired
    private MockMvc mockMvc;


    @MockitoBean
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;


    @BeforeEach
    void cleanDb(){
        bookRepository.deleteAll();
    }

    @Test
    void shouldAddAndFetchBook() throws Exception {
        BookRequestDTO request = new BookRequestDTO(
                "Test Book",
                "Test Author",
                LocalDate.of(2024, 6, 15)
        );

        // Add book
        mockMvc.perform(MockMvcRequestBuilders.post("/books")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Test Book"));


        // Verify in database
        assertThat(bookRepository.findAll()).hasSize(1);

        // Fetch all books
        mockMvc.perform(MockMvcRequestBuilders.get("/books"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].title").value("Test Book"));
    }
    @Test
    void shouldDeleteBook() throws Exception {
        Book book = new Book("Title", "Author", LocalDate.of(2024, 1, 1));
        book = bookRepository.save(book);

        mockMvc.perform(delete("/books/" + book.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string("Book removed"));

        assertThat(bookRepository.existsById(book.getId())).isFalse();
    }

    @Test
    void shouldUpdateBook() throws Exception {
        Book book = new Book("Old Title", "Old Author", LocalDate.of(2024, 1, 1));
        book = bookRepository.save(book);

        BookRequestDTO updated = new BookRequestDTO(
                "Updated Title", "New Author", LocalDate.of(2024, 5, 25)
        );

        mockMvc.perform(MockMvcRequestBuilders.put("/books/"+book.getId())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Updated Title"));

        Book updatedBook = bookRepository.findById(book.getId()).orElseThrow();
        assertThat(updatedBook.getTitle()).isEqualTo("Updated Title");
    }

    @Test
    void shouldFetchBookById() throws Exception {
        Book book = new Book("Some Title", "Some Author", LocalDate.of(2024, 3, 1));
        book = bookRepository.save(book);

        mockMvc.perform((RequestBuilder) get("/books/" + book.getId()))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Some Title"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.author").value("Some Author"));
    }

    @Test
    void shouldSearchBooksViaApi() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode response = mapper.createObjectNode();
        ArrayNode items = mapper.createArrayNode();
        ObjectNode item = mapper.createObjectNode();
        item.put("id", "test-id-1");

        ObjectNode volumeInfo = mapper.createObjectNode();
        volumeInfo.put("title", "Test Book");
        volumeInfo.putArray("authors").add("Test Author");
        volumeInfo.put("publishedDate", "2024-06-15");
        item.set("volumeInfo", volumeInfo);
        items.add(item);
        response.set("items", items);

        when(restTemplate.getForEntity(anyString(), eq(JsonNode.class)))
                .thenReturn(new ResponseEntity<>(response, HttpStatus.OK));

        mockMvc.perform(get("/books/search")
                        .param("title", "Test Book"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("test-id-1"))
                .andExpect(jsonPath("$[0].title").value("Test Book"))
                .andExpect(jsonPath("$[0].author[0]").value("Test Author"));
    }

    @Test
    void shouldAddBookViaApi() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // Outer JSON object
        ObjectNode response = mapper.createObjectNode();
        response.put("id", "test-id-1");

        // Inner volumeInfo object
        ObjectNode volumeInfo = mapper.createObjectNode();
        volumeInfo.put("title", "Test Book");
        volumeInfo.putArray("authors").add("Test Author");
        volumeInfo.put("publishedDate", "2024-06-15");

        // Attach volumeInfo inside response
        response.set("volumeInfo", volumeInfo);

        // Mock RestTemplate API call
        when(restTemplate.getForEntity(anyString(), eq(JsonNode.class)))
                .thenReturn(new ResponseEntity<>(response, HttpStatus.OK));

        // Request DTO
        GoogleApiRequestDTO id = new GoogleApiRequestDTO("test-id-1");

        // Perform POST /addViaAPI call
        mockMvc.perform(MockMvcRequestBuilders.post("/books/addViaAPI")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(id)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Book"))
                .andExpect(jsonPath("$.author").value("Test Author"))
                .andExpect(jsonPath("$.publishedDate").value("2024-06-15"));

        // DB verification
        assertThat(bookRepository.findAll()).hasSize(1);
        Book savedBook = bookRepository.findAll().get(0);
        assertThat(savedBook.getTitle()).isEqualTo("Test Book");
    }

}
