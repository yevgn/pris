package ru.antonov.securitytest.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.antonov.securitytest.dto.*;
import ru.antonov.securitytest.entity.Type;
import ru.antonov.securitytest.exception.SessionOverlappingEx;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface LibService {
    List<BookResponse> findAllBooks();
    List<BookResponse> filterBooks(BookGetRequest filter);
    List<BookResponse> uploadBooksCsv(MultipartFile file) throws IOException;
    List<BookResponse> uploadBooksJSON(List<BookPostRequest> list);
    boolean deleteBook(String name);
    @Transactional
    SessionResponse createSession(SessionRequest request) throws SessionOverlappingEx, IllegalArgumentException;
    List<SessionResponse> findAllSessions(HttpServletRequest request);
    void deleteSessionById(Integer sessionId);
    SessionResponse updateSession(Integer sessionId, SessionRequest request) throws SessionOverlappingEx;
    List<UserResponse> findAllUsers();
    UserResponse findUserByEmail(String email);
    List<SessionResponse> filterSessions(SessionFilter filter);
    byte[] getBookImage(Integer bookId) throws IOException;
    List<Type> findAllTypes();

    List<SessionTime> findReservedTimes(Integer bookId, LocalDate date);

    UserResponse findUser(HttpServletRequest request);

    List<SessionTime> findUserReservedTimes(Integer userId, LocalDate date);

    void updateUserEmail(EmailChangeRequest request);

    Map<String, Float> hourlyOccupancyRateMap();
}
