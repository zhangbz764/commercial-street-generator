package main;

import Guo_Cam.CameraController;
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
    public void setStats() {
        MallConstant.MAIN_TRAFFIC_WIDTH = 7; // 主动线初始宽度
        MallConstant.SIMPLE_SHOP_WIDTH = 12; // 小店铺宽度

        MallConstant.ATRIUM_WIDTH = 6; // 中庭宽度
        MallConstant.ATRIUM_CORRIDOR_WIDTH = 2; //中庭两侧走道宽度
    }

    private final static String inputPath = "E:\\AAA_Study\\202010_HuaianUrbanDesign\\codefiles\\20201224site.3dm";
    private final static String outputPath = "E:\\AAA_Study\\202010_HuaianUrbanDesign\\codefiles\\outputTest20200110.3dm";

    private final Importer site = new Importer();
    private Exporter exporter;

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

        this.site.loadData(inputPath);
        this.streetGenerator = new StreetGenerator(site);
    }

    /* ------------- draw ------------- */

    public void draw() {
        background(255);
        ambientLight(200, 200, 200);
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

        if (streetAdjust) {
            pointer = gcam.getCoordinateFromScreenDouble(mouseX, mouseY, 0);
            streetGenerator.keyUpdate(
                    (int) (pointer[0] + width * 0.5),
                    (int) (pointer[1] + height * 0.5),
                    this
            );
            streetGenerator.setGraphSwitch(false);
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
        if (key == '3') {
            if (subdivisionGenerator != null) {
                subdivisionGenerator.performSingleStrip();
                subdivisionGenerator.clearSelected();
            }
        }
        if (key == '4') {
            if (subdivisionGenerator != null) {
                subdivisionGenerator.performDoubleStrip();
                subdivisionGenerator.clearSelected();
            }
        }
        if (key == '`') {
            if (subdivisionGenerator != null) {
                subdivisionGenerator.performDoubleStrip();
                subdivisionGenerator.clearSelected();
            }
        }

        if (key == '.') {
            buildingGenerator = new BuildingGenerator(subdivisionGenerator);
        }

        if (key == '-') {
            this.exporter = new Exporter(streetGenerator, subdivisionGenerator, buildingGenerator);
            exporter.save(outputPath);
        }
    }
}
