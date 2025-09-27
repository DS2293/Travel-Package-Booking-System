package com.tpbs.packageservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "com.tpbs.packageservice.repository")
@EnableTransactionManagement
public class PackageServiceConfig {
    // CORS is handled centrally by API Gateway
    // No individual service CORS configuration needed
}