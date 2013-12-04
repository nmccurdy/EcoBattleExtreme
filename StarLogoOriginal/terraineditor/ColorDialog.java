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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JPanel;

import utility.Utility;

/**
 * This class represents the color chooser dialog. It consists
 * of a ColorChooser (the StarLogo version, not the Java Swing 
 * version) and a set of ok/cancel buttons. This is a light-weight
 * dialog used for changing a patch region's color.
 */
public class ColorDialog extends SLDialog {
	private static final long serialVersionUID = 0;
	
    private JButton bok, bca;
    private ColorChooser chooser;

    private JPanel p2,p3;

    public ColorDialog(Frame owner) {
		super(owner, "Choose a Color", true);
		init(); 
    }

    public ColorDialog(Dialog owner) {
    	super(owner, "Choose a Color", true);
    	init(); 
    }
    
    private void init() {
		chooser = new ColorChooser();
		
		setupComponents();
		setupGBC();
		setupPosition();
		
		initOKCancel(bok, bca);
		initFocus(bok);
		initShortcuts();
    }

    private void setupComponents() {
		bok = new JButton("OK");
		bca = new JButton("Cancel");
		if (!Utility.macosxp) {
		    formatLabel(bok);
		    formatLabel(bca);
		}
    }

    private void setupGBC() {
		getContentPane().setLayout(new BorderLayout());
		p2 = new JPanel();
		p2.setLayout(new GridLayout(1, 2, 4, 4));
		p3 = new JPanel();
		if (Utility.macintoshp) {
		    p2.add(bca);
		    p2.add(bok);
		} else {
		    p2.add(bok);
		    p2.add(bca);
		}
		p3.setLayout(new BorderLayout());
		p3.add(p2, BorderLayout.EAST);
		getContentPane().add(chooser, BorderLayout.NORTH);
		getContentPane().add(p3, BorderLayout.SOUTH);
    }

    private void setupPosition() {
		chooser.setSize(210,48);
		pack();
    }

	public Color getChosenColor() {
		return chooser.passColor;
	}
	
	public int getChosenSLColor() {
		return chooser.curcolor;
    }

    public void cancel() {
	    chooser.passColor = null;
		super.cancel();
    }
    
}