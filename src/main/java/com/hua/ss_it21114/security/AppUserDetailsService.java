package com.hua.ss_it21114.security;

import com.hua.ss_it21114.user.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppUserDetailsService implements UserDetailsService {
    private final UserRepository users;
    private final VolunteerRepository volunteers;
    private final OrganizationRepository organizations;

    public AppUserDetailsService(
            UserRepository users,
            VolunteerRepository volunteers,
            OrganizationRepository organizations
    ) {
        this.users = users;
        this.volunteers = volunteers;
        this.organizations = organizations;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u = users.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        boolean enabled = true;
        if (u.getRole() == Role.VOLUNTEER) {
            enabled = volunteers.findByUserId(u.getId())
                    .map(v -> Boolean.TRUE.equals(v.isApproved()))
                    .orElse(false);
        } else if (u.getRole() == Role.ORGANIZATION) {
            enabled = organizations.findByUserId(u.getId())
                    .map(o -> Boolean.TRUE.equals(o.isApproved()))
                    .orElse(false);
        }
        var auths = List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole().name()));
        return org.springframework.security.core.userdetails.User
                .withUsername(u.getUsername())
                .password(u.getPasswordHash())
                .authorities(auths)
                .disabled(!enabled)
                .build();
    }
}

