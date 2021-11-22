package com.gjh.adkiller.ui.home;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.gjh.adkiller.ADKillerService;
import com.gjh.adkiller.R;

public class HomeFragment extends Fragment {

    private final String TAG = getClass().getName();
    private HomeViewModel homeViewModel;


//      以下
//    private boolean bt;
//    // 以上


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        final Drawable drawableYes = ContextCompat.getDrawable(getContext(), R.drawable.ic_right);
        final Drawable drawableNo = ContextCompat.getDrawable(getContext(), R.drawable.ic_wrong);


        //        // 以下
        final Switch aSwitch = (Switch) root.findViewById(R.id.s_v);
//        aSwitch.setChecked(true);
//        aSwitch.setText(R.string.serviceOpen);
//        aSwitch.setSwitchTextAppearance(MainActivity.this,R.style.s_false);
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                Intent intent = new Intent(MainActivity.this, ADKillerService.class);
                if (b) {
                    aSwitch.setText(R.string.serviceOpen);
//                    bt = (ADKillerService.serviceImpl != null);
//                    aSwitch.setSwitchTextAppearance(MainActivity.this,R.style.s_true);
//                    startService(intent);
                }else {
                    aSwitch.setText(R.string.serviceClose);
//                    bt = false;
//                    aSwitch.setSwitchTextAppearance(MainActivity.this,R.style.s_false);
//                    intent.putExtra("tryDisable",true);
//                    startService(intent);
//                    stopService(intent);
                }
            }
        });
//        checkServiceStatus();
//        // 以上



        // set observers for widget 设置小部件的观察者
        final ImageView imageAccessibilityPermission = root.findViewById(R.id.image_accessibility_permission);
        homeViewModel.getAccessibilityPermission().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean) {
                    imageAccessibilityPermission.setImageDrawable(drawableYes);
                } else {
                    imageAccessibilityPermission.setImageDrawable(drawableNo);
                }
            }
        });

        final ImageView imagePowerPermission = root.findViewById(R.id.image_power_permission);
        homeViewModel.getPowerOptimization().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean) {
                    imagePowerPermission.setImageDrawable(drawableYes);
                } else {
                    imagePowerPermission.setImageDrawable(drawableNo);
                }
            }
        });


        // set listener for buttons
        final ImageButton btAccessibilityPermission = root.findViewById(R.id.button_accessibility_permission);
        btAccessibilityPermission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_abs = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                intent_abs.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent_abs);
            }
        });

        final ImageButton btPowerPermission = root.findViewById(R.id.button_power_permission);
        btPowerPermission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  打开电池优化的界面，让用户设置
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Intent intent = new Intent();
                    String packageName = getActivity().getPackageName();
                    PowerManager pm = (PowerManager) getActivity().getSystemService(Context.POWER_SERVICE);
                    // open battery optimization setting page
                    intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                    startActivity(intent);
                }
            }
        });

        // get the service status
        checkServiceStatus();

//        // 以下
//        final Switch aSwitch = (Switch) root.findViewById(R.id.s_v);
////        aSwitch.setChecked(true);
////        aSwitch.setSwitchTextAppearance(MainActivity.this,R.style.s_false);
//        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
////                Intent intent = new Intent(MainActivity.this, ADKillerService.class);
//                if (b) {
//                    bt = (ADKillerService.serviceImpl != null);
////                    aSwitch.setSwitchTextAppearance(MainActivity.this,R.style.s_true);
////                    startService(intent);
//                }else {
//                    bt = false;
////                    aSwitch.setSwitchTextAppearance(MainActivity.this,R.style.s_false);
////                    intent.putExtra("tryDisable",true);
////                    startService(intent);
////                    stopService(intent);
//                }
//            }
//        });
//        checkServiceStatus();
//        // 以上


        return root;
    }

    @Override
    public void onResume() {
        checkServiceStatus();

        super.onResume();
    }

    public void checkServiceStatus(){

        // detect the app storage permission
        boolean bAppPermission =
                ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        MutableLiveData<Boolean> liveData = homeViewModel.getAppPermission();
        liveData.setValue(bAppPermission);

        // detect the accessibility permission
        MutableLiveData<Boolean> accessibility = homeViewModel.getAccessibilityPermission();
        accessibility.setValue(ADKillerService.serviceImpl != null);

//        // 以下
//        accessibility.setValue(bt);
//        // 以上

//        final Switch aSwitch = (Switch) root.findViewById(R.id.s_v);
////        aSwitch.setChecked(false);
////        aSwitch.setSwitchTextAppearance(MainActivity.this,R.style.s_false);
//        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
////                Intent intent = new Intent(MainActivity.this, ADKillerService.class);
//                if (b) {
//                    ;
////                    aSwitch.setSwitchTextAppearance(MainActivity.this,R.style.s_true);
////                    startService(intent);
//                }else {
//                    ;
////                    aSwitch.setSwitchTextAppearance(MainActivity.this,R.style.s_false);
////                    intent.putExtra("tryDisable",true);
////                    startService(intent);
////                    stopService(intent);
//                }
//            }
//        });



        // detect power optimization
        PowerManager pm = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
        boolean hasIgnored = pm.isIgnoringBatteryOptimizations(getContext().getPackageName());
        MutableLiveData<Boolean> power = homeViewModel.getPowerOptimization();
        power.setValue(hasIgnored);
    }
}