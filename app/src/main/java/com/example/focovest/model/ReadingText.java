package com.example.focovest.model;

import com.example.focovest.data.DataRepository.SubjectId;

public class ReadingText {
    public String id;
    public String topicId;
    public SubjectId subject;
    public String title;
    public String author;
    public int readMinutes;
    public String body;

    public ReadingText() {}

    public ReadingText(String id, String topicId, SubjectId subject,
                       String title, String author, int readMinutes, String body) {
        this.id = id;
        this.topicId = topicId;
        this.subject = subject;
        this.title = title;
        this.author = author;
        this.readMinutes = readMinutes;
        this.body = body;
    }
}
