package com.smith.so2menu;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 101);
        }

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setBackgroundColor(Color.parseColor("#0B0E14"));
        root.setPadding(64, 64, 64, 64);

        Button perm = styledButton("Grant overlay permission");
        perm.setOnClickListener(v -> {
            Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivity(i);
        });

        Button start = styledButton("Start menu");
        start.setOnClickListener(v -> {
            if (!Settings.canDrawOverlays(this)) return;
            startService(new Intent(this, OverlayService.class));
            finish();
        });

        Button stop = styledButton("Stop menu");
        stop.setOnClickListener(v -> stopService(new Intent(this, OverlayService.class)));

        root.addView(perm);
        root.addView(start);
        root.addView(stop);
        setContentView(root);
    }

    private Button styledButton(String text) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextColor(Color.parseColor("#E5E7EB"));
        btn.setBackgroundColor(Color.parseColor("#1E3A8A"));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 20, 0, 0);
        btn.setLayoutParams(lp);
        return btn;
    }
}
