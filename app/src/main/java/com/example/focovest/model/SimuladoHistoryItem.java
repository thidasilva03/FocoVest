package com.example.focovest.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimuladoHistoryItem {
    public final String id;
    public final String userId;
    public final String examFilter;
    public final String title;
    public final int correctCount;
    public final int totalCount;
    public final int durationSec;
    public final String createdAt;
    public final Map<String, Integer> answers;
    public final List<String> questionIds;

    public SimuladoHistoryItem(String id, String userId, String examFilter, String title,
                               int correctCount, int totalCount, int durationSec, String createdAt,
                               Map<String, Integer> answers, List<String> questionIds) {
        this.id = id;
        this.userId = userId;
        this.examFilter = examFilter;
        this.title = title;
        this.correctCount = correctCount;
        this.totalCount = totalCount;
        this.durationSec = durationSec;
        this.createdAt = createdAt;
        this.answers = answers != null ? new HashMap<>(answers) : new HashMap<>();
        this.questionIds = questionIds != null ? new ArrayList<>(questionIds) : new ArrayList<>();
    }
}
