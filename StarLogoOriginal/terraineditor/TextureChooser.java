package terraineditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import torusworld.TorusWorld;

public class TextureChooser {
	private final int IMAGE_SIZE = 100, MAX_FACTOR=4, MAX_SIZE=10, APPROVE_OPTION=0,CANCEL_OPTION=1;
	
	private BufferedImage paletteImage, textureImage, outputImageSize[];
	private BufferedImage blankImage = new BufferedImage(IMAGE_SIZE,IMAGE_SIZE,BufferedImage.TYPE_INT_RGB);
	private int textureFactor;
	
	private String[] paletteNames = {"building", "glass", "grass", "grass2", "road", "rock", "star", "turtle", "water", "water2"};
	private List<BufferedImage> palette;
	
	private PalettePanel palettePanel;
	private OutputPanel outputPanel;
	private JScrollPane outputScroller,paletteScroller;
	private JFileChooser importFileChooser;
	private Component parent;
	private JFrame textureChooserFrame;
	
	private boolean available, approved;
	
	/**
	 * Creates a new TextureChooser for a given Swing Component and Starlogo texture directory
	 * @param parent Component that popped up TextureChooser, will be disabled until showTextureChooser returns
	 * @param textureDirectory starlogo directory that has the basic palette images  
	 */
	public TextureChooser(Component parent, String textureDirectory){
	    //System.out.println("TextureChooser load pictures from " + textureDirectory);
	    this.parent=parent;
		palette = new ArrayList<BufferedImage>();
		for(int i = 0; i < paletteNames.length; i++){
			try{
			    palette.add(ImageIO.read(TorusWorld.class.getResource(textureDirectory + paletteNames[i] + ".png")));
			} catch (IOException ioe){
			    palette.add(blankImage);
			    ioe.printStackTrace();
			} catch (Exception e){
			    palette.add(blankImage);
			    e.printStackTrace();
			}
		}
		paletteImage = new BufferedImage(IMAGE_SIZE*3,IMAGE_SIZE*((palette.size()-1)/3+1),BufferedImage.TYPE_INT_ARGB);
		((Graphics2D)paletteImage.getGraphics()).setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		for(int i=0;i<palette.size();i++){
			BufferedImage img = palette.get(i); 
			paletteImage.getGraphics().drawImage(img.getScaledInstance(IMAGE_SIZE,IMAGE_SIZE,
				Image.SCALE_FAST),IMAGE_SIZE*(i%3),IMAGE_SIZE*(i/3),null);
		}
		
		outputImageSize = new BufferedImage[5];
		outputImageSize[0] = new BufferedImage(64,64,BufferedImage.TYPE_INT_ARGB);
		outputImageSize[1]= new BufferedImage(128,128,BufferedImage.TYPE_INT_ARGB); 
		outputImageSize[2] = new BufferedImage(256,256,BufferedImage.TYPE_INT_ARGB); 
		outputImageSize[3] = new BufferedImage(512,512,BufferedImage.TYPE_INT_ARGB);
		outputImageSize[4] = new BufferedImage(1024,1024,BufferedImage.TYPE_INT_ARGB);
		textureImage = outputImageSize[2];
		
		textureFactor = 1;
		
		textureChooserFrame = new JFrame("Texture Chooser");
		textureChooserFrame.setResizable(false);
		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.add(createPalettePanel(),BorderLayout.WEST);
		contentPane.add(createOutputPanel(),BorderLayout.CENTER);
		contentPane.add(createButtonsPanel(),BorderLayout.SOUTH);
		textureChooserFrame.setContentPane(contentPane);
		
		importFileChooser = new JFileChooser();
		importFileChooser.setFileFilter(new ImageFilter());
	}
	
	/**
	 * Pops up the TextureChooser dialog box, in which the user creates a texture.  If this method returns
	 * 	APPROVE_OPTION, the created texture's BufferedImage and tiling factor are accessible via get methods.
	 * @return the status of the TextureChooser dialog box
	 */
	public int showTextureChooser(){ //synchronized
		available = false;
		approved = false;
		textureChooserFrame.pack();
		textureChooserFrame.setVisible(true);
		textureChooserFrame.setEnabled(true);
		if(parent!=null) {
			parent.setEnabled(false);
			textureChooserFrame.setLocation(Math.max(0,parent.getX()+parent.getWidth()/2-textureChooserFrame.getWidth()/2),
				Math.max(0,parent.getY()+parent.getHeight()/2-textureChooserFrame.getHeight()/2));
		}
		while(!available){
			try{
				wait();
			} catch (InterruptedException e) { 
				System.out.println("interrupted");
			}
		}
		if(parent!=null) parent.setEnabled(true);
		textureChooserFrame.setVisible(false);
		textureChooserFrame.setEnabled(false);
		return approved? APPROVE_OPTION:CANCEL_OPTION;
	}
	
	private synchronized void done(){
		notifyAll();
	}
	
	/**
	 * @return the created texture
	 * @requires last showTextureChooser returned APPROVE_OPTION
	 */
	public BufferedImage getTextureImage(){
		return textureImage;
	}
	
	/**
	 * @return the factor used to draw the created texture
	 * @requires last showTextureChooser returned APPROVE_OPTION
	 */
	public int getTextureFactor(){
		return textureFactor;
	}
	
	private JPanel createPalettePanel(){
		JPanel panel = new JPanel(new BorderLayout());
		JPanel palettePlaceHolderPanel = new JPanel();
		palettePanel = new PalettePanel(palette);
		paletteScroller = new JScrollPane(palettePanel);
		paletteScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		paletteScroller.setPreferredSize(new Dimension(IMAGE_SIZE*3,IMAGE_SIZE*3));
		palettePlaceHolderPanel.add(paletteScroller);
		panel.add(palettePlaceHolderPanel,BorderLayout.CENTER);
		
		JPanel buttons = new JPanel();
		JButton addToTerrain = new JButton("Add to Terrain");
		addToTerrain.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				outputPanel.addTexture(palettePanel.selection);
				outputPanel.repaint();
				
			}
		});
		buttons.add(addToTerrain);
		JButton importTexture = new JButton("Import Texture");
		importTexture.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				int status = importFileChooser.showOpenDialog(textureChooserFrame);
				textureChooserFrame.repaint();
				if(status==JFileChooser.APPROVE_OPTION){
					File f = importFileChooser.getSelectedFile();
					//String suffix = f.getName().substring(f.getName().lastIndexOf("."));
					try{ 
						BufferedImage img =ImageIO.read(f); 
						palette.add(img);
						if(palette.size()/3*IMAGE_SIZE>paletteImage.getHeight()){
							BufferedImage newPalatte = new BufferedImage(paletteImage.getWidth(),paletteImage.getHeight()*2,BufferedImage.TYPE_INT_ARGB);
							newPalatte.setData(paletteImage.getData());
							((Graphics2D)paletteImage.getGraphics()).setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
						}
						paletteImage.getGraphics().drawImage(img.getScaledInstance(IMAGE_SIZE,IMAGE_SIZE,
							Image.SCALE_FAST),IMAGE_SIZE*((palette.size()-1)%3),IMAGE_SIZE*((palette.size()-1)/3),null);
						palettePanel.repaint();
					} catch (IOException ioe) {ioe.printStackTrace();}
				}
			}
		});
		buttons.add(importTexture);
		JButton removeTexture = new JButton("Remove Texture");
		removeTexture.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if(palette.size()>0){
					palette.remove(palettePanel.selection);
					palettePanel.selection=Math.min(palettePanel.selection,palette.size()-1);
					for(int i=palettePanel.selection;i<palette.size()&&i>0;i++){
						BufferedImage img = palette.get(i);
						paletteImage.getGraphics().drawImage(img.getScaledInstance(IMAGE_SIZE,IMAGE_SIZE,
							Image.SCALE_FAST),IMAGE_SIZE*(i%3),IMAGE_SIZE*(i/3),null);
					}
					palettePanel.repaint();
				}
			}
		});
		buttons.add(removeTexture);
		panel.add(buttons,BorderLayout.SOUTH);
		return panel;
	}
	
	private JPanel createOutputPanel(){
		JPanel panel = new JPanel(new BorderLayout());
		panel.setPreferredSize(new Dimension(300,300));
		
		JPanel outputPlaceHolderPanel = new JPanel();
		outputPanel=new OutputPanel(textureFactor,8);
		outputScroller = new JScrollPane(outputPanel);
		outputScroller.setMaximumSize(new Dimension(256,256));
		outputPlaceHolderPanel.add(outputScroller);
		panel.add(outputPlaceHolderPanel,BorderLayout.CENTER);
		
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new BoxLayout(buttonsPanel,BoxLayout.Y_AXIS));
		
		JPanel factorSliderPanel = new JPanel();
		JSlider factorSlider = new JSlider(SwingConstants.HORIZONTAL,0,MAX_FACTOR,textureFactor);
		Hashtable<Integer, JLabel> factorLabelTable = new Hashtable<Integer, JLabel>();
		factorLabelTable.put(new Integer (0), new JLabel("0"));
		factorLabelTable.put(new Integer (1), new JLabel("1"));
		factorLabelTable.put(new Integer (2), new JLabel("2"));
		factorLabelTable.put(new Integer (3), new JLabel("4"));
		factorLabelTable.put(new Integer (4), new JLabel("8"));
		factorSlider.setLabelTable(factorLabelTable);
		factorSlider.setMajorTickSpacing(1);
		factorSlider.setPaintTicks(false);
		factorSlider.setPaintLabels(true);
		factorSlider.setSnapToTicks(true);
		factorSlider.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e) {
				if(outputPanel.factor!=(int)Math.pow(2,((JSlider)e.getSource()).getValue())){
					outputPanel.factor=(int)Math.pow(2,((JSlider)e.getSource()).getValue());
					if(outputPanel.selection>=outputPanel.factor*outputPanel.factor)
						outputPanel.selection=outputPanel.factor*outputPanel.factor-1;
					textureFactor=((JSlider)e.getSource()).getValue();
					outputPanel.subTextureSize=outputPanel.outputSize/outputPanel.factor;
					for(int i=0;i<outputPanel.factor*outputPanel.factor;i++){
						BufferedImage img = palette.get(outputPanel.map[i]);
						textureImage.getGraphics().drawImage(img.getScaledInstance(outputPanel.subTextureSize,outputPanel.subTextureSize,
							Image.SCALE_FAST),outputPanel.subTextureSize*(i%outputPanel.factor),outputPanel.subTextureSize*(i/outputPanel.factor),null);
					}
					outputPanel.repaint();
				}
			}
		});
		factorSliderPanel.add(new JLabel("Factor"));
		factorSliderPanel.add(factorSlider);
		buttonsPanel.add(factorSliderPanel);
		
		JPanel sizeSliderPanel = new JPanel();
		JSlider sizeSlider = new JSlider(SwingConstants.HORIZONTAL,6,MAX_SIZE,8);
		Hashtable<Integer, JLabel> sizeLabelTable = new Hashtable<Integer, JLabel>();
		sizeLabelTable.put(new Integer (6), new JLabel("64"));
		sizeLabelTable.put(new Integer (7), new JLabel("128"));
		sizeLabelTable.put(new Integer (8), new JLabel("256"));
		sizeLabelTable.put(new Integer (9), new JLabel("512"));
		sizeLabelTable.put(new Integer (10), new JLabel("1024"));
		sizeSlider.setLabelTable(sizeLabelTable);
		sizeSlider.setMajorTickSpacing(1);
		sizeSlider.setPaintTicks(false);
		sizeSlider.setPaintLabels(true);
		sizeSlider.setSnapToTicks(true);
		sizeSlider.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e) {
				if(outputPanel.outputSize!=(int)Math.pow(2,((JSlider)e.getSource()).getValue())){
					outputPanel.outputSize=(int)Math.pow(2,((JSlider)e.getSource()).getValue());
					textureImage = outputImageSize[((JSlider)e.getSource()).getValue()-6];
					outputPanel.subTextureSize=outputPanel.outputSize/outputPanel.factor;
					for(int i=0;i<outputPanel.factor*outputPanel.factor;i++){
						BufferedImage img;
						if(outputPanel.map[i]<palette.size())
							img = palette.get(outputPanel.map[i]);
						else
							img = blankImage;
						textureImage.getGraphics().drawImage(img.getScaledInstance(outputPanel.subTextureSize,outputPanel.subTextureSize,
							Image.SCALE_FAST),outputPanel.subTextureSize*(i%outputPanel.factor),outputPanel.subTextureSize*(i/outputPanel.factor),null);
					}
					outputPanel.repaint();
				}
			}
		});
		sizeSliderPanel.add(new JLabel("Size"));
		sizeSliderPanel.add(sizeSlider);
		
		buttonsPanel.add(sizeSliderPanel);
		panel.add(buttonsPanel,BorderLayout.SOUTH);
		return panel;
	}
	
	private JPanel createButtonsPanel(){
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				available=true;
				approved=true;
				done();
			}
		});
		panel.add(okButton);
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				available=true;
				approved=false;
				done();
			}
		});
		panel.add(cancelButton);
		return panel;
	}
	
	public static void main(String[] args) {
		//TextureChooser tc = new TextureChooser(null,"c:/starlogo/starlogo-c/lib/textures/");
		
		
	}
	
	private class PalettePanel extends JPanel{
		private static final long serialVersionUID = 0;
		
		private List<BufferedImage> palette;
		private int selection;
		private PalettePanel(List<BufferedImage> p){
			palette=p;
			selection=0;
			this.addMouseListener(new MouseListener(){
				public void mouseClicked(MouseEvent e) {
					selection = Math.min(3*(e.getY()/IMAGE_SIZE)+(e.getX()/IMAGE_SIZE),palette.size()-1);
					repaint();
				}
				public void mousePressed(MouseEvent e) {}
				public void mouseReleased(MouseEvent e) {}
				public void mouseEntered(MouseEvent e) {}
				public void mouseExited(MouseEvent e) {}
			});
			setPreferredSize(new Dimension(IMAGE_SIZE*3,(int)(Math.ceil(palette.size()/3.0)*IMAGE_SIZE)));
		}
		public void paint(Graphics g){
			setSize(IMAGE_SIZE*3,(int)(Math.ceil(palette.size()/3.0)*IMAGE_SIZE));
			setPreferredSize(new Dimension(IMAGE_SIZE*3,(int)(Math.ceil(palette.size()/3.0)*IMAGE_SIZE)));
			int h=getHeight(),w=getWidth();
			g.setColor(Color.white);
			g.fillRect(0,0,w,h);	
			g.drawImage(paletteImage,0,0,this);
			for(int i = palette.size();i%3>0||i<9;i++){
				g.fillRect(IMAGE_SIZE*(i%3),IMAGE_SIZE*(i/3),IMAGE_SIZE,IMAGE_SIZE);
			}
			
			g.setColor(Color.yellow);
			for(int i=0;i<3;i++)
				g.drawRect(IMAGE_SIZE*(selection%3)+i,IMAGE_SIZE*(selection/3)+i,IMAGE_SIZE-2*i,IMAGE_SIZE-2*i);
		}
	}
	private class OutputPanel extends JPanel{
		private static final long serialVersionUID = 0;
		
		private int factor, outputSize, subTextureSize, selection=0;
		private int[] map;
		
		private OutputPanel(int f, int s){
			factor=(int)Math.pow(2,f);
			outputSize=(int)Math.pow(2,s);
			subTextureSize=outputSize/factor;
			map = new int[(int)Math.pow(2,MAX_FACTOR)*(int)Math.pow(2,MAX_FACTOR)];
			for(int i=0;i<map.length;i++)map[i]=0;
			this.addMouseListener(new MouseListener(){
				public void mouseClicked(MouseEvent e) {
					selection = factor*(e.getY()/(subTextureSize))+(e.getX()/(subTextureSize));
					repaint();
				}
				public void mousePressed(MouseEvent e) {}
				public void mouseReleased(MouseEvent e) {}
				public void mouseEntered(MouseEvent e) {}
				public void mouseExited(MouseEvent e) {}
			});
			setPreferredSize(new Dimension(outputSize,outputSize));
			setSize(outputSize,outputSize);
						
			for(int i=0;i<factor*factor;i++){
				BufferedImage img = palette.get(map[i]);
				textureImage.getGraphics().drawImage(img.getScaledInstance(subTextureSize,subTextureSize,
					Image.SCALE_FAST),subTextureSize*(i%factor),subTextureSize*(i/factor),null);
			}
		}
		
		public void paint(Graphics g){
			subTextureSize=outputSize/factor;
			if(outputSize<=256){
				outputScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
				outputScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
				outputScroller.setSize(outputSize,outputSize);
			}
			else{
				outputScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
				outputScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				outputScroller.setSize(256,	256);
			}
			setPreferredSize(new Dimension(outputSize,outputSize));
			setSize(outputSize,outputSize);			
			int h=getHeight(),w=getWidth();
						
			g.drawImage(textureImage,0,0,this);
			
			g.setColor(Color.black);
			for (int i=0;i<factor;i++){
				g.drawLine(0,i*subTextureSize,w,i*subTextureSize);
				g.drawLine(i*subTextureSize,0,i*subTextureSize,h);
			}
			g.setColor(Color.yellow);
			for(int i=0;i<3;i++)
				g.drawRect(subTextureSize*(selection%factor)+i,
					subTextureSize*(selection/factor)+i,
					subTextureSize-2*i,subTextureSize-2*i);
		}
		private void addTexture(int paletteNumber){
			map[selection]=paletteNumber;
			BufferedImage img = palette.get(paletteNumber);
			textureImage.getGraphics().drawImage(img.getScaledInstance(subTextureSize,subTextureSize,
				Image.SCALE_FAST),subTextureSize*(selection%factor),subTextureSize*(selection/factor),this);
		}
	}
	
	private class ImageFilter extends FileFilter {
		public boolean accept(File f) {
			if (f.isDirectory()) {return true;}

			if(f.getName().lastIndexOf(".")!=-1){
				String extension = f.getName().substring(f.getName().lastIndexOf("."));
				if (extension != null) {
					if (extension.equalsIgnoreCase(".tiff") ||
						extension.equalsIgnoreCase(".tif")	||
						extension.equalsIgnoreCase(".gif")	||
						extension.equalsIgnoreCase(".jpg")	||
						extension.equalsIgnoreCase(".jpeg")	||
						extension.equalsIgnoreCase(".png")	||
						extension.equalsIgnoreCase(".tga")	||
						extension.equalsIgnoreCase(".bmp") ) {
						return true;
					} else {
						return false;
					}
				}
			}
			return false;
		}
		public String getDescription() {
			return "Images";
		}
	}
}
