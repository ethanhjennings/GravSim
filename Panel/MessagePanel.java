// GravSim - gravSim - MessagePanel.java
// First created Feb 11, 2012 by Ethan Jennings

package Panel;

import java.awt.BorderLayout; 
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SpringLayout;


public class MessagePanel extends JPanel implements ActionListener {
	public enum ButtonMode {OK,YES_NO,NOTHING};
	
	ArrayList<String> messages_;
	JFrame frame_;
	JButton okay_;
	JButton yes_;
	JButton no_;
	ButtonMode buttonMode_;
	MessagePanelListener msgPanelListener_;
	
	public void prepareControl(JComponent b) {
		b.setForeground(new Color(0xffffffff));
        b.setBackground(new Color(0xff000000));
        b.setFocusable(false);
        b.setBorder(BorderFactory.createLineBorder(new Color(0xffaaaaaa)));
	}
	public MessagePanel(ArrayList<String> messages, ButtonMode buttonMode) {
		init(messages,buttonMode);
	}
	public MessagePanel(String message, ButtonMode buttonMode) {
		ArrayList<String> messages = new ArrayList<String>();
		messages.add(message);
		init(messages,buttonMode);
	}
	private void init(ArrayList<String> message, ButtonMode buttonMode) {
		setLayout(null);
		messages_ = message;
		buttonMode_ = buttonMode;
		if (buttonMode_ == ButtonMode.YES_NO) {
			yes_ = new JButton("yes");
			prepareControl(yes_);
			yes_.addActionListener(this);
			no_ = new JButton("no");
			prepareControl(no_);
			no_.addActionListener(this);
	        add(yes_);
	        add(no_);
		}
		else if (buttonMode_ == ButtonMode.OK) {
			okay_ = new JButton("ok");
			prepareControl(okay_);
			okay_.addActionListener(this);
			add(okay_);
		}
		
        setBackground(new Color(0xff000000));
        //setFocusable(false);
        setBorder(BorderFactory.createLineBorder(new Color(0xffaaaaaa)));
	}
	public void setMessagePanelListener(MessagePanelListener listener) {
		msgPanelListener_ = listener;
	}
	public void setMessages(ArrayList<String> messages) {
		messages_ = messages;
	}
	@Override
	public void setBounds(int x, int y, int width, int height) {
		if (buttonMode_ == ButtonMode.YES_NO) {
			yes_.setBounds((int)(width*0.35) - 30, height - 50, 60, 30);
			no_.setBounds((int)(width*0.65) - 30, height - 50, 60, 30);
		}
		else if (buttonMode_ == ButtonMode.OK)
			okay_.setBounds((int)(width*0.5) - 30, height - 50, 60, 30);
		super.setBounds(x, y, width, height);
	}
	protected void paintComponent(Graphics g) {
		super.paintComponents(g);
		g.setColor(new Color(0xff000000));
		g.fillRect(0, 0, getBounds().width, getBounds().height);
		if (buttonMode_ == ButtonMode.YES_NO) {
			yes_.repaint();
			no_.repaint();
		}
		else if (buttonMode_ == ButtonMode.OK)
			okay_.repaint();
		
		
		//((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); 
		//((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setFont(new Font("Arial", Font.PLAIN, 14));
		g.setColor(new Color(0xffffffff));
		for (int i = 0; i < messages_.size(); i++) {
			Rectangle2D messageBounds = g.getFontMetrics().getStringBounds(messages_.get(i), g);
			if (buttonMode_ == ButtonMode.NOTHING)
				g.drawString(messages_.get(i), (int)((getBounds().getWidth()-messageBounds.getWidth())/2), (int)(getBounds().getHeight()*0.6-messageBounds.getHeight()/2+(i-messages_.size()/2)*(messageBounds.getHeight()+5)));
			else
				g.drawString(messages_.get(i), (int)((getBounds().getWidth()-messageBounds.getWidth())/2), (int)(getBounds().getHeight()*0.5-messageBounds.getHeight()/2+(i-messages_.size()/2)*(messageBounds.getHeight()+5)));
		}
	}

	public void center(int width, int height, int appletWidth, int appletHeight) {
		setBounds((appletWidth-width)/2,(appletHeight-height)/2,width,height);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == yes_)
			msgPanelListener_.buttonPressed(this,true,false);
		if (event.getSource() == no_)
			msgPanelListener_.buttonPressed(this,false,false);
		if (event.getSource() == okay_)
			msgPanelListener_.buttonPressed(this,false,true);
	}
}
