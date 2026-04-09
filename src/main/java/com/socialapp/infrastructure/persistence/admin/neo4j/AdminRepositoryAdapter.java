package com.socialapp.infrastructure.persistence.admin.neo4j;

import com.socialapp.domain.admin.repository.AdminRepository;
import com.socialapp.domain.admin.valueobject.StatEntry;
import com.socialapp.domain.admin.valueobject.Statistics;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.Result;

import java.util.List;

public class AdminRepositoryAdapter implements AdminRepository {

    private final Driver driver;

    public AdminRepositoryAdapter(Driver driver) {
        this.driver = driver;
    }

    @Override
    public Statistics getUserStatistics() {
        try (Session session = driver.session()) {

            long totalUsers = session.run(
                    "MATCH (u:UserNode) RETURN count(u) AS cnt"
            ).single().get("cnt").asLong();

            long activeUsers = session.run(
                    "MATCH (u:UserNode) WHERE u.createdAt >= datetime() - duration('P7D') RETURN count(u) AS cnt"
            ).single().get("cnt").asLong();

            long weekUsers = session.run(
                    "MATCH (u:UserNode) WHERE u.createdAt >= datetime() - duration('P7D') RETURN count(u) AS cnt"
            ).single().get("cnt").asLong();

            long monthUsers = session.run(
                    "MATCH (u:UserNode) WHERE u.createdAt >= datetime() - duration('P30D') RETURN count(u) AS cnt"
            ).single().get("cnt").asLong();

            long yearUsers = session.run(
                    "MATCH (u:UserNode) WHERE u.createdAt >= datetime() - duration('P365D') RETURN count(u) AS cnt"
            ).single().get("cnt").asLong();

            long totalFriendships = session.run(
                    "MATCH ()-[r:FRIEND]->() RETURN count(r) AS cnt"
            ).single().get("cnt").asLong();

            return new Statistics(
                    List.of(
                            new StatEntry("Tổng người dùng", totalUsers, "Tổng quan"),
                            new StatEntry("Người dùng mới (7 ngày)", activeUsers, "Tổng quan")
                    ),
                    List.of(new StatEntry("Người dùng đăng ký", weekUsers, "Tuần này")),
                    List.of(new StatEntry("Người dùng đăng ký", monthUsers, "Tháng này")),
                    List.of(new StatEntry("Người dùng đăng ký", yearUsers, "Năm nay")),
                    List.of(
                            new StatEntry("Tổng người dùng", totalUsers, "Toàn thời gian"),
                            new StatEntry("Tổng kết bạn", totalFriendships, "Toàn thời gian")
                    )
            );
        }
    }

    @Override
    public Statistics getPostStatistics() {
        try (Session session = driver.session()) {

            long totalPosts = session.run(
                    "MATCH (p:PostNode) WHERE p.deletedAt IS NULL RETURN count(p) AS cnt"
            ).single().get("cnt").asLong();

            long weekPosts = session.run(
                    "MATCH (p:PostNode) WHERE p.deletedAt IS NULL AND p.createdAt >= datetime() - duration('P7D') RETURN count(p) AS cnt"
            ).single().get("cnt").asLong();

            long monthPosts = session.run(
                    "MATCH (p:PostNode) WHERE p.deletedAt IS NULL AND p.createdAt >= datetime() - duration('P30D') RETURN count(p) AS cnt"
            ).single().get("cnt").asLong();

            long yearPosts = session.run(
                    "MATCH (p:PostNode) WHERE p.deletedAt IS NULL AND p.createdAt >= datetime() - duration('P365D') RETURN count(p) AS cnt"
            ).single().get("cnt").asLong();

            long totalLikes = session.run(
                    "MATCH ()-[r:LIKED_POST]->() RETURN count(r) AS cnt"
            ).single().get("cnt").asLong();

            long totalComments = session.run(
                    "MATCH (c:CommentNode) WHERE c.deletedAt IS NULL RETURN count(c) AS cnt"
            ).single().get("cnt").asLong();

            long totalShares = session.run(
                    "MATCH (p:PostNode) WHERE p.isShared = true AND p.deletedAt IS NULL RETURN count(p) AS cnt"
            ).single().get("cnt").asLong();

            return new Statistics(
                    List.of(
                            new StatEntry("Tổng bài đăng", totalPosts, "Tổng quan"),
                            new StatEntry("Tổng lượt thích", totalLikes, "Tổng quan"),
                            new StatEntry("Tổng bình luận", totalComments, "Tổng quan")
                    ),
                    List.of(new StatEntry("Bài đăng mới", weekPosts, "Tuần này")),
                    List.of(new StatEntry("Bài đăng mới", monthPosts, "Tháng này")),
                    List.of(new StatEntry("Bài đăng mới", yearPosts, "Năm nay")),
                    List.of(
                            new StatEntry("Tổng bài đăng", totalPosts, "Toàn thời gian"),
                            new StatEntry("Tổng lượt share", totalShares, "Toàn thời gian"),
                            new StatEntry("Tổng bình luận", totalComments, "Toàn thời gian")
                    )
            );
        }
    }
}