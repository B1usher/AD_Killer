package com.gjh.adkiller;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;
import android.widget.TextView;

import com.gjh.AD_Killer;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;


import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class Settings {
    private final String TAG = "Settings";
    private final String Preference_Name = "ADKiller_Config";

    private SharedPreferences mPreference;
    private SharedPreferences.Editor mEditor;
    private Gson mJson;

    // Singleton
    private static Settings ourInstance = new Settings();
    public static Settings getInstance() {
        return ourInstance;
    }
    private Settings() {
        initSettings();
    }

    private void initSettings() {
        mPreference = AD_Killer.getAppContext().getSharedPreferences(Preference_Name, Activity.MODE_PRIVATE);
        mEditor = mPreference.edit();
        mJson = new Gson();

        // init all settings from SharedPreferences 从SharedReferences初始化所有设置
        bSkipAdNotification = mPreference.getBoolean(SKIP_AD_NOTIFICATION, true);

        // initial duration of skip ad process 跳过ad过程的初始持续时间
        iSkipAdDuration = mPreference.getInt(SKIP_AD_DURATION, 4);

        // find all system packages, and set them as default value for whitelist 查找所有系统包，并将其设置为白名单的默认值
        PackageManager packageManager = AD_Killer.getAppContext().getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> ResolveInfoList = packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL);
        Set<String> pkgSystems = new HashSet<>();
        for (ResolveInfo e : ResolveInfoList) {
            if ((e.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM) {
                pkgSystems.add(e.activityInfo.packageName);
            }
        }

        // init whitelist of packages 初始化白名单或包
        // https://stackoverflow.com/questions/10720028/android-sharedpreferences-not-saving
        // Note that you must not modify the set instance returned by this call. The consistency of the stored data is not guaranteed if you do, nor is your ability to modify the instance at all.
        // 请注意，您不能修改此调用返回的set实例。如果您这样做，则无法保证存储数据的一致性，也根本无法修改实例。
        setWhiteListPackages = new HashSet<String>(mPreference.getStringSet(WHITELIST_PACKAGE, pkgSystems));

        // init key words 初始化关键字
        String json = mPreference.getString(KEY_WORDS_LIST, "[\"跳过\"]");
        if(json != null) {
            Type type = new TypeToken<ArrayList<String>>() {}.getType();
            listKeyWords = mJson.fromJson(json, type);
        } else {
            listKeyWords = new ArrayList<>();
        }

        // load activity widgets 加载活动小部件
        json = mPreference.getString(PACKAGE_WIDGETS, null);
        if (json != null) {
            Type type = new TypeToken<TreeMap<String, Set<PackageWidgetDescription>>>() {}.getType();
            mapPackageWidgets = mJson.fromJson(json, type);
        } else {
            mapPackageWidgets = new TreeMap<>();
        }

        // load activity positions 加载活动位置
        json = mPreference.getString(PACKAGE_POSITIONS, null);
        if (json != null) {
            Type type = new TypeToken<TreeMap<String, PackagePositionDescription>>() {}.getType();
            mapPackagePositions = mJson.fromJson(json, type);
        } else {
            mapPackagePositions = new TreeMap<>();
        }

    }

    // notification on skip ads? 跳过广告的通知？
    private static final String SKIP_AD_NOTIFICATION = "SKIP_AD_NOTIFICATION";
    private boolean bSkipAdNotification;
    public boolean isSkipAdNotification() {
        return bSkipAdNotification;
    }
    public void setSkipAdNotification(boolean bSkipAdNotification) {
        if (this.bSkipAdNotification != bSkipAdNotification) {
            this.bSkipAdNotification = bSkipAdNotification;
            mEditor.putBoolean(SKIP_AD_NOTIFICATION, this.bSkipAdNotification);
            mEditor.apply();
        }
    }

    //hide process?-----  是否隐藏进程
    private static final String SKIP_AD_PROCESS = "SKIP_AD_PROCESS";
    private boolean bSkipAdProcess;
    public boolean isbSkipAdProcess() { return bSkipAdProcess;}
    public void setSkipAdProcess(boolean bSkipAdProcess) {
        if(this.bSkipAdProcess != bSkipAdProcess) {
            this.bSkipAdProcess = bSkipAdProcess;
            mEditor.putBoolean(SKIP_AD_PROCESS, this.bSkipAdProcess);
            mEditor.apply();
        }
    }

    // duration of skip ad process 跳过ad进程的持续时间
    private static final String SKIP_AD_DURATION = "SKIP_AD_DURATION";
    private int iSkipAdDuration;
    public int getSkipAdDuration() {
        return iSkipAdDuration;
    }
    public void setSkipAdDuration(int iSkipAdDuration) {
        if (this.iSkipAdDuration != iSkipAdDuration) {
            this.iSkipAdDuration = iSkipAdDuration;
            mEditor.putInt(SKIP_AD_DURATION, this.iSkipAdDuration);
            mEditor.apply();
        }
    }


    // whitelist of packages
    private static final String WHITELIST_PACKAGE = "WHITELIST_PACKAGE";
    private Set<String> setWhiteListPackages;
    public Set<String> getWhitelistPackages() {
        return setWhiteListPackages;
    }
    public void setWhitelistPackages(Set<String> pkgs) {
        setWhiteListPackages.clear();
        setWhiteListPackages.addAll(pkgs);
        // https://stackoverflow.com/questions/10720028/android-sharedpreferences-not-saving
        mEditor.putStringSet(WHITELIST_PACKAGE, new HashSet<String>(setWhiteListPackages));
        mEditor.apply();
    }

    // list of key words 包裹白名单
    private static final String KEY_WORDS_LIST = "KEY_WORDS_LIST";
    private ArrayList<String> listKeyWords;
    public List<String> getKeyWordList() { return listKeyWords; }
    public String getKeyWordsAsString() { return String.join(" ", listKeyWords); }
    public void setKeyWordList(String text) {
        String keys[] = text.split(" ");
        listKeyWords.clear();
        listKeyWords.addAll(Arrays.asList(keys));
        String json = mJson.toJson(listKeyWords);
        mEditor.putString(KEY_WORDS_LIST, json);
        mEditor.apply();
    }

    // map of key activity widgets 关键活动小部件的映射
    private static final String PACKAGE_WIDGETS = "PACKAGE_WIDGETS";
    private Map<String, Set<PackageWidgetDescription>> mapPackageWidgets;
    public Map<String, Set<PackageWidgetDescription>> getPackageWidgets() { return mapPackageWidgets; }
    public void setPackageWidgets(Map<String, Set<PackageWidgetDescription>> map) {
        mapPackageWidgets = map;
        String json = mJson.toJson(mapPackageWidgets);
//        Log.d(TAG, json);
        mEditor.putString(PACKAGE_WIDGETS, json);
        mEditor.apply();
    }
    public String getPackageWidgetsInString() {
        String json = mJson.toJson(mapPackageWidgets);
        return json;
    }
    public boolean setPackageWidgetsInString(String value) {
        if (value != null) {
            try {
                Type type = new TypeToken<TreeMap<String, Set<PackageWidgetDescription>>>() {
                }.getType();
                mapPackageWidgets = mJson.fromJson(value, type);
                mEditor.putString(PACKAGE_WIDGETS, value);
                mEditor.apply();
            } catch (JsonSyntaxException e) {
                Log.d(TAG, Utilities.getTraceStackInString(e));
                return false;
            }
        }
        return false;
    }

    // map of key package positions 关键包装位置图
    private static final String PACKAGE_POSITIONS = "PACKAGE_POSITIONS";
    private Map<String, PackagePositionDescription> mapPackagePositions;
    public Map<String, PackagePositionDescription> getPackagePositions() { return mapPackagePositions; }
    public void setPackagePositions(Map<String, PackagePositionDescription> map) {
        mapPackagePositions = map;
        String json = mJson.toJson(mapPackagePositions);
//        Log.d(TAG, json);
        mEditor.putString(PACKAGE_POSITIONS, json);
        mEditor.apply();
    }

//    // 总的跳过次数
//    private static final String SKIP_NUM = "SKIP_NUM";
//    private int skipNum = 0;
//    public void setSkipNum() {
//        skipNum = skipNum + 1;
//    }
//    public int getSkipNum() {
//        return skipNum;
//    }


}
