package torusworld;
import torusworld.math.Vector3f;

public class BoxCameraConstrainer extends CameraConstrainer
{
    private Vector3f min = new Vector3f(-1e5f, -1e5f, -1e5f), max = new Vector3f(1e5f, 1e5f, 1e5f);
    
    public BoxCameraConstrainer( 
    		float minx, float miny, float minz, 
            float maxx, float maxy, float maxz, CameraConstrainer subConstr)
    {
    	super(subConstr);
        min = new Vector3f(minx, miny, minz);
        max = new Vector3f(maxx, maxy, maxz);
    }
    
    public BoxCameraConstrainer( 
    		float minx, float miny, float minz, 
            float maxx, float maxy, float maxz, Camera cam)
    {
    	super(cam);
        min = new Vector3f(minx, miny, minz);
        max = new Vector3f(maxx, maxy, maxz);
    }    
    
    
    public void setBoundingBox(float minx, float miny, float minz, 
                               float maxx, float maxy, float maxz)
    {
        min = new Vector3f(minx, miny, minz);
        max = new Vector3f(maxx, maxy, maxz);
    }
    
    	@Override
	public boolean checkConstraints() {
        Vector3f v;
        v = camera.getMutablePosition();         
        return !(v.x < min.x || v.y < min.y || v.z < min.z ||
        		 v.x > max.x || v.y > max.y || v.z > max.z);
	}
}
