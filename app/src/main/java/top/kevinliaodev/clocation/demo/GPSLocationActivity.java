package top.kevinliaodev.clocation.demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import top.kevinliaodev.clocation.R;
import top.madev.clocationlib.GetLocation;
import top.madev.clocationlib.bean.MyLocation;
import top.madev.clocationlib.bean.Point;
import top.madev.clocationlib.utils.BoundaryCheck;
import top.madev.clocationlib.utils.CoordinateConversion;

public class GPSLocationActivity extends AppCompatActivity {

    private TextView tv_location_info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpslocation);
        setTitle("GPS定位");
        findViewById(R.id.btn_locate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                location();
            }
        });

        tv_location_info = findViewById(R.id.tv_location_info);
    }

    private void location(){
        GetLocation.getInstance().setGPSLocationListener(new GetLocation.OnGPSLocationListener() {
            @Override
            public void onGPSLocation(double latitude, double longitude, long updateTime, float accuracy, float bearing) {
                Double lat, lon;
                MyLocation myLocation = new MyLocation(latitude, longitude);
                //由于坐标系差异，中国大陆坐标需要转换
                if(BoundaryCheck.getInstance().IsInsideChina(myLocation)) {
                    Point point = CoordinateConversion.wgs_gcj_encrypts(latitude, longitude);
                    lat = point.getLat();
                    lon = point.getLng();
                }else {
                    lat = latitude;
                    lon = longitude;
                }
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date(updateTime);
                tv_location_info.setText("经度:" + lon
                        + "\n纬度:" + lat
                        + "\n精度:" + accuracy
                        + "\n方位:" + bearing
                        + "\n获取定位时间:" + simpleDateFormat.format(date));
            }

            @Override
            public void onFailed(int errorCode, String msg) {
                tv_location_info.setText("error!\nerrorCode:" + errorCode + "\nmsg:" + msg);
            }
        });

        GetLocation.getInstance().startGPSLocation(getApplication(),this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        GetLocation.getInstance().onStop();
    }

}
