/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package patternbasedgraphdrawing;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.tue.geometrycore.geometry.linear.Rectangle;
import nl.tue.geometrycore.io.ReadItem;
import nl.tue.geometrycore.io.ipe.IPEReader;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class Converter {

    public static void main(String[] args) {

        try (IPEReader read = IPEReader.clipboardReader()) {

            List<ReadItem> items = read.read();

            Rectangle bb = Rectangle.byBoundingBox(items);

            Matrix m = null;
            int n = -1;
            double w = -1;

            for (ReadItem item : items) {
                Rectangle cell = Rectangle.byBoundingBox(item);

                if (m == null) {
                    m = new Matrix();
                    w = cell.width();
                    n = (int) Math.round(bb.width() / w);
                    m.cells = new boolean[n][n];

                    m.n = n;
                    m.cols = new String[n];
                    m.rows = new String[n];
                    for (int i = 0; i < n; i++) {
                        m.cols[i] = m.rows[i] = "" + (i + 1);
                        for (int j = 0; j < n; j++) {
                            m.cells[i][j] = false;
                        }
                    }
                }

                int row = (int) Math.round((cell.getLeft() - bb.getLeft()) / w);
                int col = (int) Math.round((bb.getTop() - cell.getTop()) / w);
                m.cells[row][col] = true;
            }

            for (int i = 0; i < n; i++) {
                System.out.print("\t" + m.cols[i]);
            }
            System.out.println("");

            for (int i = 0; i < n; i++) {
                System.out.print(m.rows[i]);
                for (int j = 0; j < n; j++) {
                    if (m.cells[i][j]) {
                        System.out.print("\t1");
                    } else {
                        System.out.print("\t0");
                    }
                }
                System.out.println("");
            }
        } catch (IOException ex) {
            Logger.getLogger(Data.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
