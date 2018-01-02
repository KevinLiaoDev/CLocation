package top.kevinliaodev.clocation.utils;

public class GetDistance {
    /**
     * 根据经纬度计算两点间的距离
     * @param lat_a
     * @param lng_a
     * @param lat_b
     * @param lng_b
     * @return
     */
    public static double getDistance(double lat_a, double lng_a, double lat_b, double lng_b) {
        final double M_PI = 3.14159265358979323846264338327950288, EARTH_RADIUS = 6378138.0;
        final double dd = M_PI / 180.0;

        double lon2 = lng_b;
        double lat2 = lat_b;

        double x1 = lat_a * dd, x2 = lat2 * dd;
        double y1 = lng_a * dd, y2 = lon2 * dd;
        double distance = (2 * EARTH_RADIUS * Math.asin(Math.sqrt(2 - 2 * Math.cos(x1)
                * Math.cos(x2) * Math.cos(y1 - y2) - 2 * Math.sin(x1)
                * Math.sin(x2)) / 2));
        return distance;
    }
}
