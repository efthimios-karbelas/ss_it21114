package com.hua.ss_it21114.auth;

import jakarta.validation.constraints.NotBlank;

public record RegisterVolunteerReq(
        @NotBlank String username,
        @NotBlank String password,
        @NotBlank String fullName
) {
}
