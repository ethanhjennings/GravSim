package gravSim;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.WritableRaster;

import Physics.Physics;
import Physics.Vector2D;

public class PotentialField {
	int distFieldPts_; // Distance between field points
	int fieldWidth_; // In units of field points
	int fieldHeight_; // In units of field points
	int appletWidth_;
	int appletHeight_;
	int topY_;
	int leftX_;
	private BufferedImage img_;
	private double gravityConstant_;
	PotentialFieldComponent[][] field_;
	PotentialField(Double gravityConstant, int fieldWidth, int fieldHeight, int appletWidth, int appletHeight, int distFieldPts) {
		fieldWidth_ = fieldWidth;
		fieldHeight_ = fieldHeight;
		appletWidth_ = appletWidth;
		appletHeight_ = appletHeight;
		distFieldPts_ = distFieldPts;
		gravityConstant_ = gravityConstant;
		leftX_ = 0;
		topY_ = 0;
		field_ = new PotentialFieldComponent[fieldWidth_][];
		for (int x = 0; x < fieldWidth_; x++) {
			field_[x] = new PotentialFieldComponent[fieldHeight];
			for (int y = 0; y < fieldHeight_; y++) {
				field_[x][y] = new PotentialFieldComponent(new Vector2D(x*distFieldPts + leftX_,y*distFieldPts + topY_));
			}
		}
		img_ = new BufferedImage(fieldWidth,fieldHeight,BufferedImage.TYPE_INT_RGB);
		img_.setAccelerationPriority(1);
	}
	public void setGravityConstant(double constant) {
		gravityConstant_ = constant;
	}
	public void resetField() {
		for (int x = 0; x < fieldWidth_; x++) {
			for (int y = 0; y < fieldHeight_; y++) {
				field_[x][y].resetVec();
			}
		}
	}
	public void addObjectToField(Vector2D objectPos, double mass, double radius, boolean usingCharge) {
		for (int x = 0; x < fieldWidth_; x++) {
			for (int y = 0; y < fieldHeight_; y++) {
				field_[x][y].addToField(objectPos, distFieldPts_, mass, radius, gravityConstant_, usingCharge);
			}
		}
	}
	public void addObjectToField(Vector2D objectPos, double mass, double radius, boolean usingCharge, int startSlice, int endSlice) {
		for (int x = 0; x < fieldWidth_; x++) {
			for (int y = startSlice; y < endSlice + 1; y++) {
				field_[x][y].addToField(objectPos, distFieldPts_, mass, radius, gravityConstant_, usingCharge);
			}
		}
	}
	public void drawField(Graphics g, ImageObserver observer, boolean useBilinear) {
		Graphics2D g2 = (Graphics2D)g;
		if (useBilinear)
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		WritableRaster raster = img_.getRaster();
		for (int x = 0; x < fieldWidth_; x++) {
			for (int y = 0; y < fieldHeight_; y++) {
				field_[x][y].drawPixel(g,raster,x,y,distFieldPts_);
			}
		}
		g.drawImage(img_, 0, 0, appletWidth_, appletHeight_, observer);
	}
	public void finalizeField() {
		for (int x = 0; x < fieldWidth_; x++) {
			for (int y = 0; y < fieldHeight_; y++) {
				field_[x][y].finalizeComponent(distFieldPts_/2);
			}
		}
	}
	public int getFieldWidth() {
		return fieldWidth_;
	}
	public int getFieldHeight() {
		return fieldHeight_;
	}
	public int getDistFieldPts() {
		return distFieldPts_;
	}
}

class PotentialFieldComponent {
	private Vector2D vec_;
	private Vector2D pos_;
	private boolean render_;
	private double magnitude_;
	PotentialFieldComponent(Vector2D pos) {
		pos_ = pos;
		vec_ = new Vector2D();
		render_ = true;
	}
	public void drawPixel(Graphics g, WritableRaster raster, int x, int y, int distFieldPts) {
		float mag = (float) vec_.getMagnitude();
		float blue = (float)mag/5;
		float green = (float)mag/15;
		if (blue > 1)
			blue = 1;
		if (green > 1)
			green = 1;
		
		float[] colors1 = {0, green*255, blue*255};
		raster.setPixel(x, y, colors1);
		
		//g.setColor(Color.BLACK);
		//g.drawLine((int)(pos_.x_), (int)(pos_.y_), (int)(pos_.x_ + vec_.x_), (int)(pos_.y_ + vec_.y_));
		
		
		
		//int ovalWidth = 5;
		//g.drawOval((int)(pos_.x_ + vec_.x_)-ovalWidth/2, (int)(pos_.y_ + vec_.y_)-ovalWidth/2, ovalWidth, ovalWidth);
		//Polygon p = new Polygon();
		//Vector2D normalized = vec_.getNormalizedVec();
		//p.addPoint((int)(pos.x_ + vec_.x_), (int)(pos.y_ + vec_.y_));
		//p.addPoint(normalized, (int)(pos.y_ + vec_.y_));
		//g.fillPolygon(p);
	}
	public void addToField(Vector2D otherObject, double width, double otherObjectMass, double radius, double gravityConstant_, boolean usingCharge) {
		//if (!render_)
		//	return;
		Vector2D vecTo = Vector2D.sub(otherObject,Vector2D.sub(pos_,new Vector2D(-width/2,-width/2)));
		double distSq = vecTo.getMagnitudeSq();
		//if (distSq > otherObjectMass*otherObjectMass*800)
		//	return;
		int x;
		if (distSq == 0)
			render_ = false;
		if (distSq < radius*radius)
			distSq = radius*radius;
		
		double force;
		if (usingCharge)
			force = Physics.getChargeFieldSq(250*gravityConstant_, otherObjectMass, distSq);
		else
			force = Physics.getGravityFieldSq(250*gravityConstant_, otherObjectMass, distSq);
		//if (force > radius*2)
		//	force = radius*2;
		//if (false) {
		vecTo.normalize();
		vecTo.scale(force);
		vec_.add(vecTo);
		//}
	}
	public void resetVec() {
		magnitude_ = 0;
		render_ = true;
		vec_ = new Vector2D();
	}
	public void setVec(Vector2D vec) {
		render_ = true;
		vec_ = vec;
	}
	public void finalizeComponent(int maxLength) {
		if (vec_.getMagnitudeSq() > maxLength*maxLength) {
			vec_.normalize();
			vec_.scale(maxLength);
		}
	}
}