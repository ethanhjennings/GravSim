package gravSim;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.WritableRaster;

import Physics.Physics;
import Physics.Vector2D;

public class GravityField {
	int distFieldPts_; // Distance between field points
	int fieldWidth_; // In units of field points
	int fieldHeight_; // In units of field points
	int appletWidth_;
	int appletHeight_;
	int topY_;
	int leftX_;
	private BufferedImage img_;
	private boolean potentialField_;
	private boolean vectorMode_;
	private double gravityConstant_;
	private boolean showingVectorHeads_;
	FieldComponent[][] field_;

	GravityField(boolean vectorMode, boolean potentialField,
			Double gravityConstant, int fieldWidth, int fieldHeight,
			int appletWidth, int appletHeight, int distFieldPts) {
		potentialField_ = potentialField;
		fieldWidth_ = fieldWidth;
		fieldHeight_ = fieldHeight;
		appletWidth_ = appletWidth;
		appletHeight_ = appletHeight;
		distFieldPts_ = distFieldPts;
		gravityConstant_ = gravityConstant;
		showingVectorHeads_ = false;
		leftX_ = 0;
		topY_ = 0;
		vectorMode_ = vectorMode;
		field_ = new FieldComponent[fieldWidth_][];
		for (int x = 0; x < fieldWidth_; x++) {
			field_[x] = new FieldComponent[fieldHeight];
			for (int y = 0; y < fieldHeight_; y++) {
				field_[x][y] =
						new FieldComponent(new Vector2D(x * distFieldPts +
														leftX_,
								y * ((float) appletHeight_ / fieldHeight_) +
										topY_));
			}
		}
		img_ =
				new BufferedImage(fieldWidth, fieldHeight,
						BufferedImage.TYPE_INT_RGB);
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

	public void addObjectToField(Vector2D objectPos, double mass,
			double radius, boolean usingCharge) {
		for (int x = 0; x < fieldWidth_; x++) {
			for (int y = 0; y < fieldHeight_; y++) {
				field_[x][y].addToField(potentialField_, objectPos,
										distFieldPts_, (float) appletHeight_ /
														fieldHeight_, mass,
										radius, gravityConstant_, usingCharge);
			}
		}
	}

	public void addObjectToField(Vector2D objectPos, double mass,
			double radius, boolean usingCharge, int startSlice, int endSlice) {
		for (int x = 0; x < fieldWidth_; x++) {
			for (int y = startSlice; y < endSlice + 1; y++) {
				field_[x][y].addToField(potentialField_, objectPos,
										distFieldPts_, (float) appletHeight_ /
														fieldHeight_, mass,
										radius, gravityConstant_, usingCharge); // FIXME:
																				// Sometimes
																				// causes
																				// errors
			}
		}
	}

	public void drawField(Graphics g, ImageObserver observer,
			boolean useBilinear) {
		if (vectorMode_) {
			for (int x = 0; x < fieldWidth_; x++) {
				for (int y = 0; y < fieldHeight_; y++) {
					field_[x][y]
							.drawVector(g,
										new Vector2D(
												distFieldPts_ / 2,
												((float) appletHeight_ / fieldHeight_) / 2),
										showingVectorHeads_);
				}
			}
		} else {
			Graphics2D g2 = (Graphics2D) g;
			if (useBilinear)
				g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
									RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			WritableRaster raster = img_.getRaster();
			for (int x = 0; x < fieldWidth_; x++) {
				for (int y = 0; y < fieldHeight_; y++) {
					field_[x][y].drawPixel(	g, raster, potentialField_, x, y,
											distFieldPts_);
				}
			}
			g.drawImage(img_, 0, 0, appletWidth_, appletHeight_, observer);
		}
	}

	public void finalizeField() {
		for (int x = 0; x < fieldWidth_; x++) {
			for (int y = 0; y < fieldHeight_; y++) {
				field_[x][y].finalizeComponent(distFieldPts_ / 2);
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

	public void setShowingVectorHeads(boolean value) {
		showingVectorHeads_ = value;
	}

	public boolean isPotentialField() {
		return potentialField_;
	}

	public void setPotentialField(boolean value) {
		potentialField_ = value;
	}

	public int getResolution() {
		return distFieldPts_;
	}
}

class FieldComponent {
	private Vector2D vec_;
	private Vector2D pos_;
	private boolean render_;
	private double magnitude_;

	FieldComponent(Vector2D pos) {
		pos_ = pos;
		vec_ = new Vector2D();
		render_ = true;
	}

	public void drawPixel(Graphics g, WritableRaster raster,
			boolean potentialField, int x, int y, int distFieldPts) {
		float red = 0;
		float green = 0;
		float blue = 0;
		if (!potentialField) {
			float mag = (float) vec_.getMagnitude();
			blue = (float) mag / 5;
			green = (float) mag / 15;
		} else {
			if (magnitude_ >= 0) {
				blue = (float) magnitude_ / 5;
				green = (float) (magnitude_) / 15;
				red = (float) (magnitude_) / 30;
			} else {
				red = (float) Math.abs(magnitude_) / 5;
				blue = (float) Math.abs(magnitude_) / 15;
				green = (float) Math.abs(magnitude_) / 15;

			}
		}
		if (blue > 1)
			blue = 1;
		if (green > 1)
			green = 1;
		if (red > 1)
			red = 1;

		float[] colors1 = { red * 255, green * 255, blue * 255 };
		raster.setPixel(x, y, colors1);
	}

	public void drawVector(Graphics g, Vector2D offset,
			boolean showingVectorHeads) {
		if (!render_)
			return;
		g.setColor(new Color(0xffffffff));
		Vector2D scaled = vec_.clone();
		if (vec_.getMagnitudeSq() > 1 * 1) {
			scaled.normalize();
			scaled.scale(1);
		}
		scaled.scale(20);
		g.drawLine(	(int) (pos_.x_ + offset.x_), (int) (pos_.y_ + offset.y_),
					(int) (pos_.x_ + scaled.x_ + offset.x_),
					(int) (pos_.y_ + scaled.y_ + offset.y_));
		if (showingVectorHeads) {
			double velDirection = scaled.getDirection();
			Vector2D leftPart =
					Vector2D.createVecDirMag(	(velDirection) - Math.PI - 0.5,
												5);
			Vector2D rightPart =
					Vector2D.createVecDirMag(	(velDirection) + Math.PI + 0.5,
												5);
			g.drawLine(	(int) (pos_.x_ + scaled.x_ + offset.x_),
						(int) (pos_.y_ + scaled.y_ + offset.y_),
						(int) (pos_.x_ + scaled.x_ + offset.x_ + leftPart.x_),
						(int) (pos_.y_ + scaled.y_ + offset.y_ + leftPart.y_));
			g.drawLine(	(int) (pos_.x_ + scaled.x_ + offset.x_),
						(int) (pos_.y_ + scaled.y_ + offset.y_),
						(int) (pos_.x_ + scaled.x_ + offset.x_ + rightPart.x_),
						(int) (pos_.y_ + scaled.y_ + offset.y_ + rightPart.y_));
		}

	}

	public void addToField(boolean potentialField, Vector2D otherObject,
			double width, double height, double otherObjectMass, double radius,
			double gravityConstant_, boolean usingCharge) {
		if (potentialField) {
			Vector2D vecTo =
					Vector2D.sub(	otherObject, Vector2D
											.sub(	pos_, new Vector2D(
															-width / 2,
															-height / 2)));
			double dist = vecTo.getMagnitude();
			magnitude_ +=
					Physics.getPotential(	10 * gravityConstant_,
											otherObjectMass, dist);
		} else {
			Vector2D vecTo =
					Vector2D.sub(	otherObject, Vector2D
											.sub(	pos_, new Vector2D(
															-width / 2,
															-height / 2)));
			double distSq = vecTo.getMagnitudeSq();

			int x;
			if (distSq == 0)
				render_ = false;
			if (distSq < radius * radius)
				distSq = radius * radius;

			double force;
			if (usingCharge)
				force =
						Physics.getChargeFieldSq(	250 * gravityConstant_,
													otherObjectMass, distSq);
			else
				force =
						Physics.getGravityFieldSq(	250 * gravityConstant_,
													otherObjectMass, distSq);

			vecTo.normalize();
			vecTo.scale(force);
			vec_.add(vecTo);
		}
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
		if (vec_.getMagnitudeSq() > maxLength * maxLength) {
			vec_.normalize();
			vec_.scale(maxLength);
		}
	}
}