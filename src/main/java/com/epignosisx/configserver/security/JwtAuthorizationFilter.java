package com.epignosisx.configserver.security;


import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.micrometer.core.instrument.util.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;

public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String HEADER_STRING = "Authorization";
    private static final Log logger = LogFactory.getLog(JwtAuthorizationFilter.class);

    private JwtProperties jwtProperties;

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, JwtProperties jwtProperties) {
        super(authenticationManager);
        this.jwtProperties = jwtProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        // Read the Authorization header, where the JWT token should be
        String header = request.getHeader(HEADER_STRING);

        // If header does not contain BEARER or is null delegate to Spring impl and exit
        if (header == null || !header.startsWith(TOKEN_PREFIX)) {
            chain.doFilter(request, response);
            return;
        }

        // If header is present, try grab user principal from database and perform authorization
        Authentication authentication = getUsernamePasswordAuthentication(request);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Continue filter execution
        chain.doFilter(request, response);
    }

    private Authentication getUsernamePasswordAuthentication(HttpServletRequest request) {
        String token = request.getHeader(HEADER_STRING)
                .replace(TOKEN_PREFIX,"");

        if (token == null) {
            logger.warn("Token not provided");
            return null;
        }

        String[] secrets = this.jwtProperties.getSecrets();
        if (secrets.length == 0) {
            logger.warn("Secret not configured");
            return null;
        }

        // parse the token and validate it
        DecodedJWT decodedJwt = null;
        for (int i = 0; i < secrets.length; i++) {
            try {
                decodedJwt = JWT.require(HMAC512(secrets[i].getBytes()))
                        .build()
                        .verify(token);

                logger.trace("Token verification passed against secret #" + (i + 1));
                break;
            } catch (Exception ex) {
                logger.warn("Token verification failed against secret #" + (i + 1));
            }
        }

        if (decodedJwt == null) {
            return null;
        }

        String username = decodedJwt.getSubject();
        if (StringUtils.isBlank(username)) {
            logger.warn("Username is blank");
            return null;
        }

        Claim claim = decodedJwt.getClaim("scope");
        if (claim == null) {
            logger.warn("Scope not provided");
            return null;
        }

        String[] scope = claim.asArray(String.class);
        if (scope == null || scope.length == 0) {
            logger.warn("Scope is blank");
            return null;
        }

        ConfigUserPrincipal principal = new ConfigUserPrincipal(username, scope);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, null, principal.getAuthorities());
        return auth;
    }
}
