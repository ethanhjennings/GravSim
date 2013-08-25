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
    	//g.setColor(slider_.getForeground());
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
    	//paintMajorTickForHorizSlider(g,new Rectangle(),5);

    }
    @Override
    protected void paintMinorTickForHorizSlider(Graphics g, Rectangle rect, int x) {
    	//g.setColor(slider_.getForeground());
    	g.setColor(Color.white);
    	g.drawLine(10, 0, 10, slider_.getHeight());
    }
    @Override
    protected void paintMajorTickForVertSlider(Graphics g, Rectangle rect, int x) {
    	//g.setColor(slider_.getForeground());
    	g.setColor(Color.white);
    	g.drawLine(10, 0, 10, slider_.getHeight());
    } 
    @Override
    protected void paintMinorTickForVertSlider(Graphics g, Rectangle rect, int x) {
    	//g.setColor(slider_.getForeground());
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

/*
public class GravSlider extends JSlider {
	public GravSlider(int orientation, int min, int max, int startValue) {
		super(orientation,min,max,startValue);
	} 
	public void paintComponent(Graphics g) {
		if (this.getOrientation() == SwingConstants.VERTICAL) {
			getBorder().paintBorder(this, g, 5, 5, getBounds().width-10, getBounds().height-10);
			double yPos_ = (1-(double)sliderModel.getValue()/sliderModel.getMaximum())*(getBounds().height-10)+5;
			g.setColor(getBackground());
			if (getValueIsAdjusting())
				g.setColor(getForeground());
			else
				g.setColor(getBackground());
			g.fillRect(0, (int)yPos_ - 5, getBounds().width, 10);
			getBorder().paintBorder(this, g, 0, (int)yPos_ - 5, getBounds().width, 10);
		}
		else {
			getBorder().paintBorder(this, g, 5, 5, getBounds().width-10, getBounds().height-10);
			double xPos_ = ((double)sliderModel.getValue()/sliderModel.getMaximum())*(getBounds().width-10)+5;
			g.setColor(getBackground());
			if (getValueIsAdjusting())
				g.setColor(getForeground());
			else
				g.setColor(getBackground());
			g.fillRect((int)xPos_ - 5, 0, 10, getBounds().height);
			getBorder().paintBorder(this, g, (int)xPos_ - 5, 0, 10, getBounds().height);
		}
		
	}
	public void paintBorder(Graphics g) {
	}
}*/
