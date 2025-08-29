package com.hua.ss_it21114.pages.organization;

import com.hua.ss_it21114.event.Event;
import com.hua.ss_it21114.event.EventRepository;
import com.hua.ss_it21114.event.EventSignup;
import com.hua.ss_it21114.event.EventSignupRepository;
import com.hua.ss_it21114.security.CurrentUserResolver;
import com.hua.ss_it21114.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Controller
@RequestMapping("/pages/organization/events")
public class PagesOrganizationSignupsController {

    private final EventRepository events;
    private final EventSignupRepository signups;
    private final CurrentUserResolver currentUser;

    public PagesOrganizationSignupsController(
            EventRepository events,
            EventSignupRepository signups,
            CurrentUserResolver currentUser
    ) {
        this.events = events;
        this.signups = signups;
        this.currentUser = currentUser;
    }

    @GetMapping("/{id}/signups")
    public String viewEventSignups(@PathVariable Long id, Model model) {
        Event e = events.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
        User me = currentUser.get();
        if (e.getOrganization() == null || !e.getOrganization().getId().equals(me.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your event");
        }
        List<EventSignup> list = signups.findByEvent_IdOrderByIdAsc(id);
        model.addAttribute("event", e);
        model.addAttribute("signups", list);
        return "pages/organization/event_signups";
    }
}
