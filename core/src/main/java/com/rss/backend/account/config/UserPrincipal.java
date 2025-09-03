package com.rss.backend.account.config;

import com.rss.backend.account.entity.User;
import java.util.Collection;
import java.util.List;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Data
public class UserPrincipal implements UserDetails {

    private String email;
    private String password;
    private Long userId;
    private Long riderId;
    private Long driverId;
    private String role;

    public UserPrincipal(User user) {
        this.userId = user.getId();
        this.role = user.getRole().name();
        this.email = user.getEmail();
        this.password = user.getPassword();
        if(role.equals("RIDER") && user.getRider() != null) {
            this.riderId = user.getRider().getId();
        } else if(role.equals("DRIVER") && user.getDriver() != null) {
            this.driverId = user.getDriver().getId();
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getUsername() {
        return email;
    }
}
