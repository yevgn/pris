package ru.antonov.securitytest.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.antonov.securitytest.auth.dto.AuthenticationResponse;
import ru.antonov.securitytest.auth.service.AuthenticationService;

@RestController
@RequestMapping("/api/v1/refresh")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin
@Tag(
        name = "Контроллер для обновления access-токенов"
)
public class RefreshAccessTokenController {

    private final AuthenticationService authService;

    @Operation(
            summary = "Обновление access-токена",
            description = "Пользователь отправляет refresh-токен, и если он оказывается валидным, то получает новый" +
                    " access-токен"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Не найдено",
                    content = @Content(mediaType = "application/json", examples = { @ExampleObject(
                            value = "{\"message\": \"Данной страницы не существует\", \"debugMessage\":\"" +
                                    "Данной страницы не существует\"}") })),
            @ApiResponse(responseCode = "400", description = "ОК"),
            @ApiResponse(responseCode = "403", description = "FORBIDDEN",
                    content = @Content(mediaType = "application/json", examples = { @ExampleObject(
                            value = "{\"message\": \"Access restricted\", \"debugMessage\":\"" +
                                    "/api/v1/lib/books\"}") }))
    })
    @PostMapping("/refresh-access-token")
    public ResponseEntity<AuthenticationResponse> refreshAccessToken(
            HttpServletRequest request
    ){
        try {
            return ResponseEntity.ok(authService.refreshAccessToken(request));
        } catch (IllegalArgumentException ex){
            return ResponseEntity.badRequest().build();
        }
    }
}
