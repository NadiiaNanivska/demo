package com.example.demo.configuration;

import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.github.perplexhub.rsql.RSQLJPASupport;
import jakarta.persistence.EntityManager;
import java.util.Map;

@Configuration
public class ApplicationConfiguration {
    @Bean
    public RSQLJPASupport rsqlJpaSupport(EntityManager entityManager) {
        return new RSQLJPASupport(Map.of("entityManager", entityManager));
    }

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
//        modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
        return modelMapper;
    }
}
