package com.socialapp.application.usecase.user;

import com.socialapp.application.dto.request.PageRequest;
import com.socialapp.application.dto.response.PageResponse;
import com.socialapp.application.dto.response.UserResponse;
import com.socialapp.application.mapper.UserMapper;
import com.socialapp.domain.repository.UserRepository;


import java.util.List;


public class SearchUserUseCase {

    private final UserRepository userRepository;
    private final UserMapper     userMapper;

    public SearchUserUseCase(UserRepository userRepository,
                             UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper     = userMapper;
    }

    public PageResponse<UserResponse> execute(String keyword, PageRequest page) {
        List<UserResponse> results = userRepository
                .searchByKeyword(keyword, page.size(), page.offset())
                .stream()
                .map(userMapper::toResponse)
                .toList();
        return PageResponse.of(results, page.page(), page.size());
    }
}