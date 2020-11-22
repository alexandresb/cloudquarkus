package org.cesi.chapter08;

import java.util.StringTokenizer;

public class CustomConfigProperty {

    private final String email;
    private final String user;


    public CustomConfigProperty(String email, String user) {
        this.email = email;
        this.user = user;

    }

    public String getEmail() {
        return email;
    }

    public String getUser() {
        return user;
    }
}
