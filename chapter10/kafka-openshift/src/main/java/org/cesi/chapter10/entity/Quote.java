package org.cesi.chapter10.entity;

import io.quarkus.runtime.annotations.RegisterForReflection;

//stock quote = cotation commerciale
@RegisterForReflection
public class Quote {
    private Company company;
    private Double value;

    public Quote() {
    }

    public Quote(Company company, Double value) {
        this.company = company;
        this.value = value;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }
}
