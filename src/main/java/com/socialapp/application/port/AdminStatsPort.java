package com.socialapp.application.port;

import java.time.LocalDate;

/**
 * Port (outbound) — Infrastructure implement bằng Cypher query tổng hợp.
 * Tách riêng để không load toàn bộ entity vào memory khi thống kê.
 */
public interface AdminStatsPort {

    long countTotalUsers();

    long countNewUsers(LocalDate from, LocalDate to);

    long countTotalPosts();

    long countNewPosts(LocalDate from, LocalDate to);

    long countTotalComments();

    long countDeletedPosts(LocalDate from, LocalDate to);
}