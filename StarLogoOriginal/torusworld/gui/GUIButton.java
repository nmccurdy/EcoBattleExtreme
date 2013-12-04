package torusworld.gui;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.event.MouseEvent;
import java.util.Timer;
import java.util.TimerTask;

public abstract class GUIButton implements GUIElement, GUIDescription
{
    static public final int AUTOREPEAT_INITIAL_DELAY = 250, AUTOREPEAT_DELAY = 33;
    static public final int BEHAVIOR_TOGGLE = 0, BEHAVIOR_RADIO = 1, BEHAVIOR_CLICK = 2, BEHAVIOR_AUTOREPEAT = 3;
  
    protected int x, y;
    protected int width, height;
    protected int behavior;
    
    protected boolean visible = true;
    
    protected boolean hover = false;
    protected boolean pressed = false;
    protected boolean enabled = false; // enabled is toggled for radio/toggle, pressed for autorepeat
    
    private Timer repeatTimer = null;
    private TimerTask repeatTimerTask = null;
    private ActionListener actionListener = null;
    private int actionId = 0, actionIdRepeat = 0;
    private ChangeListener changeListener = null;
    
    private String description;
    
    public GUIButton(int _x, int _y, int _width, int _height, int _behavior)
    {
        x = _x;
        y = _y;
        width = _width;
        height = _height;
        behavior = _behavior;
        description = "";
        if (behavior == BEHAVIOR_AUTOREPEAT)
            repeatTimer = new Timer();
    }
    
    public GUIButton(int _behavior)
    {
        this(0, 0, 0, 0, _behavior);
    }
    
    public void setDescription(String desc) { description = desc; }
    public String getDescription(){ return description; }

    
    public int getBehavior() { return behavior; }
    public int getWidth() { return width; }
    public void setWidth(int newlen) { width = newlen; }
    public int getHeight() { return height; }
    public void setHeight(int newh) { height = newh; }
    public void setWidthHeight(int neww, int newh) { width = neww; height = newh; }
    
    public boolean isEnabled() { return enabled; }
    // setEnabled should be used only to untoggle radio buttons.
    public void setEnabled(boolean en) { enabled = en; }

    public boolean isVisible() { return visible; }
    // setEnabled should be used only to disable radio buttons.
    public void setVisible(boolean vis) { visible = vis; }
    
    public int getX() { return x; }
    public int getY() { return y; }
    public void setPosition(int nx, int ny) { x = nx; y = ny; }
    public void setCenterPosition(int nx, int ny) { setPosition(nx - width/2, ny - height/2); }
    
    public boolean isMouseOver(int mx, int my)
    {
        return visible && (mx >= x) && (mx < x+width) && (my >= y) && (my < y+height);
    }
    
    public void mouseEntered(MouseEvent e)
    {
        hover = true;
    }

    public void mouseExited(MouseEvent e)
    {
        hover = false;
        pressed = false;
        if (behavior == BEHAVIOR_AUTOREPEAT)
        {
            enabled = false;
            if (repeatTimerTask != null) repeatTimerTask.cancel();
        }
    }
    
    public void mousePressed(MouseEvent e)
    {
        pressed = true;
        if (behavior == BEHAVIOR_AUTOREPEAT)
        {
            enabled = true;
            if (actionListener != null)
            {
                actionListener.actionPerformed(new ActionEvent(this, actionId, ""));
                repeatTimerTask = new TimerTask() 
                {
                    public void run()
                    {
                        actionListener.actionPerformed(new ActionEvent(this, actionIdRepeat, ""));
                    }
                };
                repeatTimer.schedule(repeatTimerTask, AUTOREPEAT_INITIAL_DELAY, AUTOREPEAT_DELAY);
            }
        }
    }
    
    public void mouseReleased(MouseEvent e)
    {
        if (!pressed) return;
        switch (behavior)
        {
            case BEHAVIOR_TOGGLE: 
                enabled = !enabled; 
                if (changeListener != null)
                    changeListener.stateChanged(new ChangeEvent(this));
                break;
                
            case BEHAVIOR_RADIO: 
                if (!enabled)
                {
                    enabled = true;
                    if (changeListener != null)
                        changeListener.stateChanged(new ChangeEvent(this));
                }
                break;
                
            case BEHAVIOR_CLICK:
                if (actionListener != null)
                    actionListener.actionPerformed(new ActionEvent(this, actionId, ""));
                break;
                
            case BEHAVIOR_AUTOREPEAT:
                if (repeatTimerTask != null) repeatTimerTask.cancel();
                enabled = false;
                break;
        }
       pressed = false;
    }
    
    public void mouseClicked(MouseEvent e)
    {
    }
    
    public void setActionListener(ActionListener l, int id)
    {
        actionListener = l;
        actionId = id;
        actionIdRepeat = id;
    }
    
    // used for autorepeat, to send a different id for the repeated calls
    public void setActionListener(ActionListener l, int id, int id2)
    {
        actionListener = l;
        actionId = id;
        actionIdRepeat = id2;
    }
    
    public void setChangeListener(ChangeListener l)
    {
        changeListener = l;
    }
    
    public void draw()
    {
        if (!visible) return;
        switch (behavior)
        {
            case BEHAVIOR_TOGGLE:
                draw(hover, (pressed && !enabled) || (!pressed & enabled));
                break;
                
            case BEHAVIOR_RADIO: 
                draw(hover && !enabled, pressed || enabled);
                break;
                
            case BEHAVIOR_CLICK:
            case BEHAVIOR_AUTOREPEAT:
                draw(hover && !pressed, pressed);
                break;
        }
    }
    
    abstract public void draw(boolean hover, boolean pressed);
}
