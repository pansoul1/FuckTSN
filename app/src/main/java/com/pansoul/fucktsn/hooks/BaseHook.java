package com.pansoul.fucktsn.hooks;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public abstract class BaseHook {
    
    protected static final String TAG = "FuckTSN";
    protected static final String TARGET_PACKAGE = "com.bxkj.student";
    
    private String hookName;
    private boolean enabled = true;
    private boolean hooked = false;

    public BaseHook(String hookName) {
        this.hookName = hookName;
    }

    public void execute(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!enabled) {
            log(hookName + " 已禁用，跳过");
            return;
        }

        try {
            log("==========================================");
            log("开始执行: " + hookName);
            hook(lpparam);
            hooked = true;
            log(hookName + " 执行成功");
            log("==========================================");
        } catch (Throwable t) {
            log("错误: " + hookName + " 执行失败: " + t.getMessage());
            t.printStackTrace();
        }
    }

    protected abstract void hook(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable;

    protected void log(String message) {
        XposedBridge.log(TAG + ": " + message);
    }

    public String getName() {
        return hookName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isHooked() {
        return hooked;
    }
}

