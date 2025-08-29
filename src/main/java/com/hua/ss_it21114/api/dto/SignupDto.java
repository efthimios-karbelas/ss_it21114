package com.hua.ss_it21114.api.dto;

public record SignupDto(
        Long id,
        Long eventId,
        String eventTitle,
        Long volunteerId,
        String volunteerName,
        String status,
        Boolean checkedIn
) {
}
