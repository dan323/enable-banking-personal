package com.personal.banking.config;

import tools.jackson.databind.ObjectMapper;
import com.personal.banking.adapter.out.persistence.InMemoryPendingAuthorizationStore;
import com.personal.banking.adapter.out.persistence.JsonFileConnectionRepository;
import com.personal.banking.port.out.ConnectionRepositoryPort;
import com.personal.banking.port.out.PendingAuthorizationPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

/** Adapter-persistence module. */
@Configuration
public class PersistenceConfig {

    @Bean
    public ConnectionRepositoryPort connectionRepositoryPort(
            @Value("${enablebanking.store-path}") String storePath, ObjectMapper mapper) {
        return new JsonFileConnectionRepository(Path.of(storePath), mapper);
    }

    @Bean
    public PendingAuthorizationPort pendingAuthorizationPort() {
        return new InMemoryPendingAuthorizationStore();
    }
}
