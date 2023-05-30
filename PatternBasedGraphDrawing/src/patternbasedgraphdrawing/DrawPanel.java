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
import nl.tue.geometrycore.geometry.BaseGeometry;
import nl.tue.geometrycore.geometry.OrientedGeometry;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.curved.BezierCurve;
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
            drawGraph(new Vector(y + 40,y));
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
    
    public void drawGraph(Vector lefttop){
        // Draw edges
        
        // Draw patterns
        
        // Draw nodes
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
