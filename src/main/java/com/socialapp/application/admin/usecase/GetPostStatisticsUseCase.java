package com.socialapp.application.admin.usecase;

import com.socialapp.domain.admin.repository.AdminRepository;
import com.socialapp.domain.admin.valueobject.Statistics;

public class GetPostStatisticsUseCase {

    private final AdminRepository adminRepository;

    public GetPostStatisticsUseCase(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public Statistics execute() {
        return adminRepository.getPostStatistics();
    }
}