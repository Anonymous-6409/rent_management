package com.moneytree.rentmanagement.security;

import com.moneytree.rentmanagement.model.Owner;
import com.moneytree.rentmanagement.model.User;
import com.moneytree.rentmanagement.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Resolves the currently authenticated {@link User} and helps controllers scope data:
 * an ADMIN sees everything; a user linked to an {@link Owner} sees only their own data.
 */
@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return Optional.empty();
        }
        String name = auth.getName();
        if (name == null || "anonymousUser".equals(name)) {
            return Optional.empty();
        }
        return userRepository.findByUsername(name);
    }

    public boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    /**
     * The owner id whose data the current user may see, if any.
     * Empty for admins (who see all) and for non-owner users (who see nothing).
     */
    public Optional<Long> currentOwnerId() {
        if (isAdmin()) {
            return Optional.empty();
        }
        return currentUser()
                .map(User::getOwner)
                .map(Owner::getId);
    }

    /** The Owner entity linked to the current (non-admin) user, if any. */
    public Optional<Owner> currentOwner() {
        if (isAdmin()) {
            return Optional.empty();
        }
        return currentUser().map(User::getOwner);
    }

    /** True if the current user is allowed to act on data belonging to the given owner. */
    public boolean ownsOwner(Owner owner) {
        if (owner == null) {
            return false;
        }
        return currentOwnerId().map(id -> id.equals(owner.getId())).orElse(false);
    }
}

