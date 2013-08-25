package gravSim;

import Panel.*;

import gravSim.DisplayPanel.ClickMode;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import Physics.Vector2D;

interface GravSimCallback {
	public void setPlayButtonState(boolean paused);

	public void clickStateChanged(ClickMode clickMode);

	public void mousePressed();

	public void setChargeSelected(boolean usingCharges);

	public void setShowVelVectors(boolean showingVelVectors);

	public void toggleShowAccelVectors(boolean showingAccelVectors);

	public void toggleShowVectorField(boolean showingVectorField);

	public void toggleShowGrid(boolean showingGrid);

	public void toggleShowTrails(boolean showingTrails);

	public void toggleShowSmoothField(boolean showingSmoothField);

	public void objectSelected(boolean objectSelected, double mass, double radius, Color c, boolean stat);

	public void clearObjects(); 

	public void toggleStatic(boolean stat);
	
	public boolean checkMouseInputOk();

	public void toggleShowPotentialField(boolean value);
}


public class GravSim implements Runnable, ActionListener,
		ItemListener, KeyListener, ChangeListener, GravSimCallback,
		MouseWheelListener, DocumentListener, MessagePanelListener, SettingsPanelListener, ComponentListener {

	private DisplayPanel displayPanel_;
	private int appletWidth_;
	private int appletHeight_;
	private JLayeredPane lpane_;

	private JButton playButton_;
	private ImageIcon playIcon_;
	private ImageIcon pauseIcon_;

	private JButton selectButton_;
	private ImageIcon selectIcon_;

	private JButton addButton_;
	private ImageIcon addIcon_;

	private JButton changeVelButton_;
	private ImageIcon changeVelIcon_;

	private JButton followButton_;
	private ImageIcon followIcon_;

	private JButton deleteButton_;
	private ImageIcon deleteIcon_;

	private JButton clearButton_;

	private JButton settingsButton_;
	private ImageIcon settingsIcon_;

	private JButton aboutButton_;

	private ImageIcon staticIcon_;
	private JButton staticButton_;

	private JButton velocityButton_;
	private ImageIcon velocityIcon_;

	private JButton circularOrbitButton_;
	private ImageIcon circularOrbitIcon_;

	private ImageIcon accelerationIcon_;
	private JButton accelerationButton_;

	private JButton vectorFieldButton_;

	private ImageIcon gridIcon_;
	private JButton smoothFieldButton_;
	
	private JButton potentialFieldButton_;

	private JButton gridButton_;

	private ImageIcon trailIcon_;
	private JButton trailButton_;

	private ImageIcon hideInterfaceIcon_;
	private ImageIcon showInterfaceIcon_;
	private JButton toggleInterfaceButton_;

	private JButton massButton_;

	private JButton chargeButton_;

	private JLabel massLabel_;
	private JLabel radiusLabel_;
	private JLabel gravityConstantLabel_;
	private JFormattedTextField massField_;
	private JFormattedTextField radiusField_;
	private JFormattedTextField gravityConstantField_;

	private JLabel currentObjectLabel_;

	private ArrayList<JButton> colorButtons_;
	private JSlider zoomSlider_;
	private SettingsPanel settingsPanel_;
	private MessagePanel confirmClearMsg_;
	private MessagePanel aboutInfoMsg_;
	private MessagePanel errorMsg_;

	private boolean showingInterface_;
	private boolean useBilinear_;
	private Vector2D mouseOffset_;
	private Graphics g_;
	
	private JFrame frame_; 

	private static final int TOGGLE_COLOR = 0xff00ff00;
	private static final int RADIO_SELECTION_COLOR = 0xffffff00;
	private static final int UNSELECTED_COLOR = 0xffaaaaaa;

	public GravSim(int appletWidth, int appletHeight, Vector2D mouseOffset) {
		appletWidth_ = appletWidth;
		appletHeight_ = appletHeight;
		mouseOffset_ = mouseOffset;
	}

	public void prepareControl(JComponent b) {
		b.setBackground(new Color(0xff000000));
		b.setFocusable(false);
		b.setBorder(BorderFactory.createLineBorder(new Color(UNSELECTED_COLOR)));
	}
	
	public void resetBounds() {
		playButton_.setBounds(10, appletHeight_ - 50, 40, 40);
		addButton_.setBounds(appletWidth_ - 330, 20, 40, 40);
		selectButton_.setBounds(appletWidth_ - 285, 20, 40, 40);
		changeVelButton_.setBounds(appletWidth_ - 240, 20, 40, 40);
		circularOrbitButton_.setBounds(appletWidth_ - 195, 20, 40, 40);
		followButton_.setBounds(appletWidth_ - 150, 20, 40, 40);
		deleteButton_.setBounds(appletWidth_ - 105, 20, 40, 40);
		clearButton_.setBounds(appletWidth_ - 60, 20, 40, 40);
		toggleInterfaceButton_.setBounds(12, 20, 40, 40);
		settingsButton_.setBounds(112, 20, 40, 40);
		aboutButton_.setBounds(62, 20, 40, 40);
		chargeButton_.setBounds(10, appletHeight_ - 110, 45, 30);
		massButton_.setBounds(10, appletHeight_ - 150, 45, 30);
		
		int velocityStart = 90;
		velocityButton_.setBounds(appletWidth_ - 60, velocityStart, 40, 40);
		accelerationButton_.setBounds(appletWidth_ - 60, velocityStart + 50,40, 40);		
		vectorFieldButton_.setBounds(appletWidth_ - 60, velocityStart + 100,40, 40);
		smoothFieldButton_.setBounds(appletWidth_ - 60, velocityStart + 150,40, 40);
		potentialFieldButton_.setBounds(appletWidth_ - 60, velocityStart + 200,40, 40);
		trailButton_.setBounds(appletWidth_ - 60, velocityStart + 250, 40, 40);
		gridButton_.setBounds(appletWidth_ - 60, velocityStart + 300, 40, 40);
		staticButton_.setBounds(460, appletHeight_-50, 40, 40);
		confirmClearMsg_.center(300, 100, appletWidth_, appletHeight_);
		settingsPanel_.center(500, 270, appletWidth_, appletHeight_);
		errorMsg_.center(400, 120, appletWidth_, appletHeight_);
		aboutInfoMsg_.center(400, 170, appletWidth_, appletHeight_);
		
		int i = 0;
		for (JButton b : colorButtons_) {
			b.setBounds(290 + i * 20, appletHeight_ - 40, 20, 20);
			i++;
		}
		
		massField_.setBounds(105, appletHeight_ - 40, 60, 20);
		massLabel_.setBounds(60, appletHeight_ - 40, 60, 20);
		radiusField_.setBounds(220, appletHeight_ - 40, 60, 20);
		radiusLabel_.setBounds(175, appletHeight_ - 40, 60, 20);
		gravityConstantField_.setBounds(620, appletHeight_ - 40, 60, 20);
		gravityConstantLabel_.setBounds(515, appletHeight_ - 40, 125, 20);
		currentObjectLabel_.setBounds(70, appletHeight_ - 65, 125, 20);
		zoomSlider_.setBounds(20, appletHeight_ - 465, 20, 300);
	}
	
	public JLayeredPane init(JFrame frame) {

		frame_ = frame;
		lpane_ = new JLayeredPane();

		frame_.setPreferredSize(new Dimension(appletWidth_, appletHeight_));
		frame_.setLayout(new BorderLayout());
		frame_.add(lpane_, BorderLayout.CENTER);
		
		playIcon_ = new ImageIcon(getClass().getResource("/res/Play.png"));
		pauseIcon_ = new ImageIcon(getClass().getResource("/res/Pause.png"));
		playButton_ = new JButton(playIcon_);
		prepareControl(playButton_);
		playButton_.addActionListener(this);
		playButton_.setToolTipText("play");

		addIcon_ = new ImageIcon(getClass().getResource("/res/Add.png"));
		addButton_ = new JButton(addIcon_);
		prepareControl(addButton_);
		addButton_.setToolTipText("add objects");
		addButton_.setBorder(BorderFactory.createLineBorder(new Color(
				RADIO_SELECTION_COLOR), 2));
		addButton_.addActionListener(this);

		selectIcon_ = new ImageIcon(getClass().getResource("/res/Pointer.png"));
		selectButton_ = new JButton(selectIcon_);
		prepareControl(selectButton_);
		selectButton_.setToolTipText("select and move objects");
		selectButton_.addActionListener(this);

		changeVelIcon_ = new ImageIcon(getClass().getResource("/res/ChangeVel.png"));
		changeVelButton_ = new JButton(changeVelIcon_);
		prepareControl(changeVelButton_);
		changeVelButton_.addActionListener(this);
		changeVelButton_.setToolTipText("change velocity objects");

		circularOrbitIcon_ = new ImageIcon(getClass().getResource("/res/Circular_Orbit.png"));
		circularOrbitButton_ = new JButton(circularOrbitIcon_);
		prepareControl(circularOrbitButton_);
		circularOrbitButton_.addActionListener(this);
		circularOrbitButton_.setToolTipText("set object on a circular orbit");

		followIcon_ = new ImageIcon(getClass().getResource("/res/Follow.png"));
		followButton_ = new JButton(followIcon_);
		prepareControl(followButton_);
		followButton_.addActionListener(this);
		followButton_.setToolTipText("center camera on and follow an object");

		deleteIcon_ = new ImageIcon(getClass().getResource("/res/Delete.png"));
		deleteButton_ = new JButton(deleteIcon_);
		prepareControl(deleteButton_);
		deleteButton_.addActionListener(this);
		deleteButton_.setToolTipText("delete objects");

		clearButton_ = new JButton("clear");
		prepareControl(clearButton_);
		clearButton_.setForeground(Color.white);
		clearButton_.addActionListener(this);
		clearButton_.setToolTipText("clear away all objects");

		hideInterfaceIcon_ = new ImageIcon(getClass().getResource("/res/Hide_Interface.png"));
		showInterfaceIcon_ = new ImageIcon(getClass().getResource("/res/Show_Interface.png"));
		toggleInterfaceButton_ = new JButton(hideInterfaceIcon_);
		prepareControl(toggleInterfaceButton_);
		toggleInterfaceButton_.addActionListener(this);
		toggleInterfaceButton_.setToolTipText("hide interface");

		settingsIcon_ = new ImageIcon(getClass().getResource("/res/Settings.png"));
		settingsButton_ = new JButton(settingsIcon_);
		prepareControl(settingsButton_);
		settingsButton_.setForeground(Color.white);
		settingsButton_.addActionListener(this);
		settingsButton_.setToolTipText("settings and more options");

		aboutButton_ = new JButton("about");
		prepareControl(aboutButton_);
		aboutButton_.setForeground(Color.white);
		aboutButton_.addActionListener(this);
		aboutButton_.setToolTipText("more about this applet");

		massButton_ = new JButton("mass");
		prepareControl(massButton_);
		massButton_.setForeground(Color.white);
		massButton_.addActionListener(this);
		massButton_.setBorder(BorderFactory.createLineBorder(new Color(
				RADIO_SELECTION_COLOR), 2));
		massButton_.setToolTipText("work with masses and gravitational forces");

		chargeButton_ = new JButton("charge");
		prepareControl(chargeButton_);
		chargeButton_.setForeground(Color.white);
		chargeButton_.addActionListener(this);
		chargeButton_.setToolTipText("work with charges and electrostatic forces");

		velocityIcon_ = new ImageIcon(getClass().getResource("/res/Velocity_Vector.png"));
		velocityButton_ = new JButton(velocityIcon_);
		prepareControl(velocityButton_);
		velocityButton_.setForeground(Color.white);
		velocityButton_.addActionListener(this);
		velocityButton_.setToolTipText("display velocity vectors");

		accelerationIcon_ = new ImageIcon(getClass().getResource("/res/Acceleration_Vector.png"));
		accelerationButton_ = new JButton(accelerationIcon_);
		prepareControl(accelerationButton_);
		accelerationButton_.setForeground(Color.white);

		accelerationButton_.addActionListener(this);
		accelerationButton_.setToolTipText("display acceleration vectors");

		vectorFieldButton_ = new JButton("<html><center>vector<br>field");
		prepareControl(vectorFieldButton_);
		vectorFieldButton_.setForeground(Color.white);

		vectorFieldButton_.addActionListener(this);
		vectorFieldButton_
				.setToolTipText("display the gravitational/electric field using discrete vectors");

		smoothFieldButton_ = new JButton(
				"<html><center><font size=2>smooth<br>field");
		prepareControl(smoothFieldButton_);
		smoothFieldButton_.setForeground(Color.white);
	
		smoothFieldButton_.addActionListener(this);
		smoothFieldButton_
				.setToolTipText("display the gravitational/electric field using a \"smooth\" color gradient");
		
		potentialFieldButton_  = new JButton(
				"<html><center><font size=3>PTNL<br>field");
		prepareControl(potentialFieldButton_);
		potentialFieldButton_.setForeground(Color.white);
		potentialFieldButton_.addActionListener(this);
		potentialFieldButton_
				.setToolTipText("display the gravitational/electric potential field using a \"smooth\" color gradient");

		trailIcon_ = new ImageIcon(getClass().getResource("/res/Trails.png"));
		trailButton_ = new JButton(trailIcon_);
		prepareControl(trailButton_);
		trailButton_.setForeground(Color.white);
		trailButton_.addActionListener(this);
		trailButton_.setToolTipText("display a trail following each object");

		gridIcon_ = new ImageIcon(getClass().getResource("/res/Grid.png"));
		gridButton_ = new JButton(gridIcon_);
		prepareControl(gridButton_);
		gridButton_.setForeground(Color.white);
		gridButton_.addActionListener(this);
		gridButton_.setToolTipText("display and snap objects to a grid");
		
		staticIcon_ = new ImageIcon(getClass().getResource("/res/StaticObject.png"));
		staticButton_ = new JButton(staticIcon_);
		prepareControl(staticButton_);
		staticButton_.setForeground(Color.white);
		staticButton_.addActionListener(this);
		staticButton_.setToolTipText("current object will be static (affect other objects but not move)");
		confirmClearMsg_ = new MessagePanel(
				"Are you sure you want to clear all objects?",
				MessagePanel.ButtonMode.YES_NO);
		confirmClearMsg_.setMessagePanelListener(this);
		confirmClearMsg_.setVisible(false);
		
		settingsPanel_ =  new SettingsPanel();
		settingsPanel_.addSettingsPanelListener(this);
		settingsPanel_.setVisible(false);

		ArrayList<String> messages = new ArrayList<String>();
		messages.add("Designed and programmed by Ethan Jennings.");
		messages.add("Java version: " + System.getProperty("java.version"));
		messages.add("# cores detected: " + Runtime.getRuntime().availableProcessors());
		aboutInfoMsg_ = new MessagePanel(messages, MessagePanel.ButtonMode.OK);
		aboutInfoMsg_.setMessagePanelListener(this);
		aboutInfoMsg_.setVisible(false);

		errorMsg_ = new MessagePanel((ArrayList<String>) (null),
				MessagePanel.ButtonMode.NOTHING);
		errorMsg_.setMessagePanelListener(this);
		errorMsg_.setVisible(false);

		colorButtons_ = new ArrayList<JButton>();
		
		int numColors = 8;
		Color newBodyColor = null;
		for (int i = 0; i < numColors; i++) {
			JButton colorButton = new JButton();
			prepareControl(colorButton);
			if (i == 1)
				colorButton.setBackground(new Color(Color.HSBtoRGB(0.10f, 1,
						1.0f)));
			else if (i == 2)
				colorButton.setBackground(new Color(Color.HSBtoRGB(0.19f, 1,
						1.0f)));
			else
				colorButton.setBackground(new Color(Color.HSBtoRGB((float) i
						/ numColors, 1, 1.0f)));
			colorButton.setBounds(290 + i * 20, appletHeight_ - 40, 20, 20);
			colorButton.addActionListener(this);
			colorButton.setToolTipText("color for current object");
			colorButtons_.add(colorButton);
			if (i == 3) {
				colorButton.setBorder(BorderFactory.createLineBorder(new Color(
						RADIO_SELECTION_COLOR), 2));
				newBodyColor = colorButton.getBackground();
			}
			lpane_.add(colorButton, new Integer(2), 0);
		}

		displayPanel_ = new DisplayPanel(appletWidth_, appletHeight_, this, newBodyColor,mouseOffset_);
		displayPanel_.setBounds(0, 0, appletWidth_, appletHeight_);
		displayPanel_.setVisible(true);
		g_ = displayPanel_.getGraphics();

		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumIntegerDigits(6);
		nf.setMaximumFractionDigits(3);
		nf.setGroupingUsed(true);

		massField_ = new JFormattedTextField(nf);
		massField_.setValue(new Double(displayPanel_.getMass()));
		massField_.setColumns(8);
		massField_.setBorder(BorderFactory.createLineBorder(new Color(
				UNSELECTED_COLOR)));
		massField_.setBackground(Color.black);
		massField_.setForeground(Color.white);
		massField_.addActionListener(this);
		massField_.getDocument().addDocumentListener(this);
		massField_.setToolTipText("in arbitrary units of mass");

		massLabel_ = new JLabel("mass:");
		massLabel_.setForeground(Color.white);

		radiusField_ = new JFormattedTextField(nf);
		radiusField_.setValue(new Double(displayPanel_.getRadius()));
		radiusField_.setColumns(8);
		radiusField_.setBorder(BorderFactory.createLineBorder(new Color(
				UNSELECTED_COLOR)));
		radiusField_.setBackground(Color.black);
		radiusField_.setForeground(Color.white);
		radiusField_.addActionListener(this);
		radiusField_.getDocument().addDocumentListener(this);
		radiusField_.setToolTipText("in units of pixels");

		radiusLabel_ = new JLabel("radius:");
		radiusLabel_.setForeground(Color.white);

		gravityConstantField_ = new JFormattedTextField(nf);
		gravityConstantField_.setValue(new Double(displayPanel_
				.getGravityConstant()));
		gravityConstantField_.setColumns(8);
		gravityConstantField_.setBorder(BorderFactory
				.createLineBorder(new Color(UNSELECTED_COLOR)));
		gravityConstantField_.setBackground(Color.black);
		gravityConstantField_.setForeground(Color.white);
		gravityConstantField_.addActionListener(this);
		gravityConstantField_.getDocument().addDocumentListener(this);

		gravityConstantLabel_ = new JLabel("gravity constant:");
		gravityConstantLabel_.setForeground(Color.white);

		currentObjectLabel_ = new JLabel("New object:");
		currentObjectLabel_.setForeground(Color.white);

		zoomSlider_ = new JSlider(JSlider.VERTICAL, 0, 500, 500);
		zoomSlider_.setExtent(100);
		prepareControl(zoomSlider_);
		zoomSlider_.setOpaque(false);
		
		zoomSlider_.addChangeListener(this);
		lpane_.add(zoomSlider_, new Integer(2), 0);
		SwingUtilities.invokeLater(new Runnable(){
	        @Override public void run() {
	    		zoomSlider_.setUI(new GravSlider(zoomSlider_,zoomSlider_.getBorder()));
	    		zoomSlider_.setBorder(new EmptyBorder(zoomSlider_.getBorder().getBorderInsets(zoomSlider_)));
	        }
	    });
		
		useBilinear_ = true;

		lpane_.setBounds(0, 0, appletWidth_, appletHeight_);
		lpane_.add(displayPanel_, new Integer(1), 0);
		lpane_.add(playButton_, new Integer(2), 0);
		lpane_.add(addButton_, new Integer(2), 0);
		lpane_.add(selectButton_, new Integer(2), 0);
		lpane_.add(changeVelButton_, new Integer(2), 0);
		lpane_.add(circularOrbitButton_, new Integer(2), 0);
		lpane_.add(followButton_, new Integer(2), 0);
		lpane_.add(deleteButton_, new Integer(2), 0);
		lpane_.add(clearButton_, new Integer(2), 0);
		lpane_.add(staticButton_, new Integer(2),0);
		lpane_.add(velocityButton_, new Integer(2), 0);
		lpane_.add(accelerationButton_, new Integer(2), 0);
		lpane_.add(vectorFieldButton_, new Integer(2), 0);
		lpane_.add(smoothFieldButton_, new Integer(2), 0);
		lpane_.add(potentialFieldButton_, new Integer(2), 0);
		lpane_.add(gridButton_, new Integer(2), 0);
		lpane_.add(trailButton_, new Integer(2), 0);
		lpane_.add(massLabel_, new Integer(2), 0);
		lpane_.add(massField_, new Integer(2), 0);
		lpane_.add(radiusLabel_, new Integer(2), 0);
		lpane_.add(radiusField_, new Integer(2), 0);
		lpane_.add(toggleInterfaceButton_, new Integer(2), 0);
		lpane_.add(settingsButton_, new Integer(2), 0);
		lpane_.add(aboutButton_, new Integer(2), 0);
		lpane_.add(massButton_, new Integer(2), 0);
		lpane_.add(chargeButton_, new Integer(2), 0);
		lpane_.add(confirmClearMsg_, new Integer(2), 0);
		lpane_.add(settingsPanel_, new Integer(2), 0);
		lpane_.add(aboutInfoMsg_, new Integer(2), 0);
		lpane_.add(errorMsg_, new Integer(2), 0);
		lpane_.add(currentObjectLabel_, new Integer(2), 0);
		lpane_.add(gravityConstantField_, new Integer(2), 0);
		lpane_.add(gravityConstantLabel_, new Integer(2), 0);
		frame_.pack();
		showingInterface_ = true;

		Thread th = new Thread(this);
		th.setPriority(Thread.MAX_PRIORITY);
		th.start();

		resetBounds();
		
		return lpane_;
	}
	
	public void resizeWindow(int width, int height) {
		appletWidth_ = width;
		appletHeight_ = height;
		displayPanel_.windowSizeChanged(width,height);
		resetBounds();
	}

	public void setInterfaceHidden(boolean state) {
		showingInterface_ = state;
		playButton_.setVisible(state);
		addButton_.setVisible(state);
		selectButton_.setVisible(state);
		changeVelButton_.setVisible(state);
		followButton_.setVisible(state);
		deleteButton_.setVisible(state);
		clearButton_.setVisible(state);
		velocityButton_.setVisible(state);
		accelerationButton_.setVisible(state);
		vectorFieldButton_.setVisible(state);
		smoothFieldButton_.setVisible(state);
		gridButton_.setVisible(state);
		trailButton_.setVisible(state);
		massLabel_.setVisible(state);
		massField_.setVisible(state);
		radiusLabel_.setVisible(state);
		radiusField_.setVisible(state);
		settingsButton_.setVisible(state);
		aboutButton_.setVisible(state);
		massButton_.setVisible(state);
		chargeButton_.setVisible(state);
		circularOrbitButton_.setVisible(state);
		potentialFieldButton_.setVisible(state);
		zoomSlider_.setVisible(state);
		currentObjectLabel_.setVisible(state);
		gravityConstantField_.setVisible(state);
		gravityConstantLabel_.setVisible(state);
		staticButton_.setVisible(state);
		displayPanel_.setInterfaceHidden(state);
		for (JButton b : colorButtons_) {
			b.setVisible(state);
		}
	}

	@Override
	public void itemStateChanged(ItemEvent arg0) {
	}

	public void resetActionButtons() {
		addButton_.setBorder(BorderFactory.createLineBorder(new Color(
				UNSELECTED_COLOR)));
		selectButton_.setBorder(BorderFactory.createLineBorder(new Color(
				UNSELECTED_COLOR)));
		changeVelButton_.setBorder(BorderFactory.createLineBorder(new Color(
				UNSELECTED_COLOR)));
		followButton_.setBorder(BorderFactory.createLineBorder(new Color(
				UNSELECTED_COLOR)));
		deleteButton_.setBorder(BorderFactory.createLineBorder(new Color(
				UNSELECTED_COLOR)));
		circularOrbitButton_.setBorder(BorderFactory.createLineBorder(new Color(
				UNSELECTED_COLOR)));
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == playButton_) {
			displayPanel_.changePlayState();
		} else if (event.getSource() == addButton_) {
			displayPanel_.setMode(ClickMode.ADD);
		} else if (event.getSource() == selectButton_) {
			displayPanel_.setMode(ClickMode.SELECT);
		} else if (event.getSource() == changeVelButton_) {
			displayPanel_.setMode(ClickMode.SET_VEL);
		} else if (event.getSource() == circularOrbitButton_) {
			displayPanel_.setMode(ClickMode.CIRC_ORBIT_1);
		} else if (event.getSource() == followButton_) {
			displayPanel_.setMode(ClickMode.FOLLOW);
		} else if (event.getSource() == staticButton_) {
			displayPanel_.toggleStatic();
		}
		else if (event.getSource() == deleteButton_) {
			displayPanel_.setMode(ClickMode.DEL);
		} else if (event.getSource() == clearButton_) {
			confirmClearMsg_.setVisible(true);
		} else if (event.getSource() == massButton_) {
			displayPanel_.setUsingCharges(false);
		} else if (event.getSource() == chargeButton_) {
			displayPanel_.setUsingCharges(true);
		} else if (event.getSource() == velocityButton_) {
			displayPanel_.toggleShowVelVectors();
		} else if (event.getSource() == accelerationButton_) {
			displayPanel_.toggleShowAccelVectors();
		} else if (event.getSource() == vectorFieldButton_) {
			displayPanel_.toggleShowVectorField();
		} else if (event.getSource() == smoothFieldButton_) {
			displayPanel_.toggleShowSmoothField();
		} else if (event.getSource() == potentialFieldButton_) {
			displayPanel_.togglePotentialField();
		} else if (event.getSource() == settingsButton_) {
			settingsPanel_.setVisible(true);
		} else if (event.getSource() == aboutButton_) {
			aboutInfoMsg_.setVisible(true);
		} else if (event.getSource() == gridButton_) {
			displayPanel_.toggleShowGrid();
		} else if (event.getSource() == trailButton_) {
			displayPanel_.toggleShowTrails();
		} else if (event.getSource() == toggleInterfaceButton_) {
			setInterfaceHidden(!showingInterface_);
			if (showingInterface_) {
				toggleInterfaceButton_.setIcon(hideInterfaceIcon_);
				toggleInterfaceButton_.setToolTipText("hide interface");
			} else {
				toggleInterfaceButton_.setIcon(showInterfaceIcon_);
				toggleInterfaceButton_.setToolTipText("show interface");
			}
		} else {
			for (JButton b : colorButtons_) {
				if (event.getSource() == b) {
					displayPanel_.colorButtonPressed(b.getBackground());
					b.setBorder(BorderFactory.createLineBorder(b.getBackground().darker().darker().darker(), 3));
				} else {
					b.setBorder(BorderFactory.createLineBorder(new Color(
							UNSELECTED_COLOR)));
				}
			}
		}
	}

	private void setStatsVisible(boolean val) {
		if (showingInterface_) {
			currentObjectLabel_.setVisible(val);
			massLabel_.setEnabled(val);
			massField_.setEnabled(val);
			if (val == false)
				massField_.setText("");
			radiusLabel_.setEnabled(val); 
			radiusField_.setEnabled(val);
			staticButton_.setEnabled(val);
			if (val == false) {
				for (JButton b : colorButtons_) {
					b.setEnabled(val);
					b.setBorder(BorderFactory.createLineBorder(new Color(
							UNSELECTED_COLOR)));
				}
			}
			else {
				for (JButton b : colorButtons_) {
					b.setEnabled(val);
				}
			}
			if (val == false)
				radiusField_.setText("");
		}
	}
	
	@Override
	public void objectSelected(boolean objectSelected, double mass, double radius, Color c, boolean stat) {
		setStatsVisible(objectSelected);
		if (objectSelected) {
			DecimalFormat ft = new DecimalFormat("0.####");
			massField_.setText(ft.format(mass));
			radiusField_.setText(ft.format(radius));
		}
		for (JButton b : colorButtons_) {
			if ((objectSelected && b.getBackground().equals(c))) {
				b.setBorder(BorderFactory.createLineBorder(b.getBackground().darker().darker().darker(), 3));
			} else {
				b.setBorder(BorderFactory.createLineBorder(new Color(UNSELECTED_COLOR)));
			}
		}
		if (objectSelected)
			toggleStatic(stat);
		else
			toggleStatic(false);
	}

	public void clickStateChanged(ClickMode clickMode) {
		resetActionButtons();
		currentObjectLabel_.setText("Current object:");
		switch (clickMode) {
		case ADD:
			currentObjectLabel_.setText("New object:");
			setStatsVisible(true);
			addButton_.setBorder(BorderFactory.createLineBorder(new Color(
					RADIO_SELECTION_COLOR), 2));
			break;
		case SELECT:
			selectButton_.setBorder(BorderFactory.createLineBorder(new Color(
					RADIO_SELECTION_COLOR), 2));
			break;
		case SET_VEL:
			changeVelButton_.setBorder(BorderFactory.createLineBorder(
					new Color(RADIO_SELECTION_COLOR), 2));
			break;
		case FOLLOW:
			followButton_.setBorder(BorderFactory.createLineBorder(new Color(
					RADIO_SELECTION_COLOR), 2));
			break;
		case CIRC_ORBIT_1:
		case CIRC_ORBIT_2:
			circularOrbitButton_.setBorder(BorderFactory.createLineBorder(new Color(
					RADIO_SELECTION_COLOR), 2));
			break;
		case DEL:
			deleteButton_.setBorder(BorderFactory.createLineBorder(new Color(
					RADIO_SELECTION_COLOR), 2));
			break;
		}
	}

	@Override
	public void run() {

		long lastTime = System.currentTimeMillis();
		while (true) {
			int maxWait = 6;
			long waitTime = 0;
			long difference = System.currentTimeMillis() - lastTime;
			waitTime = maxWait - difference;
			if (waitTime < 0)
				waitTime = 0;
			try {
				Thread.sleep(waitTime);
			} catch (InterruptedException ex) {
			}

			synchronized (displayPanel_) {
				lastTime = System.currentTimeMillis();
				int num = (int) Math.ceil(difference / maxWait);
				if (num == 0)
					num = 1;
				for (int i = 0; i < num; i++) {
					displayPanel_.runCalculations();
				}
				displayPanel_.updateFields();
				displayPanel_.repaint();
			}

		}

	}

	@Override
	public void keyPressed(KeyEvent e) {
		synchronized (displayPanel_) {
			displayPanel_.keyPressed(e);
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		synchronized (displayPanel_) {
			displayPanel_.keyReleased(e);
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void setPlayButtonState(boolean paused) {
		playButton_.setIcon((paused ? pauseIcon_ : playIcon_));
		playButton_.setToolTipText((paused ? "pause" : "play"));
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == zoomSlider_) {
			synchronized (displayPanel_) {
				displayPanel_.setScaleTarget(Math.pow(1.004,
						zoomSlider_.getValue() - 500));
			}
		}
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		int notches = e.getWheelRotation();
		zoomSlider_.setValue(zoomSlider_.getValue() - notches * 30);
	}

	@Override
	public void mousePressed() {
		frame_.requestFocus();
	}

	private static String removeCommas(String s) {
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) == ',') {
				s = s.substring(0, i) + s.substring(i + 1, s.length());
				i--;
			}
		}
		return s;
	}

	@Override
	public void changedUpdate(DocumentEvent event) {
		insertUpdate(event);
	}

	@Override
	public void insertUpdate(DocumentEvent event) {
		try {
			if (event.getDocument() == massField_.getDocument()) {
				double n = Double
						.parseDouble(removeCommas(massField_.getText()));
				if (displayPanel_.getMode() == ClickMode.ADD)
					displayPanel_.setMass(n);
				else
					displayPanel_.setCurrentMass(n);
			}
			if (event.getDocument() == radiusField_.getDocument()) {
				double n = Double.parseDouble(removeCommas(radiusField_
						.getText()));
				if (displayPanel_.getMode() == ClickMode.ADD)
					displayPanel_.setRadius(n);
				else
					displayPanel_.setCurrentRadius(n);
			}
			if (event.getDocument() == gravityConstantField_.getDocument()) {
				double n = Double
						.parseDouble(removeCommas(gravityConstantField_
								.getText()));
				displayPanel_.setGravityConstant(n);
			}
		} catch (NumberFormatException e) {
		}
	}

	@Override
	public void removeUpdate(DocumentEvent event) {
	}

	@Override
	public void setChargeSelected(boolean usingCharges) {
		if (!usingCharges) {
			massButton_.setBorder(BorderFactory.createLineBorder(new Color(
					RADIO_SELECTION_COLOR), 2));
			chargeButton_.setBorder(BorderFactory.createLineBorder(new Color(
					UNSELECTED_COLOR)));
			massLabel_.setText("mass:");
			gravityConstantLabel_.setText("gravity constant:");
		} else {
			chargeButton_.setBorder(BorderFactory.createLineBorder(new Color(
					RADIO_SELECTION_COLOR), 2));
			massButton_.setBorder(BorderFactory.createLineBorder(new Color(
					UNSELECTED_COLOR)));
			massLabel_.setText("charge:");
			gravityConstantLabel_.setText("charge constant:");
		}
	}

	@Override
	public void buttonPressed(MessagePanel source, boolean yes, boolean okay) {
		if (source == confirmClearMsg_) {
			confirmClearMsg_.setVisible(false);
			if (yes) {
				synchronized (displayPanel_) {
					displayPanel_.clearBodies();
				}
			}
		} else if (source == aboutInfoMsg_) {
			aboutInfoMsg_.setVisible(false);
		}
	}

	@Override
	public void setShowVelVectors(boolean showingVelVectors) {
		if (showingVelVectors)
			velocityButton_.setBorder(BorderFactory.createLineBorder(new Color(
					TOGGLE_COLOR), 2));
		else
			velocityButton_.setBorder(BorderFactory.createLineBorder(new Color(
					UNSELECTED_COLOR)));
	}

	@Override
	public void toggleShowAccelVectors(boolean showingAccelVectors) {
		if (showingAccelVectors)
			accelerationButton_.setBorder(BorderFactory.createLineBorder(
					new Color(TOGGLE_COLOR), 2));
		else
			accelerationButton_.setBorder(BorderFactory
					.createLineBorder(new Color(UNSELECTED_COLOR)));
	}

	@Override
	public void toggleShowSmoothField(boolean showingSmoothField) {
		if (showingSmoothField)
			smoothFieldButton_.setBorder(BorderFactory.createLineBorder(
					new Color(TOGGLE_COLOR), 2));
		else
			smoothFieldButton_.setBorder(BorderFactory
					.createLineBorder(new Color(UNSELECTED_COLOR)));
	}
	

	@Override
	public void toggleShowPotentialField(boolean value) {
		if (value)
			potentialFieldButton_.setBorder(BorderFactory.createLineBorder(
					new Color(TOGGLE_COLOR), 2));
		else
			potentialFieldButton_.setBorder(BorderFactory
					.createLineBorder(new Color(UNSELECTED_COLOR)));
	}

	@Override
	public void toggleShowVectorField(boolean showingVectorField) {
		if (showingVectorField)
			vectorFieldButton_.setBorder(BorderFactory.createLineBorder(
					new Color(TOGGLE_COLOR), 2));
		else
			vectorFieldButton_.setBorder(BorderFactory
					.createLineBorder(new Color(UNSELECTED_COLOR)));
	}

	@Override
	public void toggleShowGrid(boolean showingGrid) {
		if (showingGrid)
			gridButton_.setBorder(BorderFactory.createLineBorder(new Color(
					TOGGLE_COLOR), 2));
		else
			gridButton_.setBorder(BorderFactory.createLineBorder(new Color(
					UNSELECTED_COLOR)));
	}

	@Override
	public void toggleShowTrails(boolean showingTrails) {
		if (showingTrails)
			trailButton_.setBorder(BorderFactory.createLineBorder(new Color(
					TOGGLE_COLOR), 2));
		else
			trailButton_.setBorder(BorderFactory.createLineBorder(new Color(
					UNSELECTED_COLOR)));
	}

	@Override
	public void clearObjects() {
		confirmClearMsg_.setVisible(true);
	}

	@Override
	public void dialogClosed(SettingsPanelOptions options, boolean settingsChanged) {
		settingsPanel_.setVisible(false);
		if (settingsChanged) {
			displayPanel_.setUseBilinear(options.useBilinearSmoothing);
			displayPanel_.setAccuracy(options.calculationAccuracy);
			displayPanel_.setTailLength(options.tailLength);
			displayPanel_.resizeVectorField(options.vectorFieldSpacing);
			displayPanel_.resizeSmoothField(options.smoothFieldWidth);
			displayPanel_.setShowingVectorHeads(options.showingVectorHeads);
			displayPanel_.resizeGrid(options.gridWidth);
		}
	}

	@Override
	public void toggleStatic(boolean stat) {
		if (stat) {
		staticButton_.setBorder(BorderFactory.createLineBorder(new Color(
				TOGGLE_COLOR), 2));
		}
		else
		{
			staticButton_.setBorder(BorderFactory.createLineBorder(new Color(
					UNSELECTED_COLOR), 2));
		}
	}

	@Override
	public boolean checkMouseInputOk() {
		return !settingsPanel_.isVisible() && !aboutInfoMsg_.isVisible() && !confirmClearMsg_.isVisible();
	}

	public DisplayPanel getDisplayPanel() {
		return displayPanel_;
	}

	@Override
	public void componentHidden(ComponentEvent e) {
	}

	@Override
	public void componentMoved(ComponentEvent e) {
	}

	@Override
	public void componentResized(ComponentEvent e) {
		resizeWindow(e.getComponent().getWidth()-16, e.getComponent().getHeight()-38);
	}

	@Override
	public void componentShown(ComponentEvent e) {
		
	}

}
