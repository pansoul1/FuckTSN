package com.pansoul.fucktsn.hooks;

import android.app.Activity;
import android.os.CountDownTimer;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class AdSkipHook extends BaseHook {

    public AdSkipHook() {
        super("启动页广告跳过");
    }

    @Override
    protected void hook(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        hookSplashActivity(lpparam);
        hookCountDownTimer(lpparam);
    }

    /**
     * Hook启动页Activity，自动跳过广告
     */
    private void hookSplashActivity(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            log("正在Hook Activity.onCreate() - 检测启动页广告");
            
            XposedHelpers.findAndHookMethod(
                    Activity.class,
                    "onCreate",
                    android.os.Bundle.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            Activity activity = (Activity) param.thisObject;
                            String className = activity.getClass().getName();
                            
                          
                            if (className.contains("Splash") || className.contains("Welcome")) {
                                log("[检测] 启动页广告界面: " + className);
                                
                             
                                activity.getWindow().getDecorView().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                          
                                            XposedHelpers.callMethod(param.thisObject, "S");
                                            log("[成功] 已调用S()方法跳过广告");
                                        } catch (Throwable t1) {
                                           
                                            try {
                                                Object countDownTimer = XposedHelpers.getObjectField(param.thisObject, "f21215i");
                                                if (countDownTimer != null) {
                                                    XposedHelpers.callMethod(countDownTimer, "onFinish");
                                                    log("[成功] 已触发倒计时完成");
                                                }
                                            } catch (Throwable t2) {
                                                log("[提示] 跳过广告失败: " + t1.getMessage() + " | " + t2.getMessage());
                                            }
                                        }
                                    }
                                }, 200);
                            }
                        }
                    }
            );
            
            log("启动页Activity Hook完成");
        } catch (Throwable t) {
            log("[错误] Hook启动页Activity失败: " + t.getMessage());
        }
    }

    /**
     * Hook倒计时器，让广告倒计时立即完成
     */
    private void hookCountDownTimer(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            log("正在Hook CountDownTimer.start() - 跳过倒计时");
            
            XposedHelpers.findAndHookMethod(
                    CountDownTimer.class,
                    "start",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                          
                            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
                            boolean isFromSplash = false;
                            
                            for (StackTraceElement element : stackTrace) {
                                String className = element.getClassName();
                                if (className.contains("Splash") || className.contains("Welcome")) {
                                    isFromSplash = true;
                                    break;
                                }
                            }
                            
                            if (isFromSplash) {
                                log("[拦截] 启动页倒计时开始 -> 立即触发完成");
                               
                                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            CountDownTimer timer = (CountDownTimer) param.thisObject;
                                            timer.onFinish();
                                            timer.cancel();
                                            log("[成功] 倒计时已立即完成");
                                        } catch (Throwable t) {
                                            log("[提示] 触发倒计时完成失败: " + t.getMessage());
                                        }
                                    }
                                }, 100);
                            }
                        }
                    }
            );
            
            log("CountDownTimer Hook完成");
        } catch (Throwable t) {
            log("[错误] Hook CountDownTimer失败: " + t.getMessage());
        }
    }
}

