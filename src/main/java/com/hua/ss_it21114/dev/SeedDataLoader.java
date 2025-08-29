package com.hua.ss_it21114.dev;

import com.hua.ss_it21114.event.*;
import com.hua.ss_it21114.user.*;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Component
@Profile("dev")
@Order(9999)
public class SeedDataLoader implements ApplicationRunner {

    private final JdbcTemplate jdbc;
    private final UserRepository users;
    private final OrganizationRepository orgs;
    private final VolunteerRepository vols;
    private final PasswordEncoder passwordEncoder;
    private final EventService eventService;
    private final EventRepository eventRepository;
    private final EventSignupService signupService;
    private final Random random = new Random();

    public SeedDataLoader(
            JdbcTemplate jdbc,
            UserRepository users,
            OrganizationRepository orgs,
            VolunteerRepository vols,
            PasswordEncoder passwordEncoder,
            EventService eventService,
            EventRepository eventRepository,
            EventSignupService signupService
    ) {
        this.jdbc = jdbc;
        this.users = users;
        this.orgs = orgs;
        this.vols = vols;
        this.passwordEncoder = passwordEncoder;
        this.eventService = eventService;
        this.eventRepository = eventRepository;
        this.signupService = signupService;
    }

    @Override
    public void run(ApplicationArguments args) {
        truncateAllTables();
        int admins = 1;
        int volunteers = 50;
        int organizations = 20;
        int events = 10;
        int signups = 100;
        for (int i = 1; i <= admins; i++) {
            getOrCreateUser("admin" + i, "it21114", "Admin user " + i, Role.ADMIN);
        }
        for (int i = 1; i <= volunteers; i++) {
            User u = getOrCreateUser("volunteer" + i, "it21114", "Volunteer user " + i, Role.VOLUNTEER);
            ensureVolunteerProfile(u, random.nextDouble() < 0.8);
        }
        for (int i = 1; i <= organizations; i++) {
            User u = getOrCreateUser("organization" + i, "it21114", "Organization user " + i, Role.ORGANIZATION);
            ensureOrganizationProfile(u, "Organization " + i, random.nextDouble() < 0.8);
        }
        List<Organization> orgList = orgs.findAll();
        if (!orgList.isEmpty()) {
            for (int i = 1; i <= events; i++) {
                Organization org = orgList.get(random.nextInt(orgList.size()));
                User orgUser = org.getUser();
                createEvent(org, orgUser, "Event " + i, "Description " + i, "Location " + i, random.nextDouble() < 0.8);
            }
        }
        List<Volunteer> approvedVols = vols.findAll().stream()
                .filter(Volunteer::isApproved)
                .toList();

        List<Event> publishedEvents = eventRepository.findAll().stream()
                .filter(e -> Boolean.TRUE.equals(e.getPublished()))
                .toList();
        if (!approvedVols.isEmpty() && !publishedEvents.isEmpty()) {
            for (int i = 1; i <= signups; i++) {
                boolean signed = false;
                int attempts = 10;
                while (!signed && attempts > 0) {
                    Volunteer v = approvedVols.get(random.nextInt(approvedVols.size()));
                    User vUser = v.getUser();
                    Event e = publishedEvents.get(random.nextInt(publishedEvents.size()));
                    try {
                        EventSignup.Status[] statuses = EventSignup.Status.values();
                        signupService.signup(e.getId(), vUser, statuses[random.nextInt(statuses.length)]);
                        signed = true;
                    } catch (Exception _) {
                        attempts--;
                    }
                }
            }
        }
    }

    private User getOrCreateUser(String username, String rawPassword, String fullName, Role role) {
        return users.findByUsername(username).orElseGet(() -> {
            User u = new User();
            u.setUsername(username);
            u.setFullName(fullName);
            u.setRole(role);
            u.setPasswordHash(passwordEncoder.encode(rawPassword));
            return users.save(u);
        });
    }

    private void ensureVolunteerProfile(User u, boolean approved) {
        if (u.getRole() != Role.VOLUNTEER) return;
        vols.findByUserId(u.getId()).orElseGet(() -> {
            Volunteer v = new Volunteer();
            v.setUser(u);
            v.setApproved(approved);
            return vols.save(v);
        });
    }

    private void ensureOrganizationProfile(User u, String organizationName, boolean approved) {
        if (u.getRole() != Role.ORGANIZATION) return;
        orgs.findByUserId(u.getId()).orElseGet(() -> {
            Organization o = new Organization();
            o.setUser(u);
            o.setOrganizationName(organizationName);
            o.setApproved(approved);
            return orgs.save(o);
        });
    }

    private void truncateAllTables() {
        List<String> tables = jdbc.queryForList(
                "SELECT tablename FROM pg_tables WHERE schemaname = 'public'",
                String.class
        );
        List<String> skip = List.of("flyway_schema_history");
        List<String> truncatable = tables.stream()
                .filter(t -> skip.stream().noneMatch(s -> s.equalsIgnoreCase(t)))
                .collect(Collectors.toList());
        if (truncatable.isEmpty()) {
            return;
        }
        String joined = truncatable.stream()
                .map(t -> "\"" + t + "\"")
                .collect(Collectors.joining(", "));
        String sql = "TRUNCATE TABLE " + joined + " RESTART IDENTITY CASCADE";
        jdbc.execute(sql);
    }

    private void createEvent(Organization org, User orgUser, String title, String description, String location, Boolean published) {
        Event e = new Event();
        e.setTitle(org.getOrganizationName() + " - " + title);
        e.setDescription(description);
        e.setLocation(location);
        LocalDateTime start = LocalDateTime.now()
                .plusDays(random.nextInt(20) - 10) // +- 10
                .withHour(8 + random.nextInt(15)) // 8 - 22
                .withMinute(random.nextBoolean() ? 0 : 30)
                .withSecond(0)
                .withNano(0);
        e.setStartsAt(start);
        e.setEndsAt(start.plusHours(1 + random.nextInt(3 * 24))); // 1 to 3 * 24 h
        Event saved = eventService.createForOrg(orgUser, e);
        if (published) {
            eventService.publishByAdmin(saved.getId());
        }
    }
}
