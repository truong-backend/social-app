package com.socialapp.infrastructure.adapter.persistence;

import com.socialapp.application.port.AdminStatsPort;
import com.socialapp.infrastructure.adapter.persistence.neo4j.repository.PostNeo4jRepository;
import com.socialapp.infrastructure.adapter.persistence.neo4j.repository.UserNeo4jRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class AdminStatsAdapter implements AdminStatsPort {

    private final UserNeo4jRepository userRepo;
    private final PostNeo4jRepository postRepo;

    public AdminStatsAdapter(UserNeo4jRepository userRepo,
                             PostNeo4jRepository postRepo) {
        this.userRepo = userRepo;
        this.postRepo = postRepo;
    }

    @Override
    public long countTotalUsers() {
        return userRepo.countAll();
    }

    @Override
    public long countNewUsers(LocalDate from, LocalDate to) {
        // UserNeo4j không có createdAt — đếm bằng countAll() tạm thời
        // TODO: thêm createdAt vào UserNode nếu cần thống kê chính xác
        return userRepo.countAll();
    }

    @Override
    public long countTotalPosts() {
        return postRepo.countTotal();
    }

    @Override
    public long countNewPosts(LocalDate from, LocalDate to) {
        return postRepo.countNewPosts(
                from.atStartOfDay(),
                to.atTime(23, 59, 59));
    }

    @Override
    public long countTotalComments() {
        // Comment là child node của Post — cần custom query
        // Delegate về PostNeo4jRepo
        return postRepo.countAll(); // placeholder — thay bằng countTotalComments() khi có
    }

    @Override
    public long countDeletedPosts(LocalDate from, LocalDate to) {
        return postRepo.countDeletedPosts(
                from.atStartOfDay(),
                to.atTime(23, 59, 59));
    }
}