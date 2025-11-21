package com.pansoul.fucktsn.hooks;

import android.content.Context;
import android.content.SharedPreferences;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import java.util.Random;

public class DeviceModelRandomizerHook extends BaseHook {

    private static final String PREF_NAME = "device_model_cache";
    private static final String KEY_MODEL = "cached_model";
    
    // 常见的手机型号池（国行小米系列）
    private static final String[] MODEL_POOL = {
        // Redmi K40 系列（国行）
        "Xiaomi-M2012K11AC",
        "Xiaomi-M2012K11C",
        "Xiaomi-M2012K10C",
        "Xiaomi-22021211RC",

        // Redmi K50 系列（国行）
        "Xiaomi-22041211AC",
        "Xiaomi-22011211C",
        "Xiaomi-21121210C",
        "Xiaomi-22081212C",

        // Redmi K60 系列（国行）
        "Xiaomi-23013RK75C",
        "Xiaomi-22127RK46C",
        "Xiaomi-22122RK93C",
        "Xiaomi-23078RKD5C",

        // Redmi K70 系列（国行）
        "Xiaomi-23113RKC6C",
        "Xiaomi-23117RK66C",
        "Xiaomi-2311DRK48C",
        "Xiaomi-2407FRK8EC",

        // Redmi K80 系列（国行）
        "Xiaomi-24117RK2CC",
        "Xiaomi-24122RKC7C",
        "Xiaomi-24127RK2CC",
        "Xiaomi-25060RK16C",

        // Redmi K90 系列（国行）
        "Xiaomi-2510DRK44C",
        "Xiaomi-25102RKBEC",
        "Xiaomi-25102RK69C",

        // Xiaomi 10 系列（国行）
        "Xiaomi-M2001J2E",
        "Xiaomi-M2001J2C",
        "Xiaomi-M2001J1E",
        "Xiaomi-M2001J1C",
        "Xiaomi-M2002J9E",
        "Xiaomi-M2007J1SC",
        "Xiaomi-M2102J2SC",

        // Xiaomi 11 系列（国行）
        "Xiaomi-M2011K2C",
        "Xiaomi-M2102K1AC",
        "Xiaomi-M2102K1C",
        "Xiaomi-M2101K9C",
        "Xiaomi-2107119DC",

        // Xiaomi 12 系列（国行）
        "Xiaomi-2201123C",
        "Xiaomi-2112123AC",
        "Xiaomi-2201122C",
        "Xiaomi-2207122MC",
        "Xiaomi-2206123SC",
        "Xiaomi-2206122SC",
        "Xiaomi-2203121C",

        // Xiaomi 13 系列（国行）
        "Xiaomi-2211133C",
        "Xiaomi-2210132C",
        "Xiaomi-2304FPN6DC",

        // Xiaomi 14 系列（国行）
        "Xiaomi-23127PN0CC",
        "Xiaomi-23116PN5BC",
        "Xiaomi-2311BPN23C",
        "Xiaomi-24031PN0DC",

        // Xiaomi 15 系列（国行）
        "Xiaomi-24129PN74C",
        "Xiaomi-2410DPN6CC",
        "Xiaomi-25019PNF3C",
        "Xiaomi-25010PN30C",
        "Xiaomi-25042PN24C",

        // Xiaomi 17 系列（国行）
        "Xiaomi-25113PN0EC",
        "Xiaomi-25098PN5AC",
        "Xiaomi-2509FPN0BC"
    };
    
    private boolean hasHooked = false;

    public DeviceModelRandomizerHook() {
        super("设备型号随机化");
    }

    @Override
    protected void hook(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        // 同时使用两种方案
        hookSystemUtilsGetModelWithClassLoader(lpparam);
        hookBuildModel(lpparam);
    }

    /**
     * Hook ClassLoader 等待 com.bxkj.base.util.y 类加载（应对360加固）
     */
    private void hookSystemUtilsGetModelWithClassLoader(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            log("正在Hook ClassLoader.loadClass() - 等待 SystemUtils 类加载");
            
            XposedHelpers.findAndHookMethod(
                ClassLoader.class,
                "loadClass",
                String.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (hasHooked) {
                            return;
                        }
                        
                        String className = (String) param.args[0];
                        
                        // 当 com.bxkj.base.util.y 类被加载时
                        if ("com.bxkj.base.util.y".equals(className)) {
                            Class<?> systemUtilsClass = (Class<?>) param.getResult();
                            hookSystemUtilsGetModel(systemUtilsClass, lpparam);
                            hasHooked = true;
                        }
                    }
                }
            );
            
            log("ClassLoader Hook完成");
        } catch (Throwable t) {
            log("[错误] Hook ClassLoader失败: " + t.getMessage());
        }
    }
    
    /**
     * Hook com.bxkj.base.util.y.e() 方法
     */
    private void hookSystemUtilsGetModel(Class<?> systemUtilsClass, XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            log("正在Hook SystemUtils.e() - 设备型号获取方法");
            
            XposedHelpers.findAndHookMethod(
                systemUtilsClass,
                "e",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        try {
                            // 获取Context
                            Context context = (Context) XposedHelpers.callStaticMethod(
                                XposedHelpers.findClass("android.app.ActivityThread", lpparam.classLoader),
                                "currentApplication"
                            );
                            
                            if (context != null) {
                                String fakeModel = getCachedModel(context);
                                param.setResult(fakeModel);
                                log("[拦截] SystemUtils.e() 返回假型号: " + fakeModel);
                            }
                        } catch (Throwable t) {
                            log("[警告] 获取Context失败，使用随机型号: " + t.getMessage());
                            param.setResult(getRandomModel());
                        }
                    }
                }
            );
            
            log("设备型号Hook完成 - 已拦截 y.e() 方法");
            
        } catch (Throwable t) {
            log("[错误] Hook设备型号失败: " + t.getMessage());
            t.printStackTrace();
        }
    }
    
    /**
     * 备用方案：直接替换 Build.MODEL 字段值
     */
    private void hookBuildModel(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            log("正在Hook Build.MODEL - 备用方案");
            
            // 获取Context
            Context context = null;
            try {
                context = (Context) XposedHelpers.callStaticMethod(
                    XposedHelpers.findClass("android.app.ActivityThread", lpparam.classLoader),
                    "currentApplication"
                );
            } catch (Throwable t) {
                log("[警告] 无法立即获取Context，将延迟Hook");
            }
            
            // 如果能获取到Context，直接替换字段
            if (context != null) {
                String fakeModel = getCachedModel(context);
                
                // 拆分厂商名和型号
                String manufacturer = fakeModel;
                String actualModel = fakeModel;
                if (fakeModel.contains("-")) {
                    int dashIndex = fakeModel.indexOf("-");
                    manufacturer = fakeModel.substring(0, dashIndex);
                    actualModel = fakeModel.substring(dashIndex + 1);
                }
                
                XposedHelpers.setStaticObjectField(android.os.Build.class, "MANUFACTURER", manufacturer);
                XposedHelpers.setStaticObjectField(android.os.Build.class, "MODEL", actualModel);
                log("[成功] Build.MANUFACTURER已替换为: " + manufacturer);
                log("[成功] Build.MODEL已替换为: " + actualModel);
            } else {
                // 否则使用随机值
                String fakeModel = getRandomModel();
                
                // 拆分厂商名和型号
                String manufacturer = fakeModel;
                String actualModel = fakeModel;
                if (fakeModel.contains("-")) {
                    int dashIndex = fakeModel.indexOf("-");
                    manufacturer = fakeModel.substring(0, dashIndex);
                    actualModel = fakeModel.substring(dashIndex + 1);
                }
                
                XposedHelpers.setStaticObjectField(android.os.Build.class, "MANUFACTURER", manufacturer);
                XposedHelpers.setStaticObjectField(android.os.Build.class, "MODEL", actualModel);
                log("[成功] Build.MANUFACTURER已替换为(随机): " + manufacturer);
                log("[成功] Build.MODEL已替换为(随机): " + actualModel);
            }
            
            log("Build.MODEL Hook完成");
            
        } catch (Throwable t) {
            log("[错误] Hook Build.MODEL失败: " + t.getMessage());
            t.printStackTrace();
        }
    }
    
    /**
     * 获取缓存的设备型号，如果不存在则生成新的
     * 与设备ID使用相同的缓存机制，重置ID时一起清除
     */
    private String getCachedModel(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            String cached = prefs.getString(KEY_MODEL, null);
            
            if (cached == null || cached.isEmpty()) {
                cached = getRandomModel();
                prefs.edit().putString(KEY_MODEL, cached).apply();
                log("[生成] 新设备型号: " + cached);
            }
            
            return cached;
        } catch (Exception e) {
            log("[错误] 获取缓存型号失败: " + e.getMessage());
            return getRandomModel();
        }
    }
    
    /**
     * 从型号池中随机选择一个型号
     */
    private String getRandomModel() {
        Random random = new Random();
        return MODEL_POOL[random.nextInt(MODEL_POOL.length)];
    }
    
    /**
     * 清除缓存的设备型号（供重置ID时调用）
     */
    public static void clearCachedModel(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            prefs.edit().remove(KEY_MODEL).apply();
        } catch (Exception e) {
            // ignore
        }
    }
}
