# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

Local, **read-only** PSD2 dashboard (accounts, balances, transactions) over the Enable Banking API. Java 21, Spring Boot 4.1, Jackson 3, multi-module Maven. There is no payment code and none must be added. The project is not a git repository.

## Commands

Run from the repository root (PowerShell or bash):

```
mvn verify                                   # build every module + run all tests (do this before declaring done)
mvn -pl application test                     # tests of one module
mvn -pl application test -Dtest=PortfolioServiceTest#totalsPreferBookedBalanceAndSumAcrossBanks
mvn -pl bootstrap -am test -Dtest=ApplicationWiringTest -Dsurefire.failIfNoSpecifiedTests=false
$env:ENABLE_BANKING_APP_ID = "<uuid>"; mvn -pl bootstrap -am spring-boot:run     # then http://localhost:8080
```

`-am` is needed when a module depends on others that have not been installed. The Spring Boot plugin's working directory is the repo root (set in `bootstrap/pom.xml`) so `secrets/private.pem` and `data/connections.json` resolve there; an IDE run configuration must set the same working directory. Env vars and defaults are in `README.md` and `bootstrap/src/main/resources/application.yml`. There is no linter configured.

## Architecture: hexagonal, one Maven module per layer

```
domain                 records/exceptions, no dependencies
port                   port.in (use cases) + port.out (SPI)        -> domain
application            plain-Java services implementing port.in    -> port, domain
adapter-enablebanking  BankingProviderPort over the REST API       -> port, domain
adapter-persistence    ConnectionRepositoryPort (JSON file), PendingAuthorizationPort (in memory)
adapter-web            REST controllers + snake_case DTOs          -> port, domain
bootstrap              Spring Boot app, all @Configuration, application.yml, static/index.html
```

Each library module has a `module-info.java` (JPMS), enforced at compile time. `bootstrap` deliberately has none (Spring Boot and `@SpringBootTest` run on the classpath), and surefire runs with `useModulePath=false`. `spring-web` is an automatic module (`spring.web`); keep the build free of compiler warnings (do not use `requires transitive` on it, and avoid dependencies without a real module name). When adding a package, export/open it in that module's `module-info.java`, and `opens` it to `tools.jackson.databind` if Jackson (de)serializes its records.

Wiring rules (enforced by `bootstrap/.../SpringUsageRulesTest`):
- Beans are created only by `@Bean` methods in `bootstrap/.../config/*Config.java`. No `@Component`, `@Service`, `@Repository`, `@ConfigurationProperties`, `@Value` or `@Autowired` outside `bootstrap`.
- The only Spring annotations allowed elsewhere are the web controller ones (`@RestController`, mapping annotations, `@ExceptionHandler`) in `adapter-web`. Controllers are registered as beans in `WebConfig`, not component-scanned.
- Configuration values are read with `@Value` in the config classes and passed to adapters as plain objects (e.g. `EnableBankingSettings`, a `Path`).
- A new adapter/service = plain class + a `@Bean` method in the matching config class.

## Things that need several files to understand

- **Bank authorisation flow:** `AuthController./connect` -> `BankConnectionService.start` stores a random `state` in `PendingAuthorizationPort` and gets the bank URL from the provider -> bank redirects to `/callback` -> `complete` consumes the state (single use; unknown state = `UnknownAuthorizationStateException`, guards against forged callbacks), exchanges the code for a session, and saves the `Connection`. Enable Banking returns the accounts only once, at session creation, so they must be persisted then.
- **Provider JSON never leaves `adapter-enablebanking`:** `EnableBankingMapper` converts Enable Banking payloads to domain types; `adapter-web` DTOs (`BanksResponse`, `TransactionsResponse`) re-emit exactly the snake_case fields that `static/index.html` reads (`aspsps[].name`, `transaction_amount`, `credit_debit_indicator`, `creditor.name`, `remittance_information`, ...). Changing either side means checking `index.html`.
- **Errors:** the provider adapter translates HTTP errors to `BankingProviderException`; `ApiController` maps it to 502 and `IllegalStateException` (missing app id, unreadable key/store) to 500. `PortfolioService` swallows per-account failures into `AccountView.error` so one bank failing does not fail the summary.
- **Balance choice** is domain logic: `Balance.preferred` picks by ISO 20022 type order CLBD, ITBD, XPCD, CLAV, ITAV, else first.
- **Persistence format:** `connections.json` uses private record entities in `JsonFileConnectionRepository` (field names `sessionId`, `validUntil`, ...), kept independent of the domain model; keep it backward compatible with existing files. `save` replaces any connection with the same bank+country.
- **JWT:** `EnableBankingJwtProvider` builds and signs the RS256 JWT itself with the JDK (`Signature`) and Jackson 3, using a PKCS#8 key (PKCS#1 is rejected with a conversion hint). No JWT library is used on purpose: jjwt is a filename-based automatic module (compile warning) and its Jackson module would bring Jackson 2 back. Do not add Jackson 2 dependencies; only `com.fasterxml.jackson.annotation` (2.x) is shared with Jackson 3 by design.
- **Jackson 3 specifics:** packages are `tools.jackson.*`, `JacksonException` is unchecked.
- **Boot 4 specifics:** starters are `spring-boot-starter-webmvc` / `-restclient` / `-webmvc-test`; `@AutoConfigureMockMvc` lives in `org.springframework.boot.webmvc.test.autoconfigure`.

## Testing approach

Application services are tested with Mockito mocks of the ports (no Spring); `EnableBankingMapperTest` and `EnableBankingJwtProviderTest` cover the adapter without network; `ApplicationWiringTest` boots the real context with MockMvc and a non-existent store path. Tests never hit the real Enable Banking API.
