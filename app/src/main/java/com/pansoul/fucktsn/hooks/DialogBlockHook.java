package com.pansoul.fucktsn.hooks;

import android.content.Context;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class DialogBlockHook extends BaseHook {

    public DialogBlockHook() {
        super("权限检查绕过");
    }

    @Override
    protected void hook(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        hookNotificationPermissionCheck(lpparam);
    }

    /**
     * Hook通知权限检查，让应用认为通知已开启
     */
    private void hookNotificationPermissionCheck(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            log("正在Hook NotificationManagerCompat.areNotificationsEnabled() - 通知权限检查");
            
          
            Class<?> notificationManagerCompatClass = XposedHelpers.findClass(
                    "androidx.core.app.NotificationManagerCompat",
                    lpparam.classLoader
            );
            
            XposedHelpers.findAndHookMethod(
                    notificationManagerCompatClass,
                    "areNotificationsEnabled",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            log("[拦截] 通知权限检查 -> 返回true(已开启)");
                            param.setResult(true);
                        }
                    }
            );
            
            log("NotificationManagerCompat Hook完成");
        } catch (Throwable t) {
            log("[错误] Hook NotificationManagerCompat失败: " + t.getMessage());
            
           
            try {
                log("正在尝试备用方案: Hook NotificationManager.areNotificationsEnabled()");
                
                Class<?> notificationManagerClass = XposedHelpers.findClass(
                        "android.app.NotificationManager",
                        lpparam.classLoader
                );
                
                XposedHelpers.findAndHookMethod(
                        notificationManagerClass,
                        "areNotificationsEnabled",
                        new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                log("[拦截] NotificationManager权限检查 -> 返回true(已开启)");
                                param.setResult(true);
                            }
                        }
                );
                
                log("NotificationManager Hook完成");
            } catch (Throwable t2) {
                log("[错误] 备用方案也失败: " + t2.getMessage());
            }
        }
    }
}

