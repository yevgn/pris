package ru.antonov.securitytest.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.antonov.securitytest.auth.service.AuthenticationService;
import ru.antonov.securitytest.auth.dto.AuthenticationRequest;
import ru.antonov.securitytest.auth.dto.AuthenticationResponse;
import ru.antonov.securitytest.auth.dto.RegisterRequest;
import ru.antonov.securitytest.config.LogoutService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin
@Tag(
        name = "Контроллер для регистрации/авторизации в системе"
)
public class AuthenticationController {

    private final AuthenticationService authService;
    private final LogoutService logoutService;

    @Operation(
            summary = "Регистрация пользователя",
            description = "Ответ от сервера содержит access-токен и refresh-токен"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Не найдено",
                    content = @Content(mediaType = "application/json", examples = { @ExampleObject(
                            value = "{\"message\": \"Данной страницы не существует\", \"debugMessage\":\"" +
                                    "Данной страницы не существует\"}") })),
            @ApiResponse(responseCode = "200", description = "ОК"),
    })
    @PostMapping("/user/register")
    public ResponseEntity<AuthenticationResponse> registerUser(
            @RequestBody RegisterRequest request
    ) throws IllegalArgumentException {
        try {
            return ResponseEntity.ok(authService.registerUser(request));
        } catch (IllegalArgumentException e){
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(
            summary = "Регистрация администратора",
            description = "Ответ от сервера содержит access-токен и refresh-токен"
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
    @PostMapping("/admin/register")
    public ResponseEntity<AuthenticationResponse> registerAdmin(
            @RequestBody RegisterRequest request
    ) throws IllegalArgumentException {
        try {
            return ResponseEntity.ok(authService.registerAdmin(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(
            summary = "Авторизация",
            description = "Пользователь вводит логин и пароль, а в ответе от сервера получает access-токен и refresh-токен," +
                    " которые нужны для доступа к различным ресурсам"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Не найдено",
                    content = @Content(mediaType = "application/json", examples = { @ExampleObject(
                            value = "{\"message\": \"Данной страницы не существует\", \"debugMessage\":\"" +
                                    "Данной страницы не существует\"}") })),
            @ApiResponse(responseCode = "200", description = "ОК")
    })
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request
    )  throws IllegalArgumentException  {
        try {
            return ResponseEntity.ok(authService.authenticate(request));
        }  catch (IllegalArgumentException e){
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Object> logout(HttpServletRequest request, HttpServletResponse response) {
        logoutService.logout(request, response);
        return ResponseEntity.ok("Success");
    }

}
