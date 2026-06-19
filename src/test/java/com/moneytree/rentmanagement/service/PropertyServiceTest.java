package com.moneytree.rentmanagement.service;

import com.moneytree.rentmanagement.model.Property;
import com.moneytree.rentmanagement.model.PropertyStatus;
import com.moneytree.rentmanagement.repository.PropertyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PropertyServiceTest {

    @Mock
    private PropertyRepository propertyRepository;

    @InjectMocks
    private PropertyService propertyService;

    private Property sampleProperty(Long id) {
        Property p = new Property();
        p.setId(id);
        p.setName("Maple Court");
        p.setAddress("12 Maple St");
        p.setType("Apartment");
        p.setMonthlyRent(new BigDecimal("1200.00"));
        p.setStatus(PropertyStatus.VACANT);
        return p;
    }

    @Test
    void findAll_returnsAllProperties() {
        when(propertyRepository.findAll()).thenReturn(List.of(sampleProperty(1L), sampleProperty(2L)));

        List<Property> result = propertyService.findAll();

        assertThat(result).hasSize(2);
        verify(propertyRepository).findAll();
    }

    @Test
    void findById_existingId_returnsProperty() {
        when(propertyRepository.findById(1L)).thenReturn(Optional.of(sampleProperty(1L)));

        Property result = propertyService.findById(1L);

        assertThat(result.getName()).isEqualTo("Maple Court");
    }

    @Test
    void findById_missingId_throwsException() {
        when(propertyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> propertyService.findById(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }

    @Test
    void save_delegatesToRepository() {
        Property p = sampleProperty(null);
        when(propertyRepository.save(p)).thenReturn(sampleProperty(1L));

        Property saved = propertyService.save(p);

        assertThat(saved.getId()).isEqualTo(1L);
        verify(propertyRepository).save(p);
    }

    @Test
    void deleteById_delegatesToRepository() {
        propertyService.deleteById(5L);

        verify(propertyRepository).deleteById(5L);
    }

    @Test
    void count_returnsRepositoryCount() {
        when(propertyRepository.count()).thenReturn(7L);

        assertThat(propertyService.count()).isEqualTo(7L);
    }
}
