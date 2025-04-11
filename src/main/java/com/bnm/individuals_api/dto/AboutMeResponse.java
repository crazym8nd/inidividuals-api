package com.bnm.individuals_api.dto;

import java.time.Instant;
import java.util.Set;

public record AboutMeResponse(
    String userId,
    String mail,
    Set<String> roles,
    Instant createdAt
) {

}
