package com.moneytree.rentmanagement.config;

import com.moneytree.rentmanagement.model.Tenant;
import com.moneytree.rentmanagement.repository.TenantRepository;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class TenantConverter implements Converter<String, Tenant> {

    private final TenantRepository tenantRepository;

    public TenantConverter(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @Override
    public Tenant convert(String source) {
        if (source == null || source.isBlank() || "null".equals(source)) {
            return null;
        }
        return tenantRepository.findById(Long.valueOf(source)).orElse(null);
    }
}
