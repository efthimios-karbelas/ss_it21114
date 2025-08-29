package com.hua.ss_it21114.api;

import com.hua.ss_it21114.api.dto.ApiMessage;
import com.hua.ss_it21114.api.dto.EventDto;
import com.hua.ss_it21114.event.EventService;
import com.hua.ss_it21114.user.Organization;
import com.hua.ss_it21114.user.OrganizationRepository;
import com.hua.ss_it21114.user.Volunteer;
import com.hua.ss_it21114.user.VolunteerRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminApiController {
    private final OrganizationRepository orgs;
    private final VolunteerRepository vols;
    private final EventService events;

    public AdminApiController(OrganizationRepository orgs,
                              VolunteerRepository vols,
                              EventService events) {
        this.orgs = orgs;
        this.vols = vols;
        this.events = events;
    }

    @GetMapping("/pending/organizations")
    public List<Organization> pendingOrgs() {
        return orgs.findTop100ByApprovedFalseOrderByIdAsc();
    }

    @PostMapping("/organizations/{id}/approve")
    public ApiMessage approveOrg(@PathVariable Long id) {
        var o = orgs.findById(id).orElseThrow();
        o.setApproved(true);
        orgs.save(o);
        return new ApiMessage("Organization approved");
    }

    @GetMapping("/pending/volunteers")
    public List<Volunteer> pendingVols() {
        return vols.findTop100ByApprovedFalseOrderByIdAsc();
    }

    @PostMapping("/volunteers/{id}/approve")
    public ApiMessage approveVol(@PathVariable Long id) {
        var v = vols.findById(id).orElseThrow();
        v.setApproved(true);
        vols.save(v);
        return new ApiMessage("Volunteer approved");
    }

    @GetMapping("/pending/events")
    public List<EventDto> pendingEvents() {
        return events.listPendingForAdmin().stream().map(ApiMappers::toDto).toList();
    }

    @PostMapping("/events/{id}/publish")
    public ApiMessage publish(@PathVariable Long id) {
        events.publishByAdmin(id);
        return new ApiMessage("Published");
    }

    @DeleteMapping("/events/{id}")
    public ApiMessage deleteUnpublished(@PathVariable Long id) {
        events.deleteUnpublishedByAdmin(id);
        return new ApiMessage("Deleted");
    }
}
