package top.madev.clocationlib;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.Iterator;

/**
 * Created by Lk on 2016/12/21.
 * 获取定位
 */
public class GetLocation {

    private OnGPSLocationListener onGPSLocationListener;
    private OnMultiLocationListener onMultiLocationListener;
    private OnNetworkLocationListener onNetworkLocationListener;

    public interface OnGPSLocationListener {
        void onGPSLocation(double latitude, double longitude, long updateTime, float accuracy, float bearing);
        void onFailed(int errorCode, String msg);
    }

    public interface OnMultiLocationListener {
        void onMultiLocation(double latitude, double longitude, long updateTime, float accuracy, float bearing);
        void onFailed(int errorCode, String msg);
    }

    public interface OnNetworkLocationListener {
        void onNetworkLocation(double latitude, double longitude, long updateTime, float accuracy);
        void onFailed(int errorCode, String msg);
    }

    public void setGPSLocationListener(OnGPSLocationListener onGPSLocationListener) {
        this.onGPSLocationListener = onGPSLocationListener;
    }

    public void setMultiLocationListener(OnMultiLocationListener onMultiLocationListener) {
        this.onMultiLocationListener = onMultiLocationListener;
    }

    public void setNetworkLocationListener(OnNetworkLocationListener onNetworkLocationListener) {
        this.onNetworkLocationListener = onNetworkLocationListener;
    }

    public double latitude;
    public double longitude;
    public long updateTime;//最后更新时间，用于做精确度择优
    public float accuracy;
    public float bearing;
    private static GetLocation getLocation;
    private static LocationManager lm;
    private static final String TAG = "GetLocation";
    private boolean follow = false;//是否跟随定位
    private boolean firstGPS = true;//第一次GPS使用跟随定位第一次返回数据

    public GetLocation() {
    }

    public static GetLocation getInstance() {
        if (getLocation == null)
            getLocation = new GetLocation();
        return getLocation;
    }

    public static void onDestroy() {
        if (getLocation != null) getLocation = null;
    }

    public void onStop() {
        if (lm != null) {
            lm.removeUpdates(locationListener);
            lm = null;
        }
        NetworkLocation.getInstance().stopNetworkLocation();
        onDestroy();
    }

    //状态监听
    GpsStatus.Listener listener = new GpsStatus.Listener() {
        public void onGpsStatusChanged(int event) {
            switch (event) {
                //第一次定位
                case GpsStatus.GPS_EVENT_FIRST_FIX:
                    Log.i(TAG, "第一次定位");
                    break;
                //卫星状态改变
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                    //获取当前状态
                    if(lm!=null) {
                        try {
                            GpsStatus gpsStatus = lm.getGpsStatus(null);
                            //获取卫星颗数的默认最大值
                            int maxSatellites = gpsStatus.getMaxSatellites();
                            //创建一个迭代器保存所有卫星
                            Iterator<GpsSatellite> iters = gpsStatus.getSatellites().iterator();
                            int count = 0;
                            while (iters.hasNext() && count <= maxSatellites) {
                                GpsSatellite s = iters.next();
                                count++;
                            }
                        }catch (Exception ex){
                            ex.printStackTrace();
                        }
                    }
                    break;
                //定位启动
                case GpsStatus.GPS_EVENT_STARTED:
                    Log.i(TAG, "定位启动");
                    break;
                //定位结束
                case GpsStatus.GPS_EVENT_STOPPED:
                    if (lm != null) {
                        lm.removeUpdates(locationListener);
                        lm = null;
                    }
                    Log.i(TAG, "定位结束");
                    break;
            }
        }

        ;
    };

    //位置监听
    private LocationListener locationListener = new LocationListener() {

        /**
         * 位置信息变化时触发
         */
        public void onLocationChanged(Location location) {
            if (location != null) {
                if (firstGPS) {
                    if (onGPSLocationListener != null) {
                        onGPSLocationListener.onGPSLocation(
                                location.getLatitude(),
                                location.getLongitude(),
                                location.getTime(),
                                location.getAccuracy(),
                                location.getBearing());
                        firstGPS = false;
                    }
                } else {
                    if (onGPSLocationListener != null && follow) {
                        onGPSLocationListener.onGPSLocation(
                                location.getLatitude(),
                                location.getLongitude(),
                                location.getTime(),
                                location.getAccuracy(),
                                location.getBearing());
                    }
                }

            }
        }

        /**
         * GPS状态变化时触发
         */
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                //GPS状态为可见时
                case LocationProvider.AVAILABLE:
                    Log.i(TAG, "当前GPS状态为可见状态");
                    break;
                //GPS状态为服务区外时
                case LocationProvider.OUT_OF_SERVICE:
                    Log.i(TAG, "当前GPS状态为服务区外状态");
                    break;
                //GPS状态为暂停服务时
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.i(TAG, "当前GPS状态为暂停服务状态");
                    break;
            }
        }

        /**
         * GPS开启时触发
         */
        public void onProviderEnabled(String provider) {
            Log.i(TAG, "GPS开启时触发");
        }

        /**
         * GPS禁用时触发
         */
        public void onProviderDisabled(String provider) {
            Log.i(TAG, "GPS禁用时触发");
        }
    };

    /**
     *
     * 多重定位结果
     * 策略：同时使用GPS和网络定位，GPS先获得数据时停止网络定位（返回GPS数据）；
     * 网络定位先获得数据时（返回网络定位数据），GPS继续请求30s，GPS获取到数据则更新返回数据，未获得则关闭GPS定位。
     * @param context
     */
    public void startMultiLocation(final Application context, final Activity activity, final boolean follow) {
        this.follow = follow;
        setGPSLocationListener(new OnGPSLocationListener() {
            @Override
            public void onGPSLocation(double latitude, double longitude, long updateTime, float accuracy, float bearing) {
                if (onMultiLocationListener != null) {
                    if (follow) {
                        onMultiLocationListener.onMultiLocation(latitude, longitude, updateTime, accuracy, bearing);
                    } else {
                        onMultiLocationListener.onMultiLocation(latitude, longitude, updateTime, accuracy, -1.0f);
                    }
                    //立刻停止网络请求
                    NetworkLocation.getInstance().stopNetworkLocation();
                }
            }

            @Override
            public void onFailed(int errorCode, String msg) {
                onMultiLocationListener.onFailed(errorCode, msg);
            }
        });
        setNetworkLocationListener(new OnNetworkLocationListener() {
            @Override
            public void onNetworkLocation(double latitude, double longitude, long updateTime, float accuracy) {
                if (onMultiLocationListener != null) {
                    onMultiLocationListener.onMultiLocation(latitude, longitude, updateTime, accuracy, -1.0f);
                    //如果不需要跟踪，继续请求30sGPS，没有结果则关闭。如果需要跟踪，就不能关闭GPS定位
                    if (!follow) {
                        new Handler().postDelayed(new Runnable() {
                            public void run() {
                                Log.i(TAG, "停止GPS请求");
                                if (lm != null) {
                                    lm.removeUpdates(locationListener);
                                }
                            }

                        }, 30 * 1000);
                    }
                }
            }

            @Override
            public void onFailed(int errorCode, String msg) {
                onMultiLocationListener.onFailed(errorCode, msg);
            }
        });
        startGPSLocation(context, activity);
        startNetworkLocation(context);
    }

    /**
     * GPS定位结果
     */
    public void startGPSLocation(Application context, Activity activity) {
        firstGPS = true;
        if (lm == null) {
            lm = (LocationManager) (context.getSystemService(Context.LOCATION_SERVICE));
            //判断GPS是否正常启动
            if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Toast.makeText(context, "请开启GPS导航", Toast.LENGTH_SHORT).show();
                //返回开启GPS导航设置界面
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                activity.startActivityForResult(intent, 0);
                return;
            }
        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.i(TAG, "权限问题");
            return;
        }

        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        lm.addGpsStatusListener(listener);
        //获取位置信息
        //如果不设置查询要求，getLastKnownLocation方法传人的参数为LocationManager.GPS_PROVIDER
//        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//        if(location!=null) {
//            if (onGPSLocationListener != null)
//                onGPSLocationListener.onGPSLocation(
//                        location.getLatitude(),
//                        location.getLongitude(),
//                        location.getTime(),
//                        location.getAccuracy(),
//                        location.getBearing());
//        }

        //绑定监听，有4个参数
        //参数1，设备：有GPS_PROVIDER和NETWORK_PROVIDER两种
        //参数2，位置信息更新周期，单位毫秒
        //参数3，位置变化最小距离：当位置距离变化超过此值时，将更新位置信息
        //参数4，监听
        //备注：参数2和3，如果参数3不为0，则以参数3为准；参数3为0，则通过时间来定时更新；两者为0，则随时刷新

        // 1秒更新一次，或最小位移变化超过1米更新一次；
        //注意：此处更新准确度非常低，推荐在service里面启动一个Thread，在run中sleep(10000);然后执行handler.sendMessage(),更新位置
//        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
//        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, locationListener);
    }

    /**
     * 网络定位结果
     */
    public void startNetworkLocation(final Application application) {

        NetworkLocation.getInstance().setRequestListener(new NetworkLocation.OnNetworkLocationListener() {
            @Override
            public void onRequest(Double latitude, Double longitute, float accuracy, Long updateTime) {
                if (onNetworkLocationListener != null) onNetworkLocationListener.onNetworkLocation(latitude, longitute,updateTime, accuracy);
            }
        });
        NetworkLocation.getInstance().setRequestErrorListener(new NetworkLocation.OnRequestErrorListener() {
            @Override
            public void onRequestError(int errorCode, String msg) {
                if(getLocation!=null) onNetworkLocationListener.onFailed(errorCode, msg);
            }
        });
        NetworkLocation.getInstance().getLocationInfo(application);
    }

    /**
     * 设置请求接口（主要用于设置反向代理被墙域名）
     * @param url
     */
    public void setNetworkLocationUrl(String url){
        NetworkLocation.getInstance().setLocationUrl(url);
    }



}
