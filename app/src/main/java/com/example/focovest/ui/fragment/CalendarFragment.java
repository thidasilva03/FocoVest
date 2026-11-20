package com.example.focovest.ui.fragment;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.focovest.R;
import com.example.focovest.data.DataRepository.ExamId;
import com.example.focovest.data.ExtrasRepository;
import com.example.focovest.data.ExtrasStoreManager;
import com.example.focovest.data.StoreManager;
import com.example.focovest.domain.PlanEngine;
import com.example.focovest.domain.PlanEngine.PlanDay;
import com.example.focovest.domain.PlanEngine.PlanResult;
import com.example.focovest.model.ExamEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class CalendarFragment extends Fragment {

    private static final String[] WEEK_DAYS = {"D", "S", "T", "Q", "Q", "S", "S"};

    private StoreManager store;
    private ExtrasStoreManager extras;

    private final Calendar displayedMonth = Calendar.getInstance();
    private String selectedDateKey;

    private LinearLayout countdownContainer;
    private LinearLayout calendarWeekHeader;
    private LinearLayout calendarGridContainer;
    private LinearLayout monthEventsContainer;
    private TextView txtMonthYear;
    private TextView txtSelectedDayLabel;
    private TextView txtMonthEventsHeader;
    private EditText edtCustomEventTitle;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_calendar, container, false);

        store = StoreManager.getInstance(requireContext());
        extras = ExtrasStoreManager.getInstance(requireContext());

        displayedMonth.set(Calendar.DAY_OF_MONTH, 1);
        selectedDateKey = PlanEngine.dayKey(new Date());

        countdownContainer = v.findViewById(R.id.countdownContainer);
        calendarWeekHeader = v.findViewById(R.id.calendarWeekHeader);
        calendarGridContainer = v.findViewById(R.id.calendarGridContainer);
        monthEventsContainer = v.findViewById(R.id.monthEventsContainer);
        txtMonthYear = v.findViewById(R.id.txtMonthYear);
        txtSelectedDayLabel = v.findViewById(R.id.txtSelectedDayLabel);
        txtMonthEventsHeader = v.findViewById(R.id.txtMonthEventsHeader);
        edtCustomEventTitle = v.findViewById(R.id.edtCustomEventTitle);

        v.findViewById(R.id.btnPrevMonth).setOnClickListener(view -> {
            displayedMonth.add(Calendar.MONTH, -1);
            renderCalendar();
        });
        v.findViewById(R.id.btnNextMonth).setOnClickListener(view -> {
            displayedMonth.add(Calendar.MONTH, 1);
            renderCalendar();
        });

        Button btnAdd = v.findViewById(R.id.btnAddCustomEvent);
        btnAdd.setOnClickListener(view -> {
            String title = edtCustomEventTitle.getText().toString().trim();
            if (title.isEmpty()) {
                Toast.makeText(requireContext(), "Digite o título do lembrete.", Toast.LENGTH_SHORT).show();
                return;
            }
            ExamEvent custom = new ExamEvent(
                    "ev-custom-" + UUID.randomUUID().toString().substring(0, 6),
                    title,
                    "Lembrete pessoal adicionado no calendário.",
                    null,
                    selectedDateKey,
                    "Lembrete pessoal",
                    "pessoal",
                    true
            );
            extras.addCustomEvent(custom);
            edtCustomEventTitle.setText("");
            renderCalendar();
        });

        buildWeekHeader();
        renderCountdowns();
        renderCalendar();
        return v;
    }

    private void buildWeekHeader() {
        calendarWeekHeader.removeAllViews();
        for (String wd : WEEK_DAYS) {
            TextView tv = new TextView(requireContext());
            tv.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            tv.setGravity(Gravity.CENTER);
            tv.setText(wd);
            tv.setTextSize(12f);
            tv.setTypeface(null, Typeface.BOLD);
            tv.setTextColor(getColorRes(R.color.muted));
            calendarWeekHeader.addView(tv);
        }
    }

    private void renderCountdowns() {
        countdownContainer.removeAllViews();
        List<ExamId> targets = store.getState().targetExams;
        if (targets == null || targets.isEmpty()) return;

        Date today = PlanEngine.startOfDay(new Date());
        Calendar nowCal = Calendar.getInstance();
        int currentYear = nowCal.get(Calendar.YEAR);

        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(36, 28, 36, 28);
        card.setBackgroundResource(R.drawable.bg_card_dark);

        TextView header = new TextView(requireContext());
        header.setText("CONTAGEM REGRESSIVA · SEUS VESTIBULARES");
        header.setTextColor(getColorRes(R.color.lime));
        header.setTextSize(11f);
        header.setTypeface(null, Typeface.BOLD);
        card.addView(header);

        for (ExamId ex : targets) {
            Calendar examCal = Calendar.getInstance();
            examCal.set(currentYear, ex.month - 1, ex.day, 0, 0, 0);
            examCal.set(Calendar.MILLISECOND, 0);
            if (examCal.getTime().before(today)) {
                examCal.add(Calendar.YEAR, 1);
            }
            long diffDays = (examCal.getTimeInMillis() - today.getTime()) / (1000L * 60 * 60 * 24);

            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, 14, 0, 4);

            TextView tvLeft = new TextView(requireContext());
            tvLeft.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            tvLeft.setText(ex.id + " (" + ex.institution + ") · "
                    + String.format(Locale.US, "%02d/%02d/%04d", ex.day, ex.month, examCal.get(Calendar.YEAR)));
            tvLeft.setTextColor(Color.WHITE);
            tvLeft.setTextSize(13f);
            tvLeft.setTypeface(null, Typeface.BOLD);

            TextView tvDays = new TextView(requireContext());
            tvDays.setText(diffDays == 0 ? "HOJE" : "Faltam " + diffDays + "d");
            tvDays.setTextColor(getColorRes(R.color.lime));
            tvDays.setTextSize(12f);
            tvDays.setTypeface(null, Typeface.BOLD);

            row.addView(tvLeft);
            row.addView(tvDays);
            card.addView(row);
        }

        countdownContainer.addView(card);
    }

    private void renderCalendar() {
        SimpleDateFormat headerFmt = new SimpleDateFormat("MMMM yyyy", new Locale("pt", "BR"));
        String label = headerFmt.format(displayedMonth.getTime());
        txtMonthYear.setText(label.substring(0, 1).toUpperCase(new Locale("pt", "BR")) + label.substring(1));

        Date selectedDate = PlanEngine.parseDay(selectedDateKey);
        txtSelectedDayLabel.setText("Dia selecionado: " + PlanEngine.fmtShort(selectedDate));

        int year = displayedMonth.get(Calendar.YEAR);
        int month = displayedMonth.get(Calendar.MONTH); // 0-indexed

        List<ExamEvent> allEvents = new ArrayList<>(ExtrasRepository.buildOfficialEvents(year));
        allEvents.addAll(extras.getState().customEvents);

        Set<String> eventDays = new HashSet<>();
        List<ExamEvent> monthEvents = new ArrayList<>();
        String monthPrefix = String.format(Locale.US, "%04d-%02d-", year, month + 1);
        for (ExamEvent ev : allEvents) {
            if (ev.dateKey != null) {
                eventDays.add(ev.dateKey);
                if (ev.dateKey.startsWith(monthPrefix)) {
                    monthEvents.add(ev);
                }
            }
        }
        Collections.sort(monthEvents, (a, b) -> a.dateKey.compareTo(b.dateKey));

        Set<String> studyPlanDays = new HashSet<>();
        PlanResult plan = store.getPlanResult();
        for (PlanDay pd : plan.days) {
            studyPlanDays.add(PlanEngine.dayKey(pd.date));
        }

        // Renderizar grade mensal
        calendarGridContainer.removeAllViews();
        Calendar cal = (Calendar) displayedMonth.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1; // 0 = Dom
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        String todayKey = PlanEngine.dayKey(new Date());

        int dayCounter = 1;
        for (int week = 0; week < 6 && dayCounter <= daysInMonth; week++) {
            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(0, 4, 0, 4);

            for (int col = 0; col < 7; col++) {
                TextView cell = new TextView(requireContext());
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, 96, 1f);
                lp.setMargins(3, 2, 3, 2);
                cell.setLayoutParams(lp);
                cell.setGravity(Gravity.CENTER);
                cell.setTextSize(13f);

                if (week == 0 && col < firstDayOfWeek || dayCounter > daysInMonth) {
                    cell.setText("");
                } else {
                    final int d = dayCounter++;
                    final String key = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, d);
                    boolean isSelected = key.equals(selectedDateKey);
                    boolean isToday = key.equals(todayKey);
                    boolean hasEvent = eventDays.contains(key);
                    boolean hasStudy = studyPlanDays.contains(key);

                    String suffix = hasEvent ? "\n●" : (hasStudy ? "\n·" : "");
                    cell.setText(d + suffix);

                    if (isSelected) {
                        cell.setBackgroundResource(R.drawable.bg_card_dark);
                        cell.setTextColor(getColorRes(R.color.lime));
                        cell.setTypeface(null, Typeface.BOLD);
                    } else if (hasEvent) {
                        cell.setBackgroundResource(R.drawable.bg_pill_accent_soft);
                        cell.setTextColor(getColorRes(R.color.accent));
                        cell.setTypeface(null, Typeface.BOLD);
                    } else if (hasStudy) {
                        cell.setBackgroundResource(R.drawable.bg_pill_success);
                        cell.setTextColor(getColorRes(R.color.success_dark));
                    } else if (isToday) {
                        cell.setBackgroundResource(R.drawable.bg_input);
                        cell.setTextColor(getColorRes(R.color.ink));
                        cell.setTypeface(null, Typeface.BOLD);
                    } else {
                        cell.setTextColor(getColorRes(R.color.ink));
                    }

                    cell.setOnClickListener(view -> {
                        selectedDateKey = key;
                        renderCalendar();
                    });
                }
                row.addView(cell);
            }
            calendarGridContainer.addView(row);
        }

        // Renderizar lista de eventos do mês
        txtMonthEventsHeader.setText("Eventos deste mês (" + monthEvents.size() + ")");
        monthEventsContainer.removeAllViews();

        if (monthEvents.isEmpty()) {
            LinearLayout empty = createCard();
            TextView tv = new TextView(requireContext());
            tv.setText("Nenhum evento oficial ou lembrete cadastrado neste mês.");
            tv.setTextColor(getColorRes(R.color.muted));
            empty.addView(tv);
            monthEventsContainer.addView(empty);
        } else {
            for (ExamEvent ev : monthEvents) {
                LinearLayout card = createCard();

                TextView badge = new TextView(requireContext());
                String examLabel = ev.exam != null ? ev.exam.id + " · " : "";
                badge.setText(examLabel + PlanEngine.fmtShort(PlanEngine.parseDay(ev.dateKey)).toUpperCase(Locale.ROOT)
                        + " · " + ev.timeLabel);
                badge.setTextColor(getColorRes("prova".equals(ev.category) ? R.color.accent : R.color.success_dark));
                badge.setTextSize(11f);
                badge.setTypeface(null, Typeface.BOLD);
                card.addView(badge);

                TextView tvTitle = new TextView(requireContext());
                tvTitle.setText(ev.title);
                tvTitle.setTextColor(getColorRes(R.color.ink));
                tvTitle.setTextSize(16f);
                tvTitle.setTypeface(null, Typeface.BOLD);
                tvTitle.setPadding(0, 6, 0, 4);
                card.addView(tvTitle);

                TextView tvDesc = new TextView(requireContext());
                tvDesc.setText(ev.description);
                tvDesc.setTextColor(getColorRes(R.color.muted));
                tvDesc.setTextSize(13f);
                card.addView(tvDesc);

                if (ev.custom) {
                    TextView btnDel = new TextView(requireContext());
                    btnDel.setText("Remover lembrete");
                    btnDel.setTextColor(getColorRes(R.color.accent));
                    btnDel.setTextSize(12f);
                    btnDel.setTypeface(null, Typeface.BOLD);
                    btnDel.setPadding(0, 12, 0, 0);
                    btnDel.setOnClickListener(view -> {
                        extras.removeCustomEvent(ev.id);
                        renderCalendar();
                    });
                    card.addView(btnDel);
                }
                monthEventsContainer.addView(card);
            }
        }
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
