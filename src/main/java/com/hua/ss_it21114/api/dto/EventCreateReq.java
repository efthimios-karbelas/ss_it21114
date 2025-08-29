package com.hua.ss_it21114.api.dto;

import java.time.LocalDateTime;


public record EventCreateReq(
        String title,
        String description,
        LocalDateTime startsAt,
        LocalDateTime endsAt
) {
}