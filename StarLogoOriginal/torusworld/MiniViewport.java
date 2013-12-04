package torusworld;

import javax.media.opengl.GL;

public class MiniViewport
{
    public static final int MAX_TEX_SIZE = 256;
    
    // texWidth/texHeight are usually equal to viewWidth/viewHeight
    // but they are capped to MAX_TEX_SIZE

    private int texID, texWidth, texHeight;
    private int viewWidth, viewHeight, posX, posY;
    private float alpha = 0.7f;
    private boolean mouseInView;

    public MiniViewport(int x, int y, int width, int height)
    {
        setPos(x, y);
        setSize(width, height);
    }


    public void setPos(int x, int y)
    {
        posX = x;
        posY = y;
    }

    public void setSize(int width, int height)
    {
        viewWidth = width;
        viewHeight = height;
            
        texWidth = (width < MAX_TEX_SIZE) ? width : MAX_TEX_SIZE;
        texHeight = (height < MAX_TEX_SIZE) ? height : MAX_TEX_SIZE;
    }

    public int getViewWidth() 
    {
        return viewWidth;
    }

    public int getViewHeight() 
    {
        return viewHeight;
    }

    public int getTexWidth() 
    {
        return texWidth;
    }

    public int getTexHeight() 
    {
        return texHeight;
    }

    public int getX()
    {
        return posX;
    }

    public int getY()
    {
        return posY;
    }

    public float getAlpha()
    {
        return alpha;
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
    
    public int screenToRenderedX(int x)
    {
        return (x - posX) * texWidth / viewWidth;
    }
    
    public int screenToRenderedY(int y)
    {
        return (viewHeight + posY - y) * texHeight / viewHeight;
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

    public void init()
    {
        GL gl = SLRendering.getGL();
        int a[] = new int[1];
        gl.glGenTextures(1, a, 0);
        texID = a[0];
        gl.glBindTexture(GL.GL_TEXTURE_2D, texID);
        gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, MAX_TEX_SIZE, MAX_TEX_SIZE, 
                              0, GL.GL_RGBA, GL.GL_FLOAT, null);
    }
    
    public void copyTexture()
    {
        GL gl = SLRendering.getGL();
        gl.glBindTexture(GL.GL_TEXTURE_2D, texID);
        gl.glCopyTexSubImage2D(GL.GL_TEXTURE_2D, 0, 0, 0, 0, 0, texWidth, texHeight);
    }
    
    public void drawIt()
    {
        if (!isVisible()) return;
        
        GL gl = SLRendering.getGL();
        
        float alpha = mouseInView ? 1.f : this.alpha;
        
        gl.glDisable(GL.GL_FOG);
        gl.glDisable(GL.GL_LIGHTING);
        gl.glDisable(GL.GL_DEPTH_TEST);
        gl.glDisable(GL.GL_CULL_FACE);
        
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        
        gl.glDisable(GL.GL_TEXTURE_2D);
        
        // draw outline of mini-view
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
        
        
        gl.glBegin(GL.GL_POLYGON);

        gl.glTexCoord2f(0.f, (float) texHeight/MAX_TEX_SIZE);
        gl.glVertex2f(posX, posY);
        
        gl.glTexCoord2f((float) texWidth/MAX_TEX_SIZE, (float) texHeight/MAX_TEX_SIZE);
        gl.glVertex2f(posX + viewWidth, posY);
        
        gl.glTexCoord2f((float) texWidth/MAX_TEX_SIZE, 0.f);
        gl.glVertex2f(posX + viewWidth, posY + viewHeight);
        
        gl.glTexCoord2f(0.f, 0.f);
        gl.glVertex2f(posX, posY + viewHeight);
        
        gl.glEnd();
        
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glEnable(GL.GL_CULL_FACE);
    }
}
