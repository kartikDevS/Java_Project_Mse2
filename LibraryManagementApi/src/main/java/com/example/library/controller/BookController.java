package com.example.library.controller;

import com.example.library.model.Book;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/books")
public class BookController {
    // This list is in memory, so its contents reset when the application stops.
    private final List<Book> books = new ArrayList<>();

    @PostMapping
    public ResponseEntity<?> addBook(@RequestBody Book book) {
        if (book.getId() == null) {
            return ResponseEntity.badRequest().body("Book ID is required.");
        }

        if (findBookIndex(book.getId()) != -1) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("A book with this ID already exists.");
        }

        books.add(book);
        return ResponseEntity.status(HttpStatus.CREATED).body(book);
    }

    @GetMapping
    public List<Book> getAllBooks() {
        return books;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBookById(@PathVariable Integer id) {
        int index = findBookIndex(id);
        if (index == -1) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Book not found with ID: " + id);
        }
        return ResponseEntity.ok(books.get(index));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBook(@PathVariable Integer id, @RequestBody Book updatedBook) {
        int index = findBookIndex(id);
        if (index == -1) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Book not found with ID: " + id);
        }

        // The ID in the URL identifies the book; all other details come from the request body.
        updatedBook.setId(id);
        books.set(index, updatedBook);
        return ResponseEntity.ok(updatedBook);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> partiallyUpdateBook(@PathVariable Integer id, @RequestBody Book bookChanges) {
        int index = findBookIndex(id);
        if (index == -1) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Book not found with ID: " + id);
        }

        Book existingBook = books.get(index);
        if (bookChanges.getTitle() != null) {
            existingBook.setTitle(bookChanges.getTitle());
        }
        if (bookChanges.getAuthor() != null) {
            existingBook.setAuthor(bookChanges.getAuthor());
        }
        if (bookChanges.getCategory() != null) {
            existingBook.setCategory(bookChanges.getCategory());
        }
        if (bookChanges.getAvailableCopies() != null) {
            existingBook.setAvailableCopies(bookChanges.getAvailableCopies());
        }

        return ResponseEntity.ok(existingBook);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBook(@PathVariable Integer id) {
        int index = findBookIndex(id);
        if (index == -1) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Book not found with ID: " + id);
        }

        books.remove(index);
        return ResponseEntity.ok("Book deleted successfully.");
    }

    private int findBookIndex(Integer id) {
        for (int i = 0; i < books.size(); i++) {
            if (books.get(i).getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }
}
