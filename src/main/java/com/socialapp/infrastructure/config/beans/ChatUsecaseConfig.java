package com.socialapp.infrastructure.config.beans;

import com.socialapp.application.mapper.ChatMapper;
import com.socialapp.application.mapper.MessageMapper;
import com.socialapp.application.usecase.chat.*;
import com.socialapp.domain.repository.ChatRepository;
import com.socialapp.domain.service.ChatDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatUsecaseConfig {

    @Bean
    public AnswerCallUseCase answerCallUseCase(ChatDomainService chatDomainService) {
        return new AnswerCallUseCase(chatDomainService);
    }

    @Bean
    public EndCallUseCase endCallUseCase(ChatDomainService chatDomainService) {
        return new EndCallUseCase(chatDomainService);
    }

    @Bean
    public MarkMessageReadUseCase markMessageReadUseCase(ChatDomainService chatDomainService) {
        return new MarkMessageReadUseCase(chatDomainService);
    }

    @Bean
    public DeleteMessageUseCase deleteMessageUseCase(ChatDomainService chatDomainService) {
        return new DeleteMessageUseCase(chatDomainService);
    }

    @Bean
    public SendMessageUseCase sendMessageUseCase(ChatDomainService chatDomainService,
                                                 MessageMapper messageMapper) {
        return new SendMessageUseCase(chatDomainService, messageMapper);
    }

    @Bean
    public EditMessageUseCase editMessageUseCase(ChatDomainService chatDomainService,
                                                 ChatRepository chatRepository,
                                                 MessageMapper messageMapper) {
        return new EditMessageUseCase(chatDomainService, chatRepository, messageMapper);
    }

    @Bean
    public GetOrCreateChatUseCase getOrCreateChatUseCase(ChatDomainService chatDomainService,
                                                         ChatRepository chatRepository,
                                                         ChatMapper chatMapper) {
        return new GetOrCreateChatUseCase(chatDomainService, chatRepository, chatMapper);
    }

    @Bean
    public ListChatsUseCase listChatsUseCase(ChatRepository chatRepository,
                                             ChatMapper chatMapper) {
        return new ListChatsUseCase(chatRepository, chatMapper);
    }

    @Bean
    public StartCallUseCase startCallUseCase(ChatDomainService chatDomainService) {
        return new StartCallUseCase(chatDomainService);
    }

    @Bean
    public GetChatMessagesUseCase getChatMessagesUseCase(ChatRepository chatRepository,
                                                         MessageMapper messageMapper) {
        return new GetChatMessagesUseCase(chatRepository, messageMapper);
    }
}