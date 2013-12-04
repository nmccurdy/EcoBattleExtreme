package torusworld.gui;

import java.awt.event.MouseEvent;

public class GUIStatusBar implements GUIElement
{
    private int x, y;
    private int width, height;
    private int vertMargin, horizMargin;
    private boolean visible;
    
    GUITextManager textManager;
    
    private String rightText, statusText, tempStatusText;
    
    public GUIStatusBar()
    {
        visible = true;
        statusText = "";
        rightText = "";
        textManager = new GUITextManager(false);
    }
    
    public int getX() { return x; }
    public int getY() { return y; }
    public void setPosition(int nx, int ny)
    {
         x = nx;
         y = ny;
    }
    
    public void setTextSize(GUITextManager.TextSize textSize)
    {
        textManager.setSize(textSize);
        int text_height = textManager.getTextHeight();
        vertMargin = text_height / 3;
        horizMargin = text_height;
        height = text_height + 2 * vertMargin;
    }
    
    public void setScreenSize(int wid, int heig)
    {
        width = wid;
        setPosition(0, heig);
    }
        
    public boolean isMouseOver(int mx, int my)
    {
        return false;  // mouse doesn't interact with the status bar (since only the text is visible)
    }
    
    public boolean isVisible() { return visible; }
    public void setVisible(boolean vis) { visible = vis; }
    
    public void setStatus(String text) { statusText = text; }

    /** Sets the message on the right part of the bar
      */
    public void setRightText(String text) { rightText = text; }

    /** Sets the message on the right part of the bar if it is empty
      */
    public void setRightTextIfEmpty(String text)
    { 
        if (rightText == null || rightText.equals(""))
            rightText = text;
    }

    /** sets a status message that only shows up for a frame (for on mouse over)
      */
    public void setTempStatus(String text)
    {
        if (text != null)
            tempStatusText = text;
    }
    
    public void draw()
    {
        GUI.gl.glColor4f(0, 0, 0, 0.3f);
        GUI.gl.glRecti(x, y-height+vertMargin*2/3, x+width, y);

        int rightWidth = textManager.getTextWidth(rightText);
        textManager.displayText(x + width - rightWidth - horizMargin, y - height + vertMargin, rightText);
        
        String leftText = (tempStatusText == null || tempStatusText.equals("")) ? statusText : tempStatusText;
        tempStatusText = null;
        
        int maxLeftWidth = width - rightWidth - 3 * horizMargin;  
        int leftWidth = textManager.getTextWidth(leftText);
        
        if (leftWidth > maxLeftWidth)
        {
            // binary search for the maximum number of characters that fit
            int l, r;
            for (l = 0, r = leftText.length(); l < r;)
            {
                int m = (l + r + 1) / 2;
                String mString = leftText.substring(0, m) + "...";
                if (textManager.getTextWidth(mString) > maxLeftWidth)
                    r = m-1;
                else
                    l = m;
            }
            // trim spaces at the end
            while (l > 0 && leftText.charAt(l-1) == ' ') l--;
            leftText = leftText.substring(0, l) + "...";
        }
        
        textManager.displayText(x + horizMargin, y - height + vertMargin, leftText);
        
    }
    
    
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseClicked(MouseEvent e) {}
}
