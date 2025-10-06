package com.pansoul.fucktsn.hooks;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class RootDetectionHook extends BaseHook {

    public RootDetectionHook() {
        super("Root检测绕过");
    }

    @Override
    protected void hook(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        hookKuaishouMagiskDetection(lpparam);
        hookKuaishouRootDetection(lpparam);
        hookAppRootDetection(lpparam);
        hookAppDebugDetection(lpparam);
        hookAppEmulatorDetection(lpparam);
        hookAppHookDetection(lpparam);
        hookAppTracerPidDetection(lpparam);
    }

    private void hookKuaishouMagiskDetection(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            log("正在Hook快手Magisk检测 com.kuaishou.weapon.p0.af...");
            Class<?> afClass = XposedHelpers.findClass(
                    "com.kuaishou.weapon.p0.af",
                    lpparam.classLoader
            );

            XposedHelpers.findAndHookMethod(
                    afClass,
                    "a",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            log("[拦截] 快手Magisk挂载点检测 -> 返回0");
                            param.setResult(0);
                        }
                    }
            );

            log("快手Magisk检测Hook完成");
        } catch (Throwable t) {
            log("快手Magisk检测Hook失败: " + t.getMessage());
        }
    }

    private void hookKuaishouRootDetection(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            Class<?> ajClass = XposedHelpers.findClass(
                    "com.kuaishou.weapon.p0.aj",
                    lpparam.classLoader
            );

            XposedHelpers.findAndHookMethod(
                    ajClass,
                    "a",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            log("拦截su文件检测");
                            param.setResult(0);
                        }
                    }
            );

       
            XposedHelpers.findAndHookMethod(
                    ajClass,
                    "c",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            log("拦截su版本检测");
                            param.setResult(null);
                        }
                    }
            );

       
            XposedHelpers.findAndHookMethod(
                    ajClass,
                    "d",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            log("拦截ro.secure检测");
                            param.setResult(1);
                        }
                    }
            );

    
            XposedHelpers.findAndHookMethod(
                    ajClass,
                    "e",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            log("拦截ro.debuggable检测");
                            param.setResult(1);
                        }
                    }
            );

       
            XposedHelpers.findAndHookMethod(
                    ajClass,
                    "f",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            log("拦截ro.adb.secure检测");
                            param.setResult(1);
                        }
                    }
            );

    
            XposedHelpers.findAndHookMethod(
                    ajClass,
                    "g",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            log("拦截Root命令执行检测");
                            param.setResult(null);
                        }
                    }
            );

    
            XposedHelpers.findAndHookMethod(
                    ajClass,
                    "h",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            log("拦截Superuser.apk检测");
                            param.setResult(0);
                        }
                    }
            );


            XposedHelpers.findAndHookMethod(
                    ajClass,
                    "i",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            log("拦截magisk文件路径检测");
                            param.setResult("");
                        }
                    }
            );

        
            XposedHelpers.findAndHookMethod(
                    ajClass,
                    "j",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            log("拦截su文件路径检测");
                            param.setResult("");
                        }
                    }
            );

  
            XposedHelpers.findAndHookMethod(
                    ajClass,
                    "k",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            log("拦截Xposed堆栈检测");
                            param.setResult("");
                        }
                    }
            );

            log("Root检测已全面绕过");
        } catch (Throwable t) {
            log("快手Root检测Hook失败(可能是混淆): " + t.getMessage());
        }
    }

    private void hookAppRootDetection(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            Class<?> rootCheckClass = XposedHelpers.findClass(
                    "com.bxkj.student.v2.common.utils.safe.g",
                    lpparam.classLoader
            );

            XposedHelpers.findAndHookMethod(
                    rootCheckClass,
                    "d",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            log("拦截应用本体Root检测");
                            param.setResult(false);
                        }
                    }
            );

            log("应用Root检测已绕过");
        } catch (Throwable t) {
            log("应用Root检测Hook失败: " + t.getMessage());
        }
    }

    private void hookAppDebugDetection(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            Class<?> debugCheckClass = XposedHelpers.findClass(
                    "com.bxkj.student.v2.common.utils.safe.c",
                    lpparam.classLoader
            );

            XposedHelpers.findAndHookMethod(
                    debugCheckClass,
                    "d",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            log("拦截调试检测");
                            param.setResult(false);
                        }
                    }
            );

            log("调试检测已绕过");
        } catch (Throwable t) {
            log("调试检测Hook失败: " + t.getMessage());
        }
    }

    private void hookAppEmulatorDetection(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            Class<?> emulatorCheckClass = XposedHelpers.findClass(
                    "com.bxkj.student.v2.common.utils.safe.d",
                    lpparam.classLoader
            );

            XposedHelpers.findAndHookMethod(
                    emulatorCheckClass,
                    "a",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            log("拦截模拟器检测");
                            param.setResult(false);
                        }
                    }
            );

            log("模拟器检测已绕过");
        } catch (Throwable t) {
            log("模拟器检测Hook失败: " + t.getMessage());
        }
    }

    private void hookAppHookDetection(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            Class<?> hookCheckClass = XposedHelpers.findClass(
                    "com.bxkj.student.v2.common.utils.safe.f",
                    lpparam.classLoader
            );

            XposedHelpers.findAndHookMethod(
                    hookCheckClass,
                    "c",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            log("拦截Hook框架检测(Xposed/Frida)");
                            param.setResult(false);
                        }
                    }
            );

            log("Hook框架检测已绕过");
        } catch (Throwable t) {
            log("Hook框架检测Hook失败: " + t.getMessage());
        }
    }

    private void hookAppTracerPidDetection(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            Class<?> tracerCheckClass = XposedHelpers.findClass(
                    "com.bxkj.student.v2.common.utils.safe.h",
                    lpparam.classLoader
            );

            XposedHelpers.findAndHookMethod(
                    tracerCheckClass,
                    "a",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            log("拦截TracerPid检测(反调试)");
                            param.setResult(false);
                        }
                    }
            );

            log("TracerPid检测已绕过");
        } catch (Throwable t) {
            log("TracerPid检测Hook失败: " + t.getMessage());
        }
    }
}

