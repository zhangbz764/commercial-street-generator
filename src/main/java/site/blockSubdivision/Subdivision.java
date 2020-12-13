package site.blockSubdivision;

import math.ZGeoMath;
import subdivision.ZSD_OBB;
import subdivision.ZSD_SideStrip;
import subdivision.ZSD_SingleStrip;
import subdivision.ZSubdivision;
import wblut.geom.WB_Polygon;
import wblut.processing.WB_Render;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * site block partition
 *
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2020/12/10
 * @time 14:57
 */
public class Subdivision {
    private WB_Polygon[] sites;
    private List<List<WB_Polygon>> divideResults;

    /* ------------- constructor ------------- */

    public Subdivision(WB_Polygon[] sites) {
        this.sites = sites;
        performSubdivision();
    }

    public void performSubdivision() {
        this.divideResults = new ArrayList<>();
        ZSubdivision[] subdivisions = new ZSubdivision[sites.length];

        for (int i = 0; i < sites.length; i++) {
            System.out.println(sites[i].getNormal().toString());
            subdivisions[i] = new ZSD_OBB(ZGeoMath.polygonFaceUp(sites[i]));
            subdivisions[i].performDivide();
            divideResults.add(subdivisions[i].getAllSubPolygons());
        }
    }

    /* ------------- getter & setter ------------- */

    public void setSites(WB_Polygon[] sites) {
        this.sites = sites;
    }

    public List<List<WB_Polygon>> getDivideResults() {
        return divideResults;
    }

    /* ------------- draw ------------- */

    public void display(WB_Render render) {
        for (List<WB_Polygon> list : divideResults) {
            for (WB_Polygon p : list) {
                render.drawPolygonEdges2D(p);
            }
        }

    }
}
