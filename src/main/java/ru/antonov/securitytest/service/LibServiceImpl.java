package ru.antonov.securitytest.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import ru.antonov.securitytest.auth.token.TokenMode;
import ru.antonov.securitytest.auth.token.TokenRepository;
import ru.antonov.securitytest.config.JwtService;
import ru.antonov.securitytest.dto.*;
import ru.antonov.securitytest.entity.*;
import ru.antonov.securitytest.exception.SessionOverlappingEx;
import ru.antonov.securitytest.repository.*;

import java.io.*;

import java.nio.charset.Charset;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class LibServiceImpl implements LibService {
    private final UtilsService utilsService;
    @PersistenceContext
    private final EntityManager em;
    private final BookRepository bookRepository;
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final TypeRepository typeRepository;
    private final JwtService jwtService;
    private final TokenRepository tokenRepository;

    @Value("${start-working-hour}")
    private LocalTime startWorkingTime;
    @Value("${end-working-hour}")
    private LocalTime endWorkingTime;
    @Value("${min-days-to-plan-ahead}")
    private int minDaysToCreateSessionAhead;
    @Value("${max-days-to-plan-ahead}")
    private int maxDaysToCreateSessionAhead;

    @Override
    public List<BookResponse> filterBooks(BookGetRequest filter) {
        List<Book> listByName = findBooksByName(filter.getName());
        List<Book> filteredList = filterBooks(filter, listByName);
        List<Book> sortedList = sortBooks(filter.getSortBy(), filter.getSortOrder(), filteredList);
        return sortedList.stream().map(utilsService::mapToBookResponse).toList();
    }

    @Override
    public List<BookResponse> findAllBooks() {
        return findBooksByName("").stream().map(utilsService::mapToBookResponse).toList();
    }

    @Override
    public List<BookResponse> uploadBooksCsv(MultipartFile file) throws IOException {
        List<Book> books = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), Charset.forName("Windows-1251")));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withIgnoreHeaderCase()
                     .withTrim())) {

            Iterable<CSVRecord> csvRecords = csvParser.getRecords();

            for (CSVRecord csvRecord : csvRecords) {
                Author author = Author.builder()
                        .name(csvRecord.get("a_name"))
                        .surname(csvRecord.get("a_surname"))
                        .patronymic(csvRecord.get("a_patronymic"))
                        .scienceDegree(csvRecord.get("a_science_degree"))
                        .workplace(csvRecord.get("a_workplace"))
                        .faculty(csvRecord.get("a_faculty"))
                        .build();
                Book book = Book.builder()
                        .name(csvRecord.get("name"))
                        .author(author)
                        .type(Type.builder()
                                .name(csvRecord.get("type"))
                                .build()
                        )
                        .publishYear(Integer.parseInt(csvRecord.get("publish_year")))
                        .count(Integer.parseInt(csvRecord.get("count")))
                        .build();

                books.add(book);
            }

            bookRepository.saveAll(books);
            return books.stream().map(utilsService::mapToBookResponse).toList();
        }
    }

    @Override
    public List<BookResponse> uploadBooksJSON(List<BookPostRequest> list) {
        List<Book> books = list.stream().map(utilsService::mapToBook).toList();
        bookRepository.saveAll(books);
        return books.stream().map(utilsService::mapToBookResponse).toList();
    }

    @Override
    public boolean deleteBook(String name) {
        Optional<Book> optBook = bookRepository.findByName(name);
        if (optBook.isPresent()) {
            bookRepository.deleteByName(name);
            return true;
        }
        return false;
    }

    @Override
    public UserResponse findUser(HttpServletRequest request) {
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Нет токена/неправильная сигнатура токена");
        }

        final String token = authHeader.substring(7);
        final String username = jwtService.extractUsername(token);

        if (username != null) {
            var user = userRepository.findByEmail(username).
                    orElseThrow(() -> new UsernameNotFoundException("User not found"));

            var isTokenValid = tokenRepository.findByToken(token)
                    .filter(t -> t.getTokenMode().equals(TokenMode.ACCESS))
                    .map(t -> !t.isExpired() && !t.isRevoked())
                    .orElse(false);

            if (jwtService.isTokenValid(token, user) && isTokenValid) {
                return utilsService.mapToUserDTO(user);
            }
        }
        throw new IllegalArgumentException("Invalid token");
    }

    @Override
    @Transactional
    public SessionResponse createSession(SessionRequest request)
            throws SessionOverlappingEx, IllegalArgumentException {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        List<Book> books = request.getBookIds().stream()
                .map(
                        id -> bookRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Книга не найдена"))
                ).toList();

        // Может выбросить исключение
        checkSessionConsistency(request, books);

        Session session = Session.builder()
                .books(books)
                .user(user)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .build();

        return utilsService.mapToSessionResponse(sessionRepository.save(session));
    }

    private void checkSessionConsistency(SessionRequest request, List<Book> books)
            throws SessionOverlappingEx, IllegalArgumentException {

        if (request.getStartTime().isAfter(request.getEndTime()))
            throw new IllegalArgumentException("Неправильно указано время.");

        LocalTime startTime = request.getStartTime().toLocalTime();
        LocalTime endTime = request.getEndTime().toLocalTime();

        LocalDateTime minDateToCreateSession = LocalDateTime.now().plusDays(minDaysToCreateSessionAhead);
        LocalDateTime maxDateToCreateSession = LocalDateTime.now().plusDays(maxDaysToCreateSessionAhead);

        if( request.getStartTime().isBefore(minDateToCreateSession ) ||
                request.getStartTime().isAfter(maxDateToCreateSession) )
            throw new IllegalArgumentException(
                    String.format(
                            minDateToCreateSession.format(DateTimeFormatter.ISO_DATE),
                            maxDateToCreateSession.format(DateTimeFormatter.ISO_DATE) ,
                            "Можно назначить сеанс с %s по %s"
                    )
            );

        if (startTime.isBefore(startWorkingTime) || startTime.isAfter(endWorkingTime) ||
                endTime.isAfter(endWorkingTime)) {
            throw new IllegalArgumentException(
                    String.format("Читальный зал работает с %s до %s", startWorkingTime, endWorkingTime)
            );
        }

        int overlappingUserSessions = sessionRepository
                .countOverlappingUserSessions(request.getUserId(), request.getStartTime(), request.getEndTime());
        if (overlappingUserSessions > 0) {
            throw new SessionOverlappingEx(
                    String.format("В данный интервал времени [%s; %s] у этого пользователя уже есть запланированные сеансы",
                            request.getStartTime(), request.getEndTime())
            );
        }

        for (Book b : books) {
            int overlappingSessions = sessionRepository
                    .countOverlappingSessions(b.getId(), request.getStartTime(), request.getEndTime());

            if (b.getCount() - overlappingSessions <= 0) {
                throw new SessionOverlappingEx(
                        String.format("В данный интервал времени [%s; %s] эта книга недоступна",
                                request.getStartTime(), request.getEndTime())
                );
            }
        }
    }

    @Override
    public void updateUserEmail(EmailChangeRequest request) {
        final String EMAIL_REGEX = "^\\w+@\\w+\\.[a-zA-Z]+$";
        final Pattern PATTERN = Pattern.compile(EMAIL_REGEX);
        Matcher matcher1 = PATTERN.matcher(request.getOldEmail());
        Matcher matcher2 = PATTERN.matcher(request.getNewEmail());
        if (!matcher1.matches() || !matcher2.matches()) {
            throw new IllegalArgumentException("Неправильный формат email.");
        }

        Optional<User> userOpt = userRepository.findByEmail(request.getOldEmail());
        User user = userOpt.orElseThrow();

        userRepository.findByEmail(request.getNewEmail()).ifPresent(
                c -> {
                    throw new IllegalArgumentException(
                            String.format("Пользователь с %s email существует", request.getNewEmail()));
                }
        );

        user.setEmail(request.getNewEmail());

        userRepository.save(user);

    }

    @Override
    public List<SessionTime> findUserReservedTimes(Integer userId, LocalDate date) {
        return sessionRepository.findAllByUserIdAndDate(userId, date).parallelStream().
                map(
                        s ->
                                SessionTime.builder()
                                        .startTime(s.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")))
                                        .endTime(s.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")))
                                        .build()

                )
                .sorted(Comparator.comparing(SessionTime::getStartTime))
                .toList();
    }

    @Override
    public List<SessionTime> findReservedTimes(Integer bookId, LocalDate date) {
        return sessionRepository.findAllByIdAndDate(bookId, date).parallelStream().
                map(
                        s ->
                                SessionTime.builder()
                                        .startTime(s.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")))
                                        .endTime(s.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")))
                                        .build()

                )
                .sorted(Comparator.comparing(SessionTime::getStartTime))
                .toList();
    }

    @Override
    public List<SessionResponse> findAllSessions(HttpServletRequest request) {
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Нет токена/неправильная сигнатура токена");
        }

        final String token = authHeader.substring(7);
        final String username = jwtService.extractUsername(token);

        if (username != null) {
            var user = userRepository.findByEmail(username).
                    orElseThrow(() -> new UsernameNotFoundException("User not found"));

            var isTokenValid = tokenRepository.findByToken(token)
                    .filter(t -> t.getTokenMode().equals(TokenMode.ACCESS))
                    .map(t -> !t.isExpired() && !t.isRevoked())
                    .orElse(false);

            if (jwtService.isTokenValid(token, user) && isTokenValid) {
                List<Session> sessions = sessionRepository.findAllByUserEmail(user.getEmail());
                return sessions.stream().sorted(Comparator.comparing(Session::getStartTime))
                        .map(utilsService::mapToSessionResponse).toList();
            }
        }
        throw new IllegalArgumentException("Invalid token");
    }

    @Override
    public void deleteSessionById(Integer sessionId) {
        if (sessionRepository.findById(sessionId).isEmpty()) {
            throw new EntityNotFoundException("No such session");
        }
        sessionRepository.deleteById(sessionId);
    }

    @Override
    public SessionResponse updateSession(Integer sessionId, SessionRequest request) throws SessionOverlappingEx {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        if (sessionRepository.findById(sessionId).isEmpty()) {
            throw new EntityNotFoundException("No such session");
        }

        List<Book> books = request.getBookIds().stream()
                .map(
                        id -> bookRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Книга не найдена"))
                ).toList();

        checkSessionConsistency(request, books);

        Session session = Session.builder()
                .id(sessionId)
                .books(books)
                .user(user)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .build();

        return utilsService.mapToSessionResponse(sessionRepository.save(session));
    }

    @Override
    public List<UserResponse> findAllUsers() {
        return userRepository.findAll().stream().map(utilsService::mapToUserDTO).toList();
    }

    @Override
    public UserResponse findUserByEmail(String email) {
        return userRepository.findByEmail(email).map(utilsService::mapToUserDTO)
                .orElseThrow(() -> new EntityNotFoundException("No such user"));
    }

    @Override
    public List<SessionResponse> filterSessions(SessionFilter filter) {
        List<Session> sessions = sessionRepository.findAll();
        if (filter.getUserId() != 0) {
            sessions = sessions.stream().filter(s -> s.getUser().getId().equals(filter.getUserId())).toList();
        }
        if (filter.getStartTime() != null && filter.getEndTime() != null) {
            sessions = sessions.stream().filter(
                            s -> s.getStartTime().isAfter(filter.getStartTime()) && s.getEndTime().isBefore(filter.getEndTime())
                    )
                    .toList();
        }
        List<Session> sortedSessions = sessions.stream().sorted(Comparator.comparing(Session::getStartTime)).toList();
        return sortedSessions.stream().map(utilsService::mapToSessionResponse).toList();
    }

    @Override
    public byte[] getBookImage(Integer bookId) throws IOException {
        Optional<Book> book = bookRepository.findById(bookId);
        final String path = book.orElseThrow().getImagePath();

        FileInputStream fis = new FileInputStream(path);
        byte[] imageBytes = fis.readAllBytes();
        fis.close();
        return imageBytes;
    }

    @Override
    public Map<String, Float> hourlyOccupancyRateMap() {
        final int previousDaysAmountToCollectStats = 30;
        LocalDate today = LocalDate.now();
        LocalDate startFrom = today.minusDays(previousDaysAmountToCollectStats);

        List<Session> allSessions = sessionRepository.findAllInDateRange(startFrom, today);

        List<SessionTime> libWorkingHoursPairs = intermediateHoursPairs(
                startWorkingTime, endWorkingTime
        );

        Map<SessionTime, Float> result = new HashMap<>();
        libWorkingHoursPairs.forEach(i -> result.put(i, 0.0f));

        allSessions.forEach(s -> {
                    List<SessionTime> intermediateHoursPairs
                            = intermediateHoursPairs(s.getStartTime().toLocalTime(), s.getEndTime().toLocalTime());
                    intermediateHoursPairs.forEach(h -> {
                        result.compute(h, (k, value) -> (value == null) ? 1 : value + 1);
                    });
                }
        );

        float sum = result.entrySet().parallelStream().reduce(0.0f, (x, y) -> x + y.getValue(), Float::sum);
        float x = 1 / sum;

        return result.entrySet().stream().collect(
                Collectors.toMap(e -> e.getKey().toString(), e -> {
                            e.setValue(e.getValue() * x * 100);
                            return e.getValue();
                        },
                        (existing, replacement) -> existing,
                        TreeMap::new
                )
        );
    }

    private List<SessionTime> intermediateHoursPairs(LocalTime start, LocalTime end) {
        return Stream.iterate(
                new SessionTime(
                        convertToStringHour(String.valueOf(start.getHour())),
                        convertToStringHour(String.valueOf(start.plusHours(1).getHour()))
                ),
                s -> end.getMinute() != 0 ? Integer.parseInt(s.getStartTime()) <= end.getHour()
                        : Integer.parseInt(s.getStartTime()) < end.getHour(),
                s -> new SessionTime(
                        convertToStringHour(String.valueOf(Integer.parseInt(s.getStartTime()) + 1)),
                        convertToStringHour(String.valueOf(Integer.parseInt(s.getStartTime()) + 2))
                )
        ).toList();
    }

    private String convertToStringHour(String time){
        if(Integer.parseInt(time) < 10)
            return "0" + time;
        return time;
    }

    private List<Book> findBooksByName(String name) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Book> cr =
                cb.createQuery(Book.class);
        Root<Book> root =
                cr.from(Book.class);

        root.fetch("author");
        root.fetch("type");

        if (!name.isEmpty()) {
            cr.where(cb.like(cb.upper(root.get("name")), cb.upper(cb.literal(name))));
        }

        cr.select(root);

        return em.createQuery(cr).getResultList();
    }

    private List<Book> filterBooks(BookGetRequest filter, List<Book> l) {
        List<Book> res = l;
        if (!filter.getAName().isEmpty()) {
            res = l.stream().filter(b -> b.getAuthor().getName().equalsIgnoreCase(filter.getAName())).toList();
        }
        if (!filter.getASurname().isEmpty()) {
            res = l.stream().filter(b -> b.getAuthor().getSurname().toUpperCase().equalsIgnoreCase(filter.getASurname())).toList();
        }
        if (!filter.getAPatronymic().isEmpty()) {
            res = l.stream().filter(b -> b.getAuthor().getPatronymic().toUpperCase().equalsIgnoreCase(filter.getAPatronymic())).toList();
        }
        if (!filter.getType().isEmpty()) {
            res = l.stream().filter(b -> b.getType().getName().toUpperCase().equalsIgnoreCase(filter.getType())).toList();
        }
        if (!filter.getPublishYearFrom().isEmpty() && !filter.getPublishYearUp().isEmpty()) {
            res = l.stream().filter(b -> b.getPublishYear() >= Integer.parseInt(filter.getPublishYearFrom()) &&
                    b.getPublishYear() <= Integer.parseInt(filter.getPublishYearUp())).toList();
        }
        return res;
    }

    private List<Book> sortBooks(String sortBy, SortOrder sortOrder, List<Book> l) {

        switch (sortBy) {
            case "name" -> {
                l = l.stream().sorted(
                        (b1, b2) -> sortOrder == SortOrder.ASC ? b1.getName().compareTo(b2.getName()) :
                                b2.getName().compareTo(b1.getName())
                ).toList();
            }
            case "publish_year" -> {
                l = l.stream().sorted(
                        (b1, b2) -> sortOrder == SortOrder.ASC ? b1.getPublishYear().compareTo(b2.getPublishYear()) :
                                b2.getPublishYear().compareTo(b1.getPublishYear())
                ).toList();
            }
            default -> throw new IllegalArgumentException("Данного поля для сортировки не существует : " + sortBy);
        }
        return l;
    }

    @Override
    public List<Type> findAllTypes() {
        return typeRepository.findAll();
    }
}
