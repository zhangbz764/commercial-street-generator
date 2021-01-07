package site;

import igeo.ICurve;
import igeo.IG;
import igeo.IPoint;
import processing.core.PApplet;
import transform.ZTransform;
import wblut.geom.WB_Geometry2D;
import wblut.geom.WB_Point;
import wblut.geom.WB_PolyLine;
import wblut.geom.WB_Polygon;
import wblut.processing.WB_Render3D;

/**
 * input site geometries
 *
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2020/12/9
 * @time 16:15
 */
public class Importer {
    private double scale = 1;

    // geometries
    private WB_PolyLine[] inputSite;
    private WB_PolyLine[] inputSiteWater;
    private WB_Polygon[] inputBoundaries;
    private WB_Point[] inputEntries1;
    private WB_Point[] inputInnerNodes1;
    private WB_Point[] inputEntries2;
    private WB_Point[] inputInnerNodes2;

    // statistics
    private double[] boundaryArea;

    /* ------------- constructor ------------- */

    public Importer(double scale) {
        setScale(scale);
    }

    public Importer() {

    }

    /* ------------- loader ------------- */

    /**
     * load geometry from .3dm file
     *
     * @param path 3dm file path
     * @return void
     */
    public void loadData(String path) {
        System.out.println("** LOADING FILE **");
        IG.init();
        IG.open(path);

        // load site polyline
        ICurve[] site = IG.layer("site").curves();
        this.inputSite = new WB_PolyLine[site.length];
        for (int i = 0; i < site.length; i++) {
            WB_PolyLine geo = ZTransform.ICurveToWB_PolyLine(site[i], scale);
            inputSite[i] = geo;
        }
        ICurve[] water = IG.layer("water").curves();
        this.inputSiteWater = new WB_PolyLine[water.length];
        for (int i = 0; i < water.length; i++) {
            WB_PolyLine geo = ZTransform.ICurveToWB_PolyLine(water[i], scale);
            inputSiteWater[i] = geo;
        }

        // load entries
        IPoint[] entries1 = IG.layer("entry1").points();
        this.inputEntries1 = new WB_Point[entries1.length];
        for (int i = 0; i < entries1.length; i++) {
            inputEntries1[i] = ZTransform.IPointToWB(entries1[i], scale);
        }
        IPoint[] entries2 = IG.layer("entry2").points();
        this.inputEntries2 = new WB_Point[entries2.length];
        for (int i = 0; i < entries2.length; i++) {
            inputEntries2[i] = ZTransform.IPointToWB(entries2[i], scale);
        }

        // load inner nodes
        IPoint[] inners1 = IG.layer("inner1").points();
        this.inputInnerNodes1 = new WB_Point[inners1.length];
        for (int i = 0; i < inners1.length; i++) {
            inputInnerNodes1[i] = ZTransform.IPointToWB(inners1[i], scale);
        }
        IPoint[] inners2 = IG.layer("inner2").points();
        this.inputInnerNodes2 = new WB_Point[inners2.length];
        for (int i = 0; i < inners2.length; i++) {
            inputInnerNodes2[i] = ZTransform.IPointToWB(inners2[i], scale);
        }

        // load boundary polygon
        this.inputBoundaries = new WB_Polygon[2];
        ICurve[] boundaries = IG.layer("boundary1").curves();
        assert boundaries.length == 1;
        WB_Geometry2D geo = ZTransform.ICurveToWB(boundaries[0], scale);
        if (geo instanceof WB_Polygon) {
            inputBoundaries[0] = (WB_Polygon) geo;
        }
        boundaries = IG.layer("boundary2").curves();
        assert boundaries.length == 1;
        geo = ZTransform.ICurveToWB(boundaries[0], scale);
        if (geo instanceof WB_Polygon) {
            inputBoundaries[1] = (WB_Polygon) geo;
        }

        // record area
        assert inputBoundaries.length != 0;
        this.boundaryArea = new double[inputBoundaries.length];
        for (int i = 0; i < inputBoundaries.length; i++) {
            boundaryArea[i] = Math.abs(inputBoundaries[i].getSignedArea());
        }

        System.out.println(getInfo());
        System.out.println("**  LOADING SUCCESS **" + "\n" + "----------------------" + "\n");
    }

    /* ------------- setter & getter ------------- */

    public void setScale(double scale) {
        this.scale = scale;
    }

    public WB_PolyLine[] getInputSite() {
        return inputSite;
    }

//    public WB_PolyLine[] getInputSiteWater() {
//        return inputSiteWater;
//    }

    public WB_Polygon[] getInputBoundaries() {
        return this.inputBoundaries;
    }

    public WB_Point[] getInputEntries1() {
        return this.inputEntries1;
    }

    public WB_Point[] getInputInnerNodes1() {
        return this.inputInnerNodes1;
    }

    public WB_Point[] getInputEntries2() {
        return this.inputEntries2;
    }

    public WB_Point[] getInputInnerNodes2() {
        return this.inputInnerNodes2;
    }

    public double[] getBoundaryArea() {
        return this.boundaryArea;
    }

    public String getInfo() {
        StringBuilder builder = new StringBuilder(
                "\n" + "*** STATISTICS ***"
                        + "\n" + "entries num 1" + " ---> " + inputEntries1.length
                        + "\n" + "inner nodes num 1" + " ---> " + inputInnerNodes1.length
                        + "\n" + "entries num 2" + " ---> " + inputEntries2.length
                        + "\n" + "inner nodes num 2" + " ---> " + inputInnerNodes2.length
        );
        for (int i = 0; i < inputBoundaries.length; i++) {
            String s = "\n" + "boundary" + i + " ---> " + String.format("%.2f", boundaryArea[i]) + " ㎡";
            builder.append(s);
        }
        return builder.toString();
    }

    /*-------- print & draw --------*/

    public void display(WB_Render3D render, PApplet app) {
        app.pushStyle();
        // site
        app.noFill();
        app.strokeWeight(1.0F);
        app.stroke(128);
        for (WB_PolyLine pl : inputSite) {
            render.drawPolyLine(pl);
        }
        app.stroke(100, 149, 237);
        for (WB_PolyLine pl : inputSiteWater) {
            render.drawPolyLine(pl);
        }

        // boundary
//        app.fill(255);
        app.strokeWeight(3);
        app.stroke(0);
        for (WB_Polygon p : inputBoundaries) {
            render.drawPolygonEdges2D(p);
        }

        // nodes
//        app.noStroke();
//        app.fill(128);
//        for (WB_Point p : inputEntries1) {
//            app.ellipse(p.xf(), p.yf(), 10, 10);
//        }
//        for (WB_Point p : inputEntries2) {
//            app.ellipse(p.xf(), p.yf(), 10, 10);
//        }
//        app.fill(255, 0, 0);
//        for (WB_Point p : inputInnerNodes1) {
//            app.ellipse(p.xf(), p.yf(), 10, 10);
//        }
//        for (WB_Point p : inputInnerNodes2) {
//            app.ellipse(p.xf(), p.yf(), 10, 10);
//        }
//        app.popStyle();
    }
}
