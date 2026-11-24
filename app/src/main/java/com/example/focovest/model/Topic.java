package com.example.focovest.model;

public class Topic {
    public final String id;
    public final SubjectId subject;
    public final String title;
    public final String summary;
    public final int minutes;
    public final String defaultVideoUrl;
    public final int weightEnem;
    public final int weightPsc1;
    public final int weightPsc2;
    public final int weightPsc3;
    public final int weightSis1;
    public final int weightSis2;
    public final int weightSis3;
    public final int weightMacro;

    public Topic(String id, SubjectId subject, String title, String summary, int minutes,
                 String defaultVideoUrl, int weightEnem,
                 int weightPsc1, int weightPsc2, int weightPsc3,
                 int weightSis1, int weightSis2, int weightSis3,
                 int weightMacro) {
        this.id = id;
        this.subject = subject;
        this.title = title;
        this.summary = summary;
        this.minutes = minutes;
        this.defaultVideoUrl = defaultVideoUrl != null ? defaultVideoUrl : "";
        this.weightEnem = weightEnem;
        this.weightPsc1 = weightPsc1;
        this.weightPsc2 = weightPsc2;
        this.weightPsc3 = weightPsc3;
        this.weightSis1 = weightSis1;
        this.weightSis2 = weightSis2;
        this.weightSis3 = weightSis3;
        this.weightMacro = weightMacro;
    }

    public int getWeight(ExamId exam) {
        return getWeight(exam, 0);
    }

    /**
     * Retorna o peso de incidência do tópico na prova e etapa escolar correspondente:
     * - PSC e SIS usam a etapa dedicada à série (1, 2 ou 3);
     * - Macro e ENEM usam o conteúdo integral da prova.
     */
    public int getWeight(ExamId exam, int highSchoolYear) {
        if (exam == null) return 0;
        switch (exam) {
            case ENEM:
                return weightEnem;
            case PSC:
                if (highSchoolYear == 1) return weightPsc1;
                if (highSchoolYear == 2) return weightPsc2;
                if (highSchoolYear == 3) return weightPsc3;
                return Math.max(weightPsc1, Math.max(weightPsc2, weightPsc3));
            case SIS:
                if (highSchoolYear == 1) return weightSis1;
                if (highSchoolYear == 2) return weightSis2;
                if (highSchoolYear == 3) return weightSis3;
                return Math.max(weightSis1, Math.max(weightSis2, weightSis3));
            case Macro:
                return weightMacro;
            default:
                return 0;
        }
    }
}
