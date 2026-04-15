package com.socialapp.application.usecase.admin;

import com.socialapp.application.dto.request.PageRequest;
import com.socialapp.application.dto.response.PageResponse;
import com.socialapp.application.dto.response.UserResponse;
import com.socialapp.application.mapper.UserMapper;
import com.socialapp.domain.repository.UserRepository;


import java.util.List;

public class AdminListUsersUseCase {

    private final UserRepository userRepository;
    private final UserMapper     userMapper;

    public AdminListUsersUseCase(UserRepository userRepository,
                                 UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper     = userMapper;
    }

    public PageResponse<UserResponse> execute(String keyword, PageRequest page) {
        List<UserResponse> users = userRepository
                .searchByKeyword(keyword != null ? keyword : "", page.size(), page.offset())
                .stream()
                .map(userMapper::toResponse)
                .toList();
        return PageResponse.of(users, page.page(), page.size());
    }
}