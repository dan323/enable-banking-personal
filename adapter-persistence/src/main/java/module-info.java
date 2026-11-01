module com.personal.banking.adapter.persistence {
    requires com.personal.banking.port;
    requires transitive tools.jackson.databind;   // ObjectMapper is a constructor argument

    exports com.personal.banking.adapter.out.persistence;

    // Jackson reads/writes the private file-format records by reflection.
    opens com.personal.banking.adapter.out.persistence to tools.jackson.databind;
}
