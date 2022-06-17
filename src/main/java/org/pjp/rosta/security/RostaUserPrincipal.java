package org.pjp.rosta.security;

import java.util.Collection;
import java.util.Collections;

import org.pjp.rosta.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class RostaUserPrincipal implements UserDetails {

    private static final long serialVersionUID = -7468120881753238347L;

    private final User user;

    public RostaUserPrincipal(User user) {
        super();
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.isAdmin() ? Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN")) : Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    public void setUsername(String username) {
        user.setUsername(username);
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
        return user.isEnabled();
    }

    public boolean isPasswordChange() {
        return user.isPasswordChange();
    }

    public void setPasswordChange(boolean passwordChange) {
        user.setPasswordChange(passwordChange);
    }

}
