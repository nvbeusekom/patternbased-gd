/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package patternbasedgraphdrawing;

import java.util.ArrayList;
import nl.tue.geometrycore.geometry.BaseGeometry;
import nl.tue.geometrycore.geometry.linear.Polygon;
import patternbasedgraphdrawing.DrawPanel.Edge;

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
    // Goes from i to i+(h-1) in y-axis
    int i;
    int j;
    int h;
    int w;

    Pattern pattern;
    
    // A polygon for highlighting the pattern in the NL diagram
    BaseGeometry highlight;
    
    // Edge the must cross the diagonal following the pattern
    ArrayList<Edge> followingCrossings;
    
    
    // For debugging etc
    public PatternRectangle(int i, int j, int w, int h, double score) {
        this.i = i;
        this.j = j;
        this.h = h;
        this.w = w;
        this.score = score;
        followingCrossings = new ArrayList<>();
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
        return inRect(r,c) || inRect(c,r);
    }
    
    public boolean inRect(int x, int y){
        return x >= i && x < i+h && y >= j && y < j+w;
    }
    
    public boolean overlaps(PatternRectangle r){
        boolean xOverlap = false;
        if(this.pattern == Pattern.BICLUSTER && r.pattern == Pattern.BICLUSTER){
            xOverlap = !(i >= r.i + r.h || r.i >= i+h);
        }
        if(!(j >= r.j + r.w || r.j >= j+w) || xOverlap){
            return true;
        }
        return false;
    }
    
    public boolean contains(PatternRectangle r){
        return r.i >= i && r.j >= j && r.i + r.h <= i + h && r.j + r.w <= j + w; 
    }
    
    @Override
    public String toString(){
        return pattern.name() + ", score: " + this.score +":\n"
                + i+","+j+"---"+i+","+(j+w-1) + "\n"
                + "  |       |\n"
                + (i+h-1)+","+j+"---"+(i+h-1)+","+(j+w-1);
        
    }
    
    // Hashing 4 integers is hard with 32 bits (I think) so instead I hash array indices in any algorithms that need it.
}
