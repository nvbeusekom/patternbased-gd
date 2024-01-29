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
        
        // === Clusters ===
        
        // Consider each starting point, find the largest cluster
        for (int i = 0; i < matrix.n-1; i++) {
            int blackCells = 0;
            if(matrix.cell(i+1, i, perm)){
                blackCells++;
            }
            int bestJ = -1;
            double bestScore = 0;
            for (int j = i+2; j < matrix.n; j++) {
                for (int k = i; k < j; k++) {
                    if(matrix.cell(j, k, perm)){
                        blackCells++;
                    }
                    int width = (j-(i-1));
                    if(blackCellClusterPercentage(blackCells,width) >= CLUSTERTHRESHOLD){
                        bestJ = j;
                        bestScore = clusterScore(blackCells,width);
                    }
                        
                }
                
            }
            int width = bestJ - (i-1);
            if(bestJ > 0){
                PatternRectangle cluster = new PatternRectangle(i,i,width,width,bestScore);
                cluster.setCluster();
                patterns.add(cluster);
            }
        }
        
        // === BiClusters ===
        
        // should be O(n^4)
        // Consider each top-left point, grow bottom or right each time and find largest cluster
        for (int i = 0; i < matrix.n-1; i++) {
            for (int j = 0; j < matrix.n-1; j++) {
                // Consider top-left cell i,j
                // Grow down right, depending on which direction looks better
                int height = 2;
                int width = 2;
                int blackCells = 0;
                for (int k = 0; k < 2; k++) {
                    for (int l = 0; l < 2; l++) {
                        if(matrix.cell(i+k, j+l, perm)){
                            blackCells++;
                        }
                    }
                }
                if(i == 9 && j == 20){
                    System.out.println("Issue");
                }
                int bestWidth = -1;
                int bestHeight = -1;
                double bestScore = 0;
                while(i + height < j && j + width < matrix.n){
                    int rightGain = 0;
                    int bottomGain = 0;
                    // We can grow to the right: compute the gain
                    if(j + width < matrix.n){
                        for (int k = 0; k < height; k++) {
                            if (matrix.cell(i+k, j+width, perm)) {
                                rightGain++;
                            }
                        }
                    }
                    // We can grow to the bottom: compute the gain
                    if(i + height < j){
                        for (int k = 0; k < width; k++) {
                            if (matrix.cell(i+height, j+k, perm)) {
                                bottomGain++;
                            }
                        }
                    }
                    double percentRight = (double)(blackCells + rightGain)/(double)((width+1) * height);
                    double percentBottom = (double)(blackCells + bottomGain)/(double)(width * (height+1));
//                    System.out.println(percentRight);
//                    System.out.println(percentBottom);
                    if(percentRight > percentBottom && rightGain > 0){
                        // Grow to the right
                        blackCells += rightGain;
                        width++;
                        if(percentRight > BICLUSTERTHRESHOLD){
                            bestWidth = width;
                            bestHeight = height;
                            bestScore = width * height + percentRight;
                        }
                        
                    }
                    else if(bottomGain > 0){
                        // Grow down
                        blackCells += bottomGain;
                        height++;
                        if(percentBottom > BICLUSTERTHRESHOLD){
                            bestWidth = width;
                            bestHeight = height;
                            bestScore = width * height + percentBottom;
                        }
                    }
                    // Both 0
                    else{
                        width++;
                        height++;
                    }
                    
                }
                // As of now, take the largest possible biCluster
                if(bestWidth > 0){
                    PatternRectangle cluster = new PatternRectangle(i,j,bestWidth,bestHeight,bestScore);
                    cluster.setBiCluster();
                    patterns.add(cluster);
                }
                
            }
        }
        
        // === Stars ===
        
        int firstI = 0;
        boolean inStar = false;
        for (int i = 0; i < matrix.n; i++) {
            int blackCells = 0;
            
            for (int j = 0; j < matrix.n; j++) {
                if(matrix.cell(i, j, perm)){
                    blackCells++;
                }
            }
            if(blackCells >= (double)matrix.n / STARTHRESHOLD){
                inStar = true;
            }
            else{
                if(inStar){
                    int width = i - firstI;
                    PatternRectangle star = new PatternRectangle(firstI,0,width,matrix.n,width * width);
                    star.setStar();
                    patterns.add(star);
                }
                firstI = i+1;
                inStar = false;
            }
        }
        
        
        return patterns;
    }
    
    private double blackCellClusterPercentage(int blackCells, int width){
        // cells = 0 + 1 + ... + width-1
        int cells = (width * (width-1))/2;
        return (double) blackCells / (double) cells;
    }
    
    private double clusterScore(int blackCells, int width){
        double percentage = blackCellClusterPercentage(blackCells,width);
        
        return width*width + percentage;
    }
    
}
