package org.cesi.chapter04.integration;

public class CustomerException extends RuntimeException {
    public CustomerException() {}

    public CustomerException(String not_found) {
        super(not_found);
    }
}
