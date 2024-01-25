/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package patternbasedgraphdrawing;

import java.util.ArrayList;

/**
 *
 * @author 20184261
 * Given a matrix, this class generates the set of all rectangles that may be classified as patterns.
 */
public class PatternFinder {
    
    Matrix matrix;
    
    double CLUSTERTHRESHOLD = 0.8;
    double BICLUSTERTHRESHOLD = 0.8;
    double STARTHRESHOLD = 0.8;
    
    public PatternFinder(Matrix matrix){
        this.matrix = matrix;
    }
    
    public ArrayList<PatternRectangle> getPatterns(boolean perm){
        ArrayList<PatternRectangle> patterns = new ArrayList<>();
        
        //Clusters
        for (int i = 0; i < matrix.n-1; i++) {
            int blackCells = 0;
            if(matrix.cell(i+1, i, perm)){
                blackCells++;
            }
            int bestJ = i+2;
            double bestScore = 0;
            for (int j = i+2; j < matrix.n; j++) {
                for (int k = i; k <= j; k++) {
                    if(matrix.cell(j, k, perm)){
                        blackCells++;
                    }
                    if(clusterScore(blackCells,(j-(i-1))) >= CLUSTERTHRESHOLD){
                        bestJ = j;
                        bestScore = clusterScore(blackCells,(j-(i-1)));
                    }
                        
                }
                
            }
            int width = bestJ - (i-1);
            PatternRectangle cluster = new PatternRectangle(i,i,width,width,bestScore);
            cluster.setCluster();
            patterns.add(cluster);
        }
        
        // BiClusters
        
        // Stars
        
        return patterns;
    }
    
    private double clusterScore(int blackCells, int width){
        // cells = 0 + 1 + ... + width-1
        int cells = (width * (width-1))/2;
        double percentage = (double) blackCells / (double) cells;
        return percentage;
    }
    
}
