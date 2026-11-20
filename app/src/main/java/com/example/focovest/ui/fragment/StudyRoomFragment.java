package com.example.focovest.ui.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.*;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.*;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.focovest.R;
import com.example.focovest.util.VideoEmbedHelper;
import com.example.focovest.data.DataRepository;
import com.example.focovest.data.DataRepository.*;
import com.example.focovest.domain.PlanEngine.PlanResult;
import com.example.focovest.data.StoreManager;
import com.example.focovest.ui.activity.MainActivity;

import java.text.SimpleDateFormat;
import java.util.*;

public class StudyRoomFragment extends Fragment {

    private static final String[] LETTERS = {"A", "B", "C", "D", "E"};

    private StoreManager store;
    private Topic currentTopic;
    private String currentPhase = "aula"; // "aula" | "exercicios" | "diagnostico"
    private String activeSubTab = "questoes"; // "questoes" | "notas" | "video"
    private boolean suppressNotesWatcher = false;
    private boolean isEditingVideo = false;
    private List<Topic> unlockedTopicsList = new ArrayList<>();

    private Spinner spinnerTopicSwitcher;
    private TextView btnNextInPlan, txtSubjectBadge, txtTopicMinutes, txtTopicTitle, txtTopicSummary;
    private TextView stepPillAula, stepPillExercicios, stepPillDiagnostico;
    private Button btnHeaderSecondary, btnHeaderPrimary;
    private LinearLayout sidePanelToggleBar;
    private TextView tabShowExercises, tabShowNotes, tabShowVideo;

    // Video views
    private LinearLayout videoSection, videoEmptyState, videoControlBar, videoFormBar;
    private TextView txtVideoEmptyHint, txtVideoProvider, txtVideoError;
    private WebView webViewVideo;
    private EditText edtVideoUrl;

    // Notes views
    private LinearLayout notesSection, aulaBottomBanner;
    private EditText edtNotes;
    private TextView txtWordCount, txtSavedStatus;

    // Exercises container
    private LinearLayout exercisesSection;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_study_room, container, false);
        store = StoreManager.getInstance(requireContext());

        bindViews(v);
        setupWebView();
        setupNotesToolbar(v);
        setupTopicSpinner();

        String requestedId = ((MainActivity) requireActivity()).getSelectedTopicId();
        if (requestedId == null) requestedId = store.getState().lastTopic;
        loadTopic(requestedId);

        return v;
    }

    private void bindViews(View v) {
        spinnerTopicSwitcher = v.findViewById(R.id.spinnerTopicSwitcher);
        btnNextInPlan = v.findViewById(R.id.btnNextInPlan);
        txtSubjectBadge = v.findViewById(R.id.txtSubjectBadge);
        txtTopicMinutes = v.findViewById(R.id.txtTopicMinutes);
        txtTopicTitle = v.findViewById(R.id.txtTopicTitle);
        txtTopicSummary = v.findViewById(R.id.txtTopicSummary);

        stepPillAula = v.findViewById(R.id.stepPillAula);
        stepPillExercicios = v.findViewById(R.id.stepPillExercicios);
        stepPillDiagnostico = v.findViewById(R.id.stepPillDiagnostico);
        btnHeaderSecondary = v.findViewById(R.id.btnHeaderSecondary);
        btnHeaderPrimary = v.findViewById(R.id.btnHeaderPrimary);

        sidePanelToggleBar = v.findViewById(R.id.sidePanelToggleBar);
        tabShowExercises = v.findViewById(R.id.tabShowExercises);
        tabShowNotes = v.findViewById(R.id.tabShowNotes);
        tabShowVideo = v.findViewById(R.id.tabShowVideo);

        videoSection = v.findViewById(R.id.videoSection);
        videoEmptyState = v.findViewById(R.id.videoEmptyState);
        videoControlBar = v.findViewById(R.id.videoControlBar);
        videoFormBar = v.findViewById(R.id.videoFormBar);
        txtVideoEmptyHint = v.findViewById(R.id.txtVideoEmptyHint);
        txtVideoProvider = v.findViewById(R.id.txtVideoProvider);
        txtVideoError = v.findViewById(R.id.txtVideoError);
        webViewVideo = v.findViewById(R.id.webViewVideo);
        edtVideoUrl = v.findViewById(R.id.edtVideoUrl);

        notesSection = v.findViewById(R.id.notesSection);
        aulaBottomBanner = v.findViewById(R.id.aulaBottomBanner);
        edtNotes = v.findViewById(R.id.edtNotes);
        txtWordCount = v.findViewById(R.id.txtWordCount);
        txtSavedStatus = v.findViewById(R.id.txtSavedStatus);

        exercisesSection = v.findViewById(R.id.exercisesSection);

        v.findViewById(R.id.btnSearchYoutube).setOnClickListener(view -> {
            String q = Uri.encode(currentTopic.title + " aula vestibular");
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=" + q)));
        });

        v.findViewById(R.id.btnSaveVideoUrl).setOnClickListener(view -> submitVideoUrl());
        v.findViewById(R.id.btnChangeVideo).setOnClickListener(view -> {
            isEditingVideo = true;
            renderVideoPlayer();
        });
        v.findViewById(R.id.btnRemoveVideo).setOnClickListener(view -> {
            store.getState().videos.remove(currentTopic.id);
            store.save();
            edtVideoUrl.setText("");
            isEditingVideo = false;
            renderVideoPlayer();
        });

        v.findViewById(R.id.btnBottomGoExercises).setOnClickListener(view -> setStage("exercicios"));

        tabShowExercises.setOnClickListener(view -> { activeSubTab = "questoes"; updateVisibilityByPhase(); });
        tabShowNotes.setOnClickListener(view -> { activeSubTab = "notas"; updateVisibilityByPhase(); });
        tabShowVideo.setOnClickListener(view -> { activeSubTab = "video"; updateVisibilityByPhase(); });
    }

    private void setupWebView() {
        WebSettings ws = webViewVideo.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        webViewVideo.setWebViewClient(new WebViewClient());
    }

    private void setupTopicSpinner() {
        unlockedTopicsList.clear();
        List<String> labels = new ArrayList<>();
        for (Topic t : DataRepository.TOPICS) {
            if (store.isTopicUnlocked(t.id)) {
                unlockedTopicsList.add(t);
                boolean done = store.getState().completed.contains(t.id);
                labels.add((done ? "✓ " : "") + t.subject.name + " · " + t.title);
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, labels);
        spinnerTopicSwitcher.setAdapter(adapter);

        spinnerTopicSwitcher.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Topic chosen = unlockedTopicsList.get(position);
                if (currentTopic == null || !chosen.id.equals(currentTopic.id)) {
                    loadTopic(chosen.id);
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadTopic(String topicId) {
        Topic found = DataRepository.findTopicById(topicId);
        if (!store.isTopicUnlocked(found.id) && !unlockedTopicsList.isEmpty()) {
            found = unlockedTopicsList.get(0);
        }
        currentTopic = found;
        store.getState().lastTopic = currentTopic.id;
        store.save();

        for (int i = 0; i < unlockedTopicsList.size(); i++) {
            if (unlockedTopicsList.get(i).id.equals(currentTopic.id)) {
                spinnerTopicSwitcher.setSelection(i);
                break;
            }
        }

        // Botão Próximo do Plano
        PlanResult plan = store.getPlanResult();
        String nextId = null;
        for (String id : plan.topicIds) {
            if (!id.equals(currentTopic.id) && !store.getState().completed.contains(id) && store.isTopicUnlocked(id)) {
                nextId = id;
                break;
            }
        }
        if (nextId != null) {
            final String targetNext = nextId;
            Topic nextTopic = DataRepository.findTopicById(nextId);
            btnNextInPlan.setVisibility(View.VISIBLE);
            btnNextInPlan.setText("Próximo do plano: " + nextTopic.title + " →");
            btnNextInPlan.setOnClickListener(v -> loadTopic(targetNext));
        } else {
            btnNextInPlan.setVisibility(View.GONE);
        }

        // Cabeçalho do Tópico
        GradientDrawable badgeBg = new GradientDrawable();
        badgeBg.setColor(currentTopic.subject.getSoftColorInt());
        badgeBg.setCornerRadius(999f);
        txtSubjectBadge.setBackground(badgeBg);
        txtSubjectBadge.setTextColor(currentTopic.subject.getColorInt());
        txtSubjectBadge.setText(currentTopic.subject.name);

        txtTopicMinutes.setText("~" + currentTopic.minutes + " min");
        txtTopicTitle.setText(currentTopic.title);
        txtTopicSummary.setText(currentTopic.summary);

        // Carregar Anotações do tópico
        suppressNotesWatcher = true;
        String savedNotes = store.getState().notes.getOrDefault(currentTopic.id, "");
        edtNotes.setText(savedNotes);
        updateWordCount(savedNotes);
        txtSavedStatus.setText("Salvamento automático");
        suppressNotesWatcher = false;

        isEditingVideo = false;
        activeSubTab = "questoes";
        refreshPhaseState();
        renderVideoPlayer();
        renderExercises();
    }

    private void setStage(String stage) {
        store.getState().topicStage.put(currentTopic.id, stage);
        store.save();
        activeSubTab = "questoes";
        refreshPhaseState();
        renderExercises();
    }

    private void redoTopic() {
        for (Question q : currentTopic.questions) {
            store.getState().answers.remove(q.id);
        }
        store.getState().topicStage.put(currentTopic.id, "exercicios");
        if (store.getState().completed.contains(currentTopic.id)) {
            store.toggleCompleted(currentTopic.id);
        } else {
            store.save();
        }
        setupTopicSpinner();
        refreshPhaseState();
        renderExercises();
    }

    private void refreshPhaseState() {
        boolean done = store.getState().completed.contains(currentTopic.id);
        currentPhase = done ? "diagnostico" : store.getState().topicStage.getOrDefault(currentTopic.id, "aula");

        int answered = 0;
        for (Question q : currentTopic.questions) {
            if (store.getState().answers.containsKey(q.id)) answered++;
        }
        boolean allAnswered = answered == currentTopic.questions.size();

        int accent = getResources().getColor(R.color.accent, requireContext().getTheme());
        int success = getResources().getColor(R.color.success, requireContext().getTheme());
        int muted = getResources().getColor(R.color.muted, requireContext().getTheme());

        stepPillAula.setTextColor("aula".equals(currentPhase) ? accent : success);
        stepPillExercicios.setTextColor("exercicios".equals(currentPhase) ? accent : (done ? success : muted));
        stepPillDiagnostico.setTextColor(done ? success : muted);

        if ("aula".equals(currentPhase)) {
            btnHeaderSecondary.setVisibility(View.GONE);
            btnHeaderPrimary.setText("Responder exercícios →");
            btnHeaderPrimary.setEnabled(true);
            btnHeaderPrimary.setAlpha(1f);
            btnHeaderPrimary.setOnClickListener(v -> setStage("exercicios"));
        } else if ("exercicios".equals(currentPhase)) {
            btnHeaderSecondary.setVisibility(View.VISIBLE);
            btnHeaderSecondary.setText("← Aula");
            btnHeaderSecondary.setOnClickListener(v -> setStage("aula"));

            btnHeaderPrimary.setText("✓ Marcar como concluído");
            btnHeaderPrimary.setEnabled(allAnswered);
            btnHeaderPrimary.setAlpha(allAnswered ? 1f : 0.35f);
            btnHeaderPrimary.setOnClickListener(v -> {
                store.toggleCompleted(currentTopic.id);
                setupTopicSpinner();
                refreshPhaseState();
                renderExercises();
            });
        } else {
            btnHeaderSecondary.setVisibility(View.VISIBLE);
            btnHeaderSecondary.setText("↺ Refazer");
            btnHeaderSecondary.setOnClickListener(v -> redoTopic());

            btnHeaderPrimary.setText("✓ Concluído");
            btnHeaderPrimary.setEnabled(false);
            btnHeaderPrimary.setAlpha(0.85f);
        }

        updateVisibilityByPhase();
    }

    private void updateVisibilityByPhase() {
        if ("aula".equals(currentPhase)) {
            sidePanelToggleBar.setVisibility(View.GONE);
            videoSection.setVisibility(View.VISIBLE);
            notesSection.setVisibility(View.VISIBLE);
            aulaBottomBanner.setVisibility(View.VISIBLE);
            exercisesSection.setVisibility(View.GONE);
        } else {
            sidePanelToggleBar.setVisibility(View.VISIBLE);
            aulaBottomBanner.setVisibility(View.GONE);

            tabShowExercises.setBackgroundResource("questoes".equals(activeSubTab) ? R.drawable.bg_card : 0);
            tabShowNotes.setBackgroundResource("notas".equals(activeSubTab) ? R.drawable.bg_card : 0);
            tabShowVideo.setBackgroundResource("video".equals(activeSubTab) ? R.drawable.bg_card : 0);

            exercisesSection.setVisibility("questoes".equals(activeSubTab) ? View.VISIBLE : View.GONE);
            notesSection.setVisibility("notas".equals(activeSubTab) ? View.VISIBLE : View.GONE);
            videoSection.setVisibility("video".equals(activeSubTab) ? View.VISIBLE : View.GONE);
        }
    }

    // --- VIDEO PLAYER LOGIC ---
    private void renderVideoPlayer() {
        String rawUrl = store.getState().videos.getOrDefault(currentTopic.id, "");
        edtVideoUrl.setText(rawUrl);
        txtVideoError.setVisibility(View.GONE);
        txtVideoEmptyHint.setText("Cole abaixo o link de um vídeo do YouTube, Vimeo ou Dailymotion sobre “" + currentTopic.title + "”.");

        VideoEmbedHelper.Embed embed = VideoEmbedHelper.toEmbed(rawUrl);
        if (embed != null && !isEditingVideo) {
            videoEmptyState.setVisibility(View.GONE);
            webViewVideo.setVisibility(View.VISIBLE);
            videoControlBar.setVisibility(View.VISIBLE);
            videoFormBar.setVisibility(View.GONE);
            txtVideoProvider.setText("Reproduzindo via " + embed.provider);
            webViewVideo.loadUrl(embed.src);
        } else {
            webViewVideo.setVisibility(View.GONE);
            videoControlBar.setVisibility(View.GONE);
            videoEmptyState.setVisibility(embed == null ? View.VISIBLE : View.GONE);
            videoFormBar.setVisibility(View.VISIBLE);
        }
    }

    private void submitVideoUrl() {
        String draft = edtVideoUrl.getText().toString().trim();
        if (draft.isEmpty()) return;
        VideoEmbedHelper.Embed embed = VideoEmbedHelper.toEmbed(draft);
        if (embed == null) {
            txtVideoError.setText("Link não reconhecido. Use um link do YouTube, Vimeo ou Dailymotion.");
            txtVideoError.setVisibility(View.VISIBLE);
            return;
        }
        store.getState().videos.put(currentTopic.id, draft);
        store.save();
        isEditingVideo = false;
        renderVideoPlayer();
    }

    // --- NOTES EDITOR LOGIC ---
    private void setupNotesToolbar(View v) {
        edtNotes.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (suppressNotesWatcher || currentTopic == null) return;
                String text = s.toString();
                store.getState().notes.put(currentTopic.id, text);
                store.save();
                updateWordCount(text);
                String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
                txtSavedStatus.setText("✓ Salvo às " + time);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        v.findViewById(R.id.btnFmtBold).setOnClickListener(view -> applySpan(new StyleSpan(Typeface.BOLD)));
        v.findViewById(R.id.btnFmtItalic).setOnClickListener(view -> applySpan(new StyleSpan(Typeface.ITALIC)));
        v.findViewById(R.id.btnFmtUnderline).setOnClickListener(view -> applySpan(new UnderlineSpan()));
        v.findViewById(R.id.btnFmtH2).setOnClickListener(view -> insertPrefix("\n## "));
        v.findViewById(R.id.btnFmtBullet).setOnClickListener(view -> insertPrefix("\n• "));
        v.findViewById(R.id.btnFmtQuote).setOnClickListener(view -> insertPrefix("\n❝ "));
        v.findViewById(R.id.btnFmtClear).setOnClickListener(view -> {
            Editable e = edtNotes.getText();
            Object[] spans = e.getSpans(0, e.length(), Object.class);
            for (Object span : spans) {
                if (span instanceof StyleSpan || span instanceof UnderlineSpan) {
                    e.removeSpan(span);
                }
            }
        });
    }

    private void applySpan(Object span) {
        int start = edtNotes.getSelectionStart();
        int end = edtNotes.getSelectionEnd();
        if (start < end) {
            edtNotes.getText().setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private void insertPrefix(String prefix) {
        int pos = Math.max(0, edtNotes.getSelectionStart());
        edtNotes.getText().insert(pos, prefix);
    }

    private void updateWordCount(String text) {
        String trimmed = text.trim();
        int words = trimmed.isEmpty() ? 0 : trimmed.split("\\s+").length;
        txtWordCount.setText(words + (words == 1 ? " palavra" : " palavras"));
    }

    // --- EXERCISES & DIAGNOSIS LOGIC (Exercises.tsx) ---
    private void renderExercises() {
        exercisesSection.removeAllViews();
        boolean review = "diagnostico".equals(currentPhase);
        List<Question> questions = currentTopic.questions;
        int total = questions.size();

        if (total == 0) {
            LinearLayout emptyCard = new LinearLayout(requireContext());
            emptyCard.setOrientation(LinearLayout.VERTICAL);
            emptyCard.setPadding(48, 48, 48, 48);
            emptyCard.setBackgroundResource(R.drawable.bg_card);

            TextView t1 = new TextView(requireContext());
            t1.setText(review ? "Sem exercícios para diagnóstico" : "Exercícios em construção");
            t1.setTextSize(17f);
            t1.setTypeface(null, Typeface.BOLD);
            t1.setTextColor(getResources().getColor(R.color.ink, requireContext().getTheme()));

            TextView t2 = new TextView(requireContext());
            t2.setText(review
                    ? "Este tópico ainda não tem banco de questões neste protótipo."
                    : "O banco de questões deste tópico ainda não está disponível neste protótipo. Revise suas anotações e conclua o tópico.");
            t2.setTextColor(getResources().getColor(R.color.muted, requireContext().getTheme()));
            t2.setPadding(0, 8, 0, 0);

            emptyCard.addView(t1);
            emptyCard.addView(t2);
            exercisesSection.addView(emptyCard);
            return;
        }

        int answered = 0, correct = 0;
        for (Question q : questions) {
            Integer chosen = store.getState().answers.get(q.id);
            if (chosen != null) {
                answered++;
                if (chosen == q.answer) correct++;
            }
        }
        int pct = Math.round((correct * 100f) / total);

        // Banner superior (Resumo de Diagnóstico ou Barra de Progresso de Fixação)
        LinearLayout topBanner = new LinearLayout(requireContext());
        topBanner.setOrientation(LinearLayout.VERTICAL);
        topBanner.setPadding(40, 32, 40, 32);
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        blp.setMargins(0, 0, 0, 24);
        topBanner.setLayoutParams(blp);
        topBanner.setBackgroundResource(R.drawable.bg_card_dark);

        if (review) {
            String diagLabel = pct == 100 ? "Gabaritou!" : (pct >= 60 ? "Bom resultado" : "Vale revisar a aula");
            TextView tvDiag = new TextView(requireContext());
            tvDiag.setText("DIAGNÓSTICO · " + pct + "% (" + diagLabel + ")");
            tvDiag.setTextColor(getResources().getColor(R.color.lime, requireContext().getTheme()));
            tvDiag.setTextSize(15f);
            tvDiag.setTypeface(null, Typeface.BOLD);

            TextView tvStats = new TextView(requireContext());
            tvStats.setText("✓ " + correct + " certas (+" + (correct * 10) + " pts)   ·   ✕ " + (total - correct) + " erradas");
            tvStats.setTextColor(Color.WHITE);
            tvStats.setPadding(0, 8, 0, 0);

            topBanner.addView(tvDiag);
            topBanner.addView(tvStats);
        } else {
            TextView tvProg = new TextView(requireContext());
            tvProg.setText("EXERCÍCIOS DE FIXAÇÃO · " + answered + " de " + total + " respondidas");
            tvProg.setTextColor(Color.WHITE);
            tvProg.setTypeface(null, Typeface.BOLD);
            topBanner.addView(tvProg);
        }
        exercisesSection.addView(topBanner);

        // Lista de Questões
        for (int qi = 0; qi < questions.size(); qi++) {
            Question q = questions.get(qi);
            Integer chosen = store.getState().answers.get(q.id);
            boolean ok = chosen != null && chosen == q.answer;

            LinearLayout qCard = new LinearLayout(requireContext());
            qCard.setOrientation(LinearLayout.VERTICAL);
            qCard.setPadding(40, 36, 40, 36);
            LinearLayout.LayoutParams qlp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            qlp.setMargins(0, 0, 0, 24);
            qCard.setLayoutParams(qlp);
            qCard.setBackgroundResource(R.drawable.bg_card);

            String prefix = review ? (ok ? "✓ " : "✕ ") : String.format(Locale.US, "%02d. ", qi + 1);
            TextView tvPrompt = new TextView(requireContext());
            tvPrompt.setText(prefix + q.prompt);
            tvPrompt.setTextSize(15f);
            tvPrompt.setTypeface(null, Typeface.BOLD);
            tvPrompt.setTextColor(getResources().getColor(R.color.ink, requireContext().getTheme()));
            qCard.addView(tvPrompt);

            for (int i = 0; i < q.options.size(); i++) {
                final int optIdx = i;
                boolean isAnswer = i == q.answer;
                boolean isChosen = chosen != null && i == chosen;

                TextView btnOpt = new TextView(requireContext());
                LinearLayout.LayoutParams olp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                olp.setMargins(0, 14, 0, 0);
                btnOpt.setLayoutParams(olp);
                btnOpt.setPadding(32, 24, 32, 24);

                String suffix = "";
                if (review) {
                    if (isChosen) suffix = "  (Sua resposta)";
                    else if (isAnswer) suffix = "  (Correta)";
                }
                btnOpt.setText(LETTERS[i] + ")  " + q.options.get(i) + suffix);
                btnOpt.setTextSize(14f);

                if (review) {
                    if (isAnswer) {
                        btnOpt.setBackgroundResource(R.drawable.bg_pill_success);
                        btnOpt.setTextColor(getResources().getColor(R.color.success_dark, requireContext().getTheme()));
                        btnOpt.setTypeface(null, Typeface.BOLD);
                    } else if (isChosen) {
                        btnOpt.setBackgroundResource(R.drawable.bg_button_accent);
                        btnOpt.setTextColor(Color.WHITE);
                    } else {
                        btnOpt.setBackgroundResource(R.drawable.bg_input);
                        btnOpt.setTextColor(getResources().getColor(R.color.muted, requireContext().getTheme()));
                    }
                } else {
                    if (isChosen) {
                        btnOpt.setBackgroundResource(R.drawable.bg_card_dark);
                        btnOpt.setTextColor(Color.WHITE);
                        btnOpt.setTypeface(null, Typeface.BOLD);
                    } else {
                        btnOpt.setBackgroundResource(R.drawable.bg_input);
                        btnOpt.setTextColor(getResources().getColor(R.color.ink, requireContext().getTheme()));
                    }
                    btnOpt.setOnClickListener(v -> {
                        store.getState().answers.put(q.id, optIdx);
                        store.save();
                        refreshPhaseState();
                        renderExercises();
                    });
                }
                qCard.addView(btnOpt);
            }

            if (review) {
                TextView tvExp = new TextView(requireContext());
                LinearLayout.LayoutParams elp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                elp.setMargins(0, 20, 0, 0);
                tvExp.setLayoutParams(elp);
                tvExp.setPadding(28, 22, 28, 22);
                tvExp.setBackgroundResource(R.drawable.bg_input);
                tvExp.setText("💡 Justificativa · alternativa " + LETTERS[q.answer] + ". " + q.explanation);
                tvExp.setTextSize(13f);
                tvExp.setTextColor(getResources().getColor(R.color.ink, requireContext().getTheme()));
                qCard.addView(tvExp);
            }

            exercisesSection.addView(qCard);
        }

        // Barra inferior de conclusão (FinishBar)
        if (!review) {
            boolean all = answered == total;
            Button btnFinishBottom = new Button(requireContext());
            btnFinishBottom.setText(all
                    ? "✓ Tudo respondido! Marcar tópico como concluído"
                    : answered + " de " + total + " respondidas (responda todas para concluir)");
            btnFinishBottom.setEnabled(all);
            btnFinishBottom.setAlpha(all ? 1f : 0.4f);
            btnFinishBottom.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    getResources().getColor(R.color.ink, requireContext().getTheme())));
            btnFinishBottom.setTextColor(Color.WHITE);
            btnFinishBottom.setOnClickListener(v -> {
                store.toggleCompleted(currentTopic.id);
                setupTopicSpinner();
                refreshPhaseState();
                renderExercises();
            });
            exercisesSection.addView(btnFinishBottom);
        }
    }
}
