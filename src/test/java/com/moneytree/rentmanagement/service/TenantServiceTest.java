package com.moneytree.rentmanagement.service;

import com.moneytree.rentmanagement.model.Tenant;
import com.moneytree.rentmanagement.repository.TenantRepository;
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
class TenantServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @InjectMocks
    private TenantService tenantService;

    private Tenant sampleTenant(Long id) {
        Tenant t = new Tenant();
        t.setId(id);
        t.setName("Jane Doe");
        t.setEmail("jane@example.com");
        t.setPhone("555-0100");
        return t;
    }

    @Test
    void findAll_returnsAllTenants() {
        when(tenantRepository.findAll()).thenReturn(List.of(sampleTenant(1L)));

        assertThat(tenantService.findAll()).hasSize(1);
        verify(tenantRepository).findAll();
    }

    @Test
    void findById_existingId_returnsTenant() {
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(sampleTenant(1L)));

        assertThat(tenantService.findById(1L).getEmail()).isEqualTo("jane@example.com");
    }

    @Test
    void findById_missingId_throwsException() {
        when(tenantRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tenantService.findById(42L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("42");
    }

    @Test
    void save_delegatesToRepository() {
        Tenant t = sampleTenant(null);
        when(tenantRepository.save(t)).thenReturn(sampleTenant(1L));

        assertThat(tenantService.save(t).getId()).isEqualTo(1L);
        verify(tenantRepository).save(t);
    }

    @Test
    void deleteById_delegatesToRepository() {
        tenantService.deleteById(3L);

        verify(tenantRepository).deleteById(3L);
    }

    @Test
    void count_returnsRepositoryCount() {
        when(tenantRepository.count()).thenReturn(4L);

        assertThat(tenantService.count()).isEqualTo(4L);
    }
}
