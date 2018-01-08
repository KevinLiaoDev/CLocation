package top.madev.clocationlib;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;

import org.json.JSONException;
import org.json.JSONObject;

import cn.finalteam.okhttpfinal.BaseHttpRequestCallback;
import cn.finalteam.okhttpfinal.HttpRequest;
import cn.finalteam.okhttpfinal.OkHttpFinal;
import cn.finalteam.okhttpfinal.OkHttpFinalConfiguration;
import cn.finalteam.okhttpfinal.RequestParams;
import top.madev.clocationlib.bean.GeoLocation;
import top.madev.clocationlib.bean.LocationInfo;

/**
 * Created by lk on 2017/2/27.
 * 网络定位
 */

public class NetworkLocation {

    public static final String TAG = "NetworkLocation";
    private static NetworkLocation networkLocation;

    private OnRequestErrorListener onRequestErrorListener;
    private OnNetworkLocationListener onNetworkLocationListener;

    private String cancleURL;//取消网络请求url
    private boolean sendRequest = true;
    private long timeout = 10 * 1000;
    private String location_url = "https://www.googleapis.com/";

    public static NetworkLocation getInstance(){
        if(networkLocation == null)
            networkLocation = new NetworkLocation();
        return networkLocation;
    }

    /**
     * 获取网络定位所需基站信息和wifi信息
     */
    public void getLocationInfo(final Application application){
        sendRequest = true;

        OkHttpFinalConfiguration.Builder builder = new OkHttpFinalConfiguration.Builder();
        builder.setTimeout(timeout);
        OkHttpFinal.getInstance().init(builder.build());

        new MyAsynTask<Void, Void, String>() {//在子线程中获取附近的基站和wifi信息

            @Override
            protected String doInBackground(Void... params) {
                GeoLocation geoLocation = null;
                try {
                    geoLocation = LocationInfo.getCellInfo(application);//得到基站信息,通过基站进行定位
                } catch (Exception e) {
                    Log.i(TAG, "获取附近基站信息出现异常", e);
                }
                if (geoLocation == null) {
                    Log.i(TAG, "获取基站信息失败");
                    return "{}";
                }
                LocationInfo.getWifiInfo(application, geoLocation);
                String json = geoLocation.toJson();//这里使用gson.toJson()会被混淆，推荐使用手动拼json
                Log.i(TAG, "准备发给google的json是" + json);
                return json;
            }

            @Override
            protected void onPostExecute(String json) {
                super.onPostExecute(json);
                //开启子线程请求网络
                if (application != null) {
                    String GOOGLE_API_KEY = getMainKey(application);
                    if(TextUtils.isEmpty(GOOGLE_API_KEY)){
                        Log.i(TAG, "获取GOOGLE_API_KEY失败");
                        return;
                    }
                    if(sendRequest) sendJsonByPost(json, location_url + "geolocation/v1/geolocate?key=" + GOOGLE_API_KEY);
                } else {
                    return;
                }
            }
        }.executeDependSDK();

    }

    /**
     * 停止网络定位
     */
    public void stopNetworkLocation(){
        if(sendRequest) {
            sendRequest = false;
            HttpRequest.cancel(cancleURL);
        }
    }

    /**
     * 使用httpclient发送一个post的json请求
     * @param url
     * @return
     */
    public void sendJsonByPost(String json, String url){
        Log.i(TAG,"开始请求位置");
        cancleURL = url;
        RequestParams params = new RequestParams();
        params.applicationJson(JSON.parseObject(json));
        HttpRequest.post(url, params, timeout, new BaseHttpRequestCallback<String>(){
            @Override
            protected void onSuccess(String s) {
                super.onSuccess(s);
                 /*
                谷歌返回的json如下
                {
                   "location": {
                    "lat": 1.3553794,
                    "lng": 103.86774439999999
                   },
                   "accuracy": 16432.0
                  }
                 */
                if(s == null)
                    return;
                String result = s;
                Log.i(TAG,"成功" + s);
                if(result==null || result.length()<10 || !result.startsWith("{")){
                    Log.i(TAG,"返回格式不对"+result);
                }
                JSONObject returnJson = null;
                try {
                    returnJson = new JSONObject(result);
                    JSONObject location = returnJson.getJSONObject("location");
                    if(location == null){
                        Log.i(TAG,"条件不足，无法确定位置");
                        return;
                    }
                    double latitude = location.getDouble("lat");
                    double longitute = location.getDouble("lng");
                    double google_accuracy = returnJson.getDouble("accuracy");
                    Log.i(TAG,"谷歌返回的经纬度是:"+latitude + "," + longitute + ".精度是" + google_accuracy);
                    onNetworkLocationListener.onRequest(latitude, longitute, (float) google_accuracy, System.currentTimeMillis());
                } catch (JSONException e) {
                    Log.i(TAG,"条件不足，无法确定位置2",e);
                }

            }

            @Override
            public void onFailure(int errorCode, String msg) {
                super.onFailure(errorCode, msg);
                if(TextUtils.isEmpty(msg))
                    return;
                //这里如果报404异常的话，一般是根据当前的基站cid无法查到相关信息
                //如果返回值是 400  Bad Request，说明有的必填项没有填
                if(onRequestErrorListener!=null)
                    onRequestErrorListener.onRequestError(errorCode, msg);
                Log.i(TAG,"失败了"+msg+"   "+errorCode);
                if(errorCode==0)
                    Log.i(TAG,"谷歌没有根据现有条件查询到经纬度");
            }
        });
    }

    /**
     *获取AndroidManifest中的API_KEY
     * @param ctx
     * @return
     */
    public static String getMainKey(Context ctx){
        String packageName = ctx.getPackageName();
        PackageManager packageManager = ctx.getPackageManager();
        Bundle bd = null;
        String key = "";
        try {
            ApplicationInfo info = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            bd = info.metaData;//获取metaData标签内容
            if (bd != null) {// 获取AndroidManifest.xml文件中API_KEY
                Object keyO = bd.get("GOOGLE_LOCATION_API_KEY");
                key = keyO.toString();//这里获取的就是value值
            }
        } catch (PackageManager.NameNotFoundException localNameNotFoundException1) {

        }
        return key;
    }

    /**
     * 网络定位出现错误
     */
    public interface OnRequestErrorListener{
        void onRequestError(int errorCode, String msg);
    }
    public void setRequestErrorListener(OnRequestErrorListener onRequestErrorListener) {
        this.onRequestErrorListener = onRequestErrorListener;
    }

    /**
     * 网络定位结果
     */
    public interface OnNetworkLocationListener{
        void onRequest(Double latitude, Double longitute, float accuracy, Long updateTime);
    }
    public void setRequestListener(OnNetworkLocationListener onNetworkLocationListener) {
        this.onNetworkLocationListener = onNetworkLocationListener;
    }


    public void setLocationUrl(String url){
        location_url = url;
    }
}
