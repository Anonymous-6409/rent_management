package com.moneytree.rentmanagement.repository;

import com.moneytree.rentmanagement.model.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TenantRepository extends JpaRepository<Tenant, Long> {

    List<Tenant> findByPropertyOwnerId(Long ownerId);

    long countByPropertyOwnerId(Long ownerId);
}
