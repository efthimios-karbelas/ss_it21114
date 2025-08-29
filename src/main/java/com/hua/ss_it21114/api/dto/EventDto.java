package com.hua.ss_it21114.api.dto;

import java.time.LocalDateTime;

public record EventDto(
        Long id,
        Long organizationId,
        String organizationName,
        String title,
        String description,
        LocalDateTime startsAt,
        LocalDateTime endsAt,
        Boolean published
) {
}