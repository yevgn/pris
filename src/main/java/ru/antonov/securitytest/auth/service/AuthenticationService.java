package ru.antonov.securitytest.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.antonov.securitytest.auth.role.Role;
import ru.antonov.securitytest.auth.dto.AuthenticationRequest;
import ru.antonov.securitytest.auth.dto.AuthenticationResponse;
import ru.antonov.securitytest.auth.dto.RegisterRequest;
import ru.antonov.securitytest.config.JwtService;
import ru.antonov.securitytest.auth.token.Token;
import ru.antonov.securitytest.auth.token.TokenMode;
import ru.antonov.securitytest.auth.token.TokenRepository;
import ru.antonov.securitytest.auth.token.TokenType;
import ru.antonov.securitytest.entity.User;
import ru.antonov.securitytest.repository.UserRepository;

import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authManager;
    private final TokenRepository tokenRepository;

    public AuthenticationResponse registerUser(RegisterRequest request){
        return register(request, Role.USER);
    }

    private AuthenticationResponse register(RegisterRequest request, Role role){
        var user = User.builder()
                .name(request.getName())
                .surname(request.getSurname())
                .patronymic(request.getPatronymic())
                .gr(role == Role.ADMIN ? null : request.getGr())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();

        userRepository.findByEmail(request.getEmail()).ifPresent(
                u -> { throw new IllegalArgumentException("user with this email exists");}
        );

        userRepository.save(user);

        var jwtAccessToken = jwtService.generateAccessToken(user);
        var jwtRefreshToken = jwtService.generateRefreshToken(user);

        saveUserToken(jwtAccessToken, user, TokenMode.ACCESS);
        saveUserToken(jwtRefreshToken, user, TokenMode.REFRESH);

        return AuthenticationResponse.builder()
                .accessToken(jwtAccessToken)
                .refreshToken(jwtRefreshToken)
                .build();
    }

    public AuthenticationResponse registerAdmin(RegisterRequest request){
        return register(request, Role.ADMIN);
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request){
        // происходит проверка username и credentials
        var auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        var user = (User) auth.getPrincipal();

        var jwtAccessToken = jwtService.generateAccessToken(user);
        var jwtRefreshToken = jwtService.generateRefreshToken(user);

        revokeAllUserTokens(user.getId());
        saveUserToken(jwtAccessToken, user, TokenMode.ACCESS);
        saveUserToken(jwtRefreshToken, user, TokenMode.REFRESH);

        return AuthenticationResponse.builder()
                .accessToken(jwtAccessToken)
                .refreshToken(jwtRefreshToken)
                .build();
    }

    public AuthenticationResponse refreshAccessToken(HttpServletRequest request){
        final String authHeader = request.getHeader("Authorization");

        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            throw new IllegalArgumentException("Нет токена/неправильная сигнатура токена");
        }

        final String refreshToken = authHeader.substring(7);
        final String username = jwtService.extractUsername(refreshToken);

        if(username != null){
            var user  = userRepository.findByEmail(username).
                    orElseThrow(() -> new UsernameNotFoundException("User not found") );

            var isRefreshTokenValid = tokenRepository.findByToken(refreshToken)
                    .filter(t -> t.getTokenMode().equals(TokenMode.REFRESH))
                    .map( t -> !t.isExpired() && !t.isRevoked() )
                    .orElse(false);

            if(jwtService.isTokenValid(refreshToken, user) && isRefreshTokenValid ){
                revokeAllUserTokens(user.getId() );
                final String newAccessToken = jwtService.generateAccessToken(user);
                final String newRefreshToken = jwtService.generateRefreshToken(user);
                saveUserToken(newAccessToken, user, TokenMode.ACCESS);
                saveUserToken(newRefreshToken, user, TokenMode.REFRESH);
                return AuthenticationResponse.builder()
                        .accessToken(newAccessToken)
                        .refreshToken(newRefreshToken)
                        .build();
            }
        }
        throw new IllegalArgumentException("Invalid token");
    }

    private void revokeAllUserTokens(Integer userId){
        var validUserTokens = tokenRepository.findAllValidTokenByUser(userId);
        if(validUserTokens.isEmpty()){
            return;
        }
        validUserTokens.forEach(t -> {
            t.setRevoked(true);
            t.setExpired(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

     private void saveUserToken(String jwtToken, User user, TokenMode tokenMode){
        var token = Token.builder()
                .tokenType(TokenType.BEARER)
                .tokenMode(tokenMode)
                .expired(false)
                .revoked(false)
                .token(jwtToken)
                .user(user)
                .build();
        tokenRepository.save(token);
    }

}
