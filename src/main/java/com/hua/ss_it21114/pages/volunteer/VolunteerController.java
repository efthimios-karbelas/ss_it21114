package com.hua.ss_it21114.pages.volunteer;

import com.hua.ss_it21114.event.*;
import com.hua.ss_it21114.security.CurrentUserResolver;
import com.hua.ss_it21114.user.User;
import com.hua.ss_it21114.user.VolunteerRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

@Controller
@RequestMapping("/pages/volunteer")
@PreAuthorize("hasRole('VOLUNTEER')")
public class VolunteerController {
    private final EventService events;
    private final EventSignupService signups;
    private final EventSignupRepository signupRepository;
    private final VolunteerRepository volunteers;
    private final CurrentUserResolver currentUser;

    public VolunteerController(
            EventService events,
            EventSignupService signups,
            EventSignupRepository signupRepository,
            VolunteerRepository volunteers,
            CurrentUserResolver currentUser
    ) {
        this.events = events;
        this.signups = signups;
        this.signupRepository = signupRepository;
        this.currentUser = currentUser;
        this.volunteers = volunteers;
    }

    @GetMapping("/events")
    public String browse(@RequestParam(defaultValue = "0") int page, Model m) {
        var eventsPage = events.listPublicUpcoming(
                PageRequest.of(page, 10, Sort.by("startsAt").ascending()));
        var me = currentUser.get();
        var vol = volunteers.findByUserId(me.getId()).orElse(null);
        var visibleEventIds = eventsPage.getContent().stream()
                .map(Event::getId)
                .filter(Objects::nonNull)
                .toList();
        Set<Long> appliedIds = Collections.emptySet();
        if (vol != null) {
            appliedIds = signupRepository.findAppliedEventIdsByUserIdAndEventIds(vol.getId(), visibleEventIds);
        }
        m.addAttribute("page", eventsPage);
        m.addAttribute("appliedEventIds", appliedIds != null ? appliedIds : Collections.emptySet());
        return "pages/volunteer/events";
    }

    @PostMapping("/{eventId}/apply")
    public String apply(@PathVariable Long eventId, RedirectAttributes ra) {
        try {
            signups.signup(eventId, currentUser.get(), EventSignup.Status.PENDING);
            ra.addFlashAttribute("success", "Your application was submitted.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/pages/volunteer/events";
    }

    @GetMapping("/my-signups")
    public String mySignups(@RequestParam(defaultValue = "0") int page, Model m) {
        User me = currentUser.get();
        m.addAttribute("page", signupRepository.findByVolunteer_Id(
                me.getId(), PageRequest.of(page, 10, Sort.by("event.startsAt").descending())));
        return "pages/volunteer/my_signups";
    }

    @PostMapping("/signups/{id}/checkin")
    public String checkIn(@PathVariable Long id, RedirectAttributes ra) {
        try {
            signups.checkIn(id, currentUser.get());
            ra.addFlashAttribute("success", "Checked in successfully.");
        } catch (Exception ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/pages/volunteer/my-signups";
    }
}
