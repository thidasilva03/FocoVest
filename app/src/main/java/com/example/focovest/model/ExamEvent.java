package com.example.focovest.model;

import com.example.focovest.data.DataRepository.ExamId;

public class ExamEvent {
    public String id;
    public String title;
    public String description;
    public ExamId exam; // pode ser null para eventos pessoais
    public String dateKey; // yyyy-MM-dd
    public String timeLabel; // ex: "08:00 - 13:00"
    public String category; // "prova" | "inscricao" | "resultado" | "pessoal"
    public boolean custom;

    public ExamEvent() {}

    public ExamEvent(String id, String title, String description, ExamId exam,
                     String dateKey, String timeLabel, String category, boolean custom) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.exam = exam;
        this.dateKey = dateKey;
        this.timeLabel = timeLabel;
        this.category = category;
        this.custom = custom;
    }
}
