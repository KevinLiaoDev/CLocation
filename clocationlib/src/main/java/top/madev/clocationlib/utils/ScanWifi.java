package top.madev.clocationlib.utils;

import android.net.wifi.ScanResult;

/**
 * Created by lk on 2017/2/27.
 * 扫描搜索附近wifi信息
 */

public class ScanWifi implements Comparable<ScanWifi> {

    public final int dBm;
    public final String ssid;
    public final String mac;
    public short channel;
    public ScanWifi(ScanResult scanresult) {
        dBm = scanresult.level;
        ssid = scanresult.SSID;
        mac = scanresult.BSSID;//BSSID就是传说中的mac
        channel = getChannelByFrequency(scanresult.frequency);
    }
    public ScanWifi(String s, int i, String s1, String imac) {
        dBm = i;
        ssid = s1;
        mac = imac;
    }

    /**
     * 根据信号强度进行排序
     * @param wifiinfo
     * @return
     */
    public int compareTo(ScanWifi wifiinfo) {
        int i = wifiinfo.dBm;
        int j = dBm;
        return i - j;
    }

    /**
     * 为了防止添加wifi的列表重复，复写equals方法
     * @param obj
     * @return
     */
    public boolean equals(Object obj) {
        boolean flag = false;
        if (obj == this) {
            flag = true;
            return flag;
        } else {
            if (obj instanceof ScanWifi) {
                ScanWifi wifiinfo = (ScanWifi) obj;
                int i = wifiinfo.dBm;
                int j = dBm;
                if (i == j) {
                    String s = wifiinfo.mac;
                    String s1 = this.mac;
                    if (s.equals(s1)) {
                        flag = true;
                        return flag;
                    }
                }
                flag = false;
            } else {
                flag = false;
            }
        }
        return flag;
    }
    public int hashCode() {
        int i = dBm;
        int j = mac.hashCode();
        return i ^ j;
    }

    /**
     * 根据频率获得信道
     *
     * @param frequency
     * @return
     */
    public static short getChannelByFrequency(int frequency) {
        short channel = -1;
        switch (frequency) {
            case 2412:
                channel = 1;
                break;
            case 2417:
                channel = 2;
                break;
            case 2422:
                channel = 3;
                break;
            case 2427:
                channel = 4;
                break;
            case 2432:
                channel = 5;
                break;
            case 2437:
                channel = 6;
                break;
            case 2442:
                channel = 7;
                break;
            case 2447:
                channel = 8;
                break;
            case 2452:
                channel = 9;
                break;
            case 2457:
                channel = 10;
                break;
            case 2462:
                channel = 11;
                break;
            case 2467:
                channel = 12;
                break;
            case 2472:
                channel = 13;
                break;
            case 2484:
                channel = 14;
                break;
            case 5745:
                channel = 149;
                break;
            case 5765:
                channel = 153;
                break;
            case 5785:
                channel = 157;
                break;
            case 5805:
                channel = 161;
                break;
            case 5825:
                channel = 165;
                break;
        }
        return channel;
    }
}
