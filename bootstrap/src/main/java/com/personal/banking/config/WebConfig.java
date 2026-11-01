package com.personal.banking.config;

import com.personal.banking.adapter.in.web.ApiController;
import com.personal.banking.adapter.in.web.AuthController;
import com.personal.banking.port.in.CompleteBankConnectionUseCase;
import com.personal.banking.port.in.DisconnectBankUseCase;
import com.personal.banking.port.in.GetPortfolioUseCase;
import com.personal.banking.port.in.GetTransactionsUseCase;
import com.personal.banking.port.in.ListBanksUseCase;
import com.personal.banking.port.in.StartBankConnectionUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Adapter-web module: controllers are registered explicitly, never component-scanned. */
@Configuration
public class WebConfig {

    @Bean
    public ApiController apiController(GetPortfolioUseCase portfolio, ListBanksUseCase banks,
                                       GetTransactionsUseCase transactions, DisconnectBankUseCase disconnect) {
        return new ApiController(portfolio, banks, transactions, disconnect);
    }

    @Bean
    public AuthController authController(StartBankConnectionUseCase start, CompleteBankConnectionUseCase complete) {
        return new AuthController(start, complete);
    }
}
