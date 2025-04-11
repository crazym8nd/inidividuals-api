package com.bnm.individuals_api.configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity.CsrfSpec;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcReactiveOAuth2UserService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.ReactiveOAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtGrantedAuthoritiesConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
@Configuration
public class SecurityConfiguration {

  private final String[] publicRoutes = {"/v1/auth/test/**", "/v1/auth/login"};

  @Bean
  public SecurityWebFilterChain securityWebFilterChain(final ServerHttpSecurity http) {
    return http
        .csrf(CsrfSpec::disable)
        .authorizeExchange(configurer -> configurer
            .pathMatchers(HttpMethod.OPTIONS).permitAll()
            .pathMatchers(publicRoutes).permitAll()
            .pathMatchers("/v1/auth/info/**").authenticated()
            .anyExchange()
            .authenticated())

        .oauth2ResourceServer(customizer -> customizer.jwt(jwt -> {
          final ReactiveJwtAuthenticationConverter jwtAuthenticationConverter = new ReactiveJwtAuthenticationConverter();
          jwtAuthenticationConverter.setPrincipalClaimName("preferred_username");
          final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

          final Converter<Jwt, Collection<GrantedAuthority>> customConverter = jwt2 -> {
            final Map<String, Object> realmAccess = (Map<String, Object>) jwt2.getClaims()
                .get("realm_access");
            if (realmAccess == null || realmAccess.isEmpty()) {
              return new ArrayList<>();
            }

            final Collection<String> roles = (Collection<String>) realmAccess.get("roles");
            return roles.stream()
                .map(roleName -> "ROLE_" + roleName)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
          };

          jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(
              new ReactiveJwtGrantedAuthoritiesConverterAdapter(jwt3 -> {
                final Stream<GrantedAuthority> defaultAuthorities = jwtGrantedAuthoritiesConverter.convert(
                    jwt3).stream();
                final Stream<GrantedAuthority> customAuthorities = customConverter.convert(jwt3).stream();
                return Stream.concat(defaultAuthorities, customAuthorities)
                    .collect(Collectors.toList());
              })
          );

          jwt.jwtAuthenticationConverter(jwtAuthenticationConverter);

        }))
        .exceptionHandling(exceptionHandlingConfigurer -> exceptionHandlingConfigurer
            .authenticationEntryPoint((swe, e) -> {
              log.error("IN securityWebFilterChain - unauthorized error: {}", e.getMessage());
              return Mono.fromRunnable(
                  () -> swe.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED));
            })
            .accessDeniedHandler((swe, e) -> {
              log.error("IN securityWebFilterChain - access denied: {}", e.getMessage());

              return Mono.fromRunnable(() -> swe.getResponse().setStatusCode(HttpStatus.FORBIDDEN));
            }))
        .build();
  }

  @Bean
  public ReactiveOAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
    final OidcReactiveOAuth2UserService oidcUserService = new OidcReactiveOAuth2UserService();
    return userRequest -> oidcUserService.loadUser(userRequest)
        .map(oidcUser -> {
          final List<GrantedAuthority> grantedAuthorities = Stream.concat(
                  oidcUser.getAuthorities().stream(),
                  oidcUser.getClaimAsStringList("roles").stream()
                      .filter(authority -> authority.startsWith("ROLE_"))
                      .map(SimpleGrantedAuthority::new))
              .toList();
          return new DefaultOidcUser(grantedAuthorities, oidcUser.getIdToken(),
              oidcUser.getUserInfo(),
              "preferred_username");
        });
  }

}
