package com.pansoul.fucktsn.hooks;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import java.util.UUID;

public class DeviceIdRandomizerHook extends BaseHook {

    public DeviceIdRandomizerHook() {
        super("设备ID随机化");
    }

    @Override
    protected void hook(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        hookUUIDGeneration(lpparam);
    }

    private void hookUUIDGeneration(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            log("正在Hook UUID.nameUUIDFromBytes() - 设备ID随机化");
            
            // Hook UUID.nameUUIDFromBytes() - 让它返回随机UUID
            XposedHelpers.findAndHookMethod(
                UUID.class,
                "nameUUIDFromBytes",
                byte[].class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        try {
                            // 获取调用栈
                            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
                            
                            // 检查是否是 v.a() 调用的（用于生成gank_device_id）
                            for (StackTraceElement element : stackTrace) {
                                if (element.getClassName().equals("com.bxkj.base.v2.common.utils.v") 
                                    && element.getMethodName().equals("a")) {
                                    
                                    // 替换为随机UUID
                                    UUID randomUUID = UUID.randomUUID();
                                    param.setResult(randomUUID);
                                    
                                    log("[拦截] UUID.nameUUIDFromBytes() -> 替换为随机UUID: " + randomUUID);
                                    return;
                                }
                            }
                        } catch (Throwable t) {
                            log("[警告] 检查调用栈失败: " + t.getMessage());
                        }
                    }
                }
            );
            
            log("UUID Hook完成 - 每次生成gank_device_id时将使用随机UUID");
            
        } catch (Throwable t) {
            log("[错误] Hook UUID失败: " + t.getMessage());
            t.printStackTrace();
        }
    }
}
