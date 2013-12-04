package torusworld.model;

import java.awt.Color;

import javax.media.opengl.GL;

import com.sun.opengl.util.GLUT;

import torusworld.math.Cylinder;

/**
 * A CubeModel is a simple Model and AnimationData that shows a simple cube. Used when a model
 * cannot be loaded.
 */
public class CubeModel extends Model implements AnimationData
{
    private static final float CUBE_SIZE = 5;
    
    public void setAnimationSpeed(float speed) {}
    
    public void update() {}
        
    public float getAnimationSpeed() { return 1.0f; }
    
    public String getCurrentAnimation() { return null; }
    
    public Cylinder getBoundingCylinder() {
        return new Cylinder((float) Math.sqrt(2) * 0.5f * CUBE_SIZE, 0.5f * CUBE_SIZE, - 0.5f * CUBE_SIZE);
    }
    
    public void setStanding() {}
    
    public void setMoving() {}
    
    public void render(GL gl, int detail, Color color, boolean useSkin)
    {
        GLUT glut = new GLUT();
        gl.glColor4fv(color.getRGBComponents(null), 0);
        glut.glutSolidCube(CUBE_SIZE);
    }

    public String getModelName() { return "CubeModel"; }

    public String getSkinName() { return "NoSkin"; }

    public String[] getAvailableAnimations()
    {
        return new String[0];
    }
    
    public boolean init(GL gl) { return true; }

    public void deinit(GL gl) {}
}
