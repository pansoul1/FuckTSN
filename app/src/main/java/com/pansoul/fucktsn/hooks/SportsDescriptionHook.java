package com.pansoul.fucktsn.hooks;

import android.app.Activity;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class SportsDescriptionHook extends BaseHook {

    public SportsDescriptionHook() {
        super("锻炼说明自动跳过");
    }

    @Override
    protected void hook(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        hookSportsDescriptionActivity(lpparam);
    }

    /**
     * Hook 锻炼说明页面，自动跳过倒计时（适配360加固）
     */
    private void hookSportsDescriptionActivity(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            log("正在Hook Activity.onCreate - 检测锻炼说明页面（适配360加固）");
            
  
            XposedHelpers.findAndHookMethod(
                "android.app.Activity",
                lpparam.classLoader,
                "onCreate",
                android.os.Bundle.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
                        final Activity activity = (Activity) param.thisObject;
                        String className = activity.getClass().getName();
                        
      
                        if (className.contains("SportsDescriptionActivity")) {
                            log("[检测] 发现锻炼说明页面: " + className);
                            
                            
                            new Handler(activity.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        autoSkipSportsDescription(activity);
                                    } catch (Throwable t) {
                                        log("[错误] 自动跳过失败: " + t.getMessage());
                                    }
                                }
                            }, 1000);
                        }
                    }
                }
            );
            
            log("Activity.onCreate Hook完成（等待锻炼说明页面）");
        } catch (Throwable t) {
            log("[错误] Hook Activity失败: " + t.getMessage());
            t.printStackTrace();
        }
    }
    
    /**
     * 自动跳过锻炼说明
     */
    private void autoSkipSportsDescription(Activity activity) {
        try {
            
            View decorView = activity.getWindow().getDecorView();
        
            int checkBoxId = activity.getResources().getIdentifier("cb_ignore", "id", activity.getPackageName());
            CheckBox cbIgnore = null;
            if (checkBoxId != 0) {
                cbIgnore = decorView.findViewById(checkBoxId);
            }
            
 
            int buttonId = activity.getResources().getIdentifier("bt_start", "id", activity.getPackageName());
            Button btStart = null;
            if (buttonId != 0) {
                btStart = decorView.findViewById(buttonId);
            }
            

            if (btStart == null) {
                btStart = findButtonByText(decorView, "开始锻炼");
            }
            if (cbIgnore == null) {
                cbIgnore = findCheckBoxNearButton(decorView, btStart);
            }
            
         
            if (cbIgnore != null && !cbIgnore.isChecked()) {
                cbIgnore.setChecked(true);
                log("[锻炼说明] 已自动勾选'不再提醒'");
            }
            
 
            if (btStart != null) {
                btStart.setEnabled(true);
                btStart.setText("开始锻炼");
                btStart.performClick();
                log("[锻炼说明] 已自动点击'开始锻炼'按钮");
            } else {
                log("[警告] 未找到'开始锻炼'按钮");
            }
            
        } catch (Throwable t) {
            log("[错误] 处理锻炼说明失败: " + t.getMessage());
        }
    }
    
    /**
     * 通过文本查找按钮
     */
    private Button findButtonByText(View view, String text) {
        if (view instanceof Button) {
            Button button = (Button) view;
            CharSequence buttonText = button.getText();
            if (buttonText != null && buttonText.toString().contains(text)) {
                return button;
            }
        }
        
        if (view instanceof android.view.ViewGroup) {
            android.view.ViewGroup group = (android.view.ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                Button result = findButtonByText(group.getChildAt(i), text);
                if (result != null) {
                    return result;
                }
            }
        }
        
        return null;
    }
    
    /**
     * 查找按钮附近的复选框
     */
    private CheckBox findCheckBoxNearButton(View root, Button button) {
        if (root instanceof android.view.ViewGroup) {
            android.view.ViewGroup group = (android.view.ViewGroup) root;
            for (int i = 0; i < group.getChildCount(); i++) {
                View child = group.getChildAt(i);
                if (child instanceof CheckBox) {
                    return (CheckBox) child;
                }
            }
        }
        return null;
    }
}

