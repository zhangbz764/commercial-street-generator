package building;

import geometry.ZGeoFactory;
import wblut.geom.WB_Coord;
import wblut.geom.WB_Point;
import wblut.geom.WB_Polygon;
import wblut.geom.WB_Segment;

/**
 * description
 *
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2020/12/9
 * @time 15:59
 */
public class Wall {
    private WB_Polygon shape;

    public Wall(WB_Segment edge, double height) {
        // origin shape
        WB_Point[] points = new WB_Point[5];
        points[0] = (WB_Point) edge.getOrigin();
        points[1] = (WB_Point) edge.getEndpoint();
        points[2] = ((WB_Point) edge.getEndpoint()).add(0, height);
    }
}
