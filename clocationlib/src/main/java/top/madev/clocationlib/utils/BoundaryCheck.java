package top.madev.clocationlib.utils;

import top.madev.clocationlib.bean.MyLocation;

/**
 * Created by lk on 2017/2/28.
 * 根据国家行政边界近似判定
 * http://blog.csdn.net/rongge2008/article/details/50549940
 */

public class BoundaryCheck {

    private class Rectangle
    {
        public double West;
        public double North;
        public double East;
        public double South;
        public Rectangle(double latitude1, double longitude1, double latitude2, double longitude2)
        {
            this.West = Math.min(longitude1, longitude2);
            this.North = Math.max(latitude1, latitude2);
            this.East = Math.max(longitude1, longitude2);
            this.South = Math.min(latitude1, latitude2);
        }
    }

    private Rectangle[] region = new Rectangle[]{
                    new Rectangle(49.220400, 079.446200, 42.889900, 096.330000),
                    new Rectangle(54.141500, 109.687200, 39.374200, 135.000200),
                    new Rectangle(42.889900, 073.124600, 29.529700, 124.143255),
                    new Rectangle(29.529700, 082.968400, 26.718600, 097.035200),
                    new Rectangle(29.529700, 097.025300, 20.414096, 124.367395),
                    new Rectangle(20.414096, 107.975793, 17.871542, 111.744104),

            };
    private Rectangle[] exclude = new Rectangle[]{
                    new Rectangle(25.398623, 119.921265, 21.785006, 122.497559),
                    new Rectangle(22.284000, 101.865200, 20.098800, 106.665000),
                    new Rectangle(21.542200, 106.452500, 20.487800, 108.051000),
                    new Rectangle(55.817500, 109.032300, 50.325700, 119.127000),
                    new Rectangle(55.817500, 127.456800, 49.557400, 137.022700),
                    new Rectangle(44.892200, 131.266200, 42.569200, 137.022700),
            };

    private static BoundaryCheck boundaryCheck;

    public static BoundaryCheck getInstance(){
        if(boundaryCheck == null)
            boundaryCheck = new BoundaryCheck();
        return boundaryCheck;
    }

    private boolean InRectangle(Rectangle rect, MyLocation pos)
    {
        return rect.West <= pos.longitude && rect.East >= pos.longitude && rect.North >= pos.latitude && rect.South <= pos.latitude;
    }

    public boolean IsInsideChina(MyLocation pos)
    {
        for (int i = 0; i < region.length; i++)
        {
            if (InRectangle(region[i], pos))
            {
                for (int j = 0; j < exclude.length; j++)
                {
                    if (InRectangle(exclude[j], pos))
                    {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

}
