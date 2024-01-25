/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package patternbasedgraphdrawing;

/**
 *
 * @author 20184261
 */
public class PatternRectangle {
    
    public enum Pattern {
        CLUSTER,
        BICLUSTER,
        STAR
    }
    
    double score = 0;
    // Top left cell [i][j]
    // Goes from i to i+(w-1) in x-axis
    int i;
    int j;
    int w;
    int h;

    Pattern pattern;
    
    // For debugging etc
    public PatternRectangle(int i, int j, int w, int h, double score) {
        this.i = i;
        this.j = j;
        this.w = w;
        this.h = h;
        this.score = score;
    }
    
    public void setCluster(){
        this.pattern = Pattern.CLUSTER;
    }
    public void setBiCluster(){
        this.pattern = Pattern.BICLUSTER;
    }
    public void setStar(){
        this.pattern = Pattern.STAR;
    }
    
    // Takes a permuted matrix, computes score of this rectangle.
    public double computeScore(Matrix m){
        // Do some calculations
        
        return score;
    }
    
    // Logic for determining if an edge should be a straight line-segment
    public boolean straightEdge(int c, int r){
        // This line determines if an edge is in the rectanlge, then it should be straight
        return r >= i && r < i+w && c >= j && c < j+h;
    }
    
    public boolean overlaps(PatternRectangle r){
        if(this.j > r.j+r.w-1 || r.j > this.j + this.w-1){
            return false;
        }
        if(this.i > r.i+r.h-1 || r.i > this.i+this.h-1){
            return false;
        }
        return true;
    }
}
