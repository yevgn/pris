package ru.antonov.securitytest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookResponse {
    private Integer id;
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
}
