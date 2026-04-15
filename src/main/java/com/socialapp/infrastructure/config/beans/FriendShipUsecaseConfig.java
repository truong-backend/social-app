package com.socialapp.infrastructure.config.beans;

import com.socialapp.application.mapper.UserMapper;
import com.socialapp.application.usecase.friendship.*;
import com.socialapp.domain.repository.UserRepository;
import com.socialapp.domain.service.FriendshipDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FriendShipUsecaseConfig {

    @Bean
    public SendFriendRequestUseCase sendFriendRequestUseCase(
            FriendshipDomainService friendshipDomainService
    ) {
        return new SendFriendRequestUseCase(friendshipDomainService);
    }

    @Bean
    public AcceptFriendRequestUseCase acceptFriendRequestUseCase(
            FriendshipDomainService friendshipDomainService
    ) {
        return new AcceptFriendRequestUseCase(friendshipDomainService);
    }

    @Bean
    public RejectFriendRequestUseCase rejectFriendRequestUseCase(
            FriendshipDomainService friendshipDomainService
    ) {
        return new RejectFriendRequestUseCase(friendshipDomainService);
    }

    @Bean
    public CancelFriendRequestUseCase cancelFriendRequestUseCase(
            FriendshipDomainService friendshipDomainService
    ) {
        return new CancelFriendRequestUseCase(friendshipDomainService);
    }

    @Bean
    public BlockUserUseCase blockUserUseCase(
            FriendshipDomainService friendshipDomainService
    ) {
        return new BlockUserUseCase(friendshipDomainService);
    }

    @Bean
    public UnblockUserUseCase unblockUserUseCase(
            FriendshipDomainService friendshipDomainService
    ) {
        return new UnblockUserUseCase(friendshipDomainService);
    }

    @Bean
    public UnfriendUseCase unfriendUseCase(
            FriendshipDomainService friendshipDomainService
    ) {
        return new UnfriendUseCase(friendshipDomainService);
    }

    @Bean
    public ListFriendsUseCase listFriendsUseCase(
            UserRepository userRepository, UserMapper userMapper) {
        return new ListFriendsUseCase(userRepository, userMapper);
    }

    @Bean
    public ListSentRequestsUseCase listSentRequestsUseCase(
            UserRepository userRepository, UserMapper userMapper) {
        return new ListSentRequestsUseCase(userRepository, userMapper);
    }

    @Bean
    public ListReceivedRequestsUseCase listReceivedRequestsUseCase(
            UserRepository userRepository, UserMapper userMapper) {
        return new ListReceivedRequestsUseCase(userRepository, userMapper);
    }

    @Bean
    public GetFriendStatusUseCase getFriendStatusUseCase(
            UserRepository userRepository) {
        return new GetFriendStatusUseCase(userRepository);
    }
}