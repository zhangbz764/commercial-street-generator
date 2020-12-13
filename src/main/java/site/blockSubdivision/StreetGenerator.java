package site.blockSubdivision;

import formInteractive.blockSplit.Split;
import formInteractive.blockSplit.SplitBisector;
import formInteractive.graphAdjusting.TrafficGraph;
import formInteractive.graphAdjusting.TrafficNode;
import formInteractive.graphAdjusting.TrafficNodeFixed;
import formInteractive.graphAdjusting.TrafficNodeTree;
import processing.core.PApplet;
import render.JtsRender;
import site.InputSite;
import wblut.geom.WB_Point;
import wblut.geom.WB_Polygon;
import wblut.processing.WB_Render3D;

import java.util.ArrayList;
import java.util.List;

/**
 * description
 *
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2020/12/10
 * @time 21:04
 */
public class StreetGenerator {
    private InputSite inputSite;
    private TrafficGraph trafficGraph;
    private Split blockSplit;

    /* ------------- constructor ------------- */

    public StreetGenerator(InputSite inputSite) {
        init(inputSite);
    }

    /* ------------- initializer ------------- */

    public void init(InputSite inputSite) {
        this.inputSite = inputSite;
        List<TrafficNode> innerNodes = new ArrayList<>();
        for (WB_Point p : this.inputSite.getInputInnerNodes()) {
            TrafficNode treeNode = new TrafficNodeTree(p, this.inputSite.getInputBoundaries()[0]);
            treeNode.setRegionR(6);
            innerNodes.add(treeNode);
        }
        List<TrafficNode> entryNodes = new ArrayList<>();
        for (WB_Point p : this.inputSite.getInputEntries()) {
            TrafficNode fixedNode = new TrafficNodeFixed(p, this.inputSite.getInputBoundaries()[0]);
            fixedNode.setRegionR(6);
            entryNodes.add(fixedNode);
        }

        this.trafficGraph = new TrafficGraph(innerNodes, entryNodes);
        this.blockSplit = new SplitBisector(inputSite.getInputBoundaries()[0], trafficGraph);
    }

    /* ------------- getter ------------- */

    public TrafficGraph getTrafficGraph() {
        return trafficGraph;
    }

    public WB_Polygon[] getBlockAsArray() {
        WB_Polygon[] blocks = new WB_Polygon[blockSplit.getShopBlockNum()];
        for (int i = 0; i < blockSplit.getShopBlockNum(); i++) {
            blocks[i] = blockSplit.getShopBlockPolys().get(i);
        }
        return blocks;
    }

    /* ------------- mouse & key interaction at TRAFFIC GRAPH STEP ------------- */

    /**
     * update tree node location and graph, split polygon
     *
     * @param pointerX x
     * @param pointerY y
     * @return void
     */
    public void dragUpdate(int pointerX, int pointerY) {
        trafficGraph.setTreeNode(pointerX, pointerY);
        trafficGraph.setFixedNode(pointerX, pointerY);
        trafficGraph.setAtrium();

        blockSplit.init(inputSite.getInputBoundaries()[0], trafficGraph);
    }

    /**
     * reset fixed node to not active
     *
     * @param
     * @return void
     */
    public void releaseUpdate() {
        trafficGraph.resetActive();
    }

    public void setGraphSwitch(boolean update) {
        this.trafficGraph.update = update;
    }

    /**
     * all keyboard interaction
     *
     * @param pointerX x
     * @param pointerY y
     * @param app      PApplet
     * @return void
     */
    public void keyUpdate(int pointerX, int pointerY, PApplet app) {
        // add a TrafficNode to graph
        if (app.key == 'a' || app.key == 'A') {
            trafficGraph.addTreeNode(pointerX, pointerY, inputSite.getInputBoundaries()[0]);
            blockSplit.init(inputSite.getInputBoundaries()[0], trafficGraph);
        }
        // remove a TrafficNode to graph (mouse location)
        if (app.key == 's' || app.key == 'S') {
            trafficGraph.removeTreeNode(pointerX, pointerY);
            blockSplit.init(inputSite.getInputBoundaries()[0], trafficGraph);
        }
        // add a fixed TrafficNode to graph
        if (app.key == 'q' || app.key == 'Q') {
            trafficGraph.addFixedNode(pointerX, pointerY, inputSite.getInputBoundaries()[0]);
            blockSplit.init(inputSite.getInputBoundaries()[0], trafficGraph);
        }
        // remove a fixed TrafficNode to graph (mouse location)
        if (app.key == 'w' || app.key == 'W') {
            trafficGraph.removeFixedNode(pointerX, pointerY);
            blockSplit.init(inputSite.getInputBoundaries()[0], trafficGraph);
        }
        // increase TrafficNode's regionR
        if (app.key == 'z' || app.key == 'Z') {
            trafficGraph.changeR(pointerX, pointerY, 2);
            blockSplit.init(inputSite.getInputBoundaries()[0], trafficGraph);
        }
        // decrease TrafficNode's regionR
        if (app.key == 'x' || app.key == 'X') {
            trafficGraph.changeR(pointerX, pointerY, -2);
            blockSplit.init(inputSite.getInputBoundaries()[0], trafficGraph);
        }
        // add an atrium to treeNode
        if (app.key == 'e' || app.key == 'E') {
            trafficGraph.addOrRemoveAtrium(pointerX, pointerY);
            blockSplit.init(inputSite.getInputBoundaries()[0], trafficGraph);
        }
        // increase atrium's length along edge
        if (app.key == 'j' || app.key == 'J') {
            trafficGraph.updateSelectedAtriumLength(1);
            blockSplit.init(inputSite.getInputBoundaries()[0], trafficGraph);
        }
        // decrease atrium's length along edge
        if (app.key == 'k' || app.key == 'K') {
            trafficGraph.updateSelectedAtriumLength(-1);
            blockSplit.init(inputSite.getInputBoundaries()[0], trafficGraph);
        }
        // increase atrium's width perpendicular to linked edge
        if (app.key == 'u' || app.key == 'U') {
            trafficGraph.updateSelectedAtriumWidth(1);
            blockSplit.init(inputSite.getInputBoundaries()[0], trafficGraph);
        }
        // decrease atrium's width perpendicular to linked edge
        if (app.key == 'i' || app.key == 'I') {
            trafficGraph.updateSelectedAtriumWidth(-1);
            blockSplit.init(inputSite.getInputBoundaries()[0], trafficGraph);
        }
    }

    /**
     * start editing atrium
     *
     * @param pointerX x
     * @param pointerY y
     * @return void
     */
    public void atriumEdit(int pointerX, int pointerY) {
        trafficGraph.chooseAtrium(pointerX, pointerY);
    }

    /**
     * atrium editing end
     *
     * @param
     * @return void
     */
    public void atriumEditEnd() {
        trafficGraph.clearSelectAtrium();
    }

    /* ------------- draw ------------- */

    public void display(JtsRender jrender, WB_Render3D render, PApplet app) {
        displayInputData(render, app);

        displaySplit(jrender, app);
        displayGraph(render, app);
    }

    private void displayInputData(WB_Render3D render, PApplet app) {
        inputSite.display(render, app);
    }

    private void displayGraph(WB_Render3D render, PApplet app) {
        trafficGraph.display(render, app);
    }

    private void displaySplit(JtsRender jtsRender, PApplet app) {
        blockSplit.display(jtsRender, app);
    }
}
