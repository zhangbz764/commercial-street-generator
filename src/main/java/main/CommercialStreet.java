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
        MallConstant.MAIN_TRAFFIC_WIDTH = 12; // 主动线初始宽度
        MallConstant.SUB_TRAFFIC_WIDTH = 7; // 主动线初始宽度
        MallConstant.SIMPLE_SHOP_WIDTH = 12; // 小店铺宽度

        MallConstant.ATRIUM_WIDTH = 9.6; // 中庭宽度
        MallConstant.ATRIUM_CORRIDOR_WIDTH = 2.8; //中庭两侧走道宽度
    }

    private final static String inputPath = "E:\\AAA_Study\\202010_HuaianUrbanDesign\\codefiles\\20201224site.3dm";
    private final static String outputPath = "E:\\AAA_Study\\202010_HuaianUrbanDesign\\codefiles\\outputTest20200110.3dm";

    private final Importer site = new Importer();
    private Exporter exporter;

    private boolean streetAdjust = true;
    private boolean subdivisionAdjust = false;
    private boolean finalDisplay = false;

    private StreetGenerator streetGenerator;
    private SubdivisionGenerator subdivisionGenerator;
    private BuildingGenerator buildingGenerator;

    private CameraController gcam;
    private WB_Render render;
    private JtsRender jtsRender;

    /* ------------- settings ------------- */

    public void settings() {
        size(1920, 1080, P3D);
    }

    /* ------------- setup ------------- */

    public void setup() {
        setStats();
        gcam = new CameraController(this, 100);
        gcam.top();
        render = new WB_Render(this);
        jtsRender = new JtsRender(this);

        this.site.loadData(inputPath);
        this.streetGenerator = new StreetGenerator(site);
    }

    /* ------------- draw ------------- */

    public void draw() {
        background(30);
        ambientLight(150, 150, 150);
        directionalLight(150, 150, 150, 1, 1, -1);
        gcam.drawSystem(1000);

        if (!finalDisplay) {
            streetGenerator.display(jtsRender, render, this);
            if (subdivisionGenerator != null) {
                subdivisionGenerator.display(jtsRender, render, this);
            }
            if (buildingGenerator != null) {
                buildingGenerator.display(render, this);
            }
        } else {
            streetGenerator.finalDisplay(jtsRender, render, this);
            pushMatrix();
            translate(0, 0, 0.01f);
            if (subdivisionGenerator != null) {
                subdivisionGenerator.finalDisplay(jtsRender, render, this);
            }
            popMatrix();
            if (buildingGenerator != null) {
                buildingGenerator.finalDisplay(render, this);
            }
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
                if (subdivisionAdjust) {
                    subdivisionGenerator.setSelected2(
                            (pointer[0] + width * 0.5),
                            (pointer[1] + height * 0.5)
                    );
                }
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
        } else {
            if (mouseButton == RIGHT) {
                pointer = gcam.getCoordinateFromScreenDouble(mouseX, mouseY, 0);
                if (subdivisionGenerator != null) {
                    subdivisionGenerator.updateMouseSubGraph(
                            (int) (pointer[0] + width * 0.5),
                            (int) (pointer[1] + height * 0.5)
                    );
                }
            }
        }
    }

    public void mouseReleased() {
        if (streetAdjust) {
            streetGenerator.releaseUpdate();
        } else {
            if (subdivisionGenerator != null) {
                subdivisionGenerator.updateReleaseSubGraph();
            }
        }
    }

    public void keyPressed() {
        // generator control
        if (key == '0') {
            streetAdjust = !streetAdjust;
            if (!streetAdjust) {
                this.subdivisionGenerator = new SubdivisionGenerator();
                subdivisionGenerator.init(
                        streetGenerator.getSiteBlocks(),
                        streetGenerator.getTrafficBlocks()
                );
            } else {
                this.subdivisionGenerator = new SubdivisionGenerator();
                this.buildingGenerator = null;
            }
        }

        if (streetAdjust) {
            pointer = gcam.getCoordinateFromScreenDouble(mouseX, mouseY, 0);
            streetGenerator.keyUpdate(
                    (int) (pointer[0] + width * 0.5),
                    (int) (pointer[1] + height * 0.5),
                    this
            );
            streetGenerator.setGraphSwitch(false);
        } else {
            if (subdivisionGenerator != null) {
                pointer = gcam.getCoordinateFromScreenDouble(mouseX, mouseY, 0);
                subdivisionGenerator.updateKeySubGraph(
                        (int) (pointer[0] + width * 0.5),
                        (int) (pointer[1] + height * 0.5),
                        this
                );
            }
        }

        // display control
        if (key == 'p' || key == 'P') {
            gcam.perspective();
            finalDisplay = true;
        }
        if (key == 't' || key == 'T') {
            gcam.top();
            finalDisplay = false;
        }

        // subdivision
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
        if (key == '/') {
            if (subdivisionGenerator != null) {
                subdivisionGenerator.addSubGraph();
            }
        }
        if (key == '*') {
            if (subdivisionGenerator != null) {
                subdivisionGenerator.confirmSubGraph();
            }
        }
        if (key == '5') {
            subdivisionAdjust = true;
        }
        if (key == '6') {
            if (subdivisionGenerator != null) {
                subdivisionGenerator.union();
                subdivisionAdjust = false;
            }
        }

        // buildings
        if (key == ',') {
            buildingGenerator = new BuildingGenerator(subdivisionGenerator);
        }
        if (key == '.') {
            buildingGenerator.initAllBuildings();
        }

        // export
        if (key == '-') {
            this.exporter = new Exporter(streetGenerator, subdivisionGenerator, buildingGenerator);
            exporter.save(outputPath);
        }
    }
}
