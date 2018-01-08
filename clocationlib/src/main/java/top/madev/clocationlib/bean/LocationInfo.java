package top.madev.clocationlib.bean;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import top.madev.clocationlib.utils.ScanWifi;

/**
 * Created by lk on 2017/2/27.
 * 获取网络定位所需数据
 */

public class LocationInfo {

    public static final String TAG = "LocationInfo";

    /**
     * 得到附近的基站信息，准备传给谷歌
     */
    public static GeoLocation getCellInfo(Context context) {
        //通过TelephonyManager 获取lac:mcc:mnc:cell-id
        GeoLocation cellInfo = new GeoLocation();
        TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (mTelephonyManager == null)
            return cellInfo;
        // 返回值MCC + MNC
        /*# MCC，Mobile Country Code，移动国家代码（中国的为460）；
        * # MNC，Mobile Network Code，移动网络号码（中国移动为0，中国联通为1，中国电信为2）；
        * # LAC，Location Area Code，位置区域码;
        * # CID，Cell Identity，基站编号；
        * # BSSS，Base station signal strength，基站信号强度。
        */
        String operator = mTelephonyManager.getNetworkOperator();
        Log.i(TAG, "获取的基站信息是" + operator);
        if (operator == null || operator.length() < 5) {
            Log.i(TAG, "获取基站信息有问题,可能是手机没插sim卡");
            return cellInfo;
        }
        int mcc = Integer.parseInt(operator.substring(0, 3));
        int mnc = Integer.parseInt(operator.substring(3));
        int lac;
        int cellId;
        // 中国移动和中国联通获取LAC、CID的方式
        CellLocation cellLocation = mTelephonyManager.getCellLocation();
        if(cellLocation==null){
            Log.i(TAG,"手机没插sim卡吧");
            return cellInfo;
        }
        if(mTelephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_GSM) {
            Log.i(TAG,"当前是gsm基站");
            GsmCellLocation location = (GsmCellLocation)cellLocation;
            lac = location.getLac();
            cellId = location.getCid();
            //这些东西非常重要，是根据基站获得定位的重要依据
            Log.i(TAG, " MCC移动国家代码 = " + mcc + "\t MNC移动网络号码 = " + mnc + "\t LAC位置区域码 = " + lac + "\t CID基站编号 = " + cellId);
        }else if(mTelephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA) {
            // 中国电信获取LAC、CID的方式
            Log.i(TAG,"现在是cdma基站");
            CdmaCellLocation location1 = (CdmaCellLocation) mTelephonyManager.getCellLocation();
            lac = location1.getNetworkId();
            cellId = location1.getBaseStationId();
            cellId /= 16;
        }else {
            Log.i(TAG,"现在不知道是什么基站");
            return cellInfo;
        }
        cellInfo.radioType = determine2g3g4g(context);//这里填要慎重，要是填的不对就会报404 notFound
        cellInfo.homeMobileCountryCode = mcc;
        cellInfo.homeMobileNetworkCode = mnc;
        cellInfo.carrier = getCarrier(operator);
        cellInfo.considerIp = considerIP(context);//这里要判断是否采用了vpn，在wifi的时候可以用ip辅助定位，但是如果是用的2g3g4g信号那就只能用基站，ip会不准
        ArrayList<GoogleCellTower> towers = new ArrayList<>(1);
        GoogleCellTower bigTower = new GoogleCellTower();//这个塔是当前连接的塔，只有一个，但是对定位有决定性的作用
        bigTower.cellId = cellId;
        bigTower.mobileCountryCode = mcc;
        bigTower.mobileNetworkCode = mnc;
        bigTower.locationAreaCode = lac;
        bigTower.signalStrength = 0;
        towers.add(bigTower);
        cellInfo.cellTowers = towers;
        // 获取邻区基站信息
        if(Build.VERSION.SDK_INT<17) {//低版的android系统使用getNeighboringCellInfo方法
            List<NeighboringCellInfo> infos = mTelephonyManager.getNeighboringCellInfo();
            if(infos==null){
                Log.i(TAG,"手机型号不支持基站定位1");
                return cellInfo;
            }
            if(infos.size()==0)return cellInfo;//附近没有基站
            towers = new ArrayList<>(infos.size());
            StringBuffer sb = new StringBuffer("附近基站总数 : " + infos.size() + "\n");
            for (NeighboringCellInfo info1 : infos) { // 根据邻区总数进行循环
                GoogleCellTower tower = new GoogleCellTower();
                sb.append(" LAC : " + info1.getLac()); // 取出当前邻区的LAC
                tower.locationAreaCode = info1.getLac();
                tower.mobileCountryCode = mcc;
                tower.mobileNetworkCode = mnc;
                tower.signalStrength = info1.getRssi();
                sb.append(" CID : " + info1.getCid()); // 取出当前邻区的CID
                tower.cellId = info1.getCid();
                sb.append(" BSSS : " + (-113 + 2 * info1.getRssi()) + "\n"); // 获取邻区基站信号强度
                towers.add(tower);
            }
            Log.i(TAG,"基站信息是"+sb);
        }else {//高版android系统使用getAllCellInfo方法，并且对基站的类型加以区分
            List<CellInfo> infos = mTelephonyManager.getAllCellInfo();
            if(infos!=null) {
                if(infos.size()==0)return cellInfo;
                towers = new ArrayList<>(infos.size());
                for (CellInfo i : infos) { // 根据邻区总数进行循环
                    Log.i(TAG, "附近基站信息是" + i.toString());//这里如果出现很多cid
                    GoogleCellTower tower = new GoogleCellTower();
                    if(i instanceof CellInfoGsm){//这里的塔分为好几种类型
                        Log.i(TAG,"现在是gsm基站");
                        CellIdentityGsm cellIdentityGsm = ((CellInfoGsm)i).getCellIdentity();//从这个类里面可以取出好多有用的东西
                        if(cellIdentityGsm==null)continue;
                        tower.locationAreaCode = cellIdentityGsm.getLac();
                        tower.mobileCountryCode = cellIdentityGsm.getMcc();
                        tower.mobileNetworkCode = cellIdentityGsm.getMnc();
                        tower.signalStrength = 0;
                        tower.cellId = cellIdentityGsm.getCid();
                    }else if(i instanceof CellInfoCdma){
                        Log.i(TAG,"现在是cdma基站");
                        CellIdentityCdma cellIdentityCdma = ((CellInfoCdma)i).getCellIdentity();
                        if(cellIdentityCdma==null)continue;
                        tower.locationAreaCode = lac;
                        tower.mobileCountryCode = mcc;
                        tower.mobileNetworkCode = cellIdentityCdma.getSystemId();//cdma用sid,是系统识别码，每个地级市只有一个sid，是唯一的。
                        tower.signalStrength = 0;
                        cellIdentityCdma.getNetworkId();//NID是网络识别码，由各本地网管理，也就是由地级分公司分配。每个地级市可能有1到3个nid。
                        tower.cellId = cellIdentityCdma.getBasestationId();//cdma用bid,表示的是网络中的某一个小区，可以理解为基站。
                    }else if(i instanceof CellInfoLte) {
                        Log.i(TAG, "现在是lte基站");
                        CellIdentityLte cellIdentityLte = ((CellInfoLte) i).getCellIdentity();
                        if(cellIdentityLte==null)continue;
                        tower.locationAreaCode = lac;
                        tower.mobileCountryCode = cellIdentityLte.getMcc();
                        tower.mobileNetworkCode = cellIdentityLte.getMnc();
                        tower.cellId = cellIdentityLte.getCi();
                        tower.signalStrength = 0;
                    }else if(i instanceof CellInfoWcdma && Build.VERSION.SDK_INT>=18){
                        Log.i(TAG,"现在是wcdma基站");
                        CellIdentityWcdma cellIdentityWcdma = ((CellInfoWcdma)i).getCellIdentity();
                        if(cellIdentityWcdma==null)continue;
                        tower.locationAreaCode = cellIdentityWcdma.getLac();
                        tower.mobileCountryCode = cellIdentityWcdma.getMcc();
                        tower.mobileNetworkCode = cellIdentityWcdma.getMnc();
                        tower.cellId = cellIdentityWcdma.getCid();
                        tower.signalStrength = 0;
                    }else {
                        Log.i(TAG,"不知道现在是啥基站");
                    }
                    towers.add(tower);
                }
            }else {//有些手机拿不到的话，就用废弃的方法，有时候即使手机支持，getNeighboringCellInfo的返回结果也常常是null
                Log.i(TAG,"通过高版本SDK无法拿到基站信息，准备用低版本的方法");
                List<NeighboringCellInfo> infos2 = mTelephonyManager.getNeighboringCellInfo();
                if(infos2==null || infos2.size()==0){
                    Log.i(TAG,"该手机确实不支持基站定位，已经无能为力了");
                    return cellInfo;
                }
                towers = new ArrayList<>(infos2.size());
                StringBuffer sb = new StringBuffer("附近基站总数 : " + infos2.size() + "\n");
                for (NeighboringCellInfo i : infos2) { // 根据邻区总数进行循环
                    GoogleCellTower tower = new GoogleCellTower();
                    sb.append(" LAC : " + i.getLac()); // 取出当前邻区的LAC
                    tower.age = 0;
                    tower.locationAreaCode = i.getLac();
                    tower.mobileCountryCode = mcc;
                    tower.mobileNetworkCode = mnc;
                    sb.append(" CID : " + i.getCid()); // 取出当前邻区的CID
                    tower.cellId = i.getCid();
                    sb.append(" BSSS : " + (-113 + 2 * i.getRssi()) + "\n"); // 获取邻区基站信号强度
                    towers.add(tower);
                }
                Log.i(TAG,"基站信息是"+sb);
            }
        }
        cellInfo.cellTowers = towers;
        return cellInfo;
    }

    /**
     * 判断当前手机在2g，3g，还是4g，用于发给谷歌
     */
    public static String determine2g3g4g(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager==null)
            return null;
        if(Build.VERSION.SDK_INT<21) {//旧版本安卓获取网络状态
            NetworkInfo[] networkInfos = connectivityManager.getAllNetworkInfo();
            if(networkInfos==null)
                return null;
            for(NetworkInfo i:networkInfos){
                if(i==null)
                    continue;
                Log.i(TAG,"正在查看当前网络的制式"+i.getTypeName()+i.getType()+"   "+i.getSubtypeName());//WIFI,VPN,MOBILE+LTE
                if(i.getType()!= ConnectivityManager.TYPE_MOBILE)
                    continue;//只看流量
                else
                    Log.i(TAG,"现在是移动网络");
                return determine2g3g4g(i);
            }
        }else {//新版
            Network[] networks = connectivityManager.getAllNetworks();
            if(networks==null)
                return null;
            for(Network n:networks){
                if(n==null)
                    continue;
                NetworkInfo networkInfo = connectivityManager.getNetworkInfo(n);
                if(networkInfo==null)
                    continue;
                Log.i(TAG,"正在查看当前网络的制式"+networkInfo.getTypeName()+networkInfo.getType()+"   "+networkInfo.getSubtypeName());//WIFI,VPN,MOBILE+LTE
                if(networkInfo.getType()!= ConnectivityManager.TYPE_MOBILE)
                    continue;//只看流量
                return determine2g3g4g(networkInfo);
            }
        }
        return null;
    }

    /**
     * 看看现在用的是几g，什么网络制式
     * @param info
     * @return
     */
    public static String determine2g3g4g(NetworkInfo info){
        if(info == null)
            return null;
        switch (info.getSubtype()){
            case TelephonyManager.NETWORK_TYPE_LTE:
                Log.i(TAG,"手机信号是lte");
                return "LTE";
            case TelephonyManager.NETWORK_TYPE_EDGE:
                Log.i(TAG,"手机信号是edge");
                return "EDGE";
            case TelephonyManager.NETWORK_TYPE_CDMA:
                return "CDMA";
            case TelephonyManager.NETWORK_TYPE_GPRS:
                return "GPRS";
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                return "HSDPA";
            case TelephonyManager.NETWORK_TYPE_HSPA:
                return "HSPA";
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "HSPAP";
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                return "HSUPA";
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                return "EVDO_0";
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                return "EVDO_A";
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                return "EVDO_B";
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "IDEN";
            case TelephonyManager.NETWORK_TYPE_UMTS:
                return "UMTS";
            case TelephonyManager.NETWORK_TYPE_EHRPD:
                return "EHRPD";
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                return "RTT";
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                return "UNKNOWN";
        }
        return null;
    }

    /**
     * 根据国家代码获取通信运营商名字
     * @param operatorString
     * @return
     */
    public static String getCarrier(String operatorString){
        if(operatorString == null)
        {
            return "0";
        }

        if(operatorString.equals("46000") || operatorString.equals("46002"))
        {
            //中国移动
            return "中国移动";
        }
        else if(operatorString.equals("46001"))
        {
            //中国联通
            return "中国联通";
        }
        else if(operatorString.equals("46003"))
        {
            //中国电信
            return "中国电信";
        }

        //error
        return "未知";
    }

    /**
     * 看看现在是wifi联网还是用的流量，如果是wifi返回true，因为wifi的时候可以用ip定位,但如果这时候是vpn，那就不能用ip定位
     * @param context
     */
    public static boolean considerIP(Context context){
        boolean considerIP = true;//默认是考虑
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager==null)
            return true;
        if(!isWifiEnvironment(context))
            return false;//如果现在不是wifi网络，就不能用ip定位
        if(Build.VERSION.SDK_INT<21) {//旧版本安卓获取网络状态
            NetworkInfo[] networkInfos = connectivityManager.getAllNetworkInfo();
            if(networkInfos==null)
                return true;
            for(NetworkInfo i:networkInfos){
                if(i==null)continue;
                Log.i(TAG,"现在的网络是"+i.getTypeName()+i.getType()+"   "+i.getSubtypeName());//WIFI,VPN,MOBILE+LTE
                if(i.getType()== ConnectivityManager.TYPE_VPN){
                    Log.i(TAG,"现在用的是VPN网络，不能用ip定位");
                    considerIP = false;
                    break;
                }
            }
        }else {//新版
            Network[] networks = connectivityManager.getAllNetworks();
            if(networks==null)
                return true;
            for(Network n:networks){
                if(n==null)
                    continue;
                NetworkInfo networkInfo = connectivityManager.getNetworkInfo(n);
                if(networkInfo==null)
                    continue;
                Log.i(TAG,"现在的网络是"+networkInfo.getTypeName()+networkInfo.getType()+"   "+networkInfo.getSubtypeName());//WIFI,VPN,MOBILE+LTE
                if(networkInfo.getType()== ConnectivityManager.TYPE_VPN){
                    Log.i(TAG,"现在用的是VPN网络，不能用ip定位");
                    considerIP = false;
                    break;
                }
            }
        }
        return considerIP;
    }

    /**
     * 看看现在用wifi流量还是手机流量，如果是wifi返回true
     * @param context
     * @return
     */
    public static boolean isWifiEnvironment(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo==null){
            Log.i(TAG,"现在没有联网");
            return false;
        }
        int netType = networkInfo.getType();
        switch (netType){
            case ConnectivityManager.TYPE_WIFI:
                Log.i(TAG,"现在是wifi网络,可以用ip定位");
                return true;
            case ConnectivityManager.TYPE_VPN://这个基本没用
                Log.i(TAG,"现在是VPN网络");
                break;
            case ConnectivityManager.TYPE_MOBILE:
                Log.i(TAG,"现在是移动网络,不能用ip定位");
                int subType = networkInfo.getSubtype();
                Log.i(TAG,"移动网络子类是"+subType+"  "+networkInfo.getSubtypeName());//能判断是2g/3g/4g网络
                break;
            default:
                Log.i(TAG,"不知道现在是什么网络");
                break;
        }
        return false;
    }

    /**
     * 得到附近的wifi信息，准备传给谷歌
     * @param context
     * @param geoLocationAPI
     * @return
     */
    public static GeoLocation getWifiInfo(Context context, GeoLocation geoLocationAPI){
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if(wifiManager == null)
            return geoLocationAPI;
        Log.i(TAG,"准备开始扫描附近wifi");
        wifiManager.startScan();
        //准备所有附近wifi放到wifi列表里，包括现在正连着的wifi
        ArrayList<ScanWifi> lsAllWIFI = new ArrayList<ScanWifi>();
        List<ScanResult> lsScanResult = wifiManager.getScanResults();//记录所有附近wifi的搜索结果
        if(lsScanResult == null){
            Log.i(TAG,"搜索附近wifi热点失败");
            return geoLocationAPI;
        }
        for (ScanResult result : lsScanResult) {
            Log.i(TAG,"发现一个附近的wifi::"+result.SSID+"  mac地址是"+result.BSSID+"   信号强度是"+result.level);
            if(result == null)
                continue;
            ScanWifi scanWIFI = new ScanWifi(result);
            lsAllWIFI.add(scanWIFI);//防止重复
        }
        ArrayList<GoogleWifiInfo> wifiInfos = new ArrayList<>(lsAllWIFI.size());
        for (ScanWifi w:lsAllWIFI){
            if(w == null)continue;
            GoogleWifiInfo wifiInfo = new GoogleWifiInfo();
            wifiInfo.macAddress = w.mac.toUpperCase();//记录附近每个wifi路由器的mac地址
            wifiInfo.signalStrength = w.dBm;//通过信号强度来判断距离
            wifiInfo.channel = w.channel;//通过信道来判断ssid是否为同一个
            wifiInfos.add(wifiInfo);
        }
        geoLocationAPI.wifiAccessPoints = wifiInfos;
        return geoLocationAPI;
    }
    
}
