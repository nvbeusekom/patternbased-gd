/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package patternbasedgraphdrawing;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import nl.tue.geometrycore.geometry.BaseGeometry;
import nl.tue.geometrycore.geometry.OrientedGeometry;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.curved.BezierCurve;
import nl.tue.geometrycore.geometry.curved.Circle;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.geometry.linear.Rectangle;
import nl.tue.geometrycore.geometry.mix.GeometryCycle;
import nl.tue.geometrycore.geometryrendering.GeometryPanel;
import nl.tue.geometrycore.geometryrendering.styling.Dashing;
import nl.tue.geometrycore.geometryrendering.styling.ExtendedColors;
import nl.tue.geometrycore.geometryrendering.styling.Hashures;
import nl.tue.geometrycore.geometryrendering.styling.SizeMode;
import nl.tue.geometrycore.geometryrendering.styling.TextAnchor;
import nl.tue.geometrycore.io.ipe.IPEWriter;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class DrawPanel extends GeometryPanel {

    final Data data;
    Rectangle boundingBox = Rectangle.byCornerAndSize(new Vector(875.0,53.5),1620.0,93.0);
    ArrayList<BaseGeometry> items = new ArrayList<>();
    
    HashMap<Integer,Vector> vertexLocation;
    
    DrawPanel(Data data) {
        this.data = data;
    }
    @Override
    protected void drawScene() {
        items = new ArrayList<>();
        setSizeMode(SizeMode.WORLD);

        if(data.matrix != null){
            double y = data.matrix.rows.length * data.cellsize + 20;
            drawMatrix(new Vector(20,y));
            drawNodeLink(new Vector(y + 40,y));
        }
        
        Rectangle A4 = IPEWriter.getA4Size();
        boolean ipemode = getRenderer() instanceof IPEWriter;
        if (!ipemode) {
            setStroke(ExtendedColors.lightBlue, 1, Dashing.SOLID);
            draw(A4);
        }
        
        boundingBox = Rectangle.byBoundingBox(items);
        boundingBox.grow(data.hspace, data.hspace, 1.5*data.vspace, 1.5*data.vspace);
        setStroke(Color.white, data.stroke, Dashing.SOLID);boundingBox.center();
        draw(boundingBox);
    }

    private void drawMatrix(Vector lefttop) {
        boolean ipemode = getRenderer() instanceof IPEWriter;
        Matrix m = data.matrix;
        // labels
        setStroke(Color.black, data.stroke, Dashing.SOLID);
        setFill(null, Hashures.SOLID);

        setTextStyle(TextAnchor.BASELINE_CENTER, data.textsize);
        if (data.columnlabels) {
            Vector anchor = lefttop.clone();
            anchor.translate(data.cellsize / 2.0, data.labeloffset);
            for (int c = 0; c < m.cols.length; c++) {
                draw(anchor, m.col(c, data.permute));
                anchor.translate(data.cellsize, 0);
            }
        }
        
        if (data.rowlabels) {
            setTextStyle(TextAnchor.RIGHT, data.textsize);
            Vector anchor = lefttop.clone();
            anchor.translate(-data.labeloffset, -data.cellsize / 2.0);
            for (int r = 0; r < m.rows.length; r++) {
                draw(anchor, m.row(r, data.permute));
                anchor.translate(0, -data.cellsize);
            }
        }

        // black cells
        setStroke(data.boring ? ExtendedColors.fromUnitGray(data.gray) : null, data.stroke, Dashing.SOLID);
        setFill(Color.black, Hashures.SOLID);
        for (int c = 0; c < m.cols.length; c++) {
            for (int r = 0; r < m.rows.length; r++) {

                if (m.cell(c, r, data.permute)) {
                    Rectangle cell = Rectangle.byCorners(lefttop.clone(), Vector.add(lefttop, new Vector(data.cellsize, -data.cellsize)));
                    cell.translate(c * data.cellsize, -r * data.cellsize);
                    draw(cell);
                }
            }
        }

        // white lines
        if (!data.boring) {
            setStroke(Color.white, data.stroke, Dashing.SOLID);
            setFill(null, Hashures.SOLID);
            for (int c = 0; c < m.cols.length; c++) {
                for (int r = 0; r < m.rows.length; r++) {
                    Rectangle cell = Rectangle.byCorners(lefttop.clone(), Vector.add(lefttop, new Vector(data.cellsize, -data.cellsize)));
                    cell.translate(c * data.cellsize, -r * data.cellsize);
                    if (r > 0 && m.cell(c, r, data.permute) && m.cell(c, r - 1, data.permute)) {
                        draw(cell.topSide());
                    }
                    if (c > 0 && m.cell(c, r, data.permute) && m.cell(c - 1, r, data.permute)) {
                        draw(cell.leftSide());
                    }
                }
            }
        }

        // white cells
        setStroke(data.boring ? ExtendedColors.fromUnitGray(data.gray) : Color.black, data.stroke, Dashing.SOLID);
        setFill(Color.white, Hashures.SOLID);
        for (int c = 0; c < m.cols.length; c++) {
            for (int r = 0; r < m.rows.length; r++) {

                if (!m.cell(c, r, data.permute)) {
                    Rectangle cell = Rectangle.byCorners(lefttop.clone(), Vector.add(lefttop, new Vector(data.cellsize, -data.cellsize)));
                    cell.translate(c * data.cellsize, -r * data.cellsize);
                    draw(cell);
                }
            }
        }
        // full frame
        setStroke(data.boring ? ExtendedColors.fromUnitGray(data.gray) : Color.black, data.stroke, Dashing.SOLID);
        setFill(null, Hashures.SOLID);
        Rectangle frame = Rectangle.byCorners(lefttop.clone(), Vector.add(lefttop, new Vector(data.cellsize, -data.cellsize)));;
        frame.scale(m.cols.length, frame.leftTop());
        items.add(frame);
        draw(frame);

        setStroke(Color.black, data.stroke, Dashing.SOLID);
        Vector anchor = frame.bottomSide().getPointAt(0.5);
        anchor.translate(0, -data.labeloffset);
        setTextStyle(TextAnchor.TOP, data.textsize);
        DecimalFormat df = new DecimalFormat(data.format);
        if (data.showmoran) {
            items.add(anchor);
            if(data.write_timestep)
                draw(anchor, ipemode ? "$I(G) = " + df.format(m.moran(data.permute)) + "$" : "I = " + df.format(m.moran(data.permute)));
            else
                draw(anchor, ipemode ? "$I(G,\\rho) = " + df.format(m.moran(data.permute)) + "$" : "I = " + df.format(m.moran(data.permute)));
        }

        if (data.diag) {
            anchor.translate(0, -data.textsize * 1.5);
            draw(anchor, ipemode ? "$D(G,\\rho) = " + df.format(m.moran_diag(data.permute)) + "$" : "D = " + df.format(m.moran_diag(data.permute)));
        }
    }
    
    public int permuteIndex(int index){
        if(data.permute && data.matrix.permutation != null){
            return data.matrix.permutation[index];
        }
        return index;
    }
    
    public void drawNodeLink(Vector lefttop){
        Matrix m = data.matrix;
        vertexLocation = new HashMap<>();
        Vector deltaCell = new Vector(data.cellsize,-data.cellsize);
        
        // Patterns are based on location in the matrix (rectangle), not on index
        for (PatternRectangle rect : data.patterns) {
            switch (rect.pattern){
                case CLUSTER -> {
                    if(true){ // TODO: If no connecting clusters?
                        Vector topLeftCircle = Vector.add(lefttop, Vector.multiply((double)rect.i+0.5,deltaCell));
                        Vector bottomRightCircle = Vector.add(lefttop, Vector.multiply((double)(rect.i+rect.w-1)+0.5,deltaCell));
                        Vector centerCircle = Vector.interpolate(topLeftCircle, bottomRightCircle, 0.5);
                        // Scale to use the incircle of the square instead of excircle
                        topLeftCircle = Vector.interpolate(centerCircle, topLeftCircle, 1/Math.sqrt(2));
                        bottomRightCircle = Vector.interpolate(centerCircle, bottomRightCircle, 1/Math.sqrt(2));
                        
                        Circle c = Circle.byDiametricPoints(topLeftCircle, bottomRightCircle);
                        int sign = 1;
                        
                        // The code below makes a k-gon, but the order of vertices follows the diagonal
                        for (int i = 0; i < rect.w; i++) {
                            Vector loc = topLeftCircle.clone();
                            loc.rotate((double) (sign * (i+1)/2) * 2*Math.PI/(double)rect.w, centerCircle);
                            vertexLocation.put(permuteIndex(rect.i+i), loc);
                            sign *= -1;
                        }
                    }
                    else{
                        // idk yet
                    }
                }
                case BICLUSTER -> {
                    // We assume the bi-cluster is already chosen in such a way that no overlap occurs
                    
                    // Compute height increase
                    Vector jLoc = Vector.add(lefttop, Vector.multiply((double)rect.j+0.5,deltaCell));
                    Vector iLoc = Vector.add(lefttop, Vector.multiply((double)(rect.i)+0.5,deltaCell));
                    
                    double heightIncrease = jLoc.getY() - iLoc.getY();
                    
                    for (int i = 0; i < rect.w; i++) {
                        Vector loc = Vector.add(lefttop, Vector.multiply((double)(rect.i+i)+0.5,deltaCell));
                        loc.translate(0, heightIncrease);
                        vertexLocation.put(permuteIndex(rect.i+i), loc);
                    }

                }
                case STAR -> {
                    // Star goes down (possibly diagonally to the bottom-left)
                }
            }
        }
        
        // Get vertex locations
        for (int i = 0; i < m.cols.length; i++) {
            // Vertex p_i (permuted) is placed at location i,i
            if(!vertexLocation.containsKey(permuteIndex(i))){
                Vector loc = Vector.add(lefttop, Vector.multiply((double)i+0.5,deltaCell));
                vertexLocation.put(permuteIndex(i),loc);
            }
        }

        // Draw edges
        for (int c = 0; c < m.cols.length; c++) {
            for (int r = c+1; r < m.rows.length; r++) {
                
                if (m.cell(c, r, data.permute)) {
                    boolean straight = false;
                    for (PatternRectangle rect : data.patterns) {
                        if(rect.straightEdge(c, r)){
                            straight = true;
                            break;
                        }
                    }
                    
                    Vector locC = vertexLocation.get(permuteIndex(c));
                    Vector locR = vertexLocation.get(permuteIndex(r));
                    Vector control = new Vector(Math.max(locC.getX(), locR.getX()),Math.max(locC.getY(), locR.getY()));
                    BezierCurve bz = straight? new BezierCurve(locC,locR) : new BezierCurve(locC,control,locR);
                    draw(bz);
                }
            }
        }
        // Draw patterns
        
        // Draw nodes
        // TODO: for nodes not drawn in pattern:
        for (int i = 0; i < m.cols.length; i++) {
            // Vertex p_i (permuted) is placed at location i,i
            Vector loc = vertexLocation.get(permuteIndex(i));
            Circle node = new Circle(loc,data.vertexsize/2);
            setFill(ExtendedColors.lightGray, Hashures.SOLID);
            setStroke(Color.black, data.stroke, Dashing.SOLID);
            draw(node);
            this.setTextStyle(TextAnchor.CENTER, data.textsize);
            draw(loc,Integer.toString(permuteIndex(i)));
            
        }
        setFill(null, Hashures.SOLID);
    }
    
    @Override
    public Rectangle getBoundingRectangle() {
        return this.boundingBox;
    }

    @Override
    protected void mousePress(Vector loc, int button, boolean ctrl, boolean shift, boolean alt) {

    }

    @Override
    protected void keyPress(int keycode, boolean ctrl, boolean shift, boolean alt) {
        switch (keycode) {
            case KeyEvent.VK_V:
                data.pasteMatrices();
                break;
            case KeyEvent.VK_C:
                data.copyIPE();
                break;
            case KeyEvent.VK_S:
                data.saveIPE();
                break;
            case KeyEvent.VK_O:
                data.openMatrices();
                break;
        }
    }

}
