package Physics;

import java.awt.Graphics;
import java.awt.Rectangle;

public class Physics {
	public static double getGravityForce(double gravityConstant, double mass1, double mass2, double dist) { // Newton's Universal Law of Gravitation
		if (mass1 == 0)
			return (-gravityConstant*mass2)/(dist*dist);
		if (mass2 == 0)
			return (-gravityConstant*mass1)/(dist*dist);
		return (-gravityConstant*mass1*mass2)/(dist*dist);
	}
	public static double getGravityForceSq(double gravityConstant, double mass1, double mass2, double dist) { // Same as above but with the distance already squared (useful for optimizations that avoid square roots)
		if (mass1 == 0)
			return (-gravityConstant*mass2)/(dist);
		if (mass2 == 0)
			return (-gravityConstant*mass1)/(dist);
		return (-gravityConstant*mass1*mass2)/(dist);
	}
	public static double getGravityField(double gravityConstant, double mass, double dist) { // Used for finding the strength of the gravity field due to one object at one point.
		return (gravityConstant*mass)/(dist*dist);
	}
	public static double getGravityFieldSq(double gravityConstant, double mass, double dist) { // Same as above but with the distance already squared (useful for optimizations that avoid square roots)
		return (gravityConstant*mass)/(dist);
	}
	public static double getChargeFieldSq(double gravityConstant, double mass, double dist) { // Same as above but with the distance already squared (useful for optimizations that avoid square roots)
		return (-gravityConstant*mass)/(dist);
	}
	public static double getChargeForceSq(double gravityConstant, double mass1, double mass2, double dist) {
		if (mass1 == 0)
			return (gravityConstant*mass2)/(dist); 
		if (mass2 == 0)
			return (gravityConstant*mass1)/(dist);
		return (gravityConstant*mass1*mass2)/(dist);
	}
	public static double getCircularOrbitVel(double gravityConstant, double mass1, double mass2, double dist) {
		double force = getGravityForce(gravityConstant, mass1, mass2, dist);
		if (mass2 == 0)
			return 0;
		if (mass1 == 0)
			return Math.sqrt(Math.abs((force*dist)));
		return Math.sqrt(Math.abs((force*dist)/mass1));
	}
	public static boolean pointCircleCollision(Vector2D circCenter, double circRadius, Vector2D point) {
		if (Vector2D.sub(point, circCenter).getMagnitudeSq() < circRadius*circRadius)
			return true;
		return false;
	}
	public static boolean pointRectCollision(Vector2D point, Rectangle rect) {
		return rect.contains(point);
	}
	public static boolean pointRotatedRectCollision(Graphics g,final Vector2D point, double rectWidth, double rectHeight, Vector2D rectPos, double rectAngle) {
		Vector2D pointCpy = point.clone();
		pointCpy.rotateAboutPoint(rectPos, -rectAngle);
		return pointRectCollision(pointCpy,new Rectangle((int)rectPos.x_,(int)(rectPos.y_-rectHeight/2),(int)rectWidth,(int)rectHeight));
	}
	public static double getPotential(double gravityConstant, double mass, double dist) {
		return (gravityConstant*mass)/dist;
	}
}
