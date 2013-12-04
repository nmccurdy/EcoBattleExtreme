package torusworld;

import torusworld.math.Vector3f;

public class TerrainCameraConstrainer extends CameraConstrainer {

	public TerrainCameraConstrainer(CameraConstrainer subConstr) {
		super(subConstr);
	}

	public TerrainCameraConstrainer(Camera cam) {
		super(cam);
	}
	
	@Override
	public boolean checkConstraints() {
		Vector3f p = camera.getMutablePosition(); 
        float y = SLTerrain.getPointHeight(p.x, p.z);         
        return !(p.y < y + 0.25f);
	}
}
