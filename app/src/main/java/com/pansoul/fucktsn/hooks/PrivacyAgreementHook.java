package com.pansoul.fucktsn.hooks;

import android.content.SharedPreferences;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class PrivacyAgreementHook extends BaseHook {

    public PrivacyAgreementHook() {
        super("隐私协议自动同意");
    }

    @Override
    protected void hook(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        hookSharedPreferences(lpparam);
    }

    /**
     * Hook SharedPreferences实现类，当读取"agreement"键时返回true
     */
    private void hookSharedPreferences(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            log("正在Hook SharedPreferencesImpl - 隐私协议状态");
            
          
            Class<?> sharedPrefsImpl = XposedHelpers.findClass(
                    "android.app.SharedPreferencesImpl",
                    lpparam.classLoader
            );
            
            XposedHelpers.findAndHookMethod(
                    sharedPrefsImpl,
                    "getBoolean",
                    String.class,
                    boolean.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            String key = (String) param.args[0];
                            if ("agreement".equals(key)) {
                                log("[拦截] SharedPreferencesImpl.getBoolean(\"agreement\") -> 返回true");
                                param.setResult(true);
                            }
                        }
                    }
            );
            
            log("SharedPreferencesImpl Hook完成");
        } catch (Throwable t) {
            log("[警告] Hook SharedPreferencesImpl失败: " + t.getMessage());
        }
        
     
        try {
            log("正在Hook Activity.onCreate() - 自动点击同意按钮");
            
            XposedHelpers.findAndHookMethod(
                    android.app.Activity.class,
                    "onCreate",
                    android.os.Bundle.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            android.app.Activity activity = (android.app.Activity) param.thisObject;
                            String className = activity.getClass().getName();
                            
                   
                            if (className.contains("PrivacyAgreement") || className.contains("Privacy") || className.contains("Agreement")) {
                                log("[检测] 可能的隐私协议界面: " + className);
                                
                          
                                activity.getWindow().getDecorView().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                        
                                            int agreeId = activity.getResources().getIdentifier("bt_agree", "id", activity.getPackageName());
                                            if (agreeId != 0) {
                                                android.view.View agreeBtn = activity.findViewById(agreeId);
                                                if (agreeBtn != null) {
                                                    agreeBtn.performClick();
                                                    log("[成功] 已自动点击同意按钮");
                                                    return;
                                                }
                                            }
                                            
                                     
                                            try {
                                                XposedHelpers.callMethod(activity, "M");
                                                log("[成功] 已调用M()方法自动同意");
                                            } catch (Throwable ignored) {
                                            }
                                        } catch (Throwable t) {
                                            log("[提示] 自动点击失败: " + t.getMessage());
                                        }
                                    }
                                }, 500);
                            }
                        }
                    }
            );
            
            log("Activity.onCreate() Hook完成");
        } catch (Throwable t) {
            log("[错误] Hook Activity失败: " + t.getMessage());
        }
    }
}

