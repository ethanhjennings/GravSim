// TODO: Add generated objects
// TODO: Arrowheads not attached to vector field lines
// TODO: Gravity Constant doesn't affect field lines.
// TODO: Low quality graphics option

package gravSim;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.UIManager;

import Physics.Vector2D;


public class GravSimApplication extends JFrame  {

	private int appletWidth_;
	private int appletHeight_;

	public static void main(String[] args)
	{	
		new GravSimApplication();
	}
	
	GravSimApplication() {
		init();
	}
	
	public void init() {
		
		// Set default font for buttons and labels
		UIManager.put("Button.font",new Font("Sans Serif", Font.BOLD, 10));
		UIManager.put("Label.font", new Font("Sans Serif", Font.BOLD, 10));
		
		
		Vector2D offset = new Vector2D(8,30);
		appletWidth_ = (int) (700);
		appletHeight_ = (int) (550);
		GravSim s = new GravSim(appletWidth_,appletHeight_, offset);
		addWindowListener(
				new WindowAdapter()
					{
						public void windowClosing(WindowEvent e)	{System.exit(0);}
					}
		);
		s.init(this);
		setBounds(0,0,(int)(appletWidth_+16), (int)(appletHeight_+38));
		setMinimumSize(new Dimension(appletWidth_+16, appletHeight_+38));
		setVisible(true);
		requestFocus();
		addKeyListener(s);
		addMouseListener(s.getDisplayPanel());
		addMouseMotionListener(s.getDisplayPanel());
		addMouseWheelListener(s);
		addComponentListener(s);
	}
}
