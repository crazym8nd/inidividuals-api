package com.bnm.individuals_api.service;

import com.bnm.individuals_api.model.AuthData;
import com.bnm.individuals_api.model.Credentials;
import com.bnm.individuals_api.model.RefreshToken;
import com.bnm.individuals_api.model.UserData;
import java.security.Principal;
import reactor.core.publisher.Mono;

public interface AuthService {

  Mono<AuthData> authenticateUser(Credentials credentials);

  Mono<AuthData> refreshAccessToken(RefreshToken request);

  Mono<UserData> aboutMe(Principal principal);
}
