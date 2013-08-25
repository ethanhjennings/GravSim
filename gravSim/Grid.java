package gravSim;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

import Physics.*;

public class Grid {
	private double appletWidth_;
	private double appletHeight_;
	private double gridSize_;

	Grid(double gridSize, double appletWidth, double appletHeight) {
		gridSize_ = gridSize;

		appletWidth_ = appletWidth;
		appletHeight_ = appletHeight;
	}

	public Vector2D snapToGrid(Vector2D object, double cameraZoom,
			boolean screenSnap) {

		int subGrid = 4;

		double adjustedGridSize = gridSize_;

		while (adjustedGridSize * cameraZoom * 2 < gridSize_) {
			adjustedGridSize *= subGrid;
		}
		double posX;
		double posY;
		if (screenSnap) {
			posX =
					Math.round((object.x_ - appletWidth_ / 2) /
								adjustedGridSize) *
							adjustedGridSize + appletWidth_ / 2;
			posY =
					Math.round((object.y_ - appletHeight_ / 2) /
								adjustedGridSize) *
							adjustedGridSize + appletHeight_ / 2;
		} else {
			posX = Math.round((object.x_) / (gridSize_ / 3)) * (gridSize_ / 3);
			posY = Math.round((object.y_) / (gridSize_ / 3)) * (gridSize_ / 3);
		}
		return new Vector2D(posX, posY);
	}

	public void drawGrid(Graphics g, Vector2D cameraPos, double cameraZoom) {

		int subGrid = 4;
		double adjustedGridSize = gridSize_ * subGrid;

		while (adjustedGridSize * cameraZoom < gridSize_) {
			adjustedGridSize *= subGrid;
		}

		double subSubGridOpacity =
				1 - ((gridSize_) / (adjustedGridSize * cameraZoom));

		int startXPos =
				(int) ((cameraPos.x_ * cameraZoom + appletWidth_ / 2) %
						((adjustedGridSize) * cameraZoom) - (adjustedGridSize * cameraZoom));
		int lineNum = 0;
		for (double x = startXPos; x < appletWidth_; x +=
				(adjustedGridSize * cameraZoom) / subGrid) {
			if (lineNum % subGrid == 0)
				g.setColor(new Color(50, 50, 50,
						(int) (75 + subSubGridOpacity * 180)));
			else
				g.setColor(new Color(50, 50, 50,
						(int) (100 * subSubGridOpacity)));
			g.drawLine(	(int) x, 0, (int) x, (int) appletHeight_);
			lineNum++;
		}
		lineNum = 0;
		int startYPos =
				(int) ((cameraPos.y_ * cameraZoom + appletHeight_ / 2) %
						((adjustedGridSize) * cameraZoom) - (adjustedGridSize * cameraZoom));
		for (double y = startYPos; y < appletHeight_; y +=
				(adjustedGridSize * cameraZoom) / subGrid) {
			if (lineNum % subGrid == 0)
				g.setColor(new Color(50, 50, 50,
						(int) (75 + subSubGridOpacity * 180)));
			else
				g.setColor(new Color(50, 50, 50,
						(int) (100 * subSubGridOpacity)));
			g.drawLine(	0, (int) y, (int) appletWidth_, (int) y);
			lineNum++;
		}

		startXPos =
				(int) ((cameraPos.x_ * cameraZoom + appletWidth_ / 2) % ((adjustedGridSize * subGrid) * cameraZoom));
		lineNum = 0;
		for (double x = startXPos; x < appletWidth_; x +=
				(adjustedGridSize * subGrid * cameraZoom)) {
			g.setColor(new Color(50, 50, 50,
					(int) (210 + 40 * subSubGridOpacity)));
			g.drawLine(	(int) Math.ceil(x), 0, (int) Math.ceil(x),
						(int) appletHeight_);
			lineNum++;
		}
		lineNum = 0;
		startYPos =
				(int) ((cameraPos.y_ * cameraZoom + appletHeight_ / 2) % ((adjustedGridSize * subGrid) * cameraZoom));
		for (double y = startYPos; y < appletHeight_; y +=
				(adjustedGridSize * subGrid * cameraZoom)) {
			g.setColor(new Color(50, 50, 50,
					(int) (210 + 40 * subSubGridOpacity)));
			g.drawLine(	0, (int) Math.ceil(y), (int) appletWidth_, (int) Math
								.ceil(y));
			lineNum++;
		}
	}

	public Vector2D getTopLeftPoint(Vector2D cameraPos, double cameraZoom) {
		int subGrid = 4;
		double adjustedGridSize = gridSize_ * subGrid;

		while (adjustedGridSize * cameraZoom < gridSize_) {
			adjustedGridSize *= subGrid;
		}

		int x =
				(int) ((cameraPos.x_ * cameraZoom + appletWidth_ / 2) %
						((adjustedGridSize) * cameraZoom) - (adjustedGridSize * cameraZoom));
		int y =
				(int) ((cameraPos.y_ * cameraZoom + appletHeight_ / 2) %
						((adjustedGridSize) * cameraZoom) - (adjustedGridSize * cameraZoom));
		return new Vector2D(x, y);
	}
}
