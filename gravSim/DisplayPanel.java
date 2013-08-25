package gravSim;


import gravSim.DisplayPanel.FinishedCount;
import gravSim.DisplayPanel.GravityFieldRunnable;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;

import Physics.Vector2D;



public class DisplayPanel extends JPanel implements MouseMotionListener, MouseListener  {
	
	private static final boolean DISPLAY_FRAME_TIMES = false;
	
	private ArrayList<Body> bodies_;
	private Random r_;
	
	private boolean holdingOnSelectedBody_;
	private Body movingBody_;
	private Body selectedBody_;
	private Body followingBody_;
	
	private boolean leftMouseDown_;
	private boolean rightMouseDown_;
	
	private Vector2D actualMousePos_;
	private Vector2D mousePos_;
	 
	private boolean running_;
	
	private boolean draggingCamera_;
	private Vector2D lastMousePos_;
	
	private Vector2D cameraPos_;
	
	private int appletWidth_;
	private int appletHeight_;
	
	private float elapsedTimeUpdate_;
	private float elapsedTimeRender_;
	
	private double scale_;
	private double scaleTarget_;
	private double lastScale_;
	
	private GravityField smoothGravityField_;
	private GravityField vectorGravityField_;
	
	private boolean showingVelVectors_;
	private boolean showingAccelVectors_;
	private boolean showingSmoothField_;
	private boolean showingVectorField_;
	private boolean showingGrid_;
	private boolean showingTrails_;
	
	private double bodyMass_;
	private double bodyRadius_;
	
	private boolean static_;
	
	private Color newBodyColor_;
	
	public enum ClickMode {SELECT, ADD, SET_VEL, FOLLOW, DEL, CIRC_ORBIT_1, CIRC_ORBIT_2};
	private ClickMode mode_;
	private int numIterations_;
	
	private GravSimCallback callback_;
	private Boolean drawing_;
	
	private ArrayList<Double> lastUpdateFrames_;
	private ArrayList<Double> lastRenderFrames_;
	private double gravityConstant_;
	private boolean usingCharges_;
	private int numThreads_;
	
	private Vector2D mouseDragOffset_;
	private Vector2D startDragPos_;
	
	private String hintText_;
	private String nextHintText_;
	private boolean showingHintText_;
	private boolean changingHintText_;
	private boolean showingDragCameraHint_;
	private float hintTextAlpha_;
	
	private boolean useBilinear_;
	
	private Body orbitingBody_;
	
	private Grid grid_;
	private ExecutorService taskExecutor_;
	private ArrayBlockingQueue<Future> gravityFieldFutures_;
	private ArrayBlockingQueue<Future> vectorFieldFutures_;
	private int tailLength_;
	private boolean accelVectorsDirty_;
	private boolean interfaceHidden_;
	private boolean showingPotentialField_;
	private boolean movingObjectStartedDragging_;
	private Vector2D mouseOffset_;

	private boolean redrawField_;

	private boolean scaleChanged_;

	private FinishedCount numFinished_;

	class FinishedCount {
		public int numFinished_ = 0;
	}
	

	DisplayPanel(int appletWidth, int appletHeight, GravSimCallback callback, Color newBodyColor, Vector2D mouseOffset) {
		newBodyColor_ = newBodyColor;
		appletWidth_ = appletWidth;
		appletHeight_ = appletHeight;
		
		callback_ = callback;
		
		mouseOffset_ = mouseOffset;
	
		
		
		useBilinear_ = true;
		
		drawing_ = new Boolean(false);
		
		lastUpdateFrames_ = new ArrayList<Double>();
		lastRenderFrames_ = new ArrayList<Double>();
		
		tailLength_ = 200;
		 
		bodies_ = new ArrayList<Body>();
		bodyRadius_ = 10;
		bodyMass_ = 10;
		gravityConstant_ = 0.7;
		numIterations_ = 8;
		Random r = new Random();
		
		hintTextAlpha_ = 0;
		showingHintText_ = true;
		changingHintText_ = false;
		hintText_ = new String();
		nextHintText_ = "left click to do stuff; right click & drag to move the camera";
		showingDragCameraHint_ = true;
		
		setAccelVectorsDirty();
		
		int resolution = 7;
		smoothGravityField_ = new GravityField(false,true,gravityConstant_,appletWidth/resolution, appletHeight/resolution, appletWidth, appletHeight, resolution);
		resolution = 20;
		vectorGravityField_ = new GravityField(true,false,gravityConstant_,appletWidth/resolution, appletHeight/resolution, appletWidth, appletHeight, resolution);
		
		grid_ = new Grid(15, appletWidth_, appletHeight_);
		
		numThreads_ = Runtime.getRuntime().availableProcessors();
		numThreads_ = 4;
		taskExecutor_ = Executors.newFixedThreadPool(numThreads_);
		gravityFieldFutures_ = new ArrayBlockingQueue<Future>(numThreads_);
		vectorFieldFutures_ = new ArrayBlockingQueue<Future>(numThreads_);
		
		int numBodies = 50;
		int num = 0;
		double angleBetween = Math.PI*2/numBodies;

		holdingOnSelectedBody_ = false;
		selectedBody_ = null;
		
		cameraPos_ = new Vector2D();
		lastMousePos_ = new Vector2D();
		
		mode_ = ClickMode.ADD;
		
		running_ = false;
		
		scale_ = 1.0;
		lastScale_ = scale_;
		scaleTarget_ = scale_;
		static_ = false;
		
		showingSmoothField_ = false;
		showingPotentialField_ = false;
		showingVectorField_ = false;
		showingTrails_ = false;
		showingGrid_ = false;
		showingAccelVectors_ = false;
		showingVelVectors_ = false;
		
		toggleShowVelVectors();
		toggleShowTrails();
		
		interfaceHidden_ = false;
		
		mouseDragOffset_ = new Vector2D();
	} 
	
	protected void paintComponent(Graphics g) {
		synchronized (bodies_) {
			synchronized (smoothGravityField_) {
				drawing_ = new Boolean(true);
				if (followingBody_ != null) {
					cameraPos_.x_ = -followingBody_.getPos().x_+appletWidth_/2;// - cameraPos_.x_)*0.1;
					cameraPos_.y_ = -followingBody_.getPos().y_+appletHeight_/2; //- cameraPos_.y_)*0.1;
				}
				long startTime = System.currentTimeMillis();
				
				
				AffineTransform trans = ((Graphics2D)g).getTransform();
				
				((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); 
				((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
				
				
				
				if (showingSmoothField_ || showingPotentialField_) {
					smoothGravityField_.drawField(g,this,useBilinear_);
				}
				else {
					g.setColor(Color.BLACK);
					g.fillRect(0, 0, appletWidth_, appletHeight_);
				}
				if (showingVectorField_) { 
					vectorGravityField_.drawField(g, this, useBilinear_);
				}
				if (showingGrid_)
					grid_.drawGrid(g, cameraPos_, scale_);
				g.translate((int)appletWidth_/2, (int)appletHeight_/2);
				((Graphics2D)g).scale(scale_, scale_);
				g.translate((int)-appletWidth_/2, (int)-appletHeight_/2);
				g.translate((int)cameraPos_.x_, (int)cameraPos_.y_);
				
					((Graphics2D)g).setStroke(new BasicStroke(1));
					((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
					if (showingTrails_) {
						for (Body b : bodies_) {
							b.drawTail(g);
						}
					}
					((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); 
					for (Body b : bodies_) {
						b.draw(g,usingCharges_,b == selectedBody_);
					}
					if (showingVelVectors_) {
						((Graphics2D)g).setStroke(new BasicStroke(3));
						for (Body b : bodies_) {
							b.drawVelVec(g);
						}
					}
					if (showingAccelVectors_) {
						((Graphics2D)g).setStroke(new BasicStroke(3));
						for (Body b : bodies_) {
							b.drawAccelVec(g);
						}
					}
					if (DISPLAY_FRAME_TIMES) {
						lastRenderFrames_.add((double)(System.currentTimeMillis() - startTime));
						while (lastRenderFrames_.size() >= 30)
							lastRenderFrames_.remove(0);
						elapsedTimeRender_ = 0;
						for (Double d : lastRenderFrames_) {
							elapsedTimeRender_ += d;
						}
						elapsedTimeRender_ /= lastRenderFrames_.size();
					}
				g.setColor(Color.WHITE);
				((Graphics2D)g).setTransform(trans);

				if (!interfaceHidden_ && DISPLAY_FRAME_TIMES) {
				
					g.drawString("Render time: " + String.format("%.3f", elapsedTimeRender_), 200, 50);
					g.drawString("Update time: " + String.format("%.3f", elapsedTimeUpdate_), 200, 35);
				}
				if (hintTextAlpha_ > 0) {
					g.setFont(new Font("Dialog",Font.BOLD,16));
					g.setColor(new Color(1.0f,1.0f,1.0f,hintTextAlpha_));
					Rectangle2D hintTextRect = g.getFontMetrics().getStringBounds(hintText_,g);
					g.drawString(hintText_,(int) ((appletWidth_-hintTextRect.getWidth())/2),appletHeight_-140);
				}
				
				drawing_ = new Boolean(false);
			}
		}
	}
	
	class GravityFieldRunnable implements Callable< Void > {

		private int startYValue_;
		private int endYValue_;
		private boolean vectorField_;
		private ArrayList<Body> bodyListCopy_;
		private FinishedCount numFinished_;
		
		GravityFieldRunnable(boolean vectorField,int startYValue, int endYValue, ArrayList<Body> bodyListCopy, FinishedCount numFinished) {
			startYValue_ = startYValue;
			endYValue_ = endYValue;
			vectorField_ = vectorField;
			bodyListCopy_ = bodyListCopy;
			numFinished_ = numFinished;
		}
		

		@Override
		public Void call() {
			if (vectorField_) {
				for (Body b: bodyListCopy_) {
					vectorGravityField_.addObjectToField(realPosToScreen(b.getPos()), realDistToScreen(b.getMass()), realDistToScreen(b.getRadius()), usingCharges_,startYValue_,endYValue_);
				}
			}
			else {
				for (Body b: bodyListCopy_) {
					smoothGravityField_.addObjectToField(realPosToScreen(b.getPos()), realDistToScreen(b.getMass()), realDistToScreen(b.getRadius()), usingCharges_,startYValue_,endYValue_);
				}
			}
			numFinished_.numFinished_++;
			return null;
		}
		
	}
	
	public void startCalculatingGravity(boolean vectorField) {
		
		
		ArrayList<Body> listCopy = new ArrayList<Body>(bodies_.size());
		for (Body b : bodies_) {
			listCopy.add(b);
		}
		
		Collection<GravityFieldRunnable> tasks = new ArrayList<GravityFieldRunnable>();
		if (vectorField) {
			vectorFieldFutures_.clear();
			int verticalSlice = (int)Math.round((double)vectorGravityField_.getFieldHeight()/numThreads_);
			for (int i = 0; i < numThreads_; i++) {
				if (i == numThreads_ - 1)
					tasks.add(new GravityFieldRunnable(vectorField,i*verticalSlice,vectorGravityField_.getFieldHeight()-1,listCopy,numFinished_));
				else
					tasks.add(new GravityFieldRunnable(vectorField,i*verticalSlice,(i+1)*verticalSlice-1,listCopy,numFinished_));;
			}
		}
		else {
			gravityFieldFutures_.clear();
			int verticalSlice = (int)Math.round((double)smoothGravityField_.getFieldHeight()/numThreads_);
			for (int i = 0; i < numThreads_; i++) {
				if (i == numThreads_ - 1)
					tasks.add(new GravityFieldRunnable(vectorField,i*verticalSlice,smoothGravityField_.getFieldHeight()-1,listCopy,numFinished_));
				else
					tasks.add(new GravityFieldRunnable(vectorField,i*verticalSlice,(i+1)*verticalSlice-1,listCopy,numFinished_));			}
		}
		
		try {
			taskExecutor_.invokeAll(tasks);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void stopCalculatingGravity(boolean vectorField) {
		// Wait for all to finish
		if (vectorField) {
			synchronized (vectorFieldFutures_) {
				while (!vectorFieldFutures_.isEmpty()) {
					try {
							vectorFieldFutures_.poll().get(2,TimeUnit.SECONDS);
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					}
					catch (Exception e) {
						
					}
				}
			}
		}
		else {
			synchronized (gravityFieldFutures_) {
				while (!gravityFieldFutures_.isEmpty()) {
					try {
						gravityFieldFutures_.poll().get();
						
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public void updateFields() {
		numFinished_ = new FinishedCount();
		boolean started_ = false;
		if ((showingSmoothField_ || showingPotentialField_) && (running_ || redrawField_ || scaleChanged_)) {
			//synchronized (smoothGravityField_) {
			
			synchronized (smoothGravityField_) {
				smoothGravityField_.resetField();
				startCalculatingGravity(false);
				started_ = true;
			}
				
		}
		if (showingVectorField_ && (running_ || redrawField_ || scaleChanged_)) {
			synchronized (vectorGravityField_) {
				vectorGravityField_.resetField();
				startCalculatingGravity(true);
			}
		}
		if (redrawField_)
			redrawField_ = false;
		if (!showingHintText_ || changingHintText_) {
			hintTextAlpha_ -= 0.03;
			if (hintTextAlpha_ <= 0) {
				hintTextAlpha_ = 0;
				
				changingHintText_ = false;
			}
		}
		else if (showingHintText_) {
			hintText_ = nextHintText_;
			hintTextAlpha_ += 0.03;
			if (hintTextAlpha_ >= 1)
				hintTextAlpha_ = 1;
		}

		synchronized (smoothGravityField_) {
			stopCalculatingGravity(false);
		}
		synchronized (vectorGravityField_) {
			stopCalculatingGravity(true);
		}

	}
	
	public void runCalculations() {
		synchronized (bodies_) {
			long startTime = System.currentTimeMillis();
				scale_ += (scaleTarget_-scale_)*0.05;
				if (Math.abs(scaleTarget_-scale_) < .002)
					scale_ = scaleTarget_;
				if (scale_ != lastScale_)
					scaleChanged_ = true;
				else
					scaleChanged_ = false;
				if (running_) {
					for (int i = 0; i < numIterations_ ; i++) {
						for (Body b: bodies_) {
							if (running_ && !(b == movingBody_ || (b == selectedBody_ && holdingOnSelectedBody_))) {
								b.updateAccel(bodies_,numIterations_,gravityConstant_,usingCharges_);
								b.updateVel(bodies_,numIterations_,gravityConstant_,usingCharges_);
							}
							if (i == 0 && showingAccelVectors_ && (b == movingBody_ || (b == selectedBody_ && holdingOnSelectedBody_))) {
								b.updateAccel(bodies_,numIterations_,gravityConstant_,usingCharges_);
							}
						}
						for (Body b: bodies_) {
							if (!(b == selectedBody_ && holdingOnSelectedBody_))
								b.updatePos(numIterations_,tailLength_,showingTrails_);
						}
					}
				}
			if (DISPLAY_FRAME_TIMES) {
				lastUpdateFrames_.add((double)(System.currentTimeMillis() - startTime));
				while (lastUpdateFrames_.size() >= 30)
					lastUpdateFrames_.remove(0);
				elapsedTimeUpdate_ = 0;
				for (Double d : lastUpdateFrames_) {
					elapsedTimeUpdate_ += d;
				}
				elapsedTimeUpdate_ /= lastUpdateFrames_.size();
			}
			
			
			updateMouse();
		}
		lastScale_ = scale_;
	}

	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_SPACE) {
			running_ = !running_;
			callback_.setPlayButtonState(running_);
		}
		if (e.getKeyCode() == KeyEvent.VK_1)
			setMode(ClickMode.ADD);
		else if (e.getKeyCode() == KeyEvent.VK_2)
			setMode(ClickMode.SELECT);
		else if (e.getKeyCode() == KeyEvent.VK_3)
			setMode(ClickMode.SET_VEL);
		else if (e.getKeyCode() == KeyEvent.VK_4)
			setMode(ClickMode.CIRC_ORBIT_1);
		else if (e.getKeyCode() == KeyEvent.VK_5)
			setMode(ClickMode.FOLLOW);
		else if (e.getKeyCode() == KeyEvent.VK_6)
			setMode(ClickMode.DEL);
		else if (e.getKeyCode() == KeyEvent.VK_7)
			callback_.clearObjects();
		else if (e.getKeyCode() == KeyEvent.VK_SHIFT)
			toggleShowSmoothField();
		else if (e.getKeyCode() == KeyEvent.VK_CONTROL)
			static_ = true;
		else if (e.getKeyCode() == KeyEvent.VK_DELETE || e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
				if (selectedBody_ == followingBody_) {
					followingBody_ = null;
					draggingCamera_ = false;
				}
				synchronized (bodies_) {
					bodies_.remove(selectedBody_);
				}
				selectedBody_ = null;
				movingBody_ = null;
				callback_.objectSelected(false,0,0,null,false);
			
			setAccelVectorsDirty();
		}

	}

	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_CONTROL)
			static_ = false;
	}
	
	private void updateAccel() {
		for (Body b: bodies_) {
			b.updateAccel(bodies_,numIterations_,gravityConstant_,usingCharges_);
		}
	}
	
	public void updateMouse() {
		if (actualMousePos_ == null)
			return;
		mousePos_ = screenPosToReal(Vector2D.sub(actualMousePos_,mouseOffset_));
		synchronized (bodies_) {
			if (draggingCamera_) {  
				cameraPos_.add(Vector2D.scale(Vector2D.sub(actualMousePos_, lastMousePos_),1/scale_));
				lastMousePos_ = actualMousePos_.clone();
			}
			else if (leftMouseDown_) {
				switch (mode_) {
				case ADD:
					if  (movingBody_ != null) {
						if (showingGrid_)
							movingBody_.setPos(grid_.snapToGrid(mousePos_,scale_,true));
						else
							movingBody_.setPos(mousePos_);
						movingBody_.clearTail();
						holdingOnSelectedBody_ = true;
					}
					break;
				case SELECT:
					if  (selectedBody_ != null && (Vector2D.sub(mousePos_,mouseDragOffset_).getMagnitudeSq() > 10*10 || movingObjectStartedDragging_) && followingBody_ != selectedBody_) {
						if (showingGrid_)
							selectedBody_.setPos(grid_.snapToGrid(Vector2D.add(Vector2D.sub(mousePos_,mouseDragOffset_),startDragPos_),scale_,true));
						else
							selectedBody_.setPos(Vector2D.add(Vector2D.sub(mousePos_,mouseDragOffset_),startDragPos_));
						selectedBody_.setVel(new Vector2D());
						selectedBody_.clearTail();
						holdingOnSelectedBody_ = true;
						movingObjectStartedDragging_ = true;
					}
					break;
				case SET_VEL:
					if (selectedBody_ != null) {
						if (showingGrid_)
							selectedBody_.setScreenVec(grid_.snapToGrid(Vector2D.sub(mousePos_, selectedBody_.getPos()),scale_,false));
						else
							selectedBody_.setScreenVec(Vector2D.sub(mousePos_, selectedBody_.getPos()));
						holdingOnSelectedBody_ = true;
					}
					break;
				case DEL:
					for (int i = 0; i < bodies_.size(); i++) {
						double dist = (realDistToScreen(bodies_.get(i).getRadius()) < 25 ?  25 : realDistToScreen(bodies_.get(i).getRadius()));
						if (Physics.Physics.pointCircleCollision(bodies_.get(i).getPos(), bodies_.get(i).getRadius()+5, mousePos_)) {
							if (bodies_.get(i) == followingBody_) {
								followingBody_ = null;
								draggingCamera_ = false;
							}
							bodies_.remove(i);
							i--;
						}
					}
					break;
				}
			}
			if (!running_ && accelVectorsDirty_) {
				updateAccel();
				accelVectorsDirty_ = false;
			}
			if (selectedBody_ == null && mode_ != ClickMode.ADD) {
				callback_.objectSelected(false,0,0,null,false);
			}
		}
	}
	
	private void setAccelVectorsDirty() {
		accelVectorsDirty_ = true;
		redrawField_ = true;
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		if (followingBody_ == null || (leftMouseDown_ && !rightMouseDown_)) {
			actualMousePos_ = new Vector2D(e.getPoint());
		}
		setAccelVectorsDirty();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		
	}
	
	Vector2D screenPosToReal(Vector2D pos) {
		Point2D.Double result = new Point2D.Double();
		AffineTransform transform = new AffineTransform();
		transform.translate(-(int)cameraPos_.x_, -(int)cameraPos_.y_);
		transform.translate((int)appletWidth_/2, (int)appletHeight_/2);
		transform.scale(1/scale_, 1/scale_);
		transform.translate((int)-appletWidth_/2, (int)-appletHeight_/2);
		
		
		transform.transform(pos.toPoint2D(), result);
		return new Vector2D(result);
	}
	Vector2D realPosToScreen(Vector2D pos) {
		Point2D.Double result = new Point2D.Double();
		AffineTransform transform = new AffineTransform();
		transform.translate((int)appletWidth_/2, (int)appletHeight_/2);
		transform.scale(scale_, scale_);
		transform.translate((int)-appletWidth_/2, (int)-appletHeight_/2);
		transform.translate((int)cameraPos_.x_, (int)cameraPos_.y_);
		
		
		transform.transform(pos.toPoint2D(), result);
		return new Vector2D(result);
	}
	private double screenDistToReal(double dist) {
		return dist/scale_;
	}
	private double realDistToScreen(double dist) {
		return dist*scale_;
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		if (!callback_.checkMouseInputOk())
			return;
		setAccelVectorsDirty();
		callback_.mousePressed();
		actualMousePos_ = new Vector2D(e.getPoint());
		mousePos_ = screenPosToReal(Vector2D.sub(actualMousePos_,mouseOffset_));
		if (showingDragCameraHint_) {
			showingDragCameraHint_ = false;
			showingHintText_ = false;
		}
		synchronized (bodies_) {
			if (e.getButton() == MouseEvent.BUTTON3) {
				rightMouseDown_ = true;
				draggingCamera_ = true;
				lastMousePos_ = actualMousePos_.clone();
			}
			else if (e.getButton() == MouseEvent.BUTTON1) {
				leftMouseDown_ = true;
				selectedBody_ = null;
				movingBody_ = null;
				switch (mode_) {
					case ADD:
						boolean collision = false;
						for (Body b : bodies_) {
							double distFromBody = (realDistToScreen(b.getRadius()) < 15 ? screenDistToReal(15) : b.getRadius());
							if (Physics.Physics.pointCircleCollision(b.getPos(), distFromBody, mousePos_)) {
								collision = true;
								selectedBody_ = b;
								setMode(ClickMode.SELECT);
								mousePressed(e);
								break;
							}
						}
						if (!collision) {
							movingBody_ = new Body(mousePos_, newBodyColor_, null, bodyMass_, bodyRadius_, static_);
							bodies_.add(movingBody_);
							holdingOnSelectedBody_ = true;
						}
						break;
					case SELECT:
						for (Body b : bodies_) {
							double distFromBody = (realDistToScreen(b.getRadius()) < 15 ? screenDistToReal(15) : b.getRadius());
							if (Physics.Physics.pointCircleCollision(b.getPos(), distFromBody, mousePos_)) {
								selectedBody_ = b;
								setMode(ClickMode.SELECT);
								callback_.objectSelected(true,selectedBody_.getMass(),selectedBody_.getRadius(),selectedBody_.getColor(),selectedBody_.isStatic());
								mouseDragOffset_ = mousePos_;	
								startDragPos_ = b.getPos();
								holdingOnSelectedBody_ = true;
								break;
							}
						}
						break;
					case SET_VEL:
						boolean vecFound = false;
						for (Body b : bodies_) {
							double distFromBody = (realDistToScreen(b.getRadius()) < 15 ? screenDistToReal(15) : b.getRadius());
							if (Physics.Physics.pointCircleCollision(b.getPos(), distFromBody, mousePos_)) {
								selectedBody_ = b;
								callback_.objectSelected(true,selectedBody_.getMass(),selectedBody_.getRadius(),selectedBody_.getColor(),selectedBody_.isStatic());
								selectedBody_.setScreenVec(grid_.snapToGrid(Vector2D.sub(mousePos_, selectedBody_.getPos()),scale_,false));
								vecFound = true;
								holdingOnSelectedBody_ = true;
								break;
							}
						}
						for (Body b : bodies_) {
							double distFromBody = (realDistToScreen(b.getRadius()) < 15 ? screenDistToReal(15) : b.getRadius());
							if (Physics.Physics.pointRotatedRectCollision(getGraphics(),mousePos_, b.getScreenVel().getMagnitude()+15, 25, b.getPos(), b.getVel().getDirection())) {
								selectedBody_ = b;
								callback_.objectSelected(true,selectedBody_.getMass(),selectedBody_.getRadius(),selectedBody_.getColor(),selectedBody_.isStatic());
								selectedBody_.setScreenVec(grid_.snapToGrid(Vector2D.sub(mousePos_, selectedBody_.getPos()),scale_,false));
								vecFound = true;
								holdingOnSelectedBody_ = true;
								break;
							}
						}
						break;
					case FOLLOW:
						followingBody_ = null;
						for (Body b : bodies_) {
							double distFromBody = (realDistToScreen(b.getRadius()) < 15 ? screenDistToReal(15) : b.getRadius());
							if (Physics.Physics.pointCircleCollision(b.getPos(), distFromBody, mousePos_)) {
								//selectedBody_ = b;
								followingBody_ = b;
								showingHintText_ = false;
							}
						}
						break;
					case CIRC_ORBIT_1:
						selectedBody_ = null;
						for (Body b : bodies_) {
							double distFromBody = (realDistToScreen(b.getRadius()) < 15 ? screenDistToReal(15) : b.getRadius());
							if (Physics.Physics.pointCircleCollision(b.getPos(), distFromBody, mousePos_)) {
								selectedBody_ = b;
								orbitingBody_ = b;
								setMode(ClickMode.CIRC_ORBIT_2);
							}
						}
						break;
					case CIRC_ORBIT_2:
						for (Body b : bodies_) {
							double distFromBody = (realDistToScreen(b.getRadius()) < 15 ? screenDistToReal(15) : b.getRadius());
							if (b != orbitingBody_ && Physics.Physics.pointCircleCollision(b.getPos(), distFromBody, mousePos_)) {
								orbitingBody_.circularOrbit(b,gravityConstant_);
							}
						}
						setMode(ClickMode.CIRC_ORBIT_1);
						break;
					case DEL:
						for (int i = 0; i < bodies_.size(); i++) {
							double dist = (realDistToScreen(bodies_.get(i).getRadius()) < 25 ?  25 : realDistToScreen(bodies_.get(i).getRadius()));
							if (Physics.Physics.pointCircleCollision(bodies_.get(i).getPos(), bodies_.get(i).getRadius()+5, mousePos_)) {
								if (bodies_.get(i) == followingBody_) {
									followingBody_ = null;
									draggingCamera_ = false;
								}
								bodies_.remove(i);
								i--;
							}
						}
						break;
				}
				if (!running_ && showingAccelVectors_) {
					updateAccel();
				}
				if (selectedBody_ != null && mode_ != ClickMode.FOLLOW && mode_ != ClickMode.SELECT)
					selectedBody_.clearTail();
				if (selectedBody_ == null && mode_ != ClickMode.ADD) {
					callback_.objectSelected(false,0,0,null,false);
				}
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		setAccelVectorsDirty();
		if (e.getButton() == MouseEvent.BUTTON3) {
			rightMouseDown_ = false;
			draggingCamera_ = false;
		}
		else if (e.getButton() == MouseEvent.BUTTON1) {
			leftMouseDown_ = false;
			movingObjectStartedDragging_ = false;
			movingBody_ = null;
			holdingOnSelectedBody_ = false;
			if (!running_ && showingAccelVectors_) {
				updateAccel();
			}
			if (selectedBody_ != null && mode_ != ClickMode.FOLLOW && mode_ != ClickMode.SELECT) {
				selectedBody_.clearTail();
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}
 
	public void changePlayState() {
		running_ = !running_;
		callback_.setPlayButtonState(running_);
	}

	public void colorButtonPressed(Color background) {
		if (selectedBody_ == null)
			newBodyColor_ = background;
		else
			selectedBody_.setColor(new Color(background.getRGB()));
	}

	public void setScaleTarget(double scaleTarget) {
		scaleTarget_ = scaleTarget;
	}

	public ClickMode getMode() {
		return mode_;
	}

	public void setMode(ClickMode mode) {
		setAccelVectorsDirty();
		mode_ = mode;
		if (mode_ != ClickMode.ADD) {
			if (selectedBody_ == null)
				callback_.objectSelected(false,0,0,null,false);
			else
				callback_.objectSelected(true,selectedBody_.getMass(),selectedBody_.getRadius(),selectedBody_.getColor(),selectedBody_.isStatic());
		}
		else {
			selectedBody_ = null;			
			callback_.objectSelected(true,bodyMass_,bodyRadius_,newBodyColor_,static_);
		}
		if (mode_ == ClickMode.FOLLOW) {
			showingHintText_ = true;
			changingHintText_ = true;
			nextHintText_ = "click an obect to follow it; click nothing to stop following";
		}
		else if (mode_ == ClickMode.CIRC_ORBIT_1) {
			showingHintText_ = true;
			changingHintText_ = true;
			nextHintText_ = "click an object that you want to orbit another";
		}
		else if (mode_ == ClickMode.CIRC_ORBIT_2) {
			showingHintText_ = true;
			changingHintText_ = true;
			nextHintText_ = "click the object for it to orbit";
		}
		else {
			showingHintText_ = false;
		}
		callback_.clickStateChanged(mode_);
	}

	public void clearBodies() {
		setAccelVectorsDirty();
		bodies_.clear();
	}

	public boolean isDrawing() {
		synchronized (drawing_) {
			return drawing_;
		}
	}

	public void setMass(double n) {
		bodyMass_ = n;
	}

	public void setRadius(double n) {
		bodyRadius_ = n;
	}

	public double getMass() {
		return bodyMass_;
	}
	public double getRadius() {
		return bodyRadius_;
	}
	public void setGravityConstant(double constant) {
		setAccelVectorsDirty();
		gravityConstant_ = constant;
		smoothGravityField_.setGravityConstant(gravityConstant_);
		vectorGravityField_.setGravityConstant(gravityConstant_);
	}
	
	public double getGravityConstant() {
		return gravityConstant_;
	}

	public void setUsingCharges(boolean usingCharges) {
		usingCharges_ = usingCharges;
		setAccelVectorsDirty();
		callback_.setChargeSelected(usingCharges_);
	}

	public void toggleShowVelVectors() {
		setAccelVectorsDirty();
		showingVelVectors_ = !showingVelVectors_;
		callback_.setShowVelVectors(showingVelVectors_);
	}

	public void toggleShowAccelVectors() {
		setAccelVectorsDirty();
		showingAccelVectors_ = !showingAccelVectors_;
		callback_.toggleShowAccelVectors(showingAccelVectors_);
	}

	public void toggleShowVectorField() {
		setAccelVectorsDirty();
		showingVectorField_ = !showingVectorField_;
		callback_.toggleShowVectorField(showingVectorField_);
	}

	public void toggleShowSmoothField() {
		setAccelVectorsDirty();
		showingSmoothField_ = !showingSmoothField_;
		showingPotentialField_ = false;
		synchronized(smoothGravityField_) {
			smoothGravityField_.setPotentialField(false);
		}
		callback_.toggleShowPotentialField(false);
		callback_.toggleShowSmoothField(showingSmoothField_);
	}

	public void togglePotentialField() {
		setAccelVectorsDirty();
		showingPotentialField_ = !showingPotentialField_;
		showingSmoothField_  = false;
		synchronized(smoothGravityField_) {
			smoothGravityField_.setPotentialField(true);
		}
		callback_.toggleShowPotentialField(showingPotentialField_);
		callback_.toggleShowSmoothField(false);
	}
	
	public void toggleShowGrid() {
		setAccelVectorsDirty();
		showingGrid_ = !showingGrid_;
		callback_.toggleShowGrid(showingGrid_);
	}

	public void toggleShowTrails() {
		setAccelVectorsDirty();
		showingTrails_ = !showingTrails_;
		for (Body b: bodies_) {
			b.clearTail();
		}
		callback_.toggleShowTrails(showingTrails_);
	}

	public void setCurrentMass(double n) {
		setAccelVectorsDirty();
		if (selectedBody_ != null)
			selectedBody_.setMass(n);
	}

	public void setCurrentRadius(double n) {
		setAccelVectorsDirty();
		if (selectedBody_ != null)
			selectedBody_.setRadius(n);
	}

	public void setUseBilinear(boolean useBilinear) {
		setAccelVectorsDirty();
		useBilinear_ = useBilinear;
	}
	public void resizeSmoothField(int resolution) { 
		setAccelVectorsDirty();
		smoothGravityField_ = new GravityField(false,smoothGravityField_.isPotentialField(),gravityConstant_,appletWidth_/resolution, appletHeight_/resolution, appletWidth_, appletHeight_, resolution);
	}
	public void resizeVectorField(int resolution) {
		setAccelVectorsDirty();
		vectorGravityField_ = new GravityField(true,false,gravityConstant_,appletWidth_/resolution, appletHeight_/resolution, appletWidth_, appletHeight_, resolution);
	}
	public void setAccuracy(int accuracy) {
		setAccelVectorsDirty();
		numIterations_ = accuracy;
	}
	public void setTailLength(int tailLength) {
		setAccelVectorsDirty();
		tailLength_ = tailLength;
	}

	public void toggleStatic() {
		setAccelVectorsDirty();
		if (selectedBody_ == null) {
			static_ = !static_;
			callback_.toggleStatic(static_);
		}
		else {
			selectedBody_.setStatic(!selectedBody_.isStatic());
			callback_.toggleStatic(selectedBody_.isStatic());
		}
	}

	public void setShowingVectorHeads(boolean showingVectorHeads) {
		vectorGravityField_.setShowingVectorHeads(showingVectorHeads);
	}

	public void resizeGrid(int gridWidth) {
		grid_ = new Grid(gridWidth, appletWidth_, appletHeight_);
	}

	public void setInterfaceHidden(boolean state) {
		interfaceHidden_ = !state;
	}

	public Color getNewBodyColor() {
		return newBodyColor_;
	}

	public void windowSizeChanged(int width, int height) {
		setBounds(0,0,width,height);
		appletWidth_ = width;
		appletHeight_= height;
		resizeSmoothField(smoothGravityField_.getResolution());
		resizeVectorField(vectorGravityField_.getResolution());
	}
}
	
	
	
	