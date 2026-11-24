package com.example.focovest.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.focovest.domain.PlanEngine;
import com.example.focovest.model.ExamId;
import com.example.focovest.model.Question;
import com.example.focovest.model.SimuladoHistoryItem;
import com.example.focovest.model.SubjectId;
import com.example.focovest.model.Topic;
import com.example.focovest.model.User;
import com.example.focovest.model.UserTopicProgress;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Repositório único 100% offline apoiado no SQLite embutido (FocoVestDbHelper).
 * Todo progresso fica salvo localmente no banco de dados do aparelho.
 */
public class FocoVestRepository {

    public static final String LOCAL_USER_ID = "local-user";

    public static final int POINTS_PER_FIXATION_CORRECT = 10;
    public static final int POINTS_PER_COMPLETED_TOPIC = 30;
    public static final int POINTS_PER_SIMULADO_CORRECT = 15;

    private static FocoVestRepository instance;
    private final FocoVestDbHelper dbHelper;

    private FocoVestRepository(Context context) {
        this.dbHelper = new FocoVestDbHelper(context.getApplicationContext());
    }

    public static synchronized FocoVestRepository getInstance(Context context) {
        if (instance == null) {
            instance = new FocoVestRepository(context);
        }
        return instance;
    }

    // =========================================================================
    // PERFIL LOCAL DO USUÁRIO
    // =========================================================================

    public User getOrCreateLocalUser() {
        User existing = getUserById(LOCAL_USER_ID);
        if (existing != null) {
            return existing;
        }
        ContentValues cv = new ContentValues();
        cv.put("id", LOCAL_USER_ID);
        cv.put("name", "");
        cv.put("city", "Manaus");
        cv.put("target_course", "");
        cv.put("in_high_school", 1);
        cv.put("high_school_year", 3);
        cv.put("target_exams", "ENEM,PSC,SIS");
        cv.put("daily_hours", 3);
        cv.put("study_days", "1,2,3,4,5");
        cv.put("profile_completed", 0);
        cv.put("last_topic_id", "mat-funcoes");

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.insertWithOnConflict(FocoVestDbHelper.TABLE_USERS, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
        return getUserById(LOCAL_USER_ID);
    }

    public User getUserById(String userId) {
        if (userId == null || userId.trim().isEmpty()) return null;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT id, name, city, target_course, in_high_school, high_school_year, "
                        + "target_exams, daily_hours, study_days, profile_completed, last_topic_id "
                        + "FROM " + FocoVestDbHelper.TABLE_USERS + " WHERE id = ? LIMIT 1",
                new String[]{userId})) {
            if (!c.moveToFirst()) return null;
            return readUserFromCursor(c);
        }
    }

    private User readUserFromCursor(Cursor c) {
        return new User(
                c.getString(0),
                c.getString(1),
                c.getString(2),
                c.getString(3),
                c.getInt(4) == 1,
                c.getInt(5),
                ExamId.parseCsv(c.getString(6)),
                c.getInt(7),
                User.parseStudyDays(c.getString(8)),
                c.getInt(9) == 1,
                c.getString(10)
        );
    }

    public void updateUserOnboarding(String userId, String name, String city, String targetCourse,
                                     boolean inHighSchool, int highSchoolYear,
                                     List<ExamId> targetExams, int dailyHours, List<Integer> studyDays) {
        int normalizedYear = inHighSchool ? Math.max(1, Math.min(3, highSchoolYear)) : 0;
        List<ExamId> validExams = ExamId.filterEligible(targetExams, inHighSchool, normalizedYear);
        if (validExams.isEmpty()) {
            validExams = ExamId.availableFor(inHighSchool, normalizedYear);
        }

        ContentValues cv = new ContentValues();
        cv.put("name", name.trim());
        cv.put("city", city);
        cv.put("target_course", targetCourse != null ? targetCourse.trim() : "");
        cv.put("in_high_school", inHighSchool ? 1 : 0);
        cv.put("high_school_year", normalizedYear);
        cv.put("target_exams", ExamId.toCsv(validExams));
        cv.put("daily_hours", Math.max(1, dailyHours));
        cv.put("study_days", User.studyDaysToCsv(studyDays));
        cv.put("profile_completed", 1);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.update(FocoVestDbHelper.TABLE_USERS, cv, "id = ?", new String[]{userId});
    }

    public void updateUserProfile(String userId, String name, String city,
                                  String targetCourse, boolean inHighSchool, int highSchoolYear,
                                  List<ExamId> targetExams) {
        int normalizedYear = inHighSchool ? Math.max(1, Math.min(3, highSchoolYear)) : 0;
        List<ExamId> validExams = ExamId.filterEligible(targetExams, inHighSchool, normalizedYear);
        if (validExams.isEmpty()) {
            validExams = ExamId.availableFor(inHighSchool, normalizedYear);
        }

        ContentValues cv = new ContentValues();
        if (name != null && !name.trim().isEmpty()) {
            cv.put("name", name.trim());
        }
        cv.put("city", city);
        cv.put("target_course", targetCourse != null ? targetCourse.trim() : "");
        cv.put("in_high_school", inHighSchool ? 1 : 0);
        cv.put("high_school_year", normalizedYear);
        cv.put("target_exams", ExamId.toCsv(validExams));

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.update(FocoVestDbHelper.TABLE_USERS, cv, "id = ?", new String[]{userId});
    }

    public void updateUserRhythm(String userId, int dailyHours, List<Integer> studyDays) {
        ContentValues cv = new ContentValues();
        cv.put("daily_hours", Math.max(1, dailyHours));
        cv.put("study_days", User.studyDaysToCsv(studyDays));

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.update(FocoVestDbHelper.TABLE_USERS, cv, "id = ?", new String[]{userId});
    }

    public void updateLastTopic(String userId, String topicId) {
        if (userId == null || topicId == null) return;
        ContentValues cv = new ContentValues();
        cv.put("last_topic_id", topicId);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.update(FocoVestDbHelper.TABLE_USERS, cv, "id = ?", new String[]{userId});
    }

    /**
     * Apaga exclusivamente o progresso do usuário logado (tópicos, respostas, notas e simulados),
     * preservando a conta e os dados de outras contas no aparelho.
     */
    public void resetUserProgress(String userId) {
        if (userId == null) return;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(FocoVestDbHelper.TABLE_USER_TOPIC_PROGRESS, "user_id = ?", new String[]{userId});
            db.delete(FocoVestDbHelper.TABLE_USER_QUESTION_ANSWERS, "user_id = ?", new String[]{userId});
            db.delete(FocoVestDbHelper.TABLE_SIMULADO_HISTORY, "user_id = ?", new String[]{userId});

            ContentValues cv = new ContentValues();
            cv.put("last_topic_id", "mat-funcoes");
            db.update(FocoVestDbHelper.TABLE_USERS, cv, "id = ?", new String[]{userId});

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    // =========================================================================
    // TÓPICOS E QUESTÕES
    // =========================================================================

    public List<Topic> getAllTopics() {
        List<Topic> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT id, subject, title, summary, minutes, default_video_url, "
                        + "weight_enem, weight_psc1, weight_psc2, weight_psc3, "
                        + "weight_sis1, weight_sis2, weight_sis3, weight_macro "
                        + "FROM " + FocoVestDbHelper.TABLE_TOPICS + " ORDER BY rowid ASC",
                null)) {
            while (c.moveToNext()) {
                list.add(readTopic(c));
            }
        }
        return list;
    }

    public Topic getTopicById(String topicId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        if (topicId != null && !topicId.isEmpty()) {
            try (Cursor c = db.rawQuery(
                    "SELECT id, subject, title, summary, minutes, default_video_url, "
                            + "weight_enem, weight_psc1, weight_psc2, weight_psc3, "
                            + "weight_sis1, weight_sis2, weight_sis3, weight_macro "
                            + "FROM " + FocoVestDbHelper.TABLE_TOPICS + " WHERE id = ? LIMIT 1",
                    new String[]{topicId})) {
                if (c.moveToFirst()) return readTopic(c);
            }
        }
        List<Topic> all = getAllTopics();
        return all.isEmpty() ? null : all.get(0);
    }

    private Topic readTopic(Cursor c) {
        return new Topic(
                c.getString(0),
                SubjectId.fromCode(c.getString(1)),
                c.getString(2),
                c.getString(3),
                c.getInt(4),
                c.getString(5),
                c.getInt(6),
                c.getInt(7),
                c.getInt(8),
                c.getInt(9),
                c.getInt(10),
                c.getInt(11),
                c.getInt(12),
                c.getInt(13)
        );
    }

    public List<Question> getQuestionsForTopic(String topicId) {
        List<Question> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT id, topic_id, subject, prompt, option_a, option_b, option_c, option_d, option_e, "
                        + "correct_index, explanation FROM " + FocoVestDbHelper.TABLE_QUESTIONS
                        + " WHERE topic_id = ? ORDER BY rowid ASC",
                new String[]{topicId})) {
            while (c.moveToNext()) {
                list.add(readQuestion(c));
            }
        }
        return list;
    }

    public Question getQuestionById(String questionId) {
        if (questionId == null) return null;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT id, topic_id, subject, prompt, option_a, option_b, option_c, option_d, option_e, "
                        + "correct_index, explanation FROM " + FocoVestDbHelper.TABLE_QUESTIONS
                        + " WHERE id = ? LIMIT 1",
                new String[]{questionId})) {
            if (c.moveToFirst()) return readQuestion(c);
        }
        return null;
    }

    private Question readQuestion(Cursor c) {
        return new Question(
                c.getString(0),
                c.getString(1),
                SubjectId.fromCode(c.getString(2)),
                c.getString(3),
                c.getString(4),
                c.getString(5),
                c.getString(6),
                c.getString(7),
                c.getString(8),
                c.getInt(9),
                c.getString(10)
        );
    }

    public List<Topic> getCompletedTopics(String userId, ExamId examFilter) {
        return getCompletedTopics(userId, examFilter, 0);
    }

    public List<Topic> getCompletedTopics(String userId, ExamId examFilter, int highSchoolYear) {
        List<Topic> list = new ArrayList<>();
        if (userId == null) return list;

        String weightClause = "";
        if (examFilter != null) {
            weightClause = " AND " + weightFilterSql(examFilter, highSchoolYear);
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT t.id, t.subject, t.title, t.summary, t.minutes, t.default_video_url, "
                        + "t.weight_enem, t.weight_psc1, t.weight_psc2, t.weight_psc3, "
                        + "t.weight_sis1, t.weight_sis2, t.weight_sis3, t.weight_macro "
                        + "FROM " + FocoVestDbHelper.TABLE_TOPICS + " t "
                        + "INNER JOIN " + FocoVestDbHelper.TABLE_USER_TOPIC_PROGRESS + " p "
                        + "ON p.topic_id = t.id AND p.user_id = ? "
                        + "WHERE p.completed = 1" + weightClause + " ORDER BY t.rowid ASC",
                new String[]{userId})) {
            while (c.moveToNext()) {
                list.add(readTopic(c));
            }
        }
        return list;
    }

    public List<Question> getQuestionsForCompletedTopics(String userId, ExamId examFilter) {
        return getQuestionsForCompletedTopics(userId, examFilter, 0);
    }

    public List<Question> getQuestionsForCompletedTopics(String userId, ExamId examFilter, int highSchoolYear) {
        List<Question> list = new ArrayList<>();
        if (userId == null) return list;

        String weightClause = "";
        if (examFilter != null) {
            weightClause = " AND " + weightFilterSql(examFilter, highSchoolYear);
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT q.id, q.topic_id, q.subject, q.prompt, q.option_a, q.option_b, q.option_c, "
                        + "q.option_d, q.option_e, q.correct_index, q.explanation "
                        + "FROM " + FocoVestDbHelper.TABLE_QUESTIONS + " q "
                        + "INNER JOIN " + FocoVestDbHelper.TABLE_TOPICS + " t ON t.id = q.topic_id "
                        + "INNER JOIN " + FocoVestDbHelper.TABLE_USER_TOPIC_PROGRESS + " p "
                        + "ON p.topic_id = q.topic_id AND p.user_id = ? "
                        + "WHERE p.completed = 1" + weightClause,
                new String[]{userId})) {
            while (c.moveToNext()) {
                list.add(readQuestion(c));
            }
        }
        return list;
    }

    private String weightFilterSql(ExamId exam, int highSchoolYear) {
        switch (exam) {
            case ENEM:
                return "t.weight_enem > 0";
            case PSC:
                if (highSchoolYear == 1) return "t.weight_psc1 > 0";
                if (highSchoolYear == 2) return "t.weight_psc2 > 0";
                if (highSchoolYear == 3) return "t.weight_psc3 > 0";
                return "(t.weight_psc1 > 0 OR t.weight_psc2 > 0 OR t.weight_psc3 > 0)";
            case SIS:
                if (highSchoolYear == 1) return "t.weight_sis1 > 0";
                if (highSchoolYear == 2) return "t.weight_sis2 > 0";
                if (highSchoolYear == 3) return "t.weight_sis3 > 0";
                return "(t.weight_sis1 > 0 OR t.weight_sis2 > 0 OR t.weight_sis3 > 0)";
            case Macro:
                return "t.weight_macro > 0";
            default:
                return "t.weight_enem > 0";
        }
    }

    // =========================================================================
    // PROGRESSO POR TÓPICO E RESPOSTAS DE EXERCÍCIOS
    // =========================================================================

    public UserTopicProgress getTopicProgress(String userId, String topicId) {
        if (userId == null || topicId == null) {
            return new UserTopicProgress("", "", false, null, "aula", "", null);
        }
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT completed, completed_at, stage, notes, custom_video_url "
                        + "FROM " + FocoVestDbHelper.TABLE_USER_TOPIC_PROGRESS
                        + " WHERE user_id = ? AND topic_id = ? LIMIT 1",
                new String[]{userId, topicId})) {
            if (c.moveToFirst()) {
                return new UserTopicProgress(
                        userId,
                        topicId,
                        c.getInt(0) == 1,
                        c.getString(1),
                        c.getString(2),
                        c.getString(3),
                        c.getString(4)
                );
            }
        }
        return new UserTopicProgress(userId, topicId, false, null, "aula", "", null);
    }

    public Set<String> getCompletedTopicIds(String userId) {
        Set<String> set = new HashSet<>();
        if (userId == null) return set;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT topic_id FROM " + FocoVestDbHelper.TABLE_USER_TOPIC_PROGRESS
                        + " WHERE user_id = ? AND completed = 1",
                new String[]{userId})) {
            while (c.moveToNext()) {
                set.add(c.getString(0));
            }
        }
        return set;
    }

    public Map<String, String> getCompletedAtMap(String userId) {
        Map<String, String> map = new HashMap<>();
        if (userId == null) return map;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT topic_id, completed_at FROM " + FocoVestDbHelper.TABLE_USER_TOPIC_PROGRESS
                        + " WHERE user_id = ? AND completed = 1 AND completed_at IS NOT NULL",
                new String[]{userId})) {
            while (c.moveToNext()) {
                map.put(c.getString(0), c.getString(1));
            }
        }
        return map;
    }

    private void ensureProgressRow(SQLiteDatabase db, String userId, String topicId) {
        ContentValues cv = new ContentValues();
        cv.put("user_id", userId);
        cv.put("topic_id", topicId);
        cv.put("completed", 0);
        cv.put("stage", "aula");
        cv.put("notes", "");
        db.insertWithOnConflict(FocoVestDbHelper.TABLE_USER_TOPIC_PROGRESS, null, cv,
                SQLiteDatabase.CONFLICT_IGNORE);
    }

    public void saveTopicNotes(String userId, String topicId, String notes) {
        if (userId == null || topicId == null) return;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ensureProgressRow(db, userId, topicId);
        ContentValues cv = new ContentValues();
        cv.put("notes", notes != null ? notes : "");
        db.update(FocoVestDbHelper.TABLE_USER_TOPIC_PROGRESS, cv,
                "user_id = ? AND topic_id = ?", new String[]{userId, topicId});
    }

    public void saveTopicStage(String userId, String topicId, String stage) {
        if (userId == null || topicId == null) return;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ensureProgressRow(db, userId, topicId);
        ContentValues cv = new ContentValues();
        cv.put("stage", stage);
        db.update(FocoVestDbHelper.TABLE_USER_TOPIC_PROGRESS, cv,
                "user_id = ? AND topic_id = ?", new String[]{userId, topicId});
    }

    public void saveCustomVideoUrl(String userId, String topicId, String customUrl) {
        if (userId == null || topicId == null) return;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ensureProgressRow(db, userId, topicId);
        ContentValues cv = new ContentValues();
        if (customUrl == null || customUrl.trim().isEmpty()) {
            cv.putNull("custom_video_url");
        } else {
            cv.put("custom_video_url", customUrl.trim());
        }
        db.update(FocoVestDbHelper.TABLE_USER_TOPIC_PROGRESS, cv,
                "user_id = ? AND topic_id = ?", new String[]{userId, topicId});
    }

    public void markTopicCompleted(String userId, String topicId) {
        if (userId == null || topicId == null) return;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ensureProgressRow(db, userId, topicId);
        ContentValues cv = new ContentValues();
        cv.put("completed", 1);
        cv.put("completed_at", PlanEngine.dayKey(new Date()));
        cv.put("stage", "diagnostico");
        db.update(FocoVestDbHelper.TABLE_USER_TOPIC_PROGRESS, cv,
                "user_id = ? AND topic_id = ?", new String[]{userId, topicId});
    }

    public void redoTopic(String userId, String topicId) {
        if (userId == null || topicId == null) return;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.execSQL("DELETE FROM " + FocoVestDbHelper.TABLE_USER_QUESTION_ANSWERS
                    + " WHERE user_id = ? AND question_id IN ("
                    + "SELECT id FROM " + FocoVestDbHelper.TABLE_QUESTIONS + " WHERE topic_id = ?)",
                    new Object[]{userId, topicId});

            ensureProgressRow(db, userId, topicId);
            ContentValues cv = new ContentValues();
            cv.put("completed", 0);
            cv.putNull("completed_at");
            cv.put("stage", "exercicios");
            db.update(FocoVestDbHelper.TABLE_USER_TOPIC_PROGRESS, cv,
                    "user_id = ? AND topic_id = ?", new String[]{userId, topicId});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void saveQuestionAnswer(String userId, String questionId, int selectedIndex) {
        if (userId == null || questionId == null) return;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("user_id", userId);
        cv.put("question_id", questionId);
        cv.put("selected_index", selectedIndex);
        db.insertWithOnConflict(FocoVestDbHelper.TABLE_USER_QUESTION_ANSWERS, null, cv,
                SQLiteDatabase.CONFLICT_REPLACE);
    }

    public Map<String, Integer> getAnswersForTopic(String userId, String topicId) {
        Map<String, Integer> map = new HashMap<>();
        if (userId == null || topicId == null) return map;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT a.question_id, a.selected_index "
                        + "FROM " + FocoVestDbHelper.TABLE_USER_QUESTION_ANSWERS + " a "
                        + "INNER JOIN " + FocoVestDbHelper.TABLE_QUESTIONS + " q ON q.id = a.question_id "
                        + "WHERE a.user_id = ? AND q.topic_id = ?",
                new String[]{userId, topicId})) {
            while (c.moveToNext()) {
                map.put(c.getString(0), c.getInt(1));
            }
        }
        return map;
    }

    public int[] getFixationStats(String userId) {
        if (userId == null) return new int[]{0, 0};
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT COUNT(*), SUM(CASE WHEN a.selected_index = q.correct_index THEN 1 ELSE 0 END) "
                        + "FROM " + FocoVestDbHelper.TABLE_USER_QUESTION_ANSWERS + " a "
                        + "INNER JOIN " + FocoVestDbHelper.TABLE_QUESTIONS + " q ON q.id = a.question_id "
                        + "INNER JOIN " + FocoVestDbHelper.TABLE_USER_TOPIC_PROGRESS + " p "
                        + "ON p.topic_id = q.topic_id AND p.user_id = a.user_id "
                        + "WHERE a.user_id = ? AND p.completed = 1",
                new String[]{userId})) {
            if (c.moveToFirst()) {
                int total = c.getInt(0);
                int correct = c.isNull(1) ? 0 : c.getInt(1);
                return new int[]{correct, total};
            }
        }
        return new int[]{0, 0};
    }

    public int[] getSubjectAccuracy(String userId, SubjectId subject) {
        if (userId == null || subject == null) return new int[]{0, 0};
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT COUNT(*), SUM(CASE WHEN a.selected_index = q.correct_index THEN 1 ELSE 0 END) "
                        + "FROM " + FocoVestDbHelper.TABLE_USER_QUESTION_ANSWERS + " a "
                        + "INNER JOIN " + FocoVestDbHelper.TABLE_QUESTIONS + " q ON q.id = a.question_id "
                        + "INNER JOIN " + FocoVestDbHelper.TABLE_USER_TOPIC_PROGRESS + " p "
                        + "ON p.topic_id = q.topic_id AND p.user_id = a.user_id "
                        + "WHERE a.user_id = ? AND p.completed = 1 AND q.subject = ?",
                new String[]{userId, subject.code})) {
            if (c.moveToFirst()) {
                int total = c.getInt(0);
                int correct = c.isNull(1) ? 0 : c.getInt(1);
                return new int[]{correct, total};
            }
        }
        return new int[]{0, 0};
    }

    // =========================================================================
    // SIMULADOS DINÂMICOS (HISTÓRICO)
    // =========================================================================

    public void saveSimuladoResult(SimuladoHistoryItem item) {
        if (item == null) return;
        ContentValues cv = new ContentValues();
        cv.put("id", item.id);
        cv.put("user_id", item.userId);
        cv.put("exam_filter", item.examFilter);
        cv.put("title", item.title);
        cv.put("correct_count", item.correctCount);
        cv.put("total_count", item.totalCount);
        cv.put("duration_sec", item.durationSec);
        cv.put("created_at", item.createdAt);
        cv.put("answers_json", mapToJson(item.answers));
        cv.put("question_ids_json", listToJson(item.questionIds));

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.insert(FocoVestDbHelper.TABLE_SIMULADO_HISTORY, null, cv);
    }

    public List<SimuladoHistoryItem> getSimuladoHistory(String userId) {
        List<SimuladoHistoryItem> list = new ArrayList<>();
        if (userId == null) return list;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT id, user_id, exam_filter, title, correct_count, total_count, duration_sec, "
                        + "created_at, answers_json, question_ids_json "
                        + "FROM " + FocoVestDbHelper.TABLE_SIMULADO_HISTORY
                        + " WHERE user_id = ? ORDER BY created_at DESC, rowid DESC",
                new String[]{userId})) {
            while (c.moveToNext()) {
                list.add(new SimuladoHistoryItem(
                        c.getString(0),
                        c.getString(1),
                        c.getString(2),
                        c.getString(3),
                        c.getInt(4),
                        c.getInt(5),
                        c.getInt(6),
                        c.getString(7),
                        jsonToMap(c.getString(8)),
                        jsonToList(c.getString(9))
                ));
            }
        }
        return list;
    }

    public int getSimuladoCount(String userId) {
        if (userId == null) return 0;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM " + FocoVestDbHelper.TABLE_SIMULADO_HISTORY + " WHERE user_id = ?",
                new String[]{userId})) {
            return c.moveToFirst() ? c.getInt(0) : 0;
        }
    }

    // =========================================================================
    // PONTUAÇÃO
    // =========================================================================

    public int calculateUserPoints(String userId) {
        if (userId == null) return 0;
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        int completedTopics = 0;
        try (Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM " + FocoVestDbHelper.TABLE_USER_TOPIC_PROGRESS
                        + " WHERE user_id = ? AND completed = 1",
                new String[]{userId})) {
            if (c.moveToFirst()) completedTopics = c.getInt(0);
        }

        int[] fixation = getFixationStats(userId);
        int fixationCorrect = fixation[0];

        int simCorrectTotal = 0;
        try (Cursor c = db.rawQuery(
                "SELECT SUM(correct_count) FROM " + FocoVestDbHelper.TABLE_SIMULADO_HISTORY
                        + " WHERE user_id = ?",
                new String[]{userId})) {
            if (c.moveToFirst() && !c.isNull(0)) {
                simCorrectTotal = c.getInt(0);
            }
        }

        return fixationCorrect * POINTS_PER_FIXATION_CORRECT
                + completedTopics * POINTS_PER_COMPLETED_TOPIC
                + simCorrectTotal * POINTS_PER_SIMULADO_CORRECT;
    }

    public static String nowTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(new Date());
    }

    // =========================================================================
    // SERIALIZAÇÃO JSON NATIVA (org.json)
    // =========================================================================

    private static String mapToJson(Map<String, Integer> map) {
        JSONObject obj = new JSONObject();
        if (map != null) {
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                try {
                    obj.put(entry.getKey(), entry.getValue());
                } catch (Exception ignored) {
                }
            }
        }
        return obj.toString();
    }

    private static Map<String, Integer> jsonToMap(String raw) {
        Map<String, Integer> map = new HashMap<>();
        if (raw == null || raw.trim().isEmpty()) return map;
        try {
            JSONObject obj = new JSONObject(raw);
            Iterator<String> keys = obj.keys();
            while (keys.hasNext()) {
                String k = keys.next();
                map.put(k, obj.optInt(k));
            }
        } catch (Exception ignored) {
        }
        return map;
    }

    private static String listToJson(List<String> list) {
        JSONArray arr = new JSONArray();
        if (list != null) {
            for (String item : list) {
                arr.put(item);
            }
        }
        return arr.toString();
    }

    private static List<String> jsonToList(String raw) {
        List<String> list = new ArrayList<>();
        if (raw == null || raw.trim().isEmpty()) return list;
        try {
            JSONArray arr = new JSONArray(raw);
            for (int i = 0; i < arr.length(); i++) {
                list.add(arr.optString(i));
            }
        } catch (Exception ignored) {
        }
        return list;
    }
}
