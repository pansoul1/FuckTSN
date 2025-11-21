package com.pansoul.fucktsn.hooks;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class DeviceIdDisplayHook extends BaseHook {

    public DeviceIdDisplayHook() {
        super("设备ID展示");
    }

    @Override
    protected void hook(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        hookAboutUsActivity(lpparam);
    }

    private void hookAboutUsActivity(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            log("正在Hook AboutUsActivity.onCreate");
            
            XposedHelpers.findAndHookMethod(
                Activity.class,
                "onCreate",
                Bundle.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
                        final Activity activity = (Activity) param.thisObject;
                        String className = activity.getClass().getName();
                        
                        if (className.contains("AboutUsActivity")) {
                            log("[检测] 发现AboutUsActivity: " + className);
                            
                            // 延迟执行，确保布局完成
                            activity.getWindow().getDecorView().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        addDeviceIdCard(activity);
                                        log("[成功] 已添加设备ID卡片");
                                    } catch (Throwable t) {
                                        log("[错误] 添加失败: " + t.getMessage());
                                        t.printStackTrace();
                                    }
                                }
                            }, 500);
                        }
                    }
                }
            );
            
            log("AboutUsActivity Hook完成");
        } catch (Throwable t) {
            log("[错误] Hook失败: " + t.getMessage());
            t.printStackTrace();
        }
    }
    
    private void addDeviceIdCard(final Activity activity) {
        // 直接在DecorView上添加浮动卡片
        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        
        // 检查是否已添加
        if (decorView.findViewWithTag("device_id_card") != null) {
            log("[跳过] 卡片已存在");
            return;
        }
        
        // 创建浮动卡片（固定在底部，但距离底部有一定距离）
        LinearLayout card = createFloatingCard(activity);
        card.setTag("device_id_card");
        
        // 添加到DecorView
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.BOTTOM;
        params.setMargins(dp(activity, 16), 0, dp(activity, 16), dp(activity, 80));
        
        decorView.addView(card, params);
        
        log("[成功] 浮动卡片已添加到DecorView");
    }
    
    private LinearLayout createFloatingCard(final Activity activity) {
        // 极简圆形悬浮按钮
        LinearLayout card = new LinearLayout(activity);
        card.setOrientation(LinearLayout.VERTICAL);
        
        // 设置圆角和阴影
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.WHITE);
        drawable.setCornerRadius(dp(activity, 25)); // 圆形
        drawable.setStroke(dp(activity, 1), 0xFFEEEEEE); // 极细边框
        card.setBackground(drawable);
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            card.setElevation(dp(activity, 6));
        }
        
        // 内容
        TextView text = new TextView(activity);
        text.setText("ID");
        text.setTextSize(14);
        text.setTextColor(0xFF333333);
        text.setTypeface(null, android.graphics.Typeface.BOLD);
        text.setGravity(Gravity.CENTER);
        text.setPadding(dp(activity, 12), dp(activity, 12), dp(activity, 12), dp(activity, 12));
        
        card.addView(text);
        
        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeviceIdPage(activity);
            }
        });
        
        return card;
    }
    
    private void showDeviceIdPage(final Activity activity) {
        try {
            log("[点击] 打开设备ID管理页面");
            
            // 创建全屏的管理页面
            final ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
            final FrameLayout pageContainer = new FrameLayout(activity);
            pageContainer.setBackgroundColor(Color.WHITE); // 纯白背景
            pageContainer.setTag("device_id_page");
            pageContainer.setClickable(true); 
            
            // 隐藏底部按钮
            final View floatingCard = decorView.findViewWithTag("device_id_card");
            if (floatingCard != null) {
                floatingCard.setVisibility(View.GONE);
            }
            
            // 创建内容
            LinearLayout content = createDeviceIdPageContent(activity, pageContainer, decorView, floatingCard);
            pageContainer.addView(content);
            
            // 添加到DecorView
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            );
            decorView.addView(pageContainer, params);
            
        } catch (Throwable t) {
            log("[错误] 打开页面失败: " + t.getMessage());
            android.widget.Toast.makeText(activity, "打开失败", android.widget.Toast.LENGTH_SHORT).show();
        }
    }
    
    private LinearLayout createDeviceIdPageContent(final Activity activity, final View pageContainer, final ViewGroup decorView, final View floatingCard) {
        LinearLayout mainLayout = new LinearLayout(activity);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setBackgroundColor(Color.WHITE);
        
        // 顶部状态栏空间
        View statusBarSpace = new View(activity);
        statusBarSpace.setBackgroundColor(Color.WHITE);
        int statusBarHeight = getStatusBarHeight(activity);
        LinearLayout.LayoutParams spaceParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            statusBarHeight > 0 ? statusBarHeight : dp(activity, 25)
        );
        statusBarSpace.setLayoutParams(spaceParams);
        mainLayout.addView(statusBarSpace);
        
        // 标题栏
        mainLayout.addView(createPageTitleBar(activity, pageContainer, decorView, floatingCard));
        
        // 内容区域
        android.widget.ScrollView scrollView = new android.widget.ScrollView(activity);
        scrollView.setBackgroundColor(Color.WHITE);
        scrollView.setVerticalScrollBarEnabled(false);
        
        LinearLayout contentLayout = new LinearLayout(activity);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setPadding(dp(activity, 24), dp(activity, 32), dp(activity, 24), dp(activity, 24));
        
        // 1. 极简ID显示 (优化版)
        contentLayout.addView(createMinimalistIdSection(activity));
        
        // 2. 设备型号显示
        contentLayout.addView(createMinimalistModelSection(activity));
        
        // 3. 提示文字
        TextView tipText = new TextView(activity);
        tipText.setText("重置后将清除缓存并生成全新的设备标识");
        tipText.setTextSize(13);
        tipText.setTextColor(0xFF999999);
        tipText.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams tipParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        tipParams.topMargin = dp(activity, 24);
        tipParams.bottomMargin = dp(activity, 40);
        tipText.setLayoutParams(tipParams);
        contentLayout.addView(tipText);
        
        // 4. 操作按钮 (优化版)
        contentLayout.addView(createMinimalistActionButtons(activity));
        
        scrollView.addView(contentLayout);
        
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        );
        scrollView.setLayoutParams(scrollParams);
        mainLayout.addView(scrollView);
        
        return mainLayout;
    }
    
    private int getStatusBarHeight(Activity activity) {
        int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return activity.getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }
    
    private LinearLayout createPageTitleBar(final Activity activity, final View pageContainer, final ViewGroup decorView, final View floatingCard) {
        LinearLayout titleBar = new LinearLayout(activity);
        titleBar.setOrientation(LinearLayout.HORIZONTAL);
        titleBar.setBackgroundColor(Color.WHITE);
        titleBar.setPadding(dp(activity, 16), dp(activity, 12), dp(activity, 16), dp(activity, 12));
        titleBar.setGravity(Gravity.CENTER_VERTICAL);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleBar.setLayoutParams(params);
        
        // 关闭按钮
        TextView backButton = new TextView(activity);
        backButton.setText("关闭");
        backButton.setTextSize(15);
        backButton.setTextColor(0xFF666666);
        backButton.setPadding(dp(activity, 12), dp(activity, 8), dp(activity, 12), dp(activity, 8));
        backButton.setClickable(true);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (floatingCard != null) {
                    floatingCard.setVisibility(View.VISIBLE);
                }
                decorView.removeView(pageContainer);
            }
        });
        
        // 标题
        TextView title = new TextView(activity);
        title.setText("设备ID管理");
        title.setTextSize(18);
        title.setTextColor(0xFF333333);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
            0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f
        );
        title.setLayoutParams(titleParams);
        
        // 占位 View 保持标题居中
        View placeholder = new View(activity);
        LinearLayout.LayoutParams phParams = new LinearLayout.LayoutParams(
             dp(activity, 54), 1
        );
        placeholder.setLayoutParams(phParams);
        
        titleBar.addView(backButton);
        titleBar.addView(title);
        titleBar.addView(placeholder); 
        
        return titleBar;
    }
    
    private LinearLayout createMinimalistIdSection(Activity activity) {
        LinearLayout section = new LinearLayout(activity);
        section.setOrientation(LinearLayout.VERTICAL);
        section.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        section.setLayoutParams(params);
        
        // 标签
        TextView label = new TextView(activity);
        label.setText("当前设备标识");
        label.setTextSize(14);
        label.setTextColor(0xFF999999);
        section.addView(label);
        
        // ID 容器
        LinearLayout idContainer = new LinearLayout(activity);
        idContainer.setOrientation(LinearLayout.VERTICAL);
        idContainer.setGravity(Gravity.CENTER);
        
        // 设置浅灰背景容器
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(0xFFF7F8FA);
        bg.setCornerRadius(dp(activity, 12));
        idContainer.setBackground(bg);
        
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        containerParams.topMargin = dp(activity, 20);
        idContainer.setLayoutParams(containerParams);
        idContainer.setPadding(dp(activity, 20), dp(activity, 24), dp(activity, 20), dp(activity, 24));
        
        // ID 值
        TextView idValue = new TextView(activity);
        String id = readDeviceId(activity);
        idValue.setText(id);
        idValue.setTextSize(16); // 适中字号，避免换行太乱
        idValue.setTextColor(0xFF333333);
        idValue.setTypeface(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD);
        idValue.setGravity(Gravity.CENTER);
        idValue.setTextIsSelectable(true);
        idValue.setLineSpacing(dp(activity, 4), 1.2f); // 增加行间距
        
        idContainer.addView(idValue);
        section.addView(idContainer);
        
        return section;
    }
    
    private LinearLayout createMinimalistModelSection(Activity activity) {
        LinearLayout section = new LinearLayout(activity);
        section.setOrientation(LinearLayout.VERTICAL);
        section.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = dp(activity, 32);
        section.setLayoutParams(params);
        
        // 标签
        TextView label = new TextView(activity);
        label.setText("设备型号");
        label.setTextSize(14);
        label.setTextColor(0xFF999999);
        section.addView(label);
        
        // 型号容器
        LinearLayout modelContainer = new LinearLayout(activity);
        modelContainer.setOrientation(LinearLayout.VERTICAL);
        modelContainer.setGravity(Gravity.CENTER);
        
        // 设置浅灰背景容器
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(0xFFF7F8FA);
        bg.setCornerRadius(dp(activity, 12));
        modelContainer.setBackground(bg);
        
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        containerParams.topMargin = dp(activity, 20);
        modelContainer.setLayoutParams(containerParams);
        modelContainer.setPadding(dp(activity, 20), dp(activity, 20), dp(activity, 20), dp(activity, 20));
        
        // 型号值
        TextView modelValue = new TextView(activity);
        String model = readDeviceModel(activity);
        modelValue.setText(model);
        modelValue.setTextSize(18);
        modelValue.setTextColor(0xFF333333);
        modelValue.setTypeface(null, android.graphics.Typeface.BOLD);
        modelValue.setGravity(Gravity.CENTER);
        modelValue.setTextIsSelectable(true);
        
        modelContainer.addView(modelValue);
        section.addView(modelContainer);
        
        return section;
    }
    
    private LinearLayout createMinimalistActionButtons(final Activity activity) {
        LinearLayout container = new LinearLayout(activity);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setGravity(Gravity.CENTER);
        
        // 复制按钮 (黑色实心，占满整行)
        TextView copyBtn = new TextView(activity);
        copyBtn.setText("复制设备ID");
        copyBtn.setTextColor(Color.WHITE);
        copyBtn.setTextSize(15);
        copyBtn.setGravity(Gravity.CENTER);
        copyBtn.setTypeface(null, android.graphics.Typeface.BOLD);
        
        GradientDrawable copyBg = new GradientDrawable();
        copyBg.setColor(0xFF333333);
        copyBg.setCornerRadius(dp(activity, 24));
        copyBtn.setBackground(copyBg);
        
        LinearLayout.LayoutParams copyParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(activity, 48)
        );
        copyBtn.setLayoutParams(copyParams);
        
        copyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String deviceId = readDeviceId(activity);
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) 
                    activity.getSystemService(android.content.Context.CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText("设备ID", deviceId);
                clipboard.setPrimaryClip(clip);
                android.widget.Toast.makeText(activity, "✅ 已复制设备ID", android.widget.Toast.LENGTH_SHORT).show();
            }
        });
        
        container.addView(copyBtn);
        
        return container;
    }

    private String readDeviceId(Activity activity) {
        try {
            android.content.SharedPreferences sp = activity.getSharedPreferences("gank_device_id.xml", android.content.Context.MODE_PRIVATE);
            return sp.getString("gank_device_id", "未找到设备ID");
        } catch (Exception e) {
            return "读取失败: " + e.getMessage();
        }
    }
    
    private String readDeviceModel(Activity activity) {
        try {
            // 优先读取当前 Build.MODEL 的值（已被Hook替换）
            String currentModel = android.os.Build.MODEL;
            if (currentModel != null && !currentModel.isEmpty()) {
                return currentModel;
            }
            
            // 备用方案：从 SharedPreferences 读取
            android.content.SharedPreferences sp = activity.getSharedPreferences("device_model_cache", android.content.Context.MODE_PRIVATE);
            String model = sp.getString("cached_model", null);
            if (model == null || model.isEmpty()) {
                return "未生成（请重启应用）";
            }
            return model;
        } catch (Exception e) {
            return "读取失败: " + e.getMessage();
        }
    }

    private int dp(Activity activity, int dp) {
        return (int) (dp * activity.getResources().getDisplayMetrics().density);
    }
}
