package com.hua.ss_it21114.auth;

import com.hua.ss_it21114.security.JwtService;
import com.hua.ss_it21114.user.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final AuthenticationManager authManager;
    private final JwtService jwt;
    private final UserRepository users;

    public AuthController(AuthService authService, AuthenticationManager authManager, JwtService jwt, UserRepository users) {
        this.authService = authService;
        this.authManager = authManager;
        this.jwt = jwt;
        this.users = users;
    }

    @PostMapping("/register/volunteer")
    public ResponseEntity<?> registerVolunteer(@RequestBody @Valid RegisterVolunteerReq req) {
        authService.registerVolunteer(req.username(), req.password(), req.fullName());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/register/org")
    public ResponseEntity<?> registerOrg(@RequestBody @Valid RegisterOrgReq req) {
        authService.registerOrg(req.username(), req.password(), req.fullName(), req.organizationName());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public TokenRes login(@RequestBody @Valid LoginReq req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password()));
        var u = users.findByUsername(req.username()).orElseThrow();
        String token = jwt.generate(u.getUsername(), Map.of("role", u.getRole().name()));
        return new TokenRes(token);
    }
}
