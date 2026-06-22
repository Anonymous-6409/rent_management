package com.moneytree.rentmanagement.service;

import com.moneytree.rentmanagement.model.Owner;
import com.moneytree.rentmanagement.model.Role;
import com.moneytree.rentmanagement.model.User;
import com.moneytree.rentmanagement.repository.UserRepository;
import com.moneytree.rentmanagement.web.RegistrationForm;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private RegistrationForm form() {
        RegistrationForm f = new RegistrationForm();
        f.setUsername("jane");
        f.setFullName("Jane Doe");
        f.setPassword("secret123");
        f.setConfirmPassword("secret123");
        return f;
    }

    @Test
    void register_hashesPasswordAndSavesWithUserRole() {
        when(userRepository.existsByUsername("jane")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("hashed-pw");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = userService.register(form());

        assertThat(saved.getUsername()).isEqualTo("jane");
        assertThat(saved.getFullName()).isEqualTo("Jane Doe");
        assertThat(saved.getPassword()).isEqualTo("hashed-pw");
        assertThat(saved.getRole()).isEqualTo(Role.USER);
        assertThat(saved.isEnabled()).isTrue();
        verify(passwordEncoder).encode("secret123");
    }

    @Test
    void register_duplicateUsername_throwsAndDoesNotSave() {
        when(userRepository.existsByUsername("jane")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(form()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("jane");

        verify(userRepository, never()).save(any());
    }

    @Test
    void usernameExists_delegatesToRepository() {
        when(userRepository.existsByUsername("admin")).thenReturn(true);

        assertThat(userService.usernameExists("admin")).isTrue();
    }

    private Owner owner() {
        Owner o = new Owner();
        o.setId(7L);
        o.setName("Olivia Owner");
        o.setEmail("olivia@example.com");
        return o;
    }

    @Test
    void createOwnerAccount_createsOwnerRoleLoginAndReturnsTempPassword() {
        Owner owner = owner();
        when(userRepository.existsByUsername("olivia@example.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed-temp");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        String tempPassword = userService.createOwnerAccount(owner);

        assertThat(tempPassword).isNotBlank();

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getUsername()).isEqualTo("olivia@example.com");
        assertThat(saved.getRole()).isEqualTo(Role.OWNER);
        assertThat(saved.getOwner()).isSameAs(owner);
        assertThat(saved.getPassword()).isEqualTo("hashed-temp");
        // the returned password is the raw one, not the stored hash
        assertThat(tempPassword).isNotEqualTo("hashed-temp");
    }

    @Test
    void createOwnerAccount_existingUsername_throws() {
        Owner owner = owner();
        when(userRepository.existsByUsername("olivia@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createOwnerAccount(owner))
                .isInstanceOf(IllegalArgumentException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_correctCurrent_updatesHash() {
        User user = new User();
        user.setUsername("jane");
        user.setPassword("old-hash");
        when(userRepository.findByUsername("jane")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldpw", "old-hash")).thenReturn(true);
        when(passwordEncoder.encode("newpw123")).thenReturn("new-hash");

        userService.changePassword("jane", "oldpw", "newpw123");

        assertThat(user.getPassword()).isEqualTo("new-hash");
        verify(userRepository).save(user);
    }

    @Test
    void changePassword_wrongCurrent_throwsAndDoesNotSave() {
        User user = new User();
        user.setUsername("jane");
        user.setPassword("old-hash");
        when(userRepository.findByUsername("jane")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "old-hash")).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword("jane", "wrong", "newpw123"))
                .isInstanceOf(IllegalArgumentException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void linkUserToOwner_promotesUserToOwnerRoleAndLinks() {
        Owner owner = owner();
        User existing = new User();
        existing.setId(3L);
        existing.setUsername("jane");
        existing.setRole(Role.USER);
        when(userRepository.findById(3L)).thenReturn(Optional.of(existing));

        userService.linkUserToOwner(3L, owner);

        assertThat(existing.getRole()).isEqualTo(Role.OWNER);
        assertThat(existing.getOwner()).isSameAs(owner);
        verify(userRepository).save(existing);
    }
}
