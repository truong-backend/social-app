package com.socialapp.application.admin.usecase;

import com.socialapp.domain.admin.repository.AdminRepository;
import com.socialapp.domain.admin.valueobject.Statistics;

public class GetUserStatisticsUseCase {

    private final AdminRepository adminRepository;

    public GetUserStatisticsUseCase(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public Statistics execute() {
        return adminRepository.getUserStatistics();
    }
}