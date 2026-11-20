package com.example.focovest.ui.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.focovest.R;
import com.example.focovest.data.AuthSessionManager;
import com.example.focovest.data.DataRepository;
import com.example.focovest.data.DataRepository.ExamId;
import com.example.focovest.data.ExtrasStoreManager;
import com.example.focovest.data.StoreManager;
import com.example.focovest.model.UserAccount;
import com.example.focovest.ui.activity.LoginActivity;
import com.example.focovest.ui.dialog.ConfirmResetDialogFragment;

import java.util.*;

public class SettingsFragment extends Fragment {

    private static final String[] DAY_LABELS = {"D", "S", "T", "Q", "Q", "S", "S"};
    private StoreManager store;
    private LinearLayout root;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_scroll_container, container, false);
        root = v.findViewById(R.id.rootContainer);
        store = StoreManager.getInstance(requireContext());

        getParentFragmentManager().setFragmentResultListener(
                ConfirmResetDialogFragment.REQUEST_KEY,
                getViewLifecycleOwner(),
                (requestKey, bundle) -> {
                    if (bundle.getBoolean(ConfirmResetDialogFragment.RESULT_CONFIRMED, false)) {
                        store.resetProfile();
                        ExtrasStoreManager.getInstance(requireContext()).resetAll();
                        AuthSessionManager.getInstance(requireContext()).clearAll();
                        startActivity(new Intent(requireContext(), LoginActivity.class));
                        requireActivity().finish();
                    }
                }
        );

        render();
        return v;
    }

    private void render() {
        root.removeAllViews();
        StoreManager.State state = store.getState();

        TextView eyebrow = new TextView(requireContext());
        eyebrow.setText("CONFIGURAÇÕES");
        eyebrow.setTextColor(getColorRes(R.color.accent));
        eyebrow.setTypeface(null, Typeface.BOLD);
        eyebrow.setTextSize(11f);
        root.addView(eyebrow);

        TextView title = new TextView(requireContext());
        title.setText("Seu perfil e preferências.");
        title.setTextColor(getColorRes(R.color.ink));
        title.setTypeface(null, Typeface.BOLD);
        title.setTextSize(26f);
        title.setPadding(0, 4, 0, 16);
        root.addView(title);

        // 1. SEÇÃO PERFIL
        LinearLayout profileCard = createCard();
        addSectionTitle(profileCard, "Perfil");

        EditText edtName = new EditText(requireContext());
        edtName.setText(state.name);
        edtName.setHint("Seu nome");
        edtName.setBackgroundResource(R.drawable.bg_input);
        edtName.setPadding(28, 22, 28, 22);
        profileCard.addView(edtName);

        EditText edtCourse = new EditText(requireContext());
        edtCourse.setText(state.targetCourse);
        edtCourse.setHint("Curso desejado (Ex.: Medicina)");
        edtCourse.setBackgroundResource(R.drawable.bg_input);
        edtCourse.setPadding(28, 22, 28, 22);
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        clp.setMargins(0, 16, 0, 16);
        edtCourse.setLayoutParams(clp);
        profileCard.addView(edtCourse);

        Spinner citySpinner = new Spinner(requireContext());
        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, DataRepository.AM_CITIES);
        citySpinner.setAdapter(cityAdapter);
        int cityIdx = DataRepository.AM_CITIES.indexOf(state.city);
        if (cityIdx >= 0) citySpinner.setSelection(cityIdx);
        profileCard.addView(citySpinner);

        List<ExamId> draftExams = new ArrayList<>(state.targetExams);
        for (ExamId ex : ExamId.values()) {
            CheckBox cb = new CheckBox(requireContext());
            cb.setText(ex.id + " · " + ex.institution);
            cb.setChecked(draftExams.contains(ex));
            cb.setOnCheckedChangeListener((b, checked) -> {
                if (checked) {
                    if (!draftExams.contains(ex)) draftExams.add(ex);
                } else {
                    draftExams.remove(ex);
                }
            });
            profileCard.addView(cb);
        }

        Button btnSaveProfile = new Button(requireContext());
        btnSaveProfile.setText("Salvar perfil");
        btnSaveProfile.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getColorRes(R.color.ink)));
        btnSaveProfile.setTextColor(Color.WHITE);
        btnSaveProfile.setOnClickListener(v -> {
            if (draftExams.isEmpty()) {
                Toast.makeText(requireContext(), "Selecione ao menos um vestibular.", Toast.LENGTH_SHORT).show();
                return;
            }
            String n = edtName.getText().toString().trim();
            if (!n.isEmpty()) state.name = n;
            state.targetCourse = edtCourse.getText().toString().trim();
            state.city = citySpinner.getSelectedItem().toString();
            state.targetExams = new ArrayList<>(draftExams);
            store.save();
            Toast.makeText(requireContext(), "✓ Perfil salvo!", Toast.LENGTH_SHORT).show();
        });
        profileCard.addView(btnSaveProfile);
        root.addView(profileCard);

        // 2. SEÇÃO METODOLOGIA (Autônoma vs Turma)
        LinearLayout modeCard = createCard();
        addSectionTitle(modeCard, "Metodologia de estudo");

        boolean isAutonoma = "autonoma".equals(state.mode);
        Button btnSwitchMode = new Button(requireContext());
        btnSwitchMode.setAllCaps(false);
        btnSwitchMode.setText(isAutonoma
                ? "Atual: Autônoma (Tocar para mudar para Turma)"
                : "Atual: Por Turma (Tocar para mudar para Autônoma)");
        btnSwitchMode.setOnClickListener(v -> {
            state.mode = isAutonoma ? "turma" : "autonoma";
            if ("turma".equals(state.mode)) state.turmaJoined = true;
            state.organizerView = false;
            store.save();
            render();
        });
        modeCard.addView(btnSwitchMode);
        root.addView(modeCard);

        // 3. SEÇÃO PLAN SETTINGS (Horas por dia e Dias da semana)
        LinearLayout planCard = createCard();
        addSectionTitle(planCard, "Ritmo do Plano do Edital");

        LinearLayout hoursRow = new LinearLayout(requireContext());
        hoursRow.setOrientation(LinearLayout.HORIZONTAL);
        hoursRow.setGravity(Gravity.CENTER_VERTICAL);

        Button btnMinus = new Button(requireContext());
        btnMinus.setText("-");
        btnMinus.setOnClickListener(v -> {
            state.dailyGoal = Math.max(1, state.dailyGoal - 1);
            store.save();
            render();
        });

        TextView tvGoal = new TextView(requireContext());
        tvGoal.setText("   " + state.dailyGoal + "h/dia (" + (state.dailyGoal * state.studyDays.size()) + "h/semana)   ");
        tvGoal.setTypeface(null, Typeface.BOLD);
        tvGoal.setTextSize(16f);

        Button btnPlus = new Button(requireContext());
        btnPlus.setText("+");
        btnPlus.setOnClickListener(v -> {
            state.dailyGoal = Math.min(12, state.dailyGoal + 1);
            store.save();
            render();
        });

        hoursRow.addView(btnMinus);
        hoursRow.addView(tvGoal);
        hoursRow.addView(btnPlus);
        planCard.addView(hoursRow);

        LinearLayout daysRow = new LinearLayout(requireContext());
        daysRow.setOrientation(LinearLayout.HORIZONTAL);
        daysRow.setPadding(0, 20, 0, 0);

        for (int i = 0; i < 7; i++) {
            final int dayIdx = i;
            boolean on = state.studyDays.contains(dayIdx);
            TextView dBtn = new TextView(requireContext());
            LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(0, 96, 1f);
            dlp.setMargins(4, 0, 4, 0);
            dBtn.setLayoutParams(dlp);
            dBtn.setGravity(Gravity.CENTER);
            dBtn.setText(DAY_LABELS[i]);
            dBtn.setTypeface(null, Typeface.BOLD);
            dBtn.setBackgroundResource(on ? R.drawable.bg_button_accent : R.drawable.bg_input);
            dBtn.setTextColor(on ? Color.WHITE : getColorRes(R.color.muted));
            dBtn.setOnClickListener(v -> {
                if (on && state.studyDays.size() > 1) {
                    state.studyDays.remove(Integer.valueOf(dayIdx));
                } else if (!on) {
                    state.studyDays.add(dayIdx);
                    Collections.sort(state.studyDays);
                }
                store.save();
                render();
            });
            daysRow.addView(dBtn);
        }
        planCard.addView(daysRow);
        root.addView(planCard);

        // 4. SEÇÃO CONTA E SESSÃO
        AuthSessionManager auth = AuthSessionManager.getInstance(requireContext());
        UserAccount account = auth.getAccount();
        LinearLayout accountCard = createCard();
        addSectionTitle(accountCard, "Conta e acesso");

        TextView tvAccountStatus = new TextView(requireContext());
        if (auth.isLoggedIn() && account != null) {
            tvAccountStatus.setText("Conectado como @" + account.username + " (" + account.email + ")");
        } else {
            tvAccountStatus.setText("Usando em modo local (sem conta vinculada).");
        }
        tvAccountStatus.setTextColor(getColorRes(R.color.muted));
        tvAccountStatus.setPadding(0, 0, 0, 14);
        accountCard.addView(tvAccountStatus);

        Button btnAuthAction = new Button(requireContext());
        btnAuthAction.setAllCaps(false);
        btnAuthAction.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getColorRes(R.color.ink)));
        btnAuthAction.setTextColor(Color.WHITE);
        if (auth.isLoggedIn()) {
            btnAuthAction.setText("Sair da conta");
            btnAuthAction.setOnClickListener(v -> {
                auth.logout();
                startActivity(new Intent(requireContext(), LoginActivity.class));
                requireActivity().finish();
            });
        } else {
            btnAuthAction.setText("Entrar ou criar conta →");
            btnAuthAction.setOnClickListener(v -> {
                startActivity(new Intent(requireContext(), LoginActivity.class));
            });
        }
        accountCard.addView(btnAuthAction);
        root.addView(accountCard);

        // 5. SEÇÃO REINICIAR PERFIL (Reset com confirmação)
        LinearLayout resetCard = createCard();
        addSectionTitle(resetCard, "Dados locais");

        Button btnReset = new Button(requireContext());
        btnReset.setText("↺ Reiniciar perfil e apagar progresso");
        btnReset.setAllCaps(false);
        btnReset.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getColorRes(R.color.accent)));
        btnReset.setTextColor(Color.WHITE);
        btnReset.setOnClickListener(v -> {
            ConfirmResetDialogFragment.newInstance(
                    getString(R.string.dialog_confirm_reset_title),
                    getString(R.string.dialog_confirm_reset_message)
            ).show(getParentFragmentManager(), "confirm_reset_dialog");
        });
        resetCard.addView(btnReset);
        root.addView(resetCard);
    }

    private void addSectionTitle(LinearLayout parent, String text) {
        TextView tv = new TextView(requireContext());
        tv.setText(text);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextSize(18f);
        tv.setTextColor(getColorRes(R.color.ink));
        tv.setPadding(0, 0, 0, 14);
        parent.addView(tv);
    }

    private LinearLayout createCard() {
        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(36, 32, 36, 32);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 20);
        card.setLayoutParams(lp);
        card.setBackgroundResource(R.drawable.bg_card);
        return card;
    }

    private int getColorRes(int res) {
        return getResources().getColor(res, requireContext().getTheme());
    }
}
