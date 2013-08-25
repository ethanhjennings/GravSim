package gravSim;

import java.awt.Font;

import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.UIManager;

import Physics.Vector2D;

public class GravSimApplet extends JApplet {
	public void init() {
		
		// Set default font for buttons and labels
		UIManager.put("Button.font",new Font("Sans Serif", Font.BOLD, 12));
		UIManager.put("Label.font", new Font("Sans Serif", Font.BOLD, 12));
		
		resize(700, 550);
		GravSim s = new GravSim(700,550, new Vector2D());
		JFrame f = new JFrame();
		setContentPane(s.init(f));
		setVisible(true);
		addKeyListener(s);
		addMouseListener(s.getDisplayPanel());
		addMouseMotionListener(s.getDisplayPanel());
		addMouseWheelListener(s);
	}
}
