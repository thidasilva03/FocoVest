package com.example.focovest.model;

import com.example.focovest.data.DataRepository.ExamId;

public class PastExam {
    public String id;
    public ExamId exam;
    public int year;
    public String stageLabel;
    public String description;
    public String pdfUrl;
    public String answerKeyUrl;

    public PastExam() {}

    public PastExam(String id, ExamId exam, int year, String stageLabel,
                    String description, String pdfUrl, String answerKeyUrl) {
        this.id = id;
        this.exam = exam;
        this.year = year;
        this.stageLabel = stageLabel;
        this.description = description;
        this.pdfUrl = pdfUrl;
        this.answerKeyUrl = answerKeyUrl;
    }
}
