package com.example.focovest.ui.fragment;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import com.example.focovest.domain.PlanEngine;
import com.example.focovest.domain.PlanEngine.PlanResult;
import com.example.focovest.model.ExamId;
import com.example.focovest.model.Question;
import com.example.focovest.model.Topic;
import com.example.focovest.model.User;
import com.example.focovest.model.UserTopicProgress;
import com.example.focovest.util.VideoEmbedHelper;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Sala de Estudos offline-first:
 * - Carrega automaticamente custom_video_url ou default_video_url no WebView do YouTube.
 * - Mantém a tela de Aula (player) separada de Exercícios / Diagnóstico.
 * - Mantém o editor de Anotações disponível em ambas as telas, salvando em tempo real no SQLite.
 */
public class StudyRoomFragment extends Fragment {

    /** Nome do argumento declarado em nav_graph.xml. */
    public static final String ARG_TOPIC_ID = "topicId";

    private static final String[] LETTERS = {"A", "B", "C", "D", "E"};
    private static final String STATE_TOPIC_ID = "state_topic_id";
    private static final String STATE_SUB_TAB = "state_sub_tab";

    private FocoVestRepository repo;
    private String userId;

    private List<Topic> allTopics = new ArrayList<>();
    private Topic currentTopic;
    private List<Question> currentQuestions = new ArrayList<>();
    private UserTopicProgress currentProgress;

    private String currentPhase = "aula"; // "aula" | "exercicios" | "diagnostico"
    private String activeSubTab = "questoes"; // "questoes" | "notas"
    private boolean suppressNotesWatcher = false;
    private boolean suppressSpinnerSelection = false;
    private boolean isEditingVideo = false;
    private String lastLoadedVideoSrc = null;

    private Spinner spinnerTopicSwitcher;
    private TextView btnNextInPlan, txtSubjectBadge, txtTopicMinutes, txtTopicTitle, txtTopicSummary;
    private TextView stepPillAula, stepPillExercicios, stepPillDiagnostico;
    private Button btnHeaderSecondary, btnHeaderPrimary;
    private LinearLayout sidePanelToggleBar;
    private TextView tabShowExercises, tabShowNotes;

    // Video views
    private LinearLayout videoSection, videoEmptyState, videoControlBar, videoFormBar;
    private TextView txtVideoEmptyHint, txtVideoProvider, txtVideoError, btnRestoreDefaultVideo;
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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_study_room, container, false);
        repo = FocoVestRepository.getInstance(requireContext());
        userId = FocoVestRepository.LOCAL_USER_ID;

        bindViews(v);
        setupWebView();
        setupNotesToolbar(v);

        String requestedId = null;
        if (savedInstanceState != null) {
            requestedId = savedInstanceState.getString(STATE_TOPIC_ID, null);
            activeSubTab = savedInstanceState.getString(STATE_SUB_TAB, "questoes");
        }
        if (requestedId == null && getArguments() != null) {
            requestedId = getArguments().getString(ARG_TOPIC_ID);
        }
        if (requestedId == null) {
            User user = repo.getOrCreateLocalUser();
            if (user != null) requestedId = user.lastTopicId;
        }

        setupTopicSpinner(requestedId);
        loadTopic(requestedId);

        return v;
    }

    /** Monta os argumentos para abrir a Sala de Estudos diretamente em um tópico. */
    public static Bundle argsForTopic(String topicId) {
        Bundle args = new Bundle();
        args.putString(ARG_TOPIC_ID, topicId);
        return args;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (currentTopic != null) {
            outState.putString(STATE_TOPIC_ID, currentTopic.id);
        }
        outState.putString(STATE_SUB_TAB, activeSubTab);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (webViewVideo != null) {
            webViewVideo.onPause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (webViewVideo != null) {
            webViewVideo.onResume();
        }
    }

    @Override
    public void onDestroyView() {
        if (webViewVideo != null) {
            webViewVideo.stopLoading();
            webViewVideo.loadUrl("about:blank");
            webViewVideo.onPause();
            ViewGroup parent = (ViewGroup) webViewVideo.getParent();
            if (parent != null) {
                parent.removeView(webViewVideo);
            }
            webViewVideo.destroy();
            webViewVideo = null;
        }
        super.onDestroyView();
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

        videoSection = v.findViewById(R.id.videoSection);
        videoEmptyState = v.findViewById(R.id.videoEmptyState);
        videoControlBar = v.findViewById(R.id.videoControlBar);
        videoFormBar = v.findViewById(R.id.videoFormBar);
        txtVideoEmptyHint = v.findViewById(R.id.txtVideoEmptyHint);
        txtVideoProvider = v.findViewById(R.id.txtVideoProvider);
        txtVideoError = v.findViewById(R.id.txtVideoError);
        btnRestoreDefaultVideo = v.findViewById(R.id.btnRestoreDefaultVideo);
        webViewVideo = v.findViewById(R.id.webViewVideo);
        edtVideoUrl = v.findViewById(R.id.edtVideoUrl);

        notesSection = v.findViewById(R.id.notesSection);
        aulaBottomBanner = v.findViewById(R.id.aulaBottomBanner);
        edtNotes = v.findViewById(R.id.edtNotes);
        txtWordCount = v.findViewById(R.id.txtWordCount);
        txtSavedStatus = v.findViewById(R.id.txtSavedStatus);

        exercisesSection = v.findViewById(R.id.exercisesSection);

        v.findViewById(R.id.btnSearchYoutube).setOnClickListener(view -> {
            if (currentTopic == null) return;
            String q = Uri.encode(currentTopic.title + " aula vestibular");
            try {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.youtube.com/results?search_query=" + q)));
            } catch (ActivityNotFoundException e) {
                Toast.makeText(requireContext(), "Nenhum navegador encontrado para abrir o YouTube.",
                        Toast.LENGTH_SHORT).show();
            }
        });

        v.findViewById(R.id.btnSaveVideoUrl).setOnClickListener(view -> submitVideoUrl());
        v.findViewById(R.id.btnChangeVideo).setOnClickListener(view -> {
            isEditingVideo = !isEditingVideo;
            renderVideoPlayer(false);
        });
        btnRestoreDefaultVideo.setOnClickListener(view -> {
            if (currentTopic == null) return;
            repo.saveCustomVideoUrl(userId, currentTopic.id, null);
            currentProgress.customVideoUrl = null;
            isEditingVideo = false;
            lastLoadedVideoSrc = null;
            renderVideoPlayer(true);
            Toast.makeText(requireContext(), "Videoaula padrão restaurada.", Toast.LENGTH_SHORT).show();
        });

        v.findViewById(R.id.btnBottomGoExercises).setOnClickListener(view -> setStage("exercicios"));

        tabShowExercises.setOnClickListener(view -> {
            activeSubTab = "questoes";
            updateVisibilityByPhase();
        });
        tabShowNotes.setOnClickListener(view -> {
            activeSubTab = "notas";
            updateVisibilityByPhase();
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings ws = webViewVideo.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setMediaPlaybackRequiresUserGesture(false);
        ws.setAllowFileAccess(false);
        webViewVideo.setWebChromeClient(new WebChromeClient());
        webViewVideo.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String host = request.getUrl() != null ? request.getUrl().getHost() : null;
                if (host != null && (host.endsWith("youtube.com")
                        || host.endsWith("youtube-nocookie.com")
                        || host.endsWith("youtu.be")
                        || host.endsWith("googlevideo.com"))) {
                    return false;
                }
                return true;
            }
        });
    }

    private void setupTopicSpinner(String preserveTopicId) {
        List<Topic> dbTopics = repo.getAllTopics();
        User user = repo.getOrCreateLocalUser();
        Set<String> allowedIds = new java.util.HashSet<>();
        if (user != null) {
            for (ExamId ex : user.targetExams) {
                for (Topic t : PlanEngine.editalTopicsForExam(dbTopics, ex, user.highSchoolYear)) {
                    allowedIds.add(t.id);
                }
            }
        }
        if (preserveTopicId != null) {
            allowedIds.add(preserveTopicId);
        }

        allTopics = new ArrayList<>();
        for (Topic t : dbTopics) {
            if (allowedIds.isEmpty() || allowedIds.contains(t.id)) {
                allTopics.add(t);
            }
        }
        if (allTopics.isEmpty()) {
            allTopics = dbTopics;
        }

        Set<String> completedIds = repo.getCompletedTopicIds(userId);
        List<String> labels = new ArrayList<>();
        int targetIndex = 0;

        for (int i = 0; i < allTopics.size(); i++) {
            Topic t = allTopics.get(i);
            boolean done = completedIds.contains(t.id);
            labels.add((done ? "✓ " : "") + t.subject.name + " · " + t.title);
            if (preserveTopicId != null && t.id.equals(preserveTopicId)) {
                targetIndex = i;
            }
        }

        suppressSpinnerSelection = true;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, labels);
        spinnerTopicSwitcher.setAdapter(adapter);
        if (!allTopics.isEmpty()) {
            spinnerTopicSwitcher.setSelection(targetIndex, false);
        }
        spinnerTopicSwitcher.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (suppressSpinnerSelection) return;
                if (position < 0 || position >= allTopics.size()) return;
                Topic chosen = allTopics.get(position);
                if (currentTopic == null || !chosen.id.equals(currentTopic.id)) {
                    loadTopic(chosen.id);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        spinnerTopicSwitcher.post(() -> suppressSpinnerSelection = false);
    }

    private void loadTopic(String topicId) {
        Topic found = repo.getTopicById(topicId);
        if (found == null) return;

        currentTopic = found;
        currentQuestions = repo.getQuestionsForTopic(currentTopic.id);
        currentProgress = repo.getTopicProgress(userId, currentTopic.id);
        repo.updateLastTopic(userId, currentTopic.id);

        // Sincroniza seleção no Spinner sem disparar reload recursivo
        for (int i = 0; i < allTopics.size(); i++) {
            if (allTopics.get(i).id.equals(currentTopic.id)) {
                suppressSpinnerSelection = true;
                spinnerTopicSwitcher.setSelection(i, false);
                spinnerTopicSwitcher.post(() -> suppressSpinnerSelection = false);
                break;
            }
        }

        // Botão "Próximo do plano"
        User user = repo.getOrCreateLocalUser();
        ExamId primaryExam = (user != null && !user.targetExams.isEmpty()) ? user.targetExams.get(0) : ExamId.ENEM;
        int highSchoolYear = user != null ? user.highSchoolYear : 0;
        Set<String> completedIds = repo.getCompletedTopicIds(userId);
        PlanResult plan = PlanEngine.buildPlanForExam(
                primaryExam, highSchoolYear, allTopics,
                user != null ? user.dailyHours : 3,
                user != null ? user.studyDays : null,
                completedIds, repo.getCompletedAtMap(userId));

        Topic nextTopic = null;
        for (Topic t : plan.edital) {
            if (!t.id.equals(currentTopic.id) && !completedIds.contains(t.id)) {
                nextTopic = t;
                break;
            }
        }
        if (nextTopic != null) {
            final String nextId = nextTopic.id;
            String examLabel = user != null ? user.getExamLabel(primaryExam) : primaryExam.id;
            btnNextInPlan.setVisibility(View.VISIBLE);
            btnNextInPlan.setText("Próximo no " + examLabel + ": " + nextTopic.title + " →");
            btnNextInPlan.setOnClickListener(v -> loadTopic(nextId));
        } else {
            btnNextInPlan.setVisibility(View.GONE);
        }

        // Cabeçalho do tópico
        GradientDrawable badgeBg = new GradientDrawable();
        badgeBg.setColor(currentTopic.subject.getSoftColorInt());
        badgeBg.setCornerRadius(999f);
        txtSubjectBadge.setBackground(badgeBg);
        txtSubjectBadge.setTextColor(currentTopic.subject.getColorInt());
        txtSubjectBadge.setText(currentTopic.subject.name);

        txtTopicMinutes.setText("~" + currentTopic.minutes + " min");
        txtTopicTitle.setText(currentTopic.title);
        txtTopicSummary.setText(currentTopic.summary);

        // Carregar anotações do usuário para este tópico
        suppressNotesWatcher = true;
        edtNotes.setText(currentProgress.notes);
        updateWordCount(currentProgress.notes);
        txtSavedStatus.setText("Salvamento automático");
        suppressNotesWatcher = false;

        isEditingVideo = false;
        lastLoadedVideoSrc = null;
        activeSubTab = "questoes";

        refreshPhaseState();
        renderVideoPlayer(true);
        renderExercises();
    }

    private void setStage(String stage) {
        if (currentTopic == null) return;
        currentProgress.stage = stage;
        repo.saveTopicStage(userId, currentTopic.id, stage);
        activeSubTab = "questoes";
        if (!"aula".equals(stage) && webViewVideo != null) {
            webViewVideo.onPause();
        } else if ("aula".equals(stage) && webViewVideo != null) {
            webViewVideo.onResume();
        }
        refreshPhaseState();
        renderExercises();
    }

    private void redoTopic() {
        if (currentTopic == null) return;
        repo.redoTopic(userId, currentTopic.id);
        currentProgress = repo.getTopicProgress(userId, currentTopic.id);
        activeSubTab = "questoes";
        setupTopicSpinner(currentTopic.id);
        refreshPhaseState();
        renderExercises();
    }

    private void refreshPhaseState() {
        if (currentTopic == null || currentProgress == null) return;
        boolean done = currentProgress.completed;
        currentPhase = done ? "diagnostico" : currentProgress.stage;

        Map<String, Integer> answers = repo.getAnswersForTopic(userId, currentTopic.id);
        int answered = 0;
        for (Question q : currentQuestions) {
            if (answers.containsKey(q.id)) answered++;
        }
        boolean allAnswered = !currentQuestions.isEmpty() && answered == currentQuestions.size();

        int accent = getColorRes(R.color.accent);
        int success = getColorRes(R.color.success);
        int muted = getColorRes(R.color.muted);

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
            btnHeaderPrimary.setOnClickListener(v -> completeCurrentTopic());
        } else {
            btnHeaderSecondary.setVisibility(View.VISIBLE);
            btnHeaderSecondary.setText("← Aula");
            btnHeaderSecondary.setOnClickListener(v -> {
                currentPhase = "aula";
                updateVisibilityByPhase();
                btnHeaderSecondary.setText("↺ Refazer");
                btnHeaderSecondary.setOnClickListener(v2 -> redoTopic());
                btnHeaderPrimary.setText("Ver diagnóstico →");
                btnHeaderPrimary.setEnabled(true);
                btnHeaderPrimary.setAlpha(1f);
                btnHeaderPrimary.setOnClickListener(v2 -> {
                    refreshPhaseState();
                    renderExercises();
                });
            });

            btnHeaderPrimary.setText("✓ Concluído · ↺ Refazer");
            btnHeaderPrimary.setEnabled(true);
            btnHeaderPrimary.setAlpha(0.95f);
            btnHeaderPrimary.setOnClickListener(v -> redoTopic());
        }

        updateVisibilityByPhase();
    }

    private void completeCurrentTopic() {
        if (currentTopic == null) return;
        repo.markTopicCompleted(userId, currentTopic.id);
        currentProgress = repo.getTopicProgress(userId, currentTopic.id);
        setupTopicSpinner(currentTopic.id);
        refreshPhaseState();
        renderExercises();
        Toast.makeText(requireContext(), "Tópico concluído! +30 pontos + bônus de acertos.",
                Toast.LENGTH_SHORT).show();
    }

    private void updateVisibilityByPhase() {
        int ink = getColorRes(R.color.ink);
        int muted = getColorRes(R.color.muted);

        if ("aula".equals(currentPhase)) {
            // Fase Aula: player de vídeo + editor de anotações (sem exercícios misturados)
            sidePanelToggleBar.setVisibility(View.GONE);
            videoSection.setVisibility(View.VISIBLE);
            notesSection.setVisibility(View.VISIBLE);
            aulaBottomBanner.setVisibility(currentProgress != null && currentProgress.completed ? View.GONE : View.VISIBLE);
            exercisesSection.setVisibility(View.GONE);
        } else {
            // Fase Exercícios / Diagnóstico: separada do player de vídeo, mas com Anotações acessíveis
            videoSection.setVisibility(View.GONE);
            sidePanelToggleBar.setVisibility(View.VISIBLE);
            aulaBottomBanner.setVisibility(View.GONE);

            boolean showQ = "questoes".equals(activeSubTab);
            tabShowExercises.setBackgroundResource(showQ ? R.drawable.bg_card : 0);
            tabShowExercises.setTextColor(showQ ? ink : muted);
            tabShowNotes.setBackgroundResource(!showQ ? R.drawable.bg_card : 0);
            tabShowNotes.setTextColor(!showQ ? ink : muted);

            exercisesSection.setVisibility(showQ ? View.VISIBLE : View.GONE);
            notesSection.setVisibility(!showQ ? View.VISIBLE : View.GONE);
        }
    }

    // --- VIDEO PLAYER (YouTube default_video_url ou custom_video_url) ---
    private void renderVideoPlayer(boolean reloadWebView) {
        if (currentTopic == null || currentProgress == null) return;

        boolean hasCustom = currentProgress.customVideoUrl != null
                && !currentProgress.customVideoUrl.trim().isEmpty();
        String effectiveUrl = hasCustom ? currentProgress.customVideoUrl : currentTopic.defaultVideoUrl;

        edtVideoUrl.setText(effectiveUrl);
        txtVideoError.setVisibility(View.GONE);
        txtVideoEmptyHint.setText("Cole o link do YouTube para a aula de “" + currentTopic.title + "”.");
        btnRestoreDefaultVideo.setVisibility(hasCustom ? View.VISIBLE : View.GONE);

        VideoEmbedHelper.Embed embed = VideoEmbedHelper.toEmbed(effectiveUrl);
        if (embed != null) {
            videoEmptyState.setVisibility(View.GONE);
            webViewVideo.setVisibility(View.VISIBLE);
            videoControlBar.setVisibility(View.VISIBLE);
            videoFormBar.setVisibility(isEditingVideo ? View.VISIBLE : View.GONE);
            txtVideoProvider.setText((hasCustom ? "Link personalizado" : "Aula sugerida") + " · " + embed.provider);

            if (reloadWebView || !embed.src.equals(lastLoadedVideoSrc)) {
                lastLoadedVideoSrc = embed.src;
                String html = VideoEmbedHelper.buildIframeHtml(embed);
                webViewVideo.loadDataWithBaseURL(
                        "https://www.youtube-nocookie.com", html, "text/html", "UTF-8", null);
            }
        } else {
            webViewVideo.setVisibility(View.GONE);
            videoControlBar.setVisibility(View.GONE);
            videoEmptyState.setVisibility(View.VISIBLE);
            videoFormBar.setVisibility(View.VISIBLE);
        }
    }

    private void submitVideoUrl() {
        if (currentTopic == null) return;
        String draft = edtVideoUrl.getText().toString().trim();
        if (draft.isEmpty()) return;

        VideoEmbedHelper.Embed embed = VideoEmbedHelper.toEmbed(draft);
        if (embed == null) {
            txtVideoError.setText("Link inválido. Informe uma URL válida do YouTube (youtube.com ou youtu.be).");
            txtVideoError.setVisibility(View.VISIBLE);
            return;
        }
        repo.saveCustomVideoUrl(userId, currentTopic.id, draft);
        currentProgress.customVideoUrl = draft;
        isEditingVideo = false;
        lastLoadedVideoSrc = null;
        renderVideoPlayer(true);
        Toast.makeText(requireContext(), "Link da videoaula salvo!", Toast.LENGTH_SHORT).show();
    }

    // --- EDITOR DE ANOTAÇÕES (SQLite em tempo real) ---
    private void setupNotesToolbar(View v) {
        edtNotes.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (suppressNotesWatcher || currentTopic == null) return;
                String text = s.toString();
                currentProgress.notes = text;
                repo.saveTopicNotes(userId, currentTopic.id, text);
                updateWordCount(text);
                String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
                txtSavedStatus.setText("✓ Salvo às " + time);
            }

            @Override public void afterTextChanged(Editable s) {}
        });

        v.findViewById(R.id.btnFmtH2).setOnClickListener(view -> insertPrefix("\n## "));
        v.findViewById(R.id.btnFmtBullet).setOnClickListener(view -> insertPrefix("\n• "));
        v.findViewById(R.id.btnFmtQuote).setOnClickListener(view -> insertPrefix("\n❝ "));
        v.findViewById(R.id.btnFmtClear).setOnClickListener(view -> edtNotes.setText(""));
    }

    private void insertPrefix(String prefix) {
        int pos = Math.max(0, edtNotes.getSelectionStart());
        edtNotes.getText().insert(pos, prefix);
    }

    private void updateWordCount(String text) {
        String trimmed = text != null ? text.trim() : "";
        int words = trimmed.isEmpty() ? 0 : trimmed.split("\\s+").length;
        txtWordCount.setText(words + (words == 1 ? " palavra" : " palavras"));
    }

    // --- EXERCÍCIOS E DIAGNÓSTICO ---
    private void renderExercises() {
        exercisesSection.removeAllViews();
        if (currentTopic == null) return;

        boolean review = currentProgress != null && currentProgress.completed;
        int total = currentQuestions.size();
        Map<String, Integer> answers = repo.getAnswersForTopic(userId, currentTopic.id);

        int answered = 0;
        int correct = 0;
        for (Question q : currentQuestions) {
            Integer chosen = answers.get(q.id);
            if (chosen != null) {
                answered++;
                if (chosen == q.correctIndex) correct++;
            }
        }
        int pct = total > 0 ? Math.round((correct * 100f) / total) : 0;

        // Banner superior
        LinearLayout topBanner = new LinearLayout(requireContext());
        topBanner.setOrientation(LinearLayout.VERTICAL);
        topBanner.setPadding(dp(18), dp(14), dp(18), dp(14));
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        blp.setMargins(0, 0, 0, dp(12));
        topBanner.setLayoutParams(blp);
        topBanner.setBackgroundResource(R.drawable.bg_card_dark);

        if (review) {
            String diagLabel = pct == 100 ? "Gabaritou!" : (pct >= 60 ? "Bom resultado" : "Vale revisar a aula");
            TextView tvDiag = new TextView(requireContext());
            tvDiag.setText("DIAGNÓSTICO · " + pct + "% (" + diagLabel + ")");
            tvDiag.setTextColor(getColorRes(R.color.lime));
            tvDiag.setTextSize(15f);
            tvDiag.setTypeface(null, Typeface.BOLD);

            TextView tvStats = new TextView(requireContext());
            tvStats.setText("✓ " + correct + " certas (+" + (correct * 10) + " pts)  ·  ✕ "
                    + (total - correct) + " erradas  ·  +30 pts conclusão");
            tvStats.setTextColor(Color.WHITE);
            tvStats.setTextSize(12f);
            tvStats.setPadding(0, dp(4), 0, 0);

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

        // Lista de questões
        for (int qi = 0; qi < currentQuestions.size(); qi++) {
            Question q = currentQuestions.get(qi);
            Integer chosen = answers.get(q.id);
            boolean ok = chosen != null && chosen == q.correctIndex;

            LinearLayout qCard = new LinearLayout(requireContext());
            qCard.setOrientation(LinearLayout.VERTICAL);
            qCard.setPadding(dp(18), dp(16), dp(18), dp(16));
            LinearLayout.LayoutParams qlp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            qlp.setMargins(0, 0, 0, dp(12));
            qCard.setLayoutParams(qlp);
            qCard.setBackgroundResource(R.drawable.bg_card);

            String prefix = review ? (ok ? "✓ " : "✕ ") : String.format(Locale.US, "%02d. ", qi + 1);
            TextView tvPrompt = new TextView(requireContext());
            tvPrompt.setText(prefix + q.prompt);
            tvPrompt.setTextSize(15f);
            tvPrompt.setTypeface(null, Typeface.BOLD);
            tvPrompt.setTextColor(getColorRes(R.color.ink));
            qCard.addView(tvPrompt);

            for (int i = 0; i < q.options.size(); i++) {
                final int optIdx = i;
                boolean isAnswer = i == q.correctIndex;
                boolean isChosen = chosen != null && i == chosen;

                TextView btnOpt = new TextView(requireContext());
                LinearLayout.LayoutParams olp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                olp.setMargins(0, dp(8), 0, 0);
                btnOpt.setLayoutParams(olp);
                btnOpt.setPadding(dp(14), dp(11), dp(14), dp(11));

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
                        btnOpt.setTextColor(getColorRes(R.color.success_dark));
                        btnOpt.setTypeface(null, Typeface.BOLD);
                    } else if (isChosen) {
                        btnOpt.setBackgroundResource(R.drawable.bg_button_accent);
                        btnOpt.setTextColor(Color.WHITE);
                    } else {
                        btnOpt.setBackgroundResource(R.drawable.bg_input);
                        btnOpt.setTextColor(getColorRes(R.color.muted));
                    }
                } else {
                    if (isChosen) {
                        btnOpt.setBackgroundResource(R.drawable.bg_card_dark);
                        btnOpt.setTextColor(Color.WHITE);
                        btnOpt.setTypeface(null, Typeface.BOLD);
                    } else {
                        btnOpt.setBackgroundResource(R.drawable.bg_input);
                        btnOpt.setTextColor(getColorRes(R.color.ink));
                    }
                    btnOpt.setOnClickListener(v -> {
                        repo.saveQuestionAnswer(userId, q.id, optIdx);
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
                elp.setMargins(0, dp(10), 0, 0);
                tvExp.setLayoutParams(elp);
                tvExp.setPadding(dp(12), dp(10), dp(12), dp(10));
                tvExp.setBackgroundResource(R.drawable.bg_input);
                tvExp.setText("💡 Justificativa · alternativa " + LETTERS[q.correctIndex] + ". " + q.explanation);
                tvExp.setTextSize(13f);
                tvExp.setTextColor(getColorRes(R.color.ink));
                qCard.addView(tvExp);
            }

            exercisesSection.addView(qCard);
        }

        // Botão inferior de conclusão
        if (!review) {
            boolean all = !currentQuestions.isEmpty() && answered == total;
            MaterialButton btnFinishBottom = new MaterialButton(requireContext());
            btnFinishBottom.setText(all
                    ? "✓ Tudo respondido! Marcar tópico como concluído"
                    : answered + " de " + total + " respondidas (responda todas para concluir)");
            btnFinishBottom.setAllCaps(false);
            btnFinishBottom.setEnabled(all);
            btnFinishBottom.setAlpha(all ? 1f : 0.4f);
            btnFinishBottom.setBackgroundTintList(ColorStateList.valueOf(getColorRes(R.color.ink)));
            btnFinishBottom.setTextColor(Color.WHITE);
            btnFinishBottom.setOnClickListener(v -> completeCurrentTopic());
            exercisesSection.addView(btnFinishBottom);
        }
    }

    private int getColorRes(int res) {
        return getResources().getColor(res, requireContext().getTheme());
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
