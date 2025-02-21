package ru.antonov.securitytest.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import ru.antonov.securitytest.auth.role.Role;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthenticationFilter jwtAuthFilter;
   // private final LogoutHandler logoutHandler;
    private static final String[] WHITE_LIST_URL = {
            "/api/v1/auth/user/register",
            "/api/v1/auth/authenticate",
            "/swagger-ui/**",
            "/v3/api-docs/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests( req ->
                        req.requestMatchers(WHITE_LIST_URL)
                                .permitAll()
                                .requestMatchers("/api/v1/admin/**").hasAuthority(Role.ADMIN.name())
                                .requestMatchers("/api/v1/auth/admin/register").hasAuthority(Role.ADMIN.name())
                                .anyRequest()
                                .authenticated()
                )
                .sessionManagement(session ->  session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
//                .logout( logout ->
//                        logout.logoutUrl("/api/v1/auth/logout")
//                                .addLogoutHandler(logoutHandler)
//                                .logoutSuccessHandler(
//                                        (request, response, authentication) -> SecurityContextHolder.clearContext()
//                                )
//                );

        return http.build();
    }
}
