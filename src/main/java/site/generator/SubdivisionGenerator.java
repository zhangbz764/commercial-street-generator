package site.generator;

import geometry.ZGeoFactory;
import main.MallConstant;
import math.ZGeoMath;
import org.locationtech.jts.algorithm.MinimumDiameter;
import org.locationtech.jts.geom.Polygon;
import processing.core.PApplet;
import render.JtsRender;
import subdivision.ZSD_OBB;
import subdivision.ZSD_SideStrip;
import subdivision.ZSD_SkeVorStrip;
import transform.ZTransform;
import wblut.geom.WB_GeometryOp;
import wblut.geom.WB_Point;
import wblut.geom.WB_Polygon;
import wblut.processing.WB_Render;

import java.util.ArrayList;
import java.util.Arrays;
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
    private List<WB_Polygon> selected2;
    private SubStreetGenerator subStreetGenerator;

    private List<List<WB_Polygon>> divideResults;

    // type of results
    private List<WB_Polygon> invalidSites;
    private List<WB_Polygon> simpleShopSites;
    private List<WB_Polygon> anchorShopSites;

    /* ------------- constructor ------------- */

    public SubdivisionGenerator() {
        this.allSitesToSub = new ArrayList<>();
        this.trafficBlocks = new ArrayList<>();
        this.subTrafficBlocks = new ArrayList<>();

        this.invalidSites = new ArrayList<>();
        this.simpleShopSites = new ArrayList<>();
        this.anchorShopSites = new ArrayList<>();
        this.divideResults = new ArrayList<>();

        this.selected2 = new ArrayList<>();
    }

    /* ------------- member function ------------- */

    public void init(List<List<WB_Polygon>> sites, List<WB_Polygon> traffic) {
        setAllSitesToSub(sites);
        setTrafficBlocks(traffic);
    }

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
            System.out.println("coincident " + coincident.toString());
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
            System.out.println("indices " + Arrays.toString(indices));

            // perform subdivide
            ZSD_SideStrip side = new ZSD_SideStrip(valid);
            side.randomMode = true;
            side.setOffsetIndices(indices);
            side.setSpan(MallConstant.SIMPLE_SHOP_WIDTH);
            side.setOffsetDist(-15);
            side.performDivide();
            divideResults.add(side.getAllSubPolygons());

            for (WB_Polygon block : side.getAllSubPolygons()) {
                sortSubdivision(block);
            }
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
            for (WB_Polygon block : obb.getAllSubPolygons()) {
                sortSubdivision(block);
            }
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
            for (WB_Polygon block : skeVor.getAllSubPolygons()) {
                sortSubdivision(block);
            }
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
            for (WB_Polygon block : skeVor.getAllSubPolygons()) {
                sortSubdivision(block);
            }
            allSitesToSub.get(selectIndex).remove(selected);
        }
    }

    /**
     * add sub traffic graph in the selected block
     *
     * @return void
     */
    public void addSubGraph() {
        if (this.selected != null && this.selectIndex != -1) {
            WB_Polygon valid = ZGeoMath.polygonFaceUp(ZTransform.validateWB_Polygon(selected));
            this.subStreetGenerator = new SubStreetGenerator(valid);
        }
    }

    /**
     * drag update the sub graph by mouse
     *
     * @param pointerX mouse x
     * @param pointerY mouse y
     * @return void
     */
    public void updateMouseSubGraph(int pointerX, int pointerY) {
        if (this.subStreetGenerator != null) {
            subStreetGenerator.dragUpdate(pointerX, pointerY);
        }
    }

    /**
     * release update the sub graph by mouse
     *
     * @return void
     */
    public void updateReleaseSubGraph() {
        if (this.subStreetGenerator != null) {
            subStreetGenerator.releaseUpdate();
        }
    }

    /**
     * key update the sub graph
     *
     * @param pointerX mouse x
     * @param pointerY mouse y
     * @param app
     * @return void
     */
    public void updateKeySubGraph(int pointerX, int pointerY, PApplet app) {
        if (this.subStreetGenerator != null) {
            subStreetGenerator.keyUpdate(pointerX, pointerY, app);
        }
    }

    /**
     * make split in the sub traffic graph, can't adjust anymore
     *
     * @return void
     */
    public void confirmSubGraph() {
        if (this.selected != null && this.selectIndex != -1 && this.subStreetGenerator != null) {
            subTrafficBlocks.add(subStreetGenerator.getSubTrafficBlock());
            for (WB_Polygon block : subStreetGenerator.getSubSiteBlocks()) {
                sortSubdivision(block);
            }
            allSitesToSub.get(selectIndex).remove(selected);
            this.subStreetGenerator = null;
        }
    }

    /**
     * sort site by its area and shape
     *
     * @param initSubPolygon initial sub polygon
     * @return void
     */
    private void sortSubdivision(WB_Polygon initSubPolygon) {
        double area = Math.abs(initSubPolygon.getSignedArea());
        Polygon obb = (Polygon) MinimumDiameter.getMinimumRectangle(ZTransform.WB_PolygonToJtsPolygon(initSubPolygon));
        double length1 = obb.getCoordinates()[0].distance(obb.getCoordinates()[1]);
        double length2 = obb.getCoordinates()[1].distance(obb.getCoordinates()[2]);
        double ratio = Math.max(length1, length2) / Math.min(length1, length2);
        if (area < 50 || ratio > 10) {
            invalidSites.add(initSubPolygon);
        } else if (area >= 50 && area < 1000) {
            if (ratio > 3) {
                invalidSites.add(initSubPolygon);
            } else {
                simpleShopSites.add(initSubPolygon);
            }
        } else if (area >= 1000 && area < 2000 && ratio < 1.3) {
            anchorShopSites.add(initSubPolygon);
        } else {
            if (this.selected != null && this.selectIndex != -1) {
                allSitesToSub.get(selectIndex).add(initSubPolygon);
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

    public List<WB_Polygon> getAnchorShopSites() {
        return anchorShopSites;
    }

    public SubStreetGenerator getSubStreetGenerator() {
        return subStreetGenerator;
    }

    public List<WB_Polygon> getTrafficBlocks() {
        return trafficBlocks;
    }

    public List<WB_Polygon> getSubTrafficBlocks() {
        return subTrafficBlocks;
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

    public void setSelected2(double pointerX, double pointerY) {
        for (WB_Polygon site : invalidSites) {
            if (WB_GeometryOp.contains2D(new WB_Point(pointerX, pointerY), site)) {
                selected2.add(site);
                invalidSites.remove(site);
                break;
            }
        }
        for (WB_Polygon site : simpleShopSites) {
            if (WB_GeometryOp.contains2D(new WB_Point(pointerX, pointerY), site)) {
                selected2.add(site);
                simpleShopSites.remove(site);
                break;
            }
        }
        for (WB_Polygon site : anchorShopSites) {
            if (WB_GeometryOp.contains2D(new WB_Point(pointerX, pointerY), site)) {
                selected2.add(site);
                anchorShopSites.remove(site);
                break;
            }
        }
    }

    public void union() {
        List<WB_Polygon> union = new ArrayList<>();
        union.add(selected2.get(0));
        for (int i = 1; i < selected2.size(); i++) {
            union = ZGeoFactory.wbgf.unionPolygons2D(selected2.get(i), union);
        }
        for (WB_Polygon p : union) {
            sortSubdivision(p);
        }
        selected2 = new ArrayList<>();
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

    public void display(JtsRender jtsRender, WB_Render render, PApplet app) {
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
        for (WB_Polygon p : anchorShopSites) {
            render.drawPolygonEdges2D(p);
        }
        app.noFill();

        // draw selected
        if (selected != null) {
            app.stroke(0, 0, 255);
            app.strokeWeight(4);
            render.drawPolygonEdges2D(selected);
        }
        for (WB_Polygon p : selected2) {
            app.stroke(0, 255, 0);
            render.drawPolygonEdges2D(p);
        }

        // draw sub graph
        if (subStreetGenerator != null) {
            subStreetGenerator.display(jtsRender, render, app);
        }
        app.popStyle();
    }

    public void finalDisplay(JtsRender jtsRender, WB_Render render, PApplet app) {
        app.pushStyle();
        app.fill(130);
        for (WB_Polygon p : subTrafficBlocks) {
            render.drawPolygonEdges2D(p);
        }
        app.popStyle();
    }
}
