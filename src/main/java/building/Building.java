package building;

import org.locationtech.jts.geom.Polygon;
import wblut.geom.WB_Polygon;
import wblut.geom.WB_Vector;

/**
 * building in the commercial street
 *
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2020/12/9
 * @time 15:51
 */
public class Building {
    private WB_Polygon buildingSite;
    private WB_Vector buildingDir;

    private double area;

    /* ------------- constructor ------------- */

    public Building() {

    }

    /* ------------- getter ------------- */

    public double getSiteArea() {
        return Math.abs(buildingSite.getSignedArea());
    }

    public double getArea() {
        return area;
    }

    public WB_Vector getBuildingDir() {
        return buildingDir;
    }

    /* ------------- setter ------------- */

    public void setBuildingSite(WB_Polygon buildingSite) {
        this.buildingSite = buildingSite;
    }

    /* ------------- draw ------------- */

    public void display() {

    }
}
