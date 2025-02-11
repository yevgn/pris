package ru.antonov.securitytest.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "authors")
public class Author{
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "author_seq")
    @SequenceGenerator(name = "author_seq", sequenceName = "author_seq", allocationSize = 10)
    private Integer id;

    private String surname;

    private String name;

    private String patronymic;

    @Column(name = "science_degree")
    private String scienceDegree;

    private String workplace;

    private String faculty;
}
