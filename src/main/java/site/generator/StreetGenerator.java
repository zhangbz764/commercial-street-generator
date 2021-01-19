package site.generator;

import formInteractive.blockSplit.Split;
import formInteractive.blockSplit.SplitBisector;
import formInteractive.graphAdjusting.TrafficGraph;
import formInteractive.graphAdjusting.TrafficNode;
import formInteractive.graphAdjusting.TrafficNodeFixed;
import formInteractive.graphAdjusting.TrafficNodeTree;
import geometry.ZGeoFactory;
import main.MallConstant;
import math.ZGeoMath;
import processing.core.PApplet;
import render.JtsRender;
import render.ZRender;
import site.Importer;
import wblut.geom.WB_Point;
import wblut.geom.WB_Polygon;
import wblut.geom.WB_Transform3D;
import wblut.geom.WB_Vector;
import wblut.processing.WB_Render3D;

import java.util.ArrayList;
import java.util.List;

/**
 * generator of the main traffic street
 * includes traffic graph and atrium
 *
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2020/12/10
 * @time 21:04
 */
public class StreetGenerator {
    private final Importer inputSite;
    private TrafficGraph[] trafficGraph;
    private TrafficGraph[] subTrafficGraph;

    private Split[] blockSplit;

    private final WB_Transform3D transform3D = new WB_Transform3D();
    private WB_Polygon[] platformBridge;

    // atrium shape
    private List[] atriumShape;

    /* ------------- constructor ------------- */

    public StreetGenerator(Importer inputSite) {
        this.inputSite = inputSite;
        this.transform3D.addTranslate(new WB_Vector(0, 0, MallConstant.STOREY_HEIGHT));
        init(inputSite);
    }

    /* ------------- initializer ------------- */

    public void init(Importer inputSite) {
        List<TrafficNode> innerNodes1 = new ArrayList<>();
        for (WB_Point p : this.inputSite.getInputInnerNodes1()) {
            TrafficNode treeNode = new TrafficNodeTree(p, this.inputSite.getInputBoundaries()[0]);
            treeNode.setRegionR(MallConstant.MAIN_TRAFFIC_WIDTH * 0.5);
            innerNodes1.add(treeNode);
        }
        List<TrafficNode> entryNodes1 = new ArrayList<>();
        for (WB_Point p : this.inputSite.getInputEntries1()) {
            TrafficNode fixedNode = new TrafficNodeFixed(p, this.inputSite.getInputBoundaries()[0]);
            fixedNode.setRegionR(MallConstant.MAIN_TRAFFIC_WIDTH * 0.5);
            entryNodes1.add(fixedNode);
        }

        List<TrafficNode> innerNodes2 = new ArrayList<>();
        for (WB_Point p : this.inputSite.getInputInnerNodes2()) {
            TrafficNode treeNode = new TrafficNodeTree(p, this.inputSite.getInputBoundaries()[1]);
            treeNode.setRegionR(MallConstant.MAIN_TRAFFIC_WIDTH * 0.5);
            innerNodes2.add(treeNode);
        }
        List<TrafficNode> entryNodes2 = new ArrayList<>();
        for (WB_Point p : this.inputSite.getInputEntries2()) {
            TrafficNode fixedNode = new TrafficNodeFixed(p, this.inputSite.getInputBoundaries()[1]);
            fixedNode.setRegionR(MallConstant.MAIN_TRAFFIC_WIDTH * 0.5);
            entryNodes2.add(fixedNode);
        }

        this.trafficGraph = new TrafficGraph[this.inputSite.getInputBoundaries().length];
        trafficGraph[0] = new TrafficGraph(innerNodes1, entryNodes1);
        trafficGraph[1] = new TrafficGraph(innerNodes2, entryNodes2);

        this.blockSplit = new SplitBisector[this.inputSite.getInputBoundaries().length];
        this.atriumShape = new List[blockSplit.length];
        this.platformBridge = new WB_Polygon[blockSplit.length];

        for (int i = 0; i < blockSplit.length; i++) {
            blockSplit[i] = new SplitBisector(this.inputSite.getInputBoundaries()[i], trafficGraph[i]);
            atriumShape[i] = setAtriumShapeInOneSite(trafficGraph[i]);
            platformBridge[i] = setPlatformBridge(blockSplit[i].getPublicBlockPoly(), atriumShape[i]);
        }
    }

    /**
     * description
     *
     * @param trafficGraph
     * @return java.util.List<wblut.geom.WB_Polygon>
     */
    private List<WB_Polygon> setAtriumShapeInOneSite(TrafficGraph trafficGraph) {
        List<WB_Polygon> atriumList = new ArrayList<>();
        for (TrafficNode n : trafficGraph.getTreeNodes()) {
            if (n.hasAtrium()) {
                atriumList.add(n.getAtrium().getPolygon());
            }
        }
        return atriumList;
    }

    /**
     * get platform bridge
     *
     * @param fromSplit    traffic polygon from split
     * @param atriumShapes all atrium shape polygons
     * @return wblut.geom.WB_Polygon
     */
    private WB_Polygon setPlatformBridge(WB_Polygon fromSplit, List<WB_Polygon> atriumShapes) {
        // shell
        WB_Polygon faceUp = ZGeoMath.polygonFaceDown(fromSplit);
        WB_Point[] newShellPoints = new WB_Point[fromSplit.getNumberOfShellPoints()];
        for (int j = 0; j < newShellPoints.length; j++) {
            newShellPoints[j] = transform3D.applyAsPoint(fromSplit.getPoint(j));
        }
        if (atriumShapes.size() > 0) {
            // holes
            WB_Point[][] newHoles = new WB_Point[atriumShapes.size()][];
            for (int i = 0; i < newHoles.length; i++) {
                WB_Polygon faceDown = ZGeoMath.polygonFaceUp(atriumShapes.get(i));
                newHoles[i] = new WB_Point[faceDown.getNumberOfShellPoints()];
                for (int j = 0; j < newHoles[i].length; j++) {
                    newHoles[i][j] = transform3D.applyAsPoint(faceDown.getPoint(j));
                }
            }
            return ZGeoFactory.wbgf.createPolygonWithHoles(newShellPoints, newHoles);
        } else {
            return new WB_Polygon(newShellPoints);
        }
    }

    /* ------------- getter ------------- */

    public TrafficGraph[] getTrafficGraph() {
        return trafficGraph;
    }

    public List<List<WB_Polygon>> getSiteBlocks() {
        List<List<WB_Polygon>> result = new ArrayList<>();
        for (Split split : blockSplit) {
            List<WB_Polygon> sites = new ArrayList<>();
            for (int j = 0; j < split.getShopBlockNum(); j++) {
                sites.add(split.getShopBlockPolys().get(j));
            }
            result.add(sites);
        }
        return result;
    }

    public List<WB_Polygon> getTrafficBlocks() {
        List<WB_Polygon> result = new ArrayList<>();
        for (Split split : blockSplit) {
            result.add(split.getPublicBlockPoly());
        }
        return result;
    }

    public WB_Polygon[] getPlatformBridge() {
        return platformBridge;
    }

    public List[] getAtriumShape() {
        return atriumShape;
    }

    /* ------------- mouse & key interaction at TRAFFIC GRAPH STEP ------------- */

    private void refresh(int i) {
        blockSplit[i].init(inputSite.getInputBoundaries()[i], trafficGraph[i]);
        atriumShape[i] = setAtriumShapeInOneSite(trafficGraph[i]);
        platformBridge[i] = setPlatformBridge(blockSplit[i].getPublicBlockPoly(), atriumShape[i]);
    }

    /**
     * update tree node location and graph, split polygon
     *
     * @param pointerX x
     * @param pointerY y
     * @return void
     */
    public void dragUpdate(int pointerX, int pointerY) {
        for (int i = 0; i < trafficGraph.length; i++) {
            trafficGraph[i].setTreeNode(pointerX, pointerY);
            trafficGraph[i].setFixedNode(pointerX, pointerY);
            trafficGraph[i].setAtrium();

            if (trafficGraph[i].update) {
                refresh(i);
            }
        }
    }

    /**
     * reset fixed node to not active
     *
     * @param
     * @return void
     */
    public void releaseUpdate() {
        for (TrafficGraph graph : trafficGraph) {
            graph.resetActive();
        }
    }

    public void setGraphSwitch(boolean update) {
        for (TrafficGraph graph : trafficGraph) {
            graph.update = update;
        }
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
            for (int i = 0; i < trafficGraph.length; i++) {
                trafficGraph[i].addTreeNode(pointerX, pointerY, inputSite.getInputBoundaries()[i]);
                refresh(i);
            }
        }
        // remove a TrafficNode to graph (mouse location)
        if (app.key == 's' || app.key == 'S') {
            for (int i = 0; i < trafficGraph.length; i++) {
                trafficGraph[i].removeTreeNode(pointerX, pointerY);
                refresh(i);
            }
        }
        // add a fixed TrafficNode to graph 1
        if (app.key == 'q') {
            trafficGraph[0].addFixedNode(pointerX, pointerY, inputSite.getInputBoundaries()[0]);
            refresh(0);
        }
        // add a fixed TrafficNode to graph 2
        if (app.key == 'Q') {
            trafficGraph[1].addFixedNode(pointerX, pointerY, inputSite.getInputBoundaries()[1]);
            refresh(1);
        }
        // remove a fixed TrafficNode to graph (mouse location)
        if (app.key == 'w' || app.key == 'W') {
            for (int i = 0; i < trafficGraph.length; i++) {
                trafficGraph[i].removeFixedNode(pointerX, pointerY);
                refresh(i);
                ;
            }
        }
        // increase TrafficNode's regionR
        if (app.key == 'z' || app.key == 'Z') {
            for (int i = 0; i < trafficGraph.length; i++) {
                trafficGraph[i].changeR(pointerX, pointerY, 1);
                refresh(i);
            }
        }
        // decrease TrafficNode's regionR
        if (app.key == 'x' || app.key == 'X') {
            for (int i = 0; i < trafficGraph.length; i++) {
                trafficGraph[i].changeR(pointerX, pointerY, -1);
                refresh(i);
            }
        }
        // add an atrium to treeNode
        if (app.key == 'e' || app.key == 'E') {
            for (int i = 0; i < trafficGraph.length; i++) {
                trafficGraph[i].addOrRemoveAtrium(pointerX, pointerY);
                refresh(i);
            }
        }
        // increase atrium's length along edge
        if (app.key == 'j' || app.key == 'J') {
            for (int i = 0; i < trafficGraph.length; i++) {
                trafficGraph[i].updateSelectedAtriumLength(1);
                refresh(i);
            }
        }
        // decrease atrium's length along edge
        if (app.key == 'k' || app.key == 'K') {
            for (int i = 0; i < trafficGraph.length; i++) {
                trafficGraph[i].updateSelectedAtriumLength(-1);
                refresh(i);
            }
        }
        // increase atrium's width perpendicular to linked edge
        if (app.key == 'u' || app.key == 'U') {
            for (int i = 0; i < trafficGraph.length; i++) {
                trafficGraph[i].updateSelectedAtriumWidth(1);
                refresh(i);
            }
        }
        // decrease atrium's width perpendicular to linked edge
        if (app.key == 'i' || app.key == 'I') {
            for (int i = 0; i < trafficGraph.length; i++) {
                trafficGraph[i].updateSelectedAtriumWidth(-1);
                refresh(i);
            }
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
        for (TrafficGraph graph : trafficGraph) {
            graph.chooseAtrium(pointerX, pointerY);
        }
    }

    /**
     * atrium editing end
     *
     * @param
     * @return void
     */
    public void atriumEditEnd() {
        for (TrafficGraph graph : trafficGraph) {
            graph.clearSelectAtrium();
        }
    }

    /* ------------- draw ------------- */

    public void display(JtsRender jtsRender, WB_Render3D render, PApplet app) {
        app.pushStyle();
        displayInputData(render, app);
        displaySplit(jtsRender, app);
        displayGraph(render, app);
        app.fill(220, 100f);
        displayPlatformBridge(render, app);
        app.popStyle();
    }

    public void finalDisplay(JtsRender jtsRender, WB_Render3D render, PApplet app) {
        app.pushStyle();
        displayInputData(render, app);
        displaySplit(jtsRender, app);
        app.fill(230);
        displayPlatformBridge(render, app);
        app.popStyle();
    }

    private void displayInputData(WB_Render3D render, PApplet app) {
        inputSite.display(render, app);
    }

    private void displayGraph(WB_Render3D render, PApplet app) {
        for (TrafficGraph graph : trafficGraph) {
            graph.display(render, app);
        }
    }

    private void displaySplit(JtsRender jtsRender, PApplet app) {
        for (Split split : blockSplit) {
            split.display(jtsRender, app);
        }
    }

    private void displayPlatformBridge(WB_Render3D render, PApplet app) {
        for (WB_Polygon p : platformBridge) {
            if (p.getNumberOfHoles() > 0) {
                ZRender.drawWB_PolygonWithHoles(p, app);
            } else {
                render.drawPolygonEdges(p);
            }
        }
    }
}
