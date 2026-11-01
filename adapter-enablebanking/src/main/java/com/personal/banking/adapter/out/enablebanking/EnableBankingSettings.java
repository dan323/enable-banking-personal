package com.personal.banking.adapter.out.enablebanking;

/** Plain configuration for the Enable Banking adapter; filled in by the bootstrap module. */
public record EnableBankingSettings(
        String baseUrl,
        String appId,
        String privateKeyPath,
        String redirectUrl,
        int consentDays) {
}
