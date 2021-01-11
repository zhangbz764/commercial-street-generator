package site.generator;

import main.MallConstant;
import math.ZGeoMath;
import processing.core.PApplet;
import subdivision.ZSD_OBB;
import subdivision.ZSD_SideStrip;
import subdivision.ZSD_SkeVorStrip;
import transform.ZTransform;
import wblut.geom.WB_GeometryOp;
import wblut.geom.WB_Point;
import wblut.geom.WB_Polygon;
import wblut.processing.WB_Render;

import java.util.ArrayList;
import java.util.List;

/**
 * generator of the site block partition
 *
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2020/12/10
 * @time 14:57
 */
public class SubdivisionGenerator {
    private List<List<WB_Polygon>> allSitesToSub;
    private List<WB_Polygon> trafficBlocks;
    private List<WB_Polygon> subTrafficBlocks;

    // record selected operation
    private WB_Polygon selected;
    private int selectIndex = -1;

    private List<List<WB_Polygon>> divideResults;

    // type of results
    private List<WB_Polygon> invalidSites;
    private List<WB_Polygon> simpleShopSites;
    private List<WB_Polygon> midShopSites;


    /* ------------- constructor ------------- */

    public SubdivisionGenerator(List<List<WB_Polygon>> sites, List<WB_Polygon> traffic) {
        setAllSitesToSub(sites);
        setTrafficBlocks(traffic);
        this.invalidSites = new ArrayList<>();
        this.simpleShopSites = new ArrayList<>();
        this.midShopSites = new ArrayList<>();
        this.divideResults = new ArrayList<>();

        this.subTrafficBlocks = new ArrayList<>();
    }

    /* ------------- member function ------------- */

    /**
     * perform side strip subdivide along traffic
     *
     * @return void
     */
    public void performSideStrip() {
        if (this.selected != null && this.selectIndex != -1) {
            // calculate coincident points indices of selected polygon and the traffic polygon
            WB_Polygon valid = ZGeoMath.polygonFaceUp(ZTransform.validateWB_Polygon(selected));
            List<Integer> coincident = new ArrayList<>();
            for (int i = 0; i < valid.getNumberOfPoints() - 1; i++) {
                if (WB_GeometryOp.contains2D(valid.getPoint(i), trafficBlocks.get(selectIndex))) {
                    coincident.add(i);
                }
            }

            // reorder the coincident indices as the input integer array for side strip
            int[] indices = new int[coincident.size() - 1];
            if (coincident.contains(0) && coincident.contains(valid.getNumberOfPoints() - 2)) {
                int jumpIndex = 0;
                for (int i = 0; i < coincident.size() - 1; i++) {
                    if (coincident.get(i + 1) != coincident.get(i) + 1) {
                        jumpIndex = i;
                        break;
                    }
                }
                List<Integer> sub1 = coincident.subList(jumpIndex + 1, coincident.size());
                List<Integer> sub2 = coincident.subList(0, jumpIndex);
                for (int i = 0; i < sub1.size(); i++) {
                    indices[i] = sub1.get(i);
                }
                for (int j = 0; j < sub2.size(); j++) {
                    indices[j + sub1.size()] = sub2.get(j);
                }
            } else {
                for (int i = 0; i < coincident.size() - 1; i++) {
                    indices[i] = coincident.get(i);
                }
            }

            // perform subdivide
            ZSD_SideStrip side = new ZSD_SideStrip(valid);
            side.randomMode = true;
            side.setOffsetIndices(indices);
            side.setSpan(MallConstant.SIMPLE_SHOP_WIDTH);
            side.setOffsetDist(-15);
            side.performDivide();
            divideResults.add(side.getAllSubPolygons());

            sortSubdivision(side.getAllSubPolygons());
            allSitesToSub.get(selectIndex).remove(selected);
        }
    }

    /**
     * perform OBB-based subdivide, to deal with large site
     *
     * @return void
     */
    public void performOBB() {
        if (this.selected != null && this.selectIndex != -1) {
            // perform subdivide
            ZSD_OBB obb = new ZSD_OBB(selected);
            obb.setCellConstraint(1);
            obb.performDivide();
            sortSubdivision(obb.getAllSubPolygons());
            allSitesToSub.get(selectIndex).remove(selected);
        }
    }

    /**
     * perform single strip subdivide, divide the site into slices
     *
     * @return void
     */
    public void performSingleStrip() {
        if (this.selected != null && this.selectIndex != -1) {
            WB_Polygon valid = ZGeoMath.polygonFaceUp(ZTransform.validateWB_Polygon(selected));
            // perform subdivide
            ZSD_SkeVorStrip skeVor = new ZSD_SkeVorStrip(valid);
            skeVor.setSpan(MallConstant.MID_SHOP_WIDTH);
            skeVor.performDivide();
            sortSubdivision(skeVor.getAllSubPolygons());
            allSitesToSub.get(selectIndex).remove(selected);
        }
    }

    /**
     * perform double strip subdivide, divide the site into two series of slices
     *
     * @return void
     */
    public void performDoubleStrip() {
        if (this.selected != null && this.selectIndex != -1) {
            WB_Polygon valid = ZGeoMath.polygonFaceUp(ZTransform.validateWB_Polygon(selected));
            // perform subdivide
            ZSD_SkeVorStrip skeVor = new ZSD_SkeVorStrip(valid);
            skeVor.setSpan(MallConstant.MID_SHOP_WIDTH);
            skeVor.setDepth(6);
            skeVor.performDivide();
            sortSubdivision(skeVor.getAllSubPolygons());
            allSitesToSub.get(selectIndex).remove(selected);
        }
    }

    /**
     * add sub traffic graph in the selected block
     *
     * @param
     * @return void
     */
    public void addSubGraph() {
        if (this.selected != null && this.selectIndex != -1) {
            WB_Polygon valid = ZGeoMath.polygonFaceUp(ZTransform.validateWB_Polygon(selected));
            SubStreetGenerator subStreetGenerator = new SubStreetGenerator(valid);
        }
    }

    public void confirmSubGraph(){

    }

    public void undo() {

    }

    /**
     * sort sites by its area
     *
     * @param allInitSubPolygons initial sub polygons
     * @return void
     */
    private void sortSubdivision(List<WB_Polygon> allInitSubPolygons) {
        for (WB_Polygon polygon : allInitSubPolygons) {
            double area = Math.abs(polygon.getSignedArea());
            if (area < 50) {
                invalidSites.add(polygon);
            } else if (area >= 50 && area < 400) {
                simpleShopSites.add(polygon);
            } else if (area >= 400 && area < 750) {
                midShopSites.add(polygon);
            } else {
                if (this.selected != null && this.selectIndex != -1) {
                    allSitesToSub.get(selectIndex).add(polygon);
                }
            }
        }
    }

    /* ------------- getter & setter ------------- */

    public void setAllSitesToSub(List<List<WB_Polygon>> allSitesToSub) {
        this.allSitesToSub = allSitesToSub;
    }

    public void setTrafficBlocks(List<WB_Polygon> trafficBlocks) {
        this.trafficBlocks = trafficBlocks;
    }

    public List<List<WB_Polygon>> getDivideResults() {
        return divideResults;
    }

    public List<WB_Polygon> getSimpleShopSites() {
        return simpleShopSites;
    }

    public List<WB_Polygon> getMidShopSites() {
        return midShopSites;
    }

    /* ------------- interaction ------------- */

    /**
     * select a site by mouse
     *
     * @param pointerX pointerX
     * @param pointerY pointerX
     * @return void
     */
    public void setSelected(double pointerX, double pointerY) {
        out:
        for (int i = 0; i < allSitesToSub.size(); i++) {
            for (WB_Polygon site : allSitesToSub.get(i)) {
                if (WB_GeometryOp.contains2D(new WB_Point(pointerX, pointerY), site)) {
                    this.selected = site;
                    this.selectIndex = i;
                    break out;
                }
            }
        }
    }

    /**
     * clear selected site
     *
     * @return void
     */
    public void clearSelected() {
        this.selected = null;
        this.selectIndex = -1;
    }

    /* ------------- draw ------------- */

    public void display(WB_Render render, PApplet app) {
        app.pushStyle();
        app.stroke(0);
        app.strokeWeight(1);
        // draw each type of sites
        app.fill(50);
        for (WB_Polygon p : invalidSites) {
            render.drawPolygonEdges2D(p);
        }
        app.fill(128);
        for (List<WB_Polygon> list : allSitesToSub) {
            for (WB_Polygon p : list) {
                render.drawPolygonEdges2D(p);
            }
        }
        app.fill(160, 200, 200);
        for (WB_Polygon p : simpleShopSites) {
            render.drawPolygonEdges2D(p);
        }
        app.fill(188, 80, 45);
        for (WB_Polygon p : midShopSites) {
            render.drawPolygonEdges2D(p);
        }
        app.noFill();

        if (selected != null) {
            app.stroke(0, 0, 255);
            app.strokeWeight(4);
            render.drawPolygonEdges2D(selected);
        }
        app.popStyle();
    }
}
