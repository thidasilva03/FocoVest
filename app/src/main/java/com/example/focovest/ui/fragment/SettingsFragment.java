package com.example.focovest.ui.fragment;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.focovest.R;
import com.example.focovest.data.FocoVestRepository;
import com.example.focovest.model.ExamId;
import com.example.focovest.model.User;
import com.example.focovest.ui.dialog.ConfirmResetDialogFragment;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Configurações locais do estudante:
 * - Editar perfil e objetivos (vestibulares)
 * - Alterar ritmo de estudo (horas/dia e dias da semana)
 * - Reiniciar progresso salvo localmente
 */
public class SettingsFragment extends Fragment {

    private static final String[] DAY_LABELS = {"D", "S", "T", "Q", "Q", "S", "S"};

    private FocoVestRepository repo;
    private String userId;
    private LinearLayout root;

    // Estado de ritmo atualizado sem perder rascunho dos campos de texto
    private int currentDailyHours = 3;
    private final List<Integer> currentStudyDays = new ArrayList<>();
    private TextView tvGoalLabel;
    private LinearLayout daysRowContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_scroll_container, container, false);
        root = v.findViewById(R.id.rootContainer);
        repo = FocoVestRepository.getInstance(requireContext());
        userId = FocoVestRepository.LOCAL_USER_ID;

        getParentFragmentManager().setFragmentResultListener(
                ConfirmResetDialogFragment.REQUEST_KEY,
                getViewLifecycleOwner(),
                (requestKey, bundle) -> {
                    if (bundle.getBoolean(ConfirmResetDialogFragment.RESULT_CONFIRMED, false)) {
                        repo.resetUserProgress(userId);
                        Toast.makeText(requireContext(),
                                "Progresso reiniciado com sucesso.", Toast.LENGTH_SHORT).show();
                        render();
                    }
                }
        );

        render();
        return v;
    }

    private void render() {
        root.removeAllViews();
        User user = repo.getOrCreateLocalUser();
        if (user == null) return;

        currentDailyHours = user.dailyHours;
        currentStudyDays.clear();
        currentStudyDays.addAll(user.studyDays);

        TextView eyebrow = new TextView(requireContext());
        eyebrow.setText("CONFIGURAÇÕES");
        eyebrow.setTextColor(getColorRes(R.color.accent));
        eyebrow.setTypeface(null, Typeface.BOLD);
        eyebrow.setTextSize(11f);
        root.addView(eyebrow);

        TextView title = new TextView(requireContext());
        title.setText("Seu perfil e preferências.");
        title.setTextColor(getColorRes(R.color.ink));
        title.setTypeface(null, Typeface.BOLD);
        title.setTextSize(26f);
        title.setPadding(0, dp(2), 0, dp(12));
        root.addView(title);

        // 1. PERFIL E OBJETIVOS
        LinearLayout profileCard = createCard();
        addSectionTitle(profileCard, "Perfil e vestibulares");

        EditText edtName = new EditText(requireContext());
        edtName.setText(user.name);
        edtName.setHint("Seu nome");
        edtName.setBackgroundResource(R.drawable.bg_input);
        edtName.setPadding(dp(12), dp(10), dp(12), dp(10));
        edtName.setTextColor(getColorRes(R.color.ink));
        profileCard.addView(edtName);

        EditText edtCourse = new EditText(requireContext());
        edtCourse.setText(user.targetCourse);
        edtCourse.setHint("Curso desejado (Ex.: Medicina)");
        edtCourse.setBackgroundResource(R.drawable.bg_input);
        edtCourse.setPadding(dp(12), dp(10), dp(12), dp(10));
        edtCourse.setTextColor(getColorRes(R.color.ink));
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        clp.setMargins(0, dp(10), 0, dp(10));
        edtCourse.setLayoutParams(clp);
        profileCard.addView(edtCourse);

        Spinner citySpinner = new Spinner(requireContext());
        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, User.AM_CITIES);
        citySpinner.setAdapter(cityAdapter);
        int cityIdx = User.AM_CITIES.indexOf(user.city);
        if (cityIdx >= 0) citySpinner.setSelection(cityIdx);
        profileCard.addView(citySpinner);

        // Situação no Ensino Médio (controla vestibulares e etapas dedicadas)
        final boolean[] draftInHighSchool = {user.inHighSchool};
        final int[] draftHighSchoolYear = {user.inHighSchool ? Math.max(1, Math.min(3, user.highSchoolYear)) : 0};
        final List<ExamId> draftExams = new ArrayList<>(
                ExamId.filterEligible(user.targetExams, draftInHighSchool[0], draftHighSchoolYear[0]));

        TextView tvHsLabel = new TextView(requireContext());
        tvHsLabel.setText("Você ainda está no Ensino Médio?");
        tvHsLabel.setTextColor(getColorRes(R.color.ink));
        tvHsLabel.setTypeface(null, Typeface.BOLD);
        tvHsLabel.setTextSize(13f);
        tvHsLabel.setPadding(0, dp(14), 0, dp(6));
        profileCard.addView(tvHsLabel);

        LinearLayout hsToggleRow = new LinearLayout(requireContext());
        hsToggleRow.setOrientation(LinearLayout.HORIZONTAL);
        TextView btnHsYes = createToggleChip("Sim, estou no EM", dp(6), true);
        TextView btnHsNo = createToggleChip("Não, já concluí", dp(6), false);
        hsToggleRow.addView(btnHsYes);
        hsToggleRow.addView(btnHsNo);
        profileCard.addView(hsToggleRow);

        LinearLayout yearSection = new LinearLayout(requireContext());
        yearSection.setOrientation(LinearLayout.VERTICAL);
        yearSection.setPadding(0, dp(12), 0, 0);

        TextView tvYearLabel = new TextView(requireContext());
        tvYearLabel.setText("Série do Ensino Médio (define a etapa do PSC e SIS):");
        tvYearLabel.setTextColor(getColorRes(R.color.ink));
        tvYearLabel.setTypeface(null, Typeface.BOLD);
        tvYearLabel.setTextSize(13f);
        tvYearLabel.setPadding(0, 0, 0, dp(6));
        yearSection.addView(tvYearLabel);

        LinearLayout yearRow = new LinearLayout(requireContext());
        yearRow.setOrientation(LinearLayout.HORIZONTAL);
        TextView btnY1 = createYearChip("1ª série");
        TextView btnY2 = createYearChip("2ª série");
        TextView btnY3 = createYearChip("3ª série");
        yearRow.addView(btnY1);
        yearRow.addView(btnY2);
        yearRow.addView(btnY3);
        yearSection.addView(yearRow);
        profileCard.addView(yearSection);

        TextView tvExamsLabel = new TextView(requireContext());
        tvExamsLabel.setTextColor(getColorRes(R.color.muted));
        tvExamsLabel.setTextSize(12f);
        tvExamsLabel.setPadding(0, dp(12), 0, dp(4));
        profileCard.addView(tvExamsLabel);

        LinearLayout examsCardContainer = new LinearLayout(requireContext());
        examsCardContainer.setOrientation(LinearLayout.VERTICAL);
        profileCard.addView(examsCardContainer);

        final Runnable[] refreshSchoolingAndExams = new Runnable[1];
        refreshSchoolingAndExams[0] = () -> {
            boolean inEm = draftInHighSchool[0];
            int yr = inEm ? draftHighSchoolYear[0] : 0;

            styleChip(btnHsYes, inEm);
            styleChip(btnHsNo, !inEm);
            yearSection.setVisibility(inEm ? View.VISIBLE : View.GONE);
            styleChip(btnY1, inEm && yr == 1);
            styleChip(btnY2, inEm && yr == 2);
            styleChip(btnY3, inEm && yr == 3);

            List<ExamId> eligible = ExamId.filterEligible(draftExams, inEm, yr);
            draftExams.clear();
            draftExams.addAll(eligible);

            if (!inEm) {
                tvExamsLabel.setText("Objetivos disponíveis (Ensino Médio concluído: Macro e ENEM):");
            } else if (yr < 3) {
                tvExamsLabel.setText("Objetivos disponíveis na " + yr + "ª série (Macro exige 3ª série ou EM concluído):");
            } else {
                tvExamsLabel.setText("Objetivos disponíveis na 3ª série (gera 1 plano dedicado por vestibular):");
            }

            examsCardContainer.removeAllViews();
            int padH = dp(14);
            int padV = dp(11);
            int marginB = dp(8);

            for (ExamId ex : ExamId.availableFor(inEm, yr)) {
                boolean isSelected = draftExams.contains(ex);
                LinearLayout row = new LinearLayout(requireContext());
                row.setOrientation(LinearLayout.VERTICAL);
                row.setPadding(padH, padV, padH, padV);
                LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                rlp.setMargins(0, 0, 0, marginB);
                row.setLayoutParams(rlp);
                row.setBackgroundResource(isSelected ? R.drawable.bg_button_ink : R.drawable.bg_input);

                TextView tvExamTitle = new TextView(requireContext());
                tvExamTitle.setText((isSelected ? "✓ " : "") + ex.getDisplayLabel(inEm, yr) + " · " + ex.institution);
                tvExamTitle.setTextSize(14f);
                tvExamTitle.setTypeface(null, Typeface.BOLD);
                tvExamTitle.setTextColor(isSelected ? getColorRes(R.color.lime) : getColorRes(R.color.ink));

                TextView tvExamDesc = new TextView(requireContext());
                tvExamDesc.setText(ex.getContextualDesc(inEm, yr));
                tvExamDesc.setTextSize(12f);
                tvExamDesc.setTextColor(isSelected ? Color.parseColor("#B3FFFFFF") : getColorRes(R.color.muted));
                tvExamDesc.setPadding(0, dp(2), 0, 0);

                row.addView(tvExamTitle);
                row.addView(tvExamDesc);
                row.setOnClickListener(v -> {
                    if (draftExams.contains(ex)) {
                        draftExams.remove(ex);
                    } else {
                        draftExams.add(ex);
                    }
                    refreshSchoolingAndExams[0].run();
                });
                examsCardContainer.addView(row);
            }
        };

        btnHsYes.setOnClickListener(v -> {
            draftInHighSchool[0] = true;
            if (draftHighSchoolYear[0] < 1 || draftHighSchoolYear[0] > 3) {
                draftHighSchoolYear[0] = 3;
            }
            refreshSchoolingAndExams[0].run();
        });
        btnHsNo.setOnClickListener(v -> {
            draftInHighSchool[0] = false;
            draftHighSchoolYear[0] = 0;
            refreshSchoolingAndExams[0].run();
        });
        btnY1.setOnClickListener(v -> {
            draftInHighSchool[0] = true;
            draftHighSchoolYear[0] = 1;
            refreshSchoolingAndExams[0].run();
        });
        btnY2.setOnClickListener(v -> {
            draftInHighSchool[0] = true;
            draftHighSchoolYear[0] = 2;
            refreshSchoolingAndExams[0].run();
        });
        btnY3.setOnClickListener(v -> {
            draftInHighSchool[0] = true;
            draftHighSchoolYear[0] = 3;
            refreshSchoolingAndExams[0].run();
        });

        refreshSchoolingAndExams[0].run();

        MaterialButton btnSaveProfile = new MaterialButton(requireContext());
        btnSaveProfile.setText("Salvar perfil e objetivos");
        btnSaveProfile.setAllCaps(false);
        btnSaveProfile.setBackgroundTintList(ColorStateList.valueOf(getColorRes(R.color.ink)));
        btnSaveProfile.setTextColor(Color.WHITE);
        btnSaveProfile.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        slp.setMargins(0, dp(8), 0, 0);
        btnSaveProfile.setLayoutParams(slp);
        btnSaveProfile.setOnClickListener(v -> {
            if (draftExams.isEmpty()) {
                Toast.makeText(requireContext(), "Selecione ao menos um vestibular.", Toast.LENGTH_SHORT).show();
                return;
            }
            String newName = edtName.getText().toString().trim();
            if (newName.length() < 2) {
                Toast.makeText(requireContext(), "Informe um nome válido.", Toast.LENGTH_SHORT).show();
                return;
            }
            String newCourse = edtCourse.getText().toString().trim();
            String newCity = citySpinner.getSelectedItem() != null
                    ? citySpinner.getSelectedItem().toString()
                    : user.city;
            repo.updateUserProfile(userId, newName, newCity, newCourse,
                    draftInHighSchool[0], draftHighSchoolYear[0], draftExams);
            Toast.makeText(requireContext(), "✓ Perfil e objetivos salvos!", Toast.LENGTH_SHORT).show();
        });
        profileCard.addView(btnSaveProfile);
        root.addView(profileCard);

        // 2. RITMO DE ESTUDO (sem destruir os campos de texto acima)
        LinearLayout planCard = createCard();
        addSectionTitle(planCard, "Ritmo de estudo");

        LinearLayout hoursRow = new LinearLayout(requireContext());
        hoursRow.setOrientation(LinearLayout.HORIZONTAL);
        hoursRow.setGravity(Gravity.CENTER_VERTICAL);

        MaterialButton btnMinus = createRhythmStepButton("−");
        btnMinus.setOnClickListener(v -> {
            currentDailyHours = Math.max(1, currentDailyHours - 1);
            repo.updateUserRhythm(userId, currentDailyHours, currentStudyDays);
            refreshRhythmWidgets();
        });

        tvGoalLabel = new TextView(requireContext());
        tvGoalLabel.setTypeface(null, Typeface.BOLD);
        tvGoalLabel.setTextSize(15f);
        tvGoalLabel.setTextColor(getColorRes(R.color.ink));
        tvGoalLabel.setGravity(Gravity.CENTER);
        tvGoalLabel.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        MaterialButton btnPlus = createRhythmStepButton("+");
        btnPlus.setOnClickListener(v -> {
            currentDailyHours = Math.min(12, currentDailyHours + 1);
            repo.updateUserRhythm(userId, currentDailyHours, currentStudyDays);
            refreshRhythmWidgets();
        });

        hoursRow.addView(btnMinus);
        hoursRow.addView(tvGoalLabel);
        hoursRow.addView(btnPlus);
        planCard.addView(hoursRow);

        daysRowContainer = new LinearLayout(requireContext());
        daysRowContainer.setOrientation(LinearLayout.HORIZONTAL);
        daysRowContainer.setPadding(0, dp(14), 0, 0);
        planCard.addView(daysRowContainer);

        refreshRhythmWidgets();
        root.addView(planCard);

        // 3. REINICIAR PROGRESSO LOCAL
        LinearLayout resetCard = createCard();
        addSectionTitle(resetCard, "Dados e progresso");

        TextView tvResetHint = new TextView(requireContext());
        tvResetHint.setText("Limpa tópicos concluídos, respostas, anotações e simulados salvos neste aparelho.");
        tvResetHint.setTextColor(getColorRes(R.color.muted));
        tvResetHint.setTextSize(12f);
        tvResetHint.setPadding(0, 0, 0, dp(10));
        resetCard.addView(tvResetHint);

        MaterialButton btnReset = new MaterialButton(requireContext());
        btnReset.setText("↺ Reiniciar meu progresso");
        btnReset.setAllCaps(false);
        btnReset.setBackgroundTintList(ColorStateList.valueOf(getColorRes(R.color.accent)));
        btnReset.setTextColor(Color.WHITE);
        btnReset.setTypeface(null, Typeface.BOLD);
        btnReset.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        btnReset.setOnClickListener(v -> ConfirmResetDialogFragment.newInstance(
                getString(R.string.dialog_confirm_reset_title),
                getString(R.string.dialog_confirm_reset_message)
        ).show(getParentFragmentManager(), "confirm_reset_dialog"));
        resetCard.addView(btnReset);
        root.addView(resetCard);
    }

    private void refreshRhythmWidgets() {
        if (tvGoalLabel != null) {
            tvGoalLabel.setText(currentDailyHours + "h/dia (" + (currentDailyHours * currentStudyDays.size()) + "h/sem)");
        }
        if (daysRowContainer == null) return;
        daysRowContainer.removeAllViews();

        int h = dp(42);
        int m = dp(3);
        for (int i = 0; i < 7; i++) {
            final int dayIdx = i;
            boolean on = currentStudyDays.contains(dayIdx);
            TextView dBtn = new TextView(requireContext());
            LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(0, h, 1f);
            dlp.setMargins(m, 0, m, 0);
            dBtn.setLayoutParams(dlp);
            dBtn.setGravity(Gravity.CENTER);
            dBtn.setText(DAY_LABELS[i]);
            dBtn.setTypeface(null, Typeface.BOLD);
            dBtn.setBackgroundResource(on ? R.drawable.bg_button_accent : R.drawable.bg_input);
            dBtn.setTextColor(on ? Color.WHITE : getColorRes(R.color.muted));
            dBtn.setOnClickListener(v -> {
                if (on && currentStudyDays.size() > 1) {
                    currentStudyDays.remove(Integer.valueOf(dayIdx));
                } else if (!on) {
                    currentStudyDays.add(dayIdx);
                    Collections.sort(currentStudyDays);
                }
                repo.updateUserRhythm(userId, currentDailyHours, currentStudyDays);
                refreshRhythmWidgets();
            });
            daysRowContainer.addView(dBtn);
        }
    }

    private void addSectionTitle(LinearLayout parent, String text) {
        TextView tv = new TextView(requireContext());
        tv.setText(text);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextSize(18f);
        tv.setTextColor(getColorRes(R.color.ink));
        tv.setPadding(0, 0, 0, dp(10));
        parent.addView(tv);
    }

    private LinearLayout createCard() {
        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(16), dp(18), dp(16));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, dp(14));
        card.setLayoutParams(lp);
        card.setBackgroundResource(R.drawable.bg_card);
        return card;
    }

    private TextView createToggleChip(String label, int gapPx, boolean isLeft) {
        TextView tv = new TextView(requireContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(44), 1f);
        lp.setMargins(isLeft ? 0 : gapPx / 2, 0, isLeft ? gapPx / 2 : 0, 0);
        tv.setLayoutParams(lp);
        tv.setGravity(Gravity.CENTER);
        tv.setText(label);
        tv.setTextSize(13f);
        tv.setTypeface(null, Typeface.BOLD);
        return tv;
    }

    private TextView createYearChip(String label) {
        TextView tv = new TextView(requireContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(42), 1f);
        lp.setMargins(dp(3), 0, dp(3), 0);
        tv.setLayoutParams(lp);
        tv.setGravity(Gravity.CENTER);
        tv.setText(label);
        tv.setTextSize(13f);
        tv.setTypeface(null, Typeface.BOLD);
        return tv;
    }

    private MaterialButton createRhythmStepButton(String symbol) {
        MaterialButton btn = new MaterialButton(requireContext());
        btn.setText(symbol);
        btn.setTextSize(18f);
        btn.setTypeface(null, Typeface.BOLD);
        btn.setTextColor(Color.WHITE);
        btn.setBackgroundTintList(ColorStateList.valueOf(getColorRes(R.color.ink)));
        btn.setInsetTop(0);
        btn.setInsetBottom(0);
        btn.setMinWidth(0);
        btn.setMinimumWidth(0);
        btn.setPadding(0, 0, 0, 0);
        btn.setGravity(Gravity.CENTER);
        btn.setLayoutParams(new LinearLayout.LayoutParams(dp(44), dp(44)));
        return btn;
    }

    private void styleChip(TextView tv, boolean selected) {
        tv.setBackgroundResource(selected ? R.drawable.bg_button_ink : R.drawable.bg_input);
        tv.setTextColor(getColorRes(selected ? R.color.lime : R.color.ink));
    }

    private int getColorRes(int res) {
        return getResources().getColor(res, requireContext().getTheme());
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
