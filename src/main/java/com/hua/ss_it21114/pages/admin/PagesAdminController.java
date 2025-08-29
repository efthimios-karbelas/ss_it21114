package com.hua.ss_it21114.pages.admin;

import com.hua.ss_it21114.auth.AuthService;
import com.hua.ss_it21114.event.EventService;
import com.hua.ss_it21114.user.*;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/pages/admin")
@Validated
public class PagesAdminController {

    private final AuthService authService;
    private final OrganizationRepository organizations;
    private final VolunteerRepository volunteers;
    private final UserRepository users;
    private final EventService events;

    public PagesAdminController(
            AuthService authService,
            OrganizationRepository organizations,
            VolunteerRepository volunteers,
            UserRepository users,
            EventService events
    ) {
        this.authService = authService;
        this.organizations = organizations;
        this.volunteers = volunteers;
        this.users = users;
        this.events = events;
    }

    @GetMapping("/pending")
    public String pending(Model model) {
        model.addAttribute("pendingOrgs", organizations.findByApprovedFalse());
        model.addAttribute("pendingVols", volunteers.findByApprovedFalse());
        model.addAttribute("pendingEvents", events.listPendingForAdmin());
        return "pages/admin/pending";
    }

    @PostMapping("/organizations/{id}/approve")
    @Transactional
    public String approveOrg(@PathVariable Long id, @RequestParam("approve") boolean approve) {
        Organization org = organizations.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Organization not found"));
        org.setApproved(approve);
        organizations.save(org);
        return "redirect:/pages/admin/pending";
    }

    @PostMapping("/organizations/{id}/delete")
    @Transactional
    public String deleteOrg(@PathVariable Long id) {
        Organization org = organizations.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Organization not found"));
        organizations.delete(org);
        return "redirect:/pages/admin/pending";
    }

    @GetMapping("/organizations/{id}/edit")
    public String editOrgForm(@PathVariable Long id, Model model) {
        Organization org = organizations.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Organization not found"));
        User u = org.getUser();
        OrgEditForm form = new OrgEditForm(
                org.getId(),
                org.getOrganizationName(),
                u != null ? u.getId() : null,
                u != null ? u.getUsername() : "",
                u != null ? u.getFullName() : ""
        );
        model.addAttribute("form", form);
        return "pages/admin/edit_org";
    }

    @PostMapping("/organizations/{id}/edit")
    @Transactional
    public String editOrgSubmit(@PathVariable Long id, @ModelAttribute @Validated OrgEditForm form) {
        Organization org = organizations.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Organization not found"));
        org.setOrganizationName(form.organizationName());
        organizations.save(org);

        if (form.userId() != null) {
            User u = users.findById(form.userId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Linked user not found"));
            if (form.username() != null && !form.username().isBlank() && !form.username().equals(u.getUsername())) {
                if (users.existsByUsername(form.username()))
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
                u.setUsername(form.username());
            }
            u.setFullName(form.fullName() == null ? "" : form.fullName());
            users.save(u);
        }
        return "redirect:/pages/admin/pending";
    }

    @PostMapping("/volunteers/{id}/approve")
    @Transactional
    public String approveVol(@PathVariable Long id, @RequestParam("approve") boolean approve) {
        Volunteer vol = volunteers.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Volunteer not found"));
        vol.setApproved(approve);
        volunteers.save(vol);
        return "redirect:/pages/admin/pending";
    }

    @PostMapping("/volunteers/{id}/delete")
    @Transactional
    public String deleteVol(@PathVariable Long id) {
        Volunteer vol = volunteers.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Volunteer not found"));
        volunteers.delete(vol);
        return "redirect:/pages/admin/pending";
    }

    @GetMapping("/volunteers/{id}/edit")
    public String editVolForm(@PathVariable Long id, Model model) {
        Volunteer vol = volunteers.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Volunteer not found"));
        User u = vol.getUser();
        VolEditForm form = new VolEditForm(
                vol.getId(),
                u != null ? u.getId() : null,
                u != null ? u.getUsername() : "",
                u != null ? u.getFullName() : ""
        );
        model.addAttribute("form", form);
        return "pages/admin/edit_vol";
    }

    @PostMapping("/volunteers/{id}/edit")
    @Transactional
    public String editVolSubmit(@PathVariable Long id, @ModelAttribute @Validated VolEditForm form) {
        Volunteer vol = volunteers.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Volunteer not found"));

        if (form.userId() != null) {
            User u = users.findById(form.userId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Linked user not found"));
            if (form.username() != null && !form.username().isBlank() && !form.username().equals(u.getUsername())) {
                if (users.existsByUsername(form.username()))
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
                u.setUsername(form.username());
            }
            u.setFullName(form.fullName() == null ? "" : form.fullName());
            users.save(u);
        }
        volunteers.save(vol);
        return "redirect:/pages/admin/pending";
    }

    @GetMapping("/volunteers/new")
    public String newVolunteer(Model model) {
        model.addAttribute("form", new VolCreateForm("", "", ""));
        return "pages/admin/create_vol";
    }

    @PostMapping("/volunteers/create")
    @Transactional
    public String createVolunteer(@ModelAttribute VolCreateForm form) {
        var user = authService.registerVolunteer(form.username(), form.password(), form.fullName());
        var vol = volunteers.findByUserId(user.getId()).orElseThrow();
        vol.setApproved(false);
        volunteers.save(vol);
        return "redirect:/pages/admin/pending";
    }

    @GetMapping("/organizations/new")
    public String newOrganization(Model model) {
        model.addAttribute("form", new OrgCreateForm("", "", "", ""));
        return "pages/admin/create_org";
    }

    @PostMapping("/organizations/create")
    @Transactional
    public String createOrganization(@ModelAttribute OrgCreateForm form) {
        var user = authService.registerOrganization(
                form.username(), form.password(), form.fullName(), form.organizationName());
        var org = organizations.findByUserId(user.getId()).orElseThrow();
        org.setApproved(false);
        organizations.save(org);
        return "redirect:/pages/admin/pending";
    }

    @PostMapping("/events/{id}/publish")
    @Transactional
    public String publishEvent(@PathVariable Long id) {
        events.publishByAdmin(id);
        return "redirect:/pages/admin/pending";
    }

    @PostMapping("/events/{id}/delete")
    @Transactional
    public String deleteEvent(@PathVariable Long id) {
        events.deleteUnpublishedByAdmin(id);
        return "redirect:/pages/admin/pending";
    }

    public record OrgCreateForm(
            @NotBlank String organizationName,
            @NotBlank String username,
            @NotBlank String password,
            String fullName
    ) {
    }

    public record OrgEditForm(
            Long id,
            @NotBlank String organizationName,
            Long userId,
            @NotBlank String username,
            String fullName
    ) {
    }

    public record VolCreateForm(
            @NotBlank String username,
            @NotBlank String password,
            String fullName
    ) {
    }

    public record VolEditForm(
            Long id,
            Long userId,
            @NotBlank String username,
            String fullName
    ) {
    }


}
