package com.personal.banking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Proves every port has exactly one adapter bean and the controllers are routed. */
@SpringBootTest(properties = "enablebanking.store-path=target/test-connections-does-not-exist.json")
@AutoConfigureMockMvc
class ApplicationWiringTest {

    @Autowired
    MockMvc mvc;

    @Test
    void summaryIsServedWithNoConnections() throws Exception {
        mvc.perform(get("/api/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.banks").isEmpty());
    }

    @Test
    void banksRequestReachesTheProviderAdapter() throws Exception {
        // Without an app id the provider call fails with a config error: it reaches the adapter, not a 404.
        mvc.perform(get("/api/banks"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void callbackWithUnknownStateRedirectsWithError() throws Exception {
        mvc.perform(get("/callback").param("code", "x").param("state", "forged"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "/?error=unknown_state"));
    }
}
