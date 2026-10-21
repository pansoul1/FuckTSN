package com.pansoul.fucktsn;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.pansoul.fucktsn.utils.LogManager;

public class MainActivity extends AppCompatActivity {

    // 极简纯白设计
    private static final int COLOR_BACKGROUND = 0xFFFFFFFF; // 纯白背景
    private static final int COLOR_TEXT_PRIMARY = 0xFF000000; // 纯黑
    private static final int COLOR_TEXT_SECONDARY = 0xFF444444; // 深灰
    private static final int COLOR_TEXT_TERTIARY = 0xFF888888;  // 浅灰
    private static final int COLOR_SUCCESS = 0xFF34C759;    // 绿
    private static final int COLOR_ERROR = 0xFFFF3B30;      // 红

    private static boolean isModuleActive() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 隐藏原本的ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        
        // 沉浸式状态栏配置
        setupStatusBar();
        
        LogManager.init(this);
        createUI();
    }

    private void setupStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(COLOR_BACKGROUND); // 状态栏与背景同色
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    private void createUI() {
        // 改回 ScrollView，防止小屏幕内容显示不全
        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(COLOR_BACKGROUND);
        scrollView.setVerticalScrollBarEnabled(false);
        scrollView.setFillViewport(true); // 让内容充满屏幕，方便底部对齐

        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(dp(24), dp(40), dp(24), dp(24));

        // 1. 顶部 Header
        mainLayout.addView(createHeader());

        // 2. 状态行
        mainLayout.addView(createStatusRow());

        // 3. 分割线
        mainLayout.addView(createDivider());

        // 4. 功能列表 (恢复单列列表)
        mainLayout.addView(createSectionTitle("功能特性"));
        mainLayout.addView(createFeatureList());

        // 5. 底部说明 (自动挤到最底部)
        View spacer = new View(this);
        mainLayout.addView(spacer, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        
        mainLayout.addView(createTipsText());

        scrollView.addView(mainLayout);
        setContentView(scrollView);
    }

    private View createHeader() {
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(0, 0, 0, dp(20)); // 减少 padding

        LinearLayout textContainer = new LinearLayout(this);
        textContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        textContainer.setLayoutParams(params);

        TextView title = new TextView(this);
        title.setText("FuckTSN");
        title.setTextSize(32); 
        title.setTextColor(COLOR_TEXT_PRIMARY);
        title.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        
        TextView subtitle = new TextView(this);
        subtitle.setText("Version 2.0.25 (for TSN 2.0.25)");
        subtitle.setTextSize(14);
        subtitle.setTextColor(COLOR_TEXT_TERTIARY);

        textContainer.addView(title);
        textContainer.addView(subtitle);

        ImageView githubIcon = new ImageView(this);
        githubIcon.setImageResource(R.drawable.github_icon);
        githubIcon.setColorFilter(COLOR_TEXT_PRIMARY);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(40), dp(40));
        githubIcon.setLayoutParams(iconParams);
        githubIcon.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://github.com/pansoul1"));
            startActivity(intent);
        });

        header.addView(textContainer);
        header.addView(githubIcon);
        return header;
    }

    private View createStatusRow() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(10), 0, dp(10));

        boolean isActive = isModuleActive();

        TextView label = new TextView(this);
        label.setText("模块状态");
        label.setTextSize(15);
        label.setTextColor(COLOR_TEXT_SECONDARY);
        label.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView status = new TextView(this);
        status.setText(isActive ? "已激活" : "未激活");
        status.setTextSize(15);
        status.setTextColor(isActive ? COLOR_SUCCESS : COLOR_ERROR);
        status.setTypeface(Typeface.DEFAULT_BOLD);

        row.addView(label);
        row.addView(status);
        return row;
    }
    
    private View createDivider() {
        View divider = new View(this);
        divider.setBackgroundColor(0xFFEEEEEE); // 极淡的分割线
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1));
        lp.setMargins(0, dp(10), 0, dp(20));
        divider.setLayoutParams(lp);
        return divider;
    }

    private View createSectionTitle(String text) {
        TextView title = new TextView(this);
        title.setText(text);
        title.setTextSize(13);
        title.setTextColor(COLOR_TEXT_TERTIARY);
        title.setPadding(0, 0, 0, dp(12));
        return title;
    }

    // 改为单列列表
    private View createFeatureList() {
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);

        String[] features = {
            "Root / 安全检测绕过",
            "自动跳过 (广告 / 弹窗 / 隐私 / 说明)",
            "账号随机设备指纹绑定",
            "设备信息查看 (关于页面底部)",
            "账号快速选择与自动保存"
        };

        for (String feature : features) {
            TextView item = new TextView(this);
            item.setText("•  " + feature);
            item.setTextSize(15);
            item.setTextColor(COLOR_TEXT_SECONDARY);
            item.setPadding(0, dp(8), 0, dp(8)); // 适中的行间距
            list.addView(item);
        }
        return list;
    }

    private View createTipsText() {
        TextView tips = new TextView(this);
        tips.setText("使用说明：在 LSPosed 中勾选本模块并重启应用生效。长按账号管理悬浮窗可添加账号。");
        tips.setTextSize(12);
        tips.setTextColor(COLOR_TEXT_TERTIARY);
        tips.setLineSpacing(dp(4), 1f);
        tips.setPadding(0, dp(20), 0, 0);
        return tips;
    }

    private int dp(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}