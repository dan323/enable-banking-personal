package com.personal.banking.adapter.out.enablebanking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnableBankingJwtProviderTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @TempDir
    Path dir;

    @Test
    void signsAVerifiableRs256JwtWithTheApplicationIdAsKeyId() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        KeyPair pair = gen.generateKeyPair();
        Path pem = dir.resolve("private.pem");
        Files.writeString(pem, "-----BEGIN PRIVATE KEY-----\n"
                + Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(pair.getPrivate().getEncoded())
                + "\n-----END PRIVATE KEY-----\n");

        var provider = new EnableBankingJwtProvider(
                new EnableBankingSettings("https://x", "app-123", pem.toString(), "http://cb", 90), mapper);

        String jwt = provider.token();

        String[] parts = jwt.split("\\.");
        assertEquals(3, parts.length);
        JsonNode header = mapper.readTree(Base64.getUrlDecoder().decode(parts[0]));
        JsonNode claims = mapper.readTree(Base64.getUrlDecoder().decode(parts[1]));
        assertEquals("RS256", header.path("alg").asText());
        assertEquals("JWT", header.path("typ").asText());
        assertEquals("app-123", header.path("kid").asText());
        assertEquals("enablebanking.com", claims.path("iss").asText());
        assertEquals("api.enablebanking.com", claims.path("aud").asText());
        assertEquals(3600, claims.path("exp").asLong() - claims.path("iat").asLong());

        Signature verifier = Signature.getInstance("SHA256withRSA");
        verifier.initVerify(pair.getPublic());
        verifier.update((parts[0] + "." + parts[1]).getBytes(StandardCharsets.US_ASCII));
        assertTrue(verifier.verify(Base64.getUrlDecoder().decode(parts[2])), "signature must verify");

        assertSame(jwt, provider.token(), "token is cached until close to expiry");
    }

    @Test
    void failsClearlyWhenApplicationIdIsMissing() {
        var provider = new EnableBankingJwtProvider(
                new EnableBankingSettings("https://x", "", "k.pem", "http://cb", 90), mapper);

        IllegalStateException e = assertThrows(IllegalStateException.class, provider::token);
        assertTrue(e.getMessage().contains("ENABLE_BANKING_APP_ID"));
    }

    @Test
    void rejectsPkcs1Keys() throws Exception {
        Path pem = dir.resolve("old.pem");
        Files.writeString(pem, "-----BEGIN RSA PRIVATE KEY-----\nAAAA\n-----END RSA PRIVATE KEY-----\n");
        var provider = new EnableBankingJwtProvider(
                new EnableBankingSettings("https://x", "app", pem.toString(), "http://cb", 90), mapper);

        IllegalStateException e = assertThrows(IllegalStateException.class, provider::token);
        assertTrue(e.getMessage().contains("PKCS#1"));
    }
}
