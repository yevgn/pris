package ru.antonov.securitytest.service;

import org.springframework.stereotype.Service;
import ru.antonov.securitytest.dto.BookPostRequest;
import ru.antonov.securitytest.dto.BookResponse;
import ru.antonov.securitytest.dto.SessionResponse;
import ru.antonov.securitytest.dto.UserResponse;
import ru.antonov.securitytest.entity.*;

@Service
public class UtilsServiceImpl implements UtilsService{
    @Override
    public BookResponse mapToBookResponse(Book b) {
        return BookResponse
                .builder()
                .id(b.getId())
                .name(b.getName())
                .aName(b.getAuthor().getName())
                .aSurname(b.getAuthor().getSurname())
                .aPatronymic(b.getAuthor().getPatronymic())
                .aScienceDegree(b.getAuthor().getScienceDegree())
                .aWorkplace(b.getAuthor().getWorkplace())
                .aFaculty(b.getAuthor().getFaculty())
                .type(b.getType().getName())
                .publishYear(b.getPublishYear())
                .build();
    }

    @Override
    public Book mapToBook(BookPostRequest request) {
        return Book.builder()
                .name(request.getName())
                .author(
                        Author.builder()
                                .name(request.getAName())
                                .surname(request.getASurname())
                                .patronymic(request.getAPatronymic())
                                .faculty(request.getAFaculty())
                                .scienceDegree(request.getAScienceDegree())
                                .workplace(request.getAWorkplace())
                                .build()
                )
                .publishYear(request.getPublishYear())
                .type(
                        Type.builder()
                                .name(request.getType())
                                .build()
                )
                .count(request.getCount())
                .build();
    }

    @Override
    public SessionResponse mapToSessionResponse(Session s) {
        return SessionResponse.builder()
                .id(s.getId())
                .user(mapToUserDTO(s.getUser()))
                .books(s.getBooks().stream().map(Book::getName).toList())
                .startTime(s.getStartTime())
                .endTime(s.getEndTime())
                .build();
    }

    @Override
    public UserResponse mapToUserDTO(User u) {
        return UserResponse.builder()
                .id(u.getId())
                .surname(u.getSurname())
                .name(u.getName())
                .patronymic(u.getPatronymic())
                .gr(u.getGr())
                .email(u.getEmail())
                .build();
    }
}
