package com.example.focovest.ui.fragment;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.focovest.R;
import com.example.focovest.data.FocoVestRepository;
import com.example.focovest.domain.PlanEngine;
import com.example.focovest.domain.PlanEngine.PlanDay;
import com.example.focovest.domain.PlanEngine.PlanResult;
import com.example.focovest.model.ExamId;
import com.example.focovest.model.SubjectId;
import com.example.focovest.model.Topic;
import com.example.focovest.model.User;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Planos de estudo dedicados e independentes por objetivo (ENEM, PSC 1/2/3, SIS 1/2/3 e Macro).
 * Cada aba segue o conteúdo programático oficial daquela prova/etapa, com visualização em
 * Cronograma diário ou agrupada Por disciplina.
 */
public class PlanFragment extends Fragment {

    private static final String STATE_SELECTED_EXAM = "selected_exam";

    private LinearLayout root;
    private ExamId selectedExam = null;
    private String viewMode = "cronograma"; // "cronograma" | "disciplinas"
    private int daysShown = 15;
    private boolean showCompletedList = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_scroll_container, container, false);
        root = v.findViewById(R.id.rootContainer);

        if (savedInstanceState != null) {
            selectedExam = ExamId.fromCode(savedInstanceState.getString(STATE_SELECTED_EXAM, null));
        }

        render();
        return v;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (selectedExam != null) {
            outState.putString(STATE_SELECTED_EXAM, selectedExam.id);
        }
    }

    private void render() {
        root.removeAllViews();

        FocoVestRepository repo = FocoVestRepository.getInstance(requireContext());
        User user = repo.getOrCreateLocalUser();
        if (user == null) return;
        String userId = user.id;

        List<ExamId> userExams = user.targetExams;
        if (selectedExam == null || !userExams.contains(selectedExam)) {
            selectedExam = userExams.get(0);
        }

        List<Topic> allTopics = repo.getAllTopics();
        Set<String> completedIds = repo.getCompletedTopicIds(userId);
        Map<String, String> completedAtMap = repo.getCompletedAtMap(userId);

        PlanResult plan = PlanEngine.buildPlanForExam(
                selectedExam, user.highSchoolYear, allTopics, user.dailyHours, user.studyDays, completedIds, completedAtMap);

        String selectedExamLabel = user.getExamLabel(selectedExam);
        addEyebrow("PLANOS POR OBJETIVO · " + user.getSchoolingLabel().toUpperCase(Locale.ROOT));
        addTitle("Plano " + selectedExamLabel + " · " + selectedExam.institution);

        // Seletor de objetivos (1 aba independente por vestibular escolhido)
        renderExamSelector(user, userExams);

        // Resumo do plano dedicado
        renderPlanSummary(user, plan, selectedExamLabel);

        // Alternador Cronograma x Por disciplina
        renderViewModeToggle(plan);

        if ("disciplinas".equals(viewMode)) {
            renderPlanBySubject(user, plan, completedIds, selectedExamLabel);
        } else {
            // Cronograma de tópicos pendentes daquela prova
            renderPlanDays(user, plan, completedIds, selectedExamLabel);

            // Tópicos já concluídos refletidos neste edital
            renderCompletedSection(user, plan, selectedExamLabel);
        }
    }

    private void renderViewModeToggle(PlanResult plan) {
        LinearLayout bar = new LinearLayout(requireContext());
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setBackgroundResource(R.drawable.bg_input);
        bar.setPadding(dp(4), dp(4), dp(4), dp(4));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, dp(14));
        bar.setLayoutParams(lp);

        Map<SubjectId, List<Topic>> bySubject = PlanEngine.groupBySubject(plan.edital);
        bar.addView(createModeTab("Cronograma", "cronograma"));
        bar.addView(createModeTab("Por disciplina (" + bySubject.size() + ")", "disciplinas"));
        root.addView(bar);
    }

    private TextView createModeTab(String label, String modeId) {
        boolean active = modeId.equals(viewMode);
        TextView tv = new TextView(requireContext());
        tv.setLayoutParams(new LinearLayout.LayoutParams(0, dp(38), 1f));
        tv.setGravity(android.view.Gravity.CENTER);
        tv.setText(label);
        tv.setTextSize(13f);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setBackgroundResource(active ? R.drawable.bg_card_dark : 0);
        tv.setTextColor(getColorRes(active ? R.color.lime : R.color.muted));
        tv.setOnClickListener(v -> {
            if (!modeId.equals(viewMode)) {
                viewMode = modeId;
                render();
            }
        });
        return tv;
    }

    private void renderPlanBySubject(User user, PlanResult plan, Set<String> completedIds, String examLabel) {
        Map<SubjectId, List<Topic>> grouped = PlanEngine.groupBySubject(plan.edital);
        for (SubjectId sub : SubjectId.values()) {
            List<Topic> topics = grouped.get(sub);
            if (topics == null || topics.isEmpty()) continue;

            int doneInSub = 0;
            for (Topic t : topics) {
                if (completedIds.contains(t.id)) doneInSub++;
            }

            LinearLayout subCard = createCard(false);

            LinearLayout headerRow = new LinearLayout(requireContext());
            headerRow.setOrientation(LinearLayout.HORIZONTAL);
            headerRow.setGravity(android.view.Gravity.CENTER_VERTICAL);

            TextView tvSubName = new TextView(requireContext());
            tvSubName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            tvSubName.setText(sub.name);
            tvSubName.setTextColor(sub.getColorInt());
            tvSubName.setTypeface(null, Typeface.BOLD);
            tvSubName.setTextSize(16f);

            TextView tvSubCount = new TextView(requireContext());
            tvSubCount.setText(doneInSub + "/" + topics.size() + " concluídos");
            tvSubCount.setTextColor(getColorRes(R.color.muted));
            tvSubCount.setTextSize(12f);
            tvSubCount.setTypeface(null, Typeface.BOLD);

            headerRow.addView(tvSubName);
            headerRow.addView(tvSubCount);
            subCard.addView(headerRow);

            for (Topic t : topics) {
                boolean done = completedIds.contains(t.id);
                int inc = t.getWeight(selectedExam, user.highSchoolYear);
                subCard.addView(buildTopicRow(t, done, inc, examLabel));
            }
            root.addView(subCard);
        }
    }

    private void renderExamSelector(User user, List<ExamId> userExams) {
        LinearLayout bar = createCard(false);

        TextView hint = new TextView(requireContext());
        hint.setText("Selecione o vestibular para ver seu cronograma exclusivo:");
        hint.setTextColor(getColorRes(R.color.muted));
        hint.setTextSize(12f);
        hint.setPadding(0, 0, 0, dp(10));
        bar.addView(hint);

        HorizontalScrollView hsv = new HorizontalScrollView(requireContext());
        hsv.setScrollBarSize(0);
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);

        for (ExamId exam : userExams) {
            boolean active = exam == selectedExam;
            TextView chip = new TextView(requireContext());
            chip.setText((active ? "✓ " : "") + user.getExamLabel(exam) + " (" + exam.institution + ")");
            chip.setPadding(dp(16), dp(10), dp(16), dp(10));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, dp(8), 0);
            chip.setLayoutParams(lp);
            chip.setBackgroundResource(active ? R.drawable.bg_card_dark : R.drawable.bg_input);
            chip.setTextColor(active ? getColorRes(R.color.lime) : getColorRes(R.color.ink));
            chip.setTypeface(null, Typeface.BOLD);
            chip.setTextSize(13f);
            chip.setOnClickListener(v -> {
                selectedExam = exam;
                daysShown = 15;
                showCompletedList = false;
                render();
            });
            row.addView(chip);
        }

        hsv.addView(row);
        bar.addView(hsv);

        TextView tvDesc = new TextView(requireContext());
        tvDesc.setText(user.getExamDesc(selectedExam));
        tvDesc.setTextColor(getColorRes(R.color.muted));
        tvDesc.setTextSize(12f);
        tvDesc.setPadding(0, dp(10), 0, 0);
        bar.addView(tvDesc);

        root.addView(bar);
    }

    private void renderPlanSummary(User user, PlanResult plan, String examLabel) {
        LinearLayout card = createCard(true);

        int doneCount = plan.completedTopics.size();
        int total = plan.edital.size();
        int pct = total > 0 ? Math.round((doneCount * 100f) / total) : 0;

        TextView tvStats = new TextView(requireContext());
        tvStats.setText(doneCount + "/" + total + " tópicos concluídos no " + examLabel + " (" + pct + "%)");
        tvStats.setTypeface(null, Typeface.BOLD);
        tvStats.setTextColor(getColorRes(R.color.lime));
        tvStats.setTextSize(16f);
        card.addView(tvStats);

        TextView tvPace = new TextView(requireContext());
        tvPace.setText("Ritmo: " + user.dailyHours + "h/dia (≈ " + plan.perDay + " tópicos/dia · "
                + user.studyDays.size() + " dias/sem)");
        tvPace.setTextColor(Color.WHITE);
        tvPace.setTextSize(13f);
        tvPace.setPadding(0, dp(4), 0, 0);
        card.addView(tvPace);

        ProgressBar pb = new ProgressBar(requireContext(), null, android.R.attr.progressBarStyleHorizontal);
        LinearLayout.LayoutParams pblp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(8));
        pblp.setMargins(0, dp(12), 0, dp(12));
        pb.setLayoutParams(pblp);
        pb.setMax(100);
        pb.setProgress(pct);
        pb.setProgressTintList(ColorStateList.valueOf(getColorRes(R.color.lime)));
        card.addView(pb);

        if (plan.endDate != null) {
            TextView tvEnd = new TextView(requireContext());
            tvEnd.setText("Previsão de término do edital " + examLabel + ": " + PlanEngine.fmtShort(plan.endDate));
            tvEnd.setTextColor(Color.parseColor("#B3FFFFFF"));
            tvEnd.setTextSize(12f);
            card.addView(tvEnd);
        } else {
            TextView tvDoneAll = new TextView(requireContext());
            tvDoneAll.setText("🎉 Todos os tópicos do edital " + examLabel + " foram concluídos!");
            tvDoneAll.setTextColor(Color.WHITE);
            tvDoneAll.setTextSize(13f);
            card.addView(tvDoneAll);
        }

        TextView btnAdjust = new TextView(requireContext());
        btnAdjust.setText("Ajustar vestibulares ou ritmo em Configurações →");
        btnAdjust.setTextColor(getColorRes(R.color.lime));
        btnAdjust.setTextSize(12f);
        btnAdjust.setTypeface(null, Typeface.BOLD);
        btnAdjust.setPadding(0, dp(10), 0, 0);
        btnAdjust.setOnClickListener(v -> NavHostFragment.findNavController(this)
                .navigate(R.id.action_global_settings));
        card.addView(btnAdjust);

        root.addView(card);
    }

    private void renderPlanDays(User user, PlanResult plan, Set<String> completedIds, String examLabel) {
        if (plan.days.isEmpty()) return;

        int maxDays = Math.min(plan.days.size(), daysShown);
        for (int i = 0; i < maxDays; i++) {
            PlanDay d = plan.days.get(i);
            LinearLayout dayCard = createCard(false);

            TextView tvDate = new TextView(requireContext());
            tvDate.setText(PlanEngine.fmtShort(d.date).toUpperCase(Locale.ROOT));
            tvDate.setTextColor(getColorRes(R.color.accent));
            tvDate.setTypeface(null, Typeface.BOLD);
            tvDate.setTextSize(12f);
            dayCard.addView(tvDate);

            for (Topic t : d.topics) {
                boolean done = completedIds.contains(t.id);
                int inc = t.getWeight(selectedExam, user.highSchoolYear);
                dayCard.addView(buildTopicRow(t, done, inc, examLabel));
            }
            root.addView(dayCard);
        }

        if (plan.days.size() > maxDays) {
            MaterialButton btnMore = new MaterialButton(requireContext());
            btnMore.setText("Mostrar mais dias (" + (plan.days.size() - maxDays) + " restantes)");
            btnMore.setAllCaps(false);
            btnMore.setBackgroundTintList(ColorStateList.valueOf(getColorRes(R.color.ink)));
            btnMore.setTextColor(Color.WHITE);
            LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            blp.setMargins(0, 0, 0, dp(16));
            btnMore.setLayoutParams(blp);
            btnMore.setOnClickListener(v -> {
                daysShown += 15;
                render();
            });
            root.addView(btnMore);
        }
    }

    private void renderCompletedSection(User user, PlanResult plan, String examLabel) {
        if (plan.completedTopics.isEmpty()) return;

        LinearLayout card = createCard(false);
        TextView tvToggle = new TextView(requireContext());
        tvToggle.setText((showCompletedList ? "▾ " : "▸ ") + "Tópicos concluídos neste edital ("
                + plan.completedTopics.size() + ")");
        tvToggle.setTextColor(getColorRes(R.color.success_dark));
        tvToggle.setTypeface(null, Typeface.BOLD);
        tvToggle.setTextSize(15f);
        tvToggle.setOnClickListener(v -> {
            showCompletedList = !showCompletedList;
            render();
        });
        card.addView(tvToggle);

        if (showCompletedList) {
            for (Topic t : plan.completedTopics) {
                int inc = t.getWeight(selectedExam, user.highSchoolYear);
                card.addView(buildTopicRow(t, true, inc, examLabel));
            }
        }
        root.addView(card);
    }

    private View buildTopicRow(Topic t, boolean done, int incidence, String examLabel) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(dp(14), dp(11), dp(14), dp(11));
        LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rlp.setMargins(0, dp(8), 0, 0);
        row.setLayoutParams(rlp);
        row.setBackgroundResource(done ? R.drawable.bg_pill_success : R.drawable.bg_input);

        TextView tvT = new TextView(requireContext());
        tvT.setText((done ? "✓ " : "") + t.title);
        tvT.setTypeface(null, Typeface.BOLD);
        tvT.setTextColor(getColorRes(done ? R.color.success_dark : R.color.ink));

        TextView tvMeta = new TextView(requireContext());
        tvMeta.setText(t.subject.name + " · " + t.minutes + " min · Incidência "
                + PlanEngine.getIncidenceLabel(incidence) + " no " + examLabel);
        tvMeta.setTextColor(t.subject.getColorInt());
        tvMeta.setTextSize(12f);

        row.addView(tvT);
        row.addView(tvMeta);
        row.setOnClickListener(v -> NavHostFragment.findNavController(this)
                .navigate(R.id.action_global_study, StudyRoomFragment.argsForTopic(t.id)));
        return row;
    }

    private void addEyebrow(String text) {
        TextView tv = new TextView(requireContext());
        tv.setText(text);
        tv.setTextColor(getColorRes(R.color.accent));
        tv.setTextSize(11f);
        tv.setTypeface(null, Typeface.BOLD);
        root.addView(tv);
    }

    private void addTitle(String text) {
        TextView tv = new TextView(requireContext());
        tv.setText(text);
        tv.setTextColor(getColorRes(R.color.ink));
        tv.setTextSize(26f);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setPadding(0, dp(2), 0, dp(12));
        root.addView(tv);
    }

    private LinearLayout createCard(boolean dark) {
        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(16), dp(18), dp(16));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, dp(14));
        card.setLayoutParams(lp);
        card.setBackgroundResource(dark ? R.drawable.bg_card_dark : R.drawable.bg_card);
        return card;
    }

    private int getColorRes(int res) {
        return getResources().getColor(res, requireContext().getTheme());
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
