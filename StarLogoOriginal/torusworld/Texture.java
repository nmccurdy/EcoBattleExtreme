package torusworld;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

import com.sun.opengl.util.texture.TextureData;

/** 
 * Warning: class name Texture is reused. You must never import
 * com.sun.opengl.util.texture.Texture in any TorusWorld class.
 * 
 * Sorry, but I couldn't give up the name "Texture" :)
 */

/**
 * A Texture is a wrapper around a JOGL Texture class that implements some extra functionality.
 * Simply use bind() to bind the current texture. Note that this class uses the OpenGL texture
 * matrix to convert texcoords, so one cannot use the texture matrix outside this class without
 * refactoring. If a texture is used without this class, one should call Texture.resetMatrix to
 * reset the texture matrix.
 */
public class Texture
{
    private String name;
    private boolean mipmapped; 
    private com.sun.opengl.util.texture.Texture jogl_texture;
    
    public String name() {
        return name;
    }
    
    public Texture(String name, com.sun.opengl.util.texture.Texture tex, boolean mipmapped)
    {
        this.name = name;
        this.mipmapped = mipmapped;
        jogl_texture = tex;
    }
    
    /**
     * Bind the texture using GL_LINEAR blending
     */
    public void bind()
	{
	    jogl_texture.bind();
	    jogl_texture.setTexParameteri(GL.GL_TEXTURE_MIN_FILTER, mipmapped ? GL.GL_LINEAR_MIPMAP_LINEAR : GL.GL_LINEAR);
	    // Set the texture matrix
	    com.sun.opengl.util.texture.TextureCoords c = jogl_texture.getImageTexCoords();
	    GL gl = GLU.getCurrentGL(); //FIXME - use TNG's GL
	    int old_matrix[] = new int[1];
	    gl.glGetIntegerv(GL.GL_MATRIX_MODE, old_matrix, 0);
	    gl.glMatrixMode(GL.GL_TEXTURE);
	    gl.glLoadIdentity();
	    gl.glTranslatef(c.left(), c.bottom(), 0);
	    gl.glScalef(c.right() - c.left(), c.top() - c.bottom(), 1); 
	    gl.glMatrixMode(old_matrix[0]);
	}
    
    /**
     * Bind the texture using GL_NEAREST blending rather than linear or mipmapped
     */
    public void bindGLNearest() {
	    jogl_texture.bind();
	    jogl_texture.setTexParameteri(GL.GL_TEXTURE_MIN_FILTER, /*mipmapped ? GL.GL_LINEAR_MIPMAP_LINEAR : GL.GL_LINEAR*/GL.GL_NEAREST);
	    jogl_texture.setTexParameteri(GL.GL_TEXTURE_MAG_FILTER, /*mipmapped ? GL.GL_LINEAR_MIPMAP_LINEAR : GL.GL_LINEAR*/GL.GL_NEAREST);
	    // Set the texture matrix
	    com.sun.opengl.util.texture.TextureCoords c = jogl_texture.getImageTexCoords();
	    GL gl = GLU.getCurrentGL(); //FIXME - use TNG's GL
	    int old_matrix[] = new int[1];
	    gl.glGetIntegerv(GL.GL_MATRIX_MODE, old_matrix, 0);
	    gl.glMatrixMode(GL.GL_TEXTURE);
	    gl.glLoadIdentity();
	    gl.glTranslatef(c.left(), c.bottom(), 0);
	    gl.glScalef(c.right() - c.left(), c.top() - c.bottom(), 1); 
	    gl.glMatrixMode(old_matrix[0]);
    }
    
    /*
    public void bind()
    {
        jogl_texture.bind();
        jogl_texture.setTexParameteri(GL.GL_TEXTURE_MIN_FILTER, mipmapped ? GL.GL_LINEAR_MIPMAP_LINEAR : GL.GL_LINEAR);
        // Set the texture matrix
        com.sun.opengl.util.texture.TextureCoords c = jogl_texture.getImageTexCoords();
        GL gl = GLU.getCurrentGL();
        int old_matrix[] = new int[1];
        gl.glGetIntegerv(GL.GL_MATRIX_MODE, old_matrix, 0);
        gl.glMatrixMode(GL.GL_TEXTURE);
        gl.glLoadIdentity();
        gl.glTranslatef(c.left(), c.bottom(), 0);
        gl.glScalef(c.right() - c.left(), c.top() - c.bottom(), 1); 
        gl.glMatrixMode(old_matrix[0]);
    }
    */
    public static void resetMatrix()
    {
        GL gl = GLU.getCurrentGL();
        int old_matrix[] = new int[1];
        gl.glGetIntegerv(GL.GL_MATRIX_MODE, old_matrix, 0);
        gl.glMatrixMode(GL.GL_TEXTURE);
        gl.glLoadIdentity();
        gl.glMatrixMode(old_matrix[0]);
    }
    
    public void dispose()
    {
        jogl_texture.dispose();
        jogl_texture = null;
    }
    
    /**
     * TODO
     * Chris's new method to update the image
     * using a given texture data
     * 
     * Note that we can also use updateSubImage too
     */
    public void updateTexture(TextureData _data)
    {
    	
    	//Stopwatch.setActive(true);
    	//long id = Stopwatch.start("my group", "my label");
    	//jogl_texture.updateImage(_data); // Chris's old code
    	//jogl_texture.updateSubImage(_data, 0, 4, 4); // new, much faster call, shifted
    	jogl_texture.updateSubImage(_data, 0, 0, 0);
    	//Stopwatch.stop(id);
    	//Report report = Stopwatch.getSingleReport("my group", "my label");
    	//System.out.print(report);

    	//Testing whether we can change the size of the image in the texture (pixels)
    	/*
    	try {
    		//URL picurl = new URL("http://www.softpicks.net/screenshots/3D-Waterfall-Screensaver.jpg");
    		//com.sun.opengl.util.texture.Texture tex = TextureIO.newTexture(picurl, false, null);
    		
    		/ This is the snippet that pastes stripes in the lower right corner of the texture /
    		int w = 500;
    		int h = 500;
    		BufferedImage bimg = new BufferedImage(w,h,BufferedImage.TYPE_3BYTE_BGR);
    		
    		byte[] pixels = new byte[h*w*3];
    		for (int i = 0; i < w*h; i++) {
    			if (i % 2 == 0) {
    				pixels[i*3+2] = (byte)255;
    			}
    			if (i % 2 == 1) {
    				pixels[i] = (byte) 255;
    			}
    		}
    		WritableRaster wr = bimg.getRaster();
    		wr.setDataElements(0, 0, w, h, pixels);
    		
    		TextureData td = new TextureData(GL.GL_RGB, GL.GL_RGB, true, bimg);
    		//GLU glu = new GLU();
    		//glu.gluBuild2DMipmaps(arg0, arg1, arg2, arg3, arg4, arg5, arg6)
    		//jogl_texture.updateSubImage(td,1,400,450);
    		jogl_texture.updateSubImage(td, 0,400, 450, 1, 0,100,100);
    		//jogl_texture.
    		
    	}
    	catch(Exception e) {e.printStackTrace();}*/
    }
    
    
}
