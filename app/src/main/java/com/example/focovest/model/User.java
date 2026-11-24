package com.example.focovest.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class User {

    public static final List<String> AM_CITIES = Collections.unmodifiableList(Arrays.asList(
            "Manaus", "Parintins", "Itacoatiara", "Manacapuru", "Coari", "Tefé",
            "Tabatinga", "Humaitá", "Maués", "Benjamin Constant", "Lábrea",
            "São Gabriel da Cachoeira", "Presidente Figueiredo", "Boca do Acre",
            "Eirunepé", "Iranduba", "Borba", "Careiro"
    ));

    public String id;
    public String name;
    public String city;
    public String targetCourse;
    public boolean inHighSchool;
    public int highSchoolYear; // 0 = concluído, 1 = 1ª série, 2 = 2ª série, 3 = 3ª série
    public List<ExamId> targetExams;
    public int dailyHours;
    public List<Integer> studyDays;
    public boolean profileCompleted;
    public String lastTopicId;

    public User(String id, String name, String city, String targetCourse,
                boolean inHighSchool, int highSchoolYear,
                List<ExamId> targetExams, int dailyHours, List<Integer> studyDays,
                boolean profileCompleted, String lastTopicId) {
        this.id = id;
        this.name = name != null ? name : "";
        this.city = city != null ? city : "Manaus";
        this.targetCourse = targetCourse != null ? targetCourse : "";
        this.inHighSchool = inHighSchool;
        this.highSchoolYear = inHighSchool
                ? Math.max(1, Math.min(3, highSchoolYear == 0 ? 3 : highSchoolYear))
                : 0;
        List<ExamId> eligible = ExamId.filterEligible(targetExams, this.inHighSchool, this.highSchoolYear);
        if (eligible.isEmpty()) {
            eligible = ExamId.availableFor(this.inHighSchool, this.highSchoolYear);
        }
        this.targetExams = new ArrayList<>(eligible);
        this.dailyHours = Math.max(1, dailyHours);
        this.studyDays = studyDays != null && !studyDays.isEmpty()
                ? new ArrayList<>(studyDays)
                : new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
        this.profileCompleted = profileCompleted;
        this.lastTopicId = lastTopicId;
    }

    public String getSchoolingLabel() {
        if (!inHighSchool) {
            return "Ensino Médio concluído";
        }
        return highSchoolYear + "ª série do Ensino Médio";
    }

    public String getExamLabel(ExamId exam) {
        return exam != null ? exam.getDisplayLabel(inHighSchool, highSchoolYear) : "";
    }

    public String getExamDesc(ExamId exam) {
        return exam != null ? exam.getContextualDesc(inHighSchool, highSchoolYear) : "";
    }

    public List<ExamId> getAvailableExams() {
        return ExamId.availableFor(inHighSchool, highSchoolYear);
    }

    public static List<Integer> parseStudyDays(String csv) {
        List<Integer> list = new ArrayList<>();
        if (csv != null && !csv.trim().isEmpty()) {
            for (String token : csv.split(",")) {
                try {
                    int day = Integer.parseInt(token.trim());
                    if (day >= 0 && day <= 6 && !list.contains(day)) {
                        list.add(day);
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        Collections.sort(list);
        if (list.isEmpty()) {
            list.addAll(Arrays.asList(1, 2, 3, 4, 5));
        }
        return list;
    }

    public static String studyDaysToCsv(List<Integer> days) {
        if (days == null || days.isEmpty()) return "1,2,3,4,5";
        List<Integer> sorted = new ArrayList<>(days);
        Collections.sort(sorted);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sorted.size(); i++) {
            int d = sorted.get(i);
            if (d < 0 || d > 6) continue;
            if (sb.length() > 0) sb.append(",");
            sb.append(d);
        }
        return sb.length() > 0 ? sb.toString() : "1,2,3,4,5";
    }
}
