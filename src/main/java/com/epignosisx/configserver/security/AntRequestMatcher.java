package com.epignosisx.configserver.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.AntPathMatcher;

import javax.servlet.http.HttpServletRequest;

public class AntRequestMatcher  implements RequestMatcher {

    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public boolean matches(HttpServletRequest httpServletRequest) {
        String pathAndQuery = httpServletRequest.getRequestURI();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        for (GrantedAuthority authority : auth.getAuthorities()) {
            String allowedUri = authority.getAuthority();
            if (antPathMatcher.match(allowedUri, pathAndQuery)) {
                return true;
            }
        }

        return false;
    }
}
