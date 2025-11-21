package com.pansoul.fucktsn.hooks;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class PrivacyAgreementHook extends BaseHook {

    private boolean hasSetAgreement = false;

    public PrivacyAgreementHook() {
        super("隐私协议自动同意");
    }

    @Override
    protected void hook(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        // Hook SharedPreferences读取（不受360加固影响）
        hookSharedPreferencesRead(lpparam);
        
        // Hook ClassLoader等待LoginUser加载后设置标记
        hookClassLoaderForDelayedSet(lpparam);
    }

    /**
     * Hook SharedPreferences读取agreement键，直接返回true
     * SharedPreferences是Android系统类，不受360加固影响
     */
    private void hookSharedPreferencesRead(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            log("正在Hook SharedPreferencesImpl.getBoolean() - 协议读取");
            
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
                                param.setResult(true);
                                log("[拦截] SharedPreferences读取agreement -> 返回true");
                            }
                        }
                    }
            );
            
            log("SharedPreferences Hook完成");
        } catch (Throwable t) {
            log("[错误] Hook SharedPreferences失败: " + t.getMessage());
        }
    }

    /**
     * Hook ClassLoader.loadClass()，等待LoginUser被加载后设置标记
     * 这样可以绕过360加固的延迟加载
     */
    private void hookClassLoaderForDelayedSet(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            log("正在Hook ClassLoader.loadClass() - 等待LoginUser加载");
            
            XposedHelpers.findAndHookMethod(
                    ClassLoader.class,
                    "loadClass",
                    String.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            if (hasSetAgreement) {
                                return;
                            }
                            
                            String className = (String) param.args[0];
                            
                            // 当LoginUser类被加载时
                            if ("com.bxkj.base.user.LoginUser".equals(className)) {
                                Class<?> loginUserClass = (Class<?>) param.getResult();
                                
                                // 延迟500ms等待类完全初始化
                                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            // 获取LoginUser单例
                                            Object loginUser = XposedHelpers.callStaticMethod(
                                                    loginUserClass,
                                                    "getLoginUser"
                                            );
                                            
                                            // 设置协议标记
                                            XposedHelpers.callMethod(loginUser, "setAgreement", true);
                                            
                                            hasSetAgreement = true;
                                            log("[成功] LoginUser已加载，协议标记已设置为true");
                                        } catch (Throwable t) {
                                            log("[警告] 延迟设置标记失败: " + t.getMessage());
                                        }
                                    }
                                }, 500);
                            }
                        }
                    }
            );
            
            log("ClassLoader Hook完成");
        } catch (Throwable t) {
            log("[错误] Hook ClassLoader失败: " + t.getMessage());
        }
    }
}
