package com.moneytree.rentmanagement.config;

import com.moneytree.rentmanagement.security.PasswordChangeInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Registers MVC interceptors, including the one that forces a password change on first login.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final PasswordChangeInterceptor passwordChangeInterceptor;

    public WebConfig(PasswordChangeInterceptor passwordChangeInterceptor) {
        this.passwordChangeInterceptor = passwordChangeInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(passwordChangeInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/login", "/register", "/logout",
                        "/css/**", "/js/**", "/images/**", "/webjars/**", "/error");
    }
}
