package ru.antonov.securitytest.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Integer id;
    private String surname;
    private String name;
    private String patronymic;
    private String email;
    private String gr;
}
