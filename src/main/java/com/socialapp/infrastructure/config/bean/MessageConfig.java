package com.socialapp.infrastructure.config.bean;

import com.socialapp.application.message.usecase.*;
import com.socialapp.domain.message.repository.ChatRepository;
import com.socialapp.domain.message.repository.MessageRepository;
import com.socialapp.infrastructure.persistence.message.mapper.MessageMapper;
import com.socialapp.infrastructure.persistence.message.neo4j.MessageRepositoryAdapter;
import com.socialapp.infrastructure.persistence.message.neo4j.repository.MessageNeo4jRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageConfig {

    @Bean
    public MessageRepository messageRepository(MessageNeo4jRepository neo4jRepository,
                                               MessageMapper mapper) {
        return new MessageRepositoryAdapter(neo4jRepository, mapper);
    }

    @Bean
    public MarkMessagesReadUseCase markMessagesReadUseCase(ChatRepository chatRepository,
                                                           MessageRepository messageRepository) {
        return new MarkMessagesReadUseCase(chatRepository, messageRepository);
    }
}