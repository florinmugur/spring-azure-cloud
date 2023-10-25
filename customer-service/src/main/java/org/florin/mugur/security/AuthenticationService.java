package org.florin.mugur.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;

import static org.florin.mugur.configuration.SecurityConfiguration.AUTH_TOKEN_HEADER_NAME;
import static org.springframework.security.core.authority.AuthorityUtils.NO_AUTHORITIES;

@RequiredArgsConstructor
public class AuthenticationService {

    private final String configuredApiKey;

    public Authentication getAuthentication(HttpServletRequest request) {
        String apiKey = request.getHeader(AUTH_TOKEN_HEADER_NAME);
        if (apiKey == null || !apiKey.equals(configuredApiKey)) {
            throw new BadCredentialsException("Invalid API Key");
        }

        return new ApiKeyAuthentication(apiKey, NO_AUTHORITIES);
    }
}
