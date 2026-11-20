package com.example.focovest.ui.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.focovest.R;
import com.example.focovest.data.ExtrasRepository;
import com.example.focovest.data.ExtrasStoreManager;
import com.example.focovest.model.FlashcardItem;

public class FlashcardViewerDialogFragment extends DialogFragment {

    public static final String REQUEST_KEY = "flashcard_updated";
    private static final String ARG_FLASHCARD_ID = "flashcard_id";

    private boolean showingBack = false;
    private FlashcardItem item;

    public FlashcardViewerDialogFragment() {
        // Construtor padrão obrigatório para DialogFragment
    }

    public static FlashcardViewerDialogFragment newInstance(String flashcardId) {
        FlashcardViewerDialogFragment fragment = new FlashcardViewerDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FLASHCARD_ID, flashcardId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_flashcard_viewer, container, false);

        String id = getArguments() != null ? getArguments().getString(ARG_FLASHCARD_ID) : null;
        for (FlashcardItem fc : ExtrasRepository.FLASHCARDS) {
            if (fc.id.equals(id)) {
                item = fc;
                break;
            }
        }
        if (item == null && !ExtrasRepository.FLASHCARDS.isEmpty()) {
            item = ExtrasRepository.FLASHCARDS.get(0);
        }

        TextView txtSideBadge = v.findViewById(R.id.txtFlashcardSideBadge);
        TextView txtTitle = v.findViewById(R.id.txtFlashcardTitle);
        TextView txtBody = v.findViewById(R.id.txtFlashcardBody);
        TextView txtTip = v.findViewById(R.id.txtFlashcardTip);
        Button btnFlip = v.findViewById(R.id.btnFlipFlashcard);
        Button btnMastered = v.findViewById(R.id.btnToggleMastered);

        ExtrasStoreManager extras = ExtrasStoreManager.getInstance(requireContext());
        if (item != null) {
            extras.recordAccess("flashcard", item.id, item.frontTitle, item.subject);
        }

        Runnable updateCard = () -> {
            if (item == null) return;
            txtTitle.setText(item.subject.name + " · " + item.frontTitle);
            if (!showingBack) {
                txtSideBadge.setText("FRENTE · PERGUNTA");
                txtBody.setText(item.frontQuestion);
                txtTip.setVisibility(View.GONE);
                btnFlip.setText("Ver resposta ↻");
            } else {
                txtSideBadge.setText("VERSO · RESPOSTA");
                txtBody.setText(item.backAnswer);
                if (item.tip != null && !item.tip.isEmpty()) {
                    txtTip.setText("💡 " + item.tip);
                    txtTip.setVisibility(View.VISIBLE);
                } else {
                    txtTip.setVisibility(View.GONE);
                }
                btnFlip.setText("Voltar para pergunta ↻");
            }

            boolean mastered = extras.isFlashcardMastered(item.id);
            btnMastered.setText(mastered ? "✓ Dominado (tocar para revisar)" : "✓ Marcar como dominado");
        };

        v.findViewById(R.id.btnCloseFlashcard).setOnClickListener(view -> dismiss());

        btnFlip.setOnClickListener(view -> {
            showingBack = !showingBack;
            updateCard.run();
        });

        btnMastered.setOnClickListener(view -> {
            if (item == null) return;
            extras.toggleFlashcardMastered(item.id);
            getParentFragmentManager().setFragmentResult(REQUEST_KEY, new Bundle());
            updateCard.run();
        });

        updateCard.run();
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }
}
