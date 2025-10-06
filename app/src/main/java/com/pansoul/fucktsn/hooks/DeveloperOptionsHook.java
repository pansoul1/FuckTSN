package com.pansoul.fucktsn.hooks;

import android.provider.Settings;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class DeveloperOptionsHook extends BaseHook {

    public DeveloperOptionsHook() {
        super("开发者选项检测绕过");
    }

    @Override
    protected void hook(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        
        log("正在Hook Settings.Secure.getInt()...");
        XposedHelpers.findAndHookMethod(
                Settings.Secure.class,
                "getInt",
                android.content.ContentResolver.class,
                String.class,
                int.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        String key = (String) param.args[1];
                        if ("development_settings_enabled".equals(key)) {
                            log("[拦截] 开发者选项检测 -> 返回0(未开启)");
                            param.setResult(0);
                        }
                    }
                }
        );
        log("Settings.Secure.getInt() Hook完成");

        try {
            log("正在Hook应用检测类 com.bxkj.student.v2.ui.sports.safe.b...");
            Class<?> safeBClass = XposedHelpers.findClass(
                    "com.bxkj.student.v2.ui.sports.safe.b",
                    lpparam.classLoader
            );
            
            XposedHelpers.findAndHookMethod(
                    safeBClass,
                    "b",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            log("[拦截] 应用本体开发者检测 -> 返回false");
                            param.setResult(false);
                        }
                    }
            );
            log("应用检测类Hook完成");
        } catch (Throwable t) {
            log("备用方案Hook失败(可能是混淆/类不存在): " + t.getMessage());
        }
    }
}

