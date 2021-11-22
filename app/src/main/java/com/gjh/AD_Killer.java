package com.gjh;

import android.app.Application;
import android.content.Context;

public class AD_Killer extends Application {

    private static Context context;

    public AD_Killer() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        AD_Killer.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return context;
    }
}
