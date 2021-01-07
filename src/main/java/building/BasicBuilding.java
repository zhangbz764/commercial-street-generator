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
// TODO: 2021/1/4 完成基本building建模

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
    private double storeyHeight = 2; // 层高

    private WB_Polygon base; // must be rectangle

    private WB_Polygon front;
    private WB_Polygon right;
    private WB_Polygon back;
    private WB_Polygon left;

    private List<WB_Polygon> roof;
    private double roofAngle = Math.PI / 7;
    private double roofBuffferDist = 1;

    private List<WB_Polygon> allSurfaces;

    private double area; // base area

    /* ------------- constructor ------------- */

    public BasicBuilding(WB_Polygon buildingSite, ZPoint buildingDir) {
        this.buildingDir = buildingDir;
        setBuildCenter(buildingSite);
        setBaseByDir(buildingSite);
        this.area = Math.abs(base.getSignedArea());

        initBuilding();
    }

    public BasicBuilding(WB_Polygon buildingSite) {
        setBuildCenter(buildingSite);
        this.base = buildingSite;
        this.area = Math.abs(base.getSignedArea());

        initBuilding();
    }

    /* ------------- member function ------------- */

    /**
     * initializer
     *
     * @return void
     */
    public void initBuilding() {
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
                new WB_Point(base.getPoint(3).xd(), base.getPoint(3).yd(), storey * storeyHeight),
                base.getPoint(3)
        });

        calculateRoof();

        this.allSurfaces = new ArrayList<>();
        allSurfaces.add(base);
        allSurfaces.add(front);
        allSurfaces.add(right);
        allSurfaces.add(back);
        allSurfaces.add(left);
        allSurfaces.addAll(roof);

    }

    /**
     * calculate roof
     *
     * @return void
     */
    private void calculateRoof() {
        this.roof = new ArrayList<>();
        // buffered boundary for roof
        WB_Polygon roofBoundary = ZGeoMath.polygonFaceUp(
                ZTransform.validateWB_Polygon(
                        ZGeoFactory.wbgf.createBufferedPolygonsStraight2D(base, roofBuffferDist).get(0)
                )
        );
        // find two ridge points
        WB_Point ridgeRight = new WB_Point(
                (roofBoundary.getPoint(1).xd() + roofBoundary.getPoint(2).xd()) * 0.5,
                (roofBoundary.getPoint(1).yd() + roofBoundary.getPoint(2).yd()) * 0.5,
                storey * storeyHeight + Math.tan(roofAngle) * (roofBoundary.getSegment(1).getLength() * 0.5 - roofBuffferDist)
        );
        WB_Point ridgeLeft = new WB_Point(
                (roofBoundary.getPoint(3).xd() + roofBoundary.getPoint(0).xd()) * 0.5,
                (roofBoundary.getPoint(3).yd() + roofBoundary.getPoint(0).yd()) * 0.5,
                storey * storeyHeight + Math.tan(roofAngle) * (roofBoundary.getSegment(3).getLength() * 0.5 - roofBuffferDist)
        );

        // front slope
        roof.add(new WB_Polygon(
                new WB_Point(roofBoundary.getPoint(0).xd(), roofBoundary.getPoint(0).yd(), storey * storeyHeight - Math.tan(roofAngle) * roofBuffferDist),
                new WB_Point(roofBoundary.getPoint(1).xd(), roofBoundary.getPoint(1).yd(), storey * storeyHeight - Math.tan(roofAngle) * roofBuffferDist),
                ridgeRight,
                ridgeLeft
        ));
        // back slope
        roof.add(new WB_Polygon(
                new WB_Point(roofBoundary.getPoint(2).xd(), roofBoundary.getPoint(2).yd(), storey * storeyHeight - Math.tan(roofAngle) * roofBuffferDist),
                new WB_Point(roofBoundary.getPoint(3).xd(), roofBoundary.getPoint(3).yd(), storey * storeyHeight - Math.tan(roofAngle) * roofBuffferDist),
                ridgeLeft,
                ridgeRight
        ));
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

    public void setStorey(int storey) {
        this.storey = storey;
    }

    public void setRandomStorey() {
        this.storey = ZMath.randomInt(1, 4);
    }

    /* ------------- draw ------------- */

    public void display(WB_Render render, PApplet app) {
        app.pushMatrix();
        for (int i = 0; i < allSurfaces.size(); i++) {
            render.drawPolygonEdges(allSurfaces.get(i));
        }
        app.translate(0, 0, storey * 3);
        app.popMatrix();
    }
}
