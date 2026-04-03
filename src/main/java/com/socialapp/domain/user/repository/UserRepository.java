package com.socialapp.domain.user.repository;


import com.socialapp.domain.user.entity.User;
import com.socialapp.domain.user.valueobject.Username;
import java.util.List;
import java.util.Optional;

public interface UserRepository {

    Optional<User> findById(String id);

    boolean existsByUsername(Username username);

    // Dùng cho SearchUserUseCase
    List<User> searchByKeyword(String keyword, String requesterId);

    // Các method bổ sung thường dùng
    User save(User user);

    public Optional<User> findByUsername(Username username);

    // Nếu cần sau này
    // boolean existsById(String id);
}