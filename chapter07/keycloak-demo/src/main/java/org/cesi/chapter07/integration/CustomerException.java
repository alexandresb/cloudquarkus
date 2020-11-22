package org.cesi.chapter07.integration;

public class CustomerException extends RuntimeException {
    public CustomerException() {}

    public CustomerException(String not_found) {
        super(not_found);
    }
}
