package ru.antonov.securitytest.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "types",
        uniqueConstraints = {@UniqueConstraint(columnNames = "name")}
)
public class Type {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "type_seq")
    @SequenceGenerator(name = "type_seq", sequenceName = "type_seq", allocationSize = 10)
    private Integer id;

    private String name;
}
