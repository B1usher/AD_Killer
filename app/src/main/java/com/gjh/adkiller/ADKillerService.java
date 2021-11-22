package com.gjh.adkiller;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;

public class ADKillerService extends AccessibilityService {

    public final static int ACTION_REFRESH_KEYWORDS = 1;
    public final static int ACTION_REFRESH_PACKAGE = 2;
    public final static int ACTION_REFRESH_CUSTOMIZED_ACTIVITY = 3;
    public final static int ACTION_ACTIVITY_CUSTOMIZATION = 4;
    public final static int ACTION_STOP_SERVICE = 5;

    public static ADKillerServiceImpl serviceImpl = null;

    final private String TAG = getClass().getName();




//    @Override
//    public void onCreate() {
//        super.onCreate();
////        if (serviceImpl == null) {
////            serviceImpl = new ADKillerServiceImpl(this);
////        }
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
////        serviceImpl = null;
//    }


//    // 以下
//    private SharedPreferences sharedPreferences;
//    private NotificationManager manager;
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//        Log.d(TAG, "onCreate: ");
//        Utils.setServiceRunning(getApplicationContext(),true);
//        sharedPreferences = getApplicationContext().getSharedPreferences("app_action",MODE_PRIVATE);
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        Log.d(TAG, "onDestroy: ");
//        manager.cancel(NOTIFICATION_ID);
//        Utils.setServiceRunning(getApplicationContext(),false);
//    }
//    // 以上

//        final Switch aSwitch = (Switch) findViewById(R.id.s_v);
//        aSwitch.setChecked(false);
//        aSwitch.setSwitchTextAppearance(MainActivity.this,R.style.s_false);
//        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                Intent intent = new Intent(MainActivity.this, ADKillerService.class);
//                if (b) {
////                    aSwitch.setSwitchTextAppearance(MainActivity.this,R.style.s_true);
//                    startService(intent);
//                }else {
////                    aSwitch.setSwitchTextAppearance(MainActivity.this,R.style.s_false);
//                    intent.putExtra("tryDisable",true);
//                    startService(intent);
//                    stopService(intent);
//                }
//            }
//        }
//        );




    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        if (serviceImpl == null) {
            serviceImpl = new ADKillerServiceImpl(this);
        }
        if (serviceImpl != null) {
            serviceImpl.onServiceConnected();
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (serviceImpl != null) {
            serviceImpl.onAccessibilityEvent(event);
        }
    }

    @Override
    public void onInterrupt() {
        if (serviceImpl != null) {
            serviceImpl.onInterrupt();
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (serviceImpl != null) {
            serviceImpl.onUnbind(intent);
            serviceImpl = null;
        }
        return super.onUnbind(intent);
    }
}
