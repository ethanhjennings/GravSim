package gravSim;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.Point2D;

import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicSliderUI;
import javax.swing.plaf.metal.MetalSliderUI;

public class GravSlider extends MetalSliderUI  {

    private int thumbHeight = 25;
    private int thumbWidth = 25;
    private int thumbThickness = 15;
    private int trackThickness = 5;
    JSlider slider_;
    Border border_;
    
    public GravSlider(JSlider slider, Border border) {
    	super();
        slider_ = slider;
        border_ = border;
    }

    @Override
    protected Dimension getThumbSize() {
        return new Dimension(thumbWidth, thumbHeight);
    }

    @Override
    public void paintTrack(Graphics g) {
		if (slider_.getOrientation() == SwingConstants.VERTICAL) {
			border_.paintBorder(slider_, g, (slider_.getBounds().width-trackThickness)/2, 0, trackThickness, slider_.getBounds().height);
		}
		else {
			border_.paintBorder(slider_, g, 0, (slider_.getBounds().height-trackThickness)/2, slider_.getBounds().width, trackThickness);
		}
    }
    @Override 
    protected void paintMajorTickForHorizSlider(Graphics g, Rectangle rect, int x) {
    	g.setColor(Color.white);
    	g.drawLine(x, (slider_.getHeight()+20)/2, x, slider_.getHeight());
    } 
    @Override
	public void paintTicks(Graphics g) {
    	int start = slider_.getMinimum();
    	int end = slider_.getMaximum();
    	int numTicks = end - start + 1;
    	double xPos = thumbWidth/2;
    	double spacing = (double)(slider_.getWidth()-thumbWidth)/(numTicks-1);
    	for (int i = 0; i < numTicks; i++) {
    		paintMajorTickForHorizSlider(g,new Rectangle(),(int)Math.round(xPos));
    		xPos += spacing;
    	}

    }
    @Override
    protected void paintMinorTickForHorizSlider(Graphics g, Rectangle rect, int x) {
    	g.setColor(Color.white);
    	g.drawLine(10, 0, 10, slider_.getHeight());
    }
    @Override
    protected void paintMajorTickForVertSlider(Graphics g, Rectangle rect, int x) {
    	g.setColor(Color.white);
    	g.drawLine(10, 0, 10, slider_.getHeight());
    } 
    @Override
    protected void paintMinorTickForVertSlider(Graphics g, Rectangle rect, int x) {
    	g.setColor(Color.white);
    	g.drawLine(10, 0, 10, slider_.getHeight());
    }
    
    @Override
    public void paintThumb(Graphics g) {
    	
    	if (slider_.getValueIsAdjusting())
			g.setColor(slider_.getForeground());
		else
			g.setColor(slider_.getBackground());
    	if (slider_.getOrientation() == SwingConstants.VERTICAL) {
    		int x = (slider_.getWidth()-thumbThickness)/2; 
    		int y = thumbRect.y;
    		int width = thumbThickness; 
    		int height = thumbHeight;
    		g.fillRect(x, y, width, height);
    		border_.paintBorder(slider_,g,x,y,width,height);
    	}
    	else {
    		int x = thumbRect.x; 
    		int y = (slider_.getHeight()-thumbThickness)/2;
    		int width = thumbWidth; 
    		int height = thumbThickness;
    		g.fillRect(x, y, width, height);
    		border_.paintBorder(slider_,g,x,y,width,height);
    	}
    }

}