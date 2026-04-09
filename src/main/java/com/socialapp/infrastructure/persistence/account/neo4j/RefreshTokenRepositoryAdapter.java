package com.socialapp.infrastructure.persistence.account.neo4j;

import com.socialapp.domain.account.entity.RefreshToken;
import com.socialapp.domain.account.repository.RefreshTokenRepository;
import com.socialapp.infrastructure.persistence.account.neo4j.node.RefreshTokenNode;
import com.socialapp.infrastructure.persistence.account.neo4j.repository.RefreshTokenNeo4jRepository;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {

    private final RefreshTokenNeo4jRepository neo4jRepository;

    @Override
    public RefreshToken save(RefreshToken token) {
        return toDomain(neo4jRepository.save(toNode(token)));
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return neo4jRepository.findByToken(token).map(this::toDomain);
    }

    @Override
    public void revokeAllByAccountId(String accountId) {
        neo4jRepository.revokeAllByAccountId(accountId);
    }

    @Override
    public void deleteExpired() {
        neo4jRepository.deleteExpired();
    }

    private RefreshToken toDomain(RefreshTokenNode node) {
        return RefreshToken.reconstitute(
                node.getId(), node.getAccountId(), node.getToken(),
                node.getExpiresAt(), node.isRevoked());
    }

    private RefreshTokenNode toNode(RefreshToken t) {
        return RefreshTokenNode.builder()
                .id(t.getId()).accountId(t.getAccountId())
                .token(t.getToken()).expiresAt(t.getExpiresAt())
                .revoked(t.isRevoked())
                .build();
    }
}