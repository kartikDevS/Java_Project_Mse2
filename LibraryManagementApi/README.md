# Library Management REST API

A basic Spring Boot REST API for an OOP lab. It stores books in an `ArrayList`; it has no database. The list resets whenever the application stops.

## Run in VS Code

1. Install JDK 17 or later and the VS Code **Extension Pack for Java**.
2. Open the `LibraryManagementApi` folder in VS Code.
3. Open the VS Code terminal in this folder and run:

   ```powershell
   & 'D:\coding\java\springboot\demo\mvnw.cmd' -f 'D:\Btech\Java_exam\LibraryManagementApi\pom.xml' spring-boot:run
   ```

   This uses the Maven wrapper available in your existing `demo` folder. The first run may download Spring Boot dependencies and needs internet access. Alternatively, if Maven is installed, run `mvn spring-boot:run`.
4. Wait until Spring Boot reports that the application started. It listens at `http://localhost:8080`.
5. Keep this terminal running while sending requests from Postman.

## Postman requests

Use `http://localhost:8080/books` as the base URL. For POST and PUT, select **Body → raw → JSON** and set `Content-Type: application/json`.

### Add a book

`POST http://localhost:8080/books` — expected status: **201 Created**

```json
{
  "id": 1,
  "title": "The Java Handbook",
  "author": "Patrick Naughton",
  "category": "Programming",
  "availableCopies": 4
}
```

The client supplies the ID. A missing ID returns **400 Bad Request**; an ID already in the list returns **409 Conflict**.

### Get all books

`GET http://localhost:8080/books` — expected status: **200 OK**. Returns `[]` if no books have been added since startup.

### Get one book

`GET http://localhost:8080/books/1` — expected status: **200 OK** if found, otherwise **404 Not Found** with a message.

### Update a book

`PUT http://localhost:8080/books/1` — expected status: **200 OK** if found, otherwise **404 Not Found**.

```json
{
  "title": "The Java Handbook, Second Edition",
  "author": "Patrick Naughton",
  "category": "Programming",
  "availableCopies": 6
}
```

The ID in the URL identifies the book. The request body supplies the updated title, author, category, and available copies.

### Partially update a book

`PATCH http://localhost:8080/books/1` — expected status: **200 OK** if found, otherwise **404 Not Found**. Include only the fields you want to change; omitted fields keep their existing values.

```json
{
  "availableCopies": 8
}
```

This changes only the available copies for book 1.

### Delete a book

`DELETE http://localhost:8080/books/1` — expected status: **200 OK** with a success message, or **404 Not Found** if absent.

## Annotations to explain in a viva

- `@SpringBootApplication`: marks the application entry class and enables Spring Boot setup and component scanning.
- `@RestController`: makes the class a REST controller; returned objects are written as HTTP response bodies, usually JSON.
- `@RequestMapping("/books")`: sets the common URL prefix for the controller's endpoints.
- `@PostMapping`, `@GetMapping`, `@PutMapping`, `@PatchMapping`, `@DeleteMapping`: map HTTP methods and URL paths to Java methods. `PUT` replaces the book details; `PATCH` changes only the fields provided.
- `@RequestBody`: converts incoming JSON into a `Book` object.
- `@PathVariable`: reads a value such as `1` from `/books/1` into a method parameter.

`ResponseEntity` is used where the method needs to return a specific HTTP status and response body. `ArrayList` is the temporary in-memory storage.
