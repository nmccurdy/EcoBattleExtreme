package torusworld.gui;

import javax.media.opengl.GL;
import torusworld.TextureManager;


public class GUITextureButton extends GUIButton
{
    static public final float alphaUnpressed = 0.5f, alphaHover = 0.72f, alphaPressed = 0.95f;
  
    private String texture;
    private float texCoords[][];
    static private final float defaultTexCoords[][] = new float[][] {{0, 1}, {1, 1}, {1, 0}, {0, 0}};

    // last parameters used to easily change texture orientation (very useful for arrow buttons)
    public GUITextureButton(int _x, int _y, int _width, int _height, int _actionType, String texture_name,
                            int rotation, boolean horiz_flip, boolean vert_flip)
    {
        super(_x, _y, _width, _height, _actionType);
        texture = texture_name;
        texCoords = new float[4][2];
        for (int i = 0; i < 4; i++)
        {
            for (int j = 0; j < 2; j++)
                texCoords[i][j] = defaultTexCoords[(i+rotation) % 4][j];
            
            if (horiz_flip)
                texCoords[i][0] = 1 - texCoords[i][0];
            if (vert_flip)
                texCoords[i][1] = 1 - texCoords[i][1];
        }
    }
    
    public GUITextureButton(int _actionType, String texture_name,
                            int rotation, boolean horiz_flip, boolean vert_flip)
    {
        this(0, 0, 0, 0, _actionType, texture_name, rotation, horiz_flip, vert_flip);
    }
    
    public GUITextureButton(int _x, int _y, int _width, int _height, int _actionType, String texture_name)
    {
        this(_x, _y, _width, _height, _actionType, texture_name, 0, false, false);
    }
    
    public GUITextureButton(int _actionType, String texture_name)
    {
        this(0, 0, 0, 0, _actionType, texture_name, 0, false, false);
    }
    
    public void draw(boolean hover, boolean pressed)
    {
        float alpha;
        if (hover) 
            alpha = alphaHover;
        else
            alpha = pressed ? alphaPressed : alphaUnpressed;
        
        TextureManager.bindTexture(texture);
        GUI.gl.glEnable(GL.GL_TEXTURE_2D);
        
        GUI.gl.glColor4f(1, 1, 1, alpha);
        
        GUI.gl.glBegin(GL.GL_POLYGON);
            GUI.gl.glTexCoord2fv(texCoords[0], 0);
            GUI.gl.glVertex2f(x, y);
            GUI.gl.glTexCoord2fv(texCoords[1], 0);
            GUI.gl.glVertex2f(x+width, y);
            GUI.gl.glTexCoord2fv(texCoords[2], 0);
            GUI.gl.glVertex2f(x+width, y+height);
            GUI.gl.glTexCoord2fv(texCoords[3], 0);
            GUI.gl.glVertex2f(x, y+height);
        GUI.gl.glEnd();
        
        GUI.gl.glDisable(GL.GL_TEXTURE_2D);
    }
}
