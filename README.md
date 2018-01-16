# CLocation-中国国行安卓手机使用Google定位服务解决方案

[![License](https://img.shields.io/badge/License%20-Apache%202-337ab7.svg)](https://www.apache.org/licenses/LICENSE-2.0)

* 国行安卓手机Google地图服务解决方案---[CMaps](https://github.com/KevinLiaoDev/CMaps)
## 简介
* 相信很多Android开发人员都有这样的痛点，当国内Android用户在国外要使用地图时，好像十分尴尬，没有非常成熟的地图解决方案。
* 由于众所周知的原因，谷歌的很多优秀服务在国内无法使用，其中国行手机由于阉割无法使用谷歌地图服务是一个非常让人头痛的问题。主要问题有：
  - 一方面，国内地图服务提供商（百度地图、高德地图、腾讯地图）在境外的地图资源少得可怜。
  - 另一方面，国行安卓手机由于系统缺少谷歌三大件，使得国行手机使用谷歌地图服务几乎变得不可能。
* 本项目是为了解决**地图定位**建立，围绕Google给出的API接口资源，通过一系列技术方案，使得国行手机在没有谷歌三大件的情况下也可以使用谷歌的地图服务。主要特征有：
  - 包含WIFI信号、基站和GPS的多重定位方案，具有定位误差小、适用性广、低消耗、速度快等特点。
  - 基于谷歌数据的定位，在国外实地测试定位准确率要明显高于国内定位服务。
  - 封装后的库体积小、逻辑清晰、使用简单，项目内还有多种相关实用工具可供使用。

## 使用前准备
### 需要的权限
```xml
<uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```
### 获取Google Location API KEY
* 由于定位数据来源于谷歌，所以需要在谷歌控制台获取API KEY。详情参考[Google Maps Geolocation API](https://developers.google.com/maps/documentation/geolocation/intro?hl=zh_CN)
```xml
<meta-data
    android:name="GOOGLE_LOCATION_API_KEY"
    android:value="YOUR_API_KEY" />
```
### 切换定位域名（非必须）
```java
GetLocation.getInstance().setNetworkLocationUrl("https://googleapis.xxxxxx.com/");
```
* 虽然该方案最主要的目标用户是在境外的国行手机，那么访问谷歌接口理应没有太大问题，但是不排除调试或者部分人需要在国内使用，特意封装了修改定位域名的方法。
  - 如果你想在中国境内使用本方案中的网络定位，可在境外服务器反向代理如下接口：https://www.googleapis.com/geolocation/v1/geolocate?key=YOUR_API_KEY
  - 也可根据使用场景接入国内定位服务，在境外时使用CLocation，在境内时使用国内定位服务。
### 使用多重定位（需访问谷歌服务器，境内网络需翻墙）
```java
GetLocation.getInstance().setMultiLocationListener(new GetLocation.OnMultiLocationListener() {
    @Override
    public void onMultiLocation(double multilatitude, double multilongitude, long multiupdateTime, float multiaccuracy, float multibearing) {
        Double latitude, longitude;
        MyLocation myLocation = new MyLocation(multilatitude, multilongitude);
        //由于坐标系差异，中国大陆坐标需要转换
        if(BoundaryCheck.getInstance().IsInsideChina(myLocation)) {
            Point point = CoordinateConversion.wgs_gcj_encrypts(multilatitude, multilongitude);
            latitude = point.getLat();
            longitude = point.getLng();
        }else {
            latitude = multilatitude;
            longitude = multilongitude;
        }
        //经度:longitude，纬度:latitude，精度:multiaccuracy，方位：multiaccuracy，更新时间：multiupdateTime
    }

    @Override
    public void onFailed(int errorCode, String msg) {

    }
});
GetLocation.getInstance().startMultiLocation(getApplication(), this, false);
```

### 使用网络定位（需访问谷歌服务器，境内网络需翻墙）
```java
GetLocation.getInstance().setNetworkLocationListener(new GetLocation.OnNetworkLocationListener() {
    @Override
    public void onNetworkLocation(double latitude, double longitude, long updateTime, float accuracy) {
        Double lat, lon;
        MyLocation myLocation = new MyLocation(latitude, longitude);
        //由于坐标系差异，中国大陆坐标需要转换
        if(BoundaryCheck.getInstance().IsInsideChina(myLocation)) {
            Point point = CoordinateConversion.wgs_gcj_encrypts(latitude, longitude);
            lat = point.getLat();
            lon = point.getLng();
        }else {
            lat = latitude;
            lon = longitude;
        }
        //经度:longitude，纬度:latitude，精度:accuracy，更新时间：updateTime
    }

    @Override
    public void onFailed(int errorCode, String msg) {
        
    }
});
GetLocation.getInstance().startNetworkLocation(getApplication());
```
### 使用GPS定位(需要在室外能搜索到GPS信号的地方使用)
```java
GetLocation.getInstance().setGPSLocationListener(new GetLocation.OnGPSLocationListener() {
    @Override
    public void onGPSLocation(double latitude, double longitude, long updateTime, float accuracy, float bearing) {
        Double lat, lon;
        MyLocation myLocation = new MyLocation(latitude, longitude);
        //由于坐标系差异，中国大陆坐标需要转换
        if(BoundaryCheck.getInstance().IsInsideChina(myLocation)) {
            Point point = CoordinateConversion.wgs_gcj_encrypts(latitude, longitude);
            lat = point.getLat();
            lon = point.getLng();
        }else {
            lat = latitude;
            lon = longitude;
        }
        //经度:longitude，纬度:latitude，精度:accuracy，方位：accuracy，更新时间：updateTime
    }

    @Override
    public void onFailed(int errorCode, String msg) {
        
    }
});
GetLocation.getInstance().startGPSLocation(getApplication(),this);
```
### 释放资源
无论哪种的定位方式，在activity生命周期stop中一定要释放资源并且停止定位，以防资源浪费和异常奔溃
```java
@Override
protected void onStop() {
    super.onStop();
    GetLocation.getInstance().onStop();
}
```
### 关于
* 有任何建议或者使用中遇到问题都可以给我发邮件
* Email：kevinliaodev@163.com
