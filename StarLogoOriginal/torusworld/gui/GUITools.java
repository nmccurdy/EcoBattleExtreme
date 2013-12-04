package torusworld.gui;

public class GUITools
{
    // returns the width
    static public int EqualizeWidths(GUIButton ... buttons)
    {
        int max_width = GetMaxWidth(buttons);
        for (GUIButton button: buttons)
            button.setWidth(max_width);
        return max_width;
    }
    
    static public int GetMaxWidth(GUIButton ... buttons) {
    	int max_width = 0;
        for (GUIButton button: buttons)
            if (max_width < button.getWidth())
            	max_width = button.getWidth();        
        return max_width;
    }
    
    // returns total width/height
    static public int AlignButtons(int x, int y, boolean horizontal, int spacing, GUIButton ... buttons)
    {
        int dx = 0, dy = 0;
        for (GUIButton button: buttons)
        {
            button.setPosition(x + dx, y + dy);
            if (horizontal)
                dx += button.getWidth() + spacing;
            else
                dy += button.getHeight() + spacing;
        }
        return dx + dy;
    }

}
