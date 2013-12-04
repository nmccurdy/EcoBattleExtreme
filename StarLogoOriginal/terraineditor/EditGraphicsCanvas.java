package terraineditor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
//import java.io.*;
//import java.awt.BorderLayout;
//import net.java.games.gluegen.runtime.*; 
//import net.java.games.jogl.*; 
//import net.java.games.jogl.util.*;

//import torusworld.*;
//import starlogoc.*;

public class EditGraphicsCanvas extends JPanel
{
	private static final long serialVersionUID = 0;
	
	// Constant values
	private final int NUM_PATCHES = 101;
	public static final int PPP = EditPanel.PPP; //pixels per patch
	private final Color GRID_COLOR = new Color(0, 100, 50);
	private final Color HIGHLIGHT_COLOR = new Color(200,250,100);

	// Class variables
	private Graphics2D imageGraphics;
	private BufferedImage bImage;
	
	// Constructor
	public EditGraphicsCanvas() 
	{
		setSize(new Dimension((NUM_PATCHES*PPP + 1),(NUM_PATCHES*PPP + 1)));
		setBackground(Color.black);
		bImage = new BufferedImage((NUM_PATCHES*PPP + 1), (NUM_PATCHES*PPP + 1), BufferedImage.TYPE_INT_RGB);
		imageGraphics = bImage.createGraphics();
		drawGridLines();
		setVisible(true);
	}

	public void paintComponent(Graphics g) 
	{	
		// painting is simple for this implementation -
		// all of the methods update an image (bImage)
		// so all paint has to do is display that image. 
		g.drawImage(bImage, 0, 0, this);
	}

	/**
	 * Returns the image that serves as the graphical buffer for this canvas
	 */
	public BufferedImage getImage() 
	{
		return bImage;
	}
	
	/**
	 * Draws the entire grid in the unhighlighted state
	 */
	public void drawGridLines() 
	{
		drawGridRegion(new Region(0, 0, NUM_PATCHES, NUM_PATCHES), GRID_COLOR);
	}	
	
	/**
	 * Highlights a square by drawing a bright outline around it.
	 */
	public void highlightSquare(int x, int y)
	{
		imageGraphics.setColor(HIGHLIGHT_COLOR);
		imageGraphics.drawRect(x*PPP, y*PPP, PPP, PPP);
		repaint(x*PPP, y*PPP, PPP+1, PPP+1);
	}
	
	/**
	 * Unhighlights a square by drawing a grid-colored outline around it.
	 */
	public void unhighlightSquare(int x, int y)
	{
		imageGraphics.setColor(GRID_COLOR);
		imageGraphics.drawRect(x*PPP, y*PPP, PPP, PPP);
		repaint(x*PPP, y*PPP, PPP+1, PPP+1);
	}
	
	/**
	 * Highlights all of the squares in the box from (x1,y1) to (x2,y2), inclusive
	 */
	public void highlightRegion(Region r)
	{
		drawGridRegion(r, HIGHLIGHT_COLOR);
	}

	/**
	 * Unhighlights all of the squares in the box from (x1,y1) to (x2,y2), inclusive
	 */
	public void unhighlightRegion(Region r)
	{
		drawGridRegion(r, GRID_COLOR);
	}
	
	/**
	 * Draws a region of grid lines with the specified color.
	 * Draws a line around each square in the given region, INCLUSIVE.
	 */
	public void drawGridRegion(Region r, Color color)
	{
		// draw lines onto the image	
		imageGraphics.setColor(color);
		for (int i = r.minX; i <= (r.maxX + 1); i++) 
		{
			imageGraphics.drawLine(i*PPP, r.minY*PPP, i*PPP, (r.maxY+1)*PPP);
		}	
		for (int j = r.minY; j <= (r.maxY + 1); j++) 
		{
			imageGraphics.drawLine(r.minX*PPP, j*PPP, (r.maxX+1)*PPP, j*PPP);
		}
		
		// repaint the modified region of the canvas 
		// so the updated image will be displayed
		repaint(r.minX*PPP, r.minY*PPP, (r.maxX-r.minX+1)*PPP+1,(r.maxY-r.minY+1)*PPP+1);
	}
	
	/**
	 * Fills the specified square with the specified color.
	 */
	public void fillSquare(int x, int y, Color color)
	{
		imageGraphics.setColor(color);
		imageGraphics.fillRect(x*PPP+1, y*PPP+1, PPP-1, PPP-1);
		repaint(x*PPP, y*PPP, PPP, PPP);
	}
	
	/**
	 * Fills all the squares in the region (inclusive) with the specified color.
	 * It then draws unhighlighted grid lines around the region.  If other
	 * lines are desired, they must be manually drawn afterwards.
	 */
	public void fillRegion(Region r, Color color) 
	{
		// fill in the region and draw grid lines through it
		imageGraphics.setColor(color);
		imageGraphics.fillRect(r.minX*PPP, r.minY*PPP, (r.maxX-r.minX+1)*PPP, (r.maxY-r.minY+1)*PPP);
		drawGridRegion(r, GRID_COLOR);
	}

	/**
	 * Fills each square in the given region with a color determined
	 * by the height of that patch according to the TerrainManager.
	 */
	public void fillHeights(Region r, TerrainManager tm)
	{
		for (int x = r.minX; x <= r.maxX; x++) {
			for (int y = r.minY; y <= r.maxY; y++) {
				Color heightColor = new Color((float)Math.pow(((tm.getHeight(x,y)+50))/100,1), (float)Math.pow(((tm.getHeight(x,y)+50)/100),.4), (float)Math.pow(((tm.getHeight(x,y)+50)/100),1.9));
				fillSquare(x, y, heightColor);
			}
		}
		drawTurtles(tm); // redraw the turtles
	}

	/**
	 * Fills each square in the given region with the color of
	 * that patch according to the TerrainManager.
	 */
	public void fillColors(Region r, TerrainManager tm)
	{
		for (int x = r.minX; x <= r.maxX; x++) {
			for (int y = r.minY; y <= r.maxY; y++) {
				Color sqrColor = tm.getColor(x,y);
				fillSquare(x, y, sqrColor);
			}
		}
	}

	/**
	 * Fills each square in the given region with a color determined
	 * by taking the height of that patch according to the 
	 * TerrainManager and scaling that patch's color by the height.
	 * Colors get brighter for higher heights.
	 */
	public void fillColorsAndHeights(Region r, TerrainManager tm)
	{
		for (int x = r.minX; x <= r.maxX; x++) 
		{
			for (int y = r.minY; y <= r.maxY; y++) 
			{
				Color sqrColor = tm.getColor(x,y);
				if (!sqrColor.equals(Color.BLACK)) 
				{
					float cComp[] = Color.RGBtoHSB(sqrColor.getRed(), sqrColor.getGreen(), sqrColor.getBlue(), null);
					cComp[2] *= (tm.getHeight(x,y)+50)/100;
					fillSquare(x, y, Color.getHSBColor(cComp[0], cComp[1], cComp[2]));
				} 
				else 
				{
					Color heightColor = new Color((float)Math.pow(((tm.getHeight(x,y)+50))/100,1), (float)Math.pow(((tm.getHeight(x,y)+50)/100),.4), (float)Math.pow(((tm.getHeight(x,y)+50)/100),1.9));
					fillSquare(x, y, heightColor);
				}
			}
		}
	}

	/**
	 * Draws a dot in the center of sqaures with a turtle in them.
	 */
	public void drawTurtles(TerrainManager tm)
	{
		for (int i = 0; i < tm.turtles.size(); i++) {
			int x = ((TerrainManager.TurtlePlaceHolder)tm.turtles.get(i)).x;
			int y = ((TerrainManager.TurtlePlaceHolder)tm.turtles.get(i)).y;
			imageGraphics.setColor(Color.RED);
			imageGraphics.fillRect(x*PPP+PPP/2, y*PPP+PPP/2, 2, 2);
			repaint(x*PPP, y*PPP, PPP, PPP);
		}
	}

	// normally update() paints the background first before calling
	// paint(), but since this just displays an image in that region,
	// there's no need to display the background at all.  In fact it
	// was just causing flickering, which is now gone.
//	public void update(Graphics g) {
//		paint(g);
//	}
}
