package terraineditor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
//import java.io.*;
//import java.awt.BorderLayout;
//import net.java.games.gluegen.runtime.*; 
//import net.java.games.jogl.*; 
//import net.java.games.jogl.util.*;

//import torusworld.*;
//import starlogoc.*;

public class HeightSelector extends JPanel implements MouseMotionListener, MouseListener
{
	private static final long serialVersionUID = 0;
	
	// Constant values
	private final int NUM_PATCHES = 101;
	public static final int PPP = EditPanel.PPP;
	private final int BORDER_WIDTH = 5;
	private final int HEIGHT = (BORDER_WIDTH*2 + NUM_PATCHES*PPP + 1);
	private final int BOTTOM = HEIGHT - BORDER_WIDTH*2;

	// Class variables
	private Graphics2D imageGraphics;
	private BufferedImage bImage;
	private int heightValue = 0;
	
	// Constructor
	public HeightSelector() 
	{
		setSize(new Dimension(BORDER_WIDTH+15,HEIGHT));
		setPreferredSize(new Dimension(BORDER_WIDTH+15,HEIGHT));
		setBackground(Color.black);
		bImage = new BufferedImage(BORDER_WIDTH+15, HEIGHT, BufferedImage.TYPE_INT_RGB);
		imageGraphics = bImage.createGraphics();
		drawColors(-50,50);
		setHeightValue(-40);
		setVisible(true);

		// listen for mouse events
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	public void paintComponent(Graphics g) 
	{	
		// painting is simple for this implementation -
		// all of the methods update an image (bImage)
		// so all paint has to do is display that image. 
		setSize(new Dimension(BORDER_WIDTH+15,HEIGHT));
		g.drawImage(bImage, 0, 0, this);
	}

	// normally update() paints the background first before calling
	// paint(), but since this just displays an image in that region,
	// there's no need to display the background at all.
//	public void update(Graphics g) 
//	{
//		paint(g);
//	}

	public void setHeightValue(int heightVal) 
	{
		deselectHeight();
		heightValue = heightVal;
		selectHeight();
	}

	public int getHeightValue()
	{
		return heightValue;
	}

	private void deselectHeight() 
	{
		imageGraphics.setColor(new Color(0,0,0));
		imageGraphics.drawRect(BORDER_WIDTH-1, BOTTOM-((heightValue+50)*PPP), 11, PPP);
		imageGraphics.drawRect(BORDER_WIDTH-2, BOTTOM-((heightValue+50)*PPP), 13, PPP);
		imageGraphics.drawRect(BORDER_WIDTH-3, BOTTOM-((heightValue+50)*PPP), 15, PPP);
		repaint();
	}

	private void selectHeight()
	{
		imageGraphics.setColor(new Color(255,140,180));
		imageGraphics.drawRect(BORDER_WIDTH-1, BOTTOM-((heightValue+50)*PPP), 11, PPP);
		imageGraphics.drawRect(BORDER_WIDTH-2, BOTTOM-((heightValue+50)*PPP), 13, PPP);
		imageGraphics.drawRect(BORDER_WIDTH-3, BOTTOM-((heightValue+50)*PPP), 15, PPP);
		repaint();
	}

	// draws the color rectangles for the heights from low to high
	// in the proper part of the color band.
	public void drawColors(int low, int high) 
	{
		for (int i = low; i <= high; i++)
		{
			double c = i;
			Color heightColor = new Color((float)Math.pow(((c+50))/100,1),
				(float)Math.pow(((c+50)/100),.4), (float)Math.pow(((c+50)/100),1.9));
			imageGraphics.setColor(heightColor);
			imageGraphics.fillRect(BORDER_WIDTH, BOTTOM-((i+50)*PPP)+1, 10, PPP-1);
		}	
		repaint();
	}

	// MouseMotionListener methods
	public void mouseMoved(MouseEvent e)
	{
	}
	
	public void mouseDragged(MouseEvent e)
	{
		if (e.getY() > BORDER_WIDTH+1 && e.getY() < BOTTOM+2*PPP) {
			setHeightValue((BOTTOM-e.getY())/PPP-49);
		}
	}

	// MouseListener methods
	public void mouseClicked(MouseEvent e)
	{
	}
	
	public void mouseEntered(MouseEvent e)
	{
	}
	
	public void mouseExited(MouseEvent e)
	{
	}
	
	public void mousePressed(MouseEvent e)
	{
		if (e.getY() > BORDER_WIDTH && e.getY() < BOTTOM+2*PPP) {
			setHeightValue((BOTTOM-e.getY())/PPP-49);
		}
	}
	
	public void mouseReleased(MouseEvent e)
	{
	}
}
