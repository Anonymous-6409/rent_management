package com.moneytree.rentmanagement.service;

import com.moneytree.rentmanagement.model.Tenant;
import com.moneytree.rentmanagement.repository.TenantRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TenantService {

    private final TenantRepository tenantRepository;

    public TenantService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    public List<Tenant> findAll() {
        return tenantRepository.findAll();
    }

    public List<Tenant> findByOwner(Long ownerId) {
        return tenantRepository.findByPropertyOwnerId(ownerId);
    }

    public long countByOwner(Long ownerId) {
        return tenantRepository.countByPropertyOwnerId(ownerId);
    }

    public Tenant findById(Long id) {
        return tenantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid tenant id: " + id));
    }

    public Tenant save(Tenant tenant) {
        return tenantRepository.save(tenant);
    }

    public void deleteById(Long id) {
        tenantRepository.deleteById(id);
    }

    public long count() {
        return tenantRepository.count();
    }
}
