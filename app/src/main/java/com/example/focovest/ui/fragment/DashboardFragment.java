package com.example.focovest.ui.fragment;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.focovest.R;
import com.example.focovest.data.DataRepository;
import com.example.focovest.data.DataRepository.*;
import com.example.focovest.domain.PlanEngine;
import com.example.focovest.domain.PlanEngine.*;
import com.example.focovest.data.StoreManager;
import com.example.focovest.ui.activity.MainActivity;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class DashboardFragment extends Fragment {

    public static final int USER_BASE_POINTS = 1180;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dashboard, container, false);

        StoreManager store = StoreManager.getInstance(requireContext());
        StoreManager.State state = store.getState();
        PlanResult plan = store.getPlanResult();
        StudyPlan activePlan = store.getActivePlan();
        int points = store.getPoints();

        // Data e saudação
        TextView txtDateEyebrow = v.findViewById(R.id.txtDateEyebrow);
        String dateStr = new SimpleDateFormat("EEEE, d 'de' MMMM", new Locale("pt", "BR")).format(new Date());
        txtDateEyebrow.setText(dateStr.toUpperCase(Locale.ROOT));

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greet = hour < 12 ? "Bom dia" : (hour < 18 ? "Boa tarde" : "Boa noite");
        TextView txtGreeting = v.findViewById(R.id.txtGreeting);
        txtGreeting.setText(greet + ", " + state.name + ".");

        TextView txtSubtitleMode = v.findViewById(R.id.txtSubtitleMode);
        if ("autonoma".equals(state.mode)) {
            txtSubtitleMode.setText("Modo autônomo · " + activePlan.name + " · " + plan.perDay + " tópicos por dia.");
        } else {
            txtSubtitleMode.setText("Turma " + DataRepository.TURMA_NAME + " · Semana " + state.releasedWeeks + " em andamento.");
        }

        // Cálculo da posição no Ranking do Amazonas
        List<RankEntry> ranking = new ArrayList<>(DataRepository.rankingAmazonas());
        RankEntry me = new RankEntry(state.name + " (você)", state.city, USER_BASE_POINTS + points, 6, 2, true);
        ranking.add(me);
        ranking.sort((a, b) -> Integer.compare(b.points, a.points));
        int myPos = 1;
        for (int i = 0; i < ranking.size(); i++) {
            if (ranking.get(i).isUser) {
                myPos = i + 1;
                break;
            }
        }

        // Acertos em fixação
        int correct = 0, answered = 0;
        for (Topic t : DataRepository.TOPICS) {
            if (state.completed.contains(t.id)) {
                for (Question q : t.questions) {
                    answered++;
                    Integer chosen = state.answers.get(q.id);
                    if (chosen != null && chosen == q.answer) correct++;
                }
            }
        }

        NumberFormat nf = NumberFormat.getIntegerInstance(new Locale("pt", "BR"));
        ((TextView) v.findViewById(R.id.txtStatPoints)).setText(nf.format(USER_BASE_POINTS + points));
        ((TextView) v.findViewById(R.id.txtStatRank)).setText(myPos + "º");
        ((TextView) v.findViewById(R.id.txtStatFixacao)).setText(correct + "/" + answered);
        ((TextView) v.findViewById(R.id.txtStatSimulados)).setText(String.valueOf(state.history.size()));

        // Card Continuar Estudando
        Topic last = DataRepository.findTopicById(state.lastTopic);
        if (!store.isTopicUnlocked(last.id)) {
            for (Topic t : DataRepository.TOPICS) {
                if (store.isTopicUnlocked(t.id)) {
                    last = t;
                    break;
                }
            }
        }
        final String openTopicId = last.id;
        LinearLayout cardContinue = v.findViewById(R.id.cardContinueTopic);
        GradientDrawable bgContinue = new GradientDrawable();
        bgContinue.setColor(last.subject.getColorInt());
        bgContinue.setCornerRadius(64f);
        cardContinue.setBackground(bgContinue);

        ((TextView) v.findViewById(R.id.txtLastTopicTitle)).setText(last.title);
        ((TextView) v.findViewById(R.id.txtLastTopicSummary)).setText(last.summary);
        cardContinue.setOnClickListener(view -> ((MainActivity) requireActivity()).openTopic(openTopicId));

        // Próximo dia de estudo com tópicos pendentes
        PlanDay nextDay = null;
        for (PlanDay d : plan.days) {
            for (String tid : d.topicIds) {
                if (!state.completed.contains(tid) && store.isTopicUnlocked(tid)) {
                    nextDay = d;
                    break;
                }
            }
            if (nextDay != null) break;
        }

        TextView txtTodayPlanHeader = v.findViewById(R.id.txtTodayPlanHeader);
        boolean nextIsToday = nextDay != null && PlanEngine.dayKey(nextDay.date).equals(PlanEngine.dayKey(new Date()));
        if (nextIsToday || nextDay == null) {
            txtTodayPlanHeader.setText("Hoje no seu plano");
        } else {
            txtTodayPlanHeader.setText("Próximo dia · " + PlanEngine.fmtShort(nextDay.date));
        }

        LinearLayout pendingContainer = v.findViewById(R.id.pendingTopicsContainer);
        pendingContainer.removeAllViews();
        if (nextDay == null) {
            TextView empty = new TextView(requireContext());
            empty.setText("Tudo em dia. Que tal um simulado?");
            empty.setTextColor(getResources().getColor(R.color.muted, requireContext().getTheme()));
            pendingContainer.addView(empty);
        } else {
            for (String tid : nextDay.topicIds) {
                if (!state.completed.contains(tid) && store.isTopicUnlocked(tid)) {
                    Topic pt = DataRepository.findTopicById(tid);
                    LinearLayout item = new LinearLayout(requireContext());
                    item.setOrientation(LinearLayout.VERTICAL);
                    item.setPadding(32, 24, 32, 24);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    lp.setMargins(0, 0, 0, 14);
                    item.setLayoutParams(lp);
                    item.setBackgroundResource(R.drawable.bg_input);

                    TextView tvTitle = new TextView(requireContext());
                    tvTitle.setText(pt.title);
                    tvTitle.setTextColor(getResources().getColor(R.color.ink, requireContext().getTheme()));
                    tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);

                    TextView tvSub = new TextView(requireContext());
                    tvSub.setText(pt.subject.name + " · " + pt.minutes + " min");
                    tvSub.setTextColor(pt.subject.getColorInt());
                    tvSub.setTextSize(12f);

                    item.addView(tvTitle);
                    item.addView(tvSub);
                    item.setOnClickListener(view -> ((MainActivity) requireActivity()).openTopic(pt.id));
                    pendingContainer.addView(item);
                }
            }
        }

        v.findViewById(R.id.btnSeeFullPlan).setOnClickListener(
                view -> ((MainActivity) requireActivity()).navigateTo("plano"));

        // Desempenho por disciplina
        LinearLayout accContainer = v.findViewById(R.id.subjectAccuracyContainer);
        accContainer.removeAllViews();
        for (SubjectId sub : SubjectId.values()) {
            int[] acc = store.getSubjectAccuracy(sub);
            int pct = acc[1] > 0 ? Math.round((acc[0] * 100f) / acc[1]) : 0;

            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            rlp.setMargins(0, 0, 0, 14);
            row.setLayoutParams(rlp);

            TextView tvName = new TextView(requireContext());
            tvName.setLayoutParams(new LinearLayout.LayoutParams(220, LinearLayout.LayoutParams.WRAP_CONTENT));
            tvName.setText(sub.name);
            tvName.setTextSize(13f);
            tvName.setTextColor(getResources().getColor(R.color.ink, requireContext().getTheme()));

            ProgressBar pb = new ProgressBar(requireContext(), null, android.R.attr.progressBarStyleHorizontal);
            LinearLayout.LayoutParams pblp = new LinearLayout.LayoutParams(0, 18, 1f);
            pblp.setMargins(16, 0, 16, 0);
            pb.setLayoutParams(pblp);
            pb.setMax(100);
            pb.setProgress(pct);
            pb.setProgressTintList(android.content.res.ColorStateList.valueOf(sub.getColorInt()));

            TextView tvPct = new TextView(requireContext());
            tvPct.setLayoutParams(new LinearLayout.LayoutParams(90, LinearLayout.LayoutParams.WRAP_CONTENT));
            tvPct.setGravity(Gravity.END);
            tvPct.setText(acc[1] > 0 ? pct + "%" : "—");
            tvPct.setTextSize(12f);
            tvPct.setTextColor(getResources().getColor(R.color.muted, requireContext().getTheme()));

            row.addView(tvName);
            row.addView(pb);
            row.addView(tvPct);
            accContainer.addView(row);
        }

        // Simulado sugerido
        Simulado suggested = DataRepository.SIMULADOS.get(0);
        for (Simulado s : DataRepository.SIMULADOS) {
            for (ExamId ex : state.targetExams) {
                if (ex.id.equals(s.exam)) {
                    suggested = s;
                    break;
                }
            }
        }
        ((TextView) v.findViewById(R.id.txtSuggestedSimName)).setText(suggested.exam + " · " + suggested.name);
        ((TextView) v.findViewById(R.id.txtSuggestedSimMeta)).setText(
                suggested.questionIds.size() + " questões · " + suggested.minutes + " min");
        v.findViewById(R.id.cardSuggestedSimulado).setOnClickListener(
                view -> ((MainActivity) requireActivity()).navigateTo("simulados"));

        // Atalho Ranking
        ((TextView) v.findViewById(R.id.txtRankingBannerPos)).setText("Você está em " + myPos + "º");
        ((TextView) v.findViewById(R.id.txtRankingBannerLeader)).setText("Líder: " + ranking.get(0).name);
        v.findViewById(R.id.cardRankingShortcut).setOnClickListener(
                view -> ((MainActivity) requireActivity()).navigateTo("ranking"));

        // Atalhos para novas ferramentas (Calendário, Materiais, Fórum, Desempenho)
        v.findViewById(R.id.cardShortcutCalendar).setOnClickListener(
                view -> ((MainActivity) requireActivity()).navigateTo("calendario"));
        v.findViewById(R.id.cardShortcutMaterials).setOnClickListener(
                view -> ((MainActivity) requireActivity()).navigateTo("materiais"));
        v.findViewById(R.id.cardShortcutForum).setOnClickListener(
                view -> ((MainActivity) requireActivity()).navigateTo("forum"));
        v.findViewById(R.id.cardShortcutPerformance).setOnClickListener(
                view -> ((MainActivity) requireActivity()).navigateTo("desempenho"));

        return v;
    }
}
