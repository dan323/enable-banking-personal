package com.personal.banking.adapter.in.web;

import com.personal.banking.adapter.in.web.dto.BanksResponse;
import com.personal.banking.adapter.in.web.dto.TransactionsResponse;
import com.personal.banking.domain.exception.BankingProviderException;
import com.personal.banking.port.in.DisconnectBankUseCase;
import com.personal.banking.port.in.GetPortfolioUseCase;
import com.personal.banking.port.in.GetTransactionsUseCase;
import com.personal.banking.port.in.ListBanksUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Registered as a bean in the bootstrap module's WebConfig (no component scanning). */
@RestController
@RequestMapping("/api")
public class ApiController {

    private final GetPortfolioUseCase portfolio;
    private final ListBanksUseCase banks;
    private final GetTransactionsUseCase transactions;
    private final DisconnectBankUseCase disconnect;

    public ApiController(GetPortfolioUseCase portfolio, ListBanksUseCase banks,
                         GetTransactionsUseCase transactions, DisconnectBankUseCase disconnect) {
        this.portfolio = portfolio;
        this.banks = banks;
        this.transactions = transactions;
        this.disconnect = disconnect;
    }

    @GetMapping("/summary")
    public GetPortfolioUseCase.Summary summary() {
        return portfolio.summary();
    }

    /** Exact bank names as Enable Banking knows them (use these with /connect). */
    @GetMapping("/banks")
    public BanksResponse banks(@RequestParam(defaultValue = "ES") String country) {
        return BanksResponse.from(banks.listBanks(country));
    }

    @GetMapping("/accounts/{uid}/transactions")
    public TransactionsResponse transactions(@PathVariable String uid,
                                             @RequestParam(required = false) String dateFrom,
                                             @RequestParam(required = false) String continuationKey) {
        return TransactionsResponse.from(transactions.transactions(uid, dateFrom, continuationKey));
    }

    @DeleteMapping("/connections/{sessionId}")
    public void disconnect(@PathVariable String sessionId) {
        disconnect.disconnect(sessionId);
    }

    @ExceptionHandler(BankingProviderException.class)
    public ResponseEntity<Map<String, Object>> upstream(BankingProviderException e) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Map.of("error", e.getMessage(), "detail", String.valueOf(e.detail())));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> config(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
    }
}
