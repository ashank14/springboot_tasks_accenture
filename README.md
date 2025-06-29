# Spring Boot Book API
---
![CI](https://github.com/ashank14/springboot_tasks_accenture/actions/workflows/ci.yml/badge.svg)
---
A simple RESTful API built with **Spring Boot** for managing a collection of books.  
Includes CRUD operations using an embedded **H2 Database**, **DTOs**, **Service layer**, **global exception handling**, and **input validation**.
Deployed with base URL: https://accenture-tasks-174cd6eff134.herokuapp.com/. 
Access API docs at: https://accenture-tasks-174cd6eff134.herokuapp.com/swagger-ui.html

---


## Features

- `GET /hello` → Hello World check
- `GET /books` → Get all books
- `GET /books/{id}` → Get book by ID
- `POST /books` → Add a new book (with validation)
- `PUT /books/{id}` → Update an existing book
- `DELETE /books/{id}` → Delete a book by ID
- `GET /books/search?title={title}` → Fetches books from Google Books API
- `POST /books/addViaApi` → Adds a book from Google Books API into H2 database
- Uses **Data Transfer Objects (DTOs)** for clean API responses
- Business logic separated into a **BookService** class
- Centralized **global exception handling** via `@ControllerAdvice`
- **Validation annotations** (`@NotNull`, `@Size`, etc.) on request DTOs
- **Custom error responses** for validation and runtime errors

---

## API Endpoints

### 🔹Hello World

**GET /hello**  
Response:  
`Hello World`

---

### 🔹 Get All Books

**GET /books**  
Returns a list of all books (as `BookResponseDTO`).

---

### 🔹 Get Book By ID

**GET /books/{id}`**
- Returns book details if found (200 OK)
- Returns 404 Not Found with an error DTO if not found

---

### 🔹 Add New Book

**POST /books**  

**Request Body Example:**

```json
{
  "title": "New Book Title",
  "author": "Author Name",
  "publishedDate": "2024-06-05"
}
```

- Returns 201 Created if added
- Returns 400 Bad Request if validation fails (missing/invalid fields)

---

### 🔹 Update Book

**PUT /books/{id}**  

**Request Body Example:**

```json
{
  "title": "Updated Title",
  "author": "New Author",
  "publishedDate": "2025-01-01"
}
```

- Returns 200 OK if updated
- Returns 404 Not Found if book does not exist
- Returns 400 Bad Request if validation fails

---

### 🔹 Delete Book

**DELETE /books/{id}**
- Returns "Book removed" if deleted
- Returns 404 Not Found if invalid ID

---

### 🔹  Search Google Books by Title

**GET /books/search?title={title}**
-Fetches a list of books matching the title from the Google Books API.

**Example:**
GET /books/search?title=The Hobbit
```json
[
{
"title": "The Hobbit",
"author": "J.R.R. Tolkien",
"publishedDate": "1937-09-21"
}
]

```
### 🔹 Add a Book via Google Books API

**POST** `/books/addViaApi`

---

#### 📤 Request Body Example:

```json
{
  "id": "mxDLY0qL2mAC"
}
```

#### 📥 Response Example:

```json
{
  "id": 5,
  "title": "Pride and Prejudice",
  "author": "Jane Austen",
  "publishedDate": "1937-09-21"
}
```

##  Tech Stack

- Java 17
- Spring Boot 3.x
- Maven
- H2 Database (in-memory, with console access)
- IntelliJ IDEA (optional)
- Git / GitHub

---

##  How to Run

### Prerequisites

- Java 17 installed
- Maven installed
- IntelliJ IDEA (optional, recommended)

---

### Steps

1. Clone the repository


2. Open in IntelliJ IDEA (Recommended)
- IntelliJ will auto-import Maven dependencies

3. Run the application:


mvn spring-boot:run or directly in Intellij


4. Test the API:

- Hello World:  
  `GET http://localhost:8080/hello`

- Books APIs:  
  `GET http://localhost:8080/books`

Use **Postman** for testing.

5. Access the H2 console:  
   `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:dcbapp`

---

##  Testing

**Controller Tests**
- Use MockMvc to simulate HTTP requests to controller endpoints
- Mock service layer using Mockito
- Verify status codes, response JSON, and interactions

**Service Tests**
- Use Mockito to mock the repository
- Test service logic independently of database
- Validate transformation between entities and DTOs

## Run Tests with:
   `mvn test`

##  Notes

- Uses **H2 Database** for data storage (data resets on server restart)
- JSON serialization handled via **Jackson**
- Global error handling ensures consistent API error responses
- DTOs keep request and response objects clean
- Validation errors returned in API-friendly format

---

##  Improvements Completed

-  Created **Data Transfer Objects (DTOs)**  
-  Moved business logic to a **BookService class**  
-  Added **global exception handling** using `@ControllerAdvice`  
-  Implemented **validation annotations** (`@NotNull`, `@Size`) with `@Valid`  
-  Returned **meaningful error messages and proper HTTP status codes**

---

## API Error Responses Example

**404 Not Found**

```json
{
  "message": "Book with {id} not found",
  "status": 404
}
```

**400 Validation Error**

```json
{
  "message": "title: title is required",
  "status": 400
}
```

---

Project is now structured, validated, and API-safe!