package com.bnm.individuals_api.model;

import java.time.Instant;
import java.util.Set;

public record UserData(
    String userId,
    String mail,
    Set<String> roles,
    Instant createdAt
) {

}
