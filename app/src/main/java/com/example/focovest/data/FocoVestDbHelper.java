package com.example.focovest.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Banco SQLite embutido do FocoVest (100% offline-first, perfil único local).
 * Nasce populado automaticamente no onCreate com edital completo, videoaulas
 * e questões comentadas.
 */
public class FocoVestDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "focovest.db";
    public static final int DATABASE_VERSION = 5;

    public static final String TABLE_USERS = "users";
    public static final String TABLE_TOPICS = "topics";
    public static final String TABLE_QUESTIONS = "questions";
    public static final String TABLE_USER_TOPIC_PROGRESS = "user_topic_progress";
    public static final String TABLE_USER_QUESTION_ANSWERS = "user_question_answers";
    public static final String TABLE_SIMULADO_HISTORY = "simulado_history";

    public FocoVestDbHelper(Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USERS + " ("
                + "id TEXT PRIMARY KEY,"
                + "name TEXT NOT NULL DEFAULT '',"
                + "city TEXT NOT NULL DEFAULT 'Manaus',"
                + "target_course TEXT NOT NULL DEFAULT '',"
                + "in_high_school INTEGER NOT NULL DEFAULT 1,"
                + "high_school_year INTEGER NOT NULL DEFAULT 3,"
                + "target_exams TEXT NOT NULL DEFAULT 'ENEM,PSC,SIS',"
                + "daily_hours INTEGER NOT NULL DEFAULT 3,"
                + "study_days TEXT NOT NULL DEFAULT '1,2,3,4,5',"
                + "profile_completed INTEGER NOT NULL DEFAULT 0,"
                + "last_topic_id TEXT"
                + ")");

        db.execSQL("CREATE TABLE " + TABLE_TOPICS + " ("
                + "id TEXT PRIMARY KEY,"
                + "subject TEXT NOT NULL,"
                + "title TEXT NOT NULL,"
                + "summary TEXT NOT NULL,"
                + "minutes INTEGER NOT NULL,"
                + "default_video_url TEXT NOT NULL,"
                + "weight_enem INTEGER NOT NULL DEFAULT 0,"
                + "weight_psc1 INTEGER NOT NULL DEFAULT 0,"
                + "weight_psc2 INTEGER NOT NULL DEFAULT 0,"
                + "weight_psc3 INTEGER NOT NULL DEFAULT 0,"
                + "weight_sis1 INTEGER NOT NULL DEFAULT 0,"
                + "weight_sis2 INTEGER NOT NULL DEFAULT 0,"
                + "weight_sis3 INTEGER NOT NULL DEFAULT 0,"
                + "weight_macro INTEGER NOT NULL DEFAULT 0"
                + ")");

        db.execSQL("CREATE TABLE " + TABLE_QUESTIONS + " ("
                + "id TEXT PRIMARY KEY,"
                + "topic_id TEXT NOT NULL,"
                + "subject TEXT NOT NULL,"
                + "prompt TEXT NOT NULL,"
                + "option_a TEXT NOT NULL,"
                + "option_b TEXT NOT NULL,"
                + "option_c TEXT NOT NULL,"
                + "option_d TEXT NOT NULL,"
                + "option_e TEXT NOT NULL,"
                + "correct_index INTEGER NOT NULL,"
                + "explanation TEXT NOT NULL,"
                + "FOREIGN KEY (topic_id) REFERENCES " + TABLE_TOPICS + "(id) ON DELETE CASCADE"
                + ")");

        db.execSQL("CREATE TABLE " + TABLE_USER_TOPIC_PROGRESS + " ("
                + "user_id TEXT NOT NULL,"
                + "topic_id TEXT NOT NULL,"
                + "completed INTEGER NOT NULL DEFAULT 0,"
                + "completed_at TEXT,"
                + "stage TEXT NOT NULL DEFAULT 'aula',"
                + "notes TEXT NOT NULL DEFAULT '',"
                + "custom_video_url TEXT,"
                + "PRIMARY KEY (user_id, topic_id),"
                + "FOREIGN KEY (user_id) REFERENCES " + TABLE_USERS + "(id) ON DELETE CASCADE,"
                + "FOREIGN KEY (topic_id) REFERENCES " + TABLE_TOPICS + "(id) ON DELETE CASCADE"
                + ")");

        db.execSQL("CREATE TABLE " + TABLE_USER_QUESTION_ANSWERS + " ("
                + "user_id TEXT NOT NULL,"
                + "question_id TEXT NOT NULL,"
                + "selected_index INTEGER NOT NULL,"
                + "PRIMARY KEY (user_id, question_id),"
                + "FOREIGN KEY (user_id) REFERENCES " + TABLE_USERS + "(id) ON DELETE CASCADE,"
                + "FOREIGN KEY (question_id) REFERENCES " + TABLE_QUESTIONS + "(id) ON DELETE CASCADE"
                + ")");

        db.execSQL("CREATE TABLE " + TABLE_SIMULADO_HISTORY + " ("
                + "id TEXT PRIMARY KEY,"
                + "user_id TEXT NOT NULL,"
                + "exam_filter TEXT NOT NULL,"
                + "title TEXT NOT NULL,"
                + "correct_count INTEGER NOT NULL,"
                + "total_count INTEGER NOT NULL,"
                + "duration_sec INTEGER NOT NULL,"
                + "created_at TEXT NOT NULL,"
                + "answers_json TEXT NOT NULL,"
                + "question_ids_json TEXT NOT NULL,"
                + "FOREIGN KEY (user_id) REFERENCES " + TABLE_USERS + "(id) ON DELETE CASCADE"
                + ")");

        db.execSQL("CREATE INDEX idx_questions_topic ON " + TABLE_QUESTIONS + "(topic_id)");
        db.execSQL("CREATE INDEX idx_progress_user ON " + TABLE_USER_TOPIC_PROGRESS + "(user_id)");
        db.execSQL("CREATE INDEX idx_answers_user ON " + TABLE_USER_QUESTION_ANSWERS + "(user_id)");
        db.execSQL("CREATE INDEX idx_simulado_user ON " + TABLE_SIMULADO_HISTORY + "(user_id)");

        DatabaseSeeder.seed(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            db.execSQL("DROP TABLE IF EXISTS group_members");
            db.execSQL("DROP TABLE IF EXISTS friend_groups");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SIMULADO_HISTORY);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_QUESTION_ANSWERS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_TOPIC_PROGRESS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_QUESTIONS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_TOPICS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
            onCreate(db);
            return;
        }
        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE " + TABLE_USERS
                    + " ADD COLUMN in_high_school INTEGER NOT NULL DEFAULT 1");
            db.execSQL("ALTER TABLE " + TABLE_USERS
                    + " ADD COLUMN high_school_year INTEGER NOT NULL DEFAULT 3");
        }
        if (oldVersion < 5) {
            db.execSQL("ALTER TABLE " + TABLE_TOPICS + " ADD COLUMN weight_psc1 INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE " + TABLE_TOPICS + " ADD COLUMN weight_psc2 INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE " + TABLE_TOPICS + " ADD COLUMN weight_psc3 INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE " + TABLE_TOPICS + " ADD COLUMN weight_sis1 INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE " + TABLE_TOPICS + " ADD COLUMN weight_sis2 INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE " + TABLE_TOPICS + " ADD COLUMN weight_sis3 INTEGER NOT NULL DEFAULT 0");
            DatabaseSeeder.seedTopicsAndQuestionsOnly(db);
        }
    }
}
