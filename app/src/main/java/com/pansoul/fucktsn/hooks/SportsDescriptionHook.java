package com.pansoul.fucktsn.hooks;

import android.app.Activity;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class SportsDescriptionHook extends BaseHook {

    public SportsDescriptionHook() {
        super("运动启动流程自动跳过");
    }

    @Override
    protected void hook(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        hookSportsDescriptionActivity(lpparam);
        hookV4SportActivity(lpparam);
    }

    /**
     * Hook 锻炼说明页面，直接调用完成方法（适配360加固）
     */
    private void hookSportsDescriptionActivity(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            log("正在Hook Activity.onCreate - 检测锻炼说明页面");
            
            XposedHelpers.findAndHookMethod(
                Activity.class,
                "onCreate",
                android.os.Bundle.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
                        final Activity activity = (Activity) param.thisObject;
                        String className = activity.getClass().getName();
                        
                        if (className.contains("SportsDescriptionActivity")) {
                            log("[检测] 发现锻炼说明页面: " + className);
                            
                            // 直接调用完成方法，不模拟UI操作
                            activity.getWindow().getDecorView().post(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        // 先设置"不再提示"配置为true
                                        try {
                                            Class<?> dataClass = XposedHelpers.findClass(
                                                "com.bxkj.base.v2.data.a",
                                                lpparam.classLoader
                                            );
                                            Object dataInstance = XposedHelpers.getStaticObjectField(dataClass, "f16779d");
                                            Object configInstance = XposedHelpers.callMethod(dataInstance, "a");
                                            XposedHelpers.callMethod(configInstance, "C0", true);
                                            log("[成功] 已设置'不再提示'配置");
                                        } catch (Throwable t) {
                                            log("[警告] 设置配置失败，但继续执行: " + t.getMessage());
                                        }
                                        
                                        // 直接调用M()方法（原本是点击按钮后调用的）
                                        XposedHelpers.callMethod(activity, "M");
                                        log("[成功] 已直接调用M()方法，跳过说明页");
                                    } catch (Throwable t) {
                                        log("[错误] 调用M()失败: " + t.getMessage());
                                    }
                                }
                            });
                        }
                    }
                }
            );
            
            log("锻炼说明页面Hook完成");
        } catch (Throwable t) {
            log("[错误] Hook锻炼说明页面失败: " + t.getMessage());
            t.printStackTrace();
        }
    }
    
    /**
     * Hook V4SportActivity，阻止启动3-2-1倒计时，直接开始运动
     */
    private void hookV4SportActivity(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            log("正在Hook Activity.onCreate - 检测V4SportActivity");
            
            XposedHelpers.findAndHookMethod(
                Activity.class,
                "onCreate",
                android.os.Bundle.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
                        Activity activity = (Activity) param.thisObject;
                        String className = activity.getClass().getName();
                        
                        if (className.contains("V4SportActivity")) {
                            log("[检测] 发现V4SportActivity: " + className);
                            
                            // Hook b0()方法，阻止启动ThreeTwoOne，改为直接调用O()
                            try {
                                XposedHelpers.findAndHookMethod(
                                    activity.getClass(),
                                    "b0",
                                    new XC_MethodHook() {
                                        @Override
                                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                            // 阻止原方法执行（不启动ThreeTwoOne）
                                            param.setResult(null);
                                            log("[拦截] 已阻止启动3-2-1倒计时页面");
                                            
                                            // 直接调用O()开始运动
                                            try {
                                                XposedHelpers.callMethod(param.thisObject, "O");
                                                log("[成功] 已直接调用O()方法开始运动");
                                            } catch (Throwable t) {
                                                log("[错误] 调用O()失败: " + t.getMessage());
                                            }
                                        }
                                    }
                                );
                                log("[成功] 已Hook V4SportActivity.b0()方法");
                            } catch (Throwable t) {
                                log("[错误] Hook b0()方法失败: " + t.getMessage());
                            }
                        }
                    }
                }
            );
            
            log("V4SportActivity Hook完成");
        } catch (Throwable t) {
            log("[错误] Hook V4SportActivity失败: " + t.getMessage());
            t.printStackTrace();
        }
    }
}

