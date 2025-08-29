package com.hua.ss_it21114.auth;

import jakarta.validation.constraints.NotBlank;

public record LoginReq(@NotBlank String username, @NotBlank String password) {
}
