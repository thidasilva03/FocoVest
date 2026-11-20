package com.example.focovest.domain;

import com.example.focovest.data.DataRepository;
import com.example.focovest.data.DataRepository.ExamId;
import com.example.focovest.data.DataRepository.SubjectId;
import com.example.focovest.data.DataRepository.Topic;
import java.text.SimpleDateFormat;
import java.util.*;

public class PlanEngine {

    public static final int TOPIC_MINUTES_AVG = 55;

    public static class StudyPlan {
        public String id;
        public String name;
        public List<ExamId> exams;
        public String deadline; // YYYY-MM-DD ou null
        public int dailyHours;
        public List<Integer> studyDays; // 0 = Dom .. 6 = Sáb
        public String startDate; // YYYY-MM-DD ou null

        public StudyPlan(String id, String name, List<ExamId> exams, String deadline,
                         int dailyHours, List<Integer> studyDays, String startDate) {
            this.id = id;
            this.name = name;
            this.exams = exams;
            this.deadline = deadline;
            this.dailyHours = dailyHours;
            this.studyDays = studyDays;
            this.startDate = startDate;
        }
    }

    public static class PlanDay {
        public Date date;
        public List<String> topicIds;
        public PlanDay(Date date, List<String> topicIds) {
            this.date = date;
            this.topicIds = topicIds;
        }
    }

    public static class PlanResult {
        public List<Topic> edital;
        public List<String> topicIds;
        public List<PlanDay> days;
        public List<Topic> dropped;
        public Date endDate;
        public int perDay;

        public PlanResult(List<Topic> edital, List<String> topicIds, List<PlanDay> days,
                          List<Topic> dropped, Date endDate, int perDay) {
            this.edital = edital;
            this.topicIds = topicIds;
            this.days = days;
            this.dropped = dropped;
            this.endDate = endDate;
            this.perDay = perDay;
        }
    }

    public static String getIncidenceLabel(int tier) {
        if (tier == 3) return "Alta";
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

    public static Date parseDay(String key) {
        try {
            return startOfDay(new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(key));
        } catch (Exception e) {
            return startOfDay(new Date());
        }
    }

    public static String fmtShort(Date d) {
        return new SimpleDateFormat("EEE, dd/MM", new Locale("pt", "BR")).format(d);
    }

    public static int getJsDayOfWeek(Date d) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        return cal.get(Calendar.DAY_OF_WEEK) - 1; // 0 = Domingo .. 6 = Sábado
    }

    public static int topicsPerDay(int hours) {
        return Math.max(1, (hours * 60) / TOPIC_MINUTES_AVG);
    }

    public static int incidenceOf(Topic topic, List<ExamId> exams) {
        int max = 0;
        for (ExamId e : exams) {
            Integer v = topic.incidence.get(e);
            if (v != null && v > max) max = v;
        }
        return max;
    }

    public static List<Topic> editalTopics(List<ExamId> exams) {
        List<Topic> out = new ArrayList<>();
        int[] tiers = {3, 2, 1};
        for (int tier : tiers) {
            Map<SubjectId, Queue<Topic>> bySubject = new EnumMap<>(SubjectId.class);
            for (SubjectId s : SubjectId.values()) bySubject.put(s, new LinkedList<>());
            for (Topic t : DataRepository.TOPICS) {
                if (incidenceOf(t, exams) == tier) {
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

    public static PlanResult buildPlan(StudyPlan plan, List<String> completed, Map<String, String> completedAt) {
        Date today = startOfDay(new Date());
        List<Topic> edital = editalTopics(plan.exams);
        boolean fixed = plan.startDate != null && !plan.startDate.isEmpty();
        Date start = fixed ? parseDay(plan.startDate) : today;
        Date deadline = (plan.deadline != null && !plan.deadline.isEmpty()) ? parseDay(plan.deadline) : null;
        int cap = plan.dailyHours * 60;
        int perDay = topicsPerDay(plan.dailyHours);
        Set<String> done = new HashSet<>(completed);

        List<Topic> queue = new ArrayList<>();
        for (Topic t : edital) {
            if (fixed || !done.contains(t.id)) queue.add(t);
        }

        int usedToday = 0;
        if (!fixed) {
            String todayStr = dayKey(today);
            for (Topic t : edital) {
                if (done.contains(t.id) && completedAt.containsKey(t.id)) {
                    String cDate = completedAt.get(t.id);
                    if (cDate != null && cDate.startsWith(todayStr)) {
                        usedToday += t.minutes;
                    }
                }
            }
        }

        List<PlanDay> days = new ArrayList<>();
        Set<String> scheduled = new HashSet<>();
        int qi = 0;

        if (plan.studyDays != null && !plan.studyDays.isEmpty()) {
            for (Date d = start; qi < queue.size(); d = addDays(d, 1)) {
                if (deadline != null && !d.before(deadline)) break;
                if (days.size() > 400) break;
                if (!plan.studyDays.contains(getJsDayOfWeek(d))) continue;

                int used = dayKey(d).equals(dayKey(today)) ? usedToday : 0;
                List<String> ids = new ArrayList<>();
                while (qi < queue.size() && (used + queue.get(qi).minutes <= cap || (ids.isEmpty() && used == 0))) {
                    ids.add(queue.get(qi).id);
                    scheduled.add(queue.get(qi).id);
                    used += queue.get(qi).minutes;
                    qi++;
                }
                if (!ids.isEmpty()) days.add(new PlanDay(d, ids));
            }
        }

        List<String> topicIds = new ArrayList<>();
        for (Topic t : edital) {
            if (scheduled.contains(t.id) || (!fixed && done.contains(t.id))) {
                topicIds.add(t.id);
            }
        }

        List<Topic> dropped = new ArrayList<>();
        for (Topic t : queue) {
            if (!scheduled.contains(t.id)) dropped.add(t);
        }

        Date endDate = days.isEmpty() ? null : days.get(days.size() - 1).date;
        return new PlanResult(edital, topicIds, days, dropped, endDate, perDay);
    }
}
