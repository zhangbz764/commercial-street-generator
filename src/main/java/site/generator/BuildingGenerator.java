package site.generator;

import building.SimpleShop;
import processing.core.PApplet;
import wblut.geom.WB_Polygon;
import wblut.processing.WB_Render;

import java.util.ArrayList;
import java.util.List;

/**
 * description
 *
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2020/12/25
 * @time 14:03
 */
public class BuildingGenerator {
    private List<SimpleShop> allSimpleShops;

    /* ------------- constructor ------------- */

    public BuildingGenerator(SubdivisionGenerator subdivisionGenerator) {
        init(subdivisionGenerator);
    }

    /* ------------- member function ------------- */

    public void init(SubdivisionGenerator subdivisionGenerator) {
        setAllSimpleShops(subdivisionGenerator.getSimpleShopSites());
    }

    /* ------------- getter & setter ------------- */

    public void setAllSimpleShops(List<WB_Polygon> allSimpleShopSites) {
        this.allSimpleShops = new ArrayList<>();
        for (WB_Polygon site : allSimpleShopSites) {
            allSimpleShops.add(new SimpleShop(site));
        }
    }

    /* ------------- draw ------------- */

    public void display(WB_Render render, PApplet app) {
        app.pushStyle();
        for (SimpleShop simpleShop : allSimpleShops) {
            simpleShop.display(render, app);
        }
        app.popStyle();
    }
}
