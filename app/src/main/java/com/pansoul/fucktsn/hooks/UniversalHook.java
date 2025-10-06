package com.pansoul.fucktsn.hooks;

import android.provider.Settings;

import java.io.File;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class UniversalHook extends BaseHook {

    public UniversalHook() {
        super("通用检测绕过");
    }

    @Override
    protected void hook(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        hookDeveloperOptions(lpparam);
        hookFileExists(lpparam);
        hookRuntimeExec(lpparam);
    }

    private void hookDeveloperOptions(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            log("正在Hook Settings.Secure.getInt() - 开发者选项检测");
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
                                log("[拦截] 开发者选项检测 -> 返回0");
                                param.setResult(0);
                            }
                        }
                    }
            );
            log("开发者选项Hook完成");
        } catch (Throwable t) {
            log("开发者选项Hook失败: " + t.getMessage());
        }
    }

    private void hookFileExists(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            log("正在Hook File.exists() - Root文件检测");
            XposedHelpers.findAndHookMethod(
                    File.class,
                    "exists",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            File file = (File) param.thisObject;
                            String path = file.getAbsolutePath();
                            
              
                            if (path.contains("/su") || 
                                path.contains("magisk") ||
                                path.contains("Superuser.apk") ||
                                path.contains("frida-server")) {
                                log("[拦截] Root文件检测: " + path + " -> 返回false");
                                param.setResult(false);
                            }
                        }
                    }
            );
            log("文件检测Hook完成");
        } catch (Throwable t) {
            log("文件检测Hook失败: " + t.getMessage());
        }
    }

    private void hookRuntimeExec(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            log("正在Hook Runtime.exec() - 命令执行检测");
            XposedHelpers.findAndHookMethod(
                    Runtime.class,
                    "exec",
                    String.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            String cmd = (String) param.args[0];
                            if (cmd != null && (cmd.contains("su") || cmd.contains("which su") || cmd.contains("busybox"))) {
                                log("[拦截] 危险命令: " + cmd + " -> 抛出异常");
                                throw new Exception("Command not found");
                            }
                        }
                    }
            );
            
            XposedHelpers.findAndHookMethod(
                    Runtime.class,
                    "exec",
                    String[].class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            String[] cmds = (String[]) param.args[0];
                            if (cmds != null && cmds.length > 0) {
                                String cmd = String.join(" ", cmds);
                                if (cmd.contains("su") || cmd.contains("which") || cmd.contains("busybox")) {
                                    log("[拦截] 危险命令数组: " + cmd + " -> 抛出异常");
                                    throw new Exception("Command not found");
                                }
                            }
                        }
                    }
            );
            log("命令执行Hook完成");
        } catch (Throwable t) {
            log("命令执行Hook失败: " + t.getMessage());
        }
    }
}

