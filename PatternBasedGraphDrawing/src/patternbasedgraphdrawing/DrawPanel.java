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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import nl.tue.geometrycore.geometry.BaseGeometry;
import nl.tue.geometrycore.geometry.GeometryType;
import nl.tue.geometrycore.geometry.OrientedGeometry;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.curved.BezierCurve;
import nl.tue.geometrycore.geometry.curved.Circle;
import nl.tue.geometrycore.geometry.curved.CircularArc;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.geometry.linear.Polygon;
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
    
    Color[] lightColors = {
        ExtendedColors.lightBlue,
        ExtendedColors.lightGreen,
        ExtendedColors.lightOrange,
        ExtendedColors.lightPurple,
        ExtendedColors.lightRed,
    };
    
    // Bends should not be affected by casing
    ArrayList<CircularArc> bends = new ArrayList<>();
    
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
//                draw(anchor, Integer.toString(c));
                anchor.translate(data.cellsize, 0);
            }
        }
        
        if (data.rowlabels) {
            setTextStyle(TextAnchor.RIGHT, data.textsize);
            Vector anchor = lefttop.clone();
            anchor.translate(-data.labeloffset, -data.cellsize / 2.0);
            for (int r = 0; r < m.rows.length; r++) {
                draw(anchor, m.row(r, data.permute));
//                draw(anchor, Integer.toString(r));
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
        if(data.highlightPatterns){
            int colorIndex = 0;
            for (PatternRectangle rect : data.patterns) {
                Vector rectTopLeft = lefttop.clone();
                rectTopLeft.translate(rect.j * data.cellsize, -rect.i * data.cellsize);
                Rectangle drawRect = Rectangle.byCornerAndSize(rectTopLeft,rect.w * data.cellsize, -rect.h * data.cellsize);
                setPatternColor(colorIndex);
                colorIndex++;
                draw(drawRect);
                
            }
            setStroke(Color.black, data.stroke, Dashing.SOLID);
            setAlpha(1);
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
        
        // Between pairs of clusters, we wish to create a bipartition: up and down
        // Half of vertices of each cluster rounded down should be in each partition
        HashSet<Integer> upClusterVertices = new HashSet<>();
        HashSet<Integer> downClusterVertices = new HashSet<>();
        
        bends = new ArrayList<>();
        
        determineUpDownVertices(m,data.patterns,upClusterVertices,downClusterVertices);
        
        // Patterns are based on location in the matrix (rectangle), not on index
        
        
        for (PatternRectangle rect : data.patterns) {
            // Reset Following Crossings...
            rect.followingCrossings = new ArrayList<>();
            switch (rect.pattern){
                case CLUSTER -> {
                    if(true){ // TODO: If no connecting clusters?
                        Vector topLeftCircle = Vector.add(lefttop, Vector.multiply((double)rect.i+0.5,deltaCell));
                        Vector bottomRightCircle = Vector.add(lefttop, Vector.multiply((double)(rect.i+rect.h-1)+0.5,deltaCell));
                        Vector centerCircle = Vector.interpolate(topLeftCircle, bottomRightCircle, 0.5);
                        // Scale to use the incircle of the square instead of excircle
                        topLeftCircle = Vector.interpolate(centerCircle, topLeftCircle, 1/Math.sqrt(2));
                        bottomRightCircle = Vector.interpolate(centerCircle, bottomRightCircle, 1/Math.sqrt(2));
                        
                        Circle c = Circle.byDiametricPoints(topLeftCircle, bottomRightCircle);
                         
//                        int sign = 1;
//                        
//                        // The code below makes a k-gon, but the order of vertices follows the diagonal
//                        for (int i = 0; i < rect.h; i++) {
//                            Vector loc = topLeftCircle.clone();
//                            loc.rotate((double) (sign * (i+1)/2) * 2*Math.PI/(double)rect.h, centerCircle);
//                            vertexLocation.put(permuteIndex(rect.i+i), loc);
//                            sign *= -1;
//                        }
                        int upCount = 0;
                        int downCount = 1;
                        for (int i = 0; i < rect.w; i++) {
                            Vector loc = topLeftCircle.clone();
                            if(upClusterVertices.contains(permuteIndex(rect.i+i))){
                                // Rotate clockwise
                                loc.rotate((double) (-1 * upCount) * 2*Math.PI/(double)rect.w, centerCircle);
                                upCount++;
                            }
                            else if(downClusterVertices.contains(permuteIndex(rect.i+i))){
                                // Rotate counter-clockerwise
                                loc.rotate((double) (downCount) * 2*Math.PI/(double)rect.w, centerCircle);
                                downCount++;
                            }
                            vertexLocation.put(permuteIndex(rect.i+i), loc);
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
                    
                    double heightIncrease = iLoc.getY() - jLoc.getY();
                    
                    for (int i = 0; i < rect.w; i++) {
                        Vector loc = Vector.add(lefttop, Vector.multiply((double)(rect.j+i)+0.5,deltaCell));
                        loc.translate(0, heightIncrease);
                        vertexLocation.put(permuteIndex(rect.j+i), loc);
                    }
                    

                }
                case STAR -> {
                    // Star goes down (possibly diagonally to the bottom-left)
                    for (int i = 0; i < rect.h; i++) {
                        Vector loc = Vector.add(lefttop, Vector.multiply((double)(rect.i+i)+0.5,deltaCell));
                        Vector translation = Vector.multiply(m.n/10,new Vector(-data.cellsize,-data.cellsize));
                        vertexLocation.put(permuteIndex(rect.i+i), Vector.add(loc,translation));
                    }
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
        // Now that ALL vertex locations are known: find pattern shapes:
        for (PatternRectangle rect : data.patterns) {
            switch (rect.pattern){
                case CLUSTER -> {
                    // Already handled previously
                    Vector topLeftCircle = Vector.add(lefttop, Vector.multiply((double)rect.i+0.5,deltaCell));
                    Vector bottomRightCircle = Vector.add(lefttop, Vector.multiply((double)(rect.i+rect.h-1)+0.5,deltaCell));
                    Vector centerCircle = Vector.interpolate(topLeftCircle, bottomRightCircle, 0.5);
                    // Scale to use the incircle of the square instead of excircle
                    topLeftCircle = Vector.interpolate(centerCircle, topLeftCircle, 1/Math.sqrt(2));
                    bottomRightCircle = Vector.interpolate(centerCircle, bottomRightCircle, 1/Math.sqrt(2));

                    Circle c = Circle.byDiametricPoints(topLeftCircle, bottomRightCircle);
                    rect.highlight = c;
                }
                case BICLUSTER -> {
                    // We assume the bi-cluster is already chosen in such a way that no overlap occurs

                    Vector a = vertexLocation.get(permuteIndex(rect.i));
                    Vector b = vertexLocation.get(permuteIndex(rect.i+rect.h-1));
                    Vector c = vertexLocation.get(permuteIndex(rect.j));
                    Vector d = vertexLocation.get(permuteIndex(rect.j+rect.w-1));

                    Polygon p = new Polygon();
                    p.addVertex(c);
                    for (int i = rect.i; i < rect.i+rect.h; i++) {
                        p.addVertex(vertexLocation.get(i));
                    }
                    p.addVertex(d);

                    Polygon cHull = convexHull(p);
                    rect.highlight = cHull;
                }
                case STAR -> {
                    Vector a = vertexLocation.get(permuteIndex(rect.j));
                    Vector b = vertexLocation.get(permuteIndex(rect.j+rect.w-1));
                    Vector midAB = Vector.interpolate(a, b, 0.5);
                    Polygon p = new Polygon();
                    p.addVertex(a);
                    p.addVertex(b);
                    for (int i = rect.i; i < rect.i+rect.w; i++) {
                        Vector loc = vertexLocation.get(permuteIndex(i));
                        p.addVertex(loc);
                        if(i < rect.i+rect.w-1){
                            Vector nextLoc = vertexLocation.get(permuteIndex(i+1));
                            Vector midpoint = Vector.add(Vector.add(loc, nextLoc),midAB);
                            p.addVertex(midpoint);
                        }
                    }
                    rect.highlight = p;
                }
            }
        }
        
        // Mapping to extra bend vectors
        HashMap<Integer,Vector> bendPoints = new HashMap<>();
        HashSet<Integer> horizontal = new HashSet<>();
        HashSet<Integer> vertical = new HashSet<>();
        
        // Determine Vector for vertices if necessary
        for (int i = 0; i < data.matrix.cols.length; i++) {
            int vertex = permuteIndex(i);
            if(!upClusterVertices.contains(vertex) && !downClusterVertices.contains(vertex)){
                horizontal.add(i*(int)data.cellsize);
                vertical.add(i*(int)data.cellsize);
            }
            
        }
        
        // Determine Vector for vertices if necessary and if orthogonal edges
        if(data.orthogonalEdges){
            for (int i = 0; i < data.matrix.cols.length; i++) {
                int vertex = permuteIndex(i);
                PatternRectangle pattern;
                for (PatternRectangle check : data.patterns) {
                    if(i >= check.j && i < check.j + check.w-1){
                        pattern = check;
                        if(upClusterVertices.contains(vertex)){
                            for (int dist = i * (int)data.cellsize; dist > (int)data.cellsize*-data.matrix.cols.length ; dist-=data.distanceIncrement) {
                                double y = lefttop.getY() - (dist + 0.5);
                                Circle clusterCircle = (Circle) pattern.highlight;
                                double x = vertexLocation.get(i).getX();
                                Vector bend = new Vector(x,y);
                                LineSegment horizontalLine = new LineSegment(bend.clone(),new Vector(x + clusterCircle.getRadius(),y));
                                if(!horizontal.contains(dist) && horizontalLine.intersect(clusterCircle).isEmpty()){
                                    horizontal.add(dist);
                                    if(dist < i * (int)data.cellsize){
                                        bendPoints.put(vertex, bend);
                                    }
                                    break;
                                }
                            }
                        }
                        if(downClusterVertices.contains(vertex)){
                            for (int dist = i * (int)data.cellsize; dist > (int)data.cellsize*-data.matrix.cols.length ; dist-=data.distanceIncrement) {
                                double x = lefttop.getX() + (dist + 0.5);
                                Circle clusterCircle = (Circle) pattern.highlight;
                                double y = vertexLocation.get(i).getY();
                                Vector bend = new Vector(x,y);
                                LineSegment verticalLine = new LineSegment(bend.clone(),new Vector(x,y-clusterCircle.getRadius()));
                                if(!vertical.contains(dist) && verticalLine.intersect(clusterCircle).isEmpty()){
                                    vertical.add(dist);
                                    if(dist < i * (int)data.cellsize){
                                        bendPoints.put(vertex, bend);
                                    }
                                    break;
                                }
                            }
                        }
                        break;
                    }
                }
                if(!bendPoints.containsKey(vertex)){
                    bendPoints.put(vertex, vertexLocation.get(vertex));
                }

            }
        }
        // Edges
        setStroke(Color.black, data.stroke, Dashing.SOLID);
        setAlpha(1);
        Vector botRight = lefttop.clone();
        botRight.translate(data.cellsize * m.cols.length+1, -data.cellsize * m.rows.length+1);
        LineSegment diagonal = new LineSegment(lefttop,botRight);
//        draw(diagonal);
        
        ArrayList<ArrayList<Integer>> upEdgeLists = new ArrayList<>();
        ArrayList<ArrayList<Integer>> downEdgeLists = new ArrayList<>();
        
        for (int i = 0; i < m.cols.length; i++) {
            upEdgeLists.add(new ArrayList<>());
            downEdgeLists.add(new ArrayList<>());
        }
        
        
        // Not symmetric for edges that cross the diagonal
        int[][] distanceMatrix = new int[m.cols.length][m.rows.length]; 
        
        // Determine Edge Distance and Up/Down, for distance reason, we move away from the diagonal.
        
        HashMap<Integer,ArrayList<Edge>> heightLooseEdges = new HashMap<>();
        
        for (int diagDistance = 1; diagDistance < m.rows.length; diagDistance++) {
            for (int base = 0; base < m.cols.length; base++) {
                int c = base;
                int r = c - diagDistance;
                if(r < 0){
                    continue;
                }
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
                    if(!straight){
                        
                        boolean up = false;
                        boolean down = false;
                        
                        
                        if(upClusterVertices.contains(r) || upClusterVertices.contains(c)){
                            up = true;
                        }
                        if(downClusterVertices.contains(r) || downClusterVertices.contains(c)){
                            down = true;
                        }
                        
                        if(!up && !down){ // Basic edge can go up I guess?
                            up = true;
                        }
                        
                        
                        int distance = 0;
                        if(!(up && down)){
                            // Edge lies upwards from diagonal
                            insertAtSelf(up?upEdgeLists.get(r):downEdgeLists.get(r),c,r);
                            insertAtSelf(up?upEdgeLists.get(c):downEdgeLists.get(c),r,c);
                            if(data.orthogonalEdges){
                                
                            }
                            distance = findFreeDistance(locR, locC, r, c, diagonal, heightLooseEdges, up);
                            distanceMatrix[r][c] = distance;
                            distanceMatrix[c][r] = distance;
                            if(!heightLooseEdges.containsKey(distance)){
                                heightLooseEdges.put(distance, new ArrayList<>());
                            }
                            heightLooseEdges.get(distance).add(new Edge(r,c));
                        }
                        else{
                            // Edge crosses diagonal
                            
                            PatternRectangle precedingPattern = null;
                            for (PatternRectangle pattern : data.patterns) {
                                if(r >= pattern.j && r < pattern.j + pattern.w){
                                    precedingPattern = pattern;
                                    break;
                                }
                            }
                            
                            int indexAfterPattern = precedingPattern.j+precedingPattern.w;
                            Vector dummyLoc = Vector.interpolate(
                                    Vector.add(lefttop, Vector.multiply((double)indexAfterPattern-1+0.5,deltaCell)),
                                    Vector.add(lefttop, Vector.multiply((double)indexAfterPattern+0.5,deltaCell)),
                                    0.5); 
                            
                            int crossedDistance = 0;
                            
                            boolean firstUp = upClusterVertices.contains(r);
                            
                            insertAtSelf(firstUp?upEdgeLists.get(r):downEdgeLists.get(r),c,r);
                            insertAtSelf(!firstUp?upEdgeLists.get(c):downEdgeLists.get(c),r,c);
                            distance = findFreeDistance(locR, dummyLoc, r, indexAfterPattern-1, diagonal, heightLooseEdges, firstUp);
                            crossedDistance = findFreeDistance(dummyLoc, locC, indexAfterPattern, c, diagonal, heightLooseEdges, !firstUp);
                            
                            
                            distanceMatrix[r][c] = distance;
                            distanceMatrix[c][r] = crossedDistance;
                            
                            precedingPattern.followingCrossings.add(new Edge(r,c));
                            
                            if(!heightLooseEdges.containsKey(distance)){
                                heightLooseEdges.put(distance, new ArrayList<>());
                            }
                            heightLooseEdges.get(distance).add(new Edge(r,indexAfterPattern-1));
                            
                            if(!heightLooseEdges.containsKey(crossedDistance)){
                                heightLooseEdges.put(crossedDistance, new ArrayList<>());
                            }
                            heightLooseEdges.get(crossedDistance).add(new Edge(indexAfterPattern,c));
                        }
                        
                    }
                }
            }
        }
        
        
        Vector generalLeftExtreme = new Vector(-1,1);
        generalLeftExtreme.normalize();
        generalLeftExtreme.scale(data.vertexsize * data.edgeSpacePercentage * 0.5);
        Vector generalRightExtreme = new Vector(1,-1);
        generalRightExtreme.normalize();
        generalRightExtreme.scale(data.vertexsize * data.edgeSpacePercentage * 0.5);
        
        for (int diagDistance = 1; diagDistance < m.rows.length; diagDistance++) {
            for (int base = 0; base < m.cols.length; base++) {
                int c = base;
                int r = c - diagDistance;
                if(r < 0){
                    continue;
                }
                if (m.cell(c, r, data.permute)) {
                    boolean straight = false;
                    
                    PatternRectangle rPattern = null;
                    
                    for (PatternRectangle rect : data.patterns) {
                        if(rect.straightEdge(c, r)){
                            straight = true;
                            break;
                        }
                        if(r >= rect.j && r < rect.j + rect.w){
                            rPattern = rect;
                        }
                    }
                    if(straight){
                        continue;
                    }
                    int distance = distanceMatrix[r][c];
                    
                    ArrayList<Integer> edgeListC = null;
                    ArrayList<Integer> edgeListR = null;
                    
                    if(distance > 0){
                        edgeListC = upEdgeLists.get(c);
                        edgeListR = upEdgeLists.get(r);
                    }
                    else{
                        edgeListC = downEdgeLists.get(c);
                        edgeListR = downEdgeLists.get(r);
                    }
                    
                    Vector locC = vertexLocation.get(permuteIndex(c));
                    Vector locR = vertexLocation.get(permuteIndex(r));
                    
                    
                    Vector c1 = locC.clone();
                    Vector c2 = locC.clone();
                    c1.translate(generalLeftExtreme.clone());
                    c2.translate(generalRightExtreme.clone());
                    
                    
                    Vector cEndPoint = Vector.interpolate(c1, c2, ((double)edgeListC.indexOf(r)+1)/((double)edgeListC.size() + 1));
                    
                    Vector r1 = locR.clone();
                    Vector r2 = locR.clone();
                    r1.translate(generalLeftExtreme.clone());
                    r2.translate(generalRightExtreme.clone());
                    
                    Vector rEndPoint = Vector.interpolate(r1, r2, ((double)edgeListR.indexOf(c)+1)/((double)edgeListR.size() + 1));
                    
                    if(distanceMatrix[r][c] != distanceMatrix[c][r]){
                        // We are drawing a crossing edge
                        int indexAfterPattern = rPattern.j+rPattern.w;
                        
                        // We know the precedingPattern is a Cluster
                        
                        Circle clusterCircle = (Circle) rPattern.highlight;
                        
                        Vector bottomRightAddition = new Vector(1,-1);
                        bottomRightAddition.normalize();
                        bottomRightAddition = Vector.multiply(clusterCircle.getRadius() + 0.5 * data.cellsize, bottomRightAddition);
                        
                        Vector dummyA = Vector.add(clusterCircle.getCenter(), bottomRightAddition);
                        Vector dummyB = Vector.add(lefttop, Vector.multiply((double)indexAfterPattern+0.5-0.5,deltaCell));
                        
                        
                        Edge toPlace = new Edge(r,c);
                        ArrayList<Edge> crossings = rPattern.followingCrossings;
                               
                        Vector connector = Vector.interpolate(dummyA, dummyB, ((double)crossings.indexOf(toPlace)+1)/((double)crossings.size() + 1));
                        
                        if(data.coincidingEdges){
                            rEndPoint = vertexLocation.get(r);
                            cEndPoint = vertexLocation.get(c);
                        }
                        if(data.orthogonalEdges){
                            // Determine crossing point between i and i+1
                            for (int i = r; i < c; i++) {
                                
                            }
//                            drawOrthogonalEdge(diagonal,rEndPoint,connector,bendPoints.get(permuteIndex(r)),connector,distanceMatrix[r][c] > 0);
//                            drawOrthogonalEdge(diagonal,connector,cEndPoint,connector,bendPoints.get(permuteIndex(c)) ,distanceMatrix[c][r] > 0);
                        }
                        else{
                            drawParallelEdge(diagonal,rEndPoint,connector,distanceMatrix[r][c]);
                            drawParallelEdge(diagonal,connector,cEndPoint,distanceMatrix[c][r]);
                        }
                        
                    }else{
                        if(data.coincidingEdges){
                            rEndPoint = vertexLocation.get(r);
                            cEndPoint = vertexLocation.get(c);
                        }
                        if(diagDistance == 1 && !upClusterVertices.contains(r) && !downClusterVertices.contains(r) && !upClusterVertices.contains(c) && !downClusterVertices.contains(c)){
                            draw(new LineSegment(locR,locC));
                        }
                        else if(data.orthogonalEdges){
                            drawOrthogonalEdge(diagonal, rEndPoint, cEndPoint, bendPoints.get(permuteIndex(r)), bendPoints.get(permuteIndex(c)), distance > 0);
                        }
                        else{
                            drawParallelEdge(diagonal,rEndPoint,cEndPoint,distance);
                        }
                    }
                    
                    
                }
            }
        }
        
        // Do crossing edges
        for (PatternRectangle pattern : data.patterns) {
            for (Edge crossing : pattern.followingCrossings) {
                int r = crossing.r;
                int c = crossing.c;
                // Find suitable crossing location between i and i+1
                
                for (int i = r; i < c; i++) {
                    // Count crossings
                    Vector iLoc = vertexLocation.get(i);
                    Vector nextLoc = vertexLocation.get(i+1);
                    Vector up = new Vector(Math.max(iLoc.getX(), nextLoc.getX()),Math.max(iLoc.getY(), nextLoc.getY()));
                    Vector down = new Vector(Math.min(iLoc.getX(), nextLoc.getX()),Math.min(iLoc.getY(), nextLoc.getY()));
                    
                }
            }
        }
        
        
        // Draw bends
        setStroke(Color.black, data.stroke, Dashing.SOLID);
        setAlpha(1);
        for (CircularArc arc : bends) {
            draw(arc);
        }
        
        // Draw patterns
        if(data.highlightPatterns){
            int colorIndex = 0;
            for (PatternRectangle rect : data.patterns) {
                setPatternColor(colorIndex);
                colorIndex++;
                draw(rect.highlight);
            }
        }
        
        setStroke(Color.black, data.stroke, Dashing.SOLID);
        setAlpha(1);
        
        // Draw straight edges
        setAlpha(data.straightEdgeOpacity);
        for (int i = 0; i < data.matrix.rows.length; i++) {
            for (int j = 0; j < data.matrix.cols.length; j++) {
                if (m.cell(i, j, data.permute)) {
                    for (PatternRectangle rect : data.patterns) {
                        if(rect.straightEdge(i, j)){
                            draw(new LineSegment(vertexLocation.get(permuteIndex(i)),vertexLocation.get(permuteIndex(j))));
                            break;
                        }
                    }
                }
            }
        }
        setAlpha(1);
        
        // Draw nodes
        for (int i = 0; i < m.cols.length; i++) {
            // Vertex p_i (permuted) is placed at location i,i
            Vector loc = vertexLocation.get(permuteIndex(i));
            Circle node = new Circle(loc,data.vertexsize/2);
            setFill(ExtendedColors.lightGray, Hashures.SOLID);
            setStroke(Color.black, data.stroke, Dashing.SOLID);
            draw(node);
            this.setTextStyle(TextAnchor.CENTER, data.textsize);
            draw(loc,m.cols[i]);
        }
        setFill(null, Hashures.SOLID);
    }
    
    private int findFreeDistance(Vector locR, Vector locC, int r, int c, LineSegment diagonal, HashMap<Integer,ArrayList<Edge>> heightLooseEdges, boolean up){
        
        boolean crossing = true;
        int distance = 0;
        
        while(crossing){
            crossing = false;
            if(up){
                distance += data.distanceIncrement;
            }
            else{
                distance -= data.distanceIncrement;
            }

            LineSegment aligned = alignedEdge(diagonal,locR,locC,distance);

            for(PatternRectangle patternRect : data.patterns){
                if(!crossing){
                    BaseGeometry geom = patternRect.highlight;
                    if(!aligned.intersect(geom).isEmpty()){
                        crossing = true;
                    }
                    else{
                        for (int vertex = patternRect.j; vertex < patternRect.j+patternRect.w; vertex++) {
                            Vector center = vertexLocation.get(permuteIndex(vertex));
                            Circle vertexDrawing = new Circle(center,data.vertexsize);
                            if(!aligned.intersect(vertexDrawing).isEmpty()){
                                crossing = true;
                                break;
                            }
                        }
                    }
                }
            }
            if(heightLooseEdges.containsKey(distance) && !crossing){
                ArrayList<Edge> edgesToCheck = heightLooseEdges.get(distance);
                for (Edge e : edgesToCheck) {
                    if((e.c < c && e.c > r)||(e.r < c && e.r > r) || (c < e.c && c > e.r)||(r < e.c && r > e.r)){
                        crossing = true;

                    }
                }
            }

        }
        return distance;
    }
    
    private void determineUpDownVertices(Matrix m, ArrayList<PatternRectangle> patterns, HashSet<Integer> up, HashSet<Integer> down){
        Random random = new Random(data.randSeed);
        // ArrayList with clusters such that larger clusters occur more often (for uniform random vertex choosing)
        ArrayList<PatternRectangle> multipliedClusters = new ArrayList<>();
        ArrayList<Integer> inCluster = new ArrayList<>();
        ArrayList<Integer> inBiCluster = new ArrayList<>();
        ArrayList<Integer> inStar = new ArrayList<>();
        for (PatternRectangle rect : patterns) {
            switch(rect.pattern){
                case CLUSTER -> {
                    for (int i = rect.i; i < rect.i+rect.h; i++) {
                        inCluster.add(permuteIndex(i));
                    }
                }
                case BICLUSTER -> {
                    for (int i = rect.i; i < rect.i+rect.h; i++) {
                        inBiCluster.add(permuteIndex(i));
                    }
                }
                case STAR -> {
                    for (int i = rect.i; i < rect.i+rect.h; i++) {
                        inStar.add(permuteIndex(i));
                    }
                }
            }
        }
        for (PatternRectangle rect : patterns) {
            switch(rect.pattern){
                case CLUSTER ->{
                    multipliedClusters.add(rect);
                    int start = 1;
                    int upCount = 0;
                    int downCount = 0;
                    for (int i = rect.i; i < rect.i + rect.h; i++) {
                        if(inBiCluster.contains(permuteIndex(i))){
                            up.add(permuteIndex(i));
                            upCount++;
                        }
                        if(inStar.contains(permuteIndex(i))){
                            down.add(permuteIndex(i));
                            downCount++;
                        }
                    }
                    for (int i = rect.j; i < rect.j + rect.w; i++) {
                        multipliedClusters.add(rect);
                        if(!inBiCluster.contains(permuteIndex(i)) && !inStar.contains(permuteIndex(i))){
                            if(upCount <= downCount){
                                up.add(permuteIndex(i));
                                upCount++;
                            }
                            else{
                                down.add(permuteIndex(i));
                                downCount++;
                            }
                        }
                    }
                }
                default ->{}
            }
        }
        // Greedily do swaps to improve
        // BiCluster edges should be UP
        // Star edges should be DOWN
        // Loose edges should match the other vertex if it is in a cluster

        // Since we need to check all edges connected to a vertex, each swap takes O(n)
        // Still... we can do like O(n^3) swaps
        
        //First: swap bicluster edges up:
        
        
        int iter = 0;
        int maxIter = (int) Math.pow(m.cols.length,3);
        while(iter < maxIter){
            iter++;
            // Do a possible swap in a random cluster.
            
        }
        
    }
    
    public void insertAtSelf(ArrayList<Integer> edgeList, int insert, int self){
        for (int i = 0; i <= edgeList.size(); i++) {
            if(i == edgeList.size()){
                edgeList.add(insert);
                return;
            }
            else if(edgeList.get(i) > self){
                edgeList.add(i, insert);
                return;
            }
        }
    }
    
    // From: https://www.geeksforgeeks.org/convex-hull-using-jarvis-algorithm-or-wrapping/
    public static int orientation(Vector p, Vector q, Vector r) 
    { 
        double val = (q.getY() - p.getY()) * (r.getX() - q.getX()) - 
                  (q.getX() - p.getX()) * (r.getY() - q.getY()); 
       
        if (val == 0) return 0;  // collinear 
        return (val > 0)? 1: 2; // clock or counterclock wise 
    } 
    private Polygon convexHull(Polygon polygon){
        int n = polygon.vertexCount();
        if(n <= 3){
            return polygon;
        }
        Polygon hull = new Polygon();
        List<Vector> points = polygon.vertices();
        int l = 0; 
        for (int i = 1; i < n; i++) 
            if (points.get(i).getX() < points.get(l).getX()) 
                l = i; 
       
        // Start from leftmost point, keep moving  
        // counterclockwise until reach the start point 
        // again. This loop runs O(h) times where h is 
        // number of points in result or output. 
        int p = l, q; 
        do
        { 
            // Add current point to result 
            hull.addVertex(points.get(p)); 
       
            // Search for a point 'q' such that  
            // orientation(p, q, x) is counterclockwise  
            // for all points 'x'. The idea is to keep  
            // track of last visited most counterclock- 
            // wise point in q. If any point 'i' is more  
            // counterclock-wise than q, then update q. 
            q = (p + 1) % polygon.vertexCount(); 
              
            for (int i = 0; i < n; i++) 
            { 
               // If i is more counterclockwise than  
               // current q, then update q 
               if (orientation(points.get(p), points.get(i), points.get(q)) 
                                                   == 2) 
                   q = i; 
            } 
       
            // Now q is the most counterclockwise with 
            // respect to p. Set p as q for next iteration,  
            // so that q is added to result 'hull' 
            p = q; 
       
        } while (p != l);
        return hull;
    }
    
    public LineSegment alignedEdge(LineSegment diagonal, Vector start, Vector end, double height){
        // We are looking for the points projected onto the diagonal.
        // Imagine a triangle between the start of the diagonal, start, and start projected onto the diagonal.
        double alphaStart = diagonal.getDirection().computeCounterClockwiseAngleTo(Vector.subtract(start, diagonal.getStart()));
        double baseLengthStart = Math.cos(alphaStart) * diagonal.getStart().distanceTo(start);
        
        Vector projectionStart = Vector.interpolate(diagonal.getStart(), diagonal.getEnd(), baseLengthStart/diagonal.length());
        
        
        
        // Same for end
        double alphaEnd = diagonal.getDirection().computeCounterClockwiseAngleTo(Vector.subtract(end, diagonal.getStart()));
        double baseLengthEnd = Math.cos(alphaEnd) * diagonal.getStart().distanceTo(end);
        
        Vector projectionEnd = Vector.interpolate(diagonal.getStart(), diagonal.getEnd(), baseLengthEnd/diagonal.length());
        
        Vector start2 = new Vector(1,1);
        start2.normalize();
        start2.scale(height);
        start2.translate(projectionStart);
        
        
        Vector end2 = new Vector(1,1);
        end2.normalize();
        end2.scale(height);
        end2.translate(projectionEnd);
        return new LineSegment(start2.clone(),end2.clone());
    }
    
    public void drawOrthogonalDiagonalEdge(LineSegment diagonal, Vector start, Vector end){
        // Knowing the end points it should connect to, we can 
    }
    
    // heightStart and heightEnd indicate the cell that the first and last edge run through
    
    /*
    sB -----
    |       |
    s       |
         e-eB
    */
    public void drawOrthogonalEdge(LineSegment diagonal, Vector start, Vector end, Vector startBend, Vector endBend, boolean upwards){
        setStroke(Color.red,data.stroke,Dashing.SOLID);
        boolean extraStart = !start.isApproximately(startBend);
        boolean extraEnd = !end.isApproximately(endBend);
        ArrayList<Vector> points = new ArrayList<>();
        
        points.add(start);
        if(extraStart){
            points.add(startBend);
        }
        if(upwards){
            points.add(new Vector(Math.max(startBend.getX(), endBend.getX()),Math.max(startBend.getY(), endBend.getY())));
        }
        else{
            points.add(new Vector(Math.min(startBend.getX(), endBend.getX()),Math.min(startBend.getY(), endBend.getY())));
        }
        if(extraEnd){
            points.add(endBend);
        }
        points.add(end);
        ArrayList<BaseGeometry> edge = getEdge(points,upwards);
        setStroke(Color.WHITE,data.edgeCasing,Dashing.SOLID);
        draw(edge);
        setStroke(Color.BLACK,data.stroke,Dashing.SOLID);
        draw(edge);
    }
    
    
    public ArrayList<BaseGeometry> getEdge(ArrayList<Vector> points, boolean upwards){
        ArrayList<BaseGeometry> res = new ArrayList<>();
        
        for (int i = 1; i < points.size(); i++) {
            Vector prev = points.get(i-1);
            Vector current = points.get(i);
            Vector a = Vector.interpolate(prev.clone(), current.clone(), data.cornerSize/prev.distanceTo(current));
            Vector b = Vector.interpolate(current.clone(),prev.clone(), data.cornerSize/prev.distanceTo(current));
            res.add(new LineSegment(a,b));
            if(i < points.size()-1){
                // We add a bend for the next one
                Vector next = points.get(i+1);
                Vector c = Vector.interpolate(current.clone(),next.clone(), data.cornerSize/next.distanceTo(current));
                Vector center = Vector.subtract(next,current);
                center.normalize();
                center.scale(data.cornerSize);
                center.translate(b);
                CircularArc arc = new CircularArc(center,b,c,!upwards);
                bends.add(arc);
//                res.add(arc);
            }
        }
        
        return res;
    }
    
    public void drawParallelEdge(LineSegment diagonal, Vector start, Vector end, double height){
        
        LineSegment aligned = alignedEdge(diagonal, start, end, height);
        
        Vector start2 = aligned.getStart();
        Vector end2 = aligned.getEnd();
        
        
        Vector startCurve1 = Vector.interpolate(start2.clone(), start.clone(), data.cornerSize/start.distanceTo(start2));
        Vector startCurve2 = Vector.interpolate(start2.clone(), end2.clone(), data.cornerSize/end2.distanceTo(start2));
        
        Vector endCurve1 = Vector.interpolate(end2.clone(), start2.clone(), data.cornerSize/start2.distanceTo(end2));
        Vector endCurve2 = Vector.interpolate(end2.clone(), end.clone(), data.cornerSize/end.distanceTo(end2));
        
        Vector cornerTranslation = height > 0 ? new Vector(-1,-1) : new Vector(1,1);
        cornerTranslation.normalize();
        cornerTranslation.scale(data.cornerSize);
        
        Vector centerStart = cornerTranslation.clone();
        centerStart.translate(startCurve2);
        
        Vector centerEnd = cornerTranslation.clone();
        centerEnd.translate(endCurve1);
        
        CircularArc arc1 = new CircularArc(centerStart,startCurve1,startCurve2,height<0);
        CircularArc arc2 = new CircularArc(centerEnd,endCurve1,endCurve2,height<0);
        
        setStroke(Color.WHITE,data.edgeCasing,Dashing.SOLID);
        
        Vector paddinglessStart = Vector.interpolate(start, startCurve1, data.edgeCasing/start.distanceTo(startCurve1));
        Vector paddinglessEnd = Vector.interpolate(end, endCurve1, data.edgeCasing/end.distanceTo(endCurve1));
        
        draw(new LineSegment(paddinglessStart,startCurve1));
        draw(new LineSegment(startCurve2,endCurve1));
        draw(new LineSegment(paddinglessEnd,endCurve2));
        draw(arc1);
        draw(arc2);
        
        setStroke(Color.BLACK,data.stroke,Dashing.SOLID);
        
        draw(new LineSegment(start,startCurve1));
        draw(new LineSegment(startCurve2,endCurve1));
        draw(new LineSegment(end,endCurve2));
        draw(arc1);
        draw(arc2);
        
//        bends.add(arc1);
//        bends.add(arc2);
    }
    
    private void setPatternColor(int colorIndex){
        setStroke(lightColors[colorIndex % lightColors.length],data.stroke,Dashing.SOLID);
        setFill(lightColors[colorIndex % lightColors.length],Hashures.SOLID);
        this.setAlpha(data.highlightOpacity);
    }
    
    class Edge{
        int c;
        int r;
        public Edge(int r, int c) {
            this.c = c;
            this.r = r;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Edge other = (Edge) obj;
            if (this.c != other.c) {
                return false;
            }
            if (this.r != other.r) {
                return false;
            }
            return true;
        }
        
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
