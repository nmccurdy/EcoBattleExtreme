package torusworld;

import javax.media.opengl.GL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import torusworld.math.Vector3f;

public class SLCameraMovement
{
    private static boolean showCone = false;
    private static boolean readCoordinates = false;
    
    private static int x, y; // start mouse coordinates
    private static float z;  // glReadPixel depth result for (x,y)
    private static Vector3f point = new Vector3f(); // 3D-world point under the cursor (gluUnproject result)
    private static Vector3f initial_cam_pos = new Vector3f(); // camera position after the last frame render
    
    private static int last_x, last_y; // used to calcualte deltas on rotate
    private static float zoom_amount, zoom_sign; // wheel units to move in case of zoom
    
    private static final float rotateHScale = 0.028f;
    private static final float rotateVScale = - 0.014f;
    private static final float zoomFactor = 1.01f; 
    private static final float wheelZoomScale = 20.f; 
    private static final float wheelZoomSpeed = 300.f; 
    
    
    public static int getStartX() { return x; }
    public static int getStartY() { return y; }
    
    public static boolean isStarted() { return showCone; }
    public static void tryStart(int mx, int my)
    {
        showCone = true;
        readCoordinates = true;
        x = mx;
        y = my;
        last_x = x;
        last_y = y;
        zoom_amount = 0;
        initial_cam_pos.x = -1e10f;
    }
    
    public static void tryScheduleWheel(int mx, int my, int units)
    {
        tryStart(mx, my);
        zoom_sign = (units >= 0) ? 1 : -1;
        zoom_amount = units * zoom_sign * wheelZoomScale;
    }
        
    
    public static void stop()
    {
        showCone = false;
        readCoordinates = false;
        zoom_amount = 0;
    }
    
    private static Vector3f delta = new Vector3f();
    public static void pan(int nx, int ny)
    {
        // check if we actually got to read the point
        if (initial_cam_pos.x == -1e10f) return;
        Camera cam = SLCameras.getPerspectiveCamera();
        
        cam.unProject(nx, ny, z, delta);
        delta.negate();
        delta.add(point);

        cam.getMutablePosition().set(initial_cam_pos); // only useful for multiple pan calls per frame (low fps)
        cam.translateGlobal(delta.x, delta.y, delta.z);
    }
    
    public static void rotate(int nx, int ny)
    {
        // check if we actually got to read the point
        if (initial_cam_pos.x == -1e10f) return;
        Camera cam = SLCameras.getPerspectiveCamera();
        CameraConstrainer con = SLCameras.getPerspectiveConstrainer();
        
        // experimental rotation code
        cam.rotateHorizontallyAround(point, (nx - last_x) * rotateHScale);
        con.enforceConstraints();
        cam.rotateVerticallyAround(point, (ny - last_y) * rotateVScale);
        con.enforceConstraints();
        last_x = nx;
        last_y = ny;
    }
    
    public static void zoom(int nx, int ny)
    {
        zoom(last_y - ny);
        last_y = ny;
    }
    
    public static void zoom(float amount)
    {
        // check if we actually got to read the point
        if (initial_cam_pos.x == -1e10f) return;
        Camera cam = SLCameras.getPerspectiveCamera();
        
        float sz = SLCameras.convertDepthFromGL(z);
        sz *= Math.pow(zoomFactor, amount);
        z = SLCameras.convertDepthToGL(sz);
        
        cam.unProject(x, y, z, delta);
        delta.negate();
        delta.add(point);
        cam.translateGlobal(delta.x, delta.y, delta.z);
        
        readCoordinates = true; // z might not be accurate if the camera is constrained
    }
    
    private static ByteBuffer buf = ByteBuffer.allocateDirect(8).order(ByteOrder.nativeOrder());

    // shold be called after perspective view has been rendered 
    public static void frameRendered()
    {
        GL gl = SLRendering.getGL();

        initial_cam_pos.set(SLCameras.getPerspectiveCamera().getMutablePosition());
        if (readCoordinates)
        {
            // This is not a performance problem: the glReadPixels only happens once per 
            // button-click (or during zooming)
            buf.rewind();
            gl.glReadPixels(x, y, 1, 1, GL.GL_DEPTH_COMPONENT, GL.GL_FLOAT, buf);
            z = buf.getFloat();
        
            SLCameras.getPerspectiveCamera().unProject(x, y, z, point);
            readCoordinates = false;
            
            // check whether we like where the point is - we don't want to rotate around a point on skybox
            if (point.x < SLTerrain.getMinX() || point.x > SLTerrain.getMaxX() ||
                point.z < SLTerrain.getMinZ() || point.z > SLTerrain.getMaxZ() ||
                (point.y < 0 && point.y < SLTerrain.getPointHeight(point.x, point.z) - 50.f))
            {
                stop();
                return;
            }
            
        }
        
        if (showCone)
        {
            gl.glPushMatrix();
            gl.glTranslatef(point.x, point.y, point.z);
            gl.glRotated(-90, 1, 0, 0);
            // convert OpenGL Z to Screen-space Z and scale
            float factor = 0.008f * SLCameras.convertDepthFromGL(z);
            gl.glScalef(factor, factor, -factor);
            gl.glTranslatef(0, 0, -10);
            
            gl.glEnable(GL.GL_LIGHTING);
            gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
            
            gl.glColor3f(1, 0, 0);
            SLRendering.getGLUT().glutSolidCone(1, 10, 12, 1);
            
            gl.glEnable(GL.GL_BLEND);
            gl.glDisable(GL.GL_DEPTH_TEST);
            gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
            gl.glColor4f(1, 0, 0, 0.2f);
            SLRendering.getGLUT().glutSolidCone(1, 10, 12, 1);
            gl.glPopMatrix();
        }
        
        if (zoom_amount > 0)
        {
            // we have been scheduled only to scale
            float amount = wheelZoomSpeed / SLRendering.getCurrentFPS();
            if (amount > zoom_amount)
                amount = zoom_amount;
            zoom(amount * zoom_sign);
            zoom_amount -= amount;
            if (zoom_amount <= 0.001) 
                stop();
        }

    }
    
    
}
