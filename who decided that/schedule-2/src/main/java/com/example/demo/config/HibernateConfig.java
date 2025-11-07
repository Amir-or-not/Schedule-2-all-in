package com.example.demo.config;

import com.vladmihalcea.hibernate.type.json.JsonType;
import org.hibernate.boot.model.TypeContributions;
import org.hibernate.service.ServiceRegistry;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HibernateConfig {
    // Hibernate 6.x automatically registers basic types, so we don't need to implement TypeContributor
    // The @JdbcTypeCode(SqlTypes.JSON) annotation in the entity is sufficient
}
