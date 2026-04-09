package com.socialapp.domain.admin.repository;

import com.socialapp.domain.admin.valueobject.Statistics;

public interface AdminRepository {
    Statistics getUserStatistics();
    Statistics getPostStatistics();
}