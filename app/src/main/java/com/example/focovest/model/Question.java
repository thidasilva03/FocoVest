package com.example.focovest.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Question {
    public final String id;
    public final String topicId;
    public final SubjectId subject;
    public final String prompt;
    public final List<String> options;
    public final int correctIndex;
    public final String explanation;

    public Question(String id, String topicId, SubjectId subject, String prompt,
                    String optA, String optB, String optC, String optD, String optE,
                    int correctIndex, String explanation) {
        this.id = id;
        this.topicId = topicId;
        this.subject = subject;
        this.prompt = prompt;
        this.options = Collections.unmodifiableList(Arrays.asList(optA, optB, optC, optD, optE));
        this.correctIndex = correctIndex;
        this.explanation = explanation;
    }
}
