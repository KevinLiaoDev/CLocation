package top.kevinliaodev.clocation.bean;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by lk on 2017/2/27.
 * 基站信息
 */

public class GoogleCellTower {

    /*
GSM:
        {
  "cellTowers": [
    {
      "cellId": 42,
      "locationAreaCode": 415,
      "mobileCountryCode": 310,
      "mobileNetworkCode": 410,
      "age": 0,
      "signalStrength": -60,
      "timingAdvance": 15
    }
  ]
}
WCDMA
{
  "cellTowers": [
    {
      "cellId": 21532831,
      "locationAreaCode": 2862,
      "mobileCountryCode": 214,
      "mobileNetworkCode": 7
    }
  ]
}
    */

    //下面的是必填
    public int cellId;//（必填）：小区的唯一标识符。在 GSM 上，这就是小区 ID (CID)；CDMA 网络使用的是基站 ID (BID)。WCDMA 网络使用 UTRAN/GERAN 小区标识 (UC-Id)，这是一个 32 位的值，由无线网络控制器 (RNC) 和小区 ID 连接而成。在 WCDMA 网络中，如果只指定 16 位的小区 ID 值，返回的结果可能会不准确。
    public int locationAreaCode;//（必填）：GSM 和 WCDMA 网络的位置区域代码 (LAC)。CDMA 网络的网络 ID (NID)。
    public int mobileCountryCode;//（必填）：移动电话基站的移动国家代码 (MCC)。
    public int mobileNetworkCode;//（必填）：移动电话基站的移动网络代码。对于 GSM 和 WCDMA，这就是 MNC；CDMA 使用的是系统 ID (SID)。
    public int signalStrength;//测量到的无线信号强度（以 dBm 为单位）。
    //下面的是选填
    public int age;//自从此小区成为主小区后经过的毫秒数。如果 age 为 0，cellId 就表示当前的测量值。
    public int timingAdvance;//时间提前值。

    public JSONObject toJson(){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("cellId",cellId);
            jsonObject.put("locationAreaCode",locationAreaCode);
            jsonObject.put("mobileCountryCode",mobileCountryCode);
            jsonObject.put("mobileNetworkCode",mobileNetworkCode);
            jsonObject.put("signalStrength",signalStrength);
            jsonObject.put("age",age);
            jsonObject.put("timingAdvance",timingAdvance);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
}
