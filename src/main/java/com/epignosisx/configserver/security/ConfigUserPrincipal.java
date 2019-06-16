package com.epignosisx.configserver.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ConfigUserPrincipal implements UserDetails {

    private final String username;
    private String[] scopes;

    public ConfigUserPrincipal(String username, String[] scopes) {
        this.username = username;
        this.scopes = scopes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();

        for (int i = 0; i < this.scopes.length; i++) {
            GrantedAuthority authority = new SimpleGrantedAuthority(scopes[i]);
            authorities.add(authority);
        }

        return authorities;
    }

    @Override
    public String getPassword() {
        return "not important";
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

