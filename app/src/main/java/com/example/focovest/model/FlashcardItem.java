package com.example.focovest.model;

import com.example.focovest.data.DataRepository.SubjectId;

public class FlashcardItem {
    public String id;
    public String topicId;
    public SubjectId subject;
    public String frontTitle;
    public String frontQuestion;
    public String backAnswer;
    public String tip;

    public FlashcardItem() {}

    public FlashcardItem(String id, String topicId, SubjectId subject,
                         String frontTitle, String frontQuestion, String backAnswer, String tip) {
        this.id = id;
        this.topicId = topicId;
        this.subject = subject;
        this.frontTitle = frontTitle;
        this.frontQuestion = frontQuestion;
        this.backAnswer = backAnswer;
        this.tip = tip;
    }
}
