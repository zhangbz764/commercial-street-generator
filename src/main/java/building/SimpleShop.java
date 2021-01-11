package building;

import geometry.ZLargestRectangleRatio;
import math.ZMath;
import org.locationtech.jts.algorithm.MinimumDiameter;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import processing.core.PApplet;
import transform.ZTransform;
import wblut.geom.*;
import wblut.processing.WB_Render;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * simple shop
 *
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2021/1/5
 * @time 13:42
 */
public class SimpleShop {
    private WB_Polygon originalSite;
    private WB_Polygon generateSite;

    private List<WB_Polygon> buildingBases;
    private List<BasicBuilding> buildings;

    /* ------------- constructor ------------- */

    public SimpleShop(WB_Polygon originalSite) {
        setOriginalSite(originalSite);
        setGenerateSite(originalSite);
        setBuildingBases(generateSite);
        setBuildings(buildingBases);
    }

    /* ------------- member function ------------- */

    /**
     * set rectangle site to generate building groups
     * if the original shape is closed to its OBB, then let OBB be it's site
     * otherwise, record the w / h ratio of the OBB and generate largest rectangle in it
     *
     * @param originalSite original site polygon
     * @return void
     */
    private void setGenerateSite(final WB_Polygon originalSite) {
        Polygon jtsSite = ZTransform.WB_PolygonToJtsPolygon(originalSite);
        Geometry obb = MinimumDiameter.getMinimumRectangle(jtsSite);
        if (obb instanceof Polygon) {
            if (jtsSite.getArea() / obb.getArea() > 0.95) {
                this.generateSite = ZTransform.jtsPolygonToWB_Polygon((Polygon) obb);
            } else {
                double edgeLength1 = obb.getCoordinates()[0].distance(obb.getCoordinates()[1]);
                double edgeLength2 = obb.getCoordinates()[1].distance(obb.getCoordinates()[2]);
                double whRatio = edgeLength1 / edgeLength2;
                ZLargestRectangleRatio largestRect = new ZLargestRectangleRatio(originalSite, whRatio);
                largestRect.init();
                this.generateSite = largestRect.getLargestRectangle();
            }
        } else {
            System.out.println("oriented bounding box is not a Polygon, please check input");
        }
    }

    /**
     * choose different patterns based on the area and shape of site
     *
     * @param generateSite rectangle site to generate building groups
     * @return void
     */
    private void setBuildingBases(WB_Polygon generateSite) {
        double random = Math.random();
        if (random > 0.5) {
            setBasePattern1(generateSite);
        } else {
            setBasePattern2(generateSite);
        }
    }

    /**
     * set every single building in the bases
     *
     * @param buildingBases base rectangles computed
     * @return void
     */
    private void setBuildings(List<WB_Polygon> buildingBases) {
        this.buildings = new ArrayList<>();
        for (WB_Polygon base : buildingBases) {
            buildings.add(new BasicBuilding(base, ZMath.randomInt(2, 3.5)));
        }
    }

    /**
     * 三等分，两边建体量
     *
     * @param generateSite rectangle site to generate bases
     * @return void
     */
    private void setBasePattern1(final WB_Polygon generateSite) {
        // assert rectangle
        this.buildingBases = new ArrayList<>();
        double[] dist = ZMath.randomArray(
                2,
                generateSite.getSegment(1).getLength() * 0.3,
                generateSite.getSegment(1).getLength() * 0.36
        );
        WB_Point p = generateSite.getSegment(1).getPoint(dist[0]);
        WB_Point[] base1 = new WB_Point[]{
                generateSite.getPoint(0),
                generateSite.getPoint(1),
                p,
                generateSite.getPoint(0).add(p.sub(generateSite.getPoint(1))),
                generateSite.getPoint(0)
        };
        WB_Point q = generateSite.getSegment(3).getPoint(dist[1]);
        WB_Point[] base2 = new WB_Point[]{
                generateSite.getPoint(2),
                generateSite.getPoint(3),
                q,
                generateSite.getPoint(2).add(q.sub(generateSite.getPoint(3))),
                generateSite.getPoint(2)
        };
        buildingBases.add(new WB_Polygon(base1));
        buildingBases.add(new WB_Polygon(base2));
    }

    /**
     * 对角布置体量
     *
     * @param generateSite rectangle site to generate bases
     * @return void
     */
    private void setBasePattern2(final WB_Polygon generateSite) {
        // assert rectangle
        this.buildingBases = new ArrayList<>();
        double[] dist1 = ZMath.randomArray(
                2,
                generateSite.getSegment(0).getLength() * 0.5,
                generateSite.getSegment(0).getLength() * 0.7
        );
        double[] dist2 = ZMath.randomArray(
                2,
                generateSite.getSegment(1).getLength() * 0.5,
                generateSite.getSegment(1).getLength() * 0.7
        );
        WB_Point p = generateSite.getSegment(0).getPoint(dist1[0]);
        WB_Point[] base1 = new WB_Point[]{
                generateSite.getPoint(0),
                p,
                p.add(
                        generateSite.getSegment(1).getDirection().xd() * dist2[0],
                        generateSite.getSegment(1).getDirection().yd() * dist2[0]
                ),
                generateSite.getPoint(0).add(
                        generateSite.getSegment(1).getDirection().xd() * dist2[0],
                        generateSite.getSegment(1).getDirection().yd() * dist2[0]
                ),
                generateSite.getPoint(0)
        };
        WB_Point q = generateSite.getSegment(2).getPoint(dist1[1]);
        WB_Point[] base2 = new WB_Point[]{
                generateSite.getPoint(2),
                q,
                q.add(
                        generateSite.getSegment(3).getDirection().xd() * dist2[1],
                        generateSite.getSegment(3).getDirection().yd() * dist2[1]
                ),
                generateSite.getPoint(2).add(
                        generateSite.getSegment(3).getDirection().xd() * dist2[1],
                        generateSite.getSegment(3).getDirection().yd() * dist2[1]
                ),
                generateSite.getPoint(2)
        };
        buildingBases.add(new WB_Polygon(base1));
        buildingBases.add(new WB_Polygon(base2));
    }

    /**
     * only one building filling the whole site
     *
     * @param
     * @return void
     */
    private void setBasePattern3(final WB_Polygon generateSite) {
        // assert rectangle
        this.buildingBases = new ArrayList<>();
        buildingBases.add(generateSite);
    }

    /* ------------- setter & getter ------------- */

    public void setOriginalSite(WB_Polygon originalSite) {
        this.originalSite = originalSite;
    }

    public List<BasicBuilding> getBuildings() {
        return buildings;
    }

    /* ------------- draw ------------- */

    public void display(WB_Render render, PApplet app) {
        app.noFill();
        app.stroke(255, 0, 0);
        if (generateSite != null) {
            render.drawPolygonEdges2D(generateSite);
        }
        app.fill(200);
        app.noStroke();
        if (buildings != null) {
            for (BasicBuilding building : buildings) {
                building.display(render, app);
            }
        }
    }

}
