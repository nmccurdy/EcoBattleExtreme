package torusworld;

import torusworld.math.Vector3f;

public class CylCameraConstraint extends CameraConstrainer {

	private float hMin, hMax, rAtMin;
	private float slope;
	
	public CylCameraConstraint(float hMin, float hMax,
							   float rAtMin, float rAtMax,
							   CameraConstrainer subConstr) {
		super(subConstr);
		this.hMin = hMin; this.hMax = hMax;
		this.rAtMin = rAtMin; 
		slope = (rAtMax-rAtMin)/(hMax-hMin);
	}

	
	@Override
	public boolean checkConstraints() {
		Vector3f v = camera.getMutablePosition();
		if (v.y < hMin || v.y > hMax)
			return false;
		
		float maxR = rAtMin + slope * (v.y-hMin);		
		return (maxR*maxR > (v.x*v.x+v.z*v.z));

	}
}
