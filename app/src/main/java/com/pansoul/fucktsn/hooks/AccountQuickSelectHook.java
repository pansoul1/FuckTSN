package com.pansoul.fucktsn.hooks;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * 账号快速选择 Hook
 * 在登录界面添加账号选择功能，可以快速切换多个账号
 */
public class AccountQuickSelectHook extends BaseHook {

    private static final String PREF_NAME = "account_list";
    private static final String KEY_ACCOUNTS = "accounts";
    
    // 防止重复Hook的标记
    private boolean hasHookedLoginActivity = false;
    private boolean hasHookedLoginViewModel = false;

    public AccountQuickSelectHook() {
        super("账号快速选择");
    }

    @Override
    public void hook(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        // 使用ClassLoader监听，应对360加固
        hookLoginActivityWithClassLoader(lpparam);
        hookLoginViewModelForSave(lpparam);
    }

    /**
     * Hook ClassLoader等待LoginActivity加载（应对360加固）
     */
    private void hookLoginActivityWithClassLoader(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            log("正在Hook ClassLoader.loadClass() - 等待LoginActivity加载");
            
            XposedHelpers.findAndHookMethod(
                    ClassLoader.class,
                    "loadClass",
                    String.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            if (hasHookedLoginActivity) {
                                return;
                            }
                            
                            String className = (String) param.args[0];
                            
                            // 当LoginActivity类被加载时
                            if ("com.bxkj.student.v2.ui.LoginActivity".equals(className)) {
                                Class<?> loginActivityClass = (Class<?>) param.getResult();
                                hookLoginActivityInit(loginActivityClass, lpparam);
                                hasHookedLoginActivity = true;
                            }
                        }
                    }
            );
            
            log("ClassLoader Hook完成");
        } catch (Throwable t) {
            log("Hook ClassLoader失败: " + t.getMessage());
        }
    }

    /**
     * Hook登录界面初始化方法
     */
    private void hookLoginActivityInit(Class<?> loginActivityClass, XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            log("正在Hook LoginActivity.t() - 添加快速选择按钮");
            
            XposedHelpers.findAndHookMethod(
                    loginActivityClass,
                    "t",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            try {
                                final Object activity = param.thisObject;
                                final Context context = (Context) XposedHelpers.callMethod(activity, "H");
                                
                                // 延迟执行，确保布局完成
                                android.view.View decorView = ((android.app.Activity) activity).getWindow().getDecorView();
                                decorView.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            // 直接在DecorView上添加浮动按钮
                                            addFloatingButton((android.app.Activity) activity);
                                            
                                            // Hook登录按钮（备用方案）
                                            hookLoginButton(activity, lpparam);
                                            
                                            // 自动勾选隐私协议
                                            autoCheckAgreement(activity);
                                        } catch (Throwable t) {
                                            log("[错误] 添加浮动按钮失败: " + t.getMessage());
                                            t.printStackTrace();
                                        }
                                    }
                                }, 500);
                                
                            } catch (Throwable t) {
                                log("[错误] Hook afterHookedMethod失败: " + t.getMessage());
                                t.printStackTrace();
                            }
                        }
                    }
            );
            
            log("LoginActivity Hook完成");
        } catch (Throwable t) {
            log("Hook LoginActivity.t()失败: " + t.getMessage());
        }
    }

    /**
     * Hook ClassLoader等待LoginViewModel加载并Hook保存逻辑
     */
    private void hookLoginViewModelForSave(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            log("正在Hook ClassLoader.loadClass() - 等待LoginViewModel加载");
            
            XposedHelpers.findAndHookMethod(
                    ClassLoader.class,
                    "loadClass",
                    String.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            if (hasHookedLoginViewModel) {
                                return;
                            }
                            
                            String className = (String) param.args[0];
                            
                            // 当LoginViewModel类被加载时
                            if ("com.bxkj.student.v2.vm.LoginViewModel".equals(className)) {
                                Class<?> loginViewModelClass = (Class<?>) param.getResult();
                                log("[检测到] LoginViewModel类已加载");
                                hookLoginSuccess(loginViewModelClass, lpparam);
                                hasHookedLoginViewModel = true;
                            }
                        }
                    }
            );
        } catch (Throwable t) {
            log("Hook LoginViewModel ClassLoader失败: " + t.getMessage());
        }
    }

    /**
     * 自动勾选隐私协议
     */
    private void autoCheckAgreement(Object activity) {
        try {
            log("[调试] 尝试自动勾选隐私协议");
            
            // 获取binding
            Object binding = XposedHelpers.callMethod(activity, "L");
            
            // 获取协议复选框 cbAgree
            Object cbAgree = XposedHelpers.getObjectField(binding, "cbAgree");
            if (cbAgree instanceof android.widget.CheckBox) {
                android.widget.CheckBox checkBox = (android.widget.CheckBox) cbAgree;
                checkBox.setChecked(true);
                log("[成功] 隐私协议已自动勾选");
            } else {
                log("[警告] 未找到协议复选框");
            }
        } catch (Throwable t) {
            log("[错误] 自动勾选协议失败: " + t.getMessage());
        }
    }

    /**
     * Hook登录按钮（备用方案）
     */
    private void hookLoginButton(Object activity, XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            log("[调试] 尝试Hook登录按钮");
            
            // 获取binding
            Object binding = XposedHelpers.callMethod(activity, "L");
            
            // 获取登录按钮
            Object btnLogin = XposedHelpers.getObjectField(binding, "btLogin");
            if (btnLogin instanceof View) {
                log("[成功] 找到登录按钮，Hook点击事件");
                View loginButton = (View) btnLogin;
                
                // Hook登录按钮点击
                loginButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            log("[触发] 登录按钮被点击");
                            
                            // 获取ViewModel
                            Object viewModel = XposedHelpers.callMethod(activity, "D");
                            
                            // 获取账号
                            Object accountField = XposedHelpers.callMethod(viewModel, "F");
                            final String account = String.valueOf(XposedHelpers.callMethod(accountField, "get"));
                            log("[调试] 登录账号: " + account);
                            
                            // 获取密码
                            Object passwordField = XposedHelpers.callMethod(viewModel, "D");
                            final String password = String.valueOf(XposedHelpers.callMethod(passwordField, "get"));
                            log("[调试] 密码长度: " + (password != null ? password.length() : 0));
                            
                            // 在登录之前，先检查并应用该账号的设备指纹
                            try {
                                Context appContext = (Context) XposedHelpers.callStaticMethod(
                                    XposedHelpers.findClass("android.app.ActivityThread", lpparam.classLoader),
                                    "currentApplication"
                                );
                                
                                if (appContext != null && account != null && !account.isEmpty() && !"null".equals(account)) {
                                    ensureAndApplyDeviceFingerprint(appContext, account, password);
                                }
                            } catch (Throwable t2) {
                                log("[警告] 应用设备指纹失败: " + t2.getMessage());
                            }
                            
                            // 执行原始登录逻辑
                            XposedHelpers.callMethod(activity, "P");
                            
                            // 延迟保存（等待登录成功）
                            v.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        if (account != null && !account.isEmpty() && !"null".equals(account) &&
                                            password != null && !password.isEmpty() && !"null".equals(password)) {
                                            
                                            Context appContext = (Context) XposedHelpers.callStaticMethod(
                                                    XposedHelpers.findClass("android.app.ActivityThread", lpparam.classLoader),
                                                    "currentApplication"
                                            );
                                            
                                            if (appContext != null) {
                                                saveAccount(appContext, account, password, account);
                                                log("[成功] 账号已自动保存(登录按钮方案): " + account);
                                            }
                                        }
                                    } catch (Throwable t2) {
                                        log("[错误] 延迟保存失败: " + t2.getMessage());
                                    }
                                }
                            }, 2000);  // 登录后2秒保存
                            
                        } catch (Throwable t) {
                            log("[错误] Hook登录按钮点击失败: " + t.getMessage());
                            t.printStackTrace();
                            
                            // 即使出错也要调用原始登录方法
                            try {
                                XposedHelpers.callMethod(activity, "P");
                            } catch (Throwable t2) {
                                // ignore
                            }
                        }
                    }
                });
            } else {
                log("[警告] 未找到登录按钮");
            }
        } catch (Throwable t) {
            log("[错误] Hook登录按钮失败: " + t.getMessage());
        }
    }

    /**
     * Hook登录成功方法，自动保存账号密码
     */
    private void hookLoginSuccess(Class<?> loginViewModelClass, XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            log("正在Hook LoginViewModel.I() - 登录成功保存账号");
            
            XposedHelpers.findAndHookMethod(
                    loginViewModelClass,
                    "I",
                    Map.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            try {
                                log("[触发] LoginViewModel.I() 被调用");
                                Object viewModel = param.thisObject;
                                Map<?, ?> resultMap = (Map<?, ?>) param.args[0];
                                log("[调试] resultMap: " + resultMap);
                                
                                // 获取账号
                                Object accountField = XposedHelpers.callMethod(viewModel, "F");
                                String account = String.valueOf(XposedHelpers.callMethod(accountField, "get"));
                                log("[调试] 账号: " + account);
                                
                                // 获取密码
                                Object passwordField = XposedHelpers.callMethod(viewModel, "D");
                                String password = String.valueOf(XposedHelpers.callMethod(passwordField, "get"));
                                log("[调试] 密码长度: " + (password != null ? password.length() : 0));
                                
                                // 获取用户名
                                String username = String.valueOf(resultMap.get("username"));
                                if (username == null || "null".equals(username)) {
                                    username = String.valueOf(resultMap.get("realname"));
                                }
                                if (username == null || "null".equals(username)) {
                                    username = account;
                                }
                                log("[调试] 用户名: " + username);
                                
                                if (account != null && !account.isEmpty() && !"null".equals(account) &&
                                    password != null && !password.isEmpty() && !"null".equals(password)) {
                                    // 获取Context
                                    try {
                                        Object userData = XposedHelpers.getStaticObjectField(
                                                XposedHelpers.findClass("com.bxkj.base.v2.data.a", lpparam.classLoader),
                                                "f16779d"
                                        );
                                        Object userDataInstance = XposedHelpers.callMethod(userData, "a");
                                        Context context = (Context) XposedHelpers.callMethod(userDataInstance, "h");
                                        
                                        if (context != null) {
                                            saveAccount(context, account, password, username);
                                            log("[成功] 账号已自动保存: " + account);
                                        } else {
                                            log("[警告] Context为null，使用ApplicationContext");
                                            context = (Context) XposedHelpers.callStaticMethod(
                                                    XposedHelpers.findClass("android.app.ActivityThread", lpparam.classLoader),
                                                    "currentApplication"
                                            );
                                            if (context != null) {
                                                saveAccount(context, account, password, username);
                                                log("[成功] 账号已自动保存(ApplicationContext): " + account);
                                            }
                                        }
                                    } catch (Throwable t2) {
                                        log("[错误] 获取Context失败: " + t2.getMessage());
                                        // 尝试备用方案
                                        Context appContext = (Context) XposedHelpers.callStaticMethod(
                                                XposedHelpers.findClass("android.app.ActivityThread", lpparam.classLoader),
                                                "currentApplication"
                                        );
                                        if (appContext != null) {
                                            saveAccount(appContext, account, password, username);
                                            log("[成功] 账号已自动保存(备用方案): " + account);
                                        }
                                    }
                                } else {
                                    log("[跳过] 账号或密码为空");
                                }
                            } catch (Throwable t) {
                                log("[错误] 保存账号失败: " + t.getMessage());
                                t.printStackTrace();
                            }
                        }
                    }
            );
            
            log("LoginViewModel Hook完成");
        } catch (Throwable t) {
            log("Hook LoginViewModel.I()失败: " + t.getMessage());
        }
    }

    /**
     * 添加浮动按钮到DecorView
     */
    private void addFloatingButton(final android.app.Activity activity) {
        try {
            ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
            
            // 检查是否已添加
            if (decorView.findViewWithTag("account_quick_select_floating_btn") != null) {
                log("[跳过] 浮动按钮已存在");
                return;
            }
            
            // 创建极简风格按钮
            Button floatingBtn = new Button(activity);
            floatingBtn.setText("···");  // 使用三个点，更稳定
            floatingBtn.setTextSize(16);
            floatingBtn.setTextColor(0xFF666666);  // 灰色文字
            floatingBtn.setGravity(android.view.Gravity.CENTER);
            floatingBtn.setTag("account_quick_select_floating_btn");
            floatingBtn.setPadding(0, 0, 0, 0);
            floatingBtn.setAllCaps(false);  // 禁用大写转换
            floatingBtn.setIncludeFontPadding(false);
            floatingBtn.setMinWidth(0);
            floatingBtn.setMinHeight(0);
            floatingBtn.setMinimumWidth(0);
            floatingBtn.setMinimumHeight(0);
            
            // 极简圆形背景
            android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
            bg.setColor(0xF5FFFFFF);  // 白色背景
            bg.setCornerRadius(dp(activity, 20));
            bg.setStroke(dp(activity, 1), 0xFFDDDDDD);  // 浅灰边框
            floatingBtn.setBackground(bg);
            
            // 轻微阴影
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                floatingBtn.setElevation(dp(activity, 4));
            }
            
            // 设置位置（右下角，更小尺寸）
            android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(
                    dp(activity, 40),
                    dp(activity, 40)
            );
            params.gravity = android.view.Gravity.BOTTOM | android.view.Gravity.END;
            params.setMargins(0, 0, dp(activity, 16), dp(activity, 80));
            floatingBtn.setLayoutParams(params);
            
            // 点击事件
            floatingBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAccountListDialog(activity, activity);
                }
            });
            
            // 长按事件
            floatingBtn.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    showManageDialog(activity, activity);
                    return true;
                }
            });
            
            // 添加到DecorView
            decorView.addView(floatingBtn);
            log("[成功] 浮动按钮已添加到DecorView");
            
        } catch (Throwable t) {
            log("[错误] 添加浮动按钮失败: " + t.getMessage());
            t.printStackTrace();
        }
    }

    /**
     * 创建快速选择按钮（已废弃，改用浮动按钮）
     */
    private void createQuickSelectButton(Context context, ViewGroup accountLayout, final Object activity) {
        try {
            // 检查是否已添加
            if (accountLayout.findViewWithTag("account_quick_select_btn") != null) {
                log("[跳过] 按钮已存在");
                return;
            }
            
            // 创建一个TextView作为按钮（更灵活）
            TextView selectBtn = new TextView(context);
            selectBtn.setText("📋");
            selectBtn.setTextSize(18);
            selectBtn.setGravity(android.view.Gravity.CENTER);
            selectBtn.setPadding(dp(context, 12), dp(context, 8), dp(context, 12), dp(context, 8));
            selectBtn.setTag("account_quick_select_btn");
            selectBtn.setClickable(true);
            
            // 设置背景
            android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
            bg.setColor(0xFFF5F5F5);
            bg.setCornerRadius(dp(context, 8));
            bg.setStroke(dp(context, 1), 0xFFDDDDDD);
            selectBtn.setBackground(bg);
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(dp(context, 8), 0, 0, 0);
            params.gravity = android.view.Gravity.CENTER_VERTICAL;
            selectBtn.setLayoutParams(params);
            
            // 点击显示账号列表
            selectBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAccountListDialog((Context)XposedHelpers.callMethod(activity, "H"), activity);
                }
            });
            
            // 长按显示管理对话框
            selectBtn.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    showManageDialog((Context)XposedHelpers.callMethod(activity, "H"), activity);
                    return true;
                }
            });
            
            // 添加到布局
            accountLayout.addView(selectBtn);
            log("[成功] 按钮已添加到布局，子View数量: " + accountLayout.getChildCount());
            
        } catch (Throwable t) {
            log("[错误] 创建快速选择按钮失败: " + t.getMessage());
            t.printStackTrace();
        }
    }
    
    /**
     * dp转px
     */
    private int dp(Context context, float dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }

    /**
     * 创建美化的AlertDialog
     */
    private AlertDialog.Builder createStyledDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        return builder;
    }
    
    /**
     * 设置对话框圆角和样式
     */
    private void styleDialog(AlertDialog dialog) {
        if (dialog.getWindow() != null) {
            // 设置圆角背景
            android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
            bg.setColor(0xFFFFFFFF);  // 白色背景
            bg.setCornerRadius(dp(dialog.getContext(), 16));  // 16dp圆角
            dialog.getWindow().setBackgroundDrawable(bg);
            
            // 设置内边距
            View contentView = dialog.getWindow().getDecorView();
            if (contentView != null) {
                contentView.setPadding(
                    dp(dialog.getContext(), 8),
                    dp(dialog.getContext(), 8),
                    dp(dialog.getContext(), 8),
                    dp(dialog.getContext(), 8)
                );
            }
        }
        
        // 设置按钮文字颜色（确保可见）
        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            Button neutralButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
            
            if (positiveButton != null) {
                positiveButton.setTextColor(0xFF2196F3);  // 蓝色
                positiveButton.setAllCaps(false);
            }
            if (negativeButton != null) {
                negativeButton.setTextColor(0xFF666666);  // 灰色
                negativeButton.setAllCaps(false);
            }
            if (neutralButton != null) {
                neutralButton.setTextColor(0xFF666666);  // 灰色
                neutralButton.setAllCaps(false);
            }
        });
    }

    /**
     * 创建EditText圆角背景
     */
    private android.graphics.drawable.Drawable createEditTextBackground(Context context) {
        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setColor(0xFFF5F5F5);  // 浅灰背景
        bg.setCornerRadius(dp(context, 8));  // 8dp圆角
        bg.setStroke(dp(context, 1), 0xFFE0E0E0);  // 浅灰边框
        return bg;
    }

    /**
     * 显示账号列表对话框
     */
    private void showAccountListDialog(Context context, Object activity) {
        try {
            List<AccountInfo> accounts = loadAccounts(context);
            
            if (accounts.isEmpty()) {
                // 创建提示对话框
                TextView messageView = new TextView(context);
                messageView.setText("还没有保存的账号\n\n• 登录成功后会自动保存\n• 长按【···】按钮可手动添加\n\n💾 数据存储位置：\nSharedPreferences\n(账号密码加密存储在本地)");
                messageView.setTextColor(0xFF333333);
                messageView.setTextSize(14);
                messageView.setPadding(dp(context, 24), dp(context, 16), dp(context, 24), dp(context, 16));
                messageView.setLineSpacing(dp(context, 4), 1.0f);
                
                AlertDialog dialog = createStyledDialog(context)
                        .setTitle("暂无账号")
                        .setView(messageView)
                        .setPositiveButton("知道了", null)
                        .create();
                styleDialog(dialog);
                dialog.show();
                return;
            }
            
            // 创建账号列表
            String[] accountNames = new String[accounts.size()];
            for (int i = 0; i < accounts.size(); i++) {
                AccountInfo info = accounts.get(i);
                accountNames[i] = info.username + " (" + info.account + ")";
            }
            
            AlertDialog dialog = createStyledDialog(context)
                    .setTitle("选择账号")
                    .setItems(accountNames, (dlg, which) -> {
                        AccountInfo selected = accounts.get(which);
                        fillAccount(activity, selected);
                    })
                    .setNegativeButton("取消", null)
                    .create();
            styleDialog(dialog);
            dialog.show();
            
        } catch (Throwable t) {
            XposedBridge.log("FuckTSN: 显示账号列表失败: " + t.getMessage());
        }
    }

    /**
     * 显示管理对话框（添加/删除账号）
     */
    private void showManageDialog(Context context, Object activity) {
        try {
            AlertDialog dialog = createStyledDialog(context)
                    .setTitle("账号管理")
                    .setItems(new String[]{"➕ 添加账号", "🗑️ 删除账号", "👁️ 查看账号"}, (dlg, which) -> {
                        switch (which) {
                            case 0:
                                showAddAccountDialog(context);
                                break;
                            case 1:
                                showDeleteAccountDialog(context);
                                break;
                            case 2:
                                showViewAccountsDialog(context);
                                break;
                        }
                    })
                    .setNegativeButton("取消", null)
                    .create();
            styleDialog(dialog);
            dialog.show();
        } catch (Throwable t) {
            XposedBridge.log("FuckTSN: 显示管理对话框失败: " + t.getMessage());
        }
    }

    /**
     * 显示添加账号对话框
     */
    private void showAddAccountDialog(Context context) {
        try {
            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(dp(context, 24), dp(context, 16), dp(context, 24), dp(context, 8));
            
            // 账号标签
            TextView tvAccount = new TextView(context);
            tvAccount.setText("账号");
            tvAccount.setTextColor(0xFF666666);
            tvAccount.setTextSize(12);
            layout.addView(tvAccount);
            
            // 账号输入框
            EditText etAccount = new EditText(context);
            etAccount.setHint("请输入学号");
            etAccount.setTextColor(0xFF333333);
            etAccount.setHintTextColor(0xFFCCCCCC);
            etAccount.setTextSize(14);
            etAccount.setPadding(dp(context, 12), dp(context, 8), dp(context, 12), dp(context, 8));
            etAccount.setBackgroundDrawable(createEditTextBackground(context));
            LinearLayout.LayoutParams accountParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            accountParams.setMargins(0, dp(context, 4), 0, dp(context, 12));
            etAccount.setLayoutParams(accountParams);
            layout.addView(etAccount);
            
            // 密码标签
            TextView tvPassword = new TextView(context);
            tvPassword.setText("密码");
            tvPassword.setTextColor(0xFF666666);
            tvPassword.setTextSize(12);
            layout.addView(tvPassword);
            
            // 密码输入框
            EditText etPassword = new EditText(context);
            etPassword.setHint("请输入密码");
            etPassword.setTextColor(0xFF333333);
            etPassword.setHintTextColor(0xFFCCCCCC);
            etPassword.setTextSize(14);
            etPassword.setPadding(dp(context, 12), dp(context, 8), dp(context, 12), dp(context, 8));
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            etPassword.setBackgroundDrawable(createEditTextBackground(context));
            LinearLayout.LayoutParams passwordParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            passwordParams.setMargins(0, dp(context, 4), 0, dp(context, 12));
            etPassword.setLayoutParams(passwordParams);
            layout.addView(etPassword);
            
            // 备注标签
            TextView tvUsername = new TextView(context);
            tvUsername.setText("备注");
            tvUsername.setTextColor(0xFF666666);
            tvUsername.setTextSize(12);
            layout.addView(tvUsername);
            
            // 备注输入框
            EditText etUsername = new EditText(context);
            etUsername.setHint("昵称或姓名（可选）");
            etUsername.setTextColor(0xFF333333);
            etUsername.setHintTextColor(0xFFCCCCCC);
            etUsername.setTextSize(14);
            etUsername.setPadding(dp(context, 12), dp(context, 8), dp(context, 12), dp(context, 8));
            etUsername.setBackgroundDrawable(createEditTextBackground(context));
            LinearLayout.LayoutParams usernameParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            usernameParams.setMargins(0, dp(context, 4), 0, 0);
            etUsername.setLayoutParams(usernameParams);
            layout.addView(etUsername);
            
            AlertDialog dialog = createStyledDialog(context)
                    .setTitle("添加账号")
                    .setView(layout)
                    .setPositiveButton("添加", (dlg, which) -> {
                        String account = etAccount.getText().toString().trim();
                        String password = etPassword.getText().toString().trim();
                        String username = etUsername.getText().toString().trim();
                        
                        if (account.isEmpty() || password.isEmpty()) {
                            AlertDialog errDialog = createStyledDialog(context)
                                    .setTitle("提示")
                                    .setMessage("账号和密码不能为空")
                                    .setPositiveButton("确定", null)
                                    .create();
                            styleDialog(errDialog);
                            errDialog.show();
                            return;
                        }
                        
                        if (username.isEmpty()) {
                            username = account;
                        }
                        
                        saveAccount(context, account, password, username);
                        
                        AlertDialog successDialog = createStyledDialog(context)
                                .setMessage("✅ 账号添加成功")
                                .setPositiveButton("确定", null)
                                .create();
                        styleDialog(successDialog);
                        successDialog.show();
                    })
                    .setNegativeButton("取消", null)
                    .create();
            styleDialog(dialog);
            dialog.show();
        } catch (Throwable t) {
            XposedBridge.log("FuckTSN: 显示添加账号对话框失败: " + t.getMessage());
        }
    }

    /**
     * 显示删除账号对话框
     */
    private void showDeleteAccountDialog(Context context) {
        try {
            List<AccountInfo> accounts = loadAccounts(context);
            
            if (accounts.isEmpty()) {
                AlertDialog dialog = createStyledDialog(context)
                        .setTitle("提示")
                        .setMessage("没有保存的账号")
                        .setPositiveButton("确定", null)
                        .create();
                styleDialog(dialog);
                dialog.show();
                return;
            }
            
            String[] accountNames = new String[accounts.size()];
            for (int i = 0; i < accounts.size(); i++) {
                AccountInfo info = accounts.get(i);
                accountNames[i] = info.username + " (" + info.account + ")";
            }
            
            AlertDialog dialog = createStyledDialog(context)
                    .setTitle("删除账号")
                    .setItems(accountNames, (dlg, which) -> {
                        AccountInfo toDelete = accounts.get(which);
                        deleteAccount(context, toDelete.account);
                        
                        AlertDialog successDialog = createStyledDialog(context)
                                .setMessage("✅ 账号已删除")
                                .setPositiveButton("确定", null)
                                .create();
                        styleDialog(successDialog);
                        successDialog.show();
                    })
                    .setNegativeButton("取消", null)
                    .create();
            styleDialog(dialog);
            dialog.show();
        } catch (Throwable t) {
            XposedBridge.log("FuckTSN: 显示删除账号对话框失败: " + t.getMessage());
        }
    }

    /**
     * 显示查看账号对话框
     */
    private void showViewAccountsDialog(Context context) {
        try {
            List<AccountInfo> accounts = loadAccounts(context);
            
            if (accounts.isEmpty()) {
                AlertDialog dialog = createStyledDialog(context)
                        .setTitle("提示")
                        .setMessage("没有保存的账号")
                        .setPositiveButton("确定", null)
                        .create();
                styleDialog(dialog);
                dialog.show();
                return;
            }
            
            // 创建滚动视图
            android.widget.ScrollView scrollView = new android.widget.ScrollView(context);
            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(dp(context, 24), dp(context, 16), dp(context, 24), dp(context, 16));
            scrollView.addView(layout);
            
            for (int i = 0; i < accounts.size(); i++) {
                AccountInfo info = accounts.get(i);
                
                // 创建账号卡片
                LinearLayout card = new LinearLayout(context);
                card.setOrientation(LinearLayout.VERTICAL);
                card.setPadding(dp(context, 16), dp(context, 12), dp(context, 16), dp(context, 12));
                card.setBackgroundDrawable(createEditTextBackground(context));
                
                // 备注名
                TextView tvUsername = new TextView(context);
                tvUsername.setText(info.username);
                tvUsername.setTextColor(0xFF333333);
                tvUsername.setTextSize(16);
                tvUsername.setTypeface(null, android.graphics.Typeface.BOLD);
                card.addView(tvUsername);
                
                // 账号
                TextView tvAccount = new TextView(context);
                tvAccount.setText("账号: " + info.account);
                tvAccount.setTextColor(0xFF666666);
                tvAccount.setTextSize(14);
                tvAccount.setPadding(0, dp(context, 4), 0, 0);
                card.addView(tvAccount);
                
                // 密码
                TextView tvPassword = new TextView(context);
                tvPassword.setText("密码: " + info.password);
                tvPassword.setTextColor(0xFF666666);
                tvPassword.setTextSize(14);
                tvPassword.setPadding(0, dp(context, 2), 0, 0);
                card.addView(tvPassword);
                
                LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                cardParams.setMargins(0, 0, 0, dp(context, 12));
                card.setLayoutParams(cardParams);
                layout.addView(card);
            }
            
            AlertDialog dialog = createStyledDialog(context)
                    .setTitle("已保存的账号 (" + accounts.size() + ")")
                    .setView(scrollView)
                    .setPositiveButton("确定", null)
                    .create();
            styleDialog(dialog);
            dialog.show();
        } catch (Throwable t) {
            XposedBridge.log("FuckTSN: 显示查看账号对话框失败: " + t.getMessage());
        }
    }

    /**
     * 填充账号密码到输入框（同时应用设备指纹）
     */
    private void fillAccount(Object activity, AccountInfo accountInfo) {
        try {
            // 获取 ViewModel
            Object viewModel = XposedHelpers.callMethod(activity, "D");
            
            // 设置账号
            Object accountField = XposedHelpers.callMethod(viewModel, "F");
            XposedHelpers.callMethod(accountField, "set", accountInfo.account);
            
            // 设置密码
            Object passwordField = XposedHelpers.callMethod(viewModel, "D");
            XposedHelpers.callMethod(passwordField, "set", accountInfo.password);
            
            // 应用该账号的设备指纹
            if (accountInfo.deviceId != null && !accountInfo.deviceId.isEmpty() &&
                accountInfo.model != null && !accountInfo.model.isEmpty()) {
                
                Context context = (Context) activity;
                applyDeviceFingerprint(context, accountInfo.deviceId, accountInfo.model);
                
                log("✅ 已切换到账号 " + accountInfo.account + " 的设备指纹");
                log("  - 设备ID: " + accountInfo.deviceId);
                log("  - 设备型号: " + accountInfo.model);
            } else {
                log("⚠️ 账号 " + accountInfo.account + " 没有设备指纹，将在登录时生成");
            }
            
            XposedBridge.log("FuckTSN: 已填充账号: " + accountInfo.account);
        } catch (Throwable t) {
            XposedBridge.log("FuckTSN: 填充账号失败: " + t.getMessage());
        }
    }

    /**
     * 应用设备指纹（设备ID + 设备型号）
     */
    private void applyDeviceFingerprint(Context context, String deviceId, String model) {
        try {
            // 1. 应用设备ID
            SharedPreferences deviceIdPrefs = context.getSharedPreferences("gank_device_id.xml", Context.MODE_PRIVATE);
            deviceIdPrefs.edit().putString("gank_device_id", deviceId).apply();
            
            // 2. 拆分厂商名和型号
            // 格式: "Xiaomi-M2012K11AC" -> MANUFACTURER="Xiaomi", MODEL="M2012K11AC"
            // 格式: "SM-G9980" -> MANUFACTURER="SM", MODEL="G9980"
            String manufacturer = model;
            String actualModel = model;
            
            if (model.contains("-")) {
                int dashIndex = model.indexOf("-");
                manufacturer = model.substring(0, dashIndex);
                actualModel = model.substring(dashIndex + 1);
            }
            
            // 3. 同时替换 Build.MANUFACTURER 和 Build.MODEL
            XposedHelpers.setStaticObjectField(android.os.Build.class, "MANUFACTURER", manufacturer);
            XposedHelpers.setStaticObjectField(android.os.Build.class, "MODEL", actualModel);
            
            // 4. 同时写入设备型号缓存（供显示使用）
            SharedPreferences modelPrefs = context.getSharedPreferences("device_model_cache", Context.MODE_PRIVATE);
            modelPrefs.edit().putString("cached_model", model).apply();
            
            log("✅ 设备指纹已应用");
            log("  - MANUFACTURER: " + manufacturer);
            log("  - MODEL: " + actualModel);
            log("  - 完整型号: " + model);
        } catch (Throwable t) {
            log("❌ 应用设备指纹失败: " + t.getMessage());
        }
    }

    /**
     * 确保账号有设备指纹并应用（登录前调用）
     */
    private void ensureAndApplyDeviceFingerprint(Context context, String account, String password) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            String jsonStr = prefs.getString(KEY_ACCOUNTS, "[]");
            JSONArray accounts = new JSONArray(jsonStr);
            
            String deviceId = null;
            String model = null;
            boolean needSave = false;
            
            // 查找该账号的设备指纹
            for (int i = 0; i < accounts.length(); i++) {
                JSONObject obj = accounts.getJSONObject(i);
                if (obj.getString("account").equals(account)) {
                    deviceId = obj.optString("deviceId", null);
                    model = obj.optString("model", null);
                    
                    // 如果没有设备指纹，生成新的
                    if (deviceId == null || deviceId.isEmpty()) {
                        deviceId = java.util.UUID.randomUUID().toString();
                        obj.put("deviceId", deviceId);
                        needSave = true;
                        log("[生成] 账号 " + account + " 的设备ID");
                    }
                    if (model == null || model.isEmpty()) {
                        model = getRandomModel();
                        obj.put("model", model);
                        needSave = true;
                        log("[生成] 账号 " + account + " 的设备型号");
                    }
                    break;
                }
            }
            
            // 如果账号不存在，创建新账号
            if (deviceId == null || model == null) {
                deviceId = java.util.UUID.randomUUID().toString();
                model = getRandomModel();
                
                JSONObject newAccount = new JSONObject();
                newAccount.put("account", account);
                newAccount.put("password", password);
                newAccount.put("username", account);
                newAccount.put("deviceId", deviceId);
                newAccount.put("model", model);
                accounts.put(newAccount);
                needSave = true;
                
                log("[新增] 账号 " + account + " 并生成设备指纹");
            }
            
            // 保存更新
            if (needSave) {
                prefs.edit().putString(KEY_ACCOUNTS, accounts.toString()).apply();
            }
            
            // 立即应用设备指纹
            applyDeviceFingerprint(context, deviceId, model);
            log("✅ 已为账号 " + account + " 应用设备指纹（登录前）");
            
        } catch (Exception e) {
            log("[错误] 确保设备指纹失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 保存账号密码（同时管理设备指纹）
     */
    private void saveAccount(Context context, String account, String password, String username) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            String jsonStr = prefs.getString(KEY_ACCOUNTS, "[]");
            
            JSONArray accounts = new JSONArray(jsonStr);
            
            // 检查是否已存在，存在则更新
            boolean found = false;
            String finalDeviceId = null;
            String finalModel = null;
            
            for (int i = 0; i < accounts.length(); i++) {
                JSONObject obj = accounts.getJSONObject(i);
                if (obj.getString("account").equals(account)) {
                    obj.put("password", password);
                    
                    // 智能更新备注：
                    // 如果新备注等于账号（说明是自动保存且未获取到姓名），且原备注不等于账号（说明用户设置过备注），则保留原备注
                    String oldUsername = obj.optString("username", account);
                    if (!username.equals(account) || oldUsername.equals(account)) {
                        obj.put("username", username);
                    }
                    
                    // 如果没有设备指纹，生成新的
                    if (!obj.has("deviceId") || obj.optString("deviceId", "").isEmpty()) {
                        finalDeviceId = java.util.UUID.randomUUID().toString();
                        obj.put("deviceId", finalDeviceId);
                        log("[生成] 账号 " + account + " 的设备ID");
                    } else {
                        finalDeviceId = obj.getString("deviceId");
                    }
                    
                    if (!obj.has("model") || obj.optString("model", "").isEmpty()) {
                        finalModel = getRandomModel();
                        obj.put("model", finalModel);
                        log("[生成] 账号 " + account + " 的设备型号");
                    } else {
                        finalModel = obj.getString("model");
                    }
                    
                    found = true;
                    break;
                }
            }
            
            // 不存在则添加（同时生成设备指纹）
            if (!found) {
                finalDeviceId = java.util.UUID.randomUUID().toString();
                finalModel = getRandomModel();
                
                JSONObject newAccount = new JSONObject();
                newAccount.put("account", account);
                newAccount.put("password", password);
                newAccount.put("username", username);
                newAccount.put("deviceId", finalDeviceId);
                newAccount.put("model", finalModel);
                accounts.put(newAccount);
                log("[新增] 账号 " + account + " 并生成设备指纹");
            }
            
            // 保存到 SharedPreferences
            prefs.edit().putString(KEY_ACCOUNTS, accounts.toString()).apply();
            
            // 立即应用该账号的设备指纹
            if (finalDeviceId != null && finalModel != null) {
                applyDeviceFingerprint(context, finalDeviceId, finalModel);
                log("✅ 已自动应用账号 " + account + " 的设备指纹");
            }
        } catch (Exception e) {
            XposedBridge.log("FuckTSN: 保存账号失败: " + e.getMessage());
        }
    }

    /**
     * 从型号池中随机选择一个型号
     */
    private String getRandomModel() {
        String[] MODEL_POOL = {
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
        java.util.Random random = new java.util.Random();
        return MODEL_POOL[random.nextInt(MODEL_POOL.length)];
    }

    /**
     * 加载账号列表（包含设备指纹）
     */
    private List<AccountInfo> loadAccounts(Context context) {
        List<AccountInfo> result = new ArrayList<>();
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            String jsonStr = prefs.getString(KEY_ACCOUNTS, "[]");
            
            JSONArray accounts = new JSONArray(jsonStr);
            for (int i = 0; i < accounts.length(); i++) {
                JSONObject obj = accounts.getJSONObject(i);
                AccountInfo info = new AccountInfo();
                info.account = obj.getString("account");
                info.password = obj.getString("password");
                info.username = obj.optString("username", obj.getString("account"));
                info.deviceId = obj.optString("deviceId", "");
                info.model = obj.optString("model", "");
                result.add(info);
            }
        } catch (Exception e) {
            XposedBridge.log("FuckTSN: 加载账号列表失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 删除账号
     */
    private void deleteAccount(Context context, String account) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            String jsonStr = prefs.getString(KEY_ACCOUNTS, "[]");
            
            JSONArray accounts = new JSONArray(jsonStr);
            JSONArray newAccounts = new JSONArray();
            
            for (int i = 0; i < accounts.length(); i++) {
                JSONObject obj = accounts.getJSONObject(i);
                if (!obj.getString("account").equals(account)) {
                    newAccounts.put(obj);
                }
            }
            
            prefs.edit().putString(KEY_ACCOUNTS, newAccounts.toString()).apply();
        } catch (Exception e) {
            XposedBridge.log("FuckTSN: 删除账号失败: " + e.getMessage());
        }
    }

    /**
     * 账号信息类（包含设备指纹）
     */
    private static class AccountInfo {
        String account;
        String password;
        String username;
        String deviceId;
        String model;
    }
}

