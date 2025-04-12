package com.bnm.individuals_api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Set;

public record AboutMeResponse(
    @JsonProperty("id")
    String userId,
    @JsonProperty("email")
    String mail,
    @JsonProperty("roles")
    Set<String> roles,
    @JsonProperty("created_at")
    Instant createdAt
) {

}
