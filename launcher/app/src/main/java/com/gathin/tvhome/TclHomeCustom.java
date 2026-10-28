package com.gathin.tvhome;

import android.app.Activity;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextClock;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/** Tela inicial TCL: nenhuma animacao, nenhum servico, previsao consultada no maximo 1x/h. */
final class TclHomeCustom {
    private static final long WEATHER_INTERVAL_MS = 60L * 60L * 1000L;
    private static final long WEATHER_RETRY_MS = 10L * 60L * 1000L;
    private final Activity activity;
    private final RecyclerView grid;
    private final View nativeClock;
    private final LinearLayout centeredClock;
    private final TextView downHint;
    private final TextView weather;
    // Alvo transparente para a Home ficar sem botao destacado.
    private final View focusParking;
    private final Handler ui = new Handler(Looper.getMainLooper());
    private boolean hidden = true;
    private boolean fetchingWeather;
    private long lastWeatherCheck;

    TclHomeCustom(Activity activity, FrameLayout root, RecyclerView grid,
            View nativeClock, View topButtons) {
        this.activity = activity;
        this.grid = grid;
        this.nativeClock = nativeClock;

        // Uma View sem desenho recebe o foco, em vez do botao Configuracoes.
        focusParking = new View(activity);
        focusParking.setBackgroundColor(Color.TRANSPARENT);
        focusParking.setFocusable(true);
        focusParking.setFocusableInTouchMode(true);
        focusParking.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        FrameLayout.LayoutParams focusParams = new FrameLayout.LayoutParams(
                1, 1, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        root.addView(focusParking, focusParams);

        // O topo direito continua com os botoes de Entradas, Wi-Fi e Configuracoes.
        if (topButtons.getLayoutParams() instanceof RelativeLayout.LayoutParams) {
            RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) topButtons.getLayoutParams();
            p.addRule(RelativeLayout.ALIGN_BOTTOM, 0);
            p.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
            p.topMargin = dp(5);
            topButtons.setLayoutParams(p);
        }
        nativeClock.setAlpha(0f); // Mantem a altura original dos botoes, sem mostrar relogio duplicado.

        weather = new TextView(activity);
        weather.setText("Rio Grande, RS  ·  Carregando clima...");
        weather.setTextColor(Color.WHITE);
        weather.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        weather.setMaxLines(2);
        weather.setEllipsize(TextUtils.TruncateAt.END);
        weather.setShadowLayer(dp(3), 0, dp(1), 0xAA000000);
        weather.setFocusable(false);
        FrameLayout.LayoutParams wp = new FrameLayout.LayoutParams(
                dp(280), ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.TOP | Gravity.START);
        wp.leftMargin = dp(56);
        wp.topMargin = dp(27);
        root.addView(weather, wp);

        centeredClock = new LinearLayout(activity);
        centeredClock.setOrientation(LinearLayout.VERTICAL);
        centeredClock.setGravity(Gravity.CENTER);
        centeredClock.setFocusable(false);
        centeredClock.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);

        TextClock clock = new TextClock(activity);
        clock.setFormat12Hour("HH:mm");
        clock.setFormat24Hour("HH:mm");
        clock.setTextColor(Color.WHITE);
        clock.setTextSize(TypedValue.COMPLEX_UNIT_SP, 56);
        clock.setGravity(Gravity.CENTER);
        clock.setShadowLayer(dp(4), 0, dp(2), 0xAA000000);
        centeredClock.addView(clock, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextClock date = new TextClock(activity);
        date.setFormat12Hour("EEEE, d 'de' MMMM");
        date.setFormat24Hour("EEEE, d 'de' MMMM");
        date.setTextLocale(new Locale("pt", "BR"));
        date.setTextColor(0xFFE0E8F1);
        date.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19);
        date.setGravity(Gravity.CENTER);
        date.setShadowLayer(dp(3), 0, dp(1), 0xAA000000);
        centeredClock.addView(date, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        FrameLayout.LayoutParams centerParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER);
        root.addView(centeredClock, centerParams);

        downHint = new TextView(activity);
        downHint.setText("↓   Pressione para ver os aplicativos");
        downHint.setTextColor(0xFFE0E8F1);
        downHint.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        downHint.setGravity(Gravity.CENTER);
        downHint.setShadowLayer(dp(3), 0, dp(1), 0xAA000000);
        downHint.setFocusable(false);
        FrameLayout.LayoutParams hintParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        hintParams.bottomMargin = dp(45);
        root.addView(downHint, hintParams);
    }

    boolean isHidden() { return hidden; }
    boolean hasParkedFocus() { return focusParking.hasFocus(); }

    void hideApps() {
        hidden = true;
        grid.setVisibility(View.INVISIBLE); // Ainda ocupa espaco; nao refaz a grade.
        grid.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        centeredClock.setVisibility(View.VISIBLE);
        downHint.setVisibility(View.VISIBLE);
        View hero = activity.findViewById(R.id.hero);
        View glass = activity.findViewById(R.id.glass);
        View ambient = activity.findViewById(R.id.ambient);
        View heroArt = activity.findViewById(R.id.heroArt);
        if (hero != null) hero.setAlpha(0f);
        if (glass != null) glass.setAlpha(0f);
        if (ambient != null) ambient.setAlpha(0f);
        if (heroArt != null) heroArt.setAlpha(0f);
        nativeClock.setAlpha(0f);
        // Retira o destaque de HDMI / Wi-Fi / Configuracoes.
        focusParking.requestFocus();
    }

    void showApps() {
        hidden = false;
        grid.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        grid.setVisibility(View.VISIBLE);
        centeredClock.setVisibility(View.GONE);
        downHint.setVisibility(View.GONE);
    }

    void maybeFetchWeather() {
        if (fetchingWeather || System.currentTimeMillis() - lastWeatherCheck < WEATHER_INTERVAL_MS) return;
        fetchingWeather = true;
        lastWeatherCheck = System.currentTimeMillis();
        Thread worker = new Thread(() -> {
            HttpURLConnection con = null;
            String value = null;
            try {
                // Coordenadas aproximadas de Rio Grande, RS. Sem localizacao do usuario.
                URL url = new URL("https://api.open-meteo.com/v1/forecast?latitude=-32.035&longitude=-52.099&current=temperature_2m,weather_code&timezone=America%2FSao_Paulo");
                con = (HttpURLConnection) url.openConnection();
                con.setConnectTimeout(5000);
                con.setReadTimeout(5000);
                con.setRequestProperty("Accept", "application/json");
                try (BufferedReader r = new BufferedReader(
                        new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder b = new StringBuilder();
                    String line;
                    while ((line = r.readLine()) != null && b.length() < 12000) b.append(line);
                    JSONObject now = new JSONObject(b.toString()).getJSONObject("current");
                    double temp = now.getDouble("temperature_2m");
                    int code = now.getInt("weather_code");
                    value = String.format(new Locale("pt", "BR"), "%.0f°C · %s\nRio Grande, RS",
                            temp, description(code));
                }
            } catch (Exception ignored) { /* Rede indisponivel: mantem tela funcional. */ }
            finally { if (con != null) con.disconnect(); }
            final String result = value;
            ui.post(() -> {
                fetchingWeather = false;
                if (activity.isFinishing() || activity.isDestroyed()) return;
                if (result != null) weather.setText(result);
                else {
                    weather.setText("Clima indisponível\nRio Grande, RS");
                    lastWeatherCheck = System.currentTimeMillis() - (WEATHER_INTERVAL_MS - WEATHER_RETRY_MS);
                }
            });
        }, "TCL-clima");
        worker.setDaemon(true);
        worker.start();
    }

    private String description(int code) {
        if (code == 0) return "Céu limpo";
        if (code <= 3) return "Parcialmente nublado";
        if (code == 45 || code == 48) return "Neblina";
        if (code >= 51 && code <= 67) return "Chuva";
        if (code >= 71 && code <= 77) return "Neve";
        if (code >= 80 && code <= 82) return "Pancadas de chuva";
        if (code >= 95) return "Trovoadas";
        return "Tempo variável";
    }

    private int dp(float value) {
        return Math.round(value * activity.getResources().getDisplayMetrics().density);
    }
}
