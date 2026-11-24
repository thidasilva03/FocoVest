package com.example.focovest.util;

import android.graphics.Color;
import android.view.View;

import androidx.activity.ComponentActivity;
import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Configura edge-to-edge e aplica WindowInsets de barras do sistema, recorte de câmera
 * e teclado (opcional) preservando o padding original da View raiz.
 */
public final class EdgeToEdgeHelper {

    private EdgeToEdgeHelper() {}

    /**
     * Ativa edge-to-edge com ícones escuros (fundo claro) ou claros (fundo escuro).
     * Deve ser chamado antes de {@code setContentView(...)}.
     */
    public static void enable(ComponentActivity activity, boolean lightBackground) {
        SystemBarStyle barStyle = lightBackground
                ? SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                : SystemBarStyle.dark(Color.TRANSPARENT);
        EdgeToEdge.enable(activity, barStyle, barStyle);
    }

    /**
     * Aplica os insets de barra de status, barra de navegação, recorte de câmera e,
     * opcionalmente, teclado (IME) ao padding da View raiz sem sobrescrever o padding inicial.
     */
    public static void applySystemBarInsets(View rootView, boolean includeIme) {
        if (rootView == null) return;

        final int initialLeft = rootView.getPaddingLeft();
        final int initialTop = rootView.getPaddingTop();
        final int initialRight = rootView.getPaddingRight();
        final int initialBottom = rootView.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (view, windowInsets) -> {
            int types = WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout();
            if (includeIme) {
                types |= WindowInsetsCompat.Type.ime();
            }
            Insets insets = windowInsets.getInsets(types);
            view.setPadding(
                    initialLeft + insets.left,
                    initialTop + insets.top,
                    initialRight + insets.right,
                    initialBottom + insets.bottom
            );
            return WindowInsetsCompat.CONSUMED;
        });
    }
}
