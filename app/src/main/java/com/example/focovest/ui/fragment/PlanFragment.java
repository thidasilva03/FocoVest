package com.example.focovest.ui.fragment;

import android.graphics.Color;
import android.graphics.Typeface;
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

import java.util.*;

public class PlanFragment extends Fragment {

    private StoreManager store;
    private LinearLayout root;
    private boolean isEditingPlan = false;
    private StudyPlan editingTarget = null;
    private boolean editingTurma = false;
    private int weeksShown = 3;

    // Estado temporário do PlanEditor
    private String draftName = "";
    private List<ExamId> draftExams = new ArrayList<>();
    private String draftDeadlineMode = "edital"; // "edital" | "3m" | "6m"
    private int draftHours = 3;
    private List<Integer> draftDays = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_scroll_container, container, false);
        root = v.findViewById(R.id.rootContainer);
        store = StoreManager.getInstance(requireContext());
        render();
        return v;
    }

    private void render() {
        root.removeAllViews();
        StoreManager.State state = store.getState();

        if (isEditingPlan) {
            renderPlanEditor();
            return;
        }

        // Caso esteja no modo turma mas ainda não inseriu o código AM-2025
        if ("turma".equals(state.mode) && !state.turmaJoined) {
            addEyebrow("PLANO DE ESTUDOS");
            addTitle("Minha turma");

            LinearLayout card = createCard(false);
            TextView tvPrompt = new TextView(requireContext());
            tvPrompt.setText("Entre em uma turma\nUse o código enviado pelos organizadores (teste: AM-2025).");
            tvPrompt.setTextColor(getColorRes(R.color.ink));
            card.addView(tvPrompt);

            EditText edtCode = new EditText(requireContext());
            edtCode.setHint("CÓDIGO DA TURMA");
            edtCode.setBackgroundResource(R.drawable.bg_input);
            edtCode.setPadding(32, 24, 32, 24);
            LinearLayout.LayoutParams elp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            elp.setMargins(0, 24, 0, 16);
            edtCode.setLayoutParams(elp);
            card.addView(edtCode);

            Button btnJoin = new Button(requireContext());
            btnJoin.setText("Entrar na turma");
            btnJoin.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getColorRes(R.color.ink)));
            btnJoin.setTextColor(Color.WHITE);
            btnJoin.setOnClickListener(v -> {
                if (DataRepository.TURMA_CODE.equalsIgnoreCase(edtCode.getText().toString().trim())) {
                    state.turmaJoined = true;
                    store.save();
                    render();
                } else {
                    Toast.makeText(requireContext(), "Código não encontrado. Use AM-2025", Toast.LENGTH_SHORT).show();
                }
            });
            card.addView(btnJoin);
            root.addView(card);
            return;
        }

        boolean inTurma = store.inTurma();
        StudyPlan activePlan = store.getActivePlan();
        PlanResult plan = store.getPlanResult();

        addEyebrow(inTurma ? "PLANO DE ESTUDOS · TURMA" : "PLANO DE ESTUDOS");
        addTitle(activePlan.name);

        if (inTurma) {
            renderTurmaHeaderBar(plan);
        } else {
            renderAutonomousPlanBar(activePlan);
        }

        // Resumo do Plano (PlanSummary)
        renderPlanSummary(activePlan, plan);

        // Mural e Colegas quando em Turma (TurmaSide)
        if (inTurma) {
            renderTurmaSide(plan);
        }

        // Lista de Semanas e Dias (PlanDays)
        renderPlanDays(plan);
    }

    private void renderAutonomousPlanBar(StudyPlan activePlan) {
        StoreManager.State state = store.getState();
        LinearLayout bar = createCard(false);

        HorizontalScrollView hsv = new HorizontalScrollView(requireContext());
        hsv.setScrollBarSize(0);
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);

        // Botão Plano do Edital
        row.addView(createPlanChip("Plano do edital", StoreManager.EDITAL_PLAN_ID.equals(activePlan.id), () -> {
            state.activePlanId = StoreManager.EDITAL_PLAN_ID;
            store.save();
            render();
        }));

        // Planos personalizados criados
        for (StudyPlan p : state.plans) {
            row.addView(createPlanChip(p.name, p.id.equals(activePlan.id), () -> {
                state.activePlanId = p.id;
                store.save();
                render();
            }));
        }

        // Botão + Novo Plano
        Button btnNew = new Button(requireContext());
        btnNew.setText("+ Plano personalizado");
        btnNew.setAllCaps(false);
        btnNew.setOnClickListener(v -> openPlanEditor(null, false));
        row.addView(btnNew);

        hsv.addView(row);
        bar.addView(hsv);

        if (!StoreManager.EDITAL_PLAN_ID.equals(activePlan.id)) {
            LinearLayout actions = new LinearLayout(requireContext());
            actions.setOrientation(LinearLayout.HORIZONTAL);
            actions.setPadding(0, 16, 0, 0);

            Button btnEdit = new Button(requireContext());
            btnEdit.setText("✎ Editar plano");
            btnEdit.setAllCaps(false);
            btnEdit.setOnClickListener(v -> openPlanEditor(activePlan, false));

            Button btnDel = new Button(requireContext());
            btnDel.setText("🗑 Excluir");
            btnDel.setAllCaps(false);
            btnDel.setTextColor(getColorRes(R.color.accent));
            btnDel.setOnClickListener(v -> {
                state.plans.removeIf(p -> p.id.equals(activePlan.id));
                state.activePlanId = StoreManager.EDITAL_PLAN_ID;
                store.save();
                render();
            });

            actions.addView(btnEdit);
            actions.addView(btnDel);
            bar.addView(actions);
        }
        root.addView(bar);
    }

    private View createPlanChip(String title, boolean active, Runnable onClick) {
        TextView tv = new TextView(requireContext());
        tv.setText((active ? "✓ " : "") + title);
        tv.setPadding(32, 18, 32, 18);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 12, 0);
        tv.setLayoutParams(lp);
        tv.setBackgroundResource(active ? R.drawable.bg_card_dark : R.drawable.bg_input);
        tv.setTextColor(active ? Color.WHITE : getColorRes(R.color.ink));
        tv.setTypeface(null, Typeface.BOLD);
        tv.setOnClickListener(v -> onClick.run());
        return tv;
    }

    private void renderTurmaHeaderBar(PlanResult plan) {
        StoreManager.State state = store.getState();
        LinearLayout card = createCard(true);

        TextView code = new TextView(requireContext());
        code.setText(DataRepository.TURMA_CODE + " · " + state.releasedWeeks + " semana(s) liberada(s)");
        code.setTextColor(getColorRes(R.color.lime));
        code.setTypeface(null, Typeface.BOLD);
        card.addView(code);

        // Alternador Aluno vs Organizador
        LinearLayout toggleRow = new LinearLayout(requireContext());
        toggleRow.setOrientation(LinearLayout.HORIZONTAL);
        toggleRow.setPadding(0, 16, 0, 0);

        Button btnMode = new Button(requireContext());
        btnMode.setAllCaps(false);
        btnMode.setText(state.organizerView ? "Modo: Organizador (tocar p/ Aluno)" : "Modo: Aluno (tocar p/ Organizador)");
        btnMode.setOnClickListener(v -> {
            state.organizerView = !state.organizerView;
            store.save();
            render();
        });
        toggleRow.addView(btnMode);
        card.addView(toggleRow);

        if (state.organizerView) {
            LinearLayout orgRow = new LinearLayout(requireContext());
            orgRow.setOrientation(LinearLayout.HORIZONTAL);
            orgRow.setPadding(0, 12, 0, 0);

            Button btnMinus = new Button(requireContext());
            btnMinus.setText("- Semana");
            btnMinus.setEnabled(state.releasedWeeks > 1);
            btnMinus.setOnClickListener(v -> {
                state.releasedWeeks = Math.max(1, state.releasedWeeks - 1);
                store.save();
                render();
            });

            Button btnPlus = new Button(requireContext());
            btnPlus.setText("+ Liberar próxima");
            btnPlus.setOnClickListener(v -> {
                state.releasedWeeks++;
                store.save();
                render();
            });

            Button btnEditTurma = new Button(requireContext());
            btnEditTurma.setText("✎ Editar");
            btnEditTurma.setOnClickListener(v -> openPlanEditor(state.turmaPlan, true));

            orgRow.addView(btnMinus);
            orgRow.addView(btnPlus);
            orgRow.addView(btnEditTurma);
            card.addView(orgRow);
        }

        root.addView(card);
    }

    private void renderPlanSummary(StudyPlan activePlan, PlanResult plan) {
        StoreManager.State state = store.getState();
        LinearLayout card = createCard(false);

        int doneCount = 0;
        for (String id : plan.topicIds) {
            if (state.completed.contains(id)) doneCount++;
        }
        int total = plan.topicIds.size();
        int pct = total > 0 ? Math.round((doneCount * 100f) / total) : 0;

        TextView tvStats = new TextView(requireContext());
        tvStats.setText(doneCount + "/" + total + " tópicos concluídos (" + pct + "%) · "
                + plan.perDay + " tópicos/dia (" + activePlan.dailyHours + "h)");
        tvStats.setTypeface(null, Typeface.BOLD);
        tvStats.setTextColor(getColorRes(R.color.ink));
        card.addView(tvStats);

        ProgressBar pb = new ProgressBar(requireContext(), null, android.R.attr.progressBarStyleHorizontal);
        LinearLayout.LayoutParams pblp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 18);
        pblp.setMargins(0, 14, 0, 14);
        pb.setLayoutParams(pblp);
        pb.setMax(100);
        pb.setProgress(pct);
        pb.setProgressTintList(android.content.res.ColorStateList.valueOf(getColorRes(R.color.accent)));
        card.addView(pb);

        if (plan.endDate != null) {
            TextView tvEnd = new TextView(requireContext());
            tvEnd.setText("Último tópico previsto: " + PlanEngine.fmtShort(plan.endDate));
            tvEnd.setTextColor(getColorRes(R.color.muted));
            tvEnd.setTextSize(12f);
            card.addView(tvEnd);
        }

        if (!plan.dropped.isEmpty() && !store.inTurma()) {
            TextView tvDrop = new TextView(requireContext());
            tvDrop.setText("⚠ Para caber até o prazo, " + plan.dropped.size() + " tópicos de menor incidência ficaram de fora.");
            tvDrop.setTextColor(getColorRes(R.color.accent));
            tvDrop.setTextSize(12f);
            tvDrop.setPadding(0, 8, 0, 0);
            card.addView(tvDrop);
        }

        root.addView(card);
    }

    private void renderTurmaSide(PlanResult plan) {
        StoreManager.State state = store.getState();
        LinearLayout muralCard = createCard(false);

        TextView title = new TextView(requireContext());
        title.setText("📢 Mural da Turma");
        title.setTypeface(null, Typeface.BOLD);
        title.setTextSize(16f);
        title.setTextColor(getColorRes(R.color.ink));
        muralCard.addView(title);

        if (state.organizerView) {
            EditText edtMsg = new EditText(requireContext());
            edtMsg.setHint("Escreva um aviso para a turma…");
            edtMsg.setBackgroundResource(R.drawable.bg_input);
            edtMsg.setPadding(24, 18, 24, 18);
            muralCard.addView(edtMsg);

            Button btnPost = new Button(requireContext());
            btnPost.setText("Publicar aviso");
            btnPost.setAllCaps(false);
            btnPost.setOnClickListener(v -> {
                String txt = edtMsg.getText().toString().trim();
                if (!txt.isEmpty()) {
                    state.announcements.add(0, new StoreManager.Announcement(
                            UUID.randomUUID().toString(), txt,
                            DataRepository.TURMA_ORGANIZERS.get(0).name, PlanEngine.dayKey(new Date())));
                    store.save();
                    render();
                }
            });
            muralCard.addView(btnPost);
        }

        for (int i = 0; i < Math.min(3, state.announcements.size()); i++) {
            StoreManager.Announcement a = state.announcements.get(i);
            TextView tvA = new TextView(requireContext());
            tvA.setText("“" + a.text + "”\n— " + a.author + " (" + a.date + ")");
            tvA.setPadding(24, 16, 24, 16);
            tvA.setBackgroundResource(R.drawable.bg_input);
            LinearLayout.LayoutParams alp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            alp.setMargins(0, 12, 0, 0);
            tvA.setLayoutParams(alp);
            tvA.setTextColor(getColorRes(R.color.ink));
            tvA.setTextSize(13f);
            muralCard.addView(tvA);
        }
        root.addView(muralCard);
    }

    private void renderPlanDays(PlanResult plan) {
        StoreManager.State state = store.getState();
        StudyPlan activePlan = store.getActivePlan();
        int maxDays = Math.min(plan.days.size(), weeksShown * 5);

        for (int i = 0; i < maxDays; i++) {
            PlanDay d = plan.days.get(i);
            LinearLayout dayCard = createCard(false);

            TextView tvDate = new TextView(requireContext());
            tvDate.setText(PlanEngine.fmtShort(d.date).toUpperCase(Locale.ROOT));
            tvDate.setTextColor(getColorRes(R.color.accent));
            tvDate.setTypeface(null, Typeface.BOLD);
            tvDate.setTextSize(TwelveSpToPx());
            dayCard.addView(tvDate);

            for (String tid : d.topicIds) {
                Topic t = DataRepository.findTopicById(tid);
                boolean done = state.completed.contains(tid);
                boolean unlocked = store.isTopicUnlocked(tid);
                int inc = PlanEngine.incidenceOf(t, activePlan.exams);

                LinearLayout row = new LinearLayout(requireContext());
                row.setOrientation(LinearLayout.VERTICAL);
                row.setPadding(28, 20, 28, 20);
                LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                rlp.setMargins(0, 12, 0, 0);
                row.setLayoutParams(rlp);
                row.setBackgroundResource(done ? R.drawable.bg_pill_success : R.drawable.bg_input);

                String statusPrefix = done ? "✓ " : (!unlocked ? "🔒 " : "");
                TextView tvT = new TextView(requireContext());
                tvT.setText(statusPrefix + t.title);
                tvT.setTypeface(null, Typeface.BOLD);
                tvT.setTextColor(getColorRes(R.color.ink));

                TextView tvMeta = new TextView(requireContext());
                tvMeta.setText(t.subject.name + " · " + t.minutes + " min · Incidência " + PlanEngine.getIncidenceLabel(inc));
                tvMeta.setTextColor(t.subject.getColorInt());
                tvMeta.setTextSize(12f);

                row.addView(tvT);
                row.addView(tvMeta);

                if (unlocked) {
                    row.setOnClickListener(v -> ((MainActivity) requireActivity()).openTopic(t.id));
                }
                dayCard.addView(row);
            }
            root.addView(dayCard);
        }

        if (plan.days.size() > maxDays) {
            Button btnMore = new Button(requireContext());
            btnMore.setText("Mostrar mais semanas (" + (plan.days.size() - maxDays) + " dias restantes)");
            btnMore.setAllCaps(false);
            btnMore.setOnClickListener(v -> {
                weeksShown += 3;
                render();
            });
            root.addView(btnMore);
        }
    }

    // --- PLAN EDITOR (PlanEditor.tsx) ---
    private void openPlanEditor(StudyPlan initial, boolean isTurma) {
        StoreManager.State state = store.getState();
        this.isEditingPlan = true;
        this.editingTarget = initial;
        this.editingTurma = isTurma;
        this.draftName = initial != null ? initial.name : "";
        this.draftExams = new ArrayList<>(initial != null ? initial.exams : state.targetExams);
        this.draftHours = initial != null ? initial.dailyHours : state.dailyGoal;
        this.draftDays = new ArrayList<>(initial != null ? initial.studyDays : state.studyDays);
        this.draftDeadlineMode = (initial != null && initial.deadline != null) ? "6m" : "edital";
        render();
    }

    private void renderPlanEditor() {
        Button btnBack = new Button(requireContext());
        btnBack.setText("← Voltar ao plano");
        btnBack.setAllCaps(false);
        btnBack.setOnClickListener(v -> {
            isEditingPlan = false;
            render();
        });
        root.addView(btnBack);

        addEyebrow(editingTurma ? "PLANO DA TURMA" : "PLANO PERSONALIZADO");
        addTitle(editingTarget != null ? "Editar plano" : "Novo plano de estudos");

        LinearLayout card = createCard(false);

        EditText edtName = new EditText(requireContext());
        edtName.setHint("Nome do plano (Ex.: ENEM em 6 meses)");
        edtName.setText(draftName);
        edtName.setBackgroundResource(R.drawable.bg_input);
        edtName.setPadding(28, 22, 28, 22);
        card.addView(edtName);

        // Seletor de Vestibulares
        TextView tvEx = new TextView(requireContext());
        tvEx.setText("Vestibulares incluídos:");
        tvEx.setTypeface(null, Typeface.BOLD);
        tvEx.setPadding(0, 24, 0, 8);
        card.addView(tvEx);

        for (ExamId ex : ExamId.values()) {
            CheckBox cb = new CheckBox(requireContext());
            cb.setText(ex.id + " (" + ex.institution + ")");
            cb.setChecked(draftExams.contains(ex));
            cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                draftName = edtName.getText().toString();
                if (isChecked) {
                    if (!draftExams.contains(ex)) draftExams.add(ex);
                } else {
                    draftExams.remove(ex);
                }
            });
            card.addView(cb);
        }

        // Horas por dia
        LinearLayout hoursRow = new LinearLayout(requireContext());
        hoursRow.setOrientation(LinearLayout.HORIZONTAL);
        hoursRow.setGravity(Gravity.CENTER_VERTICAL);
        hoursRow.setPadding(0, 24, 0, 12);

        TextView tvHours = new TextView(requireContext());
        tvHours.setText("Horas por dia: " + draftHours + "h (≈ " + PlanEngine.topicsPerDay(draftHours) + " tópicos/dia)");
        tvHours.setTypeface(null, Typeface.BOLD);
        hoursRow.addView(tvHours);

        Button btnMinus = new Button(requireContext());
        btnMinus.setText("-");
        btnMinus.setOnClickListener(v -> {
            draftName = edtName.getText().toString();
            draftHours = Math.max(1, draftHours - 1);
            render();
        });

        Button btnPlus = new Button(requireContext());
        btnPlus.setText("+");
        btnPlus.setOnClickListener(v -> {
            draftName = edtName.getText().toString();
            draftHours = Math.min(10, draftHours + 1);
            render();
        });

        hoursRow.addView(btnMinus);
        hoursRow.addView(btnPlus);
        card.addView(hoursRow);

        Button btnSave = new Button(requireContext());
        btnSave.setText("Salvar plano");
        btnSave.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getColorRes(R.color.accent)));
        btnSave.setTextColor(Color.WHITE);
        btnSave.setOnClickListener(v -> {
            String finalName = edtName.getText().toString().trim();
            if (finalName.isEmpty() || draftExams.isEmpty()) {
                Toast.makeText(requireContext(), "Preencha o nome e escolha ao menos 1 vestibular.", Toast.LENGTH_SHORT).show();
                return;
            }
            String id = editingTarget != null ? editingTarget.id : "plano-" + UUID.randomUUID().toString().substring(0, 6);
            String start = editingTarget != null ? editingTarget.startDate : null;
            StudyPlan saved = new StudyPlan(id, finalName, new ArrayList<>(draftExams), null, draftHours, new ArrayList<>(draftDays), start);

            StoreManager.State s = store.getState();
            if (editingTurma) {
                s.turmaPlan = saved;
            } else {
                boolean replaced = false;
                for (int i = 0; i < s.plans.size(); i++) {
                    if (s.plans.get(i).id.equals(id)) {
                        s.plans.set(i, saved);
                        replaced = true;
                        break;
                    }
                }
                if (!replaced) s.plans.add(saved);
                s.activePlanId = id;
            }
            store.save();
            isEditingPlan = false;
            render();
        });
        card.addView(btnSave);
        root.addView(card);
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
        tv.setPadding(0, 4, 0, 16);
        root.addView(tv);
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

    private float TwelveSpToPx() { return 12f; }
}
