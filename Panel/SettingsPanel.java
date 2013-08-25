package Panel;

import gravSim.GravSlider;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.Format;
import java.text.NumberFormat;
 
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel; 
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import Panel.MessagePanel.ButtonMode;

public class SettingsPanel extends JPanel implements ActionListener, ChangeListener, KeyListener, DocumentListener {
	private JLabel accuracySliderLabel_;
	private JSlider accuracySlider_;
	private JFormattedTextField accuracyEdit_;
	
	private JLabel smoothFieldLabel_;
	private JSlider smoothFieldSlider_;
	private JFormattedTextField smoothFieldEdit_;
	
	private JLabel bilinearLabel_;
	private JCheckBox bilinearCheck_;
	
	private JLabel vectorFieldLabel_;
	private JSlider vectorFieldSlider_;
	private JFormattedTextField vectorFieldEdit_;
	
	private JLabel tailLengthLabel_;
	private JSlider tailLengthSlider_;
	private JFormattedTextField tailLengthEdit_;
	
	private JLabel gridWidthLabel_;
	private JSlider gridWidthSlider_;
	private JFormattedTextField gridWidthEdit_;

	private JButton ok_;
	private JButton cancel_;
	
	private SettingsPanelListener listener_;
	private JLabel vectorHeadLabel_;
	private JCheckBox vectorHeadCheck_;
	
	SettingsPanelListener.SettingsPanelOptions lastOptions_;
	private JButton reset_;
	
	public SettingsPanel() {
		setLayout(null);
        setBackground(new Color(0xff000000));
        setBorder(BorderFactory.createLineBorder(new Color(0xffaaaaaa)));
        
        NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumIntegerDigits(3);
		nf.setMaximumFractionDigits(0);
		nf.setGroupingUsed(true);
       
        
        accuracySliderLabel_ =  new JLabel("calculation accuracy:");
        accuracySliderLabel_.setBounds(20,10,300,25);
        accuracySliderLabel_.setForeground(Color.white);
        add(accuracySliderLabel_);
        accuracySlider_ = new JSlider(JSlider.HORIZONTAL, 0, 10, 3);
        accuracySlider_.setBounds(180,10,200,25);
        prepareSlider(accuracySlider_);
        accuracyEdit_ = new JFormattedTextField(nf);
		accuracyEdit_.setBounds(405, 13, 60, 20);
		accuracyEdit_.setToolTipText("iterations of euler's method per frame");
		prepareEdit(accuracyEdit_,8,4);
        
        smoothFieldLabel_ =  new JLabel("smooth field separation:");
        smoothFieldLabel_.setBounds(20,40,300,20);
        smoothFieldLabel_.setForeground(Color.white);
        add(smoothFieldLabel_);
        smoothFieldSlider_ = new JSlider(JSlider.HORIZONTAL, 1, 20, 5);
        smoothFieldSlider_.setBounds(180,40,200,25);
        prepareSlider(smoothFieldSlider_);
        add(smoothFieldSlider_);
        smoothFieldEdit_ = new JFormattedTextField(nf);
		smoothFieldEdit_.setBounds(405, 43, 60, 20);
		smoothFieldEdit_.setToolTipText("pixels per field square");
		prepareEdit(smoothFieldEdit_,5,2);
		
		bilinearLabel_ = new JLabel("use extra smoothing on smooth fields (bilinear interpolation)");
		bilinearLabel_.setBounds(75,73,350,20);
		bilinearLabel_.setForeground(Color.white);
		add(bilinearLabel_);
		bilinearCheck_ = new JCheckBox();
		bilinearCheck_.setBounds(50,73,300,20);
		bilinearCheck_.setBackground(Color.black);
		bilinearCheck_.setForeground(Color.white);
		bilinearCheck_.setOpaque(false);
		bilinearCheck_.setSelected(true);
		add(bilinearCheck_);
		
		vectorFieldLabel_ = new JLabel("vector field separation:");
		vectorFieldLabel_.setBounds(20,100,300,20);
		vectorFieldLabel_.setForeground(Color.white);
		add(vectorFieldLabel_);
		vectorFieldSlider_ = new JSlider(JSlider.HORIZONTAL, 10, 40, 20);
		vectorFieldSlider_.setBounds(180,100,200,25);
		prepareSlider(vectorFieldSlider_);
        vectorFieldEdit_ = new JFormattedTextField(nf);
        vectorFieldEdit_.setBounds(405, 100, 60, 20);
        vectorFieldEdit_.setToolTipText("");
		prepareEdit(vectorFieldEdit_,20,2);
		
		vectorHeadLabel_ = new JLabel("show field vector arrow heads");
		vectorHeadLabel_.setBounds(75,130,350,20);
		vectorHeadLabel_.setForeground(Color.white);
		add(vectorHeadLabel_);
		vectorHeadCheck_ = new JCheckBox();
		vectorHeadCheck_.setBounds(50,130,300,20);
		vectorHeadCheck_.setBackground(Color.black);
		vectorHeadCheck_.setForeground(Color.white);
		vectorHeadCheck_.setOpaque(false);
		vectorHeadCheck_.setSelected(false);
		add(vectorHeadCheck_);
        
		tailLengthLabel_ = new JLabel("tail length:");
		tailLengthLabel_.setBounds(20,150,300,20);
		tailLengthLabel_.setForeground(Color.white);
		add(tailLengthLabel_);
        tailLengthSlider_ = new JSlider(JSlider.HORIZONTAL, 1, 20, 4);
        tailLengthSlider_.setBounds(180,150,200,25);
        prepareSlider(tailLengthSlider_);
        tailLengthEdit_ = new JFormattedTextField(nf);
        tailLengthEdit_.setBounds(405, 150, 60, 20);
        tailLengthEdit_.setToolTipText("");
		prepareEdit(tailLengthEdit_,200,4);
		
		gridWidthLabel_ = new JLabel("grid width:");
		gridWidthLabel_.setBounds(20,180,300,20);
		gridWidthLabel_.setForeground(Color.white);
		add(gridWidthLabel_);
        gridWidthSlider_ = new JSlider(JSlider.HORIZONTAL, 0, 25, 15);
        gridWidthSlider_.setBounds(180,180,200,25);
        prepareSlider(gridWidthSlider_);
        gridWidthEdit_ = new JFormattedTextField(nf);
        gridWidthEdit_.setBounds(405, 180, 60, 20);
        gridWidthEdit_.setToolTipText("");
		prepareEdit(gridWidthEdit_,20,3);
		
		int separation = 120;
		
		ok_ = new JButton("confirm");
		ok_.setBounds((500)/2-separation-100/2,225,100,30);
		prepareButton(ok_);
		
		cancel_ = new JButton("cancel");
		cancel_.setBounds((500)/2-100/2,225,100,30);
		prepareButton(cancel_);
		
		reset_ = new JButton("reset defaults");
		reset_.setBounds((500)/2+separation-100/2,225,100,30);
		prepareButton(reset_);

		resetDefualts();

		lastOptions_ = new SettingsPanelListener.SettingsPanelOptions();
	}
	private void resetDefualts() {
		accuracyEdit_.setText("8");
		smoothFieldEdit_.setText("5");
		vectorFieldEdit_.setText("20");
		tailLengthEdit_.setText("200");
		gridWidthEdit_.setText("15");
	}
	private void prepareButton(JButton btn) {
		btn.setBorder(BorderFactory.createLineBorder(new Color(
    			0xffaaaaaa)));
		btn.setBackground(Color.black);
		btn.setForeground(Color.white);
		btn.setFocusable(false);
		btn.addActionListener(this);
		add(btn);
	}
	class InvokeLaterRunnable implements Runnable {
		private JSlider slider_;
		InvokeLaterRunnable(JSlider slider) {
			slider_ = slider;
		}
		@Override
		public void run() {
			slider_.setUI(new GravSlider(slider_,slider_.getBorder()));
			slider_.setBorder(new EmptyBorder(slider_.getBorder().getBorderInsets(slider_)));
		}
	}
	private void prepareSlider(JSlider slider) {
		prepareControl(slider);
		slider.setOpaque(false);
		slider.addChangeListener(this);
		slider.setMajorTickSpacing(1);
		slider.setPaintTicks(true);
		slider.setSnapToTicks(true);
		slider.addChangeListener(this);
    	add(slider);
    	
		SwingUtilities.invokeLater(new InvokeLaterRunnable(slider));
	}
	private void prepareEdit(JFormattedTextField field, double value, int columns) {
    	
		field.setValue(new Double(value));
		field.setColumns(columns);
    	
		field.setBorder(BorderFactory.createLineBorder(new Color(
    			0xffaaaaaa)));
		field.setBackground(Color.black);
		field.setForeground(Color.white);
		field.addActionListener(this);
    	field.getDocument().addDocumentListener(this);
    	field.setHorizontalAlignment(JFormattedTextField.CENTER);
    	field.addKeyListener(this);
    	field.getDocument().addDocumentListener(this);
    	
    	add(field);
	}
	private void prepareControl(JComponent b) {
		//b.setForeground(new Color(0xffffffff));
        b.setBackground(new Color(0xff000000));
        b.setFocusable(false);
        b.setBorder(BorderFactory.createLineBorder(new Color(0xffaaaaaa)));
	}
	public void setBounds(int x, int y, int width, int height) {
		super.setBounds(x, y, width, height);
	}
	public void center(int width, int height, int appletWidth, int appletHeight) {
		setBounds((appletWidth-width)/2,(appletHeight-height)/2,width,height);
	}
	public void addSettingsPanelListener(SettingsPanelListener listener) {
		listener_ = listener;
	}
	@Override
	public void stateChanged(ChangeEvent e) {
		try {
			if (e.getSource() == accuracySlider_) {
				if (accuracySlider_.getValueIsAdjusting()) {
					int val = accuracySlider_.getValue();
					accuracyEdit_.setText(Integer.toString((int)Math.pow(2, val)));
				}
			}
			if (e.getSource() == smoothFieldSlider_) {
				if (smoothFieldSlider_.getValueIsAdjusting()) {
					int val = smoothFieldSlider_.getValue();
					smoothFieldEdit_.setText(Integer.toString((int)(val)));
				}
			}
			if (e.getSource() == vectorFieldSlider_) {
				if (vectorFieldSlider_.getValueIsAdjusting()) {
					int val = vectorFieldSlider_.getValue();
					vectorFieldEdit_.setText(Integer.toString((int)(val)));
				}
			}
			if (e.getSource() == tailLengthSlider_) {
				if (tailLengthSlider_.getValueIsAdjusting()) {
					int val = tailLengthSlider_.getValue();
					tailLengthEdit_.setText(Integer.toString((int)(val*50)));
				}
			}
			if (e.getSource() == gridWidthSlider_) {
				if (gridWidthSlider_.getValueIsAdjusting()) {
					int val = gridWidthSlider_.getValue();
					gridWidthEdit_.setText(Integer.toString((int)(val+5)));
				}
			}
		} catch (NumberFormatException exception) {
		}
	}
	@Override
	public void insertUpdate(DocumentEvent e) {
		try {
			if (e.getDocument() == accuracyEdit_.getDocument() && !accuracySlider_.getValueIsAdjusting()) {
				int accuracy = Integer.parseInt(accuracyEdit_.getText());
				double sliderVal = Math.log(accuracy)/Math.log(2);
				accuracySlider_.setValue((int)Math.round(sliderVal));
			}
			if (e.getDocument() == smoothFieldEdit_.getDocument() && !smoothFieldSlider_.getValueIsAdjusting()) {
				int spacing = Integer.parseInt(smoothFieldEdit_.getText());
				smoothFieldSlider_.setValue(spacing);
			}
			if (e.getDocument() == vectorFieldEdit_.getDocument() && !vectorFieldSlider_.getValueIsAdjusting()) {
				int spacing = Integer.parseInt(vectorFieldEdit_.getText());
				vectorFieldSlider_.setValue(spacing);
			}
			if (e.getDocument() == tailLengthEdit_.getDocument() && !tailLengthSlider_.getValueIsAdjusting()) {
				int value = Integer.parseInt(tailLengthEdit_.getText());
				value = Math.round(value/50.0f);
				tailLengthSlider_.setValue(value);
			}
			if (e.getDocument() == gridWidthEdit_.getDocument() && !gridWidthSlider_.getValueIsAdjusting()) {
				int value = Integer.parseInt(gridWidthEdit_.getText());
				gridWidthSlider_.setValue(value-5);
			}
		} catch (NumberFormatException exception) {
		}
	}
	@Override
	public void removeUpdate(DocumentEvent e) {
		insertUpdate(e);
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == ok_) {
			SettingsPanelListener.SettingsPanelOptions options = new SettingsPanelListener.SettingsPanelOptions();
			options.calculationAccuracy = Integer.parseInt(accuracyEdit_.getText());
			options.gridWidth = Integer.parseInt(gridWidthEdit_.getText());
			options.smoothFieldWidth = Integer.parseInt(smoothFieldEdit_.getText());
			options.tailLength = Integer.parseInt(tailLengthEdit_.getText());
			options.useBilinearSmoothing = bilinearCheck_.isSelected();
			options.vectorFieldSpacing = Integer.parseInt(vectorFieldEdit_.getText());
			options.showingVectorHeads = vectorHeadCheck_.isSelected();
			listener_.dialogClosed(options, true);
		}
		if (e.getSource() == cancel_) {
			loadOptions();
			SettingsPanelListener.SettingsPanelOptions options = new SettingsPanelListener.SettingsPanelOptions();
			listener_.dialogClosed(options, false);
		}
		if (e.getSource() == reset_) {
			resetDefualts();
		}
	}
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			saveOptions();
		}
	}
	private void saveOptions() {
		lastOptions_.calculationAccuracy = Integer.parseInt(accuracyEdit_.getText());
		lastOptions_.gridWidth = Integer.parseInt(gridWidthEdit_.getText());
		lastOptions_.smoothFieldWidth = Integer.parseInt(smoothFieldEdit_.getText());
		lastOptions_.tailLength = Integer.parseInt(tailLengthEdit_.getText());
		lastOptions_.useBilinearSmoothing = bilinearCheck_.isSelected();
		lastOptions_.vectorFieldSpacing = Integer.parseInt(vectorFieldEdit_.getText());
		lastOptions_.showingVectorHeads = vectorHeadCheck_.isSelected();
	}
	private void loadOptions() {
		accuracyEdit_.setText(Integer.toString(lastOptions_.calculationAccuracy));
		gridWidthEdit_.setText(Integer.toString(lastOptions_.gridWidth));
		smoothFieldEdit_.setText(Integer.toString(lastOptions_.smoothFieldWidth));
		tailLengthEdit_.setText(Integer.toString(lastOptions_.tailLength));
		bilinearCheck_.setSelected(lastOptions_.useBilinearSmoothing);
		vectorFieldEdit_.setText(Integer.toString(lastOptions_.vectorFieldSpacing));
		vectorHeadCheck_.setSelected(lastOptions_.showingVectorHeads);
	}
	@Override
	public void changedUpdate(DocumentEvent e) { // UNIMPLEMENTED
	}
	@Override
	public void keyPressed(KeyEvent e) { // UNIMPLEMENTED
	}
	@Override
	public void keyReleased(KeyEvent e) { // UNIMPLEMENTED
	}
	@Override
	public void keyTyped(KeyEvent e) { // UNIMPLEMENTED

	}
}
