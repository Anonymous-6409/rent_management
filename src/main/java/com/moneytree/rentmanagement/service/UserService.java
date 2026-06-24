package com.moneytree.rentmanagement.service;

import com.moneytree.rentmanagement.model.Owner;
import com.moneytree.rentmanagement.model.Role;
import com.moneytree.rentmanagement.model.User;
import com.moneytree.rentmanagement.repository.UserRepository;
import com.moneytree.rentmanagement.web.RegistrationForm;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final SecureRandom RANDOM = new SecureRandom();

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Change a user's password after verifying the current one.
     * @throws IllegalArgumentException if the user is unknown or the current password is wrong.
     */
    public void changePassword(String username, String currentPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Unknown user: " + username));
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(false);
        userRepository.save(user);
    }

    /** Register a new self-service user with the USER role and a BCrypt-hashed password. */
    public User register(RegistrationForm form) {
        if (usernameExists(form.getUsername())) {
            throw new IllegalArgumentException("Username already taken: " + form.getUsername());
        }
        User user = new User();
        user.setUsername(form.getUsername().trim());
        user.setFullName(form.getFullName().trim());
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        user.setRole(Role.USER);
        user.setEnabled(true);
        return userRepository.save(user);
    }

    /**
     * Create a login account for an owner (username = the owner's email) and return the
     * generated temporary password (shown once to the admin). Throws if the username is taken.
     */
    public String createOwnerAccount(Owner owner) {
        String username = owner.getEmail();
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Owner has no email to use as a username");
        }
        if (usernameExists(username)) {
            throw new IllegalArgumentException("A login already exists for " + username);
        }
        String tempPassword = generateTempPassword();
        User user = new User();
        user.setUsername(username);
        user.setFullName(owner.getName());
        user.setPassword(passwordEncoder.encode(tempPassword));
        user.setRole(Role.OWNER);
        user.setOwner(owner);
        user.setEnabled(true);
        // Force a password change on first login since this is a temporary password.
        user.setMustChangePassword(true);
        userRepository.save(user);
        return tempPassword;
    }

    /** Promote an existing (self-registered) user to OWNER and link it to the given owner. */
    public void linkUserToOwner(Long userId, Owner owner) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user id: " + userId));
        user.setOwner(owner);
        user.setRole(Role.OWNER);
        userRepository.save(user);
    }

    /** The login account currently linked to an owner, if any. */
    public Optional<User> findOwnerAccount(Long ownerId) {
        return userRepository.findByOwnerId(ownerId);
    }

    /** Self-registered users not yet linked to any owner — candidates for linking. */
    public List<User> linkableUsers() {
        return userRepository.findByRoleAndOwnerIsNull(Role.USER);
    }

    private String generateTempPassword() {
        byte[] bytes = new byte[9];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
