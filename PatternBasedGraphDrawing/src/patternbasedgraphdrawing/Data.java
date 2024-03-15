 /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package patternbasedgraphdrawing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.curved.BezierCurve;
import nl.tue.geometrycore.geometry.linear.Rectangle;
import nl.tue.geometrycore.gui.GUIUtil;
import nl.tue.geometrycore.io.ipe.IPEWriter;
import nl.tue.geometrycore.io.tables.SummaryStats;
import nl.tue.geometrycore.util.ClipboardUtil;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class Data {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Data data = new Data();
//        MWISSolver mwis = new MWISSolver();
//        mwis.basicTest();

        GUIUtil.makeMainFrame("Pattern based Graph Drawing", data.draw, data.side);
    }
    Matrix matrix = null;

    double cellsize = 9;
    double labeloffset = 3;
    double stroke = 0.2;
    double textsize = 6;
    boolean boring = true;
    double gray = 0.5;
    String format = "0.00";
    boolean diag = false;
    boolean columnlabels = true;
    boolean rowlabels = true;
    boolean showmoran = false;
    double hspace = 20;
    double vspace = 10;
    boolean permute = true;
    boolean horizontal_layout = true;
    boolean write_timestep = true;
    boolean highlightPatterns = true;
    double highlightOpacity = 0.4;
    
    double CLUSTERTHRESHOLD = 0.9;
    double BICLUSTERTHRESHOLD = 0.9;
    double STARTHRESHOLD = 0.9;
    
    double vertexsize = 9;
    
    double edgeSpacePercentage = 0.9;
    
    double cornerSize = 4;
    int distanceIncrement = (int)cornerSize;
    double edgeCasing = 10 * stroke;
    
    double straightEdgeOpacity = 1;
    
    int randSeed = 2345;
    
    boolean orthogonalEdges = true;
    boolean coincidingEdges = true;
    
    DrawPanel draw = new DrawPanel(this);
    SidePanel side = new SidePanel(this);

    JFileChooser choose = new JFileChooser("../Files/");
    
    ArrayList<PatternRectangle> patterns = new ArrayList<>();
    
    void pasteMatrices() {
        try (BufferedReader read = new BufferedReader(new StringReader(ClipboardUtil.getClipboardContents()))) {
            matrix = IO.loadMatrices(read).get(0);
            draw.repaint();
        } catch (IOException ex) {
            Logger.getLogger(Data.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    void openMatrices() {
        int r = choose.showOpenDialog(draw);
        if (r == JFileChooser.APPROVE_OPTION) {
            try (BufferedReader read = new BufferedReader(new FileReader(choose.getSelectedFile()))) {
                matrix = IO.loadMatrices(read).get(0);
                getPatterns();
                draw.repaint();
            } catch (IOException ex) {
                Logger.getLogger(Data.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    void copyIPE() {
        try (IPEWriter write = IPEWriter.clipboardWriter()) {

            writeIPE(write, false);

        } catch (IOException ex) {
            Logger.getLogger(Data.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    void saveIPE() {
        int r = choose.showSaveDialog(draw);
        if (r == JFileChooser.APPROVE_OPTION) {
            try (IPEWriter write = IPEWriter.fileWriter(choose.getSelectedFile())) {
                writeIPE(write, true);
            } catch (IOException ex) {
                Logger.getLogger(Data.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    void printTSV() {
        System.out.println("");
        for (int i = 0; i < matrix.n; i++) {
            System.out.print("\t" + matrix.cols[i]);
        }
        System.out.println("");

        for (int i = 0; i < matrix.n; i++) {
            System.out.print(matrix.rows[i]);
            for (int j = 0; j < matrix.n; j++) {
                if (matrix.cells[i][j]) {
                    System.out.print("\t1");
                } else {
                    System.out.print("\t0");
                }
            }
            System.out.println("");
        }
    }

    void printJSO() {
        System.out.println("");
        System.out.println("matrices = [");
        boolean first = true;

        if (first) {
            first = false;
            System.out.println("  [");
        } else {
            System.out.println(", [");
        }
        for (int i = 0; i < matrix.n; i++) {
            System.out.print("    [");
            boolean firstm = true;
            for (int j = 0; j < matrix.n; j++) {
                if (matrix.cells[i][j]) {
                    System.out.print((firstm ? "" : ",") + "1");
                } else {
                    System.out.print((firstm ? "" : ",") + "0");
                }
                firstm = false;
            }
            System.out.println("]");
        }
        System.out.println("  ]");
        System.out.println("];");
    }

    void getPatterns(){
        PatternFinder pf = new PatternFinder(matrix);
        System.out.println("Finding Patterns");
        ArrayList<PatternRectangle> allPatterns = pf.getPatterns(permute, CLUSTERTHRESHOLD, BICLUSTERTHRESHOLD, STARTHRESHOLD);
        MWISSolver mwis = new MWISSolver(allPatterns,randSeed);
        System.out.println("Running MWIS solver (" + allPatterns.size() + ")");
        patterns = mwis.solve();
        System.out.println("Found " + patterns.size() + " independent patterns");
    }
    
    void writeIPE(IPEWriter write, boolean filemode) throws IOException {
//        write.setView(Rectangle.byCenterAndSize(new Vector(875.0,53.5),1620.0,93.0));
        write.initialize("\\renewcommand\\familydefault{\\sfdefault}");
        Rectangle bbox = draw.getBoundingRectangle();
        bbox.grow(10);
        write.setView(new Rectangle(0,398,0,IPEWriter.getA4Size().getTop()));
        write.setWorldview(bbox);
        write.setTextSerifs(true);
        write.configureTextHandling(true, textsize, false);
        draw.render(write);
    }
}
