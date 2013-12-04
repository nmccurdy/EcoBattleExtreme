package torusworld;

import torusworld.math.Vector3f;

public class YDirectionCameraConstrainer extends CameraConstrainer{

	private float maxYDir = 1.0f;	
	
	public YDirectionCameraConstrainer(float maxYDir, CameraConstrainer subConstr) {
		super(subConstr);
		this.maxYDir = maxYDir;
	}
	
	public YDirectionCameraConstrainer(float maxYDir, Camera cam) {
		super(cam);
		this.maxYDir = maxYDir;
	}
	
	@Override
	public boolean checkConstraints() {
		Vector3f v = camera.getMutableDirection();		 
		return !(Math.abs(v.y) > maxYDir);
	}
}
