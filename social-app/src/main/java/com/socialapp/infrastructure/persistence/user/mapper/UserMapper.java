package com.socialapp.infrastructure.persistence.user.mapper;

import com.socialapp.domain.user.entity.User;
import com.socialapp.domain.user.valueobject.*;
import com.socialapp.infrastructure.persistence.user.neo4j.node.UserNode;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class UserMapper {

    public User toDomain(UserNode node) {
        return User.reconstitute(
                node.getId(),
                Username.of(node.getUsername()),
                FullName.of(node.getFamilyName(), node.getGivenName()),
                LocalDate.parse(node.getBirthdate()),
                node.getBio(),
                UserCounts.of(
                        orZero(node.getFriendCount()),
                        orZero(node.getBlockCount()),
                        orZero(node.getRequestSentCount()),
                        orZero(node.getRequestReceivedCount())
                ),
                ChangeRestriction.of(
                        parseDate(node.getNextChangeNameDate()),
                        parseDate(node.getNextChangeUsernameDate()),
                        parseDate(node.getNextChangeBirthdateDate())
                ),
                node.getProfilePicturePath()
        );
    }

    public UserNode toNode(User user) {
        return UserNode.builder()
                .id(user.getId())
                .username(user.getUsername().getValue())
                .familyName(user.getFullName().getFamilyName())
                .givenName(user.getFullName().getGivenName())
                .birthdate(user.getBirthdate().toString())
                .bio(user.getBio())
                .friendCount(user.getCounts().getFriendCount())
                .blockCount(user.getCounts().getBlockCount())
                .requestSentCount(user.getCounts().getRequestSentCount())
                .requestReceivedCount(user.getCounts().getRequestReceivedCount())
                .nextChangeNameDate(user.getChangeRestriction().getNextChangeNameDate().toString())
                .nextChangeUsernameDate(user.getChangeRestriction().getNextChangeUsernameDate().toString())
                .nextChangeBirthdateDate(user.getChangeRestriction().getNextChangeBirthdateDate().toString())
                .profilePicturePath(user.getProfilePicturePath())
                .build();
    }

    private int orZero(Integer val) {
        return val == null ? 0 : val;
    }

    private LocalDate parseDate(String val) {
        return (val == null || val.isBlank())
                ? LocalDate.now().minusDays(1)
                : LocalDate.parse(val);
    }
}