package com.smith.so2menu;

public final class HookBridge {
    private static boolean loaded = false;
    static {
        try { System.loadLibrary("so2hook"); loaded = true; }
        catch (Throwable t) { loaded = false; }
    }
    public static boolean isAttached() { return loaded; }
    public static void setToggle(String key, boolean value) {
        if (loaded) nativeSetToggle(key, value);
    }
    public static void setInt(String key, int value) {
        if (loaded) nativeSetInt(key, value);
    }
    private static native void nativeSetToggle(String key, boolean value);
    private static native void nativeSetInt(String key, int value);
}
