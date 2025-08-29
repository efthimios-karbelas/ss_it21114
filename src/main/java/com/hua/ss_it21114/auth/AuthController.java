package com.hua.ss_it21114.auth;

import com.hua.ss_it21114.security.JwtService;
import com.hua.ss_it21114.user.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authManager;
    private final UserRepository users;
    private final OrganizationRepository organizations;
    private final VolunteerRepository volunteers;
    private final JwtService jwt;

    public AuthController(AuthenticationManager authManager,
                          UserRepository users,
                          OrganizationRepository organizations,
                          VolunteerRepository volunteers,
                          JwtService jwt) {
        this.authManager = authManager;
        this.users = users;
        this.organizations = organizations;
        this.volunteers = volunteers;
        this.jwt = jwt;
    }

    @PostMapping("/login")
    public TokenRes login(@RequestBody @Valid LoginReq req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password())
        );
        User u = users.findByUsername(req.username()).orElseThrow();
        if (!isApproved(u)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Your account is awaiting approval by an administrator."
            );
        }
        String token = jwt.generate(u.getUsername(), Map.of("role", u.getRole().name()));
        return new TokenRes(token);
    }

    private boolean isApproved(User u) {
        return switch (u.getRole()) {
            case ADMIN -> true;
            case ORGANIZATION -> organizations.findByUserId(u.getId())
                    .map(Organization::isApproved)
                    .orElse(false);
            case VOLUNTEER -> volunteers.findByUserId(u.getId())
                    .map(Volunteer::isApproved)
                    .orElse(false);
        };
    }
}
