package ru.antonov.securitytest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookPostRequest {
    private String name;
    @JsonProperty("a_name")
    private String aName;
    @JsonProperty("a_surname")
    private String aSurname;
    @JsonProperty("a_patronymic")
    private String aPatronymic;
    @JsonProperty("a_science_degree")
    private String aScienceDegree;
    @JsonProperty("a_workplace")
    private String aWorkplace;
    @JsonProperty("a_faculty")
    private String aFaculty;
    private String type;
    @JsonProperty("publish_year")
    private Integer publishYear;
    private Integer count;
}
