package com.smith.so2menu;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

public class OverlayService extends Service {

    private WindowManager wm;
    private WindowManager.LayoutParams params;
    private LinearLayout panel;
    private LinearLayout content;
    private LinearLayout tabBar;
    private ScrollView scroll;
    private MenuConfig cfg;
    private boolean collapsed = false;
    private String activeTab = "VISUAL";

    private static final String[] TABS = {"VISUAL", "COMBAT", "MOVE", "MISC"};

    @Override
    public void onCreate() {
        super.onCreate();
        cfg = new MenuConfig(this);
        startForeground(1, buildNotification());
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        buildPanel();
    }

    private Notification buildNotification() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel ch = new NotificationChannel(
                    "so2", "SO2 Menu", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(ch);
        }
        Notification.Builder b = (Build.VERSION.SDK_INT >= 26)
                ? new Notification.Builder(this, "so2")
                : new Notification.Builder(this);
        return b.setContentTitle("SO2 Menu active")
                .setSmallIcon(android.R.drawable.ic_menu_manage)
                .setOngoing(true).build();
    }

    private void buildPanel() {
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 120;
        params.y = 260;

        panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setBackground(roundRect("#0F141C", "#1E3A8A", 2, 16));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setPadding(28, 18, 18, 18);
        header.setGravity(Gravity.CENTER_VERTICAL);

        TextView title = new TextView(this);
        title.setText("SMITH // SO2");
        title.setTextColor(Color.parseColor("#3B82F6"));
        title.setTextSize(16f);
        title.setLetterSpacing(0.08f);
        title.setTypeface(android.graphics.Typeface.MONOSPACE);
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        title.setLayoutParams(tlp);

        TextView collapse = headerBtn("—");
        collapse.setOnClickListener(v -> {
            collapsed = !collapsed;
            scroll.setVisibility(collapsed ? View.GONE : View.VISIBLE);
            tabBar.setVisibility(collapsed ? View.GONE : View.VISIBLE);
            collapse.setText(collapsed ? "+" : "—");
        });
        TextView close = headerBtn("×");
        close.setOnClickListener(v -> stopSelf());

        header.addView(title);
        header.addView(collapse);
        header.addView(close);

        header.setOnTouchListener(new View.OnTouchListener() {
            int sx, sy; float tx, ty;
            @Override public boolean onTouch(View v, MotionEvent e) {
                switch (e.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        sx = params.x; sy = params.y; tx = e.getRawX(); ty = e.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = sx + (int)(e.getRawX() - tx);
                        params.y = sy + (int)(e.getRawY() - ty);
                        wm.updateViewLayout(panel, params);
                        return true;
                }
                return false;
            }
        });

        tabBar = new LinearLayout(this);
        tabBar.setOrientation(LinearLayout.HORIZONTAL);
        tabBar.setPadding(14, 0, 14, 12);
        for (String t : TABS) {
            TextView tab = tabButton(t);
            tab.setOnClickListener(v -> { activeTab = t; refreshTabs(); rebuildContent(); });
            tabBar.addView(tab);
        }

        scroll = new ScrollView(this);
        scroll.setLayoutParams(new LinearLayout.LayoutParams(740,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        scroll.setPadding(18, 0, 18, 18);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(content);

        TextView status = new TextView(this);
        status.setText("hook: " + (HookBridge.isAttached() ? "attached" : "not attached"));
        status.setTextColor(Color.parseColor(
                HookBridge.isAttached() ? "#3B82F6" : "#6B7280"));
        status.setTextSize(11f);
        status.setPadding(28, 0, 0, 16);

        panel.addView(header);
        panel.addView(tabBar);
        panel.addView(scroll);
        panel.addView(status);

        refreshTabs();
        rebuildContent();

        wm.addView(panel, params);
    }

    private TextView headerBtn(String s) {
        TextView tv = new TextView(this);
        tv.setText(s);
        tv.setTextColor(Color.parseColor("#E5E7EB"));
        tv.setTextSize(20f);
        tv.setPadding(28, 0, 10, 0);
        return tv;
    }

    private TextView tabButton(String label) {
        TextView tv = new TextView(this);
        tv.setText(label);
        tv.setTextSize(11f);
        tv.setTypeface(android.graphics.Typeface.MONOSPACE);
        tv.setLetterSpacing(0.05f);
        tv.setPadding(22, 14, 22, 14);
        tv.setTag(label);
        return tv;
    }

    private void refreshTabs() {
        for (int i = 0; i < tabBar.getChildCount(); i++) {
            TextView t = (TextView) tabBar.getChildAt(i);
            boolean active = t.getTag().equals(activeTab);
            t.setTextColor(Color.parseColor(active ? "#3B82F6" : "#6B7280"));
            t.setBackground(active
                    ? roundRect("#121826", "#3B82F6", 1, 8)
                    : roundRect("#0F141C", "#00000000", 0, 8));
        }
    }

    private void rebuildContent() {
        content.removeAllViews();
        switch (activeTab) {
            case "VISUAL":
                addToggle("esp_box", "ESP — Box");
                addToggle("esp_chams", "Chams");
                addToggle("esp_skeleton", "Skeleton");
                addToggle("esp_health", "Health bar");
                addToggle("esp_name", "Name tags");
                addToggle("esp_distance", "Distance");
                break;
            case "COMBAT":
                addToggle("aim_enable", "Aimbot");
                addToggle("aim_silent", "Silent aim");
                addToggle("aim_trigger", "Trigger bot");
                addToggle("no_recoil", "No recoil");
                addToggle("no_spread", "No spread");
                addSlider("aim_fov", "FOV", 10, 180, 90);
                addSlider("aim_smooth", "Smooth", 1, 20, 5);
                break;
            case "MOVE":
                addToggle("move_speed", "Speed hack");
                addSlider("move_speed_mul", "Speed ×100", 100, 300, 100);
                addToggle("move_jump", "Jump boost");
                addToggle("move_fly", "Fly");
                break;
            case "MISC":
                addToggle("misc_skins", "Unlock skins");
                addToggle("misc_flash", "No flash");
                addToggle("misc_radar", "Radar");
                addToggle("misc_nosmoke", "No smoke");
                break;
        }
    }

    private void addToggle(String key, String label) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(18, 12, 18, 12);
        row.setBackground(roundRect("#121826", "#00000000", 0, 10));
        LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rlp.setMargins(0, 0, 0, 8);
        row.setLayoutParams(rlp);

        TextView tv = new TextView(this);
        tv.setText(label);
        tv.setTextColor(Color.parseColor("#E5E7EB"));
        tv.setTextSize(13f);
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        tv.setLayoutParams(tlp);

        Switch sw = new Switch(this);
        sw.setChecked(cfg.getBool(key));
        sw.getThumbDrawable().setTint(Color.parseColor("#3B82F6"));
        sw.getTrackDrawable().setTint(Color.parseColor(
                sw.isChecked() ? "#1E3A8A" : "#1F2937"));
        sw.setOnCheckedChangeListener((b, checked) -> {
            cfg.setBool(key, checked);
            HookBridge.setToggle(key, checked);
            b.getTrackDrawable().setTint(Color.parseColor(
                    checked ? "#1E3A8A" : "#1F2937"));
        });

        row.addView(tv);
        row.addView(sw);
        content.addView(row);
    }

    private void addSlider(String key, String label, int min, int max, int def) {
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(18, 12, 18, 16);
        col.setBackground(roundRect("#121826", "#00000000", 0, 10));
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        clp.setMargins(0, 0, 0, 8);
        col.setLayoutParams(clp);

        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);

        TextView tv = new TextView(this);
        tv.setText(label);
        tv.setTextColor(Color.parseColor("#E5E7EB"));
        tv.setTextSize(13f);
        tv.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView val = new TextView(this);
        int cur = cfg.getInt(key, def);
        val.setText(String.valueOf(cur));
        val.setTextColor(Color.parseColor("#3B82F6"));
        val.setTextSize(13f);
        val.setTypeface(android.graphics.Typeface.MONOSPACE);

        head.addView(tv);
        head.addView(val);

        SeekBar bar = new SeekBar(this);
        bar.setMax(max - min);
        bar.setProgress(cur - min);
        bar.getProgressDrawable().setTint(Color.parseColor("#3B82F6"));
        bar.getThumb().setTint(Color.parseColor("#3B82F6"));
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int p, boolean fromUser) {
                int v = min + p;
                val.setText(String.valueOf(v));
                if (fromUser) {
                    cfg.setInt(key, v);
                    HookBridge.setInt(key, v);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        col.addView(head);
        col.addView(bar);
        content.addView(col);
    }

    private GradientDrawable roundRect(String fill, String stroke, int strokeDp, int radiusDp) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(Color.parseColor(fill));
        d.setCornerRadius(radiusDp * getResources().getDisplayMetrics().density);
        if (strokeDp > 0)
            d.setStroke(strokeDp, Color.parseColor(stroke));
        return d;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (panel != null && wm != null) wm.removeView(panel);
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
