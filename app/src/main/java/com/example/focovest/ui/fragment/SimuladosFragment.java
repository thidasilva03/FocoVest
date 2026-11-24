package com.example.focovest.ui.fragment;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.focovest.R;
import com.example.focovest.data.FocoVestRepository;
import com.example.focovest.model.ExamId;
import com.example.focovest.model.Question;
import com.example.focovest.model.SimuladoHistoryItem;
import com.example.focovest.model.Topic;
import com.example.focovest.model.User;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Simulados gerados dinamicamente com base EXCLUSIVA nos tópicos concluídos (completed = 1)
 * pelo usuário logado, com filtro por objetivo, cronômetro preciso, cartão-resposta e gabarito comentado.
 */
public class SimuladosFragment extends Fragment {

    private static final String[] LETTERS = {"A", "B", "C", "D", "E"};

    private FocoVestRepository repo;
    private String userId;
    private LinearLayout root;

    // Navegação interna: "home" | "runner" | "result"
    private String mode = "home";
    private String selectedExamFilter = "ALL"; // "ALL" ou ExamId.id

    // Estado do Runner
    private String runningTitle = "";
    private String runningExamFilter = "ALL";
    private int runningDurationTotalSec = 600;
    private long runnerStartElapsedMs = 0L;
    private List<Question> runningQuestions = new ArrayList<>();
    private int currentIdx = 0;
    private final Map<String, Integer> runnerAnswers = new HashMap<>();
    private final Set<String> flaggedQuestions = new HashSet<>();
    private TextView txtTimerView;

    // Resultado aberto
    private SimuladoHistoryItem currentResult = null;

    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (!"runner".equals(mode) || runningQuestions.isEmpty()) return;
            int elapsed = getElapsedSeconds();
            int remaining = Math.max(0, runningDurationTotalSec - elapsed);
            if (txtTimerView != null) {
                txtTimerView.setText("⏱ " + formatSec(remaining));
            }
            if (remaining <= 0) {
                finishRunner();
            } else {
                timerHandler.postDelayed(this, 500);
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_scroll_container, container, false);
        root = v.findViewById(R.id.rootContainer);
        repo = FocoVestRepository.getInstance(requireContext());
        userId = FocoVestRepository.LOCAL_USER_ID;
        render();
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        timerHandler.removeCallbacks(timerRunnable);
        txtTimerView = null;
    }

    private int getElapsedSeconds() {
        if (runnerStartElapsedMs <= 0L) return 0;
        return (int) Math.max(0L, (SystemClock.elapsedRealtime() - runnerStartElapsedMs) / 1000L);
    }

    private static String formatSec(int s) {
        return String.format(Locale.US, "%02d:%02d", s / 60, s % 60);
    }

    private void render() {
        root.removeAllViews();
        switch (mode) {
            case "runner":
                renderRunner();
                break;
            case "result":
                renderResult();
                break;
            default:
                renderHome();
                break;
        }
    }

    // =========================================================================
    // 1. TELA INICIAL: GERADOR DINÂMICO + HISTÓRICO
    // =========================================================================

    private void renderHome() {
        User user = repo.getUserById(userId);
        if (user == null) return;

        addEyebrow("SIMULADOS DINÂMICOS");
        addTitle("Treine no que você já estudou.");

        List<Topic> allCompleted = repo.getCompletedTopics(userId, null);

        // Estado vazio claro caso o aluno ainda não tenha concluído nenhum tópico
        if (allCompleted.isEmpty()) {
            LinearLayout emptyCard = createCard(true);

            TextView tvTitle = new TextView(requireContext());
            tvTitle.setText("Nenhum tópico concluído ainda");
            tvTitle.setTextColor(getColorRes(R.color.lime));
            tvTitle.setTextSize(20f);
            tvTitle.setTypeface(null, Typeface.BOLD);
            emptyCard.addView(tvTitle);

            TextView tvDesc = new TextView(requireContext());
            tvDesc.setText("Seus simulados são montados dinamicamente com questões exclusivas dos tópicos que você já marcou como concluídos na Sala de Estudos.\n\nConclua seu primeiro tópico no plano para desbloquear os simulados!");
            tvDesc.setTextColor(Color.parseColor("#CCFFFFFF"));
            tvDesc.setTextSize(13f);
            tvDesc.setPadding(0, dp(8), 0, dp(16));
            emptyCard.addView(tvDesc);

            MaterialButton btnGoPlan = new MaterialButton(requireContext());
            btnGoPlan.setText("Ir para meus planos de estudo →");
            btnGoPlan.setAllCaps(false);
            btnGoPlan.setBackgroundTintList(ColorStateList.valueOf(getColorRes(R.color.lime)));
            btnGoPlan.setTextColor(getColorRes(R.color.ink));
            btnGoPlan.setTypeface(null, Typeface.BOLD);
            btnGoPlan.setOnClickListener(v -> NavHostFragment.findNavController(this)
                .navigate(R.id.action_global_plan));
            emptyCard.addView(btnGoPlan);

            root.addView(emptyCard);
            renderHistorySection();
            return;
        }

        // Filtro por objetivo (Todos os concluídos ou um objetivo selecionado pelo aluno)
        if (!"ALL".equals(selectedExamFilter)) {
            ExamId parsed = ExamId.fromCode(selectedExamFilter);
            if (parsed == null || !user.targetExams.contains(parsed)) {
                selectedExamFilter = "ALL";
            }
        }

        LinearLayout filterCard = createCard(false);
        TextView tvFilterLabel = new TextView(requireContext());
        tvFilterLabel.setText("Filtrar simulado pelos tópicos concluídos:");
        tvFilterLabel.setTypeface(null, Typeface.BOLD);
        tvFilterLabel.setTextColor(getColorRes(R.color.ink));
        tvFilterLabel.setPadding(0, 0, 0, dp(10));
        filterCard.addView(tvFilterLabel);

        HorizontalScrollView hsv = new HorizontalScrollView(requireContext());
        hsv.setScrollBarSize(0);
        LinearLayout chipRow = new LinearLayout(requireContext());
        chipRow.setOrientation(LinearLayout.HORIZONTAL);

        chipRow.addView(createFilterChip("Todos concluídos", "ALL".equals(selectedExamFilter), () -> {
            selectedExamFilter = "ALL";
            render();
        }));
        for (ExamId ex : user.targetExams) {
            chipRow.addView(createFilterChip(user.getExamLabel(ex), ex.id.equals(selectedExamFilter), () -> {
                selectedExamFilter = ex.id;
                render();
            }));
        }
        hsv.addView(chipRow);
        filterCard.addView(hsv);
        root.addView(filterCard);

        ExamId examFilterObj = "ALL".equals(selectedExamFilter) ? null : ExamId.fromCode(selectedExamFilter);
        String examFilterLabel = examFilterObj == null ? "Geral" : user.getExamLabel(examFilterObj);
        List<Topic> matchingTopics = repo.getCompletedTopics(userId, examFilterObj, user.highSchoolYear);
        List<Question> pool = repo.getQuestionsForCompletedTopics(userId, examFilterObj, user.highSchoolYear);

        // Card de geração do simulado
        LinearLayout genCard = createCard(true);
        String scopeLabel = examFilterObj == null ? "Todos os tópicos concluídos" : "Concluídos do " + examFilterLabel;

        TextView tvBadge = new TextView(requireContext());
        tvBadge.setText(scopeLabel.toUpperCase(Locale.ROOT));
        tvBadge.setTextColor(getColorRes(R.color.lime));
        tvBadge.setTextSize(11f);
        tvBadge.setTypeface(null, Typeface.BOLD);
        genCard.addView(tvBadge);

        if (pool.isEmpty()) {
            TextView tvNone = new TextView(requireContext());
            tvNone.setText("Você ainda não concluiu tópicos do edital " + examFilterLabel + ".");
            tvNone.setTextColor(Color.WHITE);
            tvNone.setTextSize(16f);
            tvNone.setTypeface(null, Typeface.BOLD);
            tvNone.setPadding(0, dp(8), 0, dp(12));
            genCard.addView(tvNone);

            MaterialButton btnGoPlan = new MaterialButton(requireContext());
            btnGoPlan.setText("Abrir plano " + examFilterLabel + " →");
            btnGoPlan.setAllCaps(false);
            btnGoPlan.setBackgroundTintList(ColorStateList.valueOf(getColorRes(R.color.accent)));
            btnGoPlan.setTextColor(Color.WHITE);
            btnGoPlan.setOnClickListener(v -> NavHostFragment.findNavController(this)
                .navigate(R.id.action_global_plan));
            genCard.addView(btnGoPlan);
        } else {
            int maxQuestions = Math.min(pool.size(), 12);
            int minutes = Math.max(5, maxQuestions * 3);

            TextView tvGenTitle = new TextView(requireContext());
            tvGenTitle.setText("Simulado " + examFilterLabel);
            tvGenTitle.setTextColor(Color.WHITE);
            tvGenTitle.setTextSize(22f);
            tvGenTitle.setTypeface(null, Typeface.BOLD);
            tvGenTitle.setPadding(0, dp(4), 0, dp(4));
            genCard.addView(tvGenTitle);

            TextView tvMeta = new TextView(requireContext());
            tvMeta.setText(matchingTopics.size() + " tópico(s) concluído(s) · " + maxQuestions
                    + " questões sorteadas (de " + pool.size() + " disponíveis) · " + minutes + " min");
            tvMeta.setTextColor(Color.parseColor("#B3FFFFFF"));
            tvMeta.setTextSize(13f);
            tvMeta.setPadding(0, 0, 0, dp(14));
            genCard.addView(tvMeta);

            MaterialButton btnStart = new MaterialButton(requireContext());
            btnStart.setText("Iniciar simulado agora →");
            btnStart.setAllCaps(false);
            btnStart.setBackgroundTintList(ColorStateList.valueOf(getColorRes(R.color.accent)));
            btnStart.setTextColor(Color.WHITE);
            btnStart.setTypeface(null, Typeface.BOLD);
            btnStart.setOnClickListener(v -> startDynamicSimulado(
                    examFilterObj == null ? "ALL" : examFilterObj.id,
                    "Simulado · " + (examFilterObj == null ? "Concluídos" : examFilterLabel),
                    pool, maxQuestions, minutes));
            genCard.addView(btnStart);
        }
        root.addView(genCard);

        renderHistorySection();
    }

    private void renderHistorySection() {
        List<SimuladoHistoryItem> history = repo.getSimuladoHistory(userId);

        TextView secHist = new TextView(requireContext());
        secHist.setText("Histórico de simulados");
        secHist.setTypeface(null, Typeface.BOLD);
        secHist.setTextSize(20f);
        secHist.setTextColor(getColorRes(R.color.ink));
        secHist.setPadding(0, dp(8), 0, dp(10));
        root.addView(secHist);

        if (history.isEmpty()) {
            LinearLayout empty = createCard(false);
            TextView tv = new TextView(requireContext());
            tv.setText("Você ainda não concluiu nenhum simulado.");
            tv.setTextColor(getColorRes(R.color.muted));
            empty.addView(tv);
            root.addView(empty);
            return;
        }

        for (SimuladoHistoryItem h : history) {
            int pct = Math.round((h.correctCount * 100f) / Math.max(1, h.totalCount));
            LinearLayout row = createCard(false);

            TextView tvTop = new TextView(requireContext());
            String badge = "ALL".equals(h.examFilter) ? "Geral" : h.examFilter;
            tvTop.setText(badge + " · " + h.title);
            tvTop.setTypeface(null, Typeface.BOLD);
            tvTop.setTextColor(getColorRes(R.color.ink));
            tvTop.setTextSize(15f);

            TextView tvSub = new TextView(requireContext());
            tvSub.setText(pct + "% (" + h.correctCount + "/" + h.totalCount + " acertos) · Tempo: "
                    + formatSec(h.durationSec) + " · " + h.createdAt);
            tvSub.setTextColor(getColorRes(R.color.muted));
            tvSub.setTextSize(12f);
            tvSub.setPadding(0, dp(4), 0, 0);

            row.addView(tvTop);
            row.addView(tvSub);
            row.setOnClickListener(v -> {
                currentResult = h;
                mode = "result";
                render();
            });
            root.addView(row);
        }
    }

    private View createFilterChip(String label, boolean active, Runnable onClick) {
        TextView tv = new TextView(requireContext());
        tv.setText((active ? "✓ " : "") + label);
        tv.setPadding(dp(14), dp(9), dp(14), dp(9));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, dp(8), 0);
        tv.setLayoutParams(lp);
        tv.setBackgroundResource(active ? R.drawable.bg_card_dark : R.drawable.bg_input);
        tv.setTextColor(active ? getColorRes(R.color.lime) : getColorRes(R.color.ink));
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextSize(13f);
        tv.setOnClickListener(v -> onClick.run());
        return tv;
    }

    // =========================================================================
    // 2. RUNNER COM CRONÔMETRO PRECISO E CARTÃO-RESPOSTA
    // =========================================================================

    private void startDynamicSimulado(String examFilter, String title,
                                      List<Question> pool, int count, int minutes) {
        List<Question> shuffled = new ArrayList<>(pool);
        Collections.shuffle(shuffled);
        this.runningQuestions = new ArrayList<>(shuffled.subList(0, Math.min(count, shuffled.size())));
        if (this.runningQuestions.isEmpty()) {
            Toast.makeText(requireContext(), "Nenhuma questão disponível para o filtro.", Toast.LENGTH_SHORT).show();
            return;
        }

        this.runningExamFilter = examFilter;
        this.runningTitle = title;
        this.runningDurationTotalSec = minutes * 60;
        this.runnerStartElapsedMs = SystemClock.elapsedRealtime();
        this.currentIdx = 0;
        this.runnerAnswers.clear();
        this.flaggedQuestions.clear();
        this.mode = "runner";

        timerHandler.removeCallbacks(timerRunnable);
        timerHandler.postDelayed(timerRunnable, 500);
        render();
    }

    private void renderRunner() {
        if (runningQuestions.isEmpty()) {
            mode = "home";
            render();
            return;
        }
        if (currentIdx < 0 || currentIdx >= runningQuestions.size()) {
            currentIdx = 0;
        }

        Question q = runningQuestions.get(currentIdx);
        int remaining = Math.max(0, runningDurationTotalSec - getElapsedSeconds());

        // Barra topo: Sair + Cronômetro regressivo
        LinearLayout topRow = new LinearLayout(requireContext());
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);
        topRow.setPadding(0, 0, 0, dp(12));

        MaterialButton btnCancel = new MaterialButton(requireContext());
        btnCancel.setText("← Sair");
        btnCancel.setAllCaps(false);
        btnCancel.setBackgroundTintList(ColorStateList.valueOf(getColorRes(R.color.ink)));
        btnCancel.setTextColor(Color.WHITE);
        btnCancel.setOnClickListener(v -> {
            timerHandler.removeCallbacks(timerRunnable);
            mode = "home";
            render();
        });

        txtTimerView = new TextView(requireContext());
        txtTimerView.setText("⏱ " + formatSec(remaining));
        txtTimerView.setPadding(dp(14), dp(8), dp(14), dp(8));
        txtTimerView.setBackgroundResource(R.drawable.bg_card_dark);
        txtTimerView.setTextColor(getColorRes(R.color.lime));
        txtTimerView.setTypeface(null, Typeface.BOLD);

        topRow.addView(btnCancel);
        View spacer = new View(requireContext());
        spacer.setLayoutParams(new LinearLayout.LayoutParams(0, 1, 1f));
        topRow.addView(spacer);
        topRow.addView(txtTimerView);
        root.addView(topRow);

        // Card da Questão Atual
        LinearLayout qCard = createCard(false);
        boolean isFlagged = flaggedQuestions.contains(q.id);

        TextView tvHeader = new TextView(requireContext());
        tvHeader.setText(String.format(Locale.US, "Questão %02d/%02d · %s",
                currentIdx + 1, runningQuestions.size(), q.subject.name));
        tvHeader.setTextColor(q.subject.getColorInt());
        tvHeader.setTypeface(null, Typeface.BOLD);
        qCard.addView(tvHeader);

        MaterialButton btnFlag = new MaterialButton(requireContext());
        btnFlag.setText(isFlagged ? "★ Marcada p/ revisão" : "☆ Revisar depois");
        btnFlag.setAllCaps(false);
        btnFlag.setBackgroundTintList(ColorStateList.valueOf(
                getColorRes(isFlagged ? R.color.accent : R.color.ink_10)));
        btnFlag.setTextColor(isFlagged ? Color.WHITE : getColorRes(R.color.ink));
        btnFlag.setOnClickListener(v -> {
            if (isFlagged) flaggedQuestions.remove(q.id);
            else flaggedQuestions.add(q.id);
            render();
        });
        qCard.addView(btnFlag);

        TextView tvPrompt = new TextView(requireContext());
        tvPrompt.setText(q.prompt);
        tvPrompt.setTextSize(16f);
        tvPrompt.setTypeface(null, Typeface.BOLD);
        tvPrompt.setTextColor(getColorRes(R.color.ink));
        tvPrompt.setPadding(0, dp(10), 0, dp(10));
        qCard.addView(tvPrompt);

        Integer chosen = runnerAnswers.get(q.id);
        for (int i = 0; i < q.options.size(); i++) {
            final int optIdx = i;
            boolean sel = chosen != null && chosen == i;
            TextView opt = new TextView(requireContext());
            opt.setText(LETTERS[i] + ")  " + q.options.get(i));
            opt.setPadding(dp(14), dp(12), dp(14), dp(12));
            LinearLayout.LayoutParams olp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            olp.setMargins(0, dp(8), 0, 0);
            opt.setLayoutParams(olp);
            opt.setBackgroundResource(sel ? R.drawable.bg_card_dark : R.drawable.bg_input);
            opt.setTextColor(sel ? Color.WHITE : getColorRes(R.color.ink));
            opt.setOnClickListener(v -> {
                runnerAnswers.put(q.id, optIdx);
                render();
            });
            qCard.addView(opt);
        }

        // Navegação Anterior / Próxima / Entregar
        LinearLayout navRow = new LinearLayout(requireContext());
        navRow.setOrientation(LinearLayout.HORIZONTAL);
        navRow.setPadding(0, dp(16), 0, 0);

        MaterialButton btnPrev = new MaterialButton(requireContext());
        btnPrev.setText("← Anterior");
        btnPrev.setAllCaps(false);
        btnPrev.setBackgroundTintList(ColorStateList.valueOf(getColorRes(R.color.ink_10)));
        btnPrev.setTextColor(getColorRes(R.color.ink));
        btnPrev.setEnabled(currentIdx > 0);
        btnPrev.setAlpha(currentIdx > 0 ? 1f : 0.4f);
        btnPrev.setOnClickListener(v -> {
            currentIdx--;
            render();
        });

        MaterialButton btnNext = new MaterialButton(requireContext());
        btnNext.setAllCaps(false);
        if (currentIdx < runningQuestions.size() - 1) {
            btnNext.setText("Próxima →");
            btnNext.setBackgroundTintList(ColorStateList.valueOf(getColorRes(R.color.ink)));
            btnNext.setTextColor(Color.WHITE);
            btnNext.setOnClickListener(v -> {
                currentIdx++;
                render();
            });
        } else {
            btnNext.setText("Entregar simulado ✓");
            btnNext.setBackgroundTintList(ColorStateList.valueOf(getColorRes(R.color.accent)));
            btnNext.setTextColor(Color.WHITE);
            btnNext.setOnClickListener(v -> finishRunner());
        }

        navRow.addView(btnPrev);
        View sp2 = new View(requireContext());
        sp2.setLayoutParams(new LinearLayout.LayoutParams(0, 1, 1f));
        navRow.addView(sp2);
        navRow.addView(btnNext);
        qCard.addView(navRow);
        root.addView(qCard);

        // Cartão-Resposta
        LinearLayout gridCard = createCard(false);
        TextView tvGrid = new TextView(requireContext());
        tvGrid.setText("Cartão-resposta (" + runnerAnswers.size() + "/" + runningQuestions.size() + " respondidas)");
        tvGrid.setTypeface(null, Typeface.BOLD);
        tvGrid.setTextColor(getColorRes(R.color.ink));
        gridCard.addView(tvGrid);

        HorizontalScrollView hsv = new HorizontalScrollView(requireContext());
        LinearLayout chips = new LinearLayout(requireContext());
        chips.setOrientation(LinearLayout.HORIZONTAL);
        chips.setPadding(0, dp(10), 0, 0);

        for (int i = 0; i < runningQuestions.size(); i++) {
            final int qIndex = i;
            Question qq = runningQuestions.get(i);
            Integer ans = runnerAnswers.get(qq.id);
            boolean flag = flaggedQuestions.contains(qq.id);

            TextView chip = new TextView(requireContext());
            chip.setText((flag ? "★" : "") + (i + 1) + (ans != null ? ":" + LETTERS[ans] : ""));
            chip.setPadding(dp(12), dp(8), dp(12), dp(8));
            chip.setTypeface(null, Typeface.BOLD);
            LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            clp.setMargins(0, 0, dp(6), 0);
            chip.setLayoutParams(clp);
            if (i == currentIdx) {
                chip.setBackgroundResource(R.drawable.bg_button_accent);
                chip.setTextColor(Color.WHITE);
            } else if (ans != null) {
                chip.setBackgroundResource(R.drawable.bg_card_dark);
                chip.setTextColor(getColorRes(R.color.lime));
            } else {
                chip.setBackgroundResource(R.drawable.bg_input);
                chip.setTextColor(getColorRes(R.color.ink));
            }
            chip.setOnClickListener(v -> {
                currentIdx = qIndex;
                render();
            });
            chips.addView(chip);
        }
        hsv.addView(chips);
        gridCard.addView(hsv);
        root.addView(gridCard);
    }

    private void finishRunner() {
        timerHandler.removeCallbacks(timerRunnable);
        if (runningQuestions.isEmpty()) {
            mode = "home";
            render();
            return;
        }

        int correct = 0;
        List<String> qIds = new ArrayList<>();
        for (Question q : runningQuestions) {
            qIds.add(q.id);
            Integer chosen = runnerAnswers.get(q.id);
            if (chosen != null && chosen == q.correctIndex) correct++;
        }

        int durationSec = Math.min(getElapsedSeconds(), runningDurationTotalSec);
        SimuladoHistoryItem item = new SimuladoHistoryItem(
                "sim-" + UUID.randomUUID().toString().substring(0, 8),
                userId,
                runningExamFilter,
                runningTitle,
                correct,
                runningQuestions.size(),
                durationSec,
                FocoVestRepository.nowTimestamp(),
                new HashMap<>(runnerAnswers),
                qIds
        );

        repo.saveSimuladoResult(item);

        this.currentResult = item;
        this.runningQuestions = new ArrayList<>();
        this.mode = "result";
        render();
    }

    // =========================================================================
    // 3. RESULTADO E GABARITO COMENTADO
    // =========================================================================

    private void renderResult() {
        if (currentResult == null) {
            mode = "home";
            render();
            return;
        }

        MaterialButton btnBack = new MaterialButton(requireContext());
        btnBack.setText("← Voltar aos simulados");
        btnBack.setAllCaps(false);
        btnBack.setBackgroundTintList(ColorStateList.valueOf(getColorRes(R.color.ink)));
        btnBack.setTextColor(Color.WHITE);
        btnBack.setOnClickListener(v -> {
            mode = "home";
            render();
        });
        root.addView(btnBack);

        int pct = Math.round((currentResult.correctCount * 100f) / Math.max(1, currentResult.totalCount));
        LinearLayout scoreCard = createCard(true);

        String badge = "ALL".equals(currentResult.examFilter) ? "Geral" : currentResult.examFilter;
        TextView tvTitle = new TextView(requireContext());
        tvTitle.setText(badge + " · " + currentResult.title);
        tvTitle.setTextColor(Color.parseColor("#99FFFFFF"));
        scoreCard.addView(tvTitle);

        TextView tvScore = new TextView(requireContext());
        tvScore.setText(pct + "% (" + currentResult.correctCount + "/" + currentResult.totalCount + " acertos)");
        tvScore.setTextColor(getColorRes(R.color.lime));
        tvScore.setTextSize(30f);
        tvScore.setTypeface(null, Typeface.BOLD);
        scoreCard.addView(tvScore);

        TextView tvPts = new TextView(requireContext());
        tvPts.setText("Tempo: " + formatSec(currentResult.durationSec)
                + "  ·  +" + (currentResult.correctCount * FocoVestRepository.POINTS_PER_SIMULADO_CORRECT)
                + " pontos");
        tvPts.setTextColor(Color.WHITE);
        scoreCard.addView(tvPts);
        root.addView(scoreCard);

        TextView tvGab = new TextView(requireContext());
        tvGab.setText("Gabarito comentado");
        tvGab.setTypeface(null, Typeface.BOLD);
        tvGab.setTextSize(20f);
        tvGab.setTextColor(getColorRes(R.color.ink));
        tvGab.setPadding(0, dp(8), 0, dp(10));
        root.addView(tvGab);

        for (int i = 0; i < currentResult.questionIds.size(); i++) {
            Question q = repo.getQuestionById(currentResult.questionIds.get(i));
            if (q == null) continue;
            Integer chosen = currentResult.answers.get(q.id);
            boolean ok = chosen != null && chosen == q.correctIndex;

            LinearLayout c = createCard(false);
            TextView p = new TextView(requireContext());
            p.setText((ok ? "✓ " : "✕ ") + (i + 1) + ". " + q.prompt);
            p.setTypeface(null, Typeface.BOLD);
            p.setTextColor(getColorRes(ok ? R.color.success_dark : R.color.ink));
            c.addView(p);

            String myAns = (chosen != null && chosen >= 0 && chosen < q.options.size())
                    ? LETTERS[chosen] + ") " + q.options.get(chosen)
                    : "em branco";
            String rightAns = LETTERS[q.correctIndex] + ") " + q.options.get(q.correctIndex);
            TextView ans = new TextView(requireContext());
            ans.setText("Sua resposta: " + myAns + (ok ? "" : "\nCorreta: " + rightAns));
            ans.setPadding(0, dp(6), 0, dp(6));
            ans.setTextColor(getColorRes(R.color.ink));
            c.addView(ans);

            TextView exp = new TextView(requireContext());
            exp.setText("💡 " + q.explanation);
            exp.setTextColor(getColorRes(R.color.muted));
            c.addView(exp);
            root.addView(c);
        }
    }

    private void addEyebrow(String text) {
        TextView tv = new TextView(requireContext());
        tv.setText(text);
        tv.setTextColor(getColorRes(R.color.accent));
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextSize(11f);
        root.addView(tv);
    }

    private void addTitle(String text) {
        TextView tv = new TextView(requireContext());
        tv.setText(text);
        tv.setTextColor(getColorRes(R.color.ink));
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextSize(26f);
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
