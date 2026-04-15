package com.socialapp.infrastructure.config.bean;

import com.socialapp.application.call.usecase.EndCallUseCase;
import com.socialapp.application.call.usecase.InitiateCallUseCase;
import com.socialapp.application.shared.port.RealtimePublisher;
import com.socialapp.domain.message.repository.CallRepository;
import com.socialapp.domain.message.repository.ChatRepository;
import com.socialapp.infrastructure.call.InCallStore;
import com.socialapp.infrastructure.persistence.message.mapper.CallMapper;
import com.socialapp.infrastructure.persistence.message.neo4j.CallRepositoryAdapter;
import com.socialapp.infrastructure.persistence.message.neo4j.repository.CallNeo4jRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CallConfig {

    @Bean
    public CallRepository callRepository(CallNeo4jRepository neo4jRepository,
                                         CallMapper mapper) {
        return new CallRepositoryAdapter(neo4jRepository, mapper);
    }

    @Bean
    public InitiateCallUseCase initiateCallUseCase(ChatRepository chatRepository,
                                                   CallRepository callRepository,
                                                   RealtimePublisher realtimePublisher,
                                                   InCallStore inCallStore) {
        return new InitiateCallUseCase(chatRepository, callRepository,
                realtimePublisher, inCallStore);
    }

    @Bean
    public EndCallUseCase endCallUseCase(CallRepository callRepository,
                                         RealtimePublisher realtimePublisher,
                                         InCallStore inCallStore) {
        return new EndCallUseCase(callRepository, realtimePublisher, inCallStore);
    }
}