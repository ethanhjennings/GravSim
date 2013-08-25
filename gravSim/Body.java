package gravSim;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import Physics.Physics;
import Physics.Vector2D;

public class Body {
	private Vector2D pos_;
	private Vector2D vel_;
	private Vector2D accel_;
	private Vector2D lastPos_;
	private ArrayList<Vector2D> tail_;
	private double radius_;
	private double mass_;
	private boolean static_;
	private int id_;
	private static int currentId = 0;
	Color c_;
	public static final double velScale_ = 45.0;
	public static final double accelScale_ = 50000.0;
	private float selectionDashOffset_;
	private boolean collided_;
	
	public Body(Vector2D pos, Color c, Vector2D vel, double mass, double radius, boolean stat) {
		pos_ = pos.clone();
		lastPos_ = pos_.clone();
		c_ = c;
		if (vel == null)
			vel_ = new Vector2D(0,0);
		else
			vel_ = vel.clone();
		mass_ = mass;
		radius_ = radius;
		static_ = stat;
		tail_ = new ArrayList<Vector2D>();
		collided_ = false;
	}
	public void assignId() {
		id_ = currentId;
		currentId++;
	}
	public void updateAccel(final ArrayList<Body> otherActors, int numIterations, double gravityConstant, boolean usingCharges) {
		accel_ = new Vector2D();
		collided_ = false;
		if (!static_) {
			for (Body b : otherActors) {
				if (b != this) {
					Vector2D vecTo = Vector2D.sub(pos_, b.getPos());
					double distSq = vecTo.getMagnitudeSq();
					double minDistSq = Math.pow(radius_+b.getRadius(),2);
					if (distSq < minDistSq) {
						distSq = minDistSq;
						collided_ = true;
					}
					double accel;
					if (!usingCharges)
						accel = Physics.getChargeFieldSq(gravityConstant,  b.getMass(), distSq);
					else
						accel = Physics.getGravityFieldSq(gravityConstant, b.getMass(), distSq);
					vecTo.normalize();
					if (usingCharges)
					{
						if (mass_ < 0)
							vecTo.scale(-accel/(numIterations));
						else
							vecTo.scale(accel/(numIterations));
					}
					else
						vecTo.scale(accel/(numIterations));
					accel_.add(vecTo);
				}
			}
		}
	}
	public void updateVel(final ArrayList<Body> otherActors, int numIterations, double gravityConstant, boolean usingCharges) {
		if (static_)
			vel_.setXY(0, 0);
		else
			vel_.add(accel_);
	}
	public void clearTail() {
		tail_.clear();
		lastPos_ = pos_.clone();
	}
	public void updatePos(int numIterations, int tailLength, boolean updateTail) {
		if (!static_) {
			if (updateTail && Vector2D.sub(pos_,lastPos_).getMagnitudeSq() >= 1) {
				tail_.add(lastPos_);
				while (tail_.size() > tailLength)
					tail_.remove(0);
				lastPos_ = pos_.clone();
			}
			
			pos_.add(Vector2D.scale(vel_,1.0/((double)numIterations)));
		}
	}
	
	public double getRadius() {
		return radius_;
	}
	public Vector2D getPos() {
		return pos_;
	}
	public void setPos(Vector2D pos) {
		pos_ = pos.clone();
	}
	public Vector2D getVel() {
		return vel_;
	}
	public void setVel(Vector2D vel) {
		vel_ = vel;
	}
	public double getMass() {
		return mass_;
	}
	public void setMass(double mass) {
		mass_ = mass;
	}
	public void setStatic(boolean val) {
		static_ = val;
	}
	public Vector2D getScreenVel() {
		Vector2D scaled = vel_.clone();
		scaled.scale(velScale_);
		return scaled;
	}
	public Vector2D getScreenAccel() {
		Vector2D scaled = accel_.clone();
		scaled.scale(accelScale_);
		return scaled;
	}
	private void drawVec(Graphics g, Vector2D vec, boolean limitMaxLength) {
		Vector2D tempVec = vec.clone();
		boolean atMax = false;
		if (limitMaxLength && tempVec.getMagnitudeSq() > 30*30) {
			tempVec.normalize();
			tempVec.scale(30);
			atMax = true;
		}
		g.drawLine((int)pos_.x_, (int)pos_.y_, (int)(pos_.x_ + tempVec.x_), (int)(pos_.y_ + tempVec.y_));
		double velDirection = tempVec.getDirection();
		Vector2D leftPart = Vector2D.createVecDirMag((velDirection)-Math.PI-0.5, 5);
		Vector2D rightPart =  Vector2D.createVecDirMag((velDirection)+Math.PI+0.5, 5);
		g.drawLine((int)(pos_.x_ + tempVec.x_), (int)(pos_.y_ + tempVec.y_), (int)(pos_.x_ + tempVec.x_ + leftPart.x_), (int)(pos_.y_ + tempVec.y_ + leftPart.y_));
		g.drawLine((int)(pos_.x_ + tempVec.x_), (int)(pos_.y_ + tempVec.y_), (int)(pos_.x_ + tempVec.x_ + rightPart.x_), (int)(pos_.y_ + tempVec.y_ + rightPart.y_));
	}
	public void drawVelVec(Graphics g) {
		if (vel_.getMagnitudeSq()*velScale_*velScale_ < 1)
			return;
		g.setColor(new Color(0xffffffff));
		drawVec(g,getScreenVel(),false);
	}
	public void drawAccelVec(Graphics g) {
		if (accel_ == null)
			return; 
		if (accel_.getMagnitudeSq()*accelScale_*accelScale_ < 1)
			return;
		g.setColor(new Color(0xffffaa00));
		drawVec(g,getScreenAccel(),true);
	}
	public Color grayOut(Color c) {
		float hsv[] = new float[3];
		Color.RGBtoHSB(c_.getRed(), c_.getGreen(), c_.getBlue(), hsv);
		hsv[1]*=1.0f;
		hsv[2]*=0.5f;
		return new Color(Color.HSBtoRGB(hsv[0], hsv[1], hsv[2]));
	}
	public void draw(Graphics g, boolean drawSign, boolean drawSelection) {
		if (static_)
			g.setColor(grayOut(c_));
		else {
			if (collided_)
				g.setColor(new Color(c_.getRed(),c_.getGreen(),c_.getBlue(),128));
			else
				g.setColor(c_);
		}
		Graphics2D g2d = (Graphics2D)g;
		g.fillOval((int)(pos_.getX()-radius_), (int)(pos_.getY()-radius_), (int)(radius_*2), (int)(radius_*2));
		if (drawSign) {
			double thickness =  (radius_*2)*0.2;
			double size = (radius_*2)*0.8;
			if (mass_ != 0) {
				g.setColor(Color.black);
				g.fillRect((int)(pos_.x_-size/2), (int)(pos_.y_-thickness/2), (int)size, (int)thickness);
				if (mass_ > 0)
					g.fillRect((int)(pos_.x_-thickness/2), (int)(pos_.y_-size/2), (int)thickness, (int)size);
			}
				
		}
		//if (mass_ == 0) {
		//	double thickness =  (radius_*2)*0.1;
		//	double size = (radius_*2)*0.8;
		//	g.setColor(Color.black);
		//	g.fillRect((int)(pos_.x_-size*0.35), (int)(pos_.y_-size*0.25), (int)(size*0.7), (int)thickness);
		//	g.fillRect((int)(pos_.x_-thickness/2), (int)(pos_.y_-size*0.25), (int)thickness, (int)(size*0.8));
		//}
		if (drawSelection) {
			g.setColor(Color.WHITE);
			Stroke tempStroke = ((Graphics2D)g).getStroke();
			float dash[] = {5.0f};
			BasicStroke basicStroke = new BasicStroke(2.0f,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER,10.0f,dash,selectionDashOffset_);
			double selectionOvalRadius_ = 5;
			((Graphics2D)g).setStroke(basicStroke);
			g.drawOval((int)(pos_.getX()-radius_-selectionOvalRadius_), (int)(pos_.getY()-radius_-selectionOvalRadius_), (int)((radius_+selectionOvalRadius_)*2-1), (int)((radius_+selectionOvalRadius_)*2-1));
			((Graphics2D)g).setStroke(tempStroke);
			selectionDashOffset_ += 0.05f;
		}
	}
	public void drawTail(Graphics g) {
		g.setColor(c_);
		Vector2D lastPos = null;
		for (Vector2D pos : tail_) {
			if (lastPos != null)
				g.drawLine((int)lastPos.x_, (int)lastPos.y_, (int)pos.x_, (int)pos.y_);
			lastPos = pos;
		}
	}
	public void setScreenVec(Vector2D vec) {
		vec.scale(1/velScale_);
		vel_ = vec.clone();
	}
	public void setRadius(double radius) {
		radius_ = radius;
	}
	public void circularOrbit(Body b, double gravityConstant) {
		Vector2D vecTo = Vector2D.sub(pos_, b.getPos());
		double dist = vecTo.getMagnitude();
		setVel(Vector2D.createVecDirMag(vecTo.getDirection()+Math.PI/2,Physics.getCircularOrbitVel(gravityConstant, mass_, b.getMass(), dist)));
	}
	public Color getColor() {
		return c_;
	}
	public void setColor(Color color) {
		c_ = color;
	}
	public boolean isStatic() {
		return static_;
	}
}