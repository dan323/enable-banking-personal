module com.personal.banking.port {
    // Port signatures expose domain types, so consumers of the ports get the domain too.
    requires transitive com.personal.banking.domain;

    exports com.personal.banking.port.in;
    exports com.personal.banking.port.out;
}
