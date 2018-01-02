package top.kevinliaodev.clocation.bean;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by lk on 2017/2/27.
 * wifi信息
 */

public class GoogleWifiInfo {
    /**
     * macAddress : 01:23:45:67:89:AB
     * signalStrength : -65
     * age : 0
     * channel : 11
     * signalToNoiseRatio : 40
     */

    public String macAddress;//（必填）Wi-Fi 节点的 MAC 地址。分隔符必须是 :（冒号），并且十六进制数字必须使用大写字母。
    public int signalStrength;//测量到的当前信号强度（以 dBm 为单位）。
    public int age;//自从检测到此接入点后经过的毫秒数。
    public short channel;//客户端与接入点进行通信的信道
    public int signalToNoiseRatio;//测量到的当前信噪比（以 dB 为单位）。

    public JSONObject toJson(){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("signalStrength",signalStrength);
            jsonObject.put("age",age);
            jsonObject.put("macAddress",macAddress);
            jsonObject.put("channel",channel);
            jsonObject.put("signalToNoiseRatio",signalToNoiseRatio);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return  jsonObject;
    }
}
