package main;

import Guo_Cam.CameraController;
import building.BasicBuilding;
import geometry.ZPoint;
import processing.core.PApplet;
import wblut.geom.WB_Point;
import wblut.geom.WB_Polygon;
import wblut.processing.WB_Render;

/**
 * description
 *
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2020/12/23
 * @time 17:28
 */
public class TestBuilding extends PApplet {

    /* ------------- settings ------------- */

    public void settings() {
        size(1000, 1000, P3D);
    }

    /* ------------- setup ------------- */

    CameraController gcam;
    WB_Render render;
    BasicBuilding building;
    WB_Polygon site;

    public void setup() {
        gcam = new CameraController(this);
        render = new WB_Render(this);

        site = new WB_Polygon(new WB_Point[]{
                new WB_Point(0, 0),
                new WB_Point(10, 0),
                new WB_Point(10, 12),
                new WB_Point(0, 12),
                new WB_Point(0, 0)
        });

        building = new BasicBuilding(site, new ZPoint(1, 0));
        for (WB_Polygon p : building.getAllSurfaces()) {
            System.out.println(p.isPlanar());
        }
    }

    /* ------------- draw ------------- */

    public void draw() {
        background(255);
        fill(200);

        gcam.begin2d();
        building.display(render, this);

        gcam.begin3d();
        gcam.drawSystem(1000);
        building.display(render, this);
    }

    public void keyPressed() {
        if (key == '1') {
            site.getPoint(0).set(mouseX, mouseY);
            site.getPoint(site.getNumberOfPoints() - 1).set(mouseX, mouseY);
            building = new BasicBuilding(site);
        } else if (key == '2') {
            site.getPoint(1).set(mouseX, mouseY);
            building = new BasicBuilding(site);
        } else if (key == '3') {
            site.getPoint(2).set(mouseX, mouseY);
            building = new BasicBuilding(site);
        } else if (key == '4') {
            site.getPoint(3).set(mouseX, mouseY);
            building = new BasicBuilding(site);
        }
    }

}
