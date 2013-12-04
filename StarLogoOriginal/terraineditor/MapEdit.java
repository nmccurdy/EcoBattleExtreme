package terraineditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import starlogoc.PatchManager;

public class MapEdit extends JPanel implements ActionListener, ComponentListener
{
	private static final long serialVersionUID = 0;
	
	private ToolPanel tools;
	private EditPanel editPanel;
	private HeightSelector heightSelector;
	private PatchManager pManager;
	private JButton updateButton;
	private JButton saveButton;
	private JButton loadButton;
	private JToggleButton colorsView;
	private JToggleButton heightsView;
	private JToggleButton combinedView;
	private int currentTerrain;
    public JFrame window;
	
	public static void main(String s[]) 
	{
		JFrame tempFrame = new JFrame("Terrain Editor [Stand-Alone Mode]");
		tempFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		tempFrame.getContentPane().add(new MapEdit(null, tempFrame));
		tempFrame.pack();
		tempFrame.setVisible(true);
	}
	
	
    public MapEdit(PatchManager pm, JFrame window) {
	super();
	this.window = window;
	pManager = pm;
	currentTerrain = 0;
		addComponentListener(this);
		heightSelector = new HeightSelector();
		editPanel = new EditPanel(pm, heightSelector);
		JPanel centerPanel = new JPanel();		
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.X_AXIS));
		centerPanel.setBackground(Color.BLACK);
		centerPanel.add(/*"West",*/ heightSelector);
		centerPanel.add(/*"Center",*/ editPanel);
		centerPanel.add(javax.swing.Box.createHorizontalGlue());
		
		tools = new ToolPanel(editPanel, pManager, window);
		
		updateButton = new JButton("Swap this terrain into 3D view, and edit the other one");
		updateButton.addActionListener(this);
		saveButton = new JButton("Save current terrain");
		saveButton.addActionListener(this);
		loadButton = new JButton("Load terrain (overwrite this one)");
		loadButton.addActionListener(this);
		JPanel northPanel = new JPanel();
		northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.LINE_AXIS));
		northPanel.add(saveButton);
		northPanel.add(loadButton);
		northPanel.add(updateButton);
		northPanel.add(javax.swing.Box.createHorizontalGlue());

		colorsView = new JToggleButton("Colors Only");
		heightsView = new JToggleButton("Heights Only");
		combinedView = new JToggleButton("Colors, Shaded by Height");
		combinedView.setSelected(true);
		colorsView.addActionListener(this);
		heightsView.addActionListener(this);
		combinedView.addActionListener(this);
		JPanel southPanel = new JPanel();
		southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.LINE_AXIS));
		southPanel.add(javax.swing.Box.createHorizontalGlue());
		southPanel.add(new JLabel("Editor View: "));
		southPanel.add(combinedView);
		southPanel.add(heightsView);
		southPanel.add(colorsView);

		this.setLayout(new BorderLayout());
		this.add("North", northPanel);
		this.add("South", southPanel);
		this.add("Center", centerPanel);
		this.add("West", tools);
		setVisible(true);
	}

	public void setBreeds(String[] breeds)
	{
		tools.setBreeds(breeds);
	}

	public void actionPerformed(ActionEvent evnt) 
	{
		if (evnt.getSource()==updateButton) {
			currentTerrain = (currentTerrain + 1) % 2; // switch between 0 and 1 each time
			editPanel.setPatchTerrain(currentTerrain); // fix me to use a pull down or something
			// so we can choose which terrain we want.
		}
		else if (evnt.getSource()==saveButton) {
			editPanel.save();
		}
		else if (evnt.getSource()==loadButton) {
			editPanel.load();
		}
		else if (evnt.getSource()==colorsView) {
			heightsView.setSelected(false);
			combinedView.setSelected(false);
			colorsView.setSelected(true);
			editPanel.setView(editPanel.COLORS_ONLY);
		}
		else if (evnt.getSource()==heightsView) {
			heightsView.setSelected(true);
			combinedView.setSelected(false);
			colorsView.setSelected(false);
			editPanel.setView(editPanel.HEIGHTS_ONLY);
		}
		else if (evnt.getSource()==combinedView) {
			heightsView.setSelected(false);
			combinedView.setSelected(true);
			colorsView.setSelected(false);
			editPanel.setView(editPanel.COLORS_AND_HEIGHTS);
		}
	}

	public String saveTerrain() {
		return editPanel.saveTerrain();
	}

	public boolean loadTerrain(String data) {
		return editPanel.loadTerrain(data);
	}

	//Component Listener Stuff
	public void componentMoved(ComponentEvent ce)
	{
	}

	public void componentResized(ComponentEvent ce)
	{
		repaint();
	}

	public void componentShown(ComponentEvent ce)
	{
	}

	public void componentHidden(ComponentEvent ce)
	{
	}
}


