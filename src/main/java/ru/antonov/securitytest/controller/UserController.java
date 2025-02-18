package ru.antonov.securitytest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.antonov.securitytest.dto.*;
import ru.antonov.securitytest.entity.Type;
import ru.antonov.securitytest.exception.SessionOverlappingEx;
import ru.antonov.securitytest.service.LibService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/lib")
@Slf4j
@CrossOrigin
@Tag(
        name = "Контроллер для записей на сеанс"
)
public class UserController {
    private final LibService libService;

    @Operation(
            summary = "Фильтрация книг"
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
            summary = "Получение целого списка книг"
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
            summary = "Получение изображения книги"
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
    @GetMapping("/book_images/{bookId}")
    public ResponseEntity<byte[]> getBookImage(@PathVariable Integer bookId) {
        try {
            byte[] imageBytes = libService.getBookImage(bookId);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);
            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (IOException e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(
            summary = "Добавление нового сеанса в БД"
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
    @PostMapping("/sessions")
    public ResponseEntity<Object> createSession(@RequestBody SessionRequest request){
        try{
            return ResponseEntity.ok(libService.createSession(request));
        } catch (SessionOverlappingEx | IllegalArgumentException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(
            summary = "Получение списка всех сеансов пользователя"
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
    public ResponseEntity<List<SessionResponse>> findAllSessions(HttpServletRequest request){
        return ResponseEntity.ok(libService.findAllSessions(request));
    }

    @Operation(
            summary = "Удаление сеанса"
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
    @DeleteMapping("/sessions")
    public ResponseEntity<Object> deleteSession(@RequestParam("s_id") Integer sessionId){
        try {
            libService.deleteSessionById(sessionId);
            return ResponseEntity.ok("Success");
        } catch (EntityNotFoundException ex){
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @Operation(
            summary = "Изменение сеанса",
            description = "Возможно изменение таких данных как время сеанса, список выбранных книг"
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
    @PutMapping("/sessions")
    public ResponseEntity<Object> updateSessionContent(
            @RequestBody SessionRequest request, @RequestParam("s_id") Integer sessionId
    ){
        try{
            return ResponseEntity.ok(libService.updateSession(sessionId, request));
        } catch (SessionOverlappingEx e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(
            summary = "Фильтрация сеансов пользователя",
            description = "Фильтрация сеансов по времени"
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
    @PostMapping("/sessions/filter")
    public ResponseEntity<List<SessionResponse>> filterSessions(@RequestBody SessionFilter filter){
        return ResponseEntity.ok(libService.filterSessions(filter));
    }

    @Operation(
            summary = "Получение всех типов (жанров) книг"
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
    @GetMapping("/types")
    public ResponseEntity<List<Type>> findAllTypes(){
        return ResponseEntity.ok(libService.findAllTypes());
    }

    @Operation(
            summary = "Получение списков зарезервированных промежутков времени для конкретной книги в определенную дату"
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
    @GetMapping("/session_times_reserved")
    public ResponseEntity<Object> findReservedTimes(
            @RequestParam Integer bookId, @RequestParam LocalDate date
    ){
        try{
            return ResponseEntity.ok(libService.findReservedTimes(bookId, date));
        } catch (NoSuchElementException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(
            summary = "Получение списка всех зарезервированных промежутков времени для конкретного пользователя " +
                    "в определенную дату"
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
    @GetMapping("/user_session_times_reserved")
    public ResponseEntity<Object> findUserReservedTimes(
            @RequestParam Integer userId, @RequestParam LocalDate date
    ){
        try{
            return ResponseEntity.ok(libService.findUserReservedTimes(userId, date));
        } catch (NoSuchElementException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(
            summary = "Получение информации о пользователе"
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
    public ResponseEntity<Object> findUser(HttpServletRequest request){
            return ResponseEntity.ok(libService.findUser(request));
    }

    @Operation(
            summary = "Обновление email пользователя"
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
    @PatchMapping("/users/update_email")
    public ResponseEntity<Object> updateUserEmail(@RequestBody EmailChangeRequest request ){
        try{
            libService.updateUserEmail(request);
            return ResponseEntity.ok("Success");
        } catch (RuntimeException e){
            return ResponseEntity.badRequest().body(e.getClass() + " : " + e.getMessage());
        }
    }

    @Operation(
            summary = "Получение списка процентов посещений для каждого часа",
            description = "Возвращает ключ-значение, где ключ - час (12:00-13:00), а значение - " +
                    "отношение среднего числа пользователей, посещающих читальный зал в это время к общему числу посетителей"
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
    @GetMapping("/hourly-occupancy-rate")
    public ResponseEntity<Map<String, Float>> hourlyOccupancyRateMap(){
        return ResponseEntity.ok(libService.hourlyOccupancyRateMap());
    }
}
