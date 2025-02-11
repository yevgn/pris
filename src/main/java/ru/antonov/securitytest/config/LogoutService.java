package ru.antonov.securitytest.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;
import ru.antonov.securitytest.auth.token.Token;
import ru.antonov.securitytest.auth.token.TokenRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LogoutService  {
    private final TokenRepository tokenRepository;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            return ;
        }
        jwt = authHeader.substring(7);
        var storedAccessToken = tokenRepository.findByToken(jwt)
                .orElse(null);
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(jwtService.extractUsername(jwt));
        List<Token> storedRefreshTokens =
                tokenRepository.findNotRevokedAndNotExpiredRefreshTokenByUserEmail(userDetails.getUsername());

        if(storedAccessToken != null){
            storedAccessToken.setExpired(true);
            storedAccessToken.setRevoked(true);
            tokenRepository.save(storedAccessToken);
        }

        storedRefreshTokens.forEach( t ->{
            t.setExpired(true);
            t.setRevoked(true);
        });

        tokenRepository.saveAll(storedRefreshTokens);
    }
}
