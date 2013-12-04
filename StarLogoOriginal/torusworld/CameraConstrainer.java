package torusworld;
import torusworld.math.Vector3f;

public abstract class CameraConstrainer
{
    protected Camera camera;
    protected Vector3f last_position = new Vector3f();
    protected Vector3f last_dir = new Vector3f();
    protected Vector3f last_up = new Vector3f();
    protected CameraConstrainer subConstrainer;

    public CameraConstrainer(Camera cam) {
        camera = cam;
        subConstrainer = null;
    }
    
    public CameraConstrainer(CameraConstrainer subConstr) {
    	subConstrainer = subConstr;
    	camera = subConstr.camera;
    }
    
    public void storePos()
    {
        last_dir.set(camera.getMutableDirection());
        last_position.set(camera.getMutablePosition());
        last_up.set(camera.getMutableUpVector());
    }
        
    public void restorePos()
    {
        camera.getMutableDirection().set(last_dir);
        camera.getMutablePosition().set(last_position);
        camera.getMutableUpVector().set(last_up);
    }

    public abstract boolean checkConstraints();
    	
    public void enforceConstraints() {
    	if (subConstrainer != null)
    		subConstrainer.enforceConstraints();
    	if (checkConstraints()) {
    		storePos();
    	} else {
    		restorePos();
    	} 
    }
}
