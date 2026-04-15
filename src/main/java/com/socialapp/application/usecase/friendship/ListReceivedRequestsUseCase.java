package com.socialapp.application.usecase.friendship;

import com.socialapp.application.dto.request.PageRequest;
import com.socialapp.application.dto.response.PageResponse;
import com.socialapp.application.dto.response.UserResponse;
import com.socialapp.application.mapper.UserMapper;
import com.socialapp.domain.model.valueobject.UserId;
import com.socialapp.domain.repository.UserRepository;

import java.util.List;

public class ListReceivedRequestsUseCase {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public ListReceivedRequestsUseCase(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    public PageResponse<UserResponse> execute(String userId, PageRequest page) {
        int limit  = page.size() + 1;
        int offset = page.page() * page.size();
        List<UserResponse> items = userRepository
                .listReceivedRequests(new UserId(userId), limit, offset)
                .stream().map(userMapper::toResponse).toList();
        boolean hasNext = items.size() > page.size();
        return new PageResponse<>(
                hasNext ? items.subList(0, page.size()) : items,
                page.page(), page.size(), hasNext);
    }
}