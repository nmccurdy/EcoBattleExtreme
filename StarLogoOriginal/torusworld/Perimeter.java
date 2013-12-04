
package torusworld;

//import net.java.games.gluegen.runtime.*; 
import java.nio.FloatBuffer;

import javax.media.opengl.GL;

import com.sun.opengl.util.BufferUtil;
/**
 * @author Mike
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */

public class Perimeter {
	public static final int TURTLE = 3;
	public static final int FENCE = 2;
	public static final int BOX = 1;
	public static final int WALL = 0;
	public static final boolean HASWALL = false;
    
    private static final float fenceHeight = 3.f;
	
	
	// type of perimeter
	private int type;
	
	// if (type == FENCE), this determines texture
	//private String fenceTextureName;
	
	private GL gl;
    private int width, height;
    private float patchSize;
	//private TextureManager textureManager;
	
	private FloatBuffer buffer;
	private FloatBuffer wallBuffer;
	
    private static boolean textures_loaded;
    
	public Perimeter(GL gl) 
    {
		this.gl = gl;
		//this.textureManager = tm;
		
		this.type = FENCE;
        
        if (!textures_loaded)
        {
            textures_loaded = true;
            TextureManager.createTexture("Perimeter.fence", TorusWorld.class.getResource("textures/fence.png"), true);
            TextureManager.createTexture("Perimeter.turtle", TorusWorld.class.getResource("textures/turtle.png"));
        }
        // update();
	}
	
	public void draw() {
		switch (type) 
		{	
		case FENCE:
			drawFence();
			break;
		case BOX:
			drawBox();
			break;
		case WALL:
			drawWalls();
			break;
		}
		
		if (HASWALL) 
			drawWallToGround();
	}
	
	public void changeToNextType() {
		type = (type + 1) % 4;
	}

	// Force field
	private void drawBox() 
	{
		int width = 300;
		int height = 300;
		int length = 300;
		int x=-150, y=-150, z=-150;
	
		//Enables Blending Function
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		gl.glDisable(GL.GL_CULL_FACE);
		
		// gl.glCullFace(GL.GL_BACK);
		gl.glPushMatrix();
		gl.glTranslatef(0.0f, 0.0f, 0.0f);
		gl.glScalef(1.0f, 1.0f, 1.0f);
		
		gl.glBegin(GL.GL_QUADS);
		gl.glColor4f(0.5f,0.0f,0.5f,0.5f);
		gl.glTexCoord2f (1.0f, 0.0f); gl.glVertex3f(x + width, y,			z);
		gl.glTexCoord2f (1.0f, 1.0f); gl.glVertex3f(x + width, y + height,	z); 
		gl.glTexCoord2f (0.0f, 1.0f); gl.glVertex3f(x,			y + height, z);
		gl.glTexCoord2f (0.0f, 0.0f); gl.glVertex3f(x,			y,			z);
	
		gl.glColor4f(0.5f,0.0f,0.5f,0.5f);
		gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(x,			y,			z + length);
		gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(x,			y + height, z + length);
		gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(x + width, y + height, z + length); 
		gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(x + width, y,			z + length);
	
		gl.glColor4f(0.5f,0.0f,0.5f,0.5f);
		gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(x,			y,			z);
		gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(x,			y,			z + length);
		gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(x + width, y,			z + length); 
		gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(x + width, y,			z);
	
		gl.glColor4f(0.5f,0.0f,0.5f,0.5f);
		gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(x + width, y + height, z);
		gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(x + width, y + height, z + length); 
		gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(x,			y + height,	z + length);
		gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(x,			y + height,	z);
	
		gl.glColor4f(0.5f,0.0f,0.5f,0.5f);
		gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(x,			y + height,	z);	
		gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(x,			y + height,	z + length); 
		gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(x,			y,			z + length);
		gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(x,			y,			z);		
	
		gl.glColor4f(0.5f,0.0f,0.5f,0.5f);
		gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(x + width, y,			z);
		gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(x + width, y,			z + length);
		gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(x + width, y + height,	z + length); 
		gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(x + width, y + height,	z);
		gl.glEnd();
		gl.glPopMatrix();
	
		gl.glEnable(GL.GL_CULL_FACE);
		gl.glBlendFunc(GL.GL_ONE, GL.GL_ZERO);
		gl.glColor4f(1.0f,1.0f,1.0f,1.0f);
	}

	// Draw walls: used mainly for debugging
	private void drawWalls() 
	{
		int width = 300;
		int height = 300;
		int length = 300;
		int x=-150, y=-150, z=-150;
	
		//Enables Blending Function
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		gl.glDisable(GL.GL_CULL_FACE);
		
		// gl.glCullFace(GL.GL_BACK);
		gl.glPushMatrix();
		gl.glTranslatef(0.0f, 0.0f, 0.0f);
		gl.glScalef(1.0f, 1.0f, 1.0f);
		
		gl.glBegin(GL.GL_QUADS);
		gl.glColor4f(0.5f,0.0f,0.5f,0.5f);
		gl.glTexCoord2f (1.0f, 0.0f); gl.glVertex3f(x + width, y,			z);
		gl.glTexCoord2f (1.0f, 1.0f); gl.glVertex3f(x + width, y + height,	z); 
		gl.glTexCoord2f (0.0f, 1.0f); gl.glVertex3f(x,			y + height, z);
		gl.glTexCoord2f (0.0f, 0.0f); gl.glVertex3f(x,			y,			z);
	
		gl.glColor4f(0.5f,0.0f,0.5f,0.5f);
		gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(x,			y,			z + length);
		gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(x,			y + height, z + length);
		gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(x + width, y + height, z + length); 
		gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(x + width, y,			z + length);
	
		gl.glColor4f(0.5f,0.0f,0.5f,0.5f);
		gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(x,			y + height,	z);	
		gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(x,			y + height,	z + length); 
		gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(x,			y,			z + length);
		gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(x,			y,			z);		
	
		gl.glColor4f(0.5f,0.0f,0.5f,0.5f);
		gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(x + width, y,			z);
		gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(x + width, y,			z + length);
		gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(x + width, y + height,	z + length); 
		gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(x + width, y + height,	z);
		gl.glEnd();
		gl.glPopMatrix();
	
	}

	// Draw fence around terrain perimeter
	private void drawFence() 
	{
        gl.glEnable(GL.GL_TEXTURE_2D);

/*        gl.glDisable(GL.GL_TEXTURE_2D);
        gl.glColor4f(1.f, 1.f, 1.f, 0.5f);*/

		//Enables Blending Function
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        gl.glEnable(GL.GL_BLEND);
		gl.glDisable(GL.GL_CULL_FACE);
		
		if (type == FENCE) {
			gl.glEnable(GL.GL_ALPHA_TEST);	
			gl.glTexEnvi(GL.GL_TEXTURE_ENV,GL.GL_TEXTURE_ENV_MODE,GL.GL_REPLACE);
			gl.glAlphaFunc(GL.GL_GREATER, 0);
			TextureManager.bindTexture("Perimeter.fence");
		}
		else if (type == TURTLE) {
			gl.glColor4f(0.5f,0.0f,0.5f,0.5f);
            TextureManager.bindTexture("Perimeter.turtle");
		}

        gl.glInterleavedArrays(GL.GL_T2F_N3F_V3F, 0, buffer);
        gl.glDrawArrays(GL.GL_QUADS, 0, 2 * (width + height) * 4);
		
		if (type == FENCE) 
			gl.glTexEnvi(GL.GL_TEXTURE_ENV,GL.GL_TEXTURE_ENV_MODE,GL.GL_MODULATE);
	
		gl.glDisable(GL.GL_TEXTURE_2D);
	}

    private void putVertex(float tx, float ty, float nx, float ny, float nz, float x, float y, float z)
    {
        buffer.put(tx);
        buffer.put(ty);
        buffer.put(nx);
        buffer.put(ny);
        buffer.put(nz);
        buffer.put(x);
        buffer.put(y);
        buffer.put(z);
    }
    
    
    private void fencePiece(float x1, float z1, float dx, float dz, float nx, float ny, float nz)
    {
        float h1 = SLTerrain.getPointHeight(x1, z1);
        float h2 = SLTerrain.getPointHeight(x1+dx, z1+dz);
               
        putVertex(0.f, 0.f, nx, ny, nz, x1, h1, z1);
        putVertex(1.f, 0.f, nx, ny, nz, x1+dx, h2, z1+dz);
        putVertex(1.f, 1.f, nx, ny, nz, x1+dx, h2 + fenceHeight, z1+dz);
        putVertex(0.f, 1.f, nx, ny, nz, x1, h1 + fenceHeight, z1);
    }
    
	public void update() 
    {
        int newWidth = SLTerrain.getWidth();
        int newHeight = SLTerrain.getHeight();
        
        patchSize = SLTerrain.getPatchSize();
        
        if (newWidth != width || height != newHeight)
        {
            width = newWidth;
            height = newHeight;
                        
            /* perimeter * 4 corners * (2 texcoord + 3 normal + 3 vertex) */
            buffer = BufferUtil.newFloatBuffer(2 * (width + height) * 4 * (3 + 3 + 2));
            if (HASWALL)
            	wallBuffer = BufferUtil.newFloatBuffer(2 * (width + height) * 4 * (3 + 3));
        }

		// Positive Z
        buffer.rewind();
        
		for (int x = 0; x < width; x++)
            fencePiece(SLTerrain.getPatchCenterX(x) - patchSize * 0.5f, SLTerrain.getPatchCenterZ(0) - patchSize * 0.49f,  
                       patchSize, 0.f,  
                       0.f, 0.f, 1.f);

        for (int x = 0; x < width; x++)
            fencePiece(SLTerrain.getPatchCenterX(x) - patchSize * 0.5f, SLTerrain.getPatchCenterZ(height-1) + patchSize * 0.49f,  
                       patchSize, 0.f,  
                       0.f, 0.f, -1.f);
		
		for (int z = 0; z < height; z++)
		    fencePiece(SLTerrain.getPatchCenterX(width-1) + patchSize * 0.49f, SLTerrain.getPatchCenterZ(z) - patchSize * 0.5f,  
                       0.f, patchSize,  
                       -1.f, 0.f, 0.f);
		       
        for (int z = 0; z < height; z++)
            fencePiece(SLTerrain.getPatchCenterX(0) - patchSize * 0.49f, SLTerrain.getPatchCenterZ(z) - patchSize * 0.5f,  
                       0.f, patchSize,  
                       1.f, 0.f, 0.f);
		buffer.rewind();

		if (HASWALL) {
			// Positive Z
	        wallBuffer.rewind();
	        
			for (int x = 0; x < width; x++)
	            wallPiece(SLTerrain.getPatchCenterX(x) - patchSize * 0.5f, SLTerrain.getPatchCenterZ(0) - patchSize * 0.5f,  
	                       patchSize, 0.f,  
	                       0.f, 0.f, 1.f);
	
	        for (int x = 0; x < width; x++)
	            wallPiece(SLTerrain.getPatchCenterX(x) - patchSize * 0.5f, SLTerrain.getPatchCenterZ(height-1) + patchSize * 0.5f,  
	                       patchSize, 0.f,  
	                       0.f, 0.f, -1.f);
			
			for (int z = 0; z < height; z++)
			    wallPiece(SLTerrain.getPatchCenterX(width-1) + patchSize * 0.5f, SLTerrain.getPatchCenterZ(z) - patchSize * 0.5f,  
	                       0.f, patchSize,  
	                       -1.f, 0.f, 0.f);
			       
	        for (int z = 0; z < height; z++)
	            wallPiece(SLTerrain.getPatchCenterX(0) - patchSize * 0.5f, SLTerrain.getPatchCenterZ(z) - patchSize * 0.5f,  
	                       0.f, patchSize,  
	                       1.f, 0.f, 0.f);
			wallBuffer.rewind();
		}
    
    }
	
	
	private void drawWallToGround() 
	{
        gl.glInterleavedArrays(GL.GL_N3F_V3F, 0, wallBuffer);
        gl.glColor3f(0.5f, 0.5f, 0.5f);
        gl.glDrawArrays(GL.GL_QUADS, 0, 2 * (width + height) * 4);
	}

    private void putWallVertex(float nx, float ny, float nz, float x, float y, float z)
    {
        wallBuffer.put(nx);
        wallBuffer.put(ny);
        wallBuffer.put(nz);
        wallBuffer.put(x);
        wallBuffer.put(y);
        wallBuffer.put(z);
    }
    
    
    private void wallPiece(float x1, float z1, float dx, float dz, float nx, float ny, float nz)
    {
     
    	float h = SLTerrain.getPointHeight(x1+dx/2.0f, z1+dz/2.0f);
    	
        putWallVertex(nx, ny, nz, x1, 0.0f, z1);
        putWallVertex(nx, ny, nz, x1+dx, 0.0f, z1+dz);
        putWallVertex(nx, ny, nz, x1+dx, h, z1+dz);
        putWallVertex(nx, ny, nz, x1, h, z1);
    }
    	
}
