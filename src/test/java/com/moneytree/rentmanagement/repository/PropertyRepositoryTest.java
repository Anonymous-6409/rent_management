package com.moneytree.rentmanagement.repository;

import com.moneytree.rentmanagement.model.Property;
import com.moneytree.rentmanagement.model.PropertyStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PropertyRepositoryTest {

    @Autowired
    private PropertyRepository propertyRepository;

    private Property property(String name) {
        Property p = new Property();
        p.setName(name);
        p.setAddress("1 Test Ave");
        p.setType("Apartment");
        p.setMonthlyRent(new BigDecimal("1000.00"));
        p.setStatus(PropertyStatus.VACANT);
        return p;
    }

    @Test
    void saveAndFindById_roundTrips() {
        Property saved = propertyRepository.save(property("Sunset Villa"));

        assertThat(saved.getId()).isNotNull();
        assertThat(propertyRepository.findById(saved.getId()))
                .isPresent()
                .get()
                .extracting(Property::getName)
                .isEqualTo("Sunset Villa");
    }

    @Test
    void findAll_returnsAllSavedProperties() {
        propertyRepository.save(property("A"));
        propertyRepository.save(property("B"));

        assertThat(propertyRepository.findAll()).hasSize(2);
    }

    @Test
    void deleteById_removesProperty() {
        Property saved = propertyRepository.save(property("ToDelete"));

        propertyRepository.deleteById(saved.getId());

        assertThat(propertyRepository.findById(saved.getId())).isEmpty();
    }
}
