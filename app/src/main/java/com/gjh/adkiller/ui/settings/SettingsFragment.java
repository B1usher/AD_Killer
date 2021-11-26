package com.gjh.adkiller.ui.settings;

import static android.content.Context.WINDOW_SERVICE;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;

import com.gjh.adkiller.ADKillerService;
import com.gjh.adkiller.PackagePositionDescription;
import com.gjh.adkiller.PackageWidgetDescription;
import com.gjh.adkiller.R;
import com.gjh.adkiller.Settings;
import com.gjh.adkiller.Utilities;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SettingsFragment extends PreferenceFragmentCompat {

    private final String TAG = getClass().getName();
    LayoutInflater inflater;
    PackageManager packageManager;
    WindowManager winManager;

    Settings mSetting;

    MultiSelectListPreference activity_positions;
    MultiSelectListPreference activity_widgets;
    Map<String, Set<PackageWidgetDescription>> mapActivityWidgets;
    Map<String, PackagePositionDescription> mapActivityPositions;


    // 隐藏
    public void showtaskbar (boolean show){

        ActivityManager am = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        if (am != null) {
            List<ActivityManager.AppTask> tasks;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                tasks = am.getAppTasks();
                if (tasks != null && tasks.size() > 0) {
                    tasks.get(0).setExcludeFromRecents(!show);
                }
            }
        }
    }


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.ad_killer_preference, rootKey);

        mSetting = Settings.getInstance();

        initPreferences();

        winManager = (WindowManager) getActivity().getSystemService(WINDOW_SERVICE);
        inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        packageManager = getActivity().getPackageManager();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        // 获取BottomNavigationView的高度
        int resourceId = getResources().getIdentifier("design_bottom_navigation_height", "dimen", getActivity().getPackageName());
        int height = 147;
        if (resourceId > 0) {
            height = getResources().getDimensionPixelSize(resourceId);
        }

        // 为首选项片段设置底部填充，以便可以正确显示所有部分
        view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom() + height);
        return view;
    }

    private void initPreferences() {

        CheckBoxPreference notification = findPreference("skip_ad_notification");
        if(notification != null) {
            notification.setChecked(mSetting.isSkipAdNotification());
            notification.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Boolean value = (Boolean) newValue;
                    mSetting.setSkipAdNotification(value);

                    return true;
                }
            });
        }

        final SeekBarPreference duration = findPreference("skip_ad_duration");
        if(duration != null) {
            duration.setMax(10);
            duration.setMin(1);
            duration.setUpdatesContinuously(true);
            duration.setValue(mSetting.getSkipAdDuration());

            duration.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    int value = duration.getValue() + duration.getMin();
                    mSetting.setSkipAdDuration(value);

                    return true;
                }
            });
        }


        //hide 隐藏进程
        CheckBoxPreference process = findPreference("skip_ad_process");
        if(process != null) {
            process.setChecked(mSetting.isbSkipAdProcess());
            process.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Boolean value = (Boolean) newValue;
                    mSetting.setSkipAdProcess(value);

                    showtaskbar(!value);

                    return true;
                }
            });
        }



        // 检测跳过广告按钮的关键字
        EditTextPreference textKeyWords = findPreference("setting_key_words");
        if(textKeyWords != null) {
            textKeyWords.setText(mSetting.getKeyWordsAsString());
            textKeyWords.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String text = newValue.toString();
                    mSetting.setKeyWordList(text);

                    // 通知可访问性以刷新包
                    if (ADKillerService.serviceImpl != null) {
                        ADKillerService.serviceImpl.receiverHandler.sendEmptyMessage(ADKillerService.ACTION_REFRESH_KEYWORDS);
                    }

                    return true;
                }
            });
        }

        // 选择要列入白名单的包
        Preference whitelist = findPreference("setting_whitelist");
        if(whitelist != null) {
            whitelist.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    // 查找所有包
                    List<String> list = new ArrayList<>();
                    Intent intent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
                    List<ResolveInfo> ResolveInfoList = packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL);
                    for (ResolveInfo e : ResolveInfoList) {
//                        Log.d(TAG, "launcher - " + e.activityInfo.packageName);
                        list.add(e.activityInfo.packageName);
                    }

                    // 为包生成AppInformation
                    final ArrayList<AppInformation> listApp = new ArrayList<>();
                    Set<String> pkgWhitelist = mSetting.getWhitelistPackages();
                    for (String pkgName : list) {
                        try {
                            ApplicationInfo info = packageManager.getApplicationInfo(pkgName, PackageManager.GET_META_DATA);
                            AppInformation appInfo = new AppInformation(pkgName, packageManager.getApplicationLabel(info).toString(), packageManager.getApplicationIcon(info));
                            appInfo.isChecked = pkgWhitelist.contains(pkgName);
                            listApp.add(appInfo);
                        } catch (PackageManager.NameNotFoundException e) {
                            Log.e(TAG, Utilities.getTraceStackInString(e));
                        }
                    }

                    // sort apps
                    Collections.sort(listApp);

                    // listApp adapter
                    BaseAdapter baseAdapter = new BaseAdapter() {
                        @Override
                        public int getCount() {
                            return listApp.size();
                        }

                        @Override
                        public Object getItem(int position) {
                            return listApp.get(position);
                        }

                        @Override
                        public long getItemId(int position) {
                            return position;
                        }

                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            ViewHolder holder;
                            if (convertView == null) {
                                convertView = inflater.inflate(R.layout.layout_package_information, null);
                                holder = new ViewHolder(convertView);
                                convertView.setTag(holder);
                            } else {
                                holder = (ViewHolder) convertView.getTag();
                            }
                            AppInformation app = listApp.get(position);
                            holder.textView.setText(app.applicationName);
                            holder.imageView.setImageDrawable(app.applicationIcon);
                            holder.checkBox.setChecked(app.isChecked);
                            return convertView;
                        }
                    };

                    // 膨胀对话框视图
                    View viewAppList = inflater.inflate(R.layout.layout_select_packages, null);
                    ListView listView = viewAppList.findViewById(R.id.listView);
                    listView.setAdapter(baseAdapter);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            CheckBox item = ((ViewHolder) view.getTag()).checkBox;
                            AppInformation app = listApp.get(position);
                            app.isChecked = !app.isChecked;
                            item.setChecked(app.isChecked);
                        }
                    });


                    final AlertDialog dialog = new AlertDialog.Builder(getContext())
                            .setView(viewAppList)
                            .create();

                    Button btCancel = viewAppList.findViewById(R.id.button_cancel);
                    if(btCancel != null) {
                        btCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.dismiss();
                            }
                        });
                    }
                    Button btConfirm = viewAppList.findViewById(R.id.button_confirm);
                    if(btConfirm != null) {
                        btConfirm.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // 保存已检查的包
                                Set<String> pkgWhitelist = new HashSet<>();
                                for(AppInformation app: listApp) {
                                    if(app.isChecked) {
                                        pkgWhitelist.add(app.packageName);
                                    }
                                }
                                mSetting.setWhitelistPackages(pkgWhitelist);

                                // 通知可访问性以刷新包
                                if (ADKillerService.serviceImpl != null) {
                                    ADKillerService.serviceImpl.receiverHandler.sendEmptyMessage(ADKillerService.ACTION_REFRESH_PACKAGE);
                                }

                                dialog.dismiss();
                            }
                        });
                    }

                    // show the dialog
                    dialog.show();
                    return true;
                } // public boolean onPreferenceClick(Preference preference) {

                final HanyuPinyinOutputFormat outputFormat = new HanyuPinyinOutputFormat();
                class AppInformation implements Comparable{
                    String packageName;
                    String applicationName;
                    String applicationNamePinyin;
                    Drawable applicationIcon;
                    boolean isChecked;

                    public AppInformation(String packageName, String applicationName, Drawable applicationIcon) {
                        this.packageName = packageName;
                        this.applicationName = applicationName;
                        try {
                            applicationNamePinyin = PinyinHelper.toHanYuPinyinString(this.applicationName, outputFormat, "", true);
                        } catch (BadHanyuPinyinOutputFormatCombination badHanyuPinyinOutputFormatCombination) {
                            applicationNamePinyin = applicationName;
                            Log.e(TAG, Utilities.getTraceStackInString(badHanyuPinyinOutputFormatCombination));
                        }
                        this.applicationIcon = applicationIcon;
                        this.isChecked = false;
                    }

                    @Override
                    public int compareTo(Object o) {
                        AppInformation other = (AppInformation) o;

                        if(this.isChecked && !other.isChecked) {
                            return -11;
                        } else if (!this.isChecked && other.isChecked) {
                            return 1;
                        } else {
                            //
                            return this.applicationNamePinyin.compareTo(other.applicationNamePinyin);
                        }
                    }
                } // class AppInformation

                class ViewHolder {
                    TextView textView;
                    ImageView imageView;
                    CheckBox checkBox;

                    public ViewHolder(View v) {
                        textView = v.findViewById(R.id.name);
                        imageView = v.findViewById(R.id.img);
                        checkBox = v.findViewById(R.id.check);
                    }
                } // class ViewHolder {

            });
        }

        // 让用户自定义跳过广告按钮或包的位置
        Preference activity_customization = findPreference("setting_activity_customization");
        if(activity_customization != null) {
            activity_customization.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if(ADKillerService.serviceImpl != null) {
                        ADKillerService.serviceImpl.receiverHandler.sendEmptyMessage(ADKillerService.ACTION_ACTIVITY_CUSTOMIZATION);
                    } else {
                        Toast.makeText(getContext(),"AD Killer服务未运行，请打开无障碍服务!", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            });
        }

        // 管理保存的活动小部件
        activity_widgets = (MultiSelectListPreference) findPreference("setting_activity_widgets");
        mapActivityWidgets = Settings.getInstance().getPackageWidgets();
        updateMultiSelectListPreferenceEntries(activity_widgets, mapActivityWidgets.keySet());
        activity_widgets.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                HashSet<String> results = (HashSet<String>) newValue;
//                Log.d(TAG, "size " + results.size());

                // 更新活动小部件
                Set<String> keys = new HashSet<>(mapActivityWidgets.keySet());
                for(String key: keys){
                    if(!results.contains(key)) {
                        // 未选择保留此密钥，请删除该条目
                        mapActivityWidgets.remove(key);
                    }
                }
                Settings.getInstance().setPackageWidgets(mapActivityWidgets);

                // Refesh多选列表首选项
                updateMultiSelectListPreferenceEntries(activity_widgets, mapActivityWidgets.keySet());

                // send message to accessibility service
                if(ADKillerService.serviceImpl != null) {
                    ADKillerService.serviceImpl.receiverHandler.sendEmptyMessage(ADKillerService.ACTION_REFRESH_CUSTOMIZED_ACTIVITY);
                }

                return true;
            }
        });


        // 通过编辑原始设置管理“定制包小部件”的高级方法
        Preference package_widgets_advance = findPreference("setting_activity_widgets_advanced");
        if(package_widgets_advance != null) {
            package_widgets_advance.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    ManagePackageWidgetsDialogFragment newFragment = new ManagePackageWidgetsDialogFragment();
                    newFragment.show(fragmentManager, "dialog");
                    return true;
                }
            });
        }



            // 管理保存的活动位置
        activity_positions = (MultiSelectListPreference) findPreference("setting_activity_positions");
        mapActivityPositions = Settings.getInstance().getPackagePositions();
        updateMultiSelectListPreferenceEntries(activity_positions, mapActivityPositions.keySet());
        activity_positions.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                HashSet<String> results = (HashSet<String>) newValue;
//                Log.d(TAG, "size " + results.size());

                // update activity widgets
                Set<String> keys = new HashSet<>(mapActivityPositions.keySet());
                for(String key: keys){
                    if(!results.contains(key)) {
                        // this key is not selected to keep, remove the entry
                        mapActivityPositions.remove(key);
                    }
                }
                Settings.getInstance().setPackagePositions(mapActivityPositions);

                // refresh MultiSelectListPreference
                updateMultiSelectListPreferenceEntries(activity_positions, mapActivityPositions.keySet());

                // send message to accessibility service
                if(ADKillerService.serviceImpl != null) {
                    ADKillerService.serviceImpl.receiverHandler.sendEmptyMessage(ADKillerService.ACTION_REFRESH_CUSTOMIZED_ACTIVITY);
                }

                return true;
            }
        });


    }

    void updateMultiSelectListPreferenceEntries(MultiSelectListPreference preference, Set<String> keys){
        if(preference == null || keys == null)
            return;
        CharSequence[] entries = keys.toArray(new CharSequence[keys.size()]);
        preference.setEntries(entries);
        preference.setEntryValues(entries);
        preference.setValues(keys);
    }

    @Override
    public void onResume() {
        super.onResume();

        // 这些值可以通过添加新的小部件或位置、更新这两个多线的条目来更改
        mapActivityWidgets = Settings.getInstance().getPackageWidgets();
        updateMultiSelectListPreferenceEntries(activity_widgets, mapActivityWidgets.keySet());

        mapActivityPositions = Settings.getInstance().getPackagePositions();
        updateMultiSelectListPreferenceEntries(activity_positions, mapActivityPositions.keySet());
    }
}