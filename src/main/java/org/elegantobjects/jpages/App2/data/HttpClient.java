package org.elegantobjects.jpages.App2.data;

public class HttpClient {
    private final String client;

    HttpClient(String client) {
        this.client = client;
    }

    public HttpClient() {
        this.client = "Mozilla/5.0";
    }
}