package com.smith.so2menu;

import android.content.Context;
import android.content.SharedPreferences;

public class MenuConfig {
    private final SharedPreferences sp;

    public MenuConfig(Context c) {
        sp = c.getSharedPreferences("so2_menu", Context.MODE_PRIVATE);
    }
    public boolean getBool(String k) { return sp.getBoolean(k, false); }
    public void setBool(String k, boolean v) { sp.edit().putBoolean(k, v).apply(); }
    public int getInt(String k, int def) { return sp.getInt(k, def); }
    public void setInt(String k, int v) { sp.edit().putInt(k, v).apply(); }
}
