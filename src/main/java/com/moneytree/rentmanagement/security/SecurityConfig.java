package com.moneytree.rentmanagement.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/register", "/css/**", "/js/**", "/images/**", "/webjars/**")
                        .permitAll()
                        // Owner management stays admin-only.
                        .requestMatchers("/owners/**").hasRole("ADMIN")
                        // Data mutations: admins and owners (record-level ownership enforced in controllers).
                        .requestMatchers("/properties/new", "/properties/edit/**", "/properties/save",
                                "/properties/delete/**").hasAnyRole("ADMIN", "OWNER")
                        .requestMatchers("/tenants/new", "/tenants/edit/**", "/tenants/save",
                                "/tenants/delete/**").hasAnyRole("ADMIN", "OWNER")
                        .requestMatchers("/payments/new", "/payments/edit/**", "/payments/save",
                                "/payments/delete/**").hasAnyRole("ADMIN", "OWNER")
                        .requestMatchers("/reminders/generate", "/reminders/send",
                                "/reminders/delete/**").hasAnyRole("ADMIN", "OWNER")
                        // Everything else (lists, dashboard, change-password): any authenticated user.
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll());
        return http.build();
    }
}
