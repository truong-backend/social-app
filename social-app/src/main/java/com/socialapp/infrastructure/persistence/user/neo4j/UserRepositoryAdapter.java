package com.socialapp.infrastructure.persistence.user.neo4j;

import com.socialapp.domain.user.entity.User;
import com.socialapp.domain.user.repository.UserRepository;
import com.socialapp.domain.user.valueobject.Username;
import com.socialapp.infrastructure.persistence.user.mapper.UserMapper;
import com.socialapp.infrastructure.persistence.user.neo4j.repository.UserNeo4jRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final UserNeo4jRepository neo4jRepository;
    private final UserMapper          mapper;

    @Override
    public Optional<User> findById(String id) {
        return neo4jRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByUsername(Username username) {
        return neo4jRepository.findByUsername(username.getValue()).map(mapper::toDomain);
    }

    @Override
    public boolean existsByUsername(Username username) {
        return neo4jRepository.existsByUsername(username.getValue());
    }

    @Override
    public List<User> searchByKeyword(String keyword, String requesterId) {
        return neo4jRepository.searchByKeyword(keyword, requesterId, 0, 20)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public User save(User user) {
        return mapper.toDomain(neo4jRepository.save(mapper.toNode(user)));
    }
}
