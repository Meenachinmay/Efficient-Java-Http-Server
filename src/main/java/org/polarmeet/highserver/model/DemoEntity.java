package org.polarmeet.highserver.model;

public class DemoEntity {
    private String id;
    private String content;
    private String status;

    // Constructors
    public DemoEntity() {}

    public DemoEntity(String id, String content) {
        this.id = id;
        this.content = content;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // toString method for logging
    @Override
    public String toString() {
        return "DemoEntity{" +
                "id='" + id + '\'' +
                ", content='" + content + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}