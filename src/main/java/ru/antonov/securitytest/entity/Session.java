package ru.antonov.securitytest.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "sessions")
public class Session {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "req_seq")
    @SequenceGenerator(name = "req_seq", sequenceName = "req_seq", allocationSize = 10)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToMany
    @JoinTable(
            name = "session_books", // Имя соединительной таблицы
            joinColumns = @JoinColumn(name = "session_id"), // Внешний ключ для текущей сущности
            inverseJoinColumns = @JoinColumn(name = "book_id") // Внешний ключ для связанной сущности
    )
    private List<Book> books;

    @Column(name = "start_timestamp")
    LocalDateTime startTime;

    @Column(name = "end_timestamp")
    LocalDateTime endTime;
}
