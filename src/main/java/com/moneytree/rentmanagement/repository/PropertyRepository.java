package com.moneytree.rentmanagement.repository;

import com.moneytree.rentmanagement.model.Property;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertyRepository extends JpaRepository<Property, Long> {
}
