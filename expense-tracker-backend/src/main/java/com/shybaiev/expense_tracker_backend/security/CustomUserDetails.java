package com.shybaiev.expense_tracker_backend.security;

import com.shybaiev.expense_tracker_backend.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class CustomUserDetails implements UserDetails {

    private final User user;
    private final Long id;


    public CustomUserDetails(User user) {
        this.user = user;
        this.id = user.getId();
    }

    public Long getId() {
        return user.getId();
    }

    public Long id() {
        return user.getId();
    }



    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return org.springframework.security.core.authority.AuthorityUtils.createAuthorityList("ROLE_" + user.getRole().name());
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return user.isEnabled(); }
}