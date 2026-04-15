package com.socialapp.application.usecase.admin;

import com.socialapp.application.dto.request.AdminStatsRequest;
import com.socialapp.application.dto.response.AdminStatsResponse;
import com.socialapp.application.port.AdminStatsPort;

public class AdminGetStatsUseCase {

    private final AdminStatsPort adminStatsPort;

    public AdminGetStatsUseCase(AdminStatsPort adminStatsPort) {
        this.adminStatsPort = adminStatsPort;
    }

    public AdminStatsResponse execute(AdminStatsRequest req) {
        return new AdminStatsResponse(
                req.from(),
                req.to(),
                adminStatsPort.countTotalUsers(),
                adminStatsPort.countNewUsers(req.from(), req.to()),
                adminStatsPort.countTotalPosts(),
                adminStatsPort.countNewPosts(req.from(), req.to()),
                adminStatsPort.countTotalComments(),
                adminStatsPort.countDeletedPosts(req.from(), req.to())
        );
    }
}