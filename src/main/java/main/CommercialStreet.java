package main;

import Guo_Cam.CameraController;
import floors.Floor;
import processing.core.PApplet;
import render.JtsRender;
import site.InputSite;
import site.blockSubdivision.StreetGenerator;
import site.blockSubdivision.Subdivision;
import wblut.geom.WB_Point;
import wblut.processing.WB_Render;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * commercial street
 *
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2020/12/10
 * @time 11:32
 */
public class CommercialStreet extends PApplet {

    /* ------------- settings ------------- */

    public void settings() {
        size(1800, 1000, P3D);
    }

    /* ------------- setup ------------- */

    private final static String path = "E:\\AAA_Study\\202010_HuaianUrbanDesign\\20201129\\1129.3dm";
    private final InputSite site = new InputSite();

    private StreetGenerator streetGenerator;
    private Subdivision divider;

    private CameraController gcam;
    private WB_Render render;
    private JtsRender jtsRender;

    public void setup() {
        gcam = new CameraController(this);
        gcam.top();
        render = new WB_Render(this);
        jtsRender = new JtsRender(this);

        site.loadData(path);
        streetGenerator = new StreetGenerator(site);

    }

    /* ------------- draw ------------- */

    public void draw() {
        background(255);
        gcam.drawSystem(1000);

        streetGenerator.display(jtsRender, render, this);
        if (divider != null) {
            divider.display(render);
        }
    }

    /* ------------- mouse & key interaction ------------- */

    // pointer from screen
    private double[] pointer;

    public void mouseClicked() {
//        System.out.println(Arrays.toString(gcam.getCoordinateFromScreenDouble(mouseX, mouseY, 0)));
//        System.out.println(
//                (gcam.getCoordinateFromScreenDouble(mouseX, mouseY, 0)[0] + width * 0.5)
//                        + " "
//                        + (gcam.getCoordinateFromScreenDouble(mouseX, mouseY, 0)[1] + height * 0.5)
//        );
//        System.out.println(
//                (gcam.pick3dDouble(mouseX, mouseY, 0)[0] + width * 0.5)
//                        + " "
//                        + (gcam.pick3dDouble(mouseX, mouseY, 0)[1] + height * 0.5)
//        );
        System.out.println(Arrays.toString(gcam.pick3d(mouseX, mouseY)));
    }

    public void mouseDragged() {
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

    public void mouseReleased() {
        streetGenerator.releaseUpdate();
    }

    public void keyPressed() {
        // display control
        if (key == 'p' || key == 'P') {
            gcam.perspective();
        }
        if (key == 't' || key == 'T') {
            gcam.top();
        }

        if (key == '0') {
            divider = new Subdivision(streetGenerator.getBlockAsArray());
        }

        pointer = gcam.getCoordinateFromScreenDouble(mouseX, mouseY, 0);
        streetGenerator.keyUpdate(
                (int) (pointer[0] + width * 0.5),
                (int) (pointer[1] + height * 0.5),
                this
        );
    }
}
