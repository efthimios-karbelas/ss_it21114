package com.hua.ss_it21114.dev;

import com.hua.ss_it21114.user.*;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Random;

@Component
@Profile("dev")
@Order(9999)
public class SeedDataLoader implements ApplicationRunner {

    private final UserRepository users;
    private final OrganizationRepository orgs;
    private final VolunteerRepository vols;
    private final PasswordEncoder passwordEncoder;

    public SeedDataLoader(
            UserRepository users,
            OrganizationRepository orgs,
            VolunteerRepository vols,
            PasswordEncoder passwordEncoder
    ) {
        this.users = users;
        this.orgs = orgs;
        this.vols = vols;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        int admins = 1;
        int volunteers = 20;
        int organizations = 5;
        for (int i = 1; i <= admins; i++) {
            seedUserIfMissing("admin" + i, "it21114", "Admin user " + i, Role.ADMIN, null);
        }
        for (int i = 1; i <= volunteers; i++) {
            seedUserIfMissing("volunteer" + i, "it21114", "Volunteer user " + i, Role.VOLUNTEER, null);
        }
        for (int i = 1; i <= organizations; i++) {
            seedUserIfMissing("organization" + i, "it21114", "Organization user " + i, Role.ORGANIZATION, "Organization " + i);
        }
    }

    private void seedUserIfMissing(
            String username,
            String rawPassword,
            String fullName,
            Role role,
            String organizationName
    ) {

        Optional<User> existing = users.findByUsername(username);
        if (existing.isPresent()) return;

        Random random = new Random();

        User u = new User();
        u.setUsername(username);
        u.setFullName(fullName);
        u.setRole(role);
        u.setPasswordHash(passwordEncoder.encode(rawPassword));
        u = users.save(u);

        if (role == Role.VOLUNTEER) {
            Volunteer v = new Volunteer();
            v.setUser(u);
            v.setApproved(Math.random() < 0.8);
            vols.save(v);
        } else if (role == Role.ORGANIZATION) {
            Organization o = new Organization();
            o.setUser(u);
            o.setOrganizationName(organizationName);
            o.setApproved(Math.random() < 0.8);
            orgs.save(o);
        }
    }
}
