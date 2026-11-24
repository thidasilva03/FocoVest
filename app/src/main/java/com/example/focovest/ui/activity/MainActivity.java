package com.example.focovest.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.focovest.R;
import com.example.focovest.data.FocoVestRepository;
import com.example.focovest.model.User;
import com.example.focovest.util.EdgeToEdgeHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Activity principal do MVP: Painel, Estudar, Planos, Simulados e Configurações.
 * A troca de telas é feita pelo Navigation Component (res/navigation/nav_graph.xml).
 */
public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private BottomNavigationView bottomNav;
    private TextView btnHeaderConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdgeHelper.enable(this, true);
        super.onCreate(savedInstanceState);

        FocoVestRepository repo = FocoVestRepository.getInstance(this);
        User user = repo.getOrCreateLocalUser();

        if (!user.profileCompleted) {
            startActivity(new Intent(this, WelcomeActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        EdgeToEdgeHelper.applySystemBarInsets(findViewById(R.id.mainRoot), true);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.navHostFragment);
        if (navHostFragment == null) return;
        navController = navHostFragment.getNavController();

        bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setItemActiveIndicatorEnabled(false);
        // Sincroniza abas <-> destinos e cuida do botão Voltar (sempre retorna ao Painel antes de sair)
        NavigationUI.setupWithNavController(bottomNav, navController);

        btnHeaderConfig = findViewById(R.id.btnHeaderConfig);
        btnHeaderConfig.setOnClickListener(v -> navController.navigate(R.id.action_global_settings));

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            boolean configActive = destination.getId() == R.id.settingsFragment;
            updateHeaderConfigHighlight(configActive);
            if (configActive) {
                clearBottomNavSelection();
            }
        });
    }

    /** Destaca o botão ⚙ do topo quando a tela de Configurações está aberta. */
    private void updateHeaderConfigHighlight(boolean active) {
        int lime = getResources().getColor(R.color.lime, getTheme());
        int ink = getResources().getColor(R.color.ink, getTheme());
        btnHeaderConfig.setBackgroundResource(active ? R.drawable.bg_card_dark : R.drawable.bg_card);
        btnHeaderConfig.setTextColor(active ? lime : ink);
    }

    /** Configurações não é uma aba: nenhuma aba deve aparecer selecionada. */
    private void clearBottomNavSelection() {
        Menu menu = bottomNav.getMenu();
        menu.setGroupCheckable(0, true, false);
        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setChecked(false);
        }
        menu.setGroupCheckable(0, true, true);
    }
}
