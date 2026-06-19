package com.moneytree.rentmanagement.config;

import com.moneytree.rentmanagement.model.Owner;
import com.moneytree.rentmanagement.repository.OwnerRepository;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class OwnerConverter implements Converter<String, Owner> {

    private final OwnerRepository ownerRepository;

    public OwnerConverter(OwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
    }

    @Override
    public Owner convert(String source) {
        if (source == null || source.isBlank() || "null".equals(source)) {
            return null;
        }
        return ownerRepository.findById(Long.valueOf(source)).orElse(null);
    }
}
