package com.example.focovest.ui.fragment;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.focovest.R;
import com.example.focovest.data.DataRepository;
import com.example.focovest.data.DataRepository.*;
import com.example.focovest.data.StoreManager;
import com.example.focovest.data.StoreManager.SimuladoResult;
import com.example.focovest.domain.PlanEngine;

import java.util.*;

public class SimuladosFragment extends Fragment {

    private static final String[] LETTERS = {"A", "B", "C", "D", "E"};
    private StoreManager store;
    private LinearLayout root;

    // Estado de navegação interna: "list" | "runner" | "result" | "editor"
    private String mode = "list";
    private Simulado runningSim = null;
    private SimuladoResult currentResult = null;

    // Estado do Runner
    private int currentIdx = 0;
    private final Map<String, Integer> runnerAnswers = new HashMap<>();
    private final Set<String> flaggedQuestions = new HashSet<>();
    private int elapsedSec = 0;
    private TextView txtTimerView;
    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (!"runner".equals(mode) || runningSim == null) return;
            elapsedSec++;
            int remaining = Math.max(0, runningSim.minutes * 60 - elapsedSec);
            if (txtTimerView != null) {
                txtTimerView.setText("⏱ " + formatSec(remaining));
            }
            if (remaining <= 0) {
                finishRunner();
            } else {
                timerHandler.postDelayed(this, 1000);
            }
        }
    };

    // Estado do Editor de Simulado Personalizado
    private String editorName = "";
    private int editorMinutes = 20;
    private final List<String> editorTopicIds = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_scroll_container, container, false);
        root = v.findViewById(R.id.rootContainer);
        store = StoreManager.getInstance(requireContext());
        render();
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        timerHandler.removeCallbacks(timerRunnable);
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
            case "editor":
                renderEditor();
                break;
            default:
                renderList();
                break;
        }
    }

    // --- 1. LISTAGEM DE SIMULADOS ---
    private void renderList() {
        StoreManager.State state = store.getState();
        boolean autonomous = "autonoma".equals(state.mode);

        TextView eyebrow = new TextView(requireContext());
        eyebrow.setText("SIMULADOS");
        eyebrow.setTextColor(getColorRes(R.color.accent));
        eyebrow.setTypeface(null, Typeface.BOLD);
        eyebrow.setTextSize(11f);
        root.addView(eyebrow);

        TextView title = new TextView(requireContext());
        title.setText("Treine no ritmo da prova real.");
        title.setTextColor(getColorRes(R.color.ink));
        title.setTypeface(null, Typeface.BOLD);
        title.setTextSize(26f);
        title.setPadding(0, 4, 0, 16);
        root.addView(title);

        if (autonomous) {
            Button btnCreate = new Button(requireContext());
            btnCreate.setText("+ Criar simulado personalizado");
            btnCreate.setAllCaps(false);
            btnCreate.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getColorRes(R.color.ink)));
            btnCreate.setTextColor(Color.WHITE);
            btnCreate.setOnClickListener(v -> {
                editorName = "";
                editorMinutes = 20;
                editorTopicIds.clear();
                mode = "editor";
                render();
            });
            root.addView(btnCreate);

            for (Simulado custom : state.customSimulados) {
                addSimuladoCard(custom, false);
            }
        }

        TextView secOfficial = new TextView(requireContext());
        secOfficial.setText("Simulados por vestibular");
        secOfficial.setTypeface(null, Typeface.BOLD);
        secOfficial.setTextSize(20f);
        secOfficial.setTextColor(getColorRes(R.color.ink));
        secOfficial.setPadding(0, 24, 0, 12);
        root.addView(secOfficial);

        for (int i = 0; i < DataRepository.SIMULADOS.size(); i++) {
            addSimuladoCard(DataRepository.SIMULADOS.get(i), i == 0);
        }

        // Histórico
        TextView secHist = new TextView(requireContext());
        secHist.setText("Histórico recente");
        secHist.setTypeface(null, Typeface.BOLD);
        secHist.setTextSize(20f);
        secHist.setTextColor(getColorRes(R.color.ink));
        secHist.setPadding(0, 24, 0, 12);
        root.addView(secHist);

        if (state.history.isEmpty()) {
            LinearLayout empty = createCard(false);
            TextView tv = new TextView(requireContext());
            tv.setText("Você ainda não fez nenhum simulado.");
            tv.setTextColor(getColorRes(R.color.muted));
            empty.addView(tv);
            root.addView(empty);
        } else {
            for (int i = 0; i < Math.min(8, state.history.size()); i++) {
                SimuladoResult h = state.history.get(i);
                int pct = Math.round((h.correct * 100f) / Math.max(1, h.total));
                LinearLayout row = createCard(false);
                TextView tv = new TextView(requireContext());
                tv.setText((h.simExam != null ? h.simExam : "Simulado") + " · " + h.simName
                        + "\n" + pct + "% (" + h.correct + "/" + h.total + ") · Tempo: " + formatSec(h.durationSec));
                tv.setTypeface(null, Typeface.BOLD);
                tv.setTextColor(getColorRes(R.color.ink));
                row.addView(tv);
                row.setOnClickListener(v -> {
                    currentResult = h;
                    mode = "result";
                    render();
                });
                root.addView(row);
            }
        }
    }

    private void addSimuladoCard(Simulado sim, boolean dark) {
        LinearLayout card = createCard(dark);

        TextView badge = new TextView(requireContext());
        badge.setText(sim.exam + " · " + sim.questionIds.size() + " questões · " + sim.minutes + " min");
        badge.setTextColor(dark ? getColorRes(R.color.lime) : getColorRes(R.color.accent));
        badge.setTypeface(null, Typeface.BOLD);
        badge.setTextSize(12f);
        card.addView(badge);

        TextView name = new TextView(requireContext());
        name.setText(sim.name);
        name.setTextColor(dark ? Color.WHITE : getColorRes(R.color.ink));
        name.setTypeface(null, Typeface.BOLD);
        name.setTextSize(22f);
        name.setPadding(0, 6, 0, 4);
        card.addView(name);

        TextView desc = new TextView(requireContext());
        desc.setText(sim.description);
        desc.setTextColor(dark ? Color.parseColor("#B3FFFFFF") : getColorRes(R.color.muted));
        desc.setTextSize(13f);
        card.addView(desc);

        Button btnStart = new Button(requireContext());
        btnStart.setText("Iniciar simulado →");
        btnStart.setAllCaps(false);
        btnStart.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                getColorRes(dark ? R.color.accent : R.color.ink)));
        btnStart.setTextColor(Color.WHITE);
        btnStart.setOnClickListener(v -> startSimulado(sim));
        card.addView(btnStart);

        root.addView(card);
    }

    // --- 2. RUNNER DE PROVA COM CRONÓMETRO ---
    private void startSimulado(Simulado sim) {
        this.runningSim = sim;
        this.currentIdx = 0;
        this.runnerAnswers.clear();
        this.flaggedQuestions.clear();
        this.elapsedSec = 0;
        this.mode = "runner";
        timerHandler.removeCallbacks(timerRunnable);
        timerHandler.postDelayed(timerRunnable, 1000);
        render();
    }

    private void renderRunner() {
        List<Question> questions = new ArrayList<>();
        for (String qid : runningSim.questionIds) {
            Question q = DataRepository.findQuestionById(qid);
            if (q != null) questions.add(q);
        }
        Question q = questions.get(currentIdx);
        int remaining = Math.max(0, runningSim.minutes * 60 - elapsedSec);

        // Barra topo: Sair + Cronómetro
        LinearLayout topRow = new LinearLayout(requireContext());
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);
        topRow.setPadding(0, 0, 0, 16);

        Button btnCancel = new Button(requireContext());
        btnCancel.setText("← Sair");
        btnCancel.setAllCaps(false);
        btnCancel.setOnClickListener(v -> {
            timerHandler.removeCallbacks(timerRunnable);
            mode = "list";
            render();
        });

        txtTimerView = new TextView(requireContext());
        txtTimerView.setText("⏱ " + formatSec(remaining));
        txtTimerView.setPadding(32, 16, 32, 16);
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
        tvHeader.setText(String.format(Locale.US, "Questão %02d/%02d · %s", currentIdx + 1, questions.size(), q.subject.name));
        tvHeader.setTextColor(q.subject.getColorInt());
        tvHeader.setTypeface(null, Typeface.BOLD);
        qCard.addView(tvHeader);

        Button btnFlag = new Button(requireContext());
        btnFlag.setText(isFlagged ? "★ Marcada p/ revisão" : "☆ Revisar depois");
        btnFlag.setAllCaps(false);
        btnFlag.setOnClickListener(v -> {
            if (isFlagged) flaggedQuestions.remove(q.id);
            else flaggedQuestions.add(q.id);
            render();
        });
        qCard.addView(btnFlag);

        TextView tvPrompt = new TextView(requireContext());
        tvPrompt.setText(q.prompt);
        tvPrompt.setTextSize(17f);
        tvPrompt.setTypeface(null, Typeface.BOLD);
        tvPrompt.setTextColor(getColorRes(R.color.ink));
        tvPrompt.setPadding(0, 16, 0, 16);
        qCard.addView(tvPrompt);

        Integer chosen = runnerAnswers.get(q.id);
        for (int i = 0; i < q.options.size(); i++) {
            final int optIdx = i;
            boolean sel = chosen != null && chosen == i;
            TextView opt = new TextView(requireContext());
            opt.setText(LETTERS[i] + ")  " + q.options.get(i));
            opt.setPadding(32, 26, 32, 26);
            LinearLayout.LayoutParams olp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            olp.setMargins(0, 12, 0, 0);
            opt.setLayoutParams(olp);
            opt.setBackgroundResource(sel ? R.drawable.bg_card_dark : R.drawable.bg_input);
            opt.setTextColor(sel ? Color.WHITE : getColorRes(R.color.ink));
            opt.setOnClickListener(v -> {
                runnerAnswers.put(q.id, optIdx);
                render();
            });
            qCard.addView(opt);
        }

        // Navegação Anterior / Próxima / Finalizar
        LinearLayout navRow = new LinearLayout(requireContext());
        navRow.setOrientation(LinearLayout.HORIZONTAL);
        navRow.setPadding(0, 24, 0, 0);

        Button btnPrev = new Button(requireContext());
        btnPrev.setText("← Anterior");
        btnPrev.setEnabled(currentIdx > 0);
        btnPrev.setOnClickListener(v -> {
            currentIdx--;
            render();
        });

        Button btnNext = new Button(requireContext());
        if (currentIdx < questions.size() - 1) {
            btnNext.setText("Próxima →");
            btnNext.setOnClickListener(v -> {
                currentIdx++;
                render();
            });
        } else {
            btnNext.setText("Entregar simulado ✓");
            btnNext.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getColorRes(R.color.accent)));
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

        // Cartão-Resposta Rápido
        LinearLayout gridCard = createCard(false);
        TextView tvGrid = new TextView(requireContext());
        tvGrid.setText("Cartão-resposta (" + runnerAnswers.size() + "/" + questions.size() + " respondidas)");
        tvGrid.setTypeface(null, Typeface.BOLD);
        gridCard.addView(tvGrid);

        HorizontalScrollView hsv = new HorizontalScrollView(requireContext());
        LinearLayout chips = new LinearLayout(requireContext());
        chips.setOrientation(LinearLayout.HORIZONTAL);
        chips.setPadding(0, 16, 0, 0);

        for (int i = 0; i < questions.size(); i++) {
            final int qIndex = i;
            Question qq = questions.get(i);
            Integer ans = runnerAnswers.get(qq.id);
            Button b = new Button(requireContext());
            b.setText((i + 1) + (ans != null ? ":" + LETTERS[ans] : ""));
            b.setOnClickListener(v -> {
                currentIdx = qIndex;
                render();
            });
            chips.addView(b);
        }
        hsv.addView(chips);
        gridCard.addView(hsv);
        root.addView(gridCard);
    }

    private void finishRunner() {
        timerHandler.removeCallbacks(timerRunnable);
        List<Question> questions = new ArrayList<>();
        for (String qid : runningSim.questionIds) {
            Question q = DataRepository.findQuestionById(qid);
            if (q != null) questions.add(q);
        }
        int correct = 0;
        for (Question q : questions) {
            Integer a = runnerAnswers.get(q.id);
            if (a != null && a == q.answer) correct++;
        }

        SimuladoResult res = new SimuladoResult();
        res.id = UUID.randomUUID().toString();
        res.simuladoId = runningSim.id;
        res.correct = correct;
        res.total = questions.size();
        res.date = PlanEngine.dayKey(new Date());
        res.durationSec = Math.min(elapsedSec, runningSim.minutes * 60);
        res.answers = new HashMap<>(runnerAnswers);
        res.questionIds = new ArrayList<>(runningSim.questionIds);
        res.simName = runningSim.name;
        res.simExam = runningSim.exam;

        store.getState().history.add(0, res);
        store.save();

        this.currentResult = res;
        this.runningSim = null;
        this.mode = "result";
        render();
    }

    // --- 3. RESULTADO E GABARITO COMENTADO ---
    private void renderResult() {
        Button btnBack = new Button(requireContext());
        btnBack.setText("← Voltar aos simulados");
        btnBack.setAllCaps(false);
        btnBack.setOnClickListener(v -> {
            mode = "list";
            render();
        });
        root.addView(btnBack);

        int pct = Math.round((currentResult.correct * 100f) / Math.max(1, currentResult.total));
        LinearLayout scoreCard = createCard(true);

        TextView tvTitle = new TextView(requireContext());
        tvTitle.setText(currentResult.simExam + " · " + currentResult.simName);
        tvTitle.setTextColor(Color.parseColor("#99FFFFFF"));
        scoreCard.addView(tvTitle);

        TextView tvScore = new TextView(requireContext());
        tvScore.setText(pct + "% (" + currentResult.correct + "/" + currentResult.total + " acertos)");
        tvScore.setTextColor(getColorRes(R.color.lime));
        tvScore.setTextSize(32f);
        tvScore.setTypeface(null, Typeface.BOLD);
        scoreCard.addView(tvScore);

        TextView tvPts = new TextView(requireContext());
        tvPts.setText("Tempo: " + formatSec(currentResult.durationSec) + "   ·   +" + (currentResult.correct * 15) + " pontos no ranking");
        tvPts.setTextColor(Color.WHITE);
        scoreCard.addView(tvPts);
        root.addView(scoreCard);

        TextView tvGab = new TextView(requireContext());
        tvGab.setText("Gabarito comentado");
        tvGab.setTypeface(null, Typeface.BOLD);
        tvGab.setTextSize(20f);
        tvGab.setTextColor(getColorRes(R.color.ink));
        tvGab.setPadding(0, 16, 0, 12);
        root.addView(tvGab);

        for (int i = 0; i < currentResult.questionIds.size(); i++) {
            Question q = DataRepository.findQuestionById(currentResult.questionIds.get(i));
            if (q == null) continue;
            Integer chosen = currentResult.answers.get(q.id);
            boolean ok = chosen != null && chosen == q.answer;

            LinearLayout c = createCard(false);
            TextView p = new TextView(requireContext());
            p.setText((ok ? "✓ " : "✕ ") + (i + 1) + ". " + q.prompt);
            p.setTypeface(null, Typeface.BOLD);
            p.setTextColor(getColorRes(ok ? R.color.success_dark : R.color.ink));
            c.addView(p);

            String myAns = chosen != null ? LETTERS[chosen] + ") " + q.options.get(chosen) : "em branco";
            String rightAns = LETTERS[q.answer] + ") " + q.options.get(q.answer);
            TextView ans = new TextView(requireContext());
            ans.setText("Sua resposta: " + myAns + (ok ? "" : "\nCorreta: " + rightAns));
            ans.setPadding(0, 8, 0, 8);
            ans.setTextColor(getColorRes(R.color.ink));
            c.addView(ans);

            TextView exp = new TextView(requireContext());
            exp.setText("💡 " + q.explanation);
            exp.setTextColor(getColorRes(R.color.muted));
            c.addView(exp);
            root.addView(c);
        }
    }

    // --- 4. EDITOR DE SIMULADO PERSONALIZADO (SimuladoEditor.tsx) ---
    private void renderEditor() {
        Button btnBack = new Button(requireContext());
        btnBack.setText("← Cancelar");
        btnBack.setAllCaps(false);
        btnBack.setOnClickListener(v -> {
            mode = "list";
            render();
        });
        root.addView(btnBack);

        LinearLayout card = createCard(false);
        EditText edtName = new EditText(requireContext());
        edtName.setHint("Nome do simulado (Ex.: Revisão Exatas PSC)");
        edtName.setText(editorName);
        edtName.setBackgroundResource(R.drawable.bg_input);
        edtName.setPadding(28, 22, 28, 22);
        card.addView(edtName);

        TextView tvSub = new TextView(requireContext());
        tvSub.setText("Escolha os tópicos com banco de questões:");
        tvSub.setTypeface(null, Typeface.BOLD);
        tvSub.setPadding(0, 20, 0, 8);
        card.addView(tvSub);

        for (Topic t : DataRepository.TOPICS) {
            if (t.questions.isEmpty()) continue;
            CheckBox cb = new CheckBox(requireContext());
            cb.setText(t.subject.name + " · " + t.title + " (" + t.questions.size() + " questões)");
            cb.setChecked(editorTopicIds.contains(t.id));
            cb.setOnCheckedChangeListener((b, checked) -> {
                editorName = edtName.getText().toString();
                if (checked) {
                    if (!editorTopicIds.contains(t.id)) editorTopicIds.add(t.id);
                } else {
                    editorTopicIds.remove(t.id);
                }
            });
            card.addView(cb);
        }

        Button btnSave = new Button(requireContext());
        btnSave.setText("Criar simulado");
        btnSave.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getColorRes(R.color.accent)));
        btnSave.setTextColor(Color.WHITE);
        btnSave.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            if (name.isEmpty() || editorTopicIds.isEmpty()) {
                Toast.makeText(requireContext(), "Dê um nome e selecione ao menos 1 tópico.", Toast.LENGTH_SHORT).show();
                return;
            }
            List<String> qIds = new ArrayList<>();
            for (String tid : editorTopicIds) {
                Topic t = DataRepository.findTopicById(tid);
                for (Question q : t.questions) qIds.add(q.id);
            }
            Simulado custom = new Simulado("custom-" + UUID.randomUUID().toString().substring(0, 6),
                    name, "Meu simulado", editorTopicIds.size() + " tópico(s) selecionado(s)",
                    editorMinutes, qIds, new ArrayList<>(editorTopicIds), true);
            store.getState().customSimulados.add(0, custom);
            store.save();
            mode = "list";
            render();
        });
        card.addView(btnSave);
        root.addView(card);
    }

    private LinearLayout createCard(boolean dark) {
        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(36, 32, 36, 32);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 20);
        card.setLayoutParams(lp);
        card.setBackgroundResource(dark ? R.drawable.bg_card_dark : R.drawable.bg_card);
        return card;
    }

    private int getColorRes(int res) {
        return getResources().getColor(res, requireContext().getTheme());
    }
}
