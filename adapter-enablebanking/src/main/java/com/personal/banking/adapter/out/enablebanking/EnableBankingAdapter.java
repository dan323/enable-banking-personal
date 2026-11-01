package com.personal.banking.adapter.out.enablebanking;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import com.personal.banking.port.out.BankingProviderPort;

import com.personal.banking.domain.exception.BankingProviderException;
import com.personal.banking.domain.model.Balance;
import com.personal.banking.domain.model.Bank;
import com.personal.banking.domain.model.BankSession;
import com.personal.banking.domain.model.TransactionPage;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * READ-ONLY adapter for the Enable Banking API (PSD2 account information).
 * Only account-information endpoints are used; nothing here can initiate payments.
 */
public class EnableBankingAdapter implements BankingProviderPort {

    private final EnableBankingSettings props;
    private final ObjectMapper mapper;
    private final EnableBankingJwtProvider jwt;
    private final RestClient http;

    public EnableBankingAdapter(EnableBankingSettings props, ObjectMapper mapper,
                         RestClient.Builder builder, EnableBankingJwtProvider jwt) {
        this.props = props;
        this.mapper = mapper;
        this.jwt = jwt;
        this.http = builder.baseUrl(props.baseUrl()).build();
    }

    @Override
    public List<Bank> listBanks(String country) {
        return EnableBankingMapper.toBanks(get("/aspsps?country={country}&psu_type=personal", Map.of("country", country)));
    }

    @Override
    public URI startAuthorization(String bank, String country, String state) {
        ObjectNode body = mapper.createObjectNode();
        body.putObject("access").put("valid_until",
                Instant.now().plus(props.consentDays(), ChronoUnit.DAYS).toString());
        body.putObject("aspsp").put("name", bank).put("country", country);
        body.put("state", state);
        body.put("redirect_url", props.redirectUrl());
        body.put("psu_type", "personal");
        return URI.create(post("/auth", body).path("url").asText());
    }

    @Override
    public BankSession createSession(String code) {
        ObjectNode body = mapper.createObjectNode();
        body.put("code", code);
        return EnableBankingMapper.toSession(post("/sessions", body));
    }

    @Override
    public List<Balance> balances(String accountUid) {
        return EnableBankingMapper.toBalances(get("/accounts/{uid}/balances", Map.of("uid", accountUid)));
    }

    @Override
    public TransactionPage transactions(String accountUid, String dateFrom, String continuationKey) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("uid", accountUid);
        StringBuilder path = new StringBuilder("/accounts/{uid}/transactions?");
        if (dateFrom != null) {
            path.append("date_from={dateFrom}&");
            vars.put("dateFrom", dateFrom);
        }
        if (continuationKey != null) {
            path.append("continuation_key={continuationKey}&");
            vars.put("continuationKey", continuationKey);
        }
        return EnableBankingMapper.toTransactionPage(get(path.toString(), vars));
    }

    // ---------- HTTP helpers ----------

    private JsonNode get(String uriTemplate, Map<String, ?> vars) {
        try {
            return http.get().uri(uriTemplate, vars)
                    .header("Authorization", "Bearer " + jwt.token())
                    .retrieve().body(JsonNode.class);
        } catch (RestClientResponseException e) {
            throw translate(e);
        }
    }

    private JsonNode post(String path, JsonNode body) {
        try {
            return http.post().uri(path)
                    .header("Authorization", "Bearer " + jwt.token())
                    .header("Content-Type", "application/json")
                    .body(body)
                    .retrieve().body(JsonNode.class);
        } catch (RestClientResponseException e) {
            throw translate(e);
        }
    }

    private static BankingProviderException translate(RestClientResponseException e) {
        return new BankingProviderException("Enable Banking returned " + e.getStatusCode(),
                e.getResponseBodyAsString(), e);
    }
}
