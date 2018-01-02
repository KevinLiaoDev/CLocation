package top.kevinliaodev.clocation.bean;

public class Point {
    private double lat;// 纬度
    private double lng;// 经度

    public Point() {
    }

    public Point(double lng, double lat) {
        this.lng = lng;
        this.lat = lat;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Point) {
            Point bmapPoint = (Point) obj;
            return (bmapPoint.getLng() == lng && bmapPoint.getLat() == lat) ? true : false;
        } else {
            return false;
        }
    }

    public double getLat() {
        return lat;
    }
    public void setLat(double lat) {
        this.lat = lat;
    }
    public double getLng() {
        return lng;
    }
    public void setLng(double lng) {
        this.lng = lng;
    }

    @Override
    public String toString() {
        return "Point [lat=" + lat + ", lng=" + lng + "]";
    }

}
