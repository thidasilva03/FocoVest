package com.example.focovest.model;

public class UserTopicProgress {
    public final String userId;
    public final String topicId;
    public boolean completed;
    public String completedAt;
    public String stage; // "aula" | "exercicios" | "diagnostico"
    public String notes;
    public String customVideoUrl;

    public UserTopicProgress(String userId, String topicId, boolean completed, String completedAt,
                             String stage, String notes, String customVideoUrl) {
        this.userId = userId;
        this.topicId = topicId;
        this.completed = completed;
        this.completedAt = completedAt;
        this.stage = (stage == null || stage.isEmpty()) ? "aula" : stage;
        this.notes = notes != null ? notes : "";
        this.customVideoUrl = customVideoUrl;
    }
}
