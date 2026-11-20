package com.example.focovest.ui.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.focovest.R;
import com.example.focovest.data.DataRepository.SubjectId;
import com.example.focovest.data.ExtrasRepository;
import com.example.focovest.data.ExtrasStoreManager;
import com.example.focovest.model.BookItem;
import com.example.focovest.model.FlashcardItem;
import com.example.focovest.model.PastExam;
import com.example.focovest.model.ReadingText;
import com.example.focovest.ui.dialog.FlashcardViewerDialogFragment;

public class MaterialsHubFragment extends Fragment {

    private ExtrasStoreManager extras;
    private LinearLayout root;

    // Abas: "flashcards" | "textos" | "livros" | "provas"
    private String activeTab = "flashcards";
    private SubjectId subjectFilter = null;
    private ReadingText openText = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_scroll_container, container, false);
        root = v.findViewById(R.id.rootContainer);
        extras = ExtrasStoreManager.getInstance(requireContext());

        getParentFragmentManager().setFragmentResultListener(
                FlashcardViewerDialogFragment.REQUEST_KEY,
                getViewLifecycleOwner(),
                (requestKey, bundle) -> render()
        );

        render();
        return v;
    }

    private void render() {
        root.removeAllViews();

        if (openText != null) {
            renderTextReader(openText);
            return;
        }

        TextView eyebrow = new TextView(requireContext());
        eyebrow.setText("BIBLIOTECA DE MATERIAIS");
        eyebrow.setTextColor(getColorRes(R.color.accent));
        eyebrow.setTextSize(11f);
        eyebrow.setTypeface(null, Typeface.BOLD);
        root.addView(eyebrow);

        TextView title = new TextView(requireContext());
        title.setText("Flashcards, textos, livros e provas.");
        title.setTextColor(getColorRes(R.color.ink));
        title.setTextSize(25f);
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(0, 4, 0, 14);
        root.addView(title);

        // Abas principais
        LinearLayout tabBar = new LinearLayout(requireContext());
        tabBar.setOrientation(LinearLayout.HORIZONTAL);
        tabBar.setBackgroundResource(R.drawable.bg_input);
        tabBar.setPadding(6, 6, 6, 6);
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tlp.setMargins(0, 0, 0, 14);
        tabBar.setLayoutParams(tlp);

        tabBar.addView(createTab("Flashcards", "flashcards"));
        tabBar.addView(createTab("Textos", "textos"));
        tabBar.addView(createTab("Livros", "livros"));
        tabBar.addView(createTab("Provas", "provas"));
        root.addView(tabBar);

        if (!"provas".equals(activeTab)) {
            renderSubjectFilterBar();
        }

        switch (activeTab) {
            case "textos":
                renderReadingTexts();
                break;
            case "livros":
                renderBooks();
                break;
            case "provas":
                renderPastExams();
                break;
            default:
                renderFlashcards();
                break;
        }
    }

    private void renderSubjectFilterBar() {
        HorizontalScrollView hsv = new HorizontalScrollView(requireContext());
        hsv.setScrollBarSize(0);
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 0, 0, 14);

        row.addView(createSubjectChip("Todas", null));
        for (SubjectId sub : SubjectId.values()) {
            row.addView(createSubjectChip(sub.name, sub));
        }
        hsv.addView(row);
        root.addView(hsv);
    }

    private void renderFlashcards() {
        int total = ExtrasRepository.FLASHCARDS.size();
        int mastered = extras.getState().masteredFlashcards.size();

        LinearLayout summary = createCard(true);
        TextView tvSum = new TextView(requireContext());
        tvSum.setText("REVISÃO ATIVA · " + mastered + "/" + total + " FLASHCARDS DOMINADOS");
        tvSum.setTextColor(getColorRes(R.color.lime));
        tvSum.setTextSize(12f);
        tvSum.setTypeface(null, Typeface.BOLD);
        summary.addView(tvSum);

        TextView tvHint = new TextView(requireContext());
        tvHint.setText("Toque em qualquer card abaixo para testar sua memória e virar a resposta.");
        tvHint.setTextColor(Color.parseColor("#CCFFFFFF"));
        tvHint.setTextSize(13f);
        tvHint.setPadding(0, 6, 0, 0);
        summary.addView(tvHint);
        root.addView(summary);

        boolean any = false;
        for (FlashcardItem fc : ExtrasRepository.FLASHCARDS) {
            if (subjectFilter != null && fc.subject != subjectFilter) continue;
            any = true;
            boolean isMastered = extras.isFlashcardMastered(fc.id);

            LinearLayout card = createCard(false);
            TextView tvSub = new TextView(requireContext());
            tvSub.setText((isMastered ? "✓ DOMINADO · " : "") + fc.subject.name.toUpperCase());
            tvSub.setTextColor(isMastered ? getColorRes(R.color.success_dark) : fc.subject.getColorInt());
            tvSub.setTextSize(11f);
            tvSub.setTypeface(null, Typeface.BOLD);
            card.addView(tvSub);

            TextView tvTitle = new TextView(requireContext());
            tvTitle.setText(fc.frontTitle);
            tvTitle.setTextColor(getColorRes(R.color.ink));
            tvTitle.setTextSize(17f);
            tvTitle.setTypeface(null, Typeface.BOLD);
            tvTitle.setPadding(0, 6, 0, 4);
            card.addView(tvTitle);

            TextView tvQ = new TextView(requireContext());
            tvQ.setText(fc.frontQuestion);
            tvQ.setTextColor(getColorRes(R.color.muted));
            tvQ.setTextSize(13f);
            card.addView(tvQ);

            card.setOnClickListener(v ->
                    FlashcardViewerDialogFragment.newInstance(fc.id)
                            .show(getParentFragmentManager(), "flashcard_viewer"));
            root.addView(card);
        }

        if (!any) addEmptyCard("Nenhum flashcard cadastrado para esta disciplina.");
    }

    private void renderReadingTexts() {
        boolean any = false;
        for (ReadingText rt : ExtrasRepository.READING_TEXTS) {
            if (subjectFilter != null && rt.subject != subjectFilter) continue;
            any = true;
            boolean read = extras.getState().readTextIds.contains(rt.id);

            LinearLayout card = createCard(false);
            TextView tvMeta = new TextView(requireContext());
            tvMeta.setText((read ? "✓ LIDO · " : "") + rt.subject.name + " · " + rt.readMinutes + " min · " + rt.author);
            tvMeta.setTextColor(rt.subject.getColorInt());
            tvMeta.setTextSize(11f);
            tvMeta.setTypeface(null, Typeface.BOLD);
            card.addView(tvMeta);

            TextView tvTitle = new TextView(requireContext());
            tvTitle.setText(rt.title);
            tvTitle.setTextColor(getColorRes(R.color.ink));
            tvTitle.setTextSize(17f);
            tvTitle.setTypeface(null, Typeface.BOLD);
            tvTitle.setPadding(0, 6, 0, 8);
            card.addView(tvTitle);

            TextView tvAction = new TextView(requireContext());
            tvAction.setText("Ler resumo completo →");
            tvAction.setTextColor(getColorRes(R.color.accent));
            tvAction.setTextSize(13f);
            tvAction.setTypeface(null, Typeface.BOLD);
            card.addView(tvAction);

            card.setOnClickListener(v -> {
                extras.markTextRead(rt.id);
                extras.recordAccess("texto", rt.id, rt.title, rt.subject);
                openText = rt;
                render();
            });
            root.addView(card);
        }
        if (!any) addEmptyCard("Nenhum texto de apoio cadastrado para esta disciplina.");
    }

    private void renderTextReader(ReadingText rt) {
        Button btnBack = new Button(requireContext());
        btnBack.setText("← Voltar para textos");
        btnBack.setAllCaps(false);
        btnBack.setOnClickListener(v -> {
            openText = null;
            render();
        });
        root.addView(btnBack);

        LinearLayout card = createCard(false);
        TextView tvSub = new TextView(requireContext());
        tvSub.setText(rt.subject.name.toUpperCase() + " · " + rt.author + " · ~" + rt.readMinutes + " min");
        tvSub.setTextColor(rt.subject.getColorInt());
        tvSub.setTextSize(11f);
        tvSub.setTypeface(null, Typeface.BOLD);
        card.addView(tvSub);

        TextView tvTitle = new TextView(requireContext());
        tvTitle.setText(rt.title);
        tvTitle.setTextColor(getColorRes(R.color.ink));
        tvTitle.setTextSize(22f);
        tvTitle.setTypeface(null, Typeface.BOLD);
        tvTitle.setPadding(0, 8, 0, 14);
        card.addView(tvTitle);

        TextView tvBody = new TextView(requireContext());
        tvBody.setText(rt.body);
        tvBody.setTextColor(getColorRes(R.color.ink));
        tvBody.setTextSize(15f);
        tvBody.setLineSpacing(8f, 1.0f);
        card.addView(tvBody);

        root.addView(card);
    }

    private void renderBooks() {
        boolean any = false;
        for (BookItem b : ExtrasRepository.BOOKS) {
            if (subjectFilter != null && b.subject != subjectFilter) continue;
            any = true;

            LinearLayout card = createCard(false);
            TextView tvTag = new TextView(requireContext());
            tvTag.setText(b.exam.id + " · " + b.subject.name);
            tvTag.setTextColor(b.subject.getColorInt());
            tvTag.setTextSize(11f);
            tvTag.setTypeface(null, Typeface.BOLD);
            card.addView(tvTag);

            TextView tvTitle = new TextView(requireContext());
            tvTitle.setText(b.title);
            tvTitle.setTextColor(getColorRes(R.color.ink));
            tvTitle.setTextSize(18f);
            tvTitle.setTypeface(null, Typeface.BOLD);
            tvTitle.setPadding(0, 6, 0, 2);
            card.addView(tvTitle);

            TextView tvAuthor = new TextView(requireContext());
            tvAuthor.setText("Por " + b.author);
            tvAuthor.setTextColor(getColorRes(R.color.muted));
            tvAuthor.setTextSize(12f);
            tvAuthor.setPadding(0, 0, 0, 8);
            card.addView(tvAuthor);

            TextView tvSyn = new TextView(requireContext());
            tvSyn.setText(b.synopsis);
            tvSyn.setTextColor(getColorRes(R.color.ink));
            tvSyn.setTextSize(13f);
            card.addView(tvSyn);

            Button btnOpen = new Button(requireContext());
            btnOpen.setText("Pesquisar obra e análise ↗");
            btnOpen.setAllCaps(false);
            btnOpen.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getColorRes(R.color.ink)));
            btnOpen.setTextColor(Color.WHITE);
            btnOpen.setOnClickListener(v -> {
                extras.recordAccess("livro", b.id, b.title, b.subject);
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(b.referenceUrl)));
            });
            card.addView(btnOpen);

            root.addView(card);
        }
        if (!any) addEmptyCard("Nenhum livro recomendado para esta disciplina.");
    }

    private void renderPastExams() {
        for (PastExam pe : ExtrasRepository.PAST_EXAMS) {
            LinearLayout card = createCard(false);

            TextView tvBadge = new TextView(requireContext());
            tvBadge.setText(pe.exam.id + " (" + pe.exam.institution + ") · " + pe.year);
            tvBadge.setTextColor(getColorRes(R.color.accent));
            tvBadge.setTextSize(12f);
            tvBadge.setTypeface(null, Typeface.BOLD);
            card.addView(tvBadge);

            TextView tvStage = new TextView(requireContext());
            tvStage.setText(pe.stageLabel);
            tvStage.setTextColor(getColorRes(R.color.ink));
            tvStage.setTextSize(18f);
            tvStage.setTypeface(null, Typeface.BOLD);
            tvStage.setPadding(0, 4, 0, 4);
            card.addView(tvStage);

            TextView tvDesc = new TextView(requireContext());
            tvDesc.setText(pe.description);
            tvDesc.setTextColor(getColorRes(R.color.muted));
            tvDesc.setTextSize(13f);
            tvDesc.setPadding(0, 0, 0, 12);
            card.addView(tvDesc);

            Button btnPdf = new Button(requireContext());
            btnPdf.setText("Acessar cadernos e gabaritos oficiais ↗");
            btnPdf.setAllCaps(false);
            btnPdf.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getColorRes(R.color.ink)));
            btnPdf.setTextColor(Color.WHITE);
            btnPdf.setOnClickListener(v -> {
                extras.recordAccess("prova", pe.id, pe.exam.id + " " + pe.year + " · " + pe.stageLabel, null);
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(pe.pdfUrl)));
            });
            card.addView(btnPdf);

            root.addView(card);
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

    private TextView createSubjectChip(String label, SubjectId sub) {
        boolean active = (sub == null && subjectFilter == null) || (sub != null && sub == subjectFilter);
        TextView tv = new TextView(requireContext());
        tv.setText(label);
        tv.setPadding(28, 14, 28, 14);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 10, 0);
        tv.setLayoutParams(lp);
        tv.setBackgroundResource(active ? R.drawable.bg_card_dark : R.drawable.bg_card);
        tv.setTextColor(active ? Color.WHITE : getColorRes(R.color.ink));
        tv.setTextSize(12f);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setOnClickListener(v -> {
            subjectFilter = sub;
            render();
        });
        return tv;
    }

    private void addEmptyCard(String msg) {
        LinearLayout card = createCard(false);
        TextView tv = new TextView(requireContext());
        tv.setText(msg);
        tv.setTextColor(getColorRes(R.color.muted));
        card.addView(tv);
        root.addView(card);
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
