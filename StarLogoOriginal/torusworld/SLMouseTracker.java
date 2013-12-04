package torusworld;

import javax.media.opengl.GL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import torusworld.math.Vector3f;

public class SLMouseTracker
{
    // We only read back the depth every FRAME_INTERVAL frames (and if the mouse moves).  
    // This is a tradeoff between responsiveness and performance (glReadPixels is expensive).
    // Note that this only affects performance when mouse is moved around a lot.
    private static final int NORMAL_FRAME_INTERVAL = 5;
    private static int frameInterval = NORMAL_FRAME_INTERVAL;
    private static int curFrame = 0;
    private static boolean mouseMoved = false;
    private static int x, y;
    private static float z;  // glReadPixel depth result for (x,y)
    private static Vector3f point = new Vector3f(); // 3D-world point under the cursor (gluUnproject result)
    private static Camera cameraUsed;

    public static synchronized void update(Camera cam, int nx, int ny)
    {
        x = nx;
        y = ny;
        mouseMoved = true; 
        cameraUsed = cam;
    }

    private static ByteBuffer buf = ByteBuffer.allocateDirect(8).order(ByteOrder.nativeOrder());
    public static synchronized void frameRendered(Turtles turtles)
    {
        GL gl = SLRendering.getGL();

        curFrame++;
        
        if (!mouseMoved || curFrame % frameInterval != 0) return;
        mouseMoved = false;

        buf.rewind();
        gl.glReadPixels(x, y, 1, 1, GL.GL_DEPTH_COMPONENT, GL.GL_FLOAT, buf);
        z = buf.getFloat();
        
        cameraUsed.unProject(x, y, z, point);
        SLTurtlePicker.update(point, turtles);
    }
    
    public static synchronized Vector3f getPoint(){
    	//frameRendered();
    	return point;
    }
    
    public static synchronized void setRealTime(boolean realTime)
    {
        frameInterval = realTime ? 1 : NORMAL_FRAME_INTERVAL;
    }
}
