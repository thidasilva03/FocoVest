package com.example.focovest.ui.fragment;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.focovest.R;
import com.example.focovest.data.DataRepository;
import com.example.focovest.data.DataRepository.RankEntry;
import com.example.focovest.data.StoreManager;

import java.text.NumberFormat;
import java.util.*;

public class RankingFragment extends Fragment {

    public static final int USER_BASE_POINTS = 1180;
    public static final int RANKING_SIZE = 15;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_scroll_container, container, false);
        LinearLayout root = v.findViewById(R.id.rootContainer);

        StoreManager store = StoreManager.getInstance(requireContext());
        StoreManager.State state = store.getState();
        int myPoints = USER_BASE_POINTS + store.getPoints();

        List<RankEntry> list = new ArrayList<>(DataRepository.rankingAmazonas());
        RankEntry me = new RankEntry(state.name + " (você)", state.city, myPoints, 6, 2, true);
        list.add(me);
        list.sort((a, b) -> Integer.compare(b.points, a.points));

        int myPos = 1;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).isUser) {
                myPos = i + 1;
                break;
            }
        }

        NumberFormat nf = NumberFormat.getIntegerInstance(new Locale("pt", "BR"));

        TextView eyebrow = new TextView(requireContext());
        eyebrow.setText("RANKING SEMANAL");
        eyebrow.setTextColor(getColorRes(R.color.accent));
        eyebrow.setTypeface(null, Typeface.BOLD);
        eyebrow.setTextSize(11f);
        root.addView(eyebrow);

        TextView title = new TextView(requireContext());
        title.setText("Os melhores do Amazonas.");
        title.setTextColor(getColorRes(R.color.ink));
        title.setTypeface(null, Typeface.BOLD);
        title.setTextSize(26f);
        title.setPadding(0, 4, 0, 16);
        root.addView(title);

        // Card Pódio Top 3 (2º, 1º, 3º)
        LinearLayout podiumCard = new LinearLayout(requireContext());
        podiumCard.setOrientation(LinearLayout.HORIZONTAL);
        podiumCard.setGravity(Gravity.BOTTOM);
        podiumCard.setPadding(24, 48, 24, 32);
        LinearLayout.LayoutParams plp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        plp.setMargins(0, 0, 0, 20);
        podiumCard.setLayoutParams(plp);
        podiumCard.setBackgroundResource(R.drawable.bg_card_dark);

        int[] order = {1, 0, 2}; // 2º lugar, 1º lugar, 3º lugar
        for (int idx : order) {
            RankEntry p = list.get(idx);
            int place = idx + 1;

            LinearLayout col = new LinearLayout(requireContext());
            col.setOrientation(LinearLayout.VERTICAL);
            col.setGravity(Gravity.CENTER_HORIZONTAL);
            col.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            TextView tvPlace = new TextView(requireContext());
            tvPlace.setText(place == 1 ? "👑 1º" : place + "º");
            tvPlace.setTextColor(getColorRes(place == 1 ? R.color.lime : R.color.white));
            tvPlace.setTypeface(null, Typeface.BOLD);
            tvPlace.setTextSize(place == 1 ? 22f : 18f);

            TextView tvName = new TextView(requireContext());
            tvName.setText(p.name);
            tvName.setGravity(Gravity.CENTER);
            tvName.setTextColor(Color.WHITE);
            tvName.setTypeface(null, Typeface.BOLD);
            tvName.setTextSize(13f);

            TextView tvCity = new TextView(requireContext());
            tvCity.setText(p.city);
            tvCity.setTextColor(Color.parseColor("#99FFFFFF"));
            tvCity.setTextSize(11f);

            TextView tvPts = new TextView(requireContext());
            tvPts.setText(nf.format(p.points) + " pts");
            tvPts.setTextColor(getColorRes(R.color.lime));
            tvPts.setTypeface(null, Typeface.BOLD);
            tvPts.setTextSize(13f);

            col.addView(tvPlace);
            col.addView(tvName);
            col.addView(tvCity);
            col.addView(tvPts);
            podiumCard.addView(col);
        }
        root.addView(podiumCard);

        // Banner da posição do utilizador se estiver fora do Top 3
        if (myPos > 3) {
            RankEntry target = myPos <= RANKING_SIZE ? list.get(myPos - 2) : list.get(RANKING_SIZE - 1);
            int diff = Math.max(1, target.points - me.points + 1);

            LinearLayout myBanner = new LinearLayout(requireContext());
            myBanner.setOrientation(LinearLayout.VERTICAL);
            myBanner.setPadding(36, 28, 36, 28);
            LinearLayout.LayoutParams mlp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            mlp.setMargins(0, 0, 0, 20);
            myBanner.setLayoutParams(mlp);
            myBanner.setBackgroundResource(R.drawable.bg_button_accent);

            TextView tvPos = new TextView(requireContext());
            tvPos.setText("SUA POSIÇÃO: " + myPos + "º LUGAR");
            tvPos.setTextColor(Color.WHITE);
            tvPos.setTypeface(null, Typeface.BOLD);
            tvPos.setTextSize(18f);

            TextView tvTarget = new TextView(requireContext());
            tvTarget.setText("Faltam " + nf.format(diff) + " pts para alcançar " + target.name);
            tvTarget.setTextColor(Color.WHITE);
            tvTarget.setTextSize(13f);

            myBanner.addView(tvPos);
            myBanner.addView(tvTarget);
            root.addView(myBanner);
        }

        // Lista do 4º ao 15º lugar
        LinearLayout tableCard = new LinearLayout(requireContext());
        tableCard.setOrientation(LinearLayout.VERTICAL);
        tableCard.setPadding(28, 24, 28, 24);
        tableCard.setBackgroundResource(R.drawable.bg_card);

        for (int i = 3; i < Math.min(RANKING_SIZE, list.size()); i++) {
            RankEntry e = list.get(i);
            int pos = i + 1;

            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(20, 20, 20, 20);
            if (e.isUser) row.setBackgroundResource(R.drawable.bg_input);

            TextView tvNum = new TextView(requireContext());
            tvNum.setLayoutParams(new LinearLayout.LayoutParams(80, LinearLayout.LayoutParams.WRAP_CONTENT));
            tvNum.setText(pos + "º");
            tvNum.setTypeface(null, Typeface.BOLD);
            tvNum.setTextColor(getColorRes(R.color.muted));

            LinearLayout info = new LinearLayout(requireContext());
            info.setOrientation(LinearLayout.VERTICAL);
            info.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            TextView tvN = new TextView(requireContext());
            tvN.setText(e.name);
            tvN.setTypeface(null, e.isUser ? Typeface.BOLD : Typeface.NORMAL);
            tvN.setTextColor(getColorRes(R.color.ink));

            TextView tvC = new TextView(requireContext());
            tvC.setText("📍 " + e.city + " · 🔥 " + e.streak + "d");
            tvC.setTextSize(11f);
            tvC.setTextColor(getColorRes(R.color.muted));

            info.addView(tvN);
            info.addView(tvC);

            TextView tvP = new TextView(requireContext());
            tvP.setText(nf.format(e.points));
            tvP.setTypeface(null, Typeface.BOLD);
            tvP.setTextColor(getColorRes(R.color.ink));

            row.addView(tvNum);
            row.addView(info);
            row.addView(tvP);
            tableCard.addView(row);
        }

        root.addView(tableCard);
        return v;
    }

    private int getColorRes(int res) {
        return getResources().getColor(res, requireContext().getTheme());
    }
}
