/******************************************************************
 * Copyright 2003 by the Massachusetts Institute of Technology.  
 * All rights reserved.
 *
 * Developed by Mitchel Resnick, Andrew Begel, Eric Klopfer, 
 * Michael Bolin, Molly Jones, Matthew Notowidigdo, Sebastian Ortiz,
 * Michael Mandel, Tim Garnett, Max Goldman, Julie Kane, 
 * Russell Zahniser, Weifang Sun, and Robert Tau. 
 *
 * Previous versions also developed by Bill Thies, Vanessa Colella, 
 * Brian Silverman, Monica Linden, Alice Yang, and Ankur Mehta.
 *
 * Developed at the Media Laboratory, MIT, Cambridge, Massachusetts,
 * with support from the National Science Foundation and the LEGO Group.
 *
 * Permission to use, copy, or modify this software and its documentation
 * for educational and research purposes only and without fee is hereby
 * granted, provided that this copyright notice and the original authors'
 * names appear on all copies and supporting documentation.  If
 * individual files are separated from this distribution directory
 * structure, this copyright notice must be included.  For any other uses
 * of this software, in original or modified form, including but not
 * limited to distribution in whole or in part, specific prior permission
 * must be obtained from MIT.  These programs shall not be used,
 * rewritten, or adapted as the basis of a commercial software or
 * hardware product without first obtaining appropriate licenses from
 * MIT.  MIT makes no representations about the suitability of this
 * software for any purpose.  It is provided "as is" without express or
 * implied warranty.
 *
 *******************************************************************/

package terraineditor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import starlogoc.Colors;
//import java.net.*;
//import utility.*;

/**
 * This class represents the color chooser dialog for when a 
 * user wishes to change the color of a region of patches. Colors
 * are represented as a discrete list of colors and shades
 * (as StarLogo only supports a limited number of colors). 
 */
public class ColorChooser extends Component {
	private static final long serialVersionUID = 0;

    protected static final int cellWidth1 = 15;
    protected static final int cellWidth2 = 21;
    protected static final int cellHeight = 24;
    
    private Rectangle clipRect = new Rectangle();
    private Rectangle position;
    protected int colorDown = 0;
    protected int shadeDown = 5;
    protected int curcolor = 5;
    protected Color passColor = new Color(0);
    
    public ColorChooser() {
		position = new Rectangle(0, 0, cellWidth2 * 10, cellHeight * 2); 
		init();
    }

    private void init(){
	addMouseListener(new MouseAdapter() {
		public void mousePressed(MouseEvent e) {
		    if (!position.contains(new Point(e.getX(), e.getY()))) return;
		    int newcolor = -1, newshade = -1;
		    int row = (int)java.lang.Math.floor((e.getY()-position.y)/cellHeight);
		    if (row == 0) {
			newcolor = (int)java.lang.Math.floor((e.getX()-position.x)/cellWidth1);
			setColor(newcolor, shadeDown);
		    }
		    else {
			newshade = (int)java.lang.Math.floor((e.getX()-position.x)/cellWidth2);
			setColor(colorDown, newshade);
		    }
		}
		public void mouseReleased(MouseEvent e) {
		    mousePressed(e);
		}
	    });
	addMouseMotionListener(new MouseMotionAdapter() {
		public void mouseDragged(MouseEvent e) {
		    if (!position.contains(new Point(e.getX(), e.getY()))) return;
		    int newcolor = -1, newshade = -1;
		    int row = (int)java.lang.Math.floor((e.getY()-position.y)/cellHeight);
		    if (row == 0) {
			newcolor = (int)java.lang.Math.floor((e.getX()-position.x)/cellWidth1);
			setColor(newcolor, shadeDown);
		    }
		    else {
			newshade = (int)java.lang.Math.floor((e.getX()-position.x)/cellWidth2);
			setColor(colorDown, newshade);
		    }
		}
	    });

    }
    
    private void addToClipRect(Rectangle r) {
	  	if (clipRect.isEmpty()) {
		    clipRect.setBounds(r);
		} else clipRect = clipRect.union(r);
    }

    private void trim(Rectangle r) {
		// trims r so left boundary is non-negative (otherwise repaint causes bug in jdk1.2)
		if (r.getLocation().x<0)
		    r.setLocation(0, r.getLocation().y);
		if (r.getLocation().y<0) r.setLocation(r.getLocation().x, 0);
    }

    public Dimension getPreferredSize() {
    	return new Dimension(210, cellHeight * 2);
    }

	
    public void setColor(int wholeColor) {
    	if ((wholeColor < 0) || (wholeColor >= Colors.numcolors)) {
    		return;
    	}

		//do a println in the other setcolor of what the curcolor is
	    	setColor((wholeColor / 10), (wholeColor % 10));
		//Graphics g = getGraphics();
		//paint(g);
		//g.dispose();
    } 
    
    public void setColor(int newColor, int newShade) {
		if (newColor == colorDown && newShade == shadeDown) return;
		this.colorDown = newColor;
		this.shadeDown = newShade;
		curcolor = colorDown*10+shadeDown;
		addToClipRect(position);
		repaint(clipRect);
    }
 
    public void paint(Graphics g) {
		g.translate(position.x, position.y);
		//Color oldcolor = g.getColor();
		for (int i = 0; i < Colors.numprimarycolors; i++) {
		    int color = i*10+shadeDown;
		    g.setColor(Colors.colorarray[color * 32]);
		    g.fillRect(i*cellWidth1, 0, cellWidth1, cellHeight);
		}
		Color selectedColor = Colors.colorarray[curcolor * 32];
		passColor = selectedColor;
		int blueComponent = 255 - selectedColor.getBlue();
		int greenComponent = 255 - selectedColor.getGreen();
		int redComponent = 255 - selectedColor.getRed();
		Color highlightColor = new Color(redComponent, greenComponent, blueComponent);
		g.setColor(highlightColor);
		g.drawRect(colorDown*cellWidth1, 0, cellWidth1-1, cellHeight-1);
		for (int i = 0; i < 10; i++) {
		    int color = colorDown*10+i;
		    g.setColor(Colors.colorarray[color * 32]);
		    g.fillRect(i*cellWidth2, cellHeight, cellWidth2, cellHeight);
		}
		//the line below this was commented
		g.setColor(highlightColor);
		g.drawRect(shadeDown*cellWidth2, cellHeight, cellWidth2-1, cellHeight-1);
		
		g.translate(-position.x, -position.y);
		g.setColor(Color.BLACK);
		String colString = Colors.colorNumToName(curcolor);
		int inc = 0;
		if (colString.equals(Colors.doubleToString(curcolor))) {
			while(colString.equals(Colors.doubleToString(curcolor+inc)) && inc < 5) {
				inc++;
				colString = Colors.colorNumToName(curcolor+inc);
			}
			while(colString.equals(Colors.doubleToString(curcolor+inc)) && inc > -5) {
				inc--;
				colString = Colors.colorNumToName(curcolor+inc);
			}
		}
		
		// if inc is positive (meaning we had to add to curColor to get to the basic color) then
		// curColor is basic color - inc.  Similarly, if inc < 0, we need to use a + sign.
		// for example:
		// red = curColor + 3  <-- inc = 3
		// therefore curColor = red - 3.
		if (inc != 0)
			colString += ((inc>0)?" - ":" + ") + Math.abs(inc);
		int fontWidth = g.getFontMetrics().stringWidth(colString);	
		int fontHeight = g.getFontMetrics().getHeight();
		g.drawString(colString, getWidth()-fontWidth-2, cellHeight * 2-(cellHeight-fontHeight)/2);
    }

    public void repaint() {
		Graphics g = getGraphics();
		// set the clip for jview 5.00.3165 (and below?) that otherwise
		// assumes clip is from 0,0 -> 32767, 32767 and ends up not drawing
		// anything in paint
		g.setClip(new Rectangle(getSize()));
		paint(g);
		g.dispose();
    }

    public void repaint(Rectangle clip) {
		trim(clip);
		if (!clip.isEmpty()) {
		    Graphics g = getGraphics();
		    g.setClip(clip);
		    paint(g);
		    g.dispose();
		}
    }
    
} 


