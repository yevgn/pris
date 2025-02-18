package ru.antonov.securitytest.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.antonov.securitytest.entity.Session;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<Session, Integer> {
    @Query(value = "SELECT COUNT(*) from sessions s JOIN session_books sb ON s.id = sb.session_id" +
            " WHERE sb.book_id = :bookId AND " +
            "((:startTime BETWEEN s.start_timestamp AND s.end_timestamp) OR " +
            "(:endTime BETWEEN s.start_timestamp AND s.end_timestamp) OR " +
            "(s.start_timestamp BETWEEN :startTime AND :endTime))",
            nativeQuery = true)
    int countOverlappingSessions(@Param("bookId") Integer bookId,
                                 @Param("startTime") LocalDateTime startTime,
                                 @Param("endTime") LocalDateTime endTime);

    @Query("SELECT s FROM Session s WHERE s.user.email = :userEmail")
    List<Session> findAllByUserEmail(@Param("userEmail") String userEmail);

    @Query("SELECT COUNT(s) FROM Session s WHERE s.user.id = :userId AND " +
            "((:startTime BETWEEN s.startTime AND s.endTime) OR " +
            "(:endTime BETWEEN s.startTime AND s.endTime) OR " +
            "(s.startTime BETWEEN :startTime AND :endTime))")
    int countOverlappingUserSessions(@Param("userId") Integer userId,
                                     @Param("startTime") LocalDateTime startTime,
                                     @Param("endTime") LocalDateTime endTime);

    @Query(value = "SELECT * FROM session_books sb" +
            " JOIN sessions s ON sb.session_id = s.id" +
            " WHERE sb.book_id = :bookId AND DATE(s.start_timestamp) = :date",
            nativeQuery = true)
    List<Session> findAllByIdAndDate(@Param("bookId") Integer bookId, @Param("date")LocalDate date);

    @Query(value = "SELECT * FROM sessions s" +
            " WHERE s.user_id = :userId AND DATE(s.start_timestamp) = :date",
            nativeQuery = true)
    List<Session> findAllByUserIdAndDate(@Param("userId") Integer userId, @Param("date")LocalDate date);

    @Query(value = "SELECT * FROM sessions s" +
            " WHERE DATE(s.start_timestamp) BETWEEN :startDate AND :endDate",
            nativeQuery = true)
    List<Session> findAllInDateRange(@Param("startDate") LocalDate startDate, @Param("endDate")LocalDate endDate);
}
