package com.personal.banking.config;

import tools.jackson.databind.ObjectMapper;
import com.personal.banking.adapter.out.enablebanking.EnableBankingAdapter;
import com.personal.banking.adapter.out.enablebanking.EnableBankingJwtProvider;
import com.personal.banking.adapter.out.enablebanking.EnableBankingSettings;
import com.personal.banking.port.out.BankingProviderPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/** Adapter-enablebanking module: settings come from application.yml (enablebanking.*). */
@Configuration
public class EnableBankingConfig {

    @Bean
    public EnableBankingSettings enableBankingSettings(
            @Value("${enablebanking.base-url}") String baseUrl,
            @Value("${enablebanking.app-id}") String appId,
            @Value("${enablebanking.private-key-path}") String privateKeyPath,
            @Value("${enablebanking.redirect-url}") String redirectUrl,
            @Value("${enablebanking.consent-days}") int consentDays) {
        return new EnableBankingSettings(baseUrl, appId, privateKeyPath, redirectUrl, consentDays);
    }

    @Bean
    public EnableBankingJwtProvider enableBankingJwtProvider(EnableBankingSettings settings, ObjectMapper mapper) {
        return new EnableBankingJwtProvider(settings, mapper);
    }

    @Bean
    public BankingProviderPort bankingProviderPort(EnableBankingSettings settings, ObjectMapper mapper,
                                                   RestClient.Builder restClientBuilder,
                                                   EnableBankingJwtProvider jwt) {
        return new EnableBankingAdapter(settings, mapper, restClientBuilder, jwt);
    }
}
