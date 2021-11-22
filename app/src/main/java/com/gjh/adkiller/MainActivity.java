package com.gjh.adkiller;

import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        final Switch aSwitch = (Switch) findViewById(R.id.s_v);
//        aSwitch.setChecked(true);


//        Switch aSwitch = (Switch) findViewById(R.id.s_v);
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




        //去除默认标题栏
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.hide();
        }

        BottomNavigationView navView = findViewById(R.id.nav_view);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_settings, R.id.navigation_about)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);





    }

}