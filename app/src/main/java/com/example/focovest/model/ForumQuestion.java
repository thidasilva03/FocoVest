package com.example.focovest.model;

import com.example.focovest.data.DataRepository.ExamId;
import com.example.focovest.data.DataRepository.SubjectId;
import java.util.ArrayList;
import java.util.List;

public class ForumQuestion {
    public String id;
    public String title;
    public String body;
    public String authorName;
    public String authorCity;
    public ExamId exam;
    public SubjectId subject;
    public String dateKey; // yyyy-MM-dd
    public boolean mine;
    public List<ForumReply> replies = new ArrayList<>();

    public ForumQuestion() {}

    public ForumQuestion(String id, String title, String body, String authorName, String authorCity,
                         ExamId exam, SubjectId subject, String dateKey, boolean mine,
                         List<ForumReply> replies) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.authorName = authorName;
        this.authorCity = authorCity;
        this.exam = exam;
        this.subject = subject;
        this.dateKey = dateKey;
        this.mine = mine;
        this.replies = replies != null ? new ArrayList<>(replies) : new ArrayList<>();
    }
}
