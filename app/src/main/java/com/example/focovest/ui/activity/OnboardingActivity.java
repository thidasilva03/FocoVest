package com.example.focovest.ui.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.focovest.R;
import com.example.focovest.data.FocoVestRepository;
import com.example.focovest.domain.PlanEngine;
import com.example.focovest.model.ExamId;
import com.example.focovest.model.User;
import com.example.focovest.util.EdgeToEdgeHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Onboarding simplificado em 2 passos: Objetivos (vestibulares) e Ritmo de estudo.
 */
public class OnboardingActivity extends AppCompatActivity {

    private static final String[] DAY_LABELS = {"D", "S", "T", "Q", "Q", "S", "S"};

    private int step = 0; // 0 = Objetivos, 1 = Ritmo
    private int dailyGoal = 3;
    private Boolean inHighSchool = null; // null = ainda não respondeu
    private int highSchoolYear = 0; // 1, 2 ou 3 quando inHighSchool == true; 0 caso contrário
    private final List<ExamId> selectedExams = new ArrayList<>();
    private final List<Integer> selectedDays = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));

    private String loggedUserId;

    private View stepBar1, stepBar2;
    private View step0Container, step1Container;
    private View highSchoolYearSection, examsSelectionSection;
    private TextView txtStepIndicator, txtHoursLabel, txtWeeklySummary, txtExamsHint;
    private TextView btnHighSchoolYes, btnHighSchoolNo;
    private TextView btnYear1, btnYear2, btnYear3;
    private EditText edtName, edtCourse;
    private Spinner spinnerCity;
    private LinearLayout examPickerContainer, daysPickerContainer;
    private Button btnBack, btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdgeHelper.enable(this, true);
        super.onCreate(savedInstanceState);

        FocoVestRepository repo = FocoVestRepository.getInstance(this);
        User currentUser = repo.getOrCreateLocalUser();
        loggedUserId = currentUser.id;

        setContentView(R.layout.activity_onboarding);
        EdgeToEdgeHelper.applySystemBarInsets(findViewById(R.id.onboardingRoot), true);

        stepBar1 = findViewById(R.id.stepBar1);
        stepBar2 = findViewById(R.id.stepBar2);
        step0Container = findViewById(R.id.step0Container);
        step1Container = findViewById(R.id.step1Container);
        highSchoolYearSection = findViewById(R.id.highSchoolYearSection);
        examsSelectionSection = findViewById(R.id.examsSelectionSection);
        txtStepIndicator = findViewById(R.id.txtStepIndicator);
        txtHoursLabel = findViewById(R.id.txtHoursLabel);
        txtWeeklySummary = findViewById(R.id.txtWeeklySummary);
        txtExamsHint = findViewById(R.id.txtExamsHint);
        btnHighSchoolYes = findViewById(R.id.btnHighSchoolYes);
        btnHighSchoolNo = findViewById(R.id.btnHighSchoolNo);
        btnYear1 = findViewById(R.id.btnYear1);
        btnYear2 = findViewById(R.id.btnYear2);
        btnYear3 = findViewById(R.id.btnYear3);
        edtName = findViewById(R.id.edtName);
        edtCourse = findViewById(R.id.edtCourse);
        spinnerCity = findViewById(R.id.spinnerCity);
        examPickerContainer = findViewById(R.id.examPickerContainer);
        daysPickerContainer = findViewById(R.id.daysPickerContainer);
        btnBack = findViewById(R.id.btnBack);
        btnNext = findViewById(R.id.btnNext);

        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, User.AM_CITIES);
        spinnerCity.setAdapter(cityAdapter);

        if (currentUser.name != null && !currentUser.name.trim().isEmpty()) {
            edtName.setText(currentUser.name);
        }
        if (currentUser.targetCourse != null && !currentUser.targetCourse.trim().isEmpty()) {
            edtCourse.setText(currentUser.targetCourse);
        }
        int cityIndex = User.AM_CITIES.indexOf(currentUser.city);
        if (cityIndex >= 0) spinnerCity.setSelection(cityIndex);

        if (currentUser.profileCompleted) {
            inHighSchool = currentUser.inHighSchool;
            highSchoolYear = currentUser.highSchoolYear;
            selectedExams.clear();
            selectedExams.addAll(ExamId.filterEligible(currentUser.targetExams, inHighSchool, highSchoolYear));
        }

        selectedDays.clear();
        selectedDays.addAll(currentUser.studyDays);
        dailyGoal = currentUser.dailyHours;

        edtName.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { updateUI(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnHighSchoolYes.setOnClickListener(v -> {
            inHighSchool = true;
            if (highSchoolYear >= 1 && highSchoolYear <= 3) {
                syncEligibleExams();
            } else {
                selectedExams.clear();
            }
            renderSchoolingControls();
            renderExamPicker();
            updateUI();
        });

        btnHighSchoolNo.setOnClickListener(v -> {
            inHighSchool = false;
            highSchoolYear = 0;
            syncEligibleExams();
            renderSchoolingControls();
            renderExamPicker();
            updateUI();
        });

        btnYear1.setOnClickListener(v -> selectHighSchoolYear(1));
        btnYear2.setOnClickListener(v -> selectHighSchoolYear(2));
        btnYear3.setOnClickListener(v -> selectHighSchoolYear(3));

        SeekBar seek = findViewById(R.id.seekDailyGoal);
        seek.setProgress(dailyGoal);
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                dailyGoal = Math.max(1, progress);
                updateRhythmLabels();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        btnBack.setOnClickListener(v -> {
            if (step > 0) {
                step--;
                updateUI();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (step == 0) {
                step = 1;
                updateUI();
            } else {
                finishOnboarding();
            }
        });

        renderSchoolingControls();
        renderExamPicker();
        renderDaysPicker();
        updateRhythmLabels();
        updateUI();
    }

    private void selectHighSchoolYear(int year) {
        inHighSchool = true;
        highSchoolYear = year;
        syncEligibleExams();
        renderSchoolingControls();
        renderExamPicker();
        updateUI();
    }

    private boolean isSchoolingReady() {
        if (inHighSchool == null) return false;
        return !inHighSchool || (highSchoolYear >= 1 && highSchoolYear <= 3);
    }

    private void syncEligibleExams() {
        if (!isSchoolingReady()) {
            selectedExams.clear();
            return;
        }
        List<ExamId> filtered = ExamId.filterEligible(selectedExams, inHighSchool, highSchoolYear);
        selectedExams.clear();
        selectedExams.addAll(filtered);
    }

    private void renderSchoolingControls() {
        styleToggleButton(btnHighSchoolYes, Boolean.TRUE.equals(inHighSchool));
        styleToggleButton(btnHighSchoolNo, Boolean.FALSE.equals(inHighSchool));

        highSchoolYearSection.setVisibility(Boolean.TRUE.equals(inHighSchool) ? View.VISIBLE : View.GONE);
        styleToggleButton(btnYear1, Boolean.TRUE.equals(inHighSchool) && highSchoolYear == 1);
        styleToggleButton(btnYear2, Boolean.TRUE.equals(inHighSchool) && highSchoolYear == 2);
        styleToggleButton(btnYear3, Boolean.TRUE.equals(inHighSchool) && highSchoolYear == 3);
    }

    private void styleToggleButton(TextView btn, boolean selected) {
        btn.setBackgroundResource(selected ? R.drawable.bg_card_dark : R.drawable.bg_card);
        btn.setTextColor(selected
                ? getResources().getColor(R.color.lime, getTheme())
                : getResources().getColor(R.color.ink, getTheme()));
    }

    private void renderExamPicker() {
        examPickerContainer.removeAllViews();

        if (!isSchoolingReady()) {
            examsSelectionSection.setVisibility(View.GONE);
            return;
        }
        examsSelectionSection.setVisibility(View.VISIBLE);

        boolean inEm = Boolean.TRUE.equals(inHighSchool);
        if (!inEm) {
            txtExamsHint.setText("Como você já concluiu o Ensino Médio, ficam disponíveis apenas Macro e ENEM.");
        } else if (highSchoolYear == 1) {
            txtExamsHint.setText("Na 1ª série: 1ª etapa do PSC, SIS 1 e ENEM (o Macro exige 3ª série ou Ensino Médio concluído).");
        } else if (highSchoolYear == 2) {
            txtExamsHint.setText("Na 2ª série: 2ª etapa do PSC, SIS 2 e ENEM (o Macro exige 3ª série ou Ensino Médio concluído).");
        } else {
            txtExamsHint.setText("Na 3ª série: 3ª etapa do PSC, SIS 3, Macro e ENEM disponíveis.");
        }

        List<ExamId> available = ExamId.availableFor(inEm, highSchoolYear);
        int padH = dp(16);
        int padV = dp(12);
        int marginB = dp(8);

        for (ExamId exam : available) {
            boolean isSelected = selectedExams.contains(exam);
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(padH, padV, padH, padV);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, marginB);
            row.setLayoutParams(lp);
            row.setBackgroundResource(isSelected ? R.drawable.bg_card_dark : R.drawable.bg_card);

            TextView title = new TextView(this);
            title.setText((isSelected ? "✓ " : "") + exam.getDisplayLabel(inEm, highSchoolYear) + " · " + exam.institution);
            title.setTextSize(16f);
            title.setTextColor(isSelected ? Color.WHITE : getResources().getColor(R.color.ink, getTheme()));
            title.setTypeface(null, Typeface.BOLD);

            TextView desc = new TextView(this);
            desc.setText(exam.getContextualDesc(inEm, highSchoolYear));
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
        int h = dp(44);
        int m = dp(3);
        for (int i = 0; i < 7; i++) {
            final int dayIdx = i;
            boolean on = selectedDays.contains(dayIdx);
            TextView btn = new TextView(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, h, 1f);
            lp.setMargins(m, 0, m, 0);
            btn.setLayoutParams(lp);
            btn.setGravity(Gravity.CENTER);
            btn.setText(DAY_LABELS[i]);
            btn.setTextSize(14f);
            btn.setTypeface(null, Typeface.BOLD);
            btn.setBackgroundResource(on ? R.drawable.bg_button_accent : R.drawable.bg_input);
            btn.setTextColor(on ? Color.WHITE : getResources().getColor(R.color.muted, getTheme()));

            btn.setOnClickListener(v -> {
                if (selectedDays.contains(dayIdx)) {
                    selectedDays.remove(Integer.valueOf(dayIdx));
                } else {
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
        txtStepIndicator.setText(step == 0 ? "PASSO 1 DE 2 · OBJETIVOS" : "PASSO 2 DE 2 · RITMO");

        int accent = getResources().getColor(R.color.accent, getTheme());
        int inactive = getResources().getColor(R.color.ink_10, getTheme());
        stepBar1.setBackgroundColor(accent);
        stepBar2.setBackgroundColor(step >= 1 ? accent : inactive);

        step0Container.setVisibility(step == 0 ? View.VISIBLE : View.GONE);
        step1Container.setVisibility(step == 1 ? View.VISIBLE : View.GONE);

        btnBack.setVisibility(step > 0 ? View.VISIBLE : View.INVISIBLE);
        btnNext.setText(step == 0 ? "Continuar →" : "Começar a estudar →");

        boolean canNext = step == 0
                ? (edtName.getText().toString().trim().length() > 1
                        && isSchoolingReady()
                        && !selectedExams.isEmpty())
                : !selectedDays.isEmpty();

        btnNext.setEnabled(canNext);
        btnNext.setAlpha(canNext ? 1.0f : 0.35f);
    }

    private void finishOnboarding() {
        String name = edtName.getText().toString().trim();
        String course = edtCourse.getText().toString().trim();
        String city = spinnerCity.getSelectedItem() != null
                ? spinnerCity.getSelectedItem().toString()
                : "Manaus";

        boolean inEm = Boolean.TRUE.equals(inHighSchool);
        int year = inEm ? highSchoolYear : 0;

        FocoVestRepository repo = FocoVestRepository.getInstance(this);
        repo.updateUserOnboarding(loggedUserId, name, city, course, inEm, year,
                selectedExams, dailyGoal, selectedDays);

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
