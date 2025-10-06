package com.pansoul.fucktsn.utils;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LogManager {
    
    private static final String TAG = "FuckTSN";
    private static final String LOG_DIR = "FuckTSN";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
    
    private static File logFile;
    private static boolean initialized = false;
    private static boolean isXposedEnvironment = false;

    static {
        try {
            Class.forName("de.robv.android.xposed.XposedBridge");
            isXposedEnvironment = true;
        } catch (ClassNotFoundException e) {
            isXposedEnvironment = false;
        }
    }

    public static void init(Context context) {
        if (initialized) return;
        try {
            File logDir = new File(context.getExternalFilesDir(null), LOG_DIR);
            if (!logDir.exists()) {
                logDir.mkdirs();
            }
            String fileName = "hook_log_" + new SimpleDateFormat("yyyyMMdd", Locale.CHINA).format(new Date()) + ".txt";
            logFile = new File(logDir, fileName);
            initialized = true;
            i("日志模块初始化成功: " + logFile.getAbsolutePath());
        } catch (Exception e) {
            e("日志模块初始化失败: " + e.getMessage());
        }
    }

    public static void d(String message) {
        log("DEBUG", message);
    }

    public static void i(String message) {
        log("INFO", message);
    }

    public static void w(String message) {
        log("WARN", message);
    }

    public static void e(String message) {
        log("ERROR", message);
    }

    private static void log(String level, String message) {
        String logMessage = String.format("[%s] [%s] %s", DATE_FORMAT.format(new Date()), level, message);
        
        if (isXposedEnvironment) {
            try {
                Class<?> xposedBridge = Class.forName("de.robv.android.xposed.XposedBridge");
                xposedBridge.getMethod("log", String.class).invoke(null, TAG + ": " + logMessage);
            } catch (Exception ignored) {
            }
        } else {
            Log.i(TAG, logMessage);
        }
        
        autoInitIfNeeded();
        
        if (logFile != null && logFile.getParentFile() != null && logFile.getParentFile().exists()) {
            try (FileWriter writer = new FileWriter(logFile, true)) {
                writer.write(logMessage + "\n");
                writer.flush();
            } catch (IOException e) {
                Log.e(TAG, "写入日志文件失败: " + e.getMessage());
            }
        }
    }
    
    private static void autoInitIfNeeded() {
        if (initialized || !isXposedEnvironment) {
            return;
        }
        
        try {
            Context context = getApplicationContext();
            if (context != null) {
                init(context);
            }
        } catch (Exception e) {
            Log.e(TAG, "自动初始化失败: " + e.getMessage());
        }
    }
    
    private static Context getApplicationContext() {
        try {
            Class<?> activityThread = Class.forName("android.app.ActivityThread");
            Object currentActivityThread = activityThread.getMethod("currentActivityThread").invoke(null);
            if (currentActivityThread != null) {
                Object app = activityThread.getMethod("getApplication").invoke(currentActivityThread);
                if (app != null) {
                    return (Context) app;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "获取Context失败: " + e.getMessage());
        }
        return null;
    }

    public static File getLogFile() {
        return logFile;
    }

    public static String getLogPath() {
        return logFile != null ? logFile.getAbsolutePath() : "日志文件未初始化";
    }
}

