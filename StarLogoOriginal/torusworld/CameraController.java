package torusworld;

import torusworld.math.Vector3f;


public class CameraController
{
    Camera cam;
    
    public CameraController(Camera _cam)
    {
        cam = _cam;
    }
    
    // commands
    public static final int MOVE_WEST = 0, MOVE_EAST = 1, MOVE_NORTH = 2, MOVE_SOUTH = 3;
    public static final int MOVE_FORWARD = 4, MOVE_BACK = 5, MOVE_UP = 6, MOVE_DOWN = 7;
    public static final int ROTATE_LEFT = 8, ROTATE_RIGHT = 9, ROTATE_UP = 10, ROTATE_DOWN = 11;
    
    private static float STEP = 2.f, ANGLE_STEP = (float) Math.PI / 72.0f;
    
    // used for acceleration
    private static final float ACCELERATION = 1.2f, MAX_MULTIPLIER = 100;
    float multiplier;
    
    // if repeat = true, we are in a key autorepeat mode, multiplier will update to provide acceleration
    public void processCommand(int command, boolean repeat)
    {
        if (repeat)
            multiplier = Math.min(multiplier * ACCELERATION, MAX_MULTIPLIER);
        else
            multiplier = 1.f;
        
        
        // used for North, South (move regardless of the camera up/down rotation)
        Vector3f dir = cam.getMutableDirection();
        float scale = (float) Math.sqrt(dir.x * dir.x + dir.z * dir.z);
        if (scale != 0) scale = multiplier * STEP / scale;
        
        
        switch (command)
        {
            case MOVE_WEST:
                cam.translate(- multiplier * STEP, 0, 0);
                break;
                
            case MOVE_EAST:
                cam.translate(multiplier * STEP, 0, 0);
                break;
                
            case MOVE_NORTH:
                cam.translateGlobal(dir.x * scale, 0, dir.z * scale);
                break;
                
            case MOVE_SOUTH:
                cam.translateGlobal(- dir.x * scale, 0, - dir.z * scale);
                break;
                
            case MOVE_FORWARD:
                cam.translate(0, 0, - multiplier * 4 * STEP);
                break;
                
            case MOVE_BACK:
                cam.translate(0, 0, multiplier * 4 * STEP);
                break;
                
            case MOVE_UP:
                cam.translateGlobal(0, multiplier * STEP, 0);
                break;
                
            case MOVE_DOWN:
                cam.translateGlobal(0, - multiplier * STEP, 0);
                break;
                
            case ROTATE_LEFT:
                cam.rotate(0, multiplier * ANGLE_STEP, 0);
                break;
                
            case ROTATE_RIGHT:
                cam.rotate(0, - multiplier * ANGLE_STEP, 0);
                break;
                
            case ROTATE_UP:
                cam.rotate(multiplier * ANGLE_STEP, 0, 0);
                break;
                
            case ROTATE_DOWN:
                cam.rotate(- multiplier * ANGLE_STEP, 0, 0);
                break;
                
        }
    }
        
    public void processCommand(int command)
    {
        processCommand(command, false);
    }
}
        

/*
 
 // feed in start point(p1), 2 control points(p2&p3), an end point(p4),
 // and the gradient of change in terrain height from 0-1(t) to get a point on the curve.
 
public static Vector3f PointOnCurve(Vector3f p1, Vector3f p2, Vector3f p3, Vector3f p4, float t)
{
float var1, var2, var3;
Vector3f vPoint = new Vector3f (0.0f, 0.0f, 0.0f);

var1 = 1 - t;
var2 = var1 * var1 * var1;
var3 = t * t * t;

vPoint.x = var2*p1.x + 3*t*var1*var1*p2.x + 3*t*t*var1*p3.x + var3*p4.x;
vPoint.y = var2*p1.y + 3*t*var1*var1*p2.y + 3*t*t*var1*p3.y + var3*p4.y;
vPoint.z = var2*p1.z + 3*t*var1*var1*p2.z + 3*t*t*var1*p3.z + var3*p4.z;

return(vPoint);             
}
*/