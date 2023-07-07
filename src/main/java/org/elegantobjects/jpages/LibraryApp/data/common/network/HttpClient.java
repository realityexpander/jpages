package org.elegantobjects.jpages.LibraryApp.data.common.network;

public class HttpClient {
    private final String client;

    public
    HttpClient(String client) {
        this.client = client;
    }
    public
    HttpClient() {
        this.client = "Mozilla/5.0";
    }
}