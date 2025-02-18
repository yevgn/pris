package ru.antonov.securitytest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.antonov.securitytest.dto.*;
import ru.antonov.securitytest.service.LibService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin
@Tag(
        name = "Контроллер для администрирования читального зала"
)
public class AdminController {
    private final LibService libService;

    @Operation(
            summary = "Поиск книг по заданным фильтрам"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Не найдено",
                    content = @Content(mediaType = "application/json", examples = { @ExampleObject(
                            value = "{\"message\": \"Данной страницы не существует\", \"debugMessage\":\"" +
                                    "Данной страницы не существует\"}") })),
            @ApiResponse(responseCode = "200", description = "ОК"),
            @ApiResponse(responseCode = "403", description = "FORBIDDEN",
                    content = @Content(mediaType = "application/json", examples = { @ExampleObject(
                            value = "{\"message\": \"Access restricted\", \"debugMessage\":\"" +
                                    "/api/v1/lib/books\"}") }))
    })
    @GetMapping("/books/filter")
    public ResponseEntity<List<BookResponse>> filterBooks(@RequestBody BookGetRequest filter){
        return ResponseEntity.ok(libService.filterBooks(filter));
    }

    @Operation(
            summary = "Получение списка всех книг"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Не найдено",
                    content = @Content(mediaType = "application/json", examples = { @ExampleObject(
                            value = "{\"message\": \"Данной страницы не существует\", \"debugMessage\":\"" +
                                    "Данной страницы не существует\"}") })),
            @ApiResponse(responseCode = "200", description = "ОК"),
            @ApiResponse(responseCode = "403", description = "FORBIDDEN",
                    content = @Content(mediaType = "application/json", examples = { @ExampleObject(
                            value = "{\"message\": \"Access restricted\", \"debugMessage\":\"" +
                                    "/api/v1/lib/books\"}") }))
    })
    @GetMapping("/books")
    public ResponseEntity<List<BookResponse>> findAllBooks(){
        return ResponseEntity.ok(libService.findAllBooks());
    }

    @Operation(
            summary = "Добавление книг из .csv файла"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Не найдено",
                    content = @Content(mediaType = "application/json", examples = { @ExampleObject(
                            value = "{\"message\": \"Данной страницы не существует\", \"debugMessage\":\"" +
                                    "Данной страницы не существует\"}") })),
            @ApiResponse(responseCode = "200", description = "ОК"),
            @ApiResponse(responseCode = "403", description = "FORBIDDEN",
                    content = @Content(mediaType = "application/json", examples = { @ExampleObject(
                            value = "{\"message\": \"Access restricted\", \"debugMessage\":\"" +
                                    "/api/v1/lib/books\"}") }))
    })
    @PostMapping("/books/csv")
    public ResponseEntity<List<BookResponse>> uploadBooksCsv(@RequestParam("file") MultipartFile file ){
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        try {
            return ResponseEntity.ok(libService.uploadBooksCsv(file));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
            summary = "Добавление книг в .json формате"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Не найдено",
                    content = @Content(mediaType = "application/json", examples = { @ExampleObject(
                            value = "{\"message\": \"Данной страницы не существует\", \"debugMessage\":\"" +
                                    "Данной страницы не существует\"}") })),
            @ApiResponse(responseCode = "200", description = "ОК"),
            @ApiResponse(responseCode = "403", description = "FORBIDDEN",
                    content = @Content(mediaType = "application/json", examples = { @ExampleObject(
                            value = "{\"message\": \"Access restricted\", \"debugMessage\":\"" +
                                    "/api/v1/lib/books\"}") }))
    })
    @PostMapping("/books/json")
    public ResponseEntity<List<BookResponse>> uploadBooksJSON(@RequestBody List<BookPostRequest> list){
        return ResponseEntity.ok(libService.uploadBooksJSON(list));
    }

    @Operation(
            summary = "Удаление книги из БД по названию"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Не найдено",
                    content = @Content(mediaType = "application/json", examples = { @ExampleObject(
                            value = "{\"message\": \"Данной страницы не существует\", \"debugMessage\":\"" +
                                    "Данной страницы не существует\"}") })),
            @ApiResponse(responseCode = "200", description = "ОК"),
            @ApiResponse(responseCode = "403", description = "FORBIDDEN",
                    content = @Content(mediaType = "application/json", examples = { @ExampleObject(
                            value = "{\"message\": \"Access restricted\", \"debugMessage\":\"" +
                                    "/api/v1/lib/books\"}") }))
    })
    @DeleteMapping("/books")
    public ResponseEntity<String> deleteBook(@RequestParam("name") String name){
        return libService.deleteBook(name)
                ? ResponseEntity.ok("Success") : ResponseEntity.badRequest().body("Invalid name");
    }

    @Operation(
            summary = "Получение списка всех пользователей"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Не найдено",
                    content = @Content(mediaType = "application/json", examples = { @ExampleObject(
                            value = "{\"message\": \"Данной страницы не существует\", \"debugMessage\":\"" +
                                    "Данной страницы не существует\"}") })),
            @ApiResponse(responseCode = "200", description = "ОК"),
            @ApiResponse(responseCode = "403", description = "FORBIDDEN",
                    content = @Content(mediaType = "application/json", examples = { @ExampleObject(
                            value = "{\"message\": \"Access restricted\", \"debugMessage\":\"" +
                                    "/api/v1/lib/books\"}") }))
    })
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> findAllUsers(){
        return ResponseEntity.ok(libService.findAllUsers());
    }

    @Operation(
            summary = "Поиск пользователя по email"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Не найдено",
                    content = @Content(mediaType = "application/json", examples = { @ExampleObject(
                            value = "{\"message\": \"Данной страницы не существует\", \"debugMessage\":\"" +
                                    "Данной страницы не существует\"}") })),
            @ApiResponse(responseCode = "200", description = "ОК"),
            @ApiResponse(responseCode = "403", description = "FORBIDDEN",
                    content = @Content(mediaType = "application/json", examples = { @ExampleObject(
                            value = "{\"message\": \"Access restricted\", \"debugMessage\":\"" +
                                    "/api/v1/lib/books\"}") }))
    })
    @GetMapping("/users/{email}")
    public ResponseEntity<UserResponse> findUser(@PathVariable("email") String email){
        return ResponseEntity.ok(libService.findUserByEmail(email));
    }

    @Operation(
            summary = "Получение списка сеансов с фильтрацией"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Не найдено",
                    content = @Content(mediaType = "application/json", examples = { @ExampleObject(
                            value = "{\"message\": \"Данной страницы не существует\", \"debugMessage\":\"" +
                                    "Данной страницы не существует\"}") })),
            @ApiResponse(responseCode = "200", description = "ОК"),
            @ApiResponse(responseCode = "403", description = "FORBIDDEN",
                    content = @Content(mediaType = "application/json", examples = { @ExampleObject(
                            value = "{\"message\": \"Access restricted\", \"debugMessage\":\"" +
                                    "/api/v1/lib/books\"}") }))
    })
    @GetMapping("/sessions")
    public ResponseEntity<List<SessionResponse>> filterSessions(@RequestBody SessionFilter filter){
            return ResponseEntity.ok(libService.filterSessions(filter));
    }
}

