package com.example.focovest.ui.fragment;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.focovest.R;
import com.example.focovest.data.DataRepository;
import com.example.focovest.data.DataRepository.Question;
import com.example.focovest.data.DataRepository.SubjectId;
import com.example.focovest.data.ExtrasStoreManager;
import com.example.focovest.data.StoreManager;
import com.example.focovest.data.StoreManager.SimuladoResult;
import com.example.focovest.model.AccessHistoryItem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Unifica Revisão de Acertos, Caderno de Erros e Histórico de Acessos a materiais.
 */
public class PerformanceHistoryFragment extends Fragment {

    private static final String[] LETTERS = {"A", "B", "C", "D", "E"};

    private StoreManager store;
    private ExtrasStoreManager extras;
    private LinearLayout root;

    // Abas: "erros" | "acertos" | "historico"
    private String activeTab = "erros";

    private static class AnsweredRecord {
        Question question;
        int chosenIndex;
        String sourceLabel;

        AnsweredRecord(Question question, int chosenIndex, String sourceLabel) {
            this.question = question;
            this.chosenIndex = chosenIndex;
            this.sourceLabel = sourceLabel;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_scroll_container, container, false);
        root = v.findViewById(R.id.rootContainer);
        store = StoreManager.getInstance(requireContext());
        extras = ExtrasStoreManager.getInstance(requireContext());
        render();
        return v;
    }

    private void render() {
        root.removeAllViews();

        List<AnsweredRecord> correctList = new ArrayList<>();
        List<AnsweredRecord> wrongList = new ArrayList<>();
        collectQuestionRecords(correctList, wrongList);

        TextView eyebrow = new TextView(requireContext());
        eyebrow.setText("DESEMPENHO E REVISÃO");
        eyebrow.setTextColor(getColorRes(R.color.accent));
        eyebrow.setTextSize(11f);
        eyebrow.setTypeface(null, Typeface.BOLD);
        root.addView(eyebrow);

        TextView title = new TextView(requireContext());
        title.setText("Acertos, erros e histórico.");
        title.setTextColor(getColorRes(R.color.ink));
        title.setTextSize(26f);
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(0, 4, 0, 14);
        root.addView(title);

        // Card de resumo geral
        int totalAnswered = correctList.size() + wrongList.size();
        int pct = totalAnswered > 0 ? Math.round((correctList.size() * 100f) / totalAnswered) : 0;
        LinearLayout summaryCard = createCard(true);

        TextView tvRate = new TextView(requireContext());
        tvRate.setText(pct + "% de aproveitamento");
        tvRate.setTextColor(getColorRes(R.color.lime));
        tvRate.setTextSize(26f);
        tvRate.setTypeface(null, Typeface.BOLD);
        summaryCard.addView(tvRate);

        TextView tvCounts = new TextView(requireContext());
        tvCounts.setText("✓ " + correctList.size() + " acertos   ·   ✕ " + wrongList.size()
                + " erros   ·   " + extras.getState().accessHistory.size() + " acessos recentes");
        tvCounts.setTextColor(Color.WHITE);
        tvCounts.setTextSize(13f);
        tvCounts.setPadding(0, 6, 0, 0);
        summaryCard.addView(tvCounts);
        root.addView(summaryCard);

        // Barra de abas
        LinearLayout tabBar = new LinearLayout(requireContext());
        tabBar.setOrientation(LinearLayout.HORIZONTAL);
        tabBar.setBackgroundResource(R.drawable.bg_input);
        tabBar.setPadding(6, 6, 6, 6);
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tlp.setMargins(0, 0, 0, 16);
        tabBar.setLayoutParams(tlp);

        tabBar.addView(createTab("Erros (" + wrongList.size() + ")", "erros"));
        tabBar.addView(createTab("Acertos (" + correctList.size() + ")", "acertos"));
        tabBar.addView(createTab("Histórico", "historico"));
        root.addView(tabBar);

        if ("acertos".equals(activeTab)) {
            renderSubjectBreakdown();
            renderQuestionRecords(correctList, true);
        } else if ("historico".equals(activeTab)) {
            renderAccessHistory();
        } else {
            renderQuestionRecords(wrongList, false);
        }
    }

    private void collectQuestionRecords(List<AnsweredRecord> correctOut, List<AnsweredRecord> wrongOut) {
        StoreManager.State state = store.getState();
        Map<String, AnsweredRecord> latestByQuestionId = new LinkedHashMap<>();

        // 1. Questões de fixação dos tópicos
        for (Map.Entry<String, Integer> entry : state.answers.entrySet()) {
            Question q = DataRepository.findQuestionById(entry.getKey());
            if (q != null && entry.getValue() != null) {
                latestByQuestionId.put(q.id, new AnsweredRecord(q, entry.getValue(), "Exercício de fixação"));
            }
        }

        // 2. Questões de simulados realizados
        for (SimuladoResult res : state.history) {
            if (res.answers == null) continue;
            for (Map.Entry<String, Integer> entry : res.answers.entrySet()) {
                Question q = DataRepository.findQuestionById(entry.getKey());
                if (q != null && entry.getValue() != null && !latestByQuestionId.containsKey(q.id)) {
                    String src = (res.simExam != null ? res.simExam + " · " : "") + res.simName;
                    latestByQuestionId.put(q.id, new AnsweredRecord(q, entry.getValue(), src));
                }
            }
        }

        for (AnsweredRecord rec : latestByQuestionId.values()) {
            if (rec.chosenIndex == rec.question.answer) {
                correctOut.add(rec);
            } else {
                wrongOut.add(rec);
            }
        }
    }

    private void renderSubjectBreakdown() {
        LinearLayout card = createCard(false);
        TextView tvTitle = new TextView(requireContext());
        tvTitle.setText("Taxa de acerto por disciplina");
        tvTitle.setTextColor(getColorRes(R.color.ink));
        tvTitle.setTextSize(16f);
        tvTitle.setTypeface(null, Typeface.BOLD);
        tvTitle.setPadding(0, 0, 0, 12);
        card.addView(tvTitle);

        for (SubjectId sub : SubjectId.values()) {
            int[] acc = store.getSubjectAccuracy(sub);
            int pct = acc[1] > 0 ? Math.round((acc[0] * 100f) / acc[1]) : 0;

            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, 6, 0, 6);

            TextView tvSub = new TextView(requireContext());
            tvSub.setLayoutParams(new LinearLayout.LayoutParams(210, LinearLayout.LayoutParams.WRAP_CONTENT));
            tvSub.setText(sub.name);
            tvSub.setTextColor(getColorRes(R.color.ink));
            tvSub.setTextSize(13f);

            ProgressBar pb = new ProgressBar(requireContext(), null, android.R.attr.progressBarStyleHorizontal);
            LinearLayout.LayoutParams pblp = new LinearLayout.LayoutParams(0, 18, 1f);
            pblp.setMargins(12, 0, 12, 0);
            pb.setLayoutParams(pblp);
            pb.setMax(100);
            pb.setProgress(pct);
            pb.setProgressTintList(android.content.res.ColorStateList.valueOf(sub.getColorInt()));

            TextView tvVal = new TextView(requireContext());
            tvVal.setLayoutParams(new LinearLayout.LayoutParams(110, LinearLayout.LayoutParams.WRAP_CONTENT));
            tvVal.setGravity(Gravity.END);
            tvVal.setText(acc[1] > 0 ? acc[0] + "/" + acc[1] : "—");
            tvVal.setTextColor(getColorRes(R.color.muted));
            tvVal.setTextSize(12f);

            row.addView(tvSub);
            row.addView(pb);
            row.addView(tvVal);
            card.addView(row);
        }
        root.addView(card);
    }

    private void renderQuestionRecords(List<AnsweredRecord> list, boolean isCorrectTab) {
        if (list.isEmpty()) {
            LinearLayout empty = createCard(false);
            TextView tv = new TextView(requireContext());
            tv.setText(isCorrectTab
                    ? "Você ainda não registrou acertos em exercícios ou simulados."
                    : "Nenhum erro registrado até agora! Excelente ritmo de estudos.");
            tv.setTextColor(getColorRes(R.color.muted));
            empty.addView(tv);
            root.addView(empty);
            return;
        }

        for (AnsweredRecord rec : list) {
            Question q = rec.question;
            LinearLayout card = createCard(false);

            TextView tvMeta = new TextView(requireContext());
            tvMeta.setText(q.subject.name.toUpperCase() + " · " + rec.sourceLabel);
            tvMeta.setTextColor(q.subject.getColorInt());
            tvMeta.setTextSize(11f);
            tvMeta.setTypeface(null, Typeface.BOLD);
            card.addView(tvMeta);

            TextView tvPrompt = new TextView(requireContext());
            tvPrompt.setText((isCorrectTab ? "✓ " : "✕ ") + q.prompt);
            tvPrompt.setTextColor(getColorRes(R.color.ink));
            tvPrompt.setTextSize(15f);
            tvPrompt.setTypeface(null, Typeface.BOLD);
            tvPrompt.setPadding(0, 6, 0, 8);
            card.addView(tvPrompt);

            String marked = rec.chosenIndex >= 0 && rec.chosenIndex < q.options.size()
                    ? LETTERS[rec.chosenIndex] + ") " + q.options.get(rec.chosenIndex)
                    : "—";
            String right = LETTERS[q.answer] + ") " + q.options.get(q.answer);

            TextView tvAns = new TextView(requireContext());
            tvAns.setText(isCorrectTab
                    ? "Resposta correta: " + right
                    : "Sua resposta: " + marked + "\nResposta correta: " + right);
            tvAns.setTextColor(getColorRes(isCorrectTab ? R.color.success_dark : R.color.accent));
            tvAns.setTextSize(13f);
            tvAns.setPadding(0, 0, 0, 8);
            card.addView(tvAns);

            TextView tvExp = new TextView(requireContext());
            tvExp.setText("💡 " + q.explanation);
            tvExp.setTextColor(getColorRes(R.color.muted));
            tvExp.setTextSize(12f);
            tvExp.setBackgroundResource(R.drawable.bg_input);
            tvExp.setPadding(24, 16, 24, 16);
            card.addView(tvExp);

            root.addView(card);
        }
    }

    private void renderAccessHistory() {
        List<AccessHistoryItem> items = extras.getState().accessHistory;
        if (items.isEmpty()) {
            LinearLayout empty = createCard(false);
            TextView tv = new TextView(requireContext());
            tv.setText("Seu histórico de materiais acessados (flashcards, textos, livros e provas anteriores) aparecerá aqui.");
            tv.setTextColor(getColorRes(R.color.muted));
            empty.addView(tv);
            root.addView(empty);
            return;
        }

        for (AccessHistoryItem item : items) {
            LinearLayout card = createCard(false);

            TextView tvType = new TextView(requireContext());
            String typeLabel = formatType(item.materialType);
            String subLabel = item.subject != null ? " · " + item.subject.name : "";
            tvType.setText(typeLabel + subLabel + " · " + item.accessedAt);
            tvType.setTextColor(item.subject != null ? item.subject.getColorInt() : getColorRes(R.color.accent));
            tvType.setTextSize(11f);
            tvType.setTypeface(null, Typeface.BOLD);
            card.addView(tvType);

            TextView tvTitle = new TextView(requireContext());
            tvTitle.setText(item.title);
            tvTitle.setTextColor(getColorRes(R.color.ink));
            tvTitle.setTextSize(15f);
            tvTitle.setTypeface(null, Typeface.BOLD);
            tvTitle.setPadding(0, 4, 0, 0);
            card.addView(tvTitle);

            root.addView(card);
        }

        Button btnClear = new Button(requireContext());
        btnClear.setText("Limpar histórico de acessos");
        btnClear.setAllCaps(false);
        btnClear.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getColorRes(R.color.ink)));
        btnClear.setTextColor(Color.WHITE);
        btnClear.setOnClickListener(v -> {
            extras.clearAccessHistory();
            render();
        });
        root.addView(btnClear);
    }

    private String formatType(String type) {
        if (type == null) return "MATERIAL";
        switch (type) {
            case "flashcard": return "FLASHCARD";
            case "texto": return "TEXTO DE APOIO";
            case "livro": return "LIVRO";
            case "prova": return "PROVA ANTERIOR";
            case "video": return "VIDEOAULA";
            case "simulado": return "SIMULADO";
            default: return type.toUpperCase();
        }
    }

    private TextView createTab(String label, String tabId) {
        boolean active = tabId.equals(activeTab);
        TextView tv = new TextView(requireContext());
        tv.setLayoutParams(new LinearLayout.LayoutParams(0, 82, 1f));
        tv.setGravity(Gravity.CENTER);
        tv.setText(label);
        tv.setTextSize(12f);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setBackgroundResource(active ? R.drawable.bg_card : 0);
        tv.setTextColor(getColorRes(active ? R.color.ink : R.color.muted));
        tv.setOnClickListener(v -> {
            activeTab = tabId;
            render();
        });
        return tv;
    }

    private LinearLayout createCard(boolean dark) {
        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(34, 28, 34, 28);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 16);
        card.setLayoutParams(lp);
        card.setBackgroundResource(dark ? R.drawable.bg_card_dark : R.drawable.bg_card);
        return card;
    }

    private int getColorRes(int res) {
        return getResources().getColor(res, requireContext().getTheme());
    }
}
