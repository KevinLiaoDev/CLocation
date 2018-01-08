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

public class MultiLocationActivity extends AppCompatActivity {

    private TextView tv_location_info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_location);
        setTitle("多重定位");
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
        GetLocation.getInstance().setMultiLocationListener(new GetLocation.OnMultiLocationListener() {
            @Override
            public void onMultiLocation(double multilatitude, double multilongitude, long multiupdateTime, float multiaccuracy, float multibearing) {
                Double latitude, longitude;
                MyLocation myLocation = new MyLocation(multilatitude, multilongitude);
                if(BoundaryCheck.getInstance().IsInsideChina(myLocation)) {
                    Point point = CoordinateConversion.wgs_gcj_encrypts(multilatitude, multilongitude);
                    latitude = point.getLat();
                    longitude = point.getLng();
                }else {
                    latitude = multilatitude;
                    longitude = multilongitude;
                }
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date(multiupdateTime);
                tv_location_info.setText("经度:" + longitude
                        + "\n纬度:" + latitude
                        + "\n精度:" + multiaccuracy
                        + "\n获取定位时间:" + simpleDateFormat.format(date));
            }

            @Override
            public void onFailed(int errorCode, String msg) {
                tv_location_info.setText("error!\nerrorCode:" + errorCode + "\nmsg:" + msg);
            }
        });
        GetLocation.getInstance().startMultiLocation(getApplication(), this, false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        GetLocation.getInstance().onStop();
    }

}
