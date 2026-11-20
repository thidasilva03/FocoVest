package com.example.focovest.model;

public class ForumReply {
    public String id;
    public String authorName;
    public boolean isProfessor;
    public String body;
    public String dateKey; // yyyy-MM-dd

    public ForumReply() {}

    public ForumReply(String id, String authorName, boolean isProfessor, String body, String dateKey) {
        this.id = id;
        this.authorName = authorName;
        this.isProfessor = isProfessor;
        this.body = body;
        this.dateKey = dateKey;
    }
}
