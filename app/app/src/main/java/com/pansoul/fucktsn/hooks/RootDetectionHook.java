package com.pansoul.fucktsn.hooks;

import android.content.Context;
import android.os.Process;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class RootDetectionHook extends BaseHook {

    // 2.0.25 使用 SecShell 加固后，R8 把安全类重命名到默认包；
    // Transform 仍保留原包名，用于原生签名/正版校验。
    private static final Set<String> MODERN_SAFETY_CLASSES = new HashSet<>(Arrays.asList(
            "ce",          // SafeGuard 启动总控
            "cj4",         // Root 检测
            "tz0",         // 模拟器检测
            "rp1",         // Hook 检测
            "b15",         // TracerPid/断点检测
            "yj0",         // Debug 检测
            "a12",         // 签名校验 Java 包装
            "com.google.android.datatransport.Transform"
    ));

    // 用于记录已经Hook过的类,避免重复Hook（多线程加载时保持线程安全）
    private final Set<String> hookedClasses = Collections.synchronizedSet(new HashSet<String>());

    public RootDetectionHook() {
        super("增强Root/安全检测绕过 (2.0.25)");
    }

    @Override
    protected void hook(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        // 最后一道保险：拦截所有主动退出，避免漏网检测直接把进程杀掉
        hookProcessExit();

        // 使用ClassLoader监听来应对 SecShell/360 加固，同时监听两种loadClass方法
        hookClassLoaderForSafetyChecks(lpparam);
    }

    /**
     * Hook ClassLoader.loadClass 来捕获360加固延迟加载的安全检测类
     * 同时监听单参数和双参数版本的 loadClass
     */
    private void hookClassLoaderForSafetyChecks(XC_LoadPackage.LoadPackageParam lpparam) {
        log("开始监听ClassLoader以应对SecShell/360加固...");
        
        // Hook 单参数版本 loadClass(String)
        XposedHelpers.findAndHookMethod(
                ClassLoader.class,
                "loadClass",
                String.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        handleClassLoaded((String) param.args[0], param);
                    }
                }
        );
        
        // Hook 双参数版本 loadClass(String, boolean)
        XposedHelpers.findAndHookMethod(
                ClassLoader.class,
                "loadClass",
                String.class,
                boolean.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        handleClassLoaded((String) param.args[0], param);
                    }
                }
        );
        
        log("ClassLoader监听已设置");
    }
    
    /**
     * 统一处理类加载事件
     */
    private void handleClassLoaded(String className, XC_MethodHook.MethodHookParam param) {
        // 兼容旧版 com.bxkj.student / 快手 SDK；
        // 2.0.25 的安全类被 R8 重命名到默认包，需要按精确类名匹配。
        boolean legacyTarget = className.startsWith("com.bxkj.student")
                || className.startsWith("com.kuaishou.weapon");
        boolean modernTarget = MODERN_SAFETY_CLASSES.contains(className);
        if (!legacyTarget && !modernTarget) {
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
            switch (className) {
                case "com.bxkj.student.v2.common.utils.safe.g":
                    hookRootDetectionClass(clazz);
                    break;
                case "com.bxkj.student.v2.common.utils.safe.c":
                    hookDebugDetectionClass(clazz);
                    break;
                case "com.bxkj.student.v2.common.utils.safe.d":
                    hookEmulatorDetectionClass(clazz);
                    break;
                case "com.bxkj.student.v2.common.utils.safe.f":
                    hookHookDetectionClass(clazz);
                    break;
                case "com.bxkj.student.v2.common.utils.safe.h":
                    hookTracerPidDetectionClass(clazz);
                    break;

                case "com.bxkj.student.App":
                    hookRealApplicationClass(clazz);
                    break;

                // ===== 2.0.25（SecShell 脱壳后）=====
                case "ce":
                    hookSafeGuardClass(clazz);
                    break;
                case "cj4":
                    hookRootDetectionClass(clazz);
                    break;
                case "tz0":
                    hookEmulatorDetectionClass(clazz);
                    break;
                case "rp1":
                    hookModernHookDetectionClass(clazz);
                    break;
                case "b15":
                    hookTracerPidDetectionClass(clazz);
                    break;
                case "yj0":
                    hookDebugDetectionClass(clazz);
                    break;
                case "a12":
                    hookSignatureWrapperClass(clazz);
                    break;
                case "com.google.android.datatransport.Transform":
                    hookTransformClass(clazz);
                    break;

                case "com.kuaishou.weapon.p0.af":
                    hookKuaishouMagiskClass(clazz);
                    break;
                case "com.kuaishou.weapon.p0.aj":
                    hookKuaishouRootClass(clazz);
                    break;
            }
            
        } catch (Throwable t) {
            // 忽略错误,继续处理其他类
        }
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
            log(" Root检测类Hook成功: " + clazz.getName());
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
            log(" Debug检测类Hook成功: " + clazz.getName());
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
            log(" 模拟器检测类Hook成功: " + clazz.getName());
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
            log(" Hook检测类Hook成功: " + clazz.getName());
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
            log(" TracerPid检测类Hook成功: " + clazz.getName());
        } catch (Throwable t) {
            log("TracerPid检测类Hook失败: " + t.getMessage());
        }
    }

    /**
     * 在真实 Application.onCreate 之前主动加载并 Hook 2.0.25 安全类。
     * 这是 ClassLoader.loadClass 拦截之外的兜底，确保 ce.b() 执行前已经被替换。
     */
    private void hookRealApplicationClass(Class<?> appClass) {
        try {
            XposedHelpers.findAndHookMethod(
                    appClass,
                    "onCreate",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            log("[2.0.25] App.onCreate 前主动加载安全类");
                            hookModernClassesByName(appClass.getClassLoader());
                        }
                    }
            );
            log(" 真实 Application.onCreate Hook成功: " + appClass.getName());
        } catch (Throwable t) {
            log("真实 Application.onCreate Hook失败: " + t.getMessage());
        }
    }

    private void hookModernClassesByName(ClassLoader classLoader) {
        for (String name : MODERN_SAFETY_CLASSES) {
            if (hookedClasses.contains(name)) {
                continue;
            }
            try {
                Class<?> clazz = XposedHelpers.findClass(name, classLoader);
                if (clazz == null) {
                    continue;
                }
                hookedClasses.add(name);
                switch (name) {
                    case "ce":
                        hookSafeGuardClass(clazz);
                        break;
                    case "cj4":
                        hookRootDetectionClass(clazz);
                        break;
                    case "tz0":
                        hookEmulatorDetectionClass(clazz);
                        break;
                    case "rp1":
                        hookModernHookDetectionClass(clazz);
                        break;
                    case "b15":
                        hookTracerPidDetectionClass(clazz);
                        break;
                    case "yj0":
                        hookDebugDetectionClass(clazz);
                        break;
                    case "a12":
                        hookSignatureWrapperClass(clazz);
                        break;
                    case "com.google.android.datatransport.Transform":
                        hookTransformClass(clazz);
                        break;
                }
            } catch (Throwable t) {
                // 某些类可能尚未被加载，交给 loadClass 拦截继续处理
            }
        }
    }

    /**
     * 2.0.25：SafeGuard 启动总控。Hook 掉 b() 后，ce 不再执行
     * 调试/模拟器/Hook/TracerPid/Root/签名 的启动检测。
     */
    private void hookSafeGuardClass(Class<?> clazz) {
        try {
            XposedHelpers.findAndHookMethod(
                    clazz,
                    "b",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            log("[2.0.25] SafeGuard.ce.b() -> 跳过全部启动检测");
                            param.setResult(null);
                        }
                    }
            );
            log(" SafeGuard(ce.b) Hook成功: " + clazz.getName());
        } catch (Throwable t) {
            log("SafeGuard(ce.b) Hook失败: " + t.getMessage());
        }

        // ce.a() 是 SafeGuardThread 的循环体，单独兜底
        try {
            XposedHelpers.findAndHookMethod(
                    clazz,
                    "a",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            log("[2.0.25] SafeGuard.ce.a() -> 阻止后台轮询");
                            param.setResult(null);
                        }
                    }
            );
            log(" SafeGuard(ce.a) Hook成功");
        } catch (Throwable t) {
            log("SafeGuard(ce.a) Hook失败(可忽略): " + t.getMessage());
        }
    }

    /**
     * 2.0.25：签名校验 Java 包装类 a12。
     * a12.a.c(Context) 内部调用 native Transform.d(Context)。
     */
    private void hookSignatureWrapperClass(Class<?> clazz) {
        try {
            XposedHelpers.findAndHookMethod(
                    clazz,
                    "c",
                    Context.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            log("[2.0.25] 签名校验 a12.c(Context) -> 返回true");
                            param.setResult(true);
                        }
                    }
            );
            log(" 签名包装类(a12.c) Hook成功: " + clazz.getName());
        } catch (Throwable t) {
            log("签名包装类(a12.c) Hook失败: " + t.getMessage());
        }
    }

    /**
     * 2.0.25：原生签名/正版校验入口 Transform.d(Context)。
     * 如果 Xposed 能 Hook native 方法，则直接返回 true。
     */
    private void hookTransformClass(Class<?> clazz) {
        try {
            XposedHelpers.findAndHookMethod(
                    clazz,
                    "d",
                    Context.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            log("[2.0.25] Transform.d(Context) -> 返回true");
                            param.setResult(true);
                        }
                    }
            );
            log(" Transform.d 原生签名校验 Hook成功");
        } catch (Throwable t) {
            log("Transform.d Hook失败(由a12.c兜底): " + t.getMessage());
        }
    }

    /**
     * 2.0.25：rp1 的 Hook 检测。
     * a() 检查 /data/local/tmp/frida-server；
     * b() 检查 de.robv.android.xposed.XposedBridge；
     * c() 是 a() || b() 的入口。
     */
    private void hookModernHookDetectionClass(Class<?> clazz) {
        try {
            XposedHelpers.findAndHookMethod(
                    clazz,
                    "c",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            log("[2.0.25] Hook检测 rp1.c() -> 返回false");
                            param.setResult(false);
                        }
                    }
            );
            log(" Hook检测类(rp1.c) Hook成功: " + clazz.getName());
        } catch (Throwable t) {
            log("Hook检测类(rp1.c) Hook失败: " + t.getMessage());
        }

        try {
            XposedHelpers.findAndHookMethod(
                    clazz,
                    "b",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            log("[2.0.25] XposedBridge检测 rp1.b() -> 返回false");
                            param.setResult(false);
                        }
                    }
            );
        } catch (Throwable t) {
            log("rp1.b Hook失败(可忽略): " + t.getMessage());
        }

        try {
            XposedHelpers.findAndHookMethod(
                    clazz,
                    "a",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            log("[2.0.25] frida-server文件检测 rp1.a() -> 返回false");
                            param.setResult(false);
                        }
                    }
            );
        } catch (Throwable t) {
            log("rp1.a Hook失败(可忽略): " + t.getMessage());
        }
    }

    /**
     * 兜底：拦截主动退出，防止漏网检测把进程杀掉。
     */
    private void hookProcessExit() {
        hookExitMethod(System.class, "exit", int.class);
        hookExitMethod(Runtime.class, "exit", int.class);
        hookExitMethod(Runtime.class, "halt", int.class);
        try {
            XposedHelpers.findAndHookMethod(
                    Process.class,
                    "killProcess",
                    int.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            log("[2.0.25] 拦截 Process.killProcess(" + param.args[0] + ")");
                            param.setResult(null);
                        }
                    }
            );
            log(" Process.killProcess Hook成功");
        } catch (Throwable t) {
            log("Process.killProcess Hook失败: " + t.getMessage());
        }
    }

    private void hookExitMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        try {
            Object[] parameterTypesAndCallback = new Object[parameterTypes.length + 1];
            System.arraycopy(parameterTypes, 0, parameterTypesAndCallback, 0, parameterTypes.length);
            parameterTypesAndCallback[parameterTypes.length] = new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    log("[2.0.25] 拦截 " + clazz.getSimpleName() + "." + methodName
                            + "(" + (param.args.length > 0 ? param.args[0] : "") + ")");
                    param.setResult(null);
                }
            };
            XposedHelpers.findAndHookMethod(clazz, methodName, parameterTypesAndCallback);
            log(" " + clazz.getSimpleName() + "." + methodName + " Hook成功");
        } catch (Throwable t) {
            log(clazz.getSimpleName() + "." + methodName + " Hook失败: " + t.getMessage());
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
            log(" 快手Magisk检测类Hook成功");
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

            log(" 快手Root检测类Hook成功(全部10个方法)");
        } catch (Throwable t) {
            log("快手Root检测类Hook失败: " + t.getMessage());
        }
    }
}
