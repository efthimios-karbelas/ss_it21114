package com.hua.ss_it21114.event;

import com.hua.ss_it21114.user.OrganizationRepository;
import com.hua.ss_it21114.user.User;
import com.hua.ss_it21114.user.Volunteer;
import com.hua.ss_it21114.user.VolunteerRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class EventSignupService {
    private final EventRepository events;
    private final EventSignupRepository signups;
    private final VolunteerRepository volunteers;
    private final OrganizationRepository organizations;

    public EventSignupService(
            EventRepository events,
            EventSignupRepository signups,
            VolunteerRepository volunteers,
            OrganizationRepository organizations
    ) {
        this.events = events;
        this.signups = signups;
        this.volunteers = volunteers;
        this.organizations = organizations;
    }

    @Transactional
    public EventSignup signup(Long eventId, User volunteer, EventSignup.Status status) {
        Event event = events.findById(eventId).orElseThrow();
        if (Boolean.FALSE.equals(event.getPublished())) {
            throw new IllegalStateException("This event is not published yet.");
        }
        signups.findByEvent_IdAndVolunteer_Id(eventId, volunteer.getId())
                .ifPresent(s -> {
                    throw new IllegalStateException("Already signed up!");
                });
        EventSignup s = new EventSignup();
        s.setEvent(event);
        s.setVolunteer(volunteer);
        s.setStatus(status);
        return signups.save(s);
    }

    @Transactional
    public void approve(Long signupId, User org) {
        EventSignup s = signups.findById(signupId).orElseThrow();
        if (!s.getEvent().getOrganization().getId().equals(org.getId()))
            throw new AccessDeniedException("This event doesnt belong in this organization!");
        s.setStatus(EventSignup.Status.APPROVED);
    }

    @Transactional
    public void reject(Long signupId, User org) {
        EventSignup s = signups.findById(signupId).orElseThrow();
        if (!s.getEvent().getOrganization().getId().equals(org.getId()))
            throw new AccessDeniedException("This event doesnt belong in this organization!");
        s.setStatus(EventSignup.Status.REJECTED);
    }

    @Transactional
    public void checkIn(Long signupId, User volunteer) {
        EventSignup s = signups.findById(signupId).orElseThrow();
        if (!s.getVolunteer().getId().equals(volunteer.getId()))
            throw new AccessDeniedException("Not your event!");
        if (!s.canCheckIn())
            throw new IllegalStateException("Check-in not available!");
        s.setCheckedIn(true);
        s.setCheckedInAt(LocalDateTime.now());
    }

    @Transactional
    public void cancelByVolunteer(Long signupId, User volunteerUser) {
        Volunteer vol = volunteers.findByUserId(volunteerUser.getId())
                .orElseThrow(() -> new IllegalStateException("Volunteer profile not found"));

        EventSignup s = signups.findByIdAndVolunteer_Id(signupId, vol.getId())
                .orElseThrow(() -> new IllegalArgumentException("Signup not found"));
        if (Boolean.TRUE.equals(s.getCheckedIn())) {
            throw new IllegalStateException("Cannot cancel after check-in");
        }
        signups.delete(s);
    }

}
