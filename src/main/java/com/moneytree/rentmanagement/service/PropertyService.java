package com.moneytree.rentmanagement.service;

import com.moneytree.rentmanagement.model.Property;
import com.moneytree.rentmanagement.repository.PropertyRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;

    public PropertyService(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    public List<Property> findAll() {
        return propertyRepository.findAll();
    }

    public List<Property> findByOwner(Long ownerId) {
        return propertyRepository.findByOwnerId(ownerId);
    }

    public long countByOwner(Long ownerId) {
        return propertyRepository.countByOwnerId(ownerId);
    }

    public Property findById(Long id) {
        return propertyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid property id: " + id));
    }

    public Property save(Property property) {
        return propertyRepository.save(property);
    }

    public void deleteById(Long id) {
        propertyRepository.deleteById(id);
    }

    public long count() {
        return propertyRepository.count();
    }
}
