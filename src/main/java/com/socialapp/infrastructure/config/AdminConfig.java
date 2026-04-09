package com.socialapp.infrastructure.config;

import com.socialapp.application.admin.usecase.GetPostStatisticsUseCase;
import com.socialapp.application.admin.usecase.GetUserStatisticsUseCase;
import com.socialapp.domain.admin.repository.AdminRepository;
import com.socialapp.infrastructure.persistence.admin.neo4j.AdminRepositoryAdapter;
import org.neo4j.driver.Driver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdminConfig {

    @Bean
    public AdminRepository adminRepository(Driver driver) {
        return new AdminRepositoryAdapter(driver);
    }

    @Bean
    public GetUserStatisticsUseCase getUserStatisticsUseCase(AdminRepository adminRepository) {
        return new GetUserStatisticsUseCase(adminRepository);
    }

    @Bean
    public GetPostStatisticsUseCase getPostStatisticsUseCase(AdminRepository adminRepository) {
        return new GetPostStatisticsUseCase(adminRepository);
    }
}