package com.hua.ss_it21114.event;

import com.hua.ss_it21114.user.Organization;
import com.hua.ss_it21114.user.OrganizationRepository;
import com.hua.ss_it21114.user.User;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EventService {
    private final EventRepository events;
    private final OrganizationRepository organizations;

    public EventService(EventRepository events, OrganizationRepository organizations) {
        this.events = events;
        this.organizations = organizations;
    }

    public Event createForOrg(User org, Event e) {
        e.setOrganization(org);
        e.setPublished(Boolean.FALSE);
        return events.save(e);
    }

    public Page<Event> listPublicUpcoming(Pageable pageable) {
        return events.findPublishedActiveOrUpcoming(LocalDateTime.now(), pageable);
    }

    public Page<Event> listMine(User org, Pageable pageable) {
        return events.findByOrganization_Id(org.getId(), pageable);
    }

    public List<Event> listPendingForAdmin() {
        return events.findTop100ByPublishedFalseOrderByStartsAtAsc();
    }

    @Transactional
    public void publishByAdmin(Long id) {
        Event e = events.findById(id).orElseThrow();
        e.setPublished(Boolean.TRUE);
        events.save(e);
    }

    @Transactional
    public void deleteUnpublishedByAdmin(Long id) {
        Event e = events.findById(id).orElseThrow();
        if (Boolean.TRUE.equals(e.getPublished())) {
            throw new IllegalStateException("Cannot delete a published event.");
        }
        events.delete(e);
    }

    public Event create(User orgUser, String title, String description,
                        LocalDateTime startsAt, LocalDateTime endsAt) {
        Organization org = organizations.findByUserId(orgUser.getId())
                .orElseThrow(() -> new IllegalStateException("Organization not found"));
        Event e = new Event();
        e.setOrganization(org.getUser());
        e.setTitle(title);
        e.setDescription(description);
        e.setStartsAt(startsAt);
        e.setEndsAt(endsAt);
        e.setPublished(false);
        return events.save(e);
    }

    public Event updateOwned(User orgUser, Long eventId, String title,
                             String description, LocalDateTime startsAt, LocalDateTime endsAt) {
        Event e = requireOwnedByCurrentOrg(eventId, orgUser);
        e.setTitle(title);
        e.setDescription(description);
        e.setStartsAt(startsAt);
        e.setEndsAt(endsAt);
        return events.save(e);
    }

    public void deleteOwned(User orgUser, Long eventId) {
        Event e = requireOwnedByCurrentOrg(eventId, orgUser);
        events.delete(e);
    }

    public Event requireOwnedByCurrentOrg(Long eventId, User orgUser) {
        Organization org = organizations.findByUserId(orgUser.getId())
                .orElseThrow(() -> new IllegalStateException("Organization not found"));
        Event e = events.findById(eventId).orElseThrow();
        if (!e.getOrganization().getId().equals(org.getId())) {
            throw new IllegalStateException("Not your event");
        }
        return e;
    }
}
