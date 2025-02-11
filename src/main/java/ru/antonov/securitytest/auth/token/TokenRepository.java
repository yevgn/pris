package ru.antonov.securitytest.auth.token;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {
    @Query(value = """
            SELECT t FROM Token t JOIN User u on t.user.id = u.id
            WHERE u.id = :userId and (t.expired = false and t.revoked = false)
            """
    )
    List<Token> findAllValidTokenByUser(Integer userId);

    Optional<Token> findByToken(String token);

    @Query(value = """
            SELECT t FROM Token t JOIN User u ON t.user.id = u.id
            WHERE u.email = :email AND t.tokenMode = REFRESH
            AND t.expired = false AND t.revoked = false
            """)
    List<Token> findNotRevokedAndNotExpiredRefreshTokenByUserEmail(String email);
}
