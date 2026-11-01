package com.personal.banking.port.out;

import com.personal.banking.domain.model.Connection;

import java.util.List;

public interface ConnectionRepositoryPort {

    List<Connection> findAll();

    /** Saves a connection, replacing any existing one for the same bank and country. */
    void save(Connection connection);

    void remove(String sessionId);
}
