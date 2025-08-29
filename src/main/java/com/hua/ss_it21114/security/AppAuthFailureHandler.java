package com.hua.ss_it21114.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AppAuthFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {
        String msg;
        if (exception instanceof DisabledException) {
            msg = "Your account is awaiting approval by an administrator.";
        } else {
            msg = "Invalid username or password.";
        }
        request.getSession().setAttribute("LOGIN_ERROR_MSG", msg);
        response.sendRedirect("/pages/login?error");
    }
}
