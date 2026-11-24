package com.example.focovest.ui.fragment;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.focovest.model.Question;
import com.example.focovest.model.SubjectId;
import com.example.focovest.model.Topic;
import com.example.focovest.model.User;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class DashboardFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dashboard, container, false);

        FocoVestRepository repo = FocoVestRepository.getInstance(requireContext());
        User user = repo.getOrCreateLocalUser();
        if (user == null) return v;
        String userId = user.id;

        List<Topic> allTopics = repo.getAllTopics();
        Set<String> completedIds = repo.getCompletedTopicIds(userId);
        Map<String, String> completedAtMap = repo.getCompletedAtMap(userId);

        ExamId primaryExam = user.targetExams.get(0);
        PlanResult plan = PlanEngine.buildPlanForExam(
                primaryExam, user.highSchoolYear, allTopics, user.dailyHours, user.studyDays, completedIds, completedAtMap);

        // União de tópicos e disciplinas pertencentes aos objetivos/etapa escolhidos pelo estudante
        Set<String> userPlanTopicIds = new java.util.HashSet<>();
        Set<SubjectId> userPlanSubjects = new java.util.HashSet<>();
        for (ExamId ex : user.targetExams) {
            for (Topic t : PlanEngine.editalTopicsForExam(allTopics, ex, user.highSchoolYear)) {
                userPlanTopicIds.add(t.id);
                userPlanSubjects.add(t.subject);
            }
        }
        int completedInPlans = 0;
        for (String tid : userPlanTopicIds) {
            if (completedIds.contains(tid)) completedInPlans++;
        }

        int points = repo.calculateUserPoints(userId);
        int[] fixation = repo.getFixationStats(userId);
        int simCount = repo.getSimuladoCount(userId);

        // Data e saudação
        TextView txtDateEyebrow = v.findViewById(R.id.txtDateEyebrow);
        String dateStr = new SimpleDateFormat("EEEE, d 'de' MMMM", new Locale("pt", "BR")).format(new Date());
        txtDateEyebrow.setText(dateStr.toUpperCase(Locale.ROOT));

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greet = hour < 12 ? "Bom dia" : (hour < 18 ? "Boa tarde" : "Boa noite");
        ((TextView) v.findViewById(R.id.txtGreeting)).setText(greet + ", " + user.name + ".");

        StringBuilder examsLabel = new StringBuilder();
        for (int i = 0; i < user.targetExams.size(); i++) {
            if (i > 0) examsLabel.append(", ");
            examsLabel.append(user.getExamLabel(user.targetExams.get(i)));
        }
        ((TextView) v.findViewById(R.id.txtSubtitleMode)).setText(
                user.getSchoolingLabel() + " · " + examsLabel + " · " + plan.perDay + " tópicos/dia");

        NumberFormat nf = NumberFormat.getIntegerInstance(new Locale("pt", "BR"));
        ((TextView) v.findViewById(R.id.txtStatPoints)).setText(nf.format(points));
        int totalTargetTopics = userPlanTopicIds.isEmpty() ? allTopics.size() : userPlanTopicIds.size();
        ((TextView) v.findViewById(R.id.txtStatTopics)).setText(completedInPlans + "/" + totalTargetTopics);
        ((TextView) v.findViewById(R.id.txtStatFixacao)).setText(fixation[0] + "/" + fixation[1]);
        ((TextView) v.findViewById(R.id.txtStatSimulados)).setText(String.valueOf(simCount));

        // Card Continuar Estudando
        Topic last = repo.getTopicById(user.lastTopicId);
        if (last != null && !userPlanTopicIds.isEmpty() && !userPlanTopicIds.contains(last.id) && !plan.edital.isEmpty()) {
            last = plan.edital.get(0);
        }
        if (last != null) {
            final String openTopicId = last.id;
            LinearLayout cardContinue = v.findViewById(R.id.cardContinueTopic);
            GradientDrawable bgContinue = new GradientDrawable();
            bgContinue.setColor(last.subject.getColorInt());
            bgContinue.setCornerRadius(dp(24));
            cardContinue.setBackground(bgContinue);

            ((TextView) v.findViewById(R.id.txtLastTopicTitle)).setText(last.title);
            ((TextView) v.findViewById(R.id.txtLastTopicSummary)).setText(last.summary);
            cardContinue.setOnClickListener(view -> NavHostFragment.findNavController(this)
                    .navigate(R.id.action_global_study, StudyRoomFragment.argsForTopic(openTopicId)));
        }

        // Próximo dia no plano do primeiro objetivo
        String primaryExamLabel = user.getExamLabel(primaryExam);
        PlanDay nextDay = plan.days.isEmpty() ? null : plan.days.get(0);
        TextView txtTodayPlanHeader = v.findViewById(R.id.txtTodayPlanHeader);
        boolean nextIsToday = nextDay != null
                && PlanEngine.dayKey(nextDay.date).equals(PlanEngine.dayKey(new Date()));
        if (nextDay == null) {
            txtTodayPlanHeader.setText("Plano " + primaryExamLabel + " concluído");
        } else if (nextIsToday) {
            txtTodayPlanHeader.setText("Hoje no plano " + primaryExamLabel);
        } else {
            txtTodayPlanHeader.setText("Próximo dia (" + primaryExamLabel + ") · " + PlanEngine.fmtShort(nextDay.date));
        }

        LinearLayout pendingContainer = v.findViewById(R.id.pendingTopicsContainer);
        pendingContainer.removeAllViews();
        if (nextDay == null) {
            TextView empty = new TextView(requireContext());
            empty.setText("Você concluiu todos os tópicos do plano " + primaryExamLabel + "! Que tal fazer um simulado?");
            empty.setTextColor(getColorRes(R.color.muted));
            pendingContainer.addView(empty);
        } else {
            for (Topic pt : nextDay.topics) {
                LinearLayout item = new LinearLayout(requireContext());
                item.setOrientation(LinearLayout.VERTICAL);
                item.setPadding(dp(14), dp(12), dp(14), dp(12));
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(0, 0, 0, dp(8));
                item.setLayoutParams(lp);
                item.setBackgroundResource(R.drawable.bg_input);

                TextView tvTitle = new TextView(requireContext());
                tvTitle.setText(pt.title);
                tvTitle.setTextColor(getColorRes(R.color.ink));
                tvTitle.setTypeface(null, Typeface.BOLD);

                int inc = pt.getWeight(primaryExam, user.highSchoolYear);
                TextView tvSub = new TextView(requireContext());
                tvSub.setText(pt.subject.name + " · " + pt.minutes + " min · Incidência "
                        + PlanEngine.getIncidenceLabel(inc));
                tvSub.setTextColor(pt.subject.getColorInt());
                tvSub.setTextSize(12f);

                item.addView(tvTitle);
                item.addView(tvSub);
                item.setOnClickListener(view -> NavHostFragment.findNavController(this)
                        .navigate(R.id.action_global_study, StudyRoomFragment.argsForTopic(pt.id)));
                pendingContainer.addView(item);
            }
        }

        v.findViewById(R.id.btnSeeFullPlan).setOnClickListener(
                view -> NavHostFragment.findNavController(this).navigate(R.id.action_global_plan));

        // Desempenho por disciplina (apenas disciplinas presentes nos planos ativos do aluno)
        LinearLayout accContainer = v.findViewById(R.id.subjectAccuracyContainer);
        accContainer.removeAllViews();
        for (SubjectId sub : SubjectId.values()) {
            if (!userPlanSubjects.isEmpty() && !userPlanSubjects.contains(sub)) continue;
            int[] acc = repo.getSubjectAccuracy(userId, sub);
            int pct = acc[1] > 0 ? Math.round((acc[0] * 100f) / acc[1]) : 0;

            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            rlp.setMargins(0, 0, 0, dp(8));
            row.setLayoutParams(rlp);

            TextView tvName = new TextView(requireContext());
            tvName.setLayoutParams(new LinearLayout.LayoutParams(dp(92), LinearLayout.LayoutParams.WRAP_CONTENT));
            tvName.setText(sub.name);
            tvName.setTextSize(13f);
            tvName.setTextColor(getColorRes(R.color.ink));

            ProgressBar pb = new ProgressBar(requireContext(), null, android.R.attr.progressBarStyleHorizontal);
            LinearLayout.LayoutParams pblp = new LinearLayout.LayoutParams(0, dp(8), 1f);
            pblp.setMargins(dp(8), 0, dp(8), 0);
            pb.setLayoutParams(pblp);
            pb.setMax(100);
            pb.setProgress(pct);
            pb.setProgressTintList(ColorStateList.valueOf(sub.getColorInt()));

            TextView tvPct = new TextView(requireContext());
            tvPct.setLayoutParams(new LinearLayout.LayoutParams(dp(40), LinearLayout.LayoutParams.WRAP_CONTENT));
            tvPct.setGravity(Gravity.END);
            tvPct.setText(acc[1] > 0 ? pct + "%" : "—");
            tvPct.setTextSize(12f);
            tvPct.setTextColor(getColorRes(R.color.muted));

            row.addView(tvName);
            row.addView(pb);
            row.addView(tvPct);
            accContainer.addView(row);
        }

        // Card Simulado dos tópicos concluídos
        List<Question> availableSimQuestions = repo.getQuestionsForCompletedTopics(userId, null);
        TextView txtSimName = v.findViewById(R.id.txtSuggestedSimName);
        TextView txtSimMeta = v.findViewById(R.id.txtSuggestedSimMeta);
        if (availableSimQuestions.isEmpty()) {
            txtSimName.setText("Conclua seu primeiro tópico");
            txtSimMeta.setText("Seus simulados serão gerados com base no que você já estudou.");
        } else {
            txtSimName.setText("Gerar simulado personalizado →");
            txtSimMeta.setText(completedIds.size() + " tópico(s) concluído(s) · "
                    + availableSimQuestions.size() + " questões disponíveis");
        }
        v.findViewById(R.id.cardSuggestedSimulado).setOnClickListener(
                view -> NavHostFragment.findNavController(this).navigate(R.id.action_global_simulados));

        return v;
    }

    private int getColorRes(int res) {
        return getResources().getColor(res, requireContext().getTheme());
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
