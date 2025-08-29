package com.hua.ss_it21114.api;

import com.hua.ss_it21114.api.dto.ApiMessage;
import com.hua.ss_it21114.api.dto.MeDto;
import com.hua.ss_it21114.api.dto.TokenDto;
import com.hua.ss_it21114.auth.AuthService;
import com.hua.ss_it21114.auth.LoginReq;
import com.hua.ss_it21114.auth.RegisterOrgReq;
import com.hua.ss_it21114.auth.RegisterVolunteerReq;
import com.hua.ss_it21114.security.CurrentUser;
import com.hua.ss_it21114.security.JwtService;
import com.hua.ss_it21114.user.User;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserApiController {
    private final AuthService authService;
    private final JwtService jwtService;
    private final AuthenticationManager authManager;

    public UserApiController(
            AuthService authService,
            JwtService jwtService,
            AuthenticationManager authManager
    ) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.authManager = authManager;
    }

    @PostMapping("/login")
    public TokenDto login(@RequestBody LoginReq req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password())
        );
        User u = (User) auth.getPrincipal();
        String token = jwtService.generateToken(u.getUsername(), u.getRole().name(), u.getId());
        return new TokenDto(token);
    }

    @PostMapping("/register/volunteer")
    public ApiMessage registerVolunteer(@RequestBody RegisterVolunteerReq req) {
        authService.registerVolunteer(req.username(), req.password(), req.fullName());
        return new ApiMessage("Volunteer registered. Awaiting approval.");
    }

    @PostMapping("/register/organization")
    public ApiMessage registerOrg(@RequestBody RegisterOrgReq req) {
        authService.registerOrganization(req.username(), req.password(),
                req.fullName(), req.organizationName());
        return new ApiMessage("Organization registered. Awaiting approval.");
    }

    @GetMapping("/me")
    public MeDto me(@CurrentUser User u) {
        return new MeDto(u.getId(), u.getUsername(), u.getFullName(), u.getRole().name());
    }
}
