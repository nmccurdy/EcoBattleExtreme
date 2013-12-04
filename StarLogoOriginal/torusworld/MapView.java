package torusworld;

import javax.media.opengl.GL;
import starlogoc.TerrainData;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.awt.Color;
import java.util.Iterator;
 
/** @author Michael Matczynski
 */
public class MapView
{
    private int texID = 0, texWidth, texHeight;
    private int terrainWidth, terrainHeight;
    private int viewWidth, viewHeight, posX, posY;
    private float alpha = 0.7f;
    private boolean mouseInView;
    private TerrainData data;
    
    private float table[][];
    private FloatBuffer buffer;


    public MapView(int px, int py, int width, int height)
    {
        setPos(px, py);
        setSize(width, height);
    }

    // moves the position of the bottom left corner of the minimap
    public void setPos(int x, int y)
    {
        posX = x;
        posY = y;
    }

    // changes the size of the minimap
    public void setSize(int width, int height)
    {
        viewWidth = width;
        viewHeight = height;
    }
    
    public boolean isVisible()
    {
        return (alpha >= 0.01);
    }
    
    public void mousePos(int x, int y)
    {
        mouseInView = isVisible() && (x >= posX && y >= posY &&
                                    x <= posX+viewWidth && y <= posY+viewHeight);
    }
    
    public boolean mouseInViewport()
    {
        return mouseInView;
    }

    public boolean mouseInViewport(int x, int y)
    {
        mousePos(x, y);
        return mouseInView;
    }

    public void setAlpha(float alpha)
    {
        if (alpha < 0.f) alpha = 0.f;
        if (alpha > 1.f) alpha = 1.f;
        
        this.alpha = alpha;
    }
    
    public float getAlpha() { return alpha; }


    public void initTerrain(TerrainData _data)
    {
        data = _data;
        terrainWidth = data.getWidth();
        terrainHeight = data.getHeight();
        
        table = new float[terrainWidth][terrainHeight];
        buffer = ByteBuffer.allocateDirect(3*4*terrainWidth*terrainHeight).order(ByteOrder.nativeOrder()).asFloatBuffer();
        
        for (texWidth = 1; texWidth < terrainWidth; texWidth *= 2);
        for (texHeight = 1; texHeight < terrainWidth; texHeight *= 2);
        
        GL gl = SLRendering.getGL();
        if (texID == 0)
        {
            int a[] = new int[1];
            gl.glGenTextures(1, a, 0);
            texID = a[0];
        }
        
        gl.glBindTexture(GL.GL_TEXTURE_2D, texID);
        // initialize (empty) texture
        gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, texWidth, texHeight, 
                        0, GL.GL_RGBA, GL.GL_FLOAT, null);
        
        //gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGB,
        //		data.getAssociatedWidth(), data.getAssociatedHeight(),
        //		0, GL.GL_BGR, GL.GL_BYTE, data.getAssociated().getBuffer());

        
        updateTerrain();
    }
        
        
	public void updateTerrain() 
    {
        int i, j;
        float heightMin = 1e10f, heightMax = -1e10f;
        
        for (i = 0; i < terrainHeight; i++)
            for (j = 0; j < terrainWidth; j++)
            {
                table[i][j] = data.getHeight(j, i);
                if (heightMin > table[i][j]) heightMin = table[i][j];
                if (heightMax < table[i][j]) heightMax = table[i][j];
            }
        heightMin -= 10;
        
        buffer.rewind();
        float globalScale = 1.f / 255.f / (heightMax - heightMin);
        for (i = 0; i < terrainHeight; i++)
            for (j = 0; j < terrainWidth; j++)
            {
                Color col = data.getColor(j, i);
                float scale = globalScale * (table[i][j] - heightMin); 
                buffer.put(scale * col.getRed());
                buffer.put(scale * col.getGreen());
                buffer.put(scale * col.getBlue());
            }
        
        GL gl = SLRendering.getGL();
        gl.glBindTexture(GL.GL_TEXTURE_2D, texID);
        buffer.rewind();
        gl.glTexSubImage2D(GL.GL_TEXTURE_2D, 0, 0, 0, terrainWidth, terrainHeight, 
                           GL.GL_RGB, GL.GL_FLOAT, buffer);
        
                
        //gl.glTexSubImage2D(GL.GL_TEXTURE_2D, 0, 0, 0, data.getAssociatedWidth(), data.getAssociatedHeight(),
        //		TerrainData.TEX_COLOR_TYPE, TerrainData.TEX_COLOR_TYPE, data.getAssociated().getBuffer());

        
	}
    
    private void drawTurtle(Mobile m, GL gl, float alpha)
    {
        if (!m.shown) return;
        boolean selected = SLCameras.currentCamera != SLCameras.PERSPECTIVE_CAMERA && m == SLCameras.currentAgent;

        float x = (m.pos.x - SLTerrain.getPatchCenterX(0)) / (SLTerrain.patchSize * terrainWidth);
        float y = (m.pos.z - SLTerrain.getPatchCenterX(0)) / (SLTerrain.patchSize * terrainHeight);
        float size = 2 * (float) (viewWidth + viewHeight) / (terrainWidth + terrainHeight) * 
                     0.5f * (m.xScale + m.zScale);
        if (size < 0.0001) return;

        if (selected)
        {
            gl.glPointSize(2 * size);
            gl.glColor4f(1, 1, 0, alpha);
            gl.glBegin(GL.GL_POINTS);
            gl.glVertex2f(x, y);
            gl.glEnd();
        }

        gl.glPointSize(1.4f * size);
        gl.glColor4f(0, 0, 0, alpha);
        gl.glBegin(GL.GL_POINTS);
        gl.glVertex2f(x, y);
        gl.glEnd();
        
        gl.glPointSize(size);
        gl.glColor4f(m.color.getRed() / 255.f, m.color.getGreen() / 255.f, m.color.getBlue() / 255.f, alpha);
        gl.glBegin(GL.GL_POINTS);
        gl.glVertex2f(x, y);
        gl.glEnd();
        
        if (selected)
        {
            float heading = m.pos.heading;
            gl.glBegin(GL.GL_TRIANGLES);
            gl.glColor4f(1, 1, 0, alpha); 
            gl.glVertex2f(x, y);
            
            gl.glColor4f(1, 1, 0, 0); 
            gl.glVertex2f(x + 0.25f * (float)Math.sin((heading + 20.0f) * Math.PI / 180.0f),
                          y - 0.25f * (float)Math.cos((heading + 20.0f) * Math.PI / 180.0f));
            
            gl.glColor4f(1, 1, 0, 0); 
            gl.glVertex2f(x + 0.25f * (float)Math.sin((heading - 20.0f) * Math.PI / 180.0f),
                          y - 0.25f * (float)Math.cos((heading - 20.0f) * Math.PI / 180.0f));
            gl.glEnd();
        }
    }

    /**
     * Draws the mini-map.
     * 
     *  @param height the height of SpaceLand window/panel
     *  @param turtles the mobile objects that are to be rendered
     */
    public void drawIt(int height, Turtles turtles)
    {
        if (!isVisible()) return;
        GL gl = SLRendering.getGL();
        
        float alpha = mouseInView ? 1.f : this.alpha;

        gl.glPushMatrix();
        gl.glDisable(GL.GL_FOG);
        gl.glDisable(GL.GL_LIGHTING);
        gl.glDisable(GL.GL_DEPTH_TEST);
        gl.glDisable(GL.GL_CULL_FACE);
        
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        
        gl.glDisable(GL.GL_TEXTURE_2D);
        
        // draws the outline of the mini-view
        gl.glColor4f(0.0f, 0.0f, 0.0f, alpha);
        gl.glBegin(GL.GL_LINE_LOOP);
        gl.glVertex2f(posX - 2, posY + viewHeight + 2);
        gl.glVertex2f(posX + viewWidth + 1, posY + viewHeight + 2);
        gl.glVertex2f(posX + viewWidth + 1, posY - 1);
        gl.glVertex2f(posX - 2, posY - 1);
        gl.glEnd();
        
        gl.glColor4f(1.0f, 1.0f, 1.0f, alpha);
        gl.glBegin(GL.GL_LINE_LOOP);
        gl.glVertex2f(posX - 1, posY + viewHeight + 1);
        gl.glVertex2f(posX + viewWidth, posY + viewHeight + 1);
        gl.glVertex2f(posX + viewWidth, posY);
        gl.glVertex2f(posX - 1, posY);
        gl.glEnd();
        
        gl.glEnable(GL.GL_TEXTURE_2D);
        Texture.resetMatrix();
        gl.glBindTexture(GL.GL_TEXTURE_2D, texID);
        gl.glColor4f(1.0f, 1.0f, 1.0f, alpha);
        gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
        
        gl.glTranslatef(posX, posY, 0);
        gl.glScalef(viewWidth, viewHeight, 1);
        
        gl.glBegin(GL.GL_POLYGON);

        gl.glTexCoord2f(0, 0);
        gl.glVertex2f(0, 0);
        
        gl.glTexCoord2f((float) terrainWidth/texWidth, 0.f);
        gl.glVertex2f(1, 0);
        
        gl.glTexCoord2f((float) terrainWidth/texWidth, (float) terrainHeight/texHeight);
        gl.glVertex2f(1, 1);
        
        gl.glTexCoord2f(0.f, (float) terrainHeight/texHeight);
        gl.glVertex2f(0, 1);
        
        gl.glEnd();
        
        gl.glDisable(GL.GL_TEXTURE_2D);
        gl.glEnable(GL.GL_POINT_SMOOTH);
        
        // draw turtles by putting a pixel of color in each turtle's position on minimap
        Iterator<Mobile> i = turtles.getTurtleIterator();

        gl.glScissor(posX, height - posY - viewHeight, viewWidth, viewHeight);
        gl.glEnable(GL.GL_SCISSOR_TEST);

        while (i.hasNext())
            drawTurtle(i.next(), gl, alpha);
        gl.glDisable(GL.GL_SCISSOR_TEST);
        
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glEnable(GL.GL_CULL_FACE);
        gl.glPopMatrix();
    }
}
