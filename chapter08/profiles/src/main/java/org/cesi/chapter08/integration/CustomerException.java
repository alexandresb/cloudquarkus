package org.cesi.chapter08.integration;

public class CustomerException extends RuntimeException {
    public CustomerException() {}

    public CustomerException(String not_found) {
        super(not_found);
    }
}
