package com.personal.banking.adapter.in.web.dto;

import com.personal.banking.domain.model.Bank;

import java.util.List;

/** JSON shape consumed by the dashboard: {"aspsps":[{"name","country"}]}. */
public record BanksResponse(List<BankDto> aspsps) {

    public record BankDto(String name, String country) {}

    public static BanksResponse from(List<Bank> banks) {
        return new BanksResponse(banks.stream().map(b -> new BankDto(b.name(), b.country())).toList());
    }
}
