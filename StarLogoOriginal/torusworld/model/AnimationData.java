//Stores model data for a single instance of Mobile
//Primarily, this includes the current animation, which is different when the object is moving than when it is still

package torusworld.model;

import java.awt.Color;

import javax.media.opengl.GL;

import torusworld.math.Cylinder;

public interface AnimationData {
	public void update();
	public void setAnimationSpeed(float speed);
	public float getAnimationSpeed();
	public String getCurrentAnimation();
	public Cylinder getBoundingCylinder();
    public void setStanding();
    public void setMoving();
    public void render(GL gl, int detail, Color color, boolean useSkin);
}
