package com.hua.ss_it21114.pages.organization;

import com.hua.ss_it21114.event.*;
import com.hua.ss_it21114.security.CurrentUserResolver;
import com.hua.ss_it21114.user.User;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/pages/organization")
@PreAuthorize("hasRole('ORGANIZATION')")
public class OrganizationController {
    private final EventService events;
    private final EventSignupRepository signups;
    private final EventSignupService signupService;
    private final CurrentUserResolver currentUser;

    public OrganizationController(
            EventService events,
            EventSignupRepository signups,
            EventSignupService signupService,
            CurrentUserResolver currentUser
    ) {
        this.events = events;
        this.signups = signups;
        this.signupService = signupService;
        this.currentUser = currentUser;
    }

    @GetMapping("/events")
    public String myEvents(@RequestParam(defaultValue = "0") int page, Model m) {
        User me = currentUser.get();
        m.addAttribute("page", events.listMine(me,
                PageRequest.of(page, 10, Sort.by("startsAt").descending())));
        return "pages/organization/events";
    }

    @GetMapping("/events/new")
    public String newEvent(Model m) {
        m.addAttribute("form", new Event());
        return "pages/organization/event_form";
    }

    @PostMapping("/events")
    public String create(
            @ModelAttribute("form") @Valid Event form,
            BindingResult br,
            RedirectAttributes ra
    ) {
        if (br.hasErrors()) {
            return "pages/organization/event_form";
        }
        form.setPublished(false);
        events.createForOrg(currentUser.get(), form);
        ra.addFlashAttribute("success", "Event submitted for approval.");
        return "redirect:/pages/organization/events";
    }

    @GetMapping("/pending")
    public String pending(@RequestParam(defaultValue = "0") int page, Model m) {
        User me = currentUser.get();
        m.addAttribute("page",
                signups.findByEvent_Organization_IdAndStatus(
                        me.getId(), EventSignup.Status.PENDING,
                        PageRequest.of(page, 10, Sort.by("event_title").descending())));
        return "pages/organization/pending_signups";
    }

    @PostMapping("/signups/{id}/approve")
    public String approve(@PathVariable Long id, RedirectAttributes ra) {
        signupService.approve(id, currentUser.get());
        ra.addFlashAttribute("success", "Signup approved.");
        return "redirect:/pages/organization/pending";
    }

    @PostMapping("/signups/{id}/reject")
    public String reject(@PathVariable Long id, RedirectAttributes ra) {
        signupService.reject(id, currentUser.get());
        ra.addFlashAttribute("success", "Signup rejected.");
        return "redirect:/pages/organization/pending";
    }
}
