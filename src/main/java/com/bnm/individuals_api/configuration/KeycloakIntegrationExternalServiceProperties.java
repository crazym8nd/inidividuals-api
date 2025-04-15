package com.bnm.individuals_api.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "keycloak")
public record KeycloakIntegrationExternalServiceProperties(
    String clientId,
    String clientSecret,
    String realm,
    String authUrl
) {

}
