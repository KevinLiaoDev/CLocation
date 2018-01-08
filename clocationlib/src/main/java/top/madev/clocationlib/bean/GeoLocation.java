package top.madev.clocationlib.bean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import top.madev.clocationlib.bean.GoogleCellTower;
import top.madev.clocationlib.bean.GoogleWifiInfo;

/**
 * Created by lk on 2017/2/27.
 * 用于向谷歌根据基站请求经纬度的封装基站信息的类
 */

public class GeoLocation {
    /**
     * homeMobileCountryCode : 310 移动国家代码（中国的为460）；
     * homeMobileNetworkCode : 410 和基站有关
     * radioType : gsm
     * carrier : Vodafone 运营商名称
     * considerIp : true
     * cellTowers : []
     * wifiAccessPoints : []
     */

    public int homeMobileCountryCode;//设备的家庭网络的移动国家代码 (MCC)
    public int homeMobileNetworkCode;//设备的家庭网络的移动网络代码 (MNC)。
    public String radioType;//radioType：移动无线网络类型。支持的值有 lte、gsm、cdma 和 wcdma。虽然此字段是可选的，但如果提供了相应的值，就应该将此字段包括在内，以获得更精确的结果。
    public String carrier;//运营商名称。
    public boolean considerIp;//指定当 Wi-Fi 和移动电话基站的信号不可用时，是否回退到 IP 地理位置。请注意，请求头中的 IP 地址不能是设备的 IP 地址。默认为 true。将 considerIp 设置为 false 以禁用回退。
    public List<GoogleCellTower> cellTowers;
    public List<GoogleWifiInfo> wifiAccessPoints;

    public String toJson(){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("homeMobileCountryCode",homeMobileCountryCode);
            jsonObject.put("homeMobileNetworkCode",homeMobileNetworkCode);
            jsonObject.put("radioType",radioType);
            jsonObject.put("carrier",carrier);
            jsonObject.put("considerIp",considerIp);
            if(cellTowers!=null){
                JSONArray jsonArray = new JSONArray();
                for (GoogleCellTower t:cellTowers) jsonArray.put(t.toJson());
                jsonObject.put("cellTowers",jsonArray);
            }
            if(wifiAccessPoints!=null){
                JSONArray jsonArray = new JSONArray();
                for (GoogleWifiInfo w:wifiAccessPoints) jsonArray.put(w.toJson());
                jsonObject.put("wifiAccessPoints",jsonArray);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

}
