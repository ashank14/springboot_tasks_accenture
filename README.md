# Spring Boot Book API

A simple RESTful API built with **Spring Boot** for managing a collection of books.  
Includes basic CRUD operations with in-memory storage using a list.

---

## Features

- `GET /hello` → Hello World check  
- `GET /books` → Get all books  
- `GET /books/{id}` → Get book by ID  
- `POST /books` → Add a new book (with ID conflict check)  
- `PUT /books/{id}` → Update an existing book  
- `DELETE /books/{id}` → Delete a book by ID  

---

## API Endpoints

### Hello World

GET /hello

Response:  
Hello World

---

### Get All Books

GET /books

Returns a list of all books.

---

### Get Book By ID

GET /books/{id}

- Returns book details if found (200 OK)  
- Returns 404 Not Found if not found

---

### Add New Book

POST /books  
Content-Type: application/json

Request Body example:

{
  "id": 3,
  "title": "New Book Title",
  "author": "Author Name",
  "publishedDate": "2024-06-05"
}

- Returns 201 Created if added  
- Returns 409 Conflict if ID already exists

---

### Update Book

PUT /books/{id}  
Content-Type: application/json

Request Body example:

{
  "title": "Updated Title",
  "author": "New Author",
  "publishedDate": "2025-01-01"
}

- Returns 200 OK if updated  
- Returns 404 Not Found if book not found

---

### Delete Book

DELETE /books/{id}

- Returns "Book removed" if deleted  
- Returns "Book not found" if invalid ID

---

## Tech Stack

- Java 17  
- Spring Boot 3.x  
- Maven  
- IntelliJ IDEA (optional)  
- Git / GitHub  

---

## How to Run

### Prerequisites

- Java 17 installed 
- Maven installed
- IntelliJ IDEA (optional, recommended)

---

### Steps

1. Clone the repository


2. Open in IntelliJ IDEA (Recommended):

- IntelliJ will auto-import Maven dependencies  

3. Run the application using IDE or terminal using command : mvn spring-boot:run


4. Test the API:

- Hello World:  
  `GET http://localhost:8080/hello`  

- Books APIs:  
  `GET http://localhost:8080/books`  

Use Postman, curl, browser, or IntelliJ HTTP client to test.

---

## Notes

- Uses in-memory storage (`ArrayList`), so data resets on server restart  
- No database integration yet  
- JSON serialization handled by Spring Boot’s Jackson support


