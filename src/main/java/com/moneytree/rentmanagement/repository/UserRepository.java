package com.moneytree.rentmanagement.repository;

import com.moneytree.rentmanagement.model.Role;
import com.moneytree.rentmanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    Optional<User> findByOwnerId(Long ownerId);

    List<User> findByRoleAndOwnerIsNull(Role role);
}
