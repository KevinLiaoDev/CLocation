package top.madev.clocationlib.bean;

/**
 * Created by Lk on 2016/12/14.
 */
public class MyLocation {
    public double latitude;
    public double longitude;
    public long updateTime;//最后更新时间，用于做精确度择优
    public float accuracy;//精度
    public float bearing;//方位（GPS定位时可获取）
    private static MyLocation myLocation;
    MyLocation(){}

    public MyLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static MyLocation getInstance(){
        if(myLocation == null)
            myLocation = new MyLocation();
        return myLocation;
    }
}
