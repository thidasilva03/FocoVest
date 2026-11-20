package com.example.focovest.ui.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.focovest.R;
import com.example.focovest.data.DataRepository.ExamId;
import com.example.focovest.data.DataRepository.SubjectId;
import com.example.focovest.data.ExtrasStoreManager;
import com.example.focovest.data.StoreManager;
import com.example.focovest.domain.PlanEngine;
import com.example.focovest.model.ForumQuestion;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class NewForumQuestionDialogFragment extends DialogFragment {

    public static final String REQUEST_KEY = "forum_question_created";

    public NewForumQuestionDialogFragment() {
        // Construtor padrão obrigatório para DialogFragment
    }

    public static NewForumQuestionDialogFragment newInstance() {
        return new NewForumQuestionDialogFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_new_question, container, false);

        EditText edtTitle = v.findViewById(R.id.edtQuestionTitle);
        EditText edtBody = v.findViewById(R.id.edtQuestionBody);
        Spinner spinnerExam = v.findViewById(R.id.spinnerQuestionExam);
        Spinner spinnerSubject = v.findViewById(R.id.spinnerQuestionSubject);
        Button btnCancel = v.findViewById(R.id.btnCancelQuestion);
        Button btnPublish = v.findViewById(R.id.btnPublishQuestion);

        ExamId[] exams = ExamId.values();
        List<String> examLabels = new ArrayList<>();
        for (ExamId e : exams) {
            examLabels.add(e.id + " · " + e.institution);
        }
        spinnerExam.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, examLabels));

        SubjectId[] subjects = SubjectId.values();
        List<String> subjectLabels = new ArrayList<>();
        for (SubjectId s : subjects) {
            subjectLabels.add(s.name);
        }
        spinnerSubject.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, subjectLabels));

        btnCancel.setOnClickListener(view -> dismiss());

        btnPublish.setOnClickListener(view -> {
            String title = edtTitle.getText().toString().trim();
            String body = edtBody.getText().toString().trim();
            if (title.length() < 5 || body.length() < 10) {
                Toast.makeText(requireContext(),
                        "Escreva um título e detalhe sua dúvida antes de publicar.",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            StoreManager.State userState = StoreManager.getInstance(requireContext()).getState();
            String author = (userState.name != null && !userState.name.trim().isEmpty())
                    ? userState.name.trim()
                    : "Estudante";
            String city = (userState.city != null && !userState.city.isEmpty())
                    ? userState.city
                    : "Manaus";

            ExamId chosenExam = exams[spinnerExam.getSelectedItemPosition()];
            SubjectId chosenSubject = subjects[spinnerSubject.getSelectedItemPosition()];

            ForumQuestion q = new ForumQuestion(
                    "fq-" + UUID.randomUUID().toString().substring(0, 8),
                    title,
                    body,
                    author,
                    city,
                    chosenExam,
                    chosenSubject,
                    PlanEngine.dayKey(new Date()),
                    true,
                    new ArrayList<>()
            );

            ExtrasStoreManager.getInstance(requireContext()).addForumQuestion(q);
            getParentFragmentManager().setFragmentResult(REQUEST_KEY, new Bundle());
            dismiss();
        });

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
