package torusworld.gui;

import java.awt.event.MouseListener;

public interface GUIElement extends MouseListener
{
    public int getX();
    public int getY();
    public void setPosition(int nx, int ny);
    
    public boolean isMouseOver(int x, int y);
    
    public boolean isVisible();
    public void setVisible(boolean vis);
    
    public void draw();
}
