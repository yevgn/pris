package ru.antonov.securitytest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailChangeRequest {
    @JsonProperty("old_email")
    private String oldEmail;
    @JsonProperty("new_email")
    private String newEmail;
}
