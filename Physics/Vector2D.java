package Physics;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Random;

public class Vector2D extends Point2D {
	public double x_;
	public double y_;
	public Vector2D() {
		x_ = 0;
		y_ = 0;
	}
	public Vector2D(double x, double y) {
		x_ = x;
		y_ = y;
	}
	public Vector2D(Vector2D vec) {
		x_ = vec.getX();
		y_ = vec.getY();
	}
	public Vector2D(Point2D result) {
		x_ = result.getX();
		y_ = result.getY();
	}
	public Vector2D(double dist) {
		x_ = dist;
		y_ = dist;
	}
	public double getMagnitude() {
		return Math.sqrt(x_*x_+y_*y_);
	}
	public void setX(double x) {
		x_ = x;
	}
	public void setY(double y) {
		y_ = y;
	}
	public void setXY(double x, double y) {
		setLocation(x,y);
	}
	public double getX() {
		return x_;
	}
	public double getY() {
		return y_;
	}
	public void add(Vector2D vec) {
		x_ += vec.x_;
		y_ += vec.y_;
	}
	public void sub(Vector2D vec) {
		x_ -= vec.x_;
		y_ -= vec.y_;
	}
	public void scale(double scalar) {
		x_ *= scalar;
		y_ *= scalar;
	}
	public static Vector2D add(Vector2D vec1, Vector2D vec2) {
		return new Vector2D(vec1.getX() + vec2.getX(), vec1.getY() + vec2.getY());
	}
	public static Vector2D sub(Vector2D vec1, Vector2D vec2) {
		return new Vector2D(vec1.getX() - vec2.getX(), vec1.getY() - vec2.getY());
	}
	public static Vector2D scale(Vector2D vec1, double scalar) {
		return new Vector2D(vec1.getX()*scalar, vec1.getY()*scalar);
	}
	public void normalize() {
		double magnitude = getMagnitude();
		x_ /= magnitude;
		y_ /= magnitude;
	}
	public Vector2D getNormalizedVec() {
		double magnitude = getMagnitude(); 
		return new Vector2D(x_/magnitude,y_/magnitude);
	}
	public double getMagnitudeSq() {
		return x_*x_+y_*y_;
	}
	// @Doc 
	// Returns the direction that the vector is facing in radians counterclockwise of the x axis. Uses Math.atan2(). Note that positive y's point down.
	public double getDirection() {
		double angle = Math.atan2(y_, x_);
		while (angle < 0)
			angle += 2*Math.PI;
		while (angle >= 2*Math.PI)
			angle -= 2*Math.PI;
		return angle;
	}
	public Vector2D clone() {
		return new Vector2D(this);
	}
	public static Vector2D moveToPoint(Vector2D pos, Vector2D target, double speed) {
		Vector2D ret = new Vector2D(pos);
		ret.x_ += (target.x_-pos.x_)*speed;
		ret.y_ += (target.y_-pos.y_)*speed;
		return ret;
	}
	public Point2D toPoint2D() {
		return new Point2D.Double(x_,y_);
	}
	public static Vector2D createVecDirMag(double direction, double magnitude) {
		return new Vector2D(Math.cos(direction)*magnitude,Math.sin(direction)*magnitude);
	};
	public static Vector2D createRandom(Random r, int minX, int maxX, int minY, int maxY) {
		return new Vector2D(r.nextInt(maxX-minX)+minX,r.nextInt(maxY-minY)+minY);
	}
	public void rotateAboutPoint(Vector2D otherPoint, double angle) {
		sub(otherPoint);
		double x = x_;
		double y = y_;
		x_ = x*Math.cos(angle) - y*Math.sin(angle);
		y_ = x*Math.sin(angle) + y*Math.cos(angle);
		add(otherPoint);
	}
	@Override
	public void setLocation(double x, double y) {
		x_ = x;
		y_ = y;
	}
}
