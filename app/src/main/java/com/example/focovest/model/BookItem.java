package com.example.focovest.model;

import com.example.focovest.data.DataRepository.ExamId;
import com.example.focovest.data.DataRepository.SubjectId;

public class BookItem {
    public String id;
    public String title;
    public String author;
    public ExamId exam;
    public SubjectId subject;
    public String synopsis;
    public String referenceUrl;

    public BookItem() {}

    public BookItem(String id, String title, String author, ExamId exam,
                    SubjectId subject, String synopsis, String referenceUrl) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.exam = exam;
        this.subject = subject;
        this.synopsis = synopsis;
        this.referenceUrl = referenceUrl;
    }
}
