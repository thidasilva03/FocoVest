package com.example.focovest.ui.fragment;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.focovest.R;
import com.example.focovest.data.DataRepository.ExamId;
import com.example.focovest.data.ExtrasStoreManager;
import com.example.focovest.data.StoreManager;
import com.example.focovest.domain.PlanEngine;
import com.example.focovest.model.ForumQuestion;
import com.example.focovest.model.ForumReply;
import com.example.focovest.ui.dialog.NewForumQuestionDialogFragment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class ForumFragment extends Fragment {

    private StoreManager store;
    private ExtrasStoreManager extras;
    private LinearLayout root;

    // Abas: "recentes" | "vestibular" | "minhas"
    private String activeTab = "recentes";
    private ExamId selectedExamFilter = null;
    private String searchQuery = "";
    private String expandedQuestionId = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_scroll_container, container, false);
        root = v.findViewById(R.id.rootContainer);
        store = StoreManager.getInstance(requireContext());
        extras = ExtrasStoreManager.getInstance(requireContext());

        getParentFragmentManager().setFragmentResultListener(
                NewForumQuestionDialogFragment.REQUEST_KEY,
                getViewLifecycleOwner(),
                (requestKey, bundle) -> render()
        );

        render();
        return v;
    }

    private void render() {
        root.removeAllViews();

        TextView eyebrow = new TextView(requireContext());
        eyebrow.setText("FÓRUM DE DÚVIDAS");
        eyebrow.setTextColor(getColorRes(R.color.accent));
        eyebrow.setTextSize(11f);
        eyebrow.setTypeface(null, Typeface.BOLD);
        root.addView(eyebrow);

        TextView title = new TextView(requireContext());
        title.setText("Pergunte e aprenda junto.");
        title.setTextColor(getColorRes(R.color.ink));
        title.setTextSize(26f);
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(0, 4, 0, 14);
        root.addView(title);

        // Botão nova pergunta
        Button btnNew = new Button(requireContext());
        btnNew.setText("+ Fazer uma pergunta");
        btnNew.setAllCaps(false);
        btnNew.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getColorRes(R.color.accent)));
        btnNew.setTextColor(Color.WHITE);
        btnNew.setOnClickListener(v ->
                NewForumQuestionDialogFragment.newInstance()
                        .show(getParentFragmentManager(), "new_forum_question"));
        root.addView(btnNew);

        // Barra de abas
        LinearLayout tabBar = new LinearLayout(requireContext());
        tabBar.setOrientation(LinearLayout.HORIZONTAL);
        tabBar.setBackgroundResource(R.drawable.bg_input);
        tabBar.setPadding(8, 8, 8, 8);
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tlp.setMargins(0, 16, 0, 12);
        tabBar.setLayoutParams(tlp);

        tabBar.addView(createTabButton("Recentes", "recentes"));
        tabBar.addView(createTabButton("Por vestibular", "vestibular"));
        tabBar.addView(createTabButton("Minhas dúvidas", "minhas"));
        root.addView(tabBar);

        // Campo de busca
        EditText edtSearch = new EditText(requireContext());
        edtSearch.setHint("Buscar por assunto, disciplina ou palavra-chave...");
        edtSearch.setText(searchQuery);
        edtSearch.setSelection(searchQuery.length());
        edtSearch.setBackgroundResource(R.drawable.bg_card);
        edtSearch.setPadding(32, 24, 32, 24);
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        slp.setMargins(0, 0, 0, 14);
        edtSearch.setLayoutParams(slp);
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString();
                renderQuestionsList();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
        root.addView(edtSearch);

        // Filtro por vestibular (quando na aba "vestibular")
        if ("vestibular".equals(activeTab)) {
            HorizontalScrollView hsv = new HorizontalScrollView(requireContext());
            hsv.setScrollBarSize(0);
            LinearLayout chipRow = new LinearLayout(requireContext());
            chipRow.setOrientation(LinearLayout.HORIZONTAL);
            chipRow.setPadding(0, 0, 0, 14);

            chipRow.addView(createExamChip("Todos", null));
            for (ExamId ex : ExamId.values()) {
                chipRow.addView(createExamChip(ex.id, ex));
            }
            hsv.addView(chipRow);
            root.addView(hsv);
        }

        // Container dinâmico para a lista (permite filtrar sem recriar o EditText de busca)
        LinearLayout listHolder = new LinearLayout(requireContext());
        listHolder.setTag("forum_list_holder");
        listHolder.setOrientation(LinearLayout.VERTICAL);
        root.addView(listHolder);

        renderQuestionsList();
    }

    private void renderQuestionsList() {
        LinearLayout holder = root.findViewWithTag("forum_list_holder");
        if (holder == null) return;
        holder.removeAllViews();

        List<ForumQuestion> filtered = getFilteredQuestions();
        if (filtered.isEmpty()) {
            LinearLayout empty = createCard();
            TextView tv = new TextView(requireContext());
            tv.setText("minhas".equals(activeTab)
                    ? "Você ainda não publicou nenhuma dúvida. Toque em '+ Fazer uma pergunta' para começar."
                    : "Nenhuma pergunta encontrada para este filtro.");
            tv.setTextColor(getColorRes(R.color.muted));
            empty.addView(tv);
            holder.addView(empty);
            return;
        }

        for (ForumQuestion q : filtered) {
            holder.addView(buildQuestionCard(q));
        }
    }

    private List<ForumQuestion> getFilteredQuestions() {
        List<ForumQuestion> result = new ArrayList<>();
        String qLower = searchQuery.trim().toLowerCase(Locale.ROOT);

        for (ForumQuestion q : extras.getState().forumQuestions) {
            if ("minhas".equals(activeTab) && !q.mine) continue;
            if ("vestibular".equals(activeTab) && selectedExamFilter != null && q.exam != selectedExamFilter) {
                continue;
            }
            if (!qLower.isEmpty()) {
                String hay = (q.title + " " + q.body + " " + (q.subject != null ? q.subject.name : "")
                        + " " + (q.exam != null ? q.exam.id : "")).toLowerCase(Locale.ROOT);
                if (!hay.contains(qLower)) continue;
            }
            result.add(q);
        }
        return result;
    }

    private LinearLayout buildQuestionCard(ForumQuestion q) {
        LinearLayout card = createCard();

        TextView meta = new TextView(requireContext());
        String examStr = q.exam != null ? q.exam.id : "Geral";
        String subStr = q.subject != null ? q.subject.name : "Geral";
        meta.setText(examStr + " · " + subStr + " · " + q.authorName + " (" + q.authorCity + ")");
        meta.setTextColor(q.subject != null ? q.subject.getColorInt() : getColorRes(R.color.accent));
        meta.setTextSize(11f);
        meta.setTypeface(null, Typeface.BOLD);
        card.addView(meta);

        TextView tvTitle = new TextView(requireContext());
        tvTitle.setText(q.title);
        tvTitle.setTextColor(getColorRes(R.color.ink));
        tvTitle.setTextSize(16f);
        tvTitle.setTypeface(null, Typeface.BOLD);
        tvTitle.setPadding(0, 6, 0, 4);
        card.addView(tvTitle);

        TextView tvBody = new TextView(requireContext());
        tvBody.setText(q.body);
        tvBody.setTextColor(getColorRes(R.color.muted));
        tvBody.setTextSize(13f);
        card.addView(tvBody);

        boolean expanded = q.id.equals(expandedQuestionId);
        int replyCount = q.replies != null ? q.replies.size() : 0;

        TextView btnToggle = new TextView(requireContext());
        btnToggle.setText(expanded
                ? "▲ Ocultar respostas (" + replyCount + ")"
                : "💬 Ver respostas e responder (" + replyCount + ")");
        btnToggle.setTextColor(getColorRes(R.color.accent));
        btnToggle.setTextSize(12f);
        btnToggle.setTypeface(null, Typeface.BOLD);
        btnToggle.setPadding(0, 14, 0, 0);
        btnToggle.setOnClickListener(v -> {
            expandedQuestionId = expanded ? null : q.id;
            renderQuestionsList();
        });
        card.addView(btnToggle);

        if (expanded) {
            if (q.replies != null) {
                for (ForumReply r : q.replies) {
                    LinearLayout replyBox = new LinearLayout(requireContext());
                    replyBox.setOrientation(LinearLayout.VERTICAL);
                    replyBox.setPadding(24, 16, 24, 16);
                    replyBox.setBackgroundResource(r.isProfessor ? R.drawable.bg_pill_success : R.drawable.bg_input);
                    LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    rlp.setMargins(0, 12, 0, 0);
                    replyBox.setLayoutParams(rlp);

                    TextView tvAuthor = new TextView(requireContext());
                    tvAuthor.setText((r.isProfessor ? "🎓 " : "") + r.authorName + " · " + r.dateKey);
                    tvAuthor.setTextSize(11f);
                    tvAuthor.setTypeface(null, Typeface.BOLD);
                    tvAuthor.setTextColor(getColorRes(r.isProfessor ? R.color.success_dark : R.color.ink));

                    TextView tvReplyBody = new TextView(requireContext());
                    tvReplyBody.setText(r.body);
                    tvReplyBody.setTextSize(13f);
                    tvReplyBody.setTextColor(getColorRes(R.color.ink));
                    tvReplyBody.setPadding(0, 4, 0, 0);

                    replyBox.addView(tvAuthor);
                    replyBox.addView(tvReplyBody);
                    card.addView(replyBox);
                }
            }

            // Campo para responder
            EditText edtReply = new EditText(requireContext());
            edtReply.setHint("Escreva sua resposta para ajudar...");
            edtReply.setBackgroundResource(R.drawable.bg_input);
            edtReply.setPadding(24, 18, 24, 18);
            edtReply.setTextSize(13f);
            LinearLayout.LayoutParams elp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            elp.setMargins(0, 14, 0, 8);
            edtReply.setLayoutParams(elp);
            card.addView(edtReply);

            Button btnSendReply = new Button(requireContext());
            btnSendReply.setText("Enviar resposta");
            btnSendReply.setAllCaps(false);
            btnSendReply.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getColorRes(R.color.ink)));
            btnSendReply.setTextColor(Color.WHITE);
            btnSendReply.setOnClickListener(v -> {
                String text = edtReply.getText().toString().trim();
                if (text.length() < 3) {
                    Toast.makeText(requireContext(), "Escreva uma resposta antes de enviar.", Toast.LENGTH_SHORT).show();
                    return;
                }
                StoreManager.State st = store.getState();
                String author = (st.name != null && !st.name.trim().isEmpty()) ? st.name.trim() : "Estudante";
                ForumReply reply = new ForumReply(
                        "fr-" + UUID.randomUUID().toString().substring(0, 8),
                        author,
                        st.organizerView,
                        text,
                        PlanEngine.dayKey(new Date())
                );
                extras.addForumReply(q.id, reply);
                renderQuestionsList();
            });
            card.addView(btnSendReply);
        }

        return card;
    }

    private TextView createTabButton(String label, String tabId) {
        boolean active = tabId.equals(activeTab);
        TextView tv = new TextView(requireContext());
        tv.setLayoutParams(new LinearLayout.LayoutParams(0, 86, 1f));
        tv.setGravity(android.view.Gravity.CENTER);
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

    private TextView createExamChip(String label, ExamId exam) {
        boolean active = (exam == null && selectedExamFilter == null)
                || (exam != null && exam == selectedExamFilter);
        TextView tv = new TextView(requireContext());
        tv.setText(label);
        tv.setPadding(30, 14, 30, 14);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 10, 0);
        tv.setLayoutParams(lp);
        tv.setBackgroundResource(active ? R.drawable.bg_card_dark : R.drawable.bg_card);
        tv.setTextColor(active ? Color.WHITE : getColorRes(R.color.ink));
        tv.setTextSize(12f);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setOnClickListener(v -> {
            selectedExamFilter = exam;
            render();
        });
        return tv;
    }

    private LinearLayout createCard() {
        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(34, 28, 34, 28);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 16);
        card.setLayoutParams(lp);
        card.setBackgroundResource(R.drawable.bg_card);
        return card;
    }

    private int getColorRes(int res) {
        return getResources().getColor(res, requireContext().getTheme());
    }
}
