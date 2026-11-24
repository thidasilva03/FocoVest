package com.example.focovest.domain;

import com.example.focovest.model.ExamId;
import com.example.focovest.model.SubjectId;
import com.example.focovest.model.Topic;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Motor de planejamento de estudos dedicado por objetivo (ENEM, PSC, SIS ou Macro).
 * Utiliza exclusivamente o peso de incidência da prova selecionada (weight_*).
 */
public class PlanEngine {

    public static final int TOPIC_MINUTES_AVG = 55;
    private static final int MAX_SCHEDULE_DAYS = 400;
    private static final int MAX_CALENDAR_ITERATIONS = 1000;

    public static class PlanDay {
        public final Date date;
        public final List<Topic> topics;

        public PlanDay(Date date, List<Topic> topics) {
            this.date = date;
            this.topics = topics;
        }
    }

    public static class PlanResult {
        public final ExamId exam;
        public final List<Topic> edital;
        public final List<Topic> completedTopics;
        public final List<PlanDay> days;
        public final Date endDate;
        public final int perDay;

        public PlanResult(ExamId exam, List<Topic> edital, List<Topic> completedTopics,
                          List<PlanDay> days, Date endDate, int perDay) {
            this.exam = exam;
            this.edital = edital;
            this.completedTopics = completedTopics;
            this.days = days;
            this.endDate = endDate;
            this.perDay = perDay;
        }
    }

    public static String getIncidenceLabel(int tier) {
        if (tier >= 3) return "Alta";
        if (tier == 2) return "Média";
        return "Baixa";
    }

    public static Date startOfDay(Date d) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static Date addDays(Date d, int n) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(startOfDay(d));
        cal.add(Calendar.DAY_OF_MONTH, n);
        return cal.getTime();
    }

    public static String dayKey(Date d) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(d);
    }

    public static String fmtShort(Date d) {
        return new SimpleDateFormat("EEE, dd/MM", new Locale("pt", "BR")).format(d);
    }

    public static int getDayOfWeekIndex(Date d) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        return cal.get(Calendar.DAY_OF_WEEK) - 1; // 0 = Domingo .. 6 = Sábado
    }

    public static int topicsPerDay(int hours) {
        return Math.max(1, (Math.max(1, hours) * 60) / TOPIC_MINUTES_AVG);
    }

    /**
     * Monta a lista ordenada do edital para UMA única prova e etapa (ENEM, PSC 1/2/3, SIS 1/2/3 ou Macro),
     * filtrando weight > 0 e intercalando disciplinas por ordem decrescente de incidência (3 → 2 → 1).
     */
    public static List<Topic> editalTopicsForExam(List<Topic> allTopics, ExamId exam, int highSchoolYear) {
        List<Topic> out = new ArrayList<>();
        if (allTopics == null || exam == null) return out;

        int[] tiers = {3, 2, 1};
        for (int tier : tiers) {
            Map<SubjectId, Queue<Topic>> bySubject = new EnumMap<>(SubjectId.class);
            for (SubjectId s : SubjectId.values()) {
                bySubject.put(s, new LinkedList<>());
            }
            for (Topic t : allTopics) {
                if (t.getWeight(exam, highSchoolYear) == tier) {
                    bySubject.get(t.subject).add(t);
                }
            }
            boolean hasRemaining = true;
            while (hasRemaining) {
                hasRemaining = false;
                for (SubjectId s : SubjectId.values()) {
                    Queue<Topic> q = bySubject.get(s);
                    if (q != null && !q.isEmpty()) {
                        out.add(q.poll());
                        if (!q.isEmpty()) hasRemaining = true;
                    }
                }
            }
        }
        return out;
    }

    public static List<Topic> editalTopicsForExam(List<Topic> allTopics, ExamId exam) {
        return editalTopicsForExam(allTopics, exam, 0);
    }

    /**
     * Agrupa os tópicos de um edital por disciplina na ordem oficial de SubjectId,
     * preservando a prioridade de incidência dentro de cada disciplina.
     */
    public static Map<SubjectId, List<Topic>> groupBySubject(List<Topic> editalTopics) {
        Map<SubjectId, List<Topic>> grouped = new EnumMap<>(SubjectId.class);
        if (editalTopics == null) return grouped;
        for (Topic t : editalTopics) {
            List<Topic> list = grouped.get(t.subject);
            if (list == null) {
                list = new ArrayList<>();
                grouped.put(t.subject, list);
            }
            list.add(t);
        }
        return grouped;
    }

    /**
     * Gera o cronograma dedicado para o objetivo e etapa escolar informados, respeitando horas diárias,
     * dias de estudo e tópicos já concluídos pelo aluno.
     */
    public static PlanResult buildPlanForExam(ExamId exam, int highSchoolYear, List<Topic> allTopics,
                                              int dailyHours, List<Integer> studyDays,
                                              Set<String> completedIds,
                                              Map<String, String> completedAtMap) {
        Date today = startOfDay(new Date());
        List<Topic> edital = editalTopicsForExam(allTopics, exam, highSchoolYear);
        Set<String> done = completedIds != null ? completedIds : new HashSet<>();

        int safeHours = Math.max(1, dailyHours);
        int capMinutes = safeHours * 60;
        int perDay = topicsPerDay(safeHours);

        List<Topic> completedTopics = new ArrayList<>();
        List<Topic> pendingQueue = new ArrayList<>();
        for (Topic t : edital) {
            if (done.contains(t.id)) {
                completedTopics.add(t);
            } else {
                pendingQueue.add(t);
            }
        }

        int usedToday = 0;
        String todayStr = dayKey(today);
        if (completedAtMap != null) {
            for (Topic t : completedTopics) {
                String cDate = completedAtMap.get(t.id);
                if (cDate != null && cDate.startsWith(todayStr)) {
                    usedToday += t.minutes;
                }
            }
        }

        Set<Integer> validDays = new HashSet<>();
        if (studyDays != null) {
            for (Integer d : studyDays) {
                if (d != null && d >= 0 && d <= 6) validDays.add(d);
            }
        }
        if (validDays.isEmpty()) {
            for (int i = 1; i <= 5; i++) validDays.add(i);
        }

        List<PlanDay> days = new ArrayList<>();
        int qi = 0;
        int iterations = 0;

        for (Date d = today; qi < pendingQueue.size()
                && days.size() < MAX_SCHEDULE_DAYS
                && iterations < MAX_CALENDAR_ITERATIONS; d = addDays(d, 1), iterations++) {
            if (!validDays.contains(getDayOfWeekIndex(d))) continue;

            int used = dayKey(d).equals(todayStr) ? usedToday : 0;
            List<Topic> dayTopics = new ArrayList<>();
            while (qi < pendingQueue.size()
                    && (used + pendingQueue.get(qi).minutes <= capMinutes || (dayTopics.isEmpty() && used == 0))) {
                Topic next = pendingQueue.get(qi);
                dayTopics.add(next);
                used += next.minutes;
                qi++;
            }
            if (!dayTopics.isEmpty()) {
                days.add(new PlanDay(d, dayTopics));
            }
        }

        Date endDate = days.isEmpty() ? null : days.get(days.size() - 1).date;
        return new PlanResult(exam, edital, completedTopics, days, endDate, perDay);
    }
}
