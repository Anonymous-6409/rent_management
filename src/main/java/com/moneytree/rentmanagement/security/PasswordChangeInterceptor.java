package com.moneytree.rentmanagement.security;

import com.moneytree.rentmanagement.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Forces a user flagged with {@code mustChangePassword} (e.g. someone who logged in with a
 * temporary password) onto the change-password page until they set a new password.
 * The change-password and logout endpoints are excluded so the user can actually complete it.
 *
 * <p>The {@link CurrentUserService} is resolved lazily via {@link ObjectProvider} so this
 * interceptor remains loadable in MVC test slices (where that service bean is absent); when it
 * is unavailable the interceptor simply does nothing.
 */
@Component
public class PasswordChangeInterceptor implements HandlerInterceptor {

    private final ObjectProvider<CurrentUserService> currentUserServiceProvider;

    public PasswordChangeInterceptor(ObjectProvider<CurrentUserService> currentUserServiceProvider) {
        this.currentUserServiceProvider = currentUserServiceProvider;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String path = request.getRequestURI();
        String context = request.getContextPath();
        if (context != null && !context.isEmpty() && path.startsWith(context)) {
            path = path.substring(context.length());
        }

        // Always allow the password page itself and logout so the user can resolve the prompt.
        if (path.startsWith("/account/password") || path.startsWith("/logout")) {
            return true;
        }

        CurrentUserService currentUserService = currentUserServiceProvider.getIfAvailable();
        if (currentUserService == null) {
            return true;
        }

        User user = currentUserService.currentUser().orElse(null);
        if (user != null && user.isMustChangePassword()) {
            response.sendRedirect(request.getContextPath() + "/account/password?mustChange");
            return false;
        }
        return true;
    }
}
