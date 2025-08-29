package com.hua.ss_it21114.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    @Order(1)
    SecurityFilterChain apiChain(HttpSecurity http, JwtAuthFilter jwtFilter) throws Exception {
        http.securityMatcher("/api/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(reg -> reg
                        .requestMatchers("/api/user/login", "/api/user/register/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/user/**").authenticated()
                        .requestMatchers("/api/event/**").authenticated()
                        .requestMatchers("/api/eventsignup/**").authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    AuthenticationSuccessHandler roleBasedSuccessHandler() {
        return new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication auth) throws IOException, ServletException {
                boolean isAdmin = hasRole(auth, "ROLE_ADMIN");
                boolean isVolunteer = hasRole(auth, "ROLE_VOLUNTEER");
                boolean isOrganization = hasRole(auth, "ROLE_ORGANIZATION");

                if (isAdmin) {
                    response.sendRedirect(request.getContextPath() + "/pages/admin");
                } else if (isVolunteer) {
                    response.sendRedirect(request.getContextPath() + "/pages/volunteer");
                } else if (isOrganization) {
                    response.sendRedirect(request.getContextPath() + "/pages/organization");
                } else {
                    response.sendRedirect(request.getContextPath() + "/pages/login?error");
                }
            }

            private boolean hasRole(Authentication auth, String role) {
                for (GrantedAuthority ga : auth.getAuthorities()) {
                    if (ga.getAuthority().equals(role)) return true;
                }
                return false;
            }
        };
    }

    @Bean
    @Order(2)
    SecurityFilterChain pagesChain(
            HttpSecurity http,
            AuthenticationSuccessHandler roleBasedSuccessHandler,
            AppAuthFailureHandler failureHandler
    ) throws Exception {
        http.securityMatcher("/", "/pages/**", "/css/**", "/js/**", "/images/**")
                .authorizeHttpRequests(reg -> reg
                        .requestMatchers("/", "/pages/login").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/pages/admin/**").hasRole("ADMIN")
                        .requestMatchers("/pages/volunteer/**").hasRole("VOLUNTEER")
                        .requestMatchers("/pages/organization/**").hasRole("ORGANIZATION")
                        .requestMatchers("/pages/admin", "/pages/volunteer", "/pages/organization").authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(f -> f
                        .loginPage("/pages/login")
                        .loginProcessingUrl("/pages/login")
                        .successHandler(roleBasedSuccessHandler)
                        .failureHandler(failureHandler)
                        .permitAll()
                )
                .logout(l -> l
                        .logoutUrl("/pages/logout")
                        .logoutSuccessUrl("/pages/login?logout")
                );
        return http.build();
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            AppAuthFailureHandler failureHandler
    ) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/login").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/pages/login")
                        .failureHandler(failureHandler)
                        .permitAll()
                )
                .logout(l -> l.logoutUrl("/logout").permitAll());

        return http.build();
    }
}
