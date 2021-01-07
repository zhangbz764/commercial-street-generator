package main;

import Guo_Cam.CameraController;
import formInteractive.graphAdjusting.TrafficNode;
import formInteractive.graphAdjusting.spacialElements.Atrium;
import formInteractive.graphAdjusting.spacialElements.Escalator;
import processing.core.PApplet;
import render.JtsRender;
import site.Exporter;
import site.Importer;
import site.generator.BuildingGenerator;
import site.generator.StreetGenerator;
import site.generator.SubdivisionGenerator;
import wblut.processing.WB_Render;

/**
 * commercial street
 *
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2020/12/10
 * @time 11:32
 */
public class CommercialStreet extends PApplet {

    private final static double[] stats = new double[]{
            1,  // 全局缩放比例
            7,  // 动线初始宽度
            8,  // 小店铺宽度
            8,  // 中庭宽度
            2.4,  //中庭两侧走道宽度
            50,  // 扶梯服务半径
    };

    public void setStats() {
        TrafficNode.setOriginalRegionR(stats[1] * 0.5);
        Atrium.setCorridorWidth(stats[3]);
        Escalator.setServiceRadius(stats[4]);
    }

    private final static String path = "E:\\AAA_Study\\202010_HuaianUrbanDesign\\codefiles\\20201224site.3dm";
    private final Importer site = new Importer();
    private final Exporter exporter = new Exporter();

    private boolean streetAdjust = true;

    private StreetGenerator streetGenerator;
    private SubdivisionGenerator subdivisionGenerator;
    private BuildingGenerator buildingGenerator;

    private CameraController gcam;
    private WB_Render render;
    private JtsRender jtsRender;

    /* ------------- settings ------------- */

    public void settings() {
        size(1800, 1000, P3D);
    }

    /* ------------- setup ------------- */

    public void setup() {
        gcam = new CameraController(this, 100);
        gcam.top();
        render = new WB_Render(this);
        jtsRender = new JtsRender(this);

        this.site.loadData(path);
        this.streetGenerator = new StreetGenerator(site);
    }

    /* ------------- draw ------------- */

    public void draw() {
        background(255);
        ambientLight(200,200,200);
        directionalLight(150, 150, 150, 1, 1, -1);
        gcam.drawSystem(1000);

        streetGenerator.display(jtsRender, render, this);
        if (subdivisionGenerator != null) {
            subdivisionGenerator.display(render, this);
        }
        if (buildingGenerator != null) {
            buildingGenerator.display(render, this);
        }
    }

    /* ------------- mouse & key interaction ------------- */

    // pointer from screen
    private double[] pointer;

    public void mouseClicked() {
        if (!streetAdjust) {
            if (mouseButton == LEFT) {
                pointer = gcam.getCoordinateFromScreenDouble(mouseX, mouseY, 0);
                subdivisionGenerator.setSelected(
                        (pointer[0] + width * 0.5),
                        (pointer[1] + height * 0.5)
                );
            }
            if (mouseButton == RIGHT) {
                subdivisionGenerator.clearSelected();
            }
        }
    }

    public void mouseDragged() {
        if (streetAdjust) {
            if (mouseButton == RIGHT) {
                pointer = gcam.getCoordinateFromScreenDouble(mouseX, mouseY, 0);
                // drag a node of traffic graph

                streetGenerator.dragUpdate(
                        (int) (pointer[0] + width * 0.5),
                        (int) (pointer[1] + height * 0.5)
                );
                streetGenerator.setGraphSwitch(false);
            }
        }
    }

    public void mouseReleased() {
        streetGenerator.releaseUpdate();
    }

    public void keyPressed() {
        // generator control
        if (key == ',') {
            streetAdjust = !streetAdjust;
            if (!streetAdjust) {
                this.subdivisionGenerator = new SubdivisionGenerator(
                        streetGenerator.getSiteBlocks(),
                        streetGenerator.getTrafficBlocks()
                );
            }
        }

        // display control
        if (key == 'p' || key == 'P') {
            gcam.perspective();
        }
        if (key == 't' || key == 'T') {
            gcam.top();
        }


        if (key == '1') {
            if (subdivisionGenerator != null) {
                subdivisionGenerator.performSideStrip();
                subdivisionGenerator.clearSelected();
            }
        }
        if (key == '2') {
            if (subdivisionGenerator != null) {
                subdivisionGenerator.performOBB();
                subdivisionGenerator.clearSelected();
            }
        }
        if (key == '.') {
            buildingGenerator = new BuildingGenerator(subdivisionGenerator);
        }
        if (streetAdjust) {
            pointer = gcam.getCoordinateFromScreenDouble(mouseX, mouseY, 0);
            streetGenerator.keyUpdate(
                    (int) (pointer[0] + width * 0.5),
                    (int) (pointer[1] + height * 0.5),
                    this
            );
            streetGenerator.setGraphSwitch(false);
        }

        if (key == '-') {

        }
    }
}
