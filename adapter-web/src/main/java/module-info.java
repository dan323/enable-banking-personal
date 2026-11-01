module com.personal.banking.adapter.web {
    requires com.personal.banking.port;
    requires spring.web;                          // automatic module (name derived from spring-web.jar)
    requires com.fasterxml.jackson.annotation;
    requires tools.jackson.databind;      // so the DTO package can be opened to it

    exports com.personal.banking.adapter.in.web;

    // Spring (core/beans/web) invokes the controllers by reflection; Jackson serialises the DTO records.
    opens com.personal.banking.adapter.in.web;
    opens com.personal.banking.adapter.in.web.dto to tools.jackson.databind;
}
