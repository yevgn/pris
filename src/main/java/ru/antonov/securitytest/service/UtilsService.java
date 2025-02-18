package ru.antonov.securitytest.service;

import ru.antonov.securitytest.dto.BookPostRequest;
import ru.antonov.securitytest.dto.BookResponse;
import ru.antonov.securitytest.dto.SessionResponse;
import ru.antonov.securitytest.dto.UserResponse;
import ru.antonov.securitytest.entity.Book;
import ru.antonov.securitytest.entity.Session;
import ru.antonov.securitytest.entity.User;

public interface UtilsService {
    BookResponse mapToBookResponse(Book b);
    Book mapToBook(BookPostRequest request);
    SessionResponse mapToSessionResponse(Session s);
    UserResponse mapToUserDTO(User u);
}
