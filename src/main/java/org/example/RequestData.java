package org.example;

public class RequestData {
    private String path;
    private String id;
    private String body;
    private String signature;
    private String timestamp;

    public RequestData(String path, String id, String body, String signature, String timestamp) {
        this.path = path;
        this.id = id;
        this.body = body;
        this.signature = signature;
        this.timestamp = timestamp;
    }

    public String getPath() {
        return path;
    }

    public String getId() {
        return id;
    }

    public String getBody() {
        return body;
    }

    public String getSignature() {
        return signature;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
