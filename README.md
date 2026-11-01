# enable-banking-personal

A small, **local, read-only** dashboard that shows Sabadell and MyInvestor (or any bank supported by
[Enable Banking](https://enablebanking.com)) in one place, using the regulated PSD2 channel.
No bank passwords ever pass through this app: you approve access on each bank's own website.

- Java 21, Spring Boot 3, Maven
- Runs only on `127.0.0.1` (your computer)
- Only reads accounts, balances and transactions. It has no payment code.

## 1. One-time setup at Enable Banking

1. Create an account at <https://enablebanking.com> and open the Control Panel.
2. Register a new **production** application.
   - Generate the key in the browser/let them generate it, and download the `.pem` private key.
   - Production apps cannot use `localhost` as a redirect URL, so use the HTTPS relay page in `docs/callback.html`
     (served by GitHub Pages, see below): `https://dan323.github.io/enable-banking-personal/callback.html`
     It just forwards the bank's response to `http://localhost:8080/callback` in your own browser.
   - Privacy URL: `https://github.com/dan323/enable-banking-personal/blob/main/PRIVACY.md`,
     Terms URL: `https://github.com/dan323/enable-banking-personal/blob/main/TERMS.md`
3. Copy the private key to `secrets/private.pem`.
   - It must start with `-----BEGIN PRIVATE KEY-----`. If it says `BEGIN RSA PRIVATE KEY`, convert it:
     `openssl pkcs8 -topk8 -nocrypt -in old.pem -out secrets/private.pem`
4. Copy the **Application ID** (a UUID).
5. Activate the app by linking your own account ("Activate by linking accounts"). This puts the app in
   *restricted production* mode, which is meant for personal, non-commercial use of your own accounts.
   Per Enable Banking, a restricted app can only read accounts linked to it.

## 2. Run

PowerShell:

```powershell
$env:ENABLE_BANKING_APP_ID = "your-application-uuid"
$env:ENABLE_BANKING_REDIRECT_URL = "https://dan323.github.io/enable-banking-personal/callback.html"
mvn -pl bootstrap -am spring-boot:run
```

The redirect URL sent to the bank must be identical to the one registered at Enable Banking. The local app
still receives the response at `http://localhost:8080/callback` (via the relay page).

The relay page is published with GitHub Pages: repository **Settings -> Pages -> Deploy from a branch ->
`main` / `/docs`**.

In IntelliJ: open the folder as a Maven project, add `ENABLE_BANKING_APP_ID` to the run configuration's
environment variables, set its **working directory to the repository root** (so `secrets/` and `data/`
resolve), and run `EnableBankingApplication` from the `bootstrap` module.

## Architecture (hexagonal, one Maven module per layer)

```
domain                 entities and value objects; depends on nothing
port                   inbound use-case interfaces + outbound SPI interfaces  -> domain
application            use case implementations, plain Java                   -> domain, port
adapter-enablebanking  BankingProviderPort over the Enable Banking REST API   -> domain, port
adapter-persistence    JSON-file connection store, in-memory pending states   -> domain, port
adapter-web            REST controllers (inbound)                             -> domain, port
bootstrap              Spring Boot app; the only place where beans are created
```

Rules, checked by the build:

- Adapters and `application` never depend on each other; only `bootstrap` sees everything.
- Beans are created only by `@Bean` methods in `bootstrap/config/*Config.java`. No `@Component`, `@Service`,
  `@Repository` or `@ConfigurationProperties` anywhere else.
- The only Spring annotations outside `bootstrap` are the web controller ones in `adapter-web`.

Then open <http://localhost:8080>, search for **Sabadell**, click it, approve at the bank. Repeat for
**MyInvestor**.

## Notes

- Bank names must match Enable Banking's list exactly; the search box on the page uses that list. If
  MyInvestor does not appear, it is not available through Enable Banking for Spain.
- Consent lasts `ENABLE_BANKING_CONSENT_DAYS` (default 90; banks may cap it). The dashboard warns you before
  it expires; click **Reconnect** to renew.
- Connections are stored in `data/connections.json`. `secrets/` and `data/` are git-ignored.
- Restricted-mode limits (for example which accounts and how often you can fetch data) are set by Enable
  Banking and the banks; PSD2 banks usually allow only a few unattended data refreshes per day.
- Prices of funds, stocks or ETFs held at MyInvestor depend on what its PSD2 interface exposes (account
  balances and transactions). Investment positions may not be included.

## Environment variables

| Variable                    | Default                          |
|-----------------------------|----------------------------------|
| `ENABLE_BANKING_APP_ID`     | *(required)*                     |
| `ENABLE_BANKING_KEY_PATH`   | `secrets/private.pem`            |
| `ENABLE_BANKING_REDIRECT_URL` | `http://localhost:8080/callback` |
| `ENABLE_BANKING_CONSENT_DAYS` | `90`                             |
| `ENABLE_BANKING_STORE_PATH` | `data/connections.json`          |
