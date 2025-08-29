package com.hua.ss_it21114.event;

import com.hua.ss_it21114.user.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "event_signups", uniqueConstraints = @UniqueConstraint(columnNames = {"event_id", "volunteer_id"}))
public class EventSignup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Event event;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private User volunteer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    private Boolean checkedIn = false;

    private LocalDateTime checkedInAt;

    public Long getId() {
        return id;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public User getVolunteer() {
        return volunteer;
    }

    public void setVolunteer(User volunteer) {
        this.volunteer = volunteer;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Boolean getCheckedIn() {
        return checkedIn;
    }

    public void setCheckedIn(Boolean checkedIn) {
        this.checkedIn = checkedIn;
    }

    public LocalDateTime getCheckedInAt() {
        return checkedInAt;
    }

    public void setCheckedInAt(LocalDateTime checkedInAt) {
        this.checkedInAt = checkedInAt;
    }

    @Transient
    public boolean canCheckIn() {
        LocalDateTime now = LocalDateTime.now();
        System.out.println(status);
        System.out.println(checkedIn);
        System.out.println(checkedIn);
        System.out.println(now.isAfter(event.getStartsAt().minusDays(5)));
        System.out.println(now.isBefore(event.getEndsAt().plusDays(1)));
        return status == Status.APPROVED
                && Boolean.FALSE.equals(checkedIn)
                && (now.isAfter(event.getStartsAt().minusDays(5)) || now.isBefore(event.getEndsAt().plusDays(1)));
    }

    public enum Status {PENDING, APPROVED, REJECTED}
}
