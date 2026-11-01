package com.personal.banking.config;

import com.personal.banking.application.BankConnectionService;
import com.personal.banking.application.BankingQueryService;
import com.personal.banking.application.PortfolioService;
import com.personal.banking.port.out.BankingProviderPort;
import com.personal.banking.port.out.ConnectionRepositoryPort;
import com.personal.banking.port.out.PendingAuthorizationPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Application module: creates the framework-free use case implementations. */
@Configuration
public class ApplicationConfig {

    @Bean
    public PortfolioService portfolioService(ConnectionRepositoryPort connections, BankingProviderPort provider) {
        return new PortfolioService(connections, provider);
    }

    @Bean
    public BankConnectionService bankConnectionService(BankingProviderPort provider,
                                                       ConnectionRepositoryPort connections,
                                                       PendingAuthorizationPort pending) {
        return new BankConnectionService(provider, connections, pending);
    }

    @Bean
    public BankingQueryService bankingQueryService(BankingProviderPort provider) {
        return new BankingQueryService(provider);
    }
}
