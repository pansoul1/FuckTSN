package com.pansoul.fucktsn;

import com.pansoul.fucktsn.hooks.BaseHook;
import com.pansoul.fucktsn.hooks.UniversalHook;
import com.pansoul.fucktsn.hooks.RootDetectionHook;
import com.pansoul.fucktsn.hooks.PrivacyAgreementHook;
import com.pansoul.fucktsn.hooks.AdSkipHook;
import com.pansoul.fucktsn.hooks.DialogBlockHook;
import com.pansoul.fucktsn.hooks.SportsDescriptionHook;
import com.pansoul.fucktsn.hooks.DeviceIdDisplayHook;
import com.pansoul.fucktsn.hooks.DeviceIdRandomizerHook;
import com.pansoul.fucktsn.hooks.DeviceModelRandomizerHook;
import com.pansoul.fucktsn.hooks.AccountQuickSelectHook;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {

    private static final String TARGET_PACKAGE = "com.bxkj.student";
    private static final String MODULE_PACKAGE = "com.pansoul.fucktsn";
    private static final List<BaseHook> hooks = new ArrayList<>();

    static {
       
        hooks.add(new UniversalHook());
        
        // 增强的Root和安全检测绕过（针对应用本体检测类）
        hooks.add(new RootDetectionHook());
        
        hooks.add(new PrivacyAgreementHook());
        
        hooks.add(new AdSkipHook());
       
        hooks.add(new DialogBlockHook());
        
        hooks.add(new SportsDescriptionHook());
        
        hooks.add(new DeviceIdRandomizerHook());
        
        hooks.add(new DeviceModelRandomizerHook());
        
        hooks.add(new DeviceIdDisplayHook());
        
        // 账号快速选择
        hooks.add(new AccountQuickSelectHook());
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals(MODULE_PACKAGE)) {
            hookModuleStatus(lpparam);
            return;
        }

        if (!lpparam.packageName.equals(TARGET_PACKAGE)) {
            return;
        }

        XposedBridge.log("FuckTSN: ========================================");
        XposedBridge.log("FuckTSN: 目标应用已加载: " + TARGET_PACKAGE);
        XposedBridge.log("FuckTSN: 开始执行Hook模块，共 " + hooks.size() + " 个");
        XposedBridge.log("FuckTSN: ========================================");

        for (BaseHook hook : hooks) {
            hook.execute(lpparam);
        }

        XposedBridge.log("FuckTSN: ========================================");
        XposedBridge.log("FuckTSN: 所有Hook模块执行完毕");
        XposedBridge.log("FuckTSN: ========================================");
    }

    private void hookModuleStatus(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod(
                    "com.pansoul.fucktsn.MainActivity",
                    lpparam.classLoader,
                    "isModuleActive",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            param.setResult(true);
                        }
                    }
            );
            XposedBridge.log("FuckTSN: 模块激活状态Hook成功");
        } catch (Throwable t) {
            XposedBridge.log("FuckTSN: Hook模块激活状态失败: " + t.getMessage());
        }
    }

    public static List<BaseHook> getHooks() {
        return hooks;
    }
}

