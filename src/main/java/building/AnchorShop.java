package building;

import geometry.ZLargestRectangleRatio;
import geometry.ZPoint;
import math.ZGeoMath;
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

/**
 * description
 *
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2021/1/15
 * @time 16:30
 */
public class AnchorShop {
    private WB_Polygon originalSite;
    private WB_Polygon generateSite;

    private WB_Point siteCenter;
    private WB_Point siteDirPoint;

    private List<WB_Polygon> buildingBases;
    private List<BasicBuilding> buildings;

    /* ------------- constructor ------------- */

    public AnchorShop(WB_Polygon originalSite, List<WB_Polygon> trafficBlocks, List<WB_Polygon> subTrafficBlocks) {
        initGenerateSite(originalSite, trafficBlocks, subTrafficBlocks);
    }

    public void initGenerateSite(WB_Polygon originalSite, List<WB_Polygon> trafficBlocks, List<WB_Polygon> subTrafficBlocks) {
        setOriginalSite(originalSite);
        setGenerateSite(originalSite);
        // calculate site center
        double sumX = 0, sumY = 0;
        for (int i = 0; i < generateSite.getNumberOfPoints() - 1; i++) {
            sumX = sumX + generateSite.getPoint(i).xd();
            sumY = sumY + generateSite.getPoint(i).yd();
        }
        this.siteCenter = new WB_Point(
                sumX / (generateSite.getNumberOfPoints() - 1),
                sumY / (generateSite.getNumberOfPoints() - 1)
        );
        resetSiteDir(trafficBlocks, subTrafficBlocks);
    }

    public void initBuildings() {
        setBuildingBases(generateSite);
        setBuildings(buildingBases);
    }

    /* ------------- member function ------------- */

    /**
     * set rectangle site to generate building groups
     * record the w / h ratio of the OBB and generate largest rectangle in it
     *
     * @param originalSite original site polygon
     * @return void
     */
    private void setGenerateSite(final WB_Polygon originalSite) {
        Polygon jtsSite = ZTransform.WB_PolygonToJtsPolygon(originalSite);
        Geometry obb = MinimumDiameter.getMinimumRectangle(jtsSite);
        if (obb instanceof Polygon) {
            double edgeLength1 = obb.getCoordinates()[0].distance(obb.getCoordinates()[1]);
            double edgeLength2 = obb.getCoordinates()[1].distance(obb.getCoordinates()[2]);
            double whRatio = edgeLength1 / edgeLength2;
            ZLargestRectangleRatio largestRect = new ZLargestRectangleRatio(originalSite, whRatio);
            largestRect.init();
            this.generateSite = largestRect.getLargestRectangle();

            this.siteDirPoint = generateSite.getSegment(0).getCenter();
        } else {
            System.out.println("oriented bounding box is not a Polygon, please check input");
        }
    }

    /**
     * find the closest point to the traffic blocks
     * rebuild the generateSite by the direction of the closest point
     *
     * @param trafficBlocks    traffic blocks
     * @param subTrafficBlocks sub traffic blocks
     * @return void
     */
    public void resetSiteDir(List<WB_Polygon> trafficBlocks, List<WB_Polygon> subTrafficBlocks) {
        // rebuild site polygon by direction
        // the direction depends on the closest point to the traffic
        WB_Point closest = null;
        double minDist = Double.MAX_VALUE;
        for (WB_Polygon trafficBlock : trafficBlocks) {
            WB_Point currClosest = WB_GeometryOp.getClosestPoint2D(siteCenter, (WB_PolyLine) trafficBlock);
            double currSqDist = siteCenter.getSqDistance(currClosest);
            if (currSqDist < minDist) {
                closest = currClosest;
                minDist = currSqDist;
            }
        }
        for (WB_Polygon subTrafficBlock : subTrafficBlocks) {
            WB_Point currClosest = WB_GeometryOp.getClosestPoint2D(siteCenter, (WB_PolyLine) subTrafficBlock);
            double currSqDist = siteCenter.getSqDistance(currClosest);
            if (currSqDist < minDist) {
                closest = currClosest;
                minDist = currSqDist;
            }
        }
        if (closest != null) {
            ZPoint[] ray = new ZPoint[]{
                    new ZPoint(siteCenter),
                    new ZPoint(closest.sub(siteCenter))
            };
            List<WB_Point> newPoints = new ArrayList<>();
            int startIndex = 0;
            for (int i = 0; i < generateSite.getNumberSegments(); i++) {
                WB_Segment segment = generateSite.getSegment(i);
                ZPoint[] seg = new ZPoint[]{
                        new ZPoint(segment.getOrigin()),
                        new ZPoint(segment.getEndpoint()).sub(new ZPoint(segment.getOrigin()))
                };
                if (ZGeoMath.checkRaySegmentIntersection(ray, seg)) {
                    startIndex = i;
                    break;
                }
            }
            for (int i = 0; i < generateSite.getNumberOfPoints(); i++) {
                newPoints.add(generateSite.getPoint((i + startIndex) % (generateSite.getNumberOfPoints() - 1)));
            }
            this.generateSite = new WB_Polygon(newPoints);
            this.siteDirPoint = generateSite.getSegment(0).getCenter();
        }
    }

    /**
     * generate building base patterns
     *
     * @param generateSite rectangle site to generate building groups
     * @return void
     */
    public void setBuildingBases(WB_Polygon generateSite) {
        // assert rectangle
        this.buildingBases = new ArrayList<>();

        double[] dist1 = new double[]{
                ZMath.random(
                        generateSite.getSegment(0).getLength() * 0.25,
                        generateSite.getSegment(0).getLength() * 0.3
                ),
                ZMath.random(
                        generateSite.getSegment(1).getLength() * 0.35,
                        generateSite.getSegment(1).getLength() * 0.45
                )
        };
        WB_Point p1 = generateSite.getSegment(0).getPoint(dist1[0]);
        WB_Point[] base1 = new WB_Point[]{
                generateSite.getPoint(0),
                p1,
                p1.add(
                        generateSite.getSegment(1).getDirection().xd() * dist1[1],
                        generateSite.getSegment(1).getDirection().yd() * dist1[1]
                ),
                generateSite.getPoint(0).add(
                        generateSite.getSegment(1).getDirection().xd() * dist1[1],
                        generateSite.getSegment(1).getDirection().yd() * dist1[1]
                ),
                generateSite.getPoint(0)
        };

        double[] dist2 = new double[]{
                ZMath.random(
                        generateSite.getSegment(0).getLength() * 0.3,
                        generateSite.getSegment(0).getLength() * 0.4
                ),
                ZMath.random(
                        generateSite.getSegment(1).getLength() * 0.5,
                        generateSite.getSegment(1).getLength() * 0.65
                )
        };
        WB_Point p2 = generateSite.getSegment(2).getPoint(dist2[0]);
        WB_Point[] base2 = new WB_Point[]{
                generateSite.getPoint(2),
                p2,
                p2.add(
                        generateSite.getSegment(3).getDirection().xd() * dist2[1],
                        generateSite.getSegment(3).getDirection().yd() * dist2[1]
                ),
                generateSite.getPoint(2).add(
                        generateSite.getSegment(3).getDirection().xd() * dist2[1],
                        generateSite.getSegment(3).getDirection().yd() * dist2[1]
                ),
                generateSite.getPoint(2)
        };

        double[] dist3 = new double[]{
                ZMath.random(
                        generateSite.getSegment(0).getLength() * 0.4,
                        generateSite.getSegment(0).getLength() * 0.5
                ),
                ZMath.random(
                        generateSite.getSegment(1).getLength() * 0.45,
                        generateSite.getSegment(1).getLength() * 0.6
                )
        };
        WB_Point p3 = generateSite.getSegment(2).getPoint(dist3[0]);
        WB_Point[] base3 = new WB_Point[]{
                p3,
                generateSite.getPoint(3),
                generateSite.getPoint(3).add(
                        generateSite.getSegment(3).getDirection().xd() * dist3[1],
                        generateSite.getSegment(3).getDirection().yd() * dist3[1]
                ),
                p3.add(
                        generateSite.getSegment(3).getDirection().xd() * dist3[1],
                        generateSite.getSegment(3).getDirection().yd() * dist3[1]
                ),
                p3
        };
        buildingBases.add(new WB_Polygon(base1));
        buildingBases.add(new WB_Polygon(base2));
        buildingBases.add(new WB_Polygon(base3));
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
            buildings.add(new BasicBuilding(base, ZMath.randomInt(2.5, 3.5), ZMath.random(3, 4), true));
        }
    }

    /* ------------- setter & getter ------------- */

    public void setOriginalSite(WB_Polygon originalSite) {
        this.originalSite = originalSite;
    }

    public List<BasicBuilding> getBuildings() {
        return buildings;
    }

    /* ------------- draw ------------- */

    public void displayBuilding(WB_Render render, PApplet app){
        app.noStroke();
        if (buildings != null) {
            for (BasicBuilding building : buildings) {
                building.display(render, app);
            }
        }
    }

    public void displaySite(WB_Render render, PApplet app) {
        app.noFill();
        app.stroke(255, 0, 0);
        if (generateSite != null) {
            render.drawPolygonEdges2D(generateSite);
            app.line(siteCenter.xf(), siteCenter.yf(), siteDirPoint.xf(), siteDirPoint.yf());
        }
    }
}
