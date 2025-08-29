package com.hua.ss_it21114.auth;

import com.hua.ss_it21114.user.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository users;
    private final OrganizationRepository orgs;
    private final VolunteerRepository vols;
    private final PasswordEncoder encoder;

    public AuthService(UserRepository users, OrganizationRepository orgs, VolunteerRepository vols, PasswordEncoder encoder) {
        this.users = users;
        this.orgs = orgs;
        this.vols = vols;
        this.encoder = encoder;
    }

    public User registerVolunteer(String username, String rawPassword, String fullName) {
        if (users.existsByUsername(username)) throw new IllegalArgumentException("Username already used");
        User u = new User();
        u.setUsername(username);
        u.setPasswordHash(encoder.encode(rawPassword));
        u.setFullName(fullName);
        u.setRole(Role.VOLUNTEER);
        u = users.save(u);

        Volunteer v = new Volunteer();
        v.setUser(u);
        vols.save(v);
        return u;
    }

    public User registerOrganization(String username, String rawPassword, String fullName, String organizationName) {
        if (users.existsByUsername(username)) throw new IllegalArgumentException("Username already used");
        User u = new User();
        u.setUsername(username);
        u.setPasswordHash(encoder.encode(rawPassword));
        u.setFullName(fullName);
        u.setRole(Role.ORGANIZATION);
        u = users.save(u);

        Organization o = new Organization();
        o.setUser(u);
        o.setOrganizationName(organizationName);
        orgs.save(o);
        return u;
    }
}
