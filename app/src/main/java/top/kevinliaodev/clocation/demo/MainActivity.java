package top.kevinliaodev.clocation.demo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import top.kevinliaodev.clocation.R;
import top.madev.clocationlib.NetworkLocation;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        permissionCheck(this);

        findViewById(R.id.btn_multilocation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, MultiLocationActivity.class));
            }
        });
        findViewById(R.id.btn_networklocation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, NetworkLocationActivity.class));
            }
        });
        findViewById(R.id.btn_gpslocation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, GPSLocationActivity.class));
            }
        });
        TextView tv_warning = findViewById(R.id.tv_warning);

        if (NetworkLocation.getMainKey(this).equals("YOUR_API_KEY")){
            tv_warning.setVisibility(View.VISIBLE);
            tv_warning.setText("请填写GOOGLE_LOCATION_API_KEY!");
        }

    }

    private void permissionCheck(Context context)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE ) != PackageManager.PERMISSION_GRANTED
                    || context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE ) != PackageManager.PERMISSION_GRANTED
                    || context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE ) != PackageManager.PERMISSION_GRANTED
                    || context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED
                    || context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED)
            {
                // 申请一个（或多个）权限，并提供用于回调返回的获取码（用户定义)
                requestPermissions(new String[]{
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                }, 10);
            }
        }
    }

}
