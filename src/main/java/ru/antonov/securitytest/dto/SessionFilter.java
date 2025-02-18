package ru.antonov.securitytest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;


import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionFilter {
    @JsonProperty("u_id")
    private Integer userId;
    @JsonProperty("start_time")
    private LocalDateTime startTime;
    @JsonProperty("end_time")
    private LocalDateTime endTime;
}
