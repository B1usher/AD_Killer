package com.gjh.adkiller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ADKillerServiceReceiver extends BroadcastReceiver {

    private final String TAG = getClass().getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        // an Intent broadcast, just dispatch message to AD_Killer

        String action = intent.getAction();
        Log.d(TAG, action);
        if(action.contains("PACKAGE_ADDED") || action.contains("PACKAGE_REMOVED")) {
            if (ADKillerService.serviceImpl != null) {
                ADKillerService.serviceImpl.receiverHandler.sendEmptyMessage(ADKillerService.ACTION_REFRESH_PACKAGE);
            }
        }
    }
}
