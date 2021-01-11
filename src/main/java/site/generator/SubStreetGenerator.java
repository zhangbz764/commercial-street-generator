package site.generator;

import formInteractive.blockSplit.Split;
import formInteractive.blockSplit.SplitBisector;
import formInteractive.graphAdjusting.TrafficGraph;
import formInteractive.graphAdjusting.TrafficNode;
import formInteractive.graphAdjusting.TrafficNodeTree;
import processing.core.PApplet;
import wblut.geom.WB_Polygon;

import java.util.ArrayList;
import java.util.List;

/**
 * generate sub traffic space in the commercial street
 *
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2021/1/10
 * @time 22:15
 */
public class SubStreetGenerator {
    private TrafficGraph subGraph;
    private final WB_Polygon subSite;
    private Split subBlockSplit;

    /* ------------- constructor ------------- */

    public SubStreetGenerator(WB_Polygon subSite) {
        this.subSite = subSite;
        init();
    }

    /* ------------- initializer ------------- */

    public void init() {
        List<TrafficNode> treeNodes = new ArrayList<>();
        treeNodes.add(new TrafficNodeTree(subSite.getCenter(), subSite));
        this.subGraph = new TrafficGraph(treeNodes, new ArrayList<TrafficNode>());
        subBlockSplit = new SplitBisector(subSite, subGraph);
    }

    /* ------------- setter & getter ------------- */

    public WB_Polygon getSubTrafficBlock() {
        return subBlockSplit.getPublicBlockPoly();
    }

    public List<WB_Polygon> getSubSiteBlocks() {
        return subBlockSplit.getShopBlockPolys();
    }

    /* ------------- mouse & key interaction ------------- */

    /**
     * update tree node location and graph, split polygon
     *
     * @param pointerX x
     * @param pointerY y
     * @return void
     */
    public void dragUpdate(int pointerX, int pointerY) {
        subGraph.setTreeNode(pointerX, pointerY);
        subGraph.setFixedNode(pointerX, pointerY);

        if (subGraph.update) {
            subBlockSplit.init(subSite, subGraph);
        }
    }

    /**
     * reset fixed node to not active
     *
     * @param
     * @return void
     */
    public void releaseUpdate() {
        subGraph.resetActive();
    }

    public void setGraphSwitch(boolean update) {
        subGraph.update = update;
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
            subGraph.addTreeNode(pointerX, pointerY, subSite);
            subBlockSplit.init(subSite, subGraph);
        }
        // remove a TrafficNode to graph (mouse location)
        if (app.key == 's' || app.key == 'S') {
            subGraph.removeTreeNode(pointerX, pointerY);
            subBlockSplit.init(subSite, subGraph);
        }
        // add a fixed TrafficNode to graph 1
        if (app.key == 'q' || app.key == 'Q') {
            subGraph.addFixedNode(pointerX, pointerY, subSite);
            subBlockSplit.init(subSite, subGraph);
        }
        // remove a fixed TrafficNode to graph (mouse location)
        if (app.key == 'w' || app.key == 'W') {
            subGraph.removeFixedNode(pointerX, pointerY);
            subBlockSplit.init(subSite, subGraph);
        }
        // increase TrafficNode's regionR
        if (app.key == 'z' || app.key == 'Z') {
            subGraph.changeR(pointerX, pointerY, 1);
            subBlockSplit.init(subSite, subGraph);
        }
        // decrease TrafficNode's regionR
        if (app.key == 'x' || app.key == 'X') {
            subGraph.changeR(pointerX, pointerY, -1);
            subBlockSplit.init(subSite, subGraph);
        }
    }

    /* ------------- draw ------------- */
}
