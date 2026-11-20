package com.example.focovest.ui.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.focovest.R;
import com.example.focovest.data.DataRepository;
import com.example.focovest.data.DataRepository.ExamId;
import com.example.focovest.data.StoreManager;
import com.example.focovest.domain.PlanEngine;

import java.util.*;

public class OnboardingActivity extends AppCompatActivity {

    private int step = 0;
    private String selectedMode = null; // "autonoma" | "turma"
    private boolean turmaJoined = false;
    private int dailyGoal = 3;
    private final List<ExamId> selectedExams = new ArrayList<>(Arrays.asList(ExamId.PSC, ExamId.SIS, ExamId.ENEM));
    private final List<Integer> selectedDays = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
    private static final String[] DAY_LABELS = {"D", "S", "T", "Q", "Q", "S", "S"};

    private View stepBar1, stepBar2, stepBar3;
    private View step0Container, step1Container, step2Container;
    private View subStepAutonoma, subStepTurma;
    private TextView txtStepIndicator, txtHoursLabel, txtWeeklySummary, txtTurmaFeedback;
    private EditText edtName, edtCourse, edtTurmaCode;
    private Spinner spinnerCity;
    private LinearLayout examPickerContainer, daysPickerContainer;
    private LinearLayout cardModeAutonoma, cardModeTurma;
    private TextView txtTitleAutonoma, txtDescAutonoma, txtTitleTurma, txtDescTurma;
    private Button btnBack, btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        stepBar1 = findViewById(R.id.stepBar1);
        stepBar2 = findViewById(R.id.stepBar2);
        stepBar3 = findViewById(R.id.stepBar3);
        step0Container = findViewById(R.id.step0Container);
        step1Container = findViewById(R.id.step1Container);
        step2Container = findViewById(R.id.step2Container);
        subStepAutonoma = findViewById(R.id.subStepAutonoma);
        subStepTurma = findViewById(R.id.subStepTurma);
        txtStepIndicator = findViewById(R.id.txtStepIndicator);
        txtHoursLabel = findViewById(R.id.txtHoursLabel);
        txtWeeklySummary = findViewById(R.id.txtWeeklySummary);
        txtTurmaFeedback = findViewById(R.id.txtTurmaFeedback);
        edtName = findViewById(R.id.edtName);
        edtCourse = findViewById(R.id.edtCourse);
        edtTurmaCode = findViewById(R.id.edtTurmaCode);
        spinnerCity = findViewById(R.id.spinnerCity);
        examPickerContainer = findViewById(R.id.examPickerContainer);
        daysPickerContainer = findViewById(R.id.daysPickerContainer);
        cardModeAutonoma = findViewById(R.id.cardModeAutonoma);
        cardModeTurma = findViewById(R.id.cardModeTurma);
        txtTitleAutonoma = findViewById(R.id.txtTitleAutonoma);
        txtDescAutonoma = findViewById(R.id.txtDescAutonoma);
        txtTitleTurma = findViewById(R.id.txtTitleTurma);
        txtDescTurma = findViewById(R.id.txtDescTurma);
        btnBack = findViewById(R.id.btnBack);
        btnNext = findViewById(R.id.btnNext);

        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, DataRepository.AM_CITIES);
        spinnerCity.setAdapter(cityAdapter);

        StoreManager.State existingState = StoreManager.getInstance(this).getState();
        if (existingState.name != null && !existingState.name.trim().isEmpty()) {
            edtName.setText(existingState.name);
        }
        if (existingState.city != null) {
            int cityIndex = DataRepository.AM_CITIES.indexOf(existingState.city);
            if (cityIndex >= 0) spinnerCity.setSelection(cityIndex);
        }

        edtName.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { updateUI(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        cardModeAutonoma.setOnClickListener(v -> {
            selectedMode = "autonoma";
            updateUI();
        });
        cardModeTurma.setOnClickListener(v -> {
            selectedMode = "turma";
            updateUI();
        });

        SeekBar seek = findViewById(R.id.seekDailyGoal);
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                dailyGoal = Math.max(1, progress);
                updateRhythmLabels();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        Button btnJoinTurma = findViewById(R.id.btnJoinTurma);
        btnJoinTurma.setOnClickListener(v -> {
            String code = edtTurmaCode.getText().toString().trim().toUpperCase(Locale.ROOT);
            if (DataRepository.TURMA_CODE.equals(code)) {
                turmaJoined = true;
                txtTurmaFeedback.setText("✓ Você entrou na turma " + DataRepository.TURMA_NAME + ".");
                txtTurmaFeedback.setTextColor(getResources().getColor(R.color.success_dark, getTheme()));
            } else {
                turmaJoined = false;
                txtTurmaFeedback.setText("Código não encontrado. Use AM-2025 para testar.");
                txtTurmaFeedback.setTextColor(getResources().getColor(R.color.accent, getTheme()));
            }
            updateUI();
        });

        btnBack.setOnClickListener(v -> {
            if (step > 0) {
                step--;
                updateUI();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (step < 2) {
                step++;
                updateUI();
            } else {
                finishOnboarding();
            }
        });

        renderExamPicker();
        renderDaysPicker();
        updateRhythmLabels();
        updateUI();
    }

    private void renderExamPicker() {
        examPickerContainer.removeAllViews();
        for (ExamId exam : ExamId.values()) {
            boolean isSelected = selectedExams.contains(exam);
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(36, 28, 36, 28);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, 16);
            row.setLayoutParams(lp);
            row.setBackgroundResource(isSelected ? R.drawable.bg_card_dark : R.drawable.bg_card);

            TextView title = new TextView(this);
            title.setText(exam.id + " · " + exam.institution);
            title.setTextSize(16f);
            title.setTextColor(isSelected ? Color.WHITE : getResources().getColor(R.color.ink, getTheme()));
            title.setTypeface(null, android.graphics.Typeface.BOLD);

            TextView desc = new TextView(this);
            desc.setText(exam.desc);
            desc.setTextSize(12f);
            desc.setTextColor(isSelected ? Color.parseColor("#B3FFFFFF") : getResources().getColor(R.color.muted, getTheme()));

            row.addView(title);
            row.addView(desc);

            row.setOnClickListener(v -> {
                if (selectedExams.contains(exam)) selectedExams.remove(exam);
                else selectedExams.add(exam);
                renderExamPicker();
                updateUI();
            });
            examPickerContainer.addView(row);
        }
    }

    private void renderDaysPicker() {
        daysPickerContainer.removeAllViews();
        for (int i = 0; i < 7; i++) {
            final int dayIdx = i;
            boolean on = selectedDays.contains(dayIdx);
            TextView btn = new TextView(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, 110, 1f);
            lp.setMargins(6, 0, 6, 0);
            btn.setLayoutParams(lp);
            btn.setGravity(Gravity.CENTER);
            btn.setText(DAY_LABELS[i]);
            btn.setTextSize(14f);
            btn.setTypeface(null, android.graphics.Typeface.BOLD);
            btn.setBackgroundResource(on ? R.drawable.bg_button_accent : R.drawable.bg_input);
            btn.setTextColor(on ? Color.WHITE : getResources().getColor(R.color.muted, getTheme()));

            btn.setOnClickListener(v -> {
                if (selectedDays.contains(dayIdx)) selectedDays.remove(Integer.valueOf(dayIdx));
                else {
                    selectedDays.add(dayIdx);
                    Collections.sort(selectedDays);
                }
                renderDaysPicker();
                updateRhythmLabels();
                updateUI();
            });
            daysPickerContainer.addView(btn);
        }
    }

    private void updateRhythmLabels() {
        int tpd = PlanEngine.topicsPerDay(dailyGoal);
        txtHoursLabel.setText("Horas disponíveis por dia: " + dailyGoal + "h · ≈ " + tpd + " tópicos/dia");
        int totalTopics = tpd * selectedDays.size();
        int totalHours = dailyGoal * selectedDays.size();
        txtWeeklySummary.setText(totalTopics + " tópicos por semana · " + totalHours + "h de estudo.");
    }

    private void updateUI() {
        String step3Name = "turma".equals(selectedMode) ? "TURMA" : "RITMO";
        String[] stepNames = {"PERFIL", "METODOLOGIA", step3Name};
        txtStepIndicator.setText("PASSO " + (step + 1) + " DE 3 · " + stepNames[step]);

        int accent = getResources().getColor(R.color.accent, getTheme());
        int inactive = getResources().getColor(R.color.ink_10, getTheme());
        stepBar1.setBackgroundColor(step >= 0 ? accent : inactive);
        stepBar2.setBackgroundColor(step >= 1 ? accent : inactive);
        stepBar3.setBackgroundColor(step >= 2 ? accent : inactive);

        step0Container.setVisibility(step == 0 ? View.VISIBLE : View.GONE);
        step1Container.setVisibility(step == 1 ? View.VISIBLE : View.GONE);
        step2Container.setVisibility(step == 2 ? View.VISIBLE : View.GONE);

        boolean isAutonoma = "autonoma".equals(selectedMode);
        boolean isTurma = "turma".equals(selectedMode);
        cardModeAutonoma.setBackgroundResource(isAutonoma ? R.drawable.bg_card_dark : R.drawable.bg_card);
        txtTitleAutonoma.setTextColor(isAutonoma ? Color.WHITE : getResources().getColor(R.color.ink, getTheme()));
        txtDescAutonoma.setTextColor(isAutonoma ? Color.parseColor("#B3FFFFFF") : getResources().getColor(R.color.muted, getTheme()));

        cardModeTurma.setBackgroundResource(isTurma ? R.drawable.bg_card_dark : R.drawable.bg_card);
        txtTitleTurma.setTextColor(isTurma ? Color.WHITE : getResources().getColor(R.color.ink, getTheme()));
        txtDescTurma.setTextColor(isTurma ? Color.parseColor("#B3FFFFFF") : getResources().getColor(R.color.muted, getTheme()));

        subStepAutonoma.setVisibility(isAutonoma ? View.VISIBLE : View.GONE);
        subStepTurma.setVisibility(isTurma ? View.VISIBLE : View.GONE);

        btnBack.setVisibility(step > 0 ? View.VISIBLE : View.INVISIBLE);
        btnNext.setText(step < 2 ? "Continuar →" : "Começar a estudar →");

        boolean canNext = false;
        if (step == 0) {
            canNext = edtName.getText().toString().trim().length() > 1 && !selectedExams.isEmpty();
        } else if (step == 1) {
            canNext = selectedMode != null;
        } else if (step == 2) {
            canNext = isAutonoma ? !selectedDays.isEmpty() : turmaJoined;
        }
        btnNext.setEnabled(canNext);
        btnNext.setAlpha(canNext ? 1.0f : 0.35f);
    }

    private void finishOnboarding() {
        StoreManager store = StoreManager.getInstance(this);
        StoreManager.State s = store.getState();
        s.profileCreated = true;
        s.name = edtName.getText().toString().trim();
        s.city = spinnerCity.getSelectedItem().toString();
        s.targetExams = new ArrayList<>(selectedExams);
        s.targetCourse = edtCourse.getText().toString().trim();
        s.mode = selectedMode;
        s.turmaJoined = "turma".equals(selectedMode) && turmaJoined;
        s.dailyGoal = dailyGoal;
        s.studyDays = new ArrayList<>(selectedDays);
        store.save();

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
