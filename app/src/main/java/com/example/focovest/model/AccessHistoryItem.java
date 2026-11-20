package com.example.focovest.model;

import com.example.focovest.data.DataRepository.SubjectId;

public class AccessHistoryItem {
    public String id;
    public String materialType; // "flashcard" | "texto" | "livro" | "prova" | "video" | "simulado"
    public String materialId;
    public String title;
    public SubjectId subject; // pode ser null (ex: prova geral)
    public String accessedAt; // dd/MM/yyyy HH:mm

    public AccessHistoryItem() {}

    public AccessHistoryItem(String id, String materialType, String materialId,
                             String title, SubjectId subject, String accessedAt) {
        this.id = id;
        this.materialType = materialType;
        this.materialId = materialId;
        this.title = title;
        this.subject = subject;
        this.accessedAt = accessedAt;
    }
}
