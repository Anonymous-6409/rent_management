package com.moneytree.rentmanagement.service;

import com.moneytree.rentmanagement.model.Owner;
import com.moneytree.rentmanagement.repository.OwnerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OwnerServiceTest {

    @Mock
    private OwnerRepository ownerRepository;

    @InjectMocks
    private OwnerService ownerService;

    private Owner owner(Long id) {
        Owner o = new Owner();
        o.setId(id);
        o.setName("Olivia Owner");
        o.setEmail("olivia@example.com");
        return o;
    }

    @Test
    void findAll_returnsAllOwners() {
        when(ownerRepository.findAll()).thenReturn(List.of(owner(1L)));

        assertThat(ownerService.findAll()).hasSize(1);
    }

    @Test
    void findById_missingId_throwsException() {
        when(ownerRepository.findById(8L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ownerService.findById(8L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("8");
    }

    @Test
    void save_delegatesToRepository() {
        Owner o = owner(null);
        when(ownerRepository.save(o)).thenReturn(owner(1L));

        assertThat(ownerService.save(o).getId()).isEqualTo(1L);
    }

    @Test
    void deleteById_delegatesToRepository() {
        ownerService.deleteById(2L);

        verify(ownerRepository).deleteById(2L);
    }
}
