package com.pansoul.fucktsn;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.pansoul.fucktsn.utils.LogManager;

public class MainActivity extends AppCompatActivity {

    private static boolean isModuleActive() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
      
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        
      
        setupStatusBar();
        
        LogManager.init(this);
        createUI();
    }

    private void setupStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.WHITE); 
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
              
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }
    }

    private void createUI() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(Color.WHITE);
        
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(dp(24), dp(24), dp(24), dp(24));

       
        mainLayout.addView(createHeader());

       
        mainLayout.addView(createStatus());
        
       
        mainLayout.addView(createFeatureList());
        
       
        mainLayout.addView(createTips());

        scrollView.addView(mainLayout);
        setContentView(scrollView);
    }

    private LinearLayout createHeader() {
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(0, 0, 0, dp(32));
        
        
        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setOrientation(LinearLayout.HORIZONTAL);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);
        
        LinearLayout titleContainer = new LinearLayout(this);
        titleContainer.setOrientation(LinearLayout.VERTICAL);
        
        LinearLayout.LayoutParams titleContainerParams = new LinearLayout.LayoutParams(
            0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f
        );
        titleContainer.setLayoutParams(titleContainerParams);
        
        TextView title = new TextView(this);
        title.setText("FuckTSN");
        title.setTextSize(28);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(0xFF1A1A1A);
        
        TextView subtitle = new TextView(this);
        subtitle.setText("v2.0.17");
        subtitle.setTextSize(14);
        subtitle.setTextColor(0xFF999999);
        subtitle.setPadding(0, dp(6), 0, 0);
        
        titleContainer.addView(title);
        titleContainer.addView(subtitle);
        
        
        ImageView githubIcon = new ImageView(this);
        githubIcon.setImageResource(R.drawable.github_icon);
        githubIcon.setColorFilter(0xFF333333); 
        
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(64), dp(64));
        githubIcon.setLayoutParams(iconParams);
        githubIcon.setPadding(dp(8), dp(8), dp(8), dp(8));
        githubIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        githubIcon.setClickable(true);
        githubIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://github.com/pansoul1"));
                startActivity(intent);
            }
        });
        
        titleRow.addView(titleContainer);
        titleRow.addView(githubIcon);
        
        header.addView(titleRow);
        
        return header;
    }

    private LinearLayout createStatus() {
        LinearLayout section = new LinearLayout(this);
        section.setOrientation(LinearLayout.HORIZONTAL);
        section.setPadding(0, 0, 0, dp(40));
        section.setGravity(Gravity.CENTER_VERTICAL);
        
        boolean isActive = isModuleActive();
        
        TextView dot = new TextView(this);
        dot.setText("●");
        dot.setTextSize(16);
        dot.setTextColor(isActive ? 0xFF4CAF50 : 0xFFF44336);
        
        TextView statusText = new TextView(this);
        statusText.setText(isActive ? "模块已激活" : "模块未激活");
        statusText.setTextSize(16);
        statusText.setTextColor(0xFF1A1A1A);
        statusText.setPadding(dp(10), 0, 0, 0);
        
        section.addView(dot);
        section.addView(statusText);
        
        return section;
    }

    private LinearLayout createFeatureList() {
        LinearLayout section = new LinearLayout(this);
        section.setOrientation(LinearLayout.VERTICAL);
        section.setPadding(0, 0, 0, dp(40));
        
        TextView title = new TextView(this);
        title.setText("功能");
        title.setTextSize(18);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(0xFF1A1A1A);
        title.setPadding(0, 0, 0, dp(20));
        section.addView(title);
        
        String[] features = {
            "通用检测绕过",
            "隐私协议自动同意",
            "启动页广告跳过",
            "通知权限绕过",
            "锻炼说明自动跳过"
        };
        
        for (String feature : features) {
            section.addView(createFeatureItem(feature));
        }
        
        return section;
    }

    private LinearLayout createFeatureItem(String name) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.HORIZONTAL);
        item.setPadding(0, 0, 0, dp(16));
        item.setGravity(Gravity.CENTER_VERTICAL);
        
        TextView check = new TextView(this);
        check.setText("✓");
        check.setTextSize(14);
        check.setTextColor(0xFF4CAF50);
        check.setTypeface(null, Typeface.BOLD);
        
        TextView text = new TextView(this);
        text.setText(name);
        text.setTextSize(15);
        text.setTextColor(0xFF333333);
        text.setPadding(dp(12), 0, 0, 0);
        
        item.addView(check);
        item.addView(text);
        
        return item;
    }

    private LinearLayout createTips() {
        LinearLayout section = new LinearLayout(this);
        section.setOrientation(LinearLayout.VERTICAL);
        
        TextView title = new TextView(this);
        title.setText("使用");
        title.setTextSize(18);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(0xFF1A1A1A);
        title.setPadding(0, 0, 0, dp(20));
        section.addView(title);
        
        section.addView(createTipItem("在 LSPosed 中激活模块"));
        section.addView(createTipItem("重启体适能应用"));
        
        return section;
    }

    private LinearLayout createTipItem(String text) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.HORIZONTAL);
        item.setPadding(0, 0, 0, dp(12));
        
        TextView bullet = new TextView(this);
        bullet.setText("•");
        bullet.setTextSize(14);
        bullet.setTextColor(0xFF666666);
        
        TextView tipText = new TextView(this);
        tipText.setText(text);
        tipText.setTextSize(15);
        tipText.setTextColor(0xFF555555);
        tipText.setPadding(dp(10), 0, 0, 0);
        
        item.addView(bullet);
        item.addView(tipText);
        
        return item;
    }

    private int dp(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}

