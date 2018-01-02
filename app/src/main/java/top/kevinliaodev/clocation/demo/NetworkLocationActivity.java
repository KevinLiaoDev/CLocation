package top.kevinliaodev.clocation.demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import top.kevinliaodev.clocation.GetLocation;
import top.kevinliaodev.clocation.R;
import top.kevinliaodev.clocation.bean.MyLocation;
import top.kevinliaodev.clocation.bean.Point;
import top.kevinliaodev.clocation.utils.BoundaryCheck;
import top.kevinliaodev.clocation.utils.CoordinateConversion;

public class NetworkLocationActivity extends AppCompatActivity {

    private TextView tv_location_info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_location);
        setTitle("网络定位");
        findViewById(R.id.btn_locate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /**
                 * 如果你想在中国大陆内使用谷歌网络定位服务，你可以选择翻墙或者反向代理以下接口
                 * https://www.googleapis.com/geolocation/v1/geolocate?key=YOUR_API_KEY
                 * 并通过以下方法设置你的接口域名
                 */
//                GetLocation.getInstance().setNetworkLocationUrl("https://googleapis.xxxxxx.com/");
                location();
            }
        });
        tv_location_info = findViewById(R.id.tv_location_info);
    }

    private void location(){

        GetLocation.getInstance().setNetworkLocationListener(new GetLocation.OnNetworkLocationListener() {
            @Override
            public void onNetworkLocation(double latitude, double longitude, long updateTime, float accuracy) {
                Double lat, lon;
                MyLocation myLocation = new MyLocation(latitude, longitude);
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
                        + "\n获取定位时间:" + simpleDateFormat.format(date));
            }

            @Override
            public void onFailed(int errorCode, String msg) {
                tv_location_info.setText("error!\nerrorCode:" + errorCode + "\nmsg:" + msg);
            }
        });
        GetLocation.getInstance().startNetworkLocation(getApplication());
    }

    @Override
    protected void onStop() {
        super.onStop();
        GetLocation.getInstance().onStop();
    }

}
