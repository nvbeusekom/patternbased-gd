/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package patternbasedgraphdrawing;

import javax.swing.JSpinner;
//import matrixrenderer.TSP.CostFunction;
import nl.tue.geometrycore.gui.sidepanel.SideTab;
import nl.tue.geometrycore.gui.sidepanel.TabbedSidePanel;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class SidePanel extends TabbedSidePanel {

    final Data data;

    SidePanel(Data data) {
        this.data = data;

        initIO();
        initSettings();
//        initPermute();
    }

    private void initIO() {
        SideTab tab = addTab("IO");

        tab.addButton("Open Matrices [o]", (e) -> data.openMatrices());
        tab.addButton("Save IPE [s]", (e) -> data.saveIPE());
        tab.addSpace(4);
        tab.addButton("Paste Matrices [v]", (e) -> data.pasteMatrices());
        tab.addButton("Cope IPE [c]", (e) -> data.copyIPE());
        tab.addSpace(4);
        tab.addButton("Print JS object", (e) -> data.printJSO());
        tab.addButton("Print TSV ", (e) -> data.printTSV());
    }

    private void initSettings() {
        SideTab tab = addTab("Settings");
        tab.addLabel("General Settings:");
        tab.addLabel("hspace");
        tab.addDoubleSpinner(data.hspace, 0, Integer.MAX_VALUE, 1, (e, v) -> {
            data.hspace = v;
            data.draw.repaint();
        }); tab.addLabel("vspace");
        tab.addDoubleSpinner(data.vspace, 0, Integer.MAX_VALUE, 1, (e, v) -> {
            data.vspace = v;
            data.draw.repaint();
        });

        tab.addLabel("format");
        tab.addTextField(data.format, (e, v) -> {
            data.format = v;
            data.draw.repaint();
        });
        tab.addLabel("stroke");
        tab.addDoubleSpinner(data.stroke, 0, Double.MAX_VALUE, 0.2, (e, v) -> {
            data.stroke = v;
            data.draw.repaint();
        });
        tab.addCheckbox("Highlight Patterns", data.highlightPatterns, (e, v) -> {
            data.highlightPatterns = v;
            data.draw.repaint();
        });
        tab.addLabel("Highlight opacity");
        tab.addDoubleSpinner(data.highlightOpacity, 0, 1, 0.1, (e, v) -> {
            data.highlightOpacity = v;
            data.draw.repaint();
        });
        
        tab.addSpace(2);
        
        tab.addLabel("Cluster Threshold");
        tab.addDoubleSpinner(data.CLUSTERTHRESHOLD, 0, 1, 0.05, (e, v) -> {
            data.CLUSTERTHRESHOLD = v;
            data.getPatterns();
            data.draw.repaint();
        });
        tab.addLabel("BiCluster Threshold");
        tab.addDoubleSpinner(data.BICLUSTERTHRESHOLD, 0, 1, 0.05, (e, v) -> {
            data.BICLUSTERTHRESHOLD = v;
            data.getPatterns();
            data.draw.repaint();
        });
        tab.addLabel("Star Threshold");
        tab.addDoubleSpinner(data.STARTHRESHOLD, 0, 1, 0.05, (e, v) -> {
            data.STARTHRESHOLD = v;
            data.getPatterns();
            data.draw.repaint();
        });
        
        tab.addSpace(2);
        
        tab.addLabel("Matrix Settings:");
        tab.addCheckbox("boring method", data.boring, (e, v) -> {
            data.boring = v;
            data.draw.repaint();
        });

        tab.addLabel("cellsize");
        tab.addDoubleSpinner(data.cellsize, 0, Double.MAX_VALUE, 1, (e, v) -> {
            data.cellsize = v;
            data.draw.repaint();
        });

        tab.addLabel("labeloffset");
        tab.addDoubleSpinner(data.labeloffset, 0, Double.MAX_VALUE, 1, (e, v) -> {
            data.labeloffset = v;
            data.draw.repaint();
        });

        tab.addLabel("textsize");
        tab.addDoubleSpinner(data.textsize, 0, Double.MAX_VALUE, 1, (e, v) -> {
            data.textsize = v;
            data.draw.repaint();
        });

        tab.addLabel("gray");
        tab.addDoubleSpinner(data.gray, 0, 1, 0.1, (e, v) -> {
            data.gray = v;
            data.draw.repaint();
        });

        tab.addCheckbox("Include diagonal", data.diag, (e, v) -> {
            data.diag = v;
            data.draw.repaint();
        });

        tab.addCheckbox("Show column labels", data.columnlabels, (e, v) -> {
            data.columnlabels = v;
            data.draw.repaint();
        });
        tab.addCheckbox("Show row labels", data.rowlabels, (e, v) -> {
            data.rowlabels = v;
            data.draw.repaint();
        });
        
        tab.addCheckbox("Show morans", data.showmoran, (e, v) -> {
            data.showmoran = v;
            data.draw.repaint();
        });
        tab.addLabel("NL Settings:");
        tab.addLabel("vertex size");
        tab.addDoubleSpinner(data.vertexsize, 0, Double.MAX_VALUE, 1, (e, v) -> {
            data.vertexsize = v;
            data.draw.repaint();
        });
        tab.addLabel("Cluster scaling");
        tab.addDoubleSpinner(data.clusterScaling, 0, Double.MAX_VALUE, 0.1, (e, v) -> {
            data.clusterScaling = v;
            data.draw.repaint();
        });
        tab.addLabel("Cluster Circularity");
        tab.addDoubleSpinner(data.clusterCircularity, 0, Double.MAX_VALUE, 0.1, (e, v) -> {
            data.clusterCircularity = v;
            data.draw.repaint();
        });
        
        tab.addCheckbox("Orthogonal Edges", data.orthogonalEdges, (e, v) -> {
            data.orthogonalEdges = v;
            data.draw.repaint();
        });
        tab.addLabel("corner size");
        tab.addDoubleSpinner(data.cornerSize, 0, Double.MAX_VALUE, 1, (e, v) -> {
            data.cornerSize = v;
            data.draw.repaint();
        });
        tab.addLabel("distance increment");
        tab.addIntegerSpinner(data.distanceIncrement, 0, Integer.MAX_VALUE, 1, (e, v) -> {
            data.distanceIncrement = v;
            data.draw.repaint();
        });
        tab.addLabel("Edge Space Percent");
        tab.addDoubleSpinner(data.edgeSpacePercentage, 0, Double.MAX_VALUE, 0.1, (e, v) -> {
            data.edgeSpacePercentage = v;
            data.draw.repaint();
        });
        tab.addLabel("White Edge Padding");
        tab.addDoubleSpinner(data.edgeCasing, 0, Double.MAX_VALUE, 0.1, (e, v) -> {
            data.edgeCasing = v;
            data.draw.repaint();
        });
        tab.addLabel("Straight Edge Opacity");
        tab.addDoubleSpinner(data.straightEdgeOpacity, 0, Double.MAX_VALUE, 0.1, (e, v) -> {
            data.straightEdgeOpacity = v;
            data.draw.repaint();
            
        });
    }

//    private void initPermute() {
//        SideTab tab = addTab("Permute");
//
//        final TSP tsp = new TSP();
//
//        tab.addLabel("Apply to matrix");
//        JSpinner spinMatrix = tab.addIntegerSpinner(0, 0, Integer.MAX_VALUE, 1, null);
//
//        tab.addLabel("Time limit (s)");
//        JSpinner spinTimeLimit = tab.addIntegerSpinner(120, 10, Integer.MAX_VALUE, 10, null);
//
//        tab.addSpace(4);
//
//        tab.addButton("TSP initialize", (e) -> {
//            tsp.clear();
//            
//            Matrix matrix = data.matrices.get((int) spinMatrix.getValue());
//            double dn = (double) matrix.n;
//            double dm = (double) matrix.m;
//
//            double cB = dn / (2 * (dn - 1) * dm);
//            double cW = dn / (2 * (dn - 1) * (dn * dn - dm));
//
//            CostFunction cost = (i, j) -> {
//                int b = 0;
//                int w = 0;
//                for (int k = 0; k < matrix.n; k++) {
//                    if (matrix.cell(i, k, false) && matrix.cell(j, k, false)) {
//                        b++;
//                    } else if (!matrix.cell(i, k, false) && !matrix.cell(j, k, false)) {
//                        w++;
//                    }
//                }
//                return -(2 * b * cB + 2 * w * cW - 1 / (dn - 1));
//            };
//
//            tsp.initialize(matrix, cost);
//        });
//        tab.addButton("TSP clear", (e) -> {
//            tsp.clear();
//        });
//
//        tab.addSpace(4);
//
//        tab.addButton("TSP run", (e) -> {
//            tsp.matrix.permutation = tsp.permute((int) spinTimeLimit.getValue());
//            tsp.matrix.moran_perm = Double.NaN;
//            tsp.matrix.moran_perm_diag = Double.NaN;
//            data.draw.repaint();
//        });
//    }

}
