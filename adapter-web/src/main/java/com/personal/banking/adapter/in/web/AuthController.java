package com.personal.banking.adapter.in.web;

import com.personal.banking.port.in.CompleteBankConnectionUseCase;
import com.personal.banking.port.in.StartBankConnectionUseCase;
import com.personal.banking.domain.exception.UnknownAuthorizationStateException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/** Bank authorisation flow: /connect sends you to your bank, /callback receives you back. */
@RestController
public class AuthController {

    private final StartBankConnectionUseCase start;
    private final CompleteBankConnectionUseCase complete;

    public AuthController(StartBankConnectionUseCase start, CompleteBankConnectionUseCase complete) {
        this.start = start;
        this.complete = complete;
    }

    @GetMapping("/connect")
    public ResponseEntity<Void> connect(@RequestParam String bank, @RequestParam(defaultValue = "ES") String country) {
        return ResponseEntity.status(302).location(start.start(bank, country)).build();
    }

    @GetMapping("/callback")
    public ResponseEntity<Void> callback(@RequestParam(required = false) String code,
                                         @RequestParam(required = false) String state,
                                         @RequestParam(required = false) String error) {
        if (error != null || code == null || state == null) {
            return redirect("/?error=" + (error != null ? error : "missing_code"));
        }
        try {
            String bank = complete.complete(code, state);
            return redirect("/?connected=" + encode(bank));
        } catch (UnknownAuthorizationStateException e) {
            return redirect("/?error=unknown_state");
        } catch (RuntimeException e) {
            return redirect("/?error=" + encode(String.valueOf(e.getMessage())));
        }
    }

    private static ResponseEntity<Void> redirect(String to) {
        return ResponseEntity.status(302).location(URI.create(to)).build();
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
