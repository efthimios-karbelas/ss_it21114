package com.hua.ss_it21114.security;

import com.hua.ss_it21114.user.User;
import com.hua.ss_it21114.user.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserResolver {
    private final UserRepository users;

    public CurrentUserResolver(UserRepository users) {
        this.users = users;
    }

    public User get() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) throw new IllegalStateException("No authentication present");
        Object p = auth.getPrincipal();
        String username;
        if (p instanceof User) {
            return (User) p;
        } else if (p instanceof UserDetails ud) {
            username = ud.getUsername();
        } else if (p instanceof String s) {
            username = s;
        } else {
            throw new IllegalStateException("Unsupported principal: " + p);
        }
        return users.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found: " + username));
    }
}
