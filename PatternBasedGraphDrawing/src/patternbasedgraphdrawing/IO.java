/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package patternbasedgraphdrawing;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class IO {
    
    public static List<Matrix> loadMatrices(BufferedReader read) throws IOException {
       List<Matrix> matrices = new ArrayList();

        Matrix curr = null;
        int nextrow = 0;
        String line = read.readLine();
        while (line != null) {
            String[] split = line.split("\t");
            if (split.length > 1) {
                if (curr == null || nextrow >= curr.cols.length) {
                    curr = new Matrix();
                    matrices.add(curr);
                    curr.cols = new String[split.length - 1];
                    for (int c = 1; c < split.length; c++) {
                        curr.cols[c - 1] = split[c];
                    }
                    curr.rows = new String[split.length - 1];
                    curr.cells = new boolean[split.length - 1][split.length - 1];
                    nextrow = 0;
                } else {
                    curr.rows[nextrow] = split[0];
                    for (int c = 1; c < split.length; c++) {
                        curr.cells[c - 1][nextrow] = split[c].trim().equals("") ? false : Integer.parseInt(split[c]) == 1;
                    }
                    nextrow++;
                }
            }
            line = read.readLine();
        }
        
        for (Matrix m : matrices) {
            m.moran(false);
        }
        
        return matrices;
    } 
}
