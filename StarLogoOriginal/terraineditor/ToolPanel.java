package terraineditor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import starlogoc.Colors;
import starlogoc.PatchManager;

public class ToolPanel extends JPanel implements ActionListener
{
	private static final long serialVersionUID = 0;
	
	//private String[] breeds = {"Turtles"};
	private JToggleButton wallButton = new JToggleButton("Raise Area");
	private JToggleButton trenchButton = new JToggleButton("Lower Area");
	private JToggleButton moundButton = new JToggleButton("Draw Mound");
	private JToggleButton pitButton = new JToggleButton("Draw Crater");
	private JToggleButton levelButton = new JToggleButton("Level Area");
	//private JToggleButton breedButton = new JToggleButton("Add Turtle");
	private JToggleButton paintButton = new JToggleButton("Paint Area");
	private JToggleButton colorButton = new JToggleButton("Color") {
		private static final long serialVersionUID = 1L;
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			((Graphics2D) g).fillRoundRect(4,4,this.getWidth()-8,this.getHeight()-8,3,3);
			g.setColor(Color.BLACK);
			((Graphics2D) g).drawString("Change Color", 15,(this.getHeight()+8)/2);
		}
	};
	//private JToggleButton textureButton = new JToggleButton("Texture");
	private EditPanel editor;
	//private PatchManager pManager;
	//private TextureChooser tc;
	// constructor

	private JFrame window;
	public ToolPanel(EditPanel ed, PatchManager pm, JFrame window) 
	{
		super();
		this.window = window;
		//tc = new TextureChooser(this, "textures/");
		editor = ed;
		//pManager = pm;
		setMinimumSize(new Dimension(100, 300));
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		wallButton.setMaximumSize(new Dimension(200, 150));
		trenchButton.setMaximumSize(new Dimension(200, 150));
		moundButton.setMaximumSize(new Dimension(200, 150));
		pitButton.setMaximumSize(new Dimension(200, 150));
		levelButton.setMaximumSize(new Dimension(200, 150));
		paintButton.setMaximumSize(new Dimension(200, 150));
		//		breedButton.setMaximumSize(new Dimension(200, 150));
		colorButton.setMaximumSize(new Dimension(200, 150));
		colorButton.setBackground(Colors.colorarray[55*32]);
		colorButton.setForeground(Colors.colorarray[55*32]);
		//textureButton.setMaximumSize(new Dimension(200, 150));
		add(wallButton);
		wallButton.addActionListener(this);
		add(trenchButton);
		trenchButton.addActionListener(this);
		add(moundButton);
		moundButton.addActionListener(this);
		add(pitButton);
		pitButton.addActionListener(this);
		add(levelButton);
		levelButton.addActionListener(this);
		add(paintButton);
		paintButton.addActionListener(this);
		//		add(breedButton);
		//		breedButton.addActionListener(this);
		add(colorButton);
		colorButton.addActionListener(this);
		//add(textureButton);
		//textureButton.addActionListener(this);

		// set up to use levelling tool as default
		editor.setTool(editor.LEVEL);
		levelButton.setSelected(true);

		setVisible(true);
	}

	public void actionPerformed(ActionEvent evnt) 
	{
		if (evnt.getSource()==wallButton) 
		{
			editor.setTool(editor.WALL);
			deselectAll();
			wallButton.setSelected(true);
		}
		else if (evnt.getSource()==trenchButton) 
		{
			editor.setTool(editor.TRENCH);
			deselectAll();
			trenchButton.setSelected(true);
		}
		else if (evnt.getSource()==levelButton) 
		{
			editor.setTool(editor.LEVEL);
			deselectAll();
			levelButton.setSelected(true);
		}
		else if (evnt.getSource()==pitButton) 
		{
			editor.setTool(editor.PIT);
			deselectAll();
			pitButton.setSelected(true);
		}
		else if (evnt.getSource()==moundButton) 
		{
			editor.setTool(editor.MOUND);
			deselectAll();
			moundButton.setSelected(true);
		}
			//		else if (evnt.getSource()==breedButton) {
			//		    Object[] possibleValues = breeds;
			//			Object selectedValue = JOptionPane.showInputDialog(null,
			//                "Please select species to add", "Add Species",
			//                JOptionPane.INFORMATION_MESSAGE, null,
			//                possibleValues, null);
			//			String breedName = (String) selectedValue;
			//			// only select tool if they chose a breed
			//			if(breedName != null) {			
			//				editor.setTool(editor.BREED);
			//				editor.setBreed(breedName);
			//				deselectAll();
			//				breedButton.setSelected(true);
			//			}
			//			else {
			//				breedButton.setSelected(false);
			//			}
			//		}
		else if (evnt.getSource()==paintButton) {
			editor.setTool(editor.PAINT);
			deselectAll();
			paintButton.setSelected(true);			
		}
		else if (evnt.getSource()==colorButton) 
		{
			colorButton.setSelected(false);
			repaint();
			ColorDialog cd = new ColorDialog(window);
			cd.setBounds(colorButton.getX(), colorButton.getY(), cd.getWidth(), cd.getHeight());
			cd.setVisible(true);
		    Color color = cd.getChosenColor();

			// do nothing if they don't choose a color
			if (color != null) 
			{
				editor.setColor(cd.getChosenSLColor());
				colorButton.setBackground(color);
				colorButton.setForeground(color);
				colorButton.repaint();
			}
			colorButton.setFocusPainted(false);
			repaint();
		}
//		else if (evnt.getSource()==textureButton) 
//		{
//			//int texNum =
//			tc.showTextureChooser();
//			editor.setTool(editor.TEXTURE);
//			editor.setTexture(1);
//			deselectAll();
//			textureButton.setSelected(true);
//		}
		repaint();
	}

	public void setBreeds(String[] breeds)
	{
		//this.breeds = breeds;
	}


	private void deselectAll() 
	{
		wallButton.setSelected(false);
		trenchButton.setSelected(false);
		moundButton.setSelected(false);
		pitButton.setSelected(false);
		levelButton.setSelected(false);
		//		breedButton.setSelected(false);
		colorButton.setSelected(false);
		paintButton.setSelected(false);
		//textureButton.setSelected(false);
	}
}
