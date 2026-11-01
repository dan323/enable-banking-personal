package com.personal.banking.adapter.out.enablebanking;

import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/** Signs the short-lived RS256 JWT that authenticates this application to Enable Banking. */
public class EnableBankingJwtProvider {

    private static final Base64.Encoder B64URL = Base64.getUrlEncoder().withoutPadding();

    private final EnableBankingSettings props;
    private final ObjectMapper mapper;

    private PrivateKey privateKey;
    private String cachedJwt;
    private Instant cachedJwtExpiry = Instant.EPOCH;

    public EnableBankingJwtProvider(EnableBankingSettings props, ObjectMapper mapper) {
        this.props = props;
        this.mapper = mapper;
    }

    public synchronized String token() {
        Instant now = Instant.now();
        if (cachedJwt != null && now.isBefore(cachedJwtExpiry.minusSeconds(60))) {
            return cachedJwt;
        }
        if (props.appId() == null || props.appId().isBlank()) {
            throw new IllegalStateException("ENABLE_BANKING_APP_ID is not set. See README.");
        }
        Instant exp = now.plus(1, ChronoUnit.HOURS);

        Map<String, Object> header = new LinkedHashMap<>();
        header.put("typ", "JWT");
        header.put("alg", "RS256");
        header.put("kid", props.appId());

        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("iss", "enablebanking.com");
        claims.put("aud", "api.enablebanking.com");
        claims.put("iat", now.getEpochSecond());
        claims.put("exp", exp.getEpochSecond());

        String signingInput = encode(header) + "." + encode(claims);
        cachedJwt = signingInput + "." + B64URL.encodeToString(sign(signingInput));
        cachedJwtExpiry = exp;
        return cachedJwt;
    }

    private String encode(Map<String, Object> json) {
        return B64URL.encodeToString(mapper.writeValueAsBytes(json));
    }

    private byte[] sign(String signingInput) {
        try {
            Signature rs256 = Signature.getInstance("SHA256withRSA");
            rs256.initSign(loadKey());
            rs256.update(signingInput.getBytes(StandardCharsets.US_ASCII));
            return rs256.sign();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Cannot sign JWT", e);
        }
    }

    private PrivateKey loadKey() {
        if (privateKey != null) return privateKey;
        try {
            String pem = Files.readString(Path.of(props.privateKeyPath()));
            if (pem.contains("BEGIN RSA PRIVATE KEY")) {
                throw new IllegalStateException("Key is in PKCS#1 format. Convert it with: "
                        + "openssl pkcs8 -topk8 -nocrypt -in old.pem -out private.pem");
            }
            String b64 = pem.replaceAll("-----[A-Z ]+-----", "").replaceAll("\\s", "");
            privateKey = KeyFactory.getInstance("RSA")
                    .generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(b64)));
            return privateKey;
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read private key at " + props.privateKeyPath(), e);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Invalid private key", e);
        }
    }
}
