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
    // Goes from i to i+(h-1) in x-axis
    int i;
    int j;
    int h;
    int w;

    Pattern pattern;
    
    // For debugging etc
    public PatternRectangle(int i, int j, int w, int h, double score) {
        this.i = i;
        this.j = j;
        this.h = h;
        this.w = w;
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
    public boolean straightEdge(int r, int c){
        // This line determines if an edge is in the rectanlge, then it should be straight
        return r >= i && r < i+h && c >= j && c < j+w;
    }
    
    public boolean overlaps(PatternRectangle r){
        boolean yOverlap = false;
        if(this.pattern == Pattern.BICLUSTER && r.pattern == Pattern.BICLUSTER){
            yOverlap = !(j >= r.j + r.w || r.j >= j+w);
        }
        if(!(i >= r.i + r.h || r.i >= i+h) || yOverlap){
            return true;
        }
        return false;
    }
}
