package com.moneytree.rentmanagement.repository;

import com.moneytree.rentmanagement.model.Owner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OwnerRepository extends JpaRepository<Owner, Long> {
}
