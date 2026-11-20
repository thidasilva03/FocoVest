package com.example.focovest.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.focovest.R;

/**
 * DialogFragment de confirmação com construtor padrão vazio + newInstance()
 * para evitar crashes em recriação de tela (rotação/mudança de configuração).
 */
public class ConfirmResetDialogFragment extends DialogFragment {

    public static final String REQUEST_KEY = "confirm_reset_request";
    public static final String RESULT_CONFIRMED = "confirmed";

    private static final String ARG_TITLE = "title";
    private static final String ARG_MESSAGE = "message";

    public ConfirmResetDialogFragment() {
        // Construtor padrão obrigatório para DialogFragment
    }

    public static ConfirmResetDialogFragment newInstance(String title, String message) {
        ConfirmResetDialogFragment fragment = new ConfirmResetDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_MESSAGE, message);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();
        String title = args != null ? args.getString(ARG_TITLE, getString(R.string.dialog_confirm_reset_title))
                : getString(R.string.dialog_confirm_reset_title);
        String message = args != null ? args.getString(ARG_MESSAGE, getString(R.string.dialog_confirm_reset_message))
                : getString(R.string.dialog_confirm_reset_message);

        return new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton(R.string.dialog_cancel, (d, which) -> dismiss())
                .setPositiveButton(R.string.dialog_confirm, (d, which) -> {
                    Bundle result = new Bundle();
                    result.putBoolean(RESULT_CONFIRMED, true);
                    getParentFragmentManager().setFragmentResult(REQUEST_KEY, result);
                    dismiss();
                })
                .create();
    }
}
