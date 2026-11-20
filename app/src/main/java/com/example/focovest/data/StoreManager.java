package com.example.focovest.data;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.focovest.domain.PlanEngine;
import com.example.focovest.data.DataRepository.*;
import com.example.focovest.domain.PlanEngine.*;
import com.google.gson.Gson;
import java.util.*;

public class StoreManager {

    public static final String EDITAL_PLAN_ID = "edital";
    private static final String PREFS_NAME = "rumo_estudos_v2";
    private static final String STATE_KEY = "app_state_json";

    private static StoreManager instance;
    private final SharedPreferences prefs;
    private final Gson gson = new Gson();
    private State state;

    public static class SimuladoResult {
        public String id;
        public String simuladoId;
        public int correct;
        public int total;
        public String date;
        public int durationSec;
        public Map<String, Integer> answers = new HashMap<>();
        public List<String> questionIds = new ArrayList<>();
        public String simName;
        public String simExam;
    }

    public static class Announcement {
        public String id, text, author, date;
        public Announcement(String id, String text, String author, String date) {
            this.id = id; this.text = text; this.author = author; this.date = date;
        }
    }

    public static class State {
        public boolean profileCreated = false;
        public String name = "";
        public List<ExamId> targetExams = new ArrayList<>(Arrays.asList(ExamId.PSC, ExamId.SIS, ExamId.ENEM));
        public String targetCourse = "";
        public String mode = "autonoma"; // "autonoma" | "turma"
        public Map<String, String> notes = new HashMap<>();
        public Map<String, String> videos = new HashMap<>();
        public Map<String, Integer> answers = new HashMap<>();
        public List<String> completed = new ArrayList<>();
        public Map<String, String> completedAt = new HashMap<>();
        public List<Simulado> customSimulados = new ArrayList<>();
        public List<StudyPlan> plans = new ArrayList<>();
        public String activePlanId = EDITAL_PLAN_ID;
        public StudyPlan turmaPlan;
        public Map<String, String> topicStage = new HashMap<>();
        public int dailyGoal = 3;
        public List<Integer> studyDays = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
        public boolean turmaJoined = false;
        public int releasedWeeks = 2;
        public boolean organizerView = false;
        public List<Announcement> announcements = new ArrayList<>();
        public List<SimuladoResult> history = new ArrayList<>();
        public String city = "Manaus";
        public String lastTopic = "mat-funcoes";
    }

    private StoreManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        loadState();
    }

    public static synchronized StoreManager getInstance(Context context) {
        if (instance == null) {
            instance = new StoreManager(context);
        }
        return instance;
    }

    private State createDefaultState() {
        State s = new State();
        Date today = PlanEngine.startOfDay(new Date());
        int offset = -7 - ((PlanEngine.getJsDayOfWeek(today) + 6) % 7);
        String startTurma = PlanEngine.dayKey(PlanEngine.addDays(today, offset));
        s.turmaPlan = new StudyPlan("turma", DataRepository.TURMA_NAME,
                Arrays.asList(ExamId.PSC, ExamId.SIS), null, 2, Arrays.asList(1, 2, 3, 4, 5), startTurma);
        s.announcements.add(new Announcement("a1",
                "Simulado de Ciências da Natureza liberado! Façam até domingo para entrar no ranking da turma.",
                "Profa. Ana Ribeiro", PlanEngine.dayKey(PlanEngine.addDays(today, -1))));
        return s;
    }

    private void loadState() {
        String raw = prefs.getString(STATE_KEY, null);
        if (raw == null) {
            state = createDefaultState();
            return;
        }
        try {
            state = gson.fromJson(raw, State.class);
            if (state.targetExams == null || state.targetExams.isEmpty()) {
                state.targetExams = new ArrayList<>(Arrays.asList(ExamId.PSC, ExamId.SIS, ExamId.ENEM));
            }
        } catch (Exception e) {
            state = createDefaultState();
        }
    }

    public void save() {
        prefs.edit().putString(STATE_KEY, gson.toJson(state)).apply();
    }

    public State getState() {
        return state;
    }

    public void resetProfile() {
        state = createDefaultState();
        save();
    }

    public boolean inTurma() {
        return "turma".equals(state.mode) && state.turmaJoined;
    }

    public StudyPlan getActivePlan() {
        if (inTurma()) return state.turmaPlan;
        for (StudyPlan p : state.plans) {
            if (p.id.equals(state.activePlanId)) return p;
        }
        return new StudyPlan(EDITAL_PLAN_ID, "Plano do edital",
                state.targetExams, null, state.dailyGoal, state.studyDays, null);
    }

    public PlanResult getPlanResult() {
        return PlanEngine.buildPlan(getActivePlan(), state.completed, state.completedAt);
    }

    public int getPoints() {
        int correctAnswers = 0;
        for (Topic t : DataRepository.TOPICS) {
            if (state.completed.contains(t.id)) {
                for (Question q : t.questions) {
                    Integer chosen = state.answers.get(q.id);
                    if (chosen != null && chosen == q.answer) correctAnswers++;
                }
            }
        }
        Map<String, Integer> bestSims = new HashMap<>();
        for (SimuladoResult h : state.history) {
            int prev = bestSims.getOrDefault(h.simuladoId, 0);
            if (h.correct > prev) bestSims.put(h.simuladoId, h.correct);
        }
        int simPts = 0;
        for (int c : bestSims.values()) simPts += c * 15;

        return correctAnswers * 10 + state.completed.size() * 30 + simPts;
    }

    public void toggleCompleted(String topicId) {
        if (state.completed.contains(topicId)) {
            state.completed.remove(topicId);
            state.completedAt.remove(topicId);
        } else {
            state.completed.add(topicId);
            state.completedAt.put(topicId, PlanEngine.dayKey(new Date()));
        }
        save();
    }

    public boolean isTopicUnlocked(String topicId) {
        if ("autonoma".equals(state.mode) || !state.turmaJoined) return true;
        PlanResult turmaRes = PlanEngine.buildPlan(state.turmaPlan, state.completed, state.completedAt);
        Date turmaStart = PlanEngine.parseDay(state.turmaPlan.startDate);
        for (PlanDay d : turmaRes.days) {
            long diffDays = (d.date.getTime() - turmaStart.getTime()) / (1000L * 60 * 60 * 24);
            int weekIdx = (int) (diffDays / 7);
            if (d.topicIds.contains(topicId)) {
                return weekIdx < state.releasedWeeks;
            }
        }
        return false;
    }

    public int[] getSubjectAccuracy(SubjectId sub) {
        int correct = 0, total = 0;
        for (Topic t : DataRepository.TOPICS) {
            if (t.subject == sub && state.completed.contains(t.id)) {
                for (Question q : t.questions) {
                    if (state.answers.containsKey(q.id)) {
                        total++;
                        if (state.answers.get(q.id) == q.answer) correct++;
                    }
                }
            }
        }
        return new int[]{correct, total};
    }

    public List<Simulado> getAllSimulados() {
        List<Simulado> all = new ArrayList<>(DataRepository.SIMULADOS);
        all.addAll(state.customSimulados);
        return all;
    }
}
