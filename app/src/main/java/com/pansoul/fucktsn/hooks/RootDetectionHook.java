package com.pansoul.fucktsn.hooks;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import java.util.HashSet;
import java.util.Set;

public class RootDetectionHook extends BaseHook {

    // 用于记录已经Hook过的类,避免重复Hook
    private final Set<String> hookedClasses = new HashSet<>();

    public RootDetectionHook() {
        super("增强Root/安全检测绕过");
    }

    @Override
    protected void hook(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        // 使用ClassLoader监听来应对360加固
        hookClassLoaderForSafetyChecks(lpparam);
    }

    /**
     * Hook ClassLoader.loadClass 来捕获360加固延迟加载的安全检测类
     */
    private void hookClassLoaderForSafetyChecks(XC_LoadPackage.LoadPackageParam lpparam) {
        log("开始监听ClassLoader以应对360加固...");
        
        XposedHelpers.findAndHookMethod(
                ClassLoader.class,
                "loadClass",
                String.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        String className = (String) param.args[0];
                        
                        // 只处理目标应用的类
                        if (!className.startsWith("com.bxkj.student") && 
                            !className.startsWith("com.kuaishou.weapon")) {
                            return;
                        }
                        
                        // 避免重复Hook
                        if (hookedClasses.contains(className)) {
                            return;
                        }
                        
                        try {
                            Class<?> clazz = (Class<?>) param.getResult();
                            if (clazz == null) {
                                return;
                            }
                            
                            // 标记为已处理
                            hookedClasses.add(className);
                            
                            // 根据类名判断要Hook的内容
                            if (className.equals("com.bxkj.student.v2.common.utils.safe.g")) {
                                hookRootDetectionClass(clazz);
                            } else if (className.equals("com.bxkj.student.v2.common.utils.safe.c")) {
                                hookDebugDetectionClass(clazz);
                            } else if (className.equals("com.bxkj.student.v2.common.utils.safe.d")) {
                                hookEmulatorDetectionClass(clazz);
                            } else if (className.equals("com.bxkj.student.v2.common.utils.safe.f")) {
                                hookHookDetectionClass(clazz);
                            } else if (className.equals("com.bxkj.student.v2.common.utils.safe.h")) {
                                hookTracerPidDetectionClass(clazz);
                            } else if (className.equals("com.kuaishou.weapon.p0.af")) {
                                hookKuaishouMagiskClass(clazz);
                            } else if (className.equals("com.kuaishou.weapon.p0.aj")) {
                                hookKuaishouRootClass(clazz);
                            }
                            
                        } catch (Throwable t) {
                            // 忽略错误,继续处理其他类
                        }
                    }
                }
        );
        
        log("ClassLoader监听已设置");
    }

    /**
     * Hook Root检测类
     */
    private void hookRootDetectionClass(Class<?> clazz) {
        try {
            XposedHelpers.findAndHookMethod(
                    clazz,
                    "d",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            log("[ClassLoader拦截] 应用本体Root检测 -> 返回false");
                            param.setResult(false);
                        }
                    }
            );
            log("✓ Root检测类Hook成功: " + clazz.getName());
        } catch (Throwable t) {
            log("Root检测类Hook失败: " + t.getMessage());
        }
    }

    /**
     * Hook Debug检测类
     */
    private void hookDebugDetectionClass(Class<?> clazz) {
        try {
            XposedHelpers.findAndHookMethod(
                    clazz,
                    "d",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            log("[ClassLoader拦截] 调试检测 -> 返回false");
                            param.setResult(false);
                        }
                    }
            );
            log("✓ Debug检测类Hook成功: " + clazz.getName());
        } catch (Throwable t) {
            log("Debug检测类Hook失败: " + t.getMessage());
        }
    }

    /**
     * Hook 模拟器检测类
     */
    private void hookEmulatorDetectionClass(Class<?> clazz) {
        try {
            XposedHelpers.findAndHookMethod(
                    clazz,
                    "a",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            log("[ClassLoader拦截] 模拟器检测 -> 返回false");
                            param.setResult(false);
                        }
                    }
            );
            log("✓ 模拟器检测类Hook成功: " + clazz.getName());
        } catch (Throwable t) {
            log("模拟器检测类Hook失败: " + t.getMessage());
        }
    }

    /**
     * Hook Hook框架检测类
     */
    private void hookHookDetectionClass(Class<?> clazz) {
        try {
            XposedHelpers.findAndHookMethod(
                    clazz,
                    "c",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            log("[ClassLoader拦截] Hook框架检测 -> 返回false");
                            param.setResult(false);
                        }
                    }
            );
            log("✓ Hook检测类Hook成功: " + clazz.getName());
        } catch (Throwable t) {
            log("Hook检测类Hook失败: " + t.getMessage());
        }
    }

    /**
     * Hook TracerPid检测类
     */
    private void hookTracerPidDetectionClass(Class<?> clazz) {
        try {
            XposedHelpers.findAndHookMethod(
                    clazz,
                    "a",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            log("[ClassLoader拦截] TracerPid检测 -> 返回false");
                            param.setResult(false);
                        }
                    }
            );
            log("✓ TracerPid检测类Hook成功: " + clazz.getName());
        } catch (Throwable t) {
            log("TracerPid检测类Hook失败: " + t.getMessage());
        }
    }

    /**
     * Hook 快手SDK Magisk检测类
     */
    private void hookKuaishouMagiskClass(Class<?> clazz) {
        try {
            XposedHelpers.findAndHookMethod(
                    clazz,
                    "a",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            log("[ClassLoader拦截] 快手Magisk检测 -> 返回0");
                            param.setResult(0);
                        }
                    }
            );
            log("✓ 快手Magisk检测类Hook成功");
        } catch (Throwable t) {
            log("快手Magisk检测类Hook失败: " + t.getMessage());
        }
    }

    /**
     * Hook 快手SDK Root检测类
     */
    private void hookKuaishouRootClass(Class<?> clazz) {
        try {
            // su文件检测
            XposedHelpers.findAndHookMethod(
                    clazz,
                    "a",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            log("[ClassLoader拦截] 快手su文件检测 -> 返回0");
                            param.setResult(0);
                        }
                    }
            );

            // su版本检测
            XposedHelpers.findAndHookMethod(
                    clazz,
                    "c",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            log("[ClassLoader拦截] 快手su版本检测 -> 返回null");
                            param.setResult(null);
                        }
                    }
            );

            // ro.secure检测
            XposedHelpers.findAndHookMethod(
                    clazz,
                    "d",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            param.setResult(1);
                        }
                    }
            );

            // ro.debuggable检测
            XposedHelpers.findAndHookMethod(
                    clazz,
                    "e",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            param.setResult(1);
                        }
                    }
            );

            // ro.adb.secure检测
            XposedHelpers.findAndHookMethod(
                    clazz,
                    "f",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            param.setResult(1);
                        }
                    }
            );

            // Root命令执行检测
            XposedHelpers.findAndHookMethod(
                    clazz,
                    "g",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            log("[ClassLoader拦截] 快手Root命令检测 -> 返回null");
                            param.setResult(null);
                        }
                    }
            );

            // Superuser.apk检测
            XposedHelpers.findAndHookMethod(
                    clazz,
                    "h",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            param.setResult(0);
                        }
                    }
            );

            // magisk文件路径检测
            XposedHelpers.findAndHookMethod(
                    clazz,
                    "i",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            param.setResult("");
                        }
                    }
            );

            // su文件路径检测
            XposedHelpers.findAndHookMethod(
                    clazz,
                    "j",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            param.setResult("");
                        }
                    }
            );

            // Xposed堆栈检测
            XposedHelpers.findAndHookMethod(
                    clazz,
                    "k",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            log("[ClassLoader拦截] 快手Xposed堆栈检测 -> 返回空字符串");
                            param.setResult("");
                        }
                    }
            );

            log("✓ 快手Root检测类Hook成功(全部10个方法)");
        } catch (Throwable t) {
            log("快手Root检测类Hook失败: " + t.getMessage());
        }
    }
}
