package com.personal.banking.adapter.out.persistence;

import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.personal.banking.port.out.ConnectionRepositoryPort;
import com.personal.banking.domain.model.Account;
import com.personal.banking.domain.model.Connection;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Keeps authorised bank connections in a local JSON file (data/connections.json). */
public class JsonFileConnectionRepository implements ConnectionRepositoryPort {

    // File format, kept independent from the domain model so the two can evolve separately.
    // Field names match the format written before the hexagonal refactor.
    record AccountEntity(String uid, String iban, String name, String currency) {}

    record ConnectionEntity(String sessionId, String bank, String country,
                            String validUntil, List<AccountEntity> accounts) {}

    private final Path file;
    private final ObjectMapper mapper;

    public JsonFileConnectionRepository(Path file, ObjectMapper mapper) {
        this.file = file;
        this.mapper = mapper;
    }

    @Override
    public synchronized List<Connection> findAll() {
        return readEntities().stream().map(JsonFileConnectionRepository::toDomain).toList();
    }

    @Override
    public synchronized void save(Connection connection) {
        List<ConnectionEntity> list = readEntities();
        list.removeIf(c -> c.bank().equals(connection.bank()) && c.country().equals(connection.country()));
        list.add(toEntity(connection));
        write(list);
    }

    @Override
    public synchronized void remove(String sessionId) {
        List<ConnectionEntity> list = readEntities();
        list.removeIf(c -> c.sessionId().equals(sessionId));
        write(list);
    }

    private List<ConnectionEntity> readEntities() {
        try {
            if (!Files.exists(file)) return new ArrayList<>();
            return new ArrayList<>(mapper.readValue(file.toFile(), new TypeReference<List<ConnectionEntity>>() {}));
        } catch (JacksonException e) {   // Jackson 3: unchecked, covers I/O and parse errors
            throw new IllegalStateException("Cannot read " + file, e);
        }
    }

    private void write(List<ConnectionEntity> list) {
        try {
            Files.createDirectories(file.toAbsolutePath().getParent());
            mapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), list);
        } catch (IOException | JacksonException e) {
            throw new IllegalStateException("Cannot write " + file, e);
        }
    }

    private static Connection toDomain(ConnectionEntity e) {
        return new Connection(e.sessionId(), e.bank(), e.country(), e.validUntil(),
                e.accounts().stream()
                        .map(a -> new Account(a.uid(), a.iban(), a.name(), a.currency()))
                        .toList());
    }

    private static ConnectionEntity toEntity(Connection c) {
        return new ConnectionEntity(c.sessionId(), c.bank(), c.country(), c.validUntil(),
                c.accounts().stream()
                        .map(a -> new AccountEntity(a.uid(), a.iban(), a.name(), a.currency()))
                        .toList());
    }
}
