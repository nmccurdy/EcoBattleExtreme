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
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.UIManager;
import javax.swing.event.EventListenerList;
import javax.swing.text.JTextComponent;

import torusworld.OkCancelListener;

import utility.Utility;


abstract class SLDialog extends JDialog
{

    // protected Frame parent;

    public static Font TEXT_FONT;
    public static Font LABEL_FONT;
    public OKCancel okcancel;
    private Component focus;
    
    // event listeners to this dialog
    private EventListenerList listeners = new EventListenerList();

    static
    {
        try
        {
            TEXT_FONT = new Font("Monospaced", Font.PLAIN, 12);
            if (Utility.macintoshp)
                TEXT_FONT = Font.getFont("starlogo.text.font", TEXT_FONT);
            LABEL_FONT = new Font("Dialog", Font.PLAIN, 12);
            if (Utility.macintoshp)
                LABEL_FONT = Font.getFont("starlogo.label.font", LABEL_FONT);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    // automaticall adds window closer
    protected SLDialog(Frame owner, String title, boolean modal)
    {
        this(owner, title, modal, false);

    }

    // automaticall adds window closer
    protected SLDialog(Dialog owner, String title, boolean modal)
    {
        this(owner, title, modal, false);
    }

    protected SLDialog(Frame owner, String title, boolean modal,
            boolean resizable)
    {
        super(owner, title, modal);
        // this.parent = parent;
        addWindowListener(new Closer());
        // addWindowListener(new MenuEnabler());
        if (!(resizable || Utility.unixp))
            setResizable(false);
        addNotify();
    }

    protected SLDialog(Dialog owner, String title, boolean modal,
            boolean resizable)
    {
        super(owner, title, modal);
        // this.parent = parent;
        addWindowListener(new Closer());
        // addWindowListener(new MenuEnabler());
        if (!(resizable || Utility.unixp))
            setResizable(false);
        addNotify();
    }

    protected SLDialog(String title, boolean modal)
    {
        this(title, modal, false);

    }

    protected SLDialog(String title, boolean modal, boolean resizable)
    {
        super();
        setTitle(title);
        setModal(modal);
        addWindowListener(new Closer());
        if (!(resizable || Utility.unixp))
            setResizable(false);
        addNotify();
    }

    public void addNotify()
    {
        super.addNotify();
        if (Utility.macosxp)
        {
            setBackground(UIManager.getColor("window"));
        } else
        {
            setBackground(SystemColor.control);
        }
    }

    /**
     * Initializes button listener for ok/cancel.
     */
    protected void initOKCancel(JButton bok, JButton bcancel)
    {
        okcancel = new OKCancel(bok, bcancel);
    }

    /**
     * Sets default focus to <focus>.
     */
    protected void initFocus(Component focus)
    {
        // todo: differentiate between focus-grabbing techniques based on VM?
        this.focus = focus;
        FocusGrabber fg = new FocusGrabber(focus);
        addFocusListener(fg);
        addWindowListener(fg);
    }

    /**
     * Initializes key shortcuts.
     */
    protected void initShortcuts()
    {
        addKeyListeners(this, new Shortcuts(this));
    }

    // Size must be set before this function is called.
    protected void initLocation()
    {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int screenwidth = screen.width;
        Dimension dialog = getSize();
        int dialogwidth = dialog.width;
        if (!Utility.solarisp)
            setLocation((screenwidth - dialogwidth) / 2, 60);
    }

    /**
     * Format text areas.
     */
    public static void formatText(JTextComponent tc)
    {
        tc.setFont(TEXT_FONT);
        colorText(tc);
    }

    /**
     * Format labels.
     */
    public static void formatLabel(Component c)
    {
        c.setFont(LABEL_FONT);
    }

    public static void colorText(JTextComponent c)
    {
        c.setBackground(Color.white);
        c.setForeground(Color.black);
    }
    
    public void addOkCancelListener(OkCancelListener l) {
    	listeners.add(OkCancelListener.class, l); 
    }
    
    public void removeOkCancelListener(OkCancelListener l) {
    	listeners.remove(OkCancelListener.class, l); 
    }

    // function to call when canceling out of dialog
    protected void cancel() {
        setVisible(false);
        OkCancelListener[] ls = listeners.getListeners(OkCancelListener.class);
        for (int j = 0; j < ls.length; j++)
            ls[j].cancel(); 
    }

    // function to call when accepting dialog
    protected void ok() {
        setVisible(false);
        OkCancelListener[] ls = listeners.getListeners(OkCancelListener.class);
        for (int j = 0; j < ls.length; j++)
            ls[j].ok(); 
    }

    // adds <a> to <container> and all components of <container>
    protected static void addKeyListeners(Container container, KeyListener key)
    {
        // add to this
        container.addKeyListener(key);
        // add to components in this
        Component[] components = container.getComponents();
        for (int i = 0; i < components.length; i++)
        {
            if (components[i] instanceof Container)
            {
                addKeyListeners((Container) components[i], key);
            } else
            {
                components[i].addKeyListener(key);
            }
        }
    }

    public boolean shortcutsOn()
    {
        return true;
    }

    /** * classes ** */

    // invokes ok/cancel functions upon pressing ok/cancel buttons
    protected class OKCancel implements ActionListener
    {
        public final JButton bok;
        public final JButton bcancel;

        public OKCancel(JButton bok, JButton bcancel)
        {
            this.bok = bok;
            this.bcancel = bcancel;
            if (bok != null)
            {
                bok.addActionListener(this);
            }
            if (bcancel != null)
            {
                bcancel.addActionListener(this);
            }
        }

        public void actionPerformed(ActionEvent evt)
        {
            JButton b = (JButton) evt.getSource();
            if (b == bok)
            {
                ok();
            } else if (b == bcancel)
            {
                cancel();
            }
        }
    }

    // maps return to ok, escape to cancel
    private class Shortcuts extends KeyAdapter
    {

        private SLDialog sld;

        public Shortcuts(SLDialog sl)
        {
            this.sld = sl;
        }

        public void keyPressed(KeyEvent e)
        {
            char ch = e.getKeyChar();
            if ((ch == '\n') || (ch == '\r'))
            {
                if (sld.shortcutsOn())
                {
                    e.consume();
                    ok();
                }
            } else if (ch == '\u001B')
            {
                if (sld.shortcutsOn())
                {
                    e.consume();
                    cancel();
                }
            }
        }
    }

    // calls cancel upon closing window
    private class Closer extends WindowAdapter
    {
        public void windowActivated(WindowEvent evt)
        {
            if (SLDialog.this.okcancel != null)
            {
                JButton defaultp = (SLDialog.this.focus instanceof JButton) ? ((JButton) SLDialog.this.focus)
                        : SLDialog.this.okcancel.bok;
                SLDialog.this.getRootPane().setDefaultButton(defaultp);
            }
        }

        public void windowClosing(WindowEvent evt)
        {
            cancel();
        }
    }

    // grabs focus for given component when dialog is activated
    private class FocusGrabber extends WindowAdapter implements FocusListener
    {
        Component focus;

        public FocusGrabber(Component focus)
        {
            this.focus = focus;
        }

        // SUN needs this one
        public void focusGained(FocusEvent e)
        {
            focus.requestFocus();
        }

        public void focusLost(FocusEvent e)
        {
        	
        }

        // JVIEW needs this one (doesn't send focus event when dialog made
        // active)
        public void windowActivated(WindowEvent evt)
        {
            focus.requestFocus();
        }
    }
}
