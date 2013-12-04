package torusworld.gui;

import java.util.Vector;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

public class GUIRadioGroup
{
    Vector<GUIButton> buttons = new Vector<GUIButton>();
    
    int selection = 0;
    
    ChangeListener changeListener = null;
    
    ChangeListener internalListener = new ChangeListener()
        {
            // this function is only called for a radiobutton if the button is enabled
            public void stateChanged(ChangeEvent e)
            {
                GUIButton b = (GUIButton) e.getSource();
                setSelection(buttons.indexOf(b));
                if (changeListener != null)
                    changeListener.stateChanged(new ChangeEvent(this));
            }
        };
        
    // returns an ID for the button (offset in vector)
    public int addButton(GUIButton butt)
    {
        int id = buttons.size();
        if (butt.getBehavior() != GUIButton.BEHAVIOR_RADIO)
            return -1;
        buttons.addElement(butt);
        if (butt.isEnabled())
            selection = id;
        butt.setChangeListener(internalListener);
        return id;
    }
    
    public void setChangeListener(ChangeListener l)
    {
        changeListener = l;
    }
    
    public void setSelection(int sel)
    {
        buttons.elementAt(selection).setEnabled(false);
        selection = sel;
        buttons.elementAt(selection).setEnabled(true);
    }
    
    public int getSelection()
    {
        return selection;
    }
    

}
