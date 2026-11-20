package com.example.focovest.ui.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.focovest.*;
import com.example.focovest.data.StoreManager;
import com.example.focovest.ui.fragment.*;

public class MainActivity extends AppCompatActivity {

    private String currentView = "painel";
    private String selectedTopicId = null;

    private TextView navPainel, navEstudo, navPlano, navSimulados, navRanking;
    private TextView btnHeaderCalendar, btnHeaderMaterials, btnHeaderForum, btnHeaderPerformance, btnHeaderConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StoreManager store = StoreManager.getInstance(this);
        if (!store.getState().profileCreated) {
            startActivity(new Intent(this, OnboardingActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        navPainel = findViewById(R.id.navPainel);
        navEstudo = findViewById(R.id.navEstudo);
        navPlano = findViewById(R.id.navPlano);
        navSimulados = findViewById(R.id.navSimulados);
        navRanking = findViewById(R.id.navRanking);
        btnHeaderCalendar = findViewById(R.id.btnHeaderCalendar);
        btnHeaderMaterials = findViewById(R.id.btnHeaderMaterials);
        btnHeaderForum = findViewById(R.id.btnHeaderForum);
        btnHeaderPerformance = findViewById(R.id.btnHeaderPerformance);
        btnHeaderConfig = findViewById(R.id.btnHeaderConfig);

        navPainel.setOnClickListener(v -> navigateTo("painel"));
        navEstudo.setOnClickListener(v -> navigateTo("estudo"));
        navPlano.setOnClickListener(v -> navigateTo("plano"));
        navSimulados.setOnClickListener(v -> navigateTo("simulados"));
        navRanking.setOnClickListener(v -> navigateTo("ranking"));
        btnHeaderCalendar.setOnClickListener(v -> navigateTo("calendario"));
        btnHeaderMaterials.setOnClickListener(v -> navigateTo("materiais"));
        btnHeaderForum.setOnClickListener(v -> navigateTo("forum"));
        btnHeaderPerformance.setOnClickListener(v -> navigateTo("desempenho"));
        btnHeaderConfig.setOnClickListener(v -> navigateTo("config"));

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (!"painel".equals(currentView)) {
                    navigateTo("painel");
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        navigateTo("painel");
    }

    public void openTopic(String topicId) {
        this.selectedTopicId = topicId;
        StoreManager.getInstance(this).getState().lastTopic = topicId;
        StoreManager.getInstance(this).save();
        navigateTo("estudo");
    }

    public String getSelectedTopicId() {
        return selectedTopicId;
    }

    public void navigateTo(String view) {
        this.currentView = view;
        StoreManager store = StoreManager.getInstance(this);
        navPlano.setText("turma".equals(store.getState().mode) ? "Minha turma" : "Meu plano");

        updateNavHighlight();

        Fragment fragment;
        switch (view) {
            case "painel":
                fragment = new DashboardFragment();
                break;
            case "estudo":
                fragment = new StudyRoomFragment();
                break;
            case "plano":
                fragment = new PlanFragment();
                break;
            case "simulados":
                fragment = new SimuladosFragment();
                break;
            case "ranking":
                fragment = new RankingFragment();
                break;
            case "calendario":
                fragment = new CalendarFragment();
                break;
            case "materiais":
                fragment = new MaterialsHubFragment();
                break;
            case "forum":
                fragment = new ForumFragment();
                break;
            case "desempenho":
                fragment = new PerformanceHistoryFragment();
                break;
            case "config":
            default:
                fragment = new SettingsFragment();
                break;
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    private void updateNavHighlight() {
        int lime = getResources().getColor(R.color.lime, getTheme());
        int ink = getResources().getColor(R.color.ink, getTheme());
        int inactive = Color.parseColor("#99FFFFFF");

        navPainel.setTextColor("painel".equals(currentView) ? lime : inactive);
        navEstudo.setTextColor("estudo".equals(currentView) ? lime : inactive);
        navPlano.setTextColor("plano".equals(currentView) ? lime : inactive);
        navSimulados.setTextColor("simulados".equals(currentView) ? lime : inactive);
        navRanking.setTextColor("ranking".equals(currentView) ? lime : inactive);

        styleHeaderButton(btnHeaderCalendar, "calendario".equals(currentView), lime, ink);
        styleHeaderButton(btnHeaderMaterials, "materiais".equals(currentView), lime, ink);
        styleHeaderButton(btnHeaderForum, "forum".equals(currentView), lime, ink);
        styleHeaderButton(btnHeaderPerformance, "desempenho".equals(currentView), lime, ink);
        styleHeaderButton(btnHeaderConfig, "config".equals(currentView), lime, ink);
    }

    private void styleHeaderButton(TextView btn, boolean active, int lime, int ink) {
        btn.setBackgroundResource(active ? R.drawable.bg_card_dark : R.drawable.bg_card);
        btn.setTextColor(active ? lime : ink);
    }
}