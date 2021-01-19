package building;

import geometry.ZGeoFactory;
import geometry.ZPoint;
import math.ZGeoMath;
import math.ZMath;
import processing.core.PApplet;
import transform.ZTransform;
import wblut.geom.*;
import wblut.processing.WB_Render;

import java.util.ArrayList;
import java.util.List;

/**
 * building in the commercial street
 *
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2020/12/9
 * @time 15:51
 */
public class BasicBuilding {
    private ZPoint buildCenter;
    private ZPoint buildingDir = new ZPoint(0, 1);

    private int storey = 2; // 层数
    private double storeyHeight = 3; // 层高

    private WB_Polygon base; // must be rectangle

    private WB_Polygon front;
    private WB_Polygon right;
    private WB_Polygon back;
    private WB_Polygon left;

    private List<WB_Polygon> roof;
    private double roofAngle = Math.PI / 7;
    private double roofBufferDistFB = 1;
    private double roofBufferDistLR = 0.5;

    private List<WB_Polygon> allSurfaces;

    private double area; // base area

    /* ------------- constructor ------------- */

    public BasicBuilding(WB_Polygon buildingSite, ZPoint buildingDir) {
        this.buildingDir = buildingDir;
        setBuildCenter(buildingSite);
        setBaseByDir(buildingSite);
        this.area = Math.abs(base.getSignedArea());
        setRoofBufferDist();

        initBuilding();
    }

    public BasicBuilding(WB_Polygon buildingSite) {
        setBuildCenter(buildingSite);
        this.base = buildingSite;
        this.area = Math.abs(base.getSignedArea());
        setRoofBufferDist();

        initBuilding();
    }

    public BasicBuilding(WB_Polygon buildingSite, int storey, double storeyHeight, boolean autoDir) {
        if (!autoDir) {
            setBuildCenter(buildingSite);
            this.base = buildingSite;
            this.storey = storey;
            this.storeyHeight = storeyHeight;
            this.area = Math.abs(base.getSignedArea());
            setRoofBufferDist();

            initBuilding();
        } else {
            setBuildCenter(buildingSite);
            setBaseByAutoDir(buildingSite);
            this.storey = storey;
            this.storeyHeight = storeyHeight;
            this.area = Math.abs(base.getSignedArea());
            setRoofBufferDist();

            initBuilding();
        }
    }

    /* ------------- member function ------------- */

    /**
     * initializer
     *
     * @return void
     */
    public void initBuilding() {
        this.roof = new ArrayList<>();
        // buffered boundary for roof
//        WB_Polygon roofBoundary = ZGeoMath.polygonFaceUp(
//                ZTransform.validateWB_Polygon(
//                        ZGeoFactory.wbgf.createBufferedPolygonsStraight2D(base, roofBufferDistFB).get(0)
//                )
//        );

        WB_Vector v0 = (WB_Vector) base.getSegment(0).getDirection();
        WB_Vector v1 = (WB_Vector) base.getSegment(1).getDirection();
        WB_Point p0 = base.getPoint(0).add(v0.scale(roofBufferDistLR * -1)).add(v1.scale(roofBufferDistFB * -1));
        WB_Point p1 = base.getPoint(1).add(v0.scale(roofBufferDistLR)).add(v1.scale(roofBufferDistFB * -1));
        WB_Point p2 = base.getPoint(2).add(v0.scale(roofBufferDistLR)).add(v1.scale(roofBufferDistFB));
        WB_Point p3 = base.getPoint(3).add(v0.scale(roofBufferDistLR * -1)).add(v1.scale(roofBufferDistFB));

        // find two ridge points
        double ridgeZ = storey * storeyHeight + Math.tan(roofAngle) * (p1.getDistance(p2) * 0.5 - roofBufferDistFB);
        WB_Point ridgeRight = new WB_Point(
                (p1.xd() + p2.xd()) * 0.5,
                (p1.yd() + p2.yd()) * 0.5,
                ridgeZ
        );
        WB_Point ridgeLeft = new WB_Point(
                (p3.xd() + p0.xd()) * 0.5,
                (p3.yd() + p0.yd()) * 0.5,
                ridgeZ
        );

        // front slope
        WB_Point firstP1 = new WB_Point(p0.xd(), p0.yd(), storey * storeyHeight - Math.tan(roofAngle) * roofBufferDistFB);
        roof.add(new WB_Polygon(
                firstP1,
                new WB_Point(p1.xd(), p1.yd(), storey * storeyHeight - Math.tan(roofAngle) * roofBufferDistFB),
                ridgeRight,
                ridgeLeft,
                firstP1
        ));
        // back slope
        WB_Point firstP2 = new WB_Point(p2.xd(), p2.yd(), storey * storeyHeight - Math.tan(roofAngle) * roofBufferDistFB);
        roof.add(new WB_Polygon(
                firstP2,
                new WB_Point(p3.xd(), p3.yd(), storey * storeyHeight - Math.tan(roofAngle) * roofBufferDistFB),
                ridgeLeft,
                ridgeRight,
                firstP2
        ));

        this.front = new WB_Polygon(new WB_Point[]{
                base.getPoint(0),
                base.getPoint(1),
                new WB_Point(base.getPoint(1).xd(), base.getPoint(1).yd(), storey * storeyHeight),
                new WB_Point(base.getPoint(0).xd(), base.getPoint(0).yd(), storey * storeyHeight),
                base.getPoint(0)
        });
        this.right = new WB_Polygon(new WB_Point[]{
                base.getPoint(1),
                base.getPoint(2),
                new WB_Point(base.getPoint(2).xd(), base.getPoint(2).yd(), storey * storeyHeight),
                new WB_Point(base.getSegment(1).getCenter().xd(), base.getSegment(1).getCenter().yd(), ridgeZ),
                new WB_Point(base.getPoint(1).xd(), base.getPoint(1).yd(), storey * storeyHeight),
                base.getPoint(1)
        });
        this.back = new WB_Polygon(new WB_Point[]{
                base.getPoint(2),
                base.getPoint(3),
                new WB_Point(base.getPoint(3).xd(), base.getPoint(3).yd(), storey * storeyHeight),
                new WB_Point(base.getPoint(2).xd(), base.getPoint(2).yd(), storey * storeyHeight),
                base.getPoint(2)
        });
        this.left = new WB_Polygon(new WB_Point[]{
                base.getPoint(3),
                base.getPoint(0),
                new WB_Point(base.getPoint(0).xd(), base.getPoint(0).yd(), storey * storeyHeight),
                new WB_Point(base.getSegment(3).getCenter().xd(), base.getSegment(3).getCenter().yd(), ridgeZ),
                new WB_Point(base.getPoint(3).xd(), base.getPoint(3).yd(), storey * storeyHeight),
                base.getPoint(3)
        });

        this.allSurfaces = new ArrayList<>();
        allSurfaces.add(base);
        allSurfaces.add(front);
        allSurfaces.add(right);
        allSurfaces.add(back);
        allSurfaces.add(left);
        allSurfaces.addAll(roof);
    }

    /**
     * description
     *
     * @param dir target vector
     * @return void
     */
    private void applyTransform(WB_Vector dir) {
        WB_Transform3D transform3D = new WB_Transform3D(new WB_Vector(0, 1, 0), dir);
        for (WB_Polygon p : allSurfaces) {
            for (int i = 0; i < p.getNumberOfPoints(); i++) {
                p.getPoint(i).set(transform3D.applyAsPoint(p.getPoint(i)));
            }
        }
    }

    /* ------------- getter ------------- */

    public double getArea() {
        return area;
    }

    public ZPoint getBuildingDir() {
        return buildingDir;
    }

    public List<WB_Polygon> getAllSurfaces() {
        return allSurfaces;
    }

    public List<WB_Polygon> getRoof() {
        return roof;
    }

    /* ------------- setter ------------- */

    /**
     * calculate center
     *
     * @param buildingSite input original site
     * @return void
     */
    private void setBuildCenter(WB_Polygon buildingSite) {
        // calculate center
        double sumX = 0, sumY = 0;
        for (int i = 0; i < buildingSite.getNumberOfPoints() - 1; i++) {
            sumX = sumX + buildingSite.getPoint(i).xd();
            sumY = sumY + buildingSite.getPoint(i).yd();
        }
        this.buildCenter = new ZPoint(
                sumX / (buildingSite.getNumberOfPoints() - 1),
                sumY / (buildingSite.getNumberOfPoints() - 1)
        );
    }

    /**
     * reset points order of original site
     * by checking which edge is intersected with buildingDir
     *
     * @param buildingSite input original site
     * @return void
     */
    private void setBaseByDir(WB_Polygon buildingSite) {
        List<WB_Point> newPoints = new ArrayList<>();
        int startIndex = 0;
        ZPoint[] ray = new ZPoint[]{this.buildCenter, this.buildingDir};
        for (int i = 0; i < buildingSite.getNumberSegments(); i++) {
            WB_Segment segment = buildingSite.getSegment(i);
            ZPoint[] seg = new ZPoint[]{
                    new ZPoint(segment.getOrigin()),
                    new ZPoint(segment.getEndpoint()).sub(new ZPoint(segment.getOrigin()))
            };
            if (ZGeoMath.checkRaySegmentIntersection(ray, seg)) {
                startIndex = i;
                break;
            }
        }
        for (int i = 0; i < buildingSite.getNumberOfPoints(); i++) {
            newPoints.add(buildingSite.getPoint((i + startIndex) % (buildingSite.getNumberOfPoints() - 1)));
        }
        this.base = new WB_Polygon(newPoints);
    }

    /**
     * reset base by judging the longer edge
     *
     * @param buildingSite input original site
     * @return void
     */
    private void setBaseByAutoDir(WB_Polygon buildingSite) {
        if (buildingSite.getSegment(0).getLength() < buildingSite.getSegment(1).getLength()) {
            WB_Point[] newPoints = new WB_Point[buildingSite.getNumberOfPoints()];
            newPoints[0] = buildingSite.getPoint(1);
            newPoints[1] = buildingSite.getPoint(2);
            newPoints[2] = buildingSite.getPoint(3);
            newPoints[3] = buildingSite.getPoint(0);
            newPoints[4] = buildingSite.getPoint(1);
            this.base = new WB_Polygon(newPoints);
        } else {
            this.base = buildingSite;
        }
    }

    /**
     * set roof buffer distance by the longest edge of base
     *
     * @return void
     */
    private void setRoofBufferDist() {
        double length1 = base.getSegment(0).getLength();
        double length2 = base.getSegment(1).getLength();
        this.roofBufferDistFB = Math.max(length1, length2) * 0.12;
    }

    public void setStorey(int storey) {
        this.storey = storey;
    }

    public void setRandomStorey() {
        this.storey = ZMath.randomInt(1, 4);
    }

    /* ------------- draw ------------- */

    public void display(WB_Render render, PApplet app) {
        app.pushMatrix();
        app.fill(40, 35, 26);
        render.drawPolygonEdges(roof);
        app.fill(151, 145, 138);
        for (int i = 0; i < allSurfaces.size(); i++) {
            if (!roof.contains(allSurfaces.get(i))) {
                render.drawPolygonEdges(allSurfaces.get(i));
            }
        }
        app.translate(0, 0, storey * 3);
        app.popMatrix();
    }
}
