package com.moneytree.rentmanagement.repository;

import com.moneytree.rentmanagement.model.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantRepository extends JpaRepository<Tenant, Long> {
}
