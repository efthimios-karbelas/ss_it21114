package com.hua.ss_it21114.api;

import com.hua.ss_it21114.api.dto.ApiMessage;
import com.hua.ss_it21114.api.dto.SignupDto;
import com.hua.ss_it21114.event.EventSignup;
import com.hua.ss_it21114.event.EventSignupRepository;
import com.hua.ss_it21114.event.EventSignupService;
import com.hua.ss_it21114.security.CurrentUserResolver;
import com.hua.ss_it21114.user.User;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/eventsignup")
public class EventSignupApiController {
    private final EventSignupService signups;
    private final EventSignupRepository repo;
    private final CurrentUserResolver currentUser;

    public EventSignupApiController(EventSignupService signups,
                                    EventSignupRepository repo,
                                    CurrentUserResolver currentUser) {
        this.signups = signups;
        this.repo = repo;
        this.currentUser = currentUser;
    }

    @PreAuthorize("hasRole('VOLUNTEER')")
    @PostMapping("/event/{eventId}")
    public SignupDto signup(@PathVariable Long eventId) {
        User u = currentUser.get();
        return ApiMappers.toDto(signups.signup(eventId, u, EventSignup.Status.PENDING));
    }

    @PreAuthorize("hasRole('VOLUNTEER')")
    @GetMapping("/my")
    public Iterable<SignupDto> mySignups(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        User u = currentUser.get();
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return repo.findByVolunteer_Id(u.getId(), pageable)
                .map(ApiMappers::toDto);
    }

    @PreAuthorize("hasRole('VOLUNTEER')")
    @DeleteMapping("/{id}")
    public ApiMessage cancel(@PathVariable Long id) {
        signups.cancelByVolunteer(id, currentUser.get());
        return new ApiMessage("Cancelled");
    }

    @PreAuthorize("hasRole('ORGANIZATION')")
    @PostMapping("/{id}/approve")
    public ApiMessage approve(@PathVariable Long id) {
        signups.approve(id, currentUser.get());
        return new ApiMessage("Approved");
    }

    @PreAuthorize("hasRole('ORGANIZATION')")
    @PostMapping("/{id}/reject")
    public ApiMessage reject(@PathVariable Long id) {
        signups.reject(id, currentUser.get());
        return new ApiMessage("Rejected");
    }

    @PreAuthorize("hasRole('ORGANIZATION')")
    @PostMapping("/{id}/checkin")
    public ApiMessage checkIn(@PathVariable Long id) {
        signups.checkIn(id, currentUser.get());
        return new ApiMessage("Checked in");
    }
}
