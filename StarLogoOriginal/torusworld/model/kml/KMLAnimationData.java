package torusworld.model.kml;

import java.awt.Color;

import javax.media.opengl.GL;
import torusworld.math.Cylinder;
import torusworld.model.AnimationData;

public class KMLAnimationData implements AnimationData {
	private KMLModel model;
	
	public KMLAnimationData(KMLModel newModel) {
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
