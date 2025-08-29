package com.hua.ss_it21114.api;

import com.hua.ss_it21114.api.dto.*;
import com.hua.ss_it21114.event.EventRepository;
import com.hua.ss_it21114.event.EventService;
import com.hua.ss_it21114.security.CurrentUserResolver;
import com.hua.ss_it21114.user.User;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/event")
public class EventApiController {
    private final EventService events;
    private final EventRepository repo;
    private final CurrentUserResolver currentUser;

    public EventApiController(EventService events, EventRepository repo, CurrentUserResolver currentUser) {
        this.events = events;
        this.repo = repo;
        this.currentUser = currentUser;
    }

    @GetMapping
    public Iterable<EventDto> list() {
        return repo.findAll().stream().map(ApiMappers::toDto).toList();
    }

    @PreAuthorize("hasRole('ORGANIZATION')")
    @PostMapping
    public EventDto create(@RequestBody EventCreateReq req) {
        User u = currentUser.get();
        var e = events.create(u, req.title(), req.description(), req.startsAt(), req.endsAt());
        return ApiMappers.toDto(e);
    }

    @PreAuthorize("hasRole('ORGANIZATION')")
    @PutMapping("/{id}")
    public EventDto update(@PathVariable Long id, @RequestBody EventUpdateReq req) {
        var e = events.updateOwned(currentUser.get(), id,
                req.title(), req.description(), req.startsAt(), req.endsAt());
        return ApiMappers.toDto(e);
    }

    @PreAuthorize("hasRole('ORGANIZATION')")
    @DeleteMapping("/{id}")
    public ApiMessage delete(@PathVariable Long id) {
        events.deleteOwned(currentUser.get(), id);
        return new ApiMessage("Deleted");
    }
}
