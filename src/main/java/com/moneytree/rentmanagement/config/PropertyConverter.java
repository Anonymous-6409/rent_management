package com.moneytree.rentmanagement.config;

import com.moneytree.rentmanagement.model.Property;
import com.moneytree.rentmanagement.repository.PropertyRepository;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class PropertyConverter implements Converter<String, Property> {

    private final PropertyRepository propertyRepository;

    public PropertyConverter(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    @Override
    public Property convert(String source) {
        if (source == null || source.isBlank() || "null".equals(source)) {
            return null;
        }
        return propertyRepository.findById(Long.valueOf(source)).orElse(null);
    }
}
