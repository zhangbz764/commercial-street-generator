package site;

import igeo.ICurve;
import igeo.IG;
import igeo.IPoint;
import processing.core.PApplet;
import transform.ZTransform;
import wblut.geom.WB_Geometry2D;
import wblut.geom.WB_Point;
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
public class InputSite {
    private double scale = 1;

    // geometries
    private WB_Polygon[] inputBoundaries;
    private WB_Point[] inputEntries;
    private WB_Point[] inputInnerNodes;

    // statistics
    private double[] boundaryArea;

    /* ------------- constructor ------------- */

    public InputSite(double scale) {
        setScale(scale);
    }

    public InputSite() {

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

        // load entries
        IPoint[] entries = IG.layer("entry").points();
        this.inputEntries = new WB_Point[entries.length];
        for (int i = 0; i < entries.length; i++) {
            inputEntries[i] = ZTransform.IPointToWB(entries[i], scale);
        }

        // load inner nodes
        IPoint[] inners = IG.layer("inner").points();
        this.inputInnerNodes = new WB_Point[inners.length];
        for (int i = 0; i < inners.length; i++) {
            inputInnerNodes[i] = ZTransform.IPointToWB(inners[i], scale);
        }

        // load boundary polygon
        ICurve[] boundaries = IG.layer("boundary").curves();
        this.inputBoundaries = new WB_Polygon[boundaries.length];
        for (int i = 0; i < boundaries.length; i++) {
            WB_Geometry2D geo = ZTransform.ICurveToWB(boundaries[i], scale);
            if (geo instanceof WB_Polygon) {
                inputBoundaries[i] = (WB_Polygon) geo;
            }
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

    public WB_Polygon[] getInputBoundaries() {
        return this.inputBoundaries;
    }

    public WB_Point[] getInputEntries() {
        return this.inputEntries;
    }

    public WB_Point[] getInputInnerNodes() {
        return this.inputInnerNodes;
    }

    public double[] getBoundaryArea() {
        return this.boundaryArea;
    }

    public String getInfo() {
        StringBuilder builder = new StringBuilder(
                "\n" + "*** STATISTICS ***"
                        + "\n" + "entries num" + " ---> " + inputEntries.length
                        + "\n" + "inner nodes num" + " ---> " + inputInnerNodes.length
        );
        for (int i = 0; i < inputBoundaries.length; i++) {
            String s = "\n" + "boundary" + i + " ---> " + boundaryArea[i] + " ㎡";
            builder.append(s);
        }
        return builder.toString();
    }

    /*-------- print & draw --------*/

    public void display(WB_Render3D render, PApplet app) {
        app.pushStyle();
        app.fill(255);
        app.strokeWeight(3);
        app.stroke(0);
        for (WB_Polygon p : inputBoundaries) {
            render.drawPolygonEdges2D(p);
        }
        app.noStroke();
        app.fill(128);
        for (WB_Point p : inputEntries) {
            app.ellipse(p.xf(), p.yf(), 15, 15);
        }
        app.fill(255, 0, 0);
        for (WB_Point p : inputInnerNodes) {
            app.ellipse(p.xf(), p.yf(), 15, 15);
        }
        app.popStyle();
    }
}
