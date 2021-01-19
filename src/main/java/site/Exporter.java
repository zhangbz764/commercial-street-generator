package site;

import building.AnchorShop;
import building.BasicBuilding;
import building.SimpleShop;
import igeo.IG;
import site.generator.BuildingGenerator;
import site.generator.StreetGenerator;
import site.generator.SubdivisionGenerator;
import transform.ZTransform;
import wblut.geom.WB_PolyLine;
import wblut.geom.WB_Polygon;

import java.util.List;

/**
 * export geometries to local 3dm file
 *
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2021/1/4
 * @time 16:19
 */
public class Exporter {

    /* ------------- constructor ------------- */

    public Exporter(StreetGenerator streetGenerator,
                    SubdivisionGenerator subdivisionGenerator,
                    BuildingGenerator buildingGenerator
    ) {
        IG.init();
        saveStreetGenerator(streetGenerator);
        saveSubdivisionGenerator(subdivisionGenerator);
        saveBuildingGenerator(buildingGenerator);
    }

    /* ------------- member function ------------- */

    public void save(String path) {
        IG.save(path);
    }

    /**
     * save all geometries in the streetGenerator
     *
     * @param streetGenerator
     * @return void
     */
    private void saveStreetGenerator(StreetGenerator streetGenerator) {
        for (WB_Polygon traffic : streetGenerator.getTrafficBlocks()) {
            ZTransform.WB_PolyLineToICurve(traffic).layer("traffic");
        }
        for (List atriumList : streetGenerator.getAtriumShape()) {
            for (Object atrium : atriumList) {
                ZTransform.WB_PolyLineToICurve((WB_PolyLine) atrium).layer("atrium");
            }
        }
        for (List<WB_Polygon> list : streetGenerator.getSiteBlocks()) {
            for (WB_Polygon site : list) {
                ZTransform.WB_PolyLineToICurve(site).layer("sites");
            }
        }
    }

    /**
     * save all geometries in the subdivisionGenerator
     *
     * @param subdivisionGenerator
     * @return void
     */
    private void saveSubdivisionGenerator(SubdivisionGenerator subdivisionGenerator) {
//        for (WB_Polygon simpleShop : subdivisionGenerator.getSimpleShopSites()) {
//            ZTransform.WB_PolyLineToICurve(simpleShop).layer("simpleShop");
//        }
        for (WB_Polygon subTraffic : subdivisionGenerator.getSubTrafficBlocks()) {
            ZTransform.WB_PolyLineToICurve(subTraffic).layer("subTraffic");
        }
    }

    /**
     * save all geometries in the buildingGenerator
     *
     * @param buildingGenerator
     * @return void
     */
    private void saveBuildingGenerator(BuildingGenerator buildingGenerator) {
        for (SimpleShop simpleShop : buildingGenerator.getAllSimpleShops()) {
            for (BasicBuilding basicBuilding : simpleShop.getBuildings()) {
                for (WB_Polygon surface : basicBuilding.getAllSurfaces()) {
                    if (!basicBuilding.getRoof().contains(surface)) {
                        ZTransform.WB_PolyLineToICurve(surface).layer("buildings");
                    }
                }
            }
        }
        for (SimpleShop simpleShop : buildingGenerator.getAllSimpleShops()) {
            for (BasicBuilding basicBuilding : simpleShop.getBuildings()) {
                for (WB_Polygon roofSurface : basicBuilding.getRoof()) {
                    ZTransform.WB_PolyLineToICurve(roofSurface).layer("roof");
                }
            }
        }

        for (AnchorShop anchorShop : buildingGenerator.getAllAnchorShops()) {
            for (BasicBuilding basicBuilding : anchorShop.getBuildings()) {
                for (WB_Polygon surface : basicBuilding.getAllSurfaces()) {
                    if (!basicBuilding.getRoof().contains(surface)) {
                        ZTransform.WB_PolyLineToICurve(surface).layer("buildings");
                    }
                }
            }
        }
        for (AnchorShop anchorShop : buildingGenerator.getAllAnchorShops()) {
            for (BasicBuilding basicBuilding : anchorShop.getBuildings()) {
                for (WB_Polygon roofSurface : basicBuilding.getRoof()) {
                    ZTransform.WB_PolyLineToICurve(roofSurface).layer("roof");
                }
            }
        }
    }
}
