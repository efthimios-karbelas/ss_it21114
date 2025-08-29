package com.hua.ss_it21114.pages;


import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RootController {

    @GetMapping("/")
    public String root(Authentication auth) {
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            if (hasRole(auth, "ROLE_ADMIN")) return "redirect:/pages/admin";
            if (hasRole(auth, "ROLE_VOLUNTEER")) return "redirect:/pages/volunteer";
            if (hasRole(auth, "ROLE_ORGANIZATION")) return "redirect:/pages/organization";
        }
        return "redirect:/pages/login";
    }

    private boolean hasRole(Authentication auth, String role) {
        for (GrantedAuthority ga : auth.getAuthorities()) {
            if (ga.getAuthority().equals(role)) return true;
        }
        return false;
    }
}