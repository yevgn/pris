package ru.antonov.securitytest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookGetRequest {
    private String name;
    @JsonProperty("a_name")
    private String aName;
    @JsonProperty("a_surname")
    private String aSurname;
    @JsonProperty("a_patronymic")
    private String aPatronymic;
    @JsonProperty("publish_year_from")
    private String publishYearFrom;
    @JsonProperty("publish_year_up")
    private String publishYearUp;
    private String type;
    @JsonProperty("sort_by")
    private String sortBy;
    @JsonProperty("sort_order")
    private SortOrder sortOrder;
}
