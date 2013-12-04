package torusworld;

import java.nio.*;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

import starlogoc.GameManager;
import torusworld.math.Vector3f;

public class SLCameras
{
    public static float perspFOV = 30.f;
    public static float zNear = 0.2f, zFar = 3200.0f; // Z-buffer Near/Far Clipping plane depth
    
    public static final int PERSPECTIVE_CAMERA = 0;
    public static final int TURTLE_EYE_CAMERA = 1;
    public static final int OVER_THE_SHOULDER_CAMERA = 2;
    public static final int ORTHOGRAPHIC_CAMERA = 3; /* camera doesn't really exist, used internally */
    private static Camera cameras[];
    private static CameraConstrainer cameraConstrainers[];
    private static CameraController perspectiveControl;
    
    public static int currentCamera = PERSPECTIVE_CAMERA;
    public static boolean orthoView = false;
    public static Mobile currentAgent = null;
    
    private SLCameras() {}
    
    public static int getMainCameraId() { return orthoView ? ORTHOGRAPHIC_CAMERA : currentCamera; }
    
    public static Camera getCamera(int id) { return cameras[id]; }
    public static Camera getCurrentCamera() { return cameras[currentCamera]; }
    public static Camera getMainViewCamera() { return cameras[getMainCameraId()]; }
    public static Camera getMiniViewCamera() { return orthoView ? getCurrentCamera() : null; }
    
    public static CameraConstrainer getConstrainer(int camera_id) { return cameraConstrainers[camera_id]; }

    public static Camera getPerspectiveCamera() { return cameras[PERSPECTIVE_CAMERA]; }
    public static CameraConstrainer getPerspectiveConstrainer() { return cameraConstrainers[PERSPECTIVE_CAMERA]; }
    public static CameraController getPerspectiveControl() { return perspectiveControl; }

    public static void init(int halfSize, float centerY, float skyBoxSize)
    {
        cameras = new Camera[4];
        for (int c = 0; c < 4; c++)
            cameras[c] = new Camera(halfSize, centerY);
        
        cameraConstrainers = new CameraConstrainer[3];
        for (int c = 0; c < 3; c++)
        {
            CameraConstrainer constr = new TerrainCameraConstrainer(
            		new BoxCameraConstrainer(-skyBoxSize/2, -skyBoxSize/2, -skyBoxSize/2,
                            				 skyBoxSize/2, skyBoxSize/2, skyBoxSize/2,
                            	new YDirectionCameraConstrainer(0.98f, cameras[c])));
            
            float w = (float)SLTerrain.getWidth()*SLTerrain.getPatchSize();
            float l = (float)SLTerrain.getHeight()*SLTerrain.getPatchSize();
            
            
            constr = 
            	new CylCameraConstraint(0.0f, 1000.0f, 
            							w*2.0f, w*2.0f + ((float)Math.sqrt(l*l+w*w)*2.5f-w*2.0f)*2.0f, constr);
            
             cameraConstrainers[c] =
             	new CylCameraConstraint(0.0f, 1000.0f, 
             			w*2.0f + ((float)Math.sqrt(l*l+w*w)*2.5f-w*2.0f)*2.0f, w*2.0f, constr);

        }
        
        perspectiveControl = new CameraController(cameras[PERSPECTIVE_CAMERA]);
        
        cameras[ORTHOGRAPHIC_CAMERA].setPosition(new Vector3f(0.f, halfSize, 0.f));
        cameras[ORTHOGRAPHIC_CAMERA].setDirection(new Vector3f(0.f, -1.f, -0f));
        cameras[ORTHOGRAPHIC_CAMERA].setUpVector(new Vector3f(0.f, 0.f, -1.f));
    }
    
    public static void applyCamera(int camera_id, int width, int height) 
    {
        GL gl = SLRendering.getGL();
        GLU glu = SLRendering.getGLU();
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();
        if (camera_id != ORTHOGRAPHIC_CAMERA) {
            gl.glViewport(0, 0, width, height);
            glu.gluPerspective(perspFOV, (float) width / (float) height, zNear, zFar);
        } else {
            int dimension = ((width < height) ? width : height) - 2;
            gl.glViewport((width-dimension)/2,  (height-dimension)/2, dimension, dimension);
            gl.glOrtho(SLTerrain.getMinX(), SLTerrain.getMaxX(),
                       SLTerrain.getMinZ(), SLTerrain.getMaxZ(),
                       -1000, zFar);
        }
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glLoadIdentity();
        cameras[camera_id].applyCamera();
    }

    // functions used to convert between OpenGL framebuffer depths (which are in range [0,1])
    // and screen-space depths, range [zNear, zFar]

    public static float convertDepthFromGL(float z)
    {
        return zNear * zFar / (zFar - z * (zFar - zNear));
    }

    public static float convertDepthToGL(float z)
    {
        return (1.f / zNear - 1.f / z) / (1.f / zNear - 1.f / zFar);
    }

    public static int getCurrentAgentWho() {
    	if (currentAgent == null) 
    		return 0;
    	else
    		return currentAgent.who;
    }
    
    public static void loadPositionsFromFloatBuffer (FloatBuffer fb) {
    	
    	Vector3f v1, v2, v3;
    	
    	for (int i=0;i<3;i++) {
    		
    		v1 = new Vector3f(fb.get(i*9+0), fb.get(i*9+1), fb.get(i*9+2));
			SLCameras.getCamera(i).getMutableDirection().set(v1);
			
    		v2 = new Vector3f(fb.get(i*9+3), fb.get(i*9+4), fb.get(i*9+5));
			SLCameras.getCamera(i).getMutablePosition().set(v2);
			
    		v3 = new Vector3f(fb.get(i*9+6), fb.get(i*9+7), fb.get(i*9+8));
			SLCameras.getCamera(i).getMutableUpVector().set(v3);
			
			SLCameras.getCamera(i).setDefaultView(v2, v1, v3);
    	}
    	
    	currentCamera = (int)fb.get(27);    	
    	float isOrtho = fb.get(29);
    	orthoView = (isOrtho > 0.0f);
    	
    	GameManager.getGameManager().setOverhead(SLCameras.orthoView);
    	
    }


}
