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
    double score = 0;
    // Top left cell [i][j]
    double i;
    double j;
    double w;
    double h;

    public PatternRectangle(double i, double j, double w, double h, double score) {
        this.i = i;
        this.j = j;
        this.w = w;
        this.h = h;
        this.score = score;
    }
    
    // Takes a permuted matrix, computes score of this rectangle.
    public double computeScore(Matrix m){
        // Do some calculations
        
        return score;
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
