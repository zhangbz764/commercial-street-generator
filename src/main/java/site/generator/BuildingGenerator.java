package site.generator;

import building.AnchorShop;
import building.SimpleShop;
import processing.core.PApplet;
import wblut.geom.WB_Polygon;
import wblut.processing.WB_Render;

import java.util.ArrayList;
import java.util.List;

/**
 * generate buildings in the commercial street
 *
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2020/12/25
 * @time 14:03
 */
public class BuildingGenerator {
    private List<SimpleShop> allSimpleShops;
    private List<AnchorShop> allAnchorShops;

    /* ------------- constructor ------------- */

    public BuildingGenerator(SubdivisionGenerator subdivisionGenerator) {
        init(subdivisionGenerator);
    }

    /* ------------- member function ------------- */

    public void init(SubdivisionGenerator subdivisionGenerator) {
        setAllSimpleShops(subdivisionGenerator);
        setAllAnchorShops(subdivisionGenerator);
    }

    public void initAllBuildings() {
        for (SimpleShop simpleShop : allSimpleShops) {
            simpleShop.initBuildings();
        }
        for (AnchorShop anchorShop : allAnchorShops) {
            anchorShop.initBuildings();
        }
    }

    /* ------------- getter & setter ------------- */

    public void setAllSimpleShops(SubdivisionGenerator subdivisionGenerator) {
        this.allSimpleShops = new ArrayList<>();
        for (WB_Polygon site : subdivisionGenerator.getSimpleShopSites()) {
            allSimpleShops.add(
                    new SimpleShop(
                            site,
                            subdivisionGenerator.getTrafficBlocks(),
                            subdivisionGenerator.getSubTrafficBlocks()
                    )
            );
        }
    }

    public void setAllAnchorShops(SubdivisionGenerator subdivisionGenerator) {
        this.allAnchorShops = new ArrayList<>();
        for (WB_Polygon site : subdivisionGenerator.getAnchorShopSites()) {
            allAnchorShops.add(
                    new AnchorShop(
                            site,
                            subdivisionGenerator.getTrafficBlocks(),
                            subdivisionGenerator.getSubTrafficBlocks()
                    )
            );
        }
    }

    public List<SimpleShop> getAllSimpleShops() {
        return allSimpleShops;
    }

    public List<AnchorShop> getAllAnchorShops() {
        return allAnchorShops;
    }

    /* ------------- draw ------------- */

    public void display(WB_Render render, PApplet app) {
        app.pushStyle();
        for (SimpleShop simpleShop : allSimpleShops) {
            simpleShop.displaySite(render, app);
            simpleShop.displayBuilding(render, app);
        }
        for (AnchorShop anchorShop : allAnchorShops) {
            anchorShop.displaySite(render, app);
            anchorShop.displayBuilding(render, app);
        }
        app.popStyle();
    }

    public void finalDisplay(WB_Render render, PApplet app){
        app.pushStyle();
        for (SimpleShop simpleShop : allSimpleShops) {
            simpleShop.displayBuilding(render, app);
        }
        for (AnchorShop anchorShop : allAnchorShops) {
            anchorShop.displayBuilding(render, app);
        }
        app.popStyle();
    }
}
