package torusworld.model.obj;

import java.awt.Color;

import javax.media.opengl.GL;
import torusworld.math.Cylinder;
import torusworld.model.AnimationData;

public class OBJAnimationData implements AnimationData {
	private OBJModel model;
	
	public OBJAnimationData(OBJModel newModel) {
		model = newModel;
	}
	
	public void setAnimationSpeed(float speed) {
		
	}
	
	public void update() {
		
	}
	
	public float getAnimationSpeed() {
		return 1.0f;
	}
	
	public String getCurrentAnimation() {
		return null;
	}
	
	public Cylinder getBoundingCylinder() {
		return model.getBoundingCylinder();
	}
	
    public void setStanding() {
    	
    }
    
    public void setMoving() {
    	
    }
    
    public void render(GL gl, int detail, Color color, boolean useSkin)
    {
    	model.render(gl, color, useSkin);
    }
}
