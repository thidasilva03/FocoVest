package com.example.focovest.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.focovest.data.DataRepository.SubjectId;
import com.example.focovest.domain.PlanEngine;
import com.example.focovest.model.AccessHistoryItem;
import com.example.focovest.model.ExamEvent;
import com.example.focovest.model.ForumQuestion;
import com.example.focovest.model.ForumReply;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Gerenciador de persistência complementar (Calendário customizado, Fórum, Flashcards e Histórico de Acessos).
 * Mantém seus dados isolados em uma chave própria para não alterar o StoreManager existente.
 */
public class ExtrasStoreManager {

    private static final String PREFS_NAME = "focovest_extras_v1";
    private static final String STATE_KEY = "extras_state_json";
    private static final int MAX_HISTORY_ITEMS = 50;

    private static ExtrasStoreManager instance;
    private final SharedPreferences prefs;
    private final Gson gson = new Gson();
    private ExtrasState state;

    public static class ExtrasState {
        public List<ExamEvent> customEvents = new ArrayList<>();
        public List<ForumQuestion> forumQuestions = new ArrayList<>();
        public List<String> masteredFlashcards = new ArrayList<>();
        public List<String> readTextIds = new ArrayList<>();
        public List<AccessHistoryItem> accessHistory = new ArrayList<>();
    }

    private ExtrasStoreManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        loadState();
    }

    public static synchronized ExtrasStoreManager getInstance(Context context) {
        if (instance == null) {
            instance = new ExtrasStoreManager(context);
        }
        return instance;
    }

    private ExtrasState createDefaultState() {
        ExtrasState s = new ExtrasState();
        String todayKey = PlanEngine.dayKey(new Date());
        s.forumQuestions.addAll(ExtrasRepository.createSeedForumQuestions(todayKey));
        return s;
    }

    private void loadState() {
        String raw = prefs.getString(STATE_KEY, null);
        if (raw == null) {
            state = createDefaultState();
            return;
        }
        try {
            state = gson.fromJson(raw, ExtrasState.class);
            if (state == null) state = createDefaultState();
            if (state.customEvents == null) state.customEvents = new ArrayList<>();
            if (state.forumQuestions == null) state.forumQuestions = new ArrayList<>();
            if (state.masteredFlashcards == null) state.masteredFlashcards = new ArrayList<>();
            if (state.readTextIds == null) state.readTextIds = new ArrayList<>();
            if (state.accessHistory == null) state.accessHistory = new ArrayList<>();
        } catch (Exception e) {
            state = createDefaultState();
        }
    }

    public void save() {
        prefs.edit().putString(STATE_KEY, gson.toJson(state)).apply();
    }

    public ExtrasState getState() {
        return state;
    }

    public void resetAll() {
        state = createDefaultState();
        save();
    }

    public void addCustomEvent(ExamEvent event) {
        state.customEvents.add(0, event);
        save();
    }

    public void removeCustomEvent(String eventId) {
        state.customEvents.removeIf(e -> e.id.equals(eventId));
        save();
    }

    public void addForumQuestion(ForumQuestion question) {
        state.forumQuestions.add(0, question);
        save();
    }

    public void addForumReply(String questionId, ForumReply reply) {
        for (ForumQuestion q : state.forumQuestions) {
            if (q.id.equals(questionId)) {
                q.replies.add(reply);
                save();
                break;
            }
        }
    }

    public boolean isFlashcardMastered(String flashcardId) {
        return state.masteredFlashcards.contains(flashcardId);
    }

    public void toggleFlashcardMastered(String flashcardId) {
        if (state.masteredFlashcards.contains(flashcardId)) {
            state.masteredFlashcards.remove(flashcardId);
        } else {
            state.masteredFlashcards.add(flashcardId);
        }
        save();
    }

    public void markTextRead(String textId) {
        if (!state.readTextIds.contains(textId)) {
            state.readTextIds.add(textId);
            save();
        }
    }

    public void recordAccess(String materialType, String materialId, String title, SubjectId subject) {
        String timestamp = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("pt", "BR")).format(new Date());
        // Evita duplicar o item imediatamente no topo
        state.accessHistory.removeIf(item ->
                materialType.equals(item.materialType) && materialId.equals(item.materialId));
        state.accessHistory.add(0, new AccessHistoryItem(
                UUID.randomUUID().toString(),
                materialType,
                materialId,
                title,
                subject,
                timestamp
        ));
        if (state.accessHistory.size() > MAX_HISTORY_ITEMS) {
            state.accessHistory = new ArrayList<>(state.accessHistory.subList(0, MAX_HISTORY_ITEMS));
        }
        save();
    }

    public void clearAccessHistory() {
        state.accessHistory.clear();
        save();
    }
}
