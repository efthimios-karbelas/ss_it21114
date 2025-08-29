package com.hua.ss_it21114.api;

import com.hua.ss_it21114.api.dto.EventDto;
import com.hua.ss_it21114.api.dto.SignupDto;
import com.hua.ss_it21114.event.Event;
import com.hua.ss_it21114.event.EventSignup;

public class ApiMappers {
    public static EventDto toDto(Event e) {
        var org = e.getOrganization();
        return new EventDto(
                e.getId(),
                org != null ? org.getId() : null,
                org != null ? org.getFullName() : null,
                e.getTitle(),
                e.getDescription(),
                e.getStartsAt(),
                e.getEndsAt(),
                e.getPublished()
        );
    }

    public static SignupDto toDto(EventSignup s) {
        return new SignupDto(
                s.getId(),
                s.getEvent().getId(),
                s.getEvent().getTitle(),
                s.getVolunteer().getId(),
                s.getVolunteer().getFullName(),
                s.getStatus().name(),
                s.getCheckedIn()
        );
    }
}
