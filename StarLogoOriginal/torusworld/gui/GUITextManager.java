package torusworld.gui;

import com.sun.opengl.util.GLUT;

public class GUITextManager
{
    public enum TextSize { SMALL, NORMAL, LARGE };
    
    private TextSize size;
    private float colorR, colorG, colorB, colorA;
    private boolean outline;
    private float outlineR, outlineG, outlineB, outlineA;

    private static int fonts[] = {GLUT.BITMAP_HELVETICA_10, GLUT.BITMAP_HELVETICA_12, GLUT.BITMAP_HELVETICA_18};
    private static int fontHeights[] = {10, 12, 18};
    
    
    GUITextManager()
    {
        this(false);
    }
    
    GUITextManager(boolean outline)
    {
        setSize(TextSize.NORMAL);
        setOutline(outline);
        setColor(1, 1, 1, 1);
        setOutlineColor(0, 0, 0, 1);
    }
    
    void setSize(TextSize s)
    {
        size = s;
    }
    
    void setColor(float R, float G, float B, float A)
    {
        colorR = R;
        colorG = G;
        colorB = B;
        colorA = A;
    }
    
    void setOutline(boolean val)
    {
        outline = val;
    }

    void setOutlineColor(float R, float G, float B, float A)
    {
        outlineR = R;
        outlineG = G;
        outlineB = B;
        outlineA = A;
    }
    
    int getTextHeight()
    {
        return fontHeights[size.ordinal()];
    }
    
    int getTextWidth(String text)
    {
        if (text == null) return 0;
        return GUI.glut.glutBitmapLength(fonts[size.ordinal()], text);
    }
    
    void displayText(int x, int y, String text)
    {
        if (text == null) return;
        int font = fonts[size.ordinal()];
        y += getTextHeight();
        
        if (outline)
        {        
            GUI.gl.glColor4f(outlineR, outlineG, outlineB, outlineA);

            GUI.gl.glRasterPos2i(x-1, y);
            GUI.glut.glutBitmapString(font, text);
            GUI.gl.glRasterPos2i(x, y-1);
            GUI.glut.glutBitmapString(font, text);
            
            GUI.gl.glRasterPos2i(x+1, y);
            GUI.glut.glutBitmapString(font, text);
            GUI.gl.glRasterPos2i(x, y+1);
            GUI.glut.glutBitmapString(font, text);
        }
        
        GUI.gl.glColor4f(colorR, colorG, colorB, colorA);
        GUI.gl.glRasterPos2i(x, y);
        GUI.glut.glutBitmapString(font, text);
   }
}
