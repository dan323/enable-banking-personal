module com.personal.banking.adapter.enablebanking {
    requires com.personal.banking.port;

    // Constructors of the public adapter classes take ObjectMapper and RestClient.Builder.
    requires transitive tools.jackson.databind;
    requires spring.web;          // automatic module (name derived from spring-web.jar)

    exports com.personal.banking.adapter.out.enablebanking;
}
