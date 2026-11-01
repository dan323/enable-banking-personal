package com.personal.banking.application;

import com.personal.banking.port.in.GetPortfolioUseCase;
import com.personal.banking.port.out.BankingProviderPort;
import com.personal.banking.port.out.ConnectionRepositoryPort;
import com.personal.banking.domain.model.Account;
import com.personal.banking.domain.model.Balance;
import com.personal.banking.domain.model.Connection;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PortfolioServiceTest {

    private final ConnectionRepositoryPort connections = Mockito.mock(ConnectionRepositoryPort.class);
    private final BankingProviderPort provider = Mockito.mock(BankingProviderPort.class);
    private final PortfolioService service = new PortfolioService(connections, provider);

    @Test
    void totalsPreferBookedBalanceAndSumAcrossBanks() {
        Mockito.when(connections.findAll()).thenReturn(List.of(
                new Connection("s1", "Sabadell", "ES", null,
                        List.of(new Account("a1", "ES00", "Cuenta", "EUR"))),
                new Connection("s2", "MyInvestor", "ES", null,
                        List.of(new Account("a2", "ES01", "Cuenta", "EUR")))));

        Mockito.when(provider.balances("a1")).thenReturn(List.of(
                new Balance("ITAV", new BigDecimal("999.00"), "EUR"),
                new Balance("CLBD", new BigDecimal("100.50"), "EUR")));
        Mockito.when(provider.balances("a2")).thenReturn(List.of(
                new Balance("XPCD", new BigDecimal("200.25"), "EUR")));

        GetPortfolioUseCase.Summary s = service.summary();

        assertEquals(0, new BigDecimal("300.75").compareTo(s.totalsByCurrency().get("EUR")));
    }

    @Test
    void accountWithoutBalanceReportsErrorAndIsExcludedFromTotals() {
        Mockito.when(connections.findAll()).thenReturn(List.of(
                new Connection("s1", "Sabadell", "ES", null,
                        List.of(new Account("a1", "ES00", "Cuenta", "EUR")))));
        Mockito.when(provider.balances("a1")).thenReturn(List.of());

        GetPortfolioUseCase.Summary s = service.summary();

        GetPortfolioUseCase.AccountView view = s.banks().get(0).accounts().get(0);
        assertNull(view.balance());
        assertEquals("No balance returned", view.error());
        assertEquals(true, s.totalsByCurrency().isEmpty());
    }

    @Test
    void providerFailureIsReportedPerAccountWithoutFailingTheSummary() {
        Mockito.when(connections.findAll()).thenReturn(List.of(
                new Connection("s1", "Sabadell", "ES", null,
                        List.of(new Account("a1", "ES00", "Cuenta", "EUR")))));
        Mockito.when(provider.balances("a1")).thenThrow(new IllegalStateException("boom"));

        GetPortfolioUseCase.Summary s = service.summary();

        assertEquals("boom", s.banks().get(0).accounts().get(0).error());
    }
}
