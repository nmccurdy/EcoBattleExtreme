package torusworld;

import torusworld.math.Vector3f;
import torusworld.math.Matrix3f;
import torusworld.math.Quaternion;

/*
 * We use Verlet integration to solve for the basic mass-spring equation
 * x" = k (xt - x)
 * Where x is the camera position, xt is the "target" position
 * 
 * We use Time Corrected Verlet integration. Instead of including a damping factor fx', 
 * we use the friction-modified Verlet equations.  
 */

public class CameraSmoother
{
    public static final double K = 50;
    public static final double F = 15;
    
    public static final double directionKScale = 0.8;
    
    private class VectorSmoother
    {
        double ux, uy, uz;
        double vx, vy, vz;
        
        int step = 0;
        
        public void reset()
        {
            step = 0;
        }
        
        public void smoothVector(Vector3f target, double dt, double dtr)
        {
            double tx = target.x, ty = target.y, tz = target.z;
            step++;
            if (step > 2)   // if we have enough data
            {
                double k = K * dt * dt;
                double f = F * dt;
//                // Forward Verlet Integration
//                tx = (1 + dtr - f) * ux - (dtr - f) * vx + k * (tx - ux);
//                ty = (1 + dtr - f) * uy - (dtr - f) * vy + k * (ty - uy);
//                tz = (1 + dtr - f) * uz - (dtr - f) * vz + k * (tz - uz);
//                System.out.println("   after: " + target);
                
              // Backward Verlet Integration (use next pos for accelartion computation)
              // Should always be stable :)
              tx = 1.0/(1.0 + k) * ((1 + dtr - f) * ux - (dtr - f) * vx + k * tx);
              ty = 1.0/(1.0 + k) * ((1 + dtr - f) * uy - (dtr - f) * vy + k * ty);
              tz = 1.0/(1.0 + k) * ((1 + dtr - f) * uz - (dtr - f) * vz + k * tz);
              
                
            }
            vx = ux;
            vy = uy;
            vz = uz;
            
            ux = tx;
            uy = ty;
            uz = tz;
            
            target.set((float) tx, (float) ty, (float) tz);
        }
    }
    
    private class MatrixSmoother
    {
        private Quaternion x1 = new Quaternion();
        private Quaternion x2 = new Quaternion();
        
        int step = 0;
        
        public void reset()
        {
            step = 0;
        }
        
        public void smoothMatrix(Matrix3f target, double dt, double dtr)
        {
            step++;
                        
            Quaternion xt = new Quaternion();
            xt.fromRotationMatrix(target);
            
            if (step > 2) // if we have enough data
            {
                double k = directionKScale * K * dt * dt;
                double f = F * dt;
                
//                //Forward Verlet Integration
//                
//                //res = (2 - f) * x1 - (1 - f) * x2 + k * (xt - x1)
//                //res = (2 - f - k) * x1 + (f - 1) * x2 + k * xt
//                //res = (2 - f - k) * x1 + (1 - (2 - f - k) - k) * x2 + k * xt
//                //2 * res = 2 * (2 - f - k) * x1 + (2 - 2 * (2 - f - k) - 2 * k) * x2 + 2 * k * xt
//                //2 * res = slerp(x2, x1, 2 * (2 - f - k)) + slerp(x2, xt, 2 * k);
//                //res = slerp(slerp(x2, x1, 2 * (2 - f - k)), slerp(x2, xt, 2 * k), 0.5f);
//                
//                // time correction: replace 2 - f with 1 + dtr - f (and 1 - f with dtr - f)

//                xt = Quaternion.slerp(Quaternion.lerp(x2, x1, 2 * (1 + dtr - f - k)), 
//                                      Quaternion.slerp(x2, xt, 2 * k), 
//                                      0.5);  
                
                
                //Backward Verlet Integration
                // (not perfectly stable because quaternion "driving force" can change between one rotation direction
                //  to the other if there are large fluctuations; but more stable than the forward).
                
                //res = 1/(1+k) ((2 - f) * x1 - (1 - f) * x2 + k * xt)
                //res = (2-f)/(1+k) * x1 + (1 - (2-f)/(1+k) - k/(1+k)) * x2+ k/(1+k) * xt
                //2 * res = 2 * (2-f)/(1+k) * x1 + (2 - 2 * (2-f)/(1+k) - 2 * k/(1+k)) * x2) + 2 * k/(1+k) * xt
                //2 * res = slerp(x2, x1, 2 * (2-f)/(1+k)) + slerp(x2, xt, 2 * k/(1+k))
                //res = slerp(slerp(x2, x1, 2 * (2-f)/(1+k)), slerp(x2, xt, 2 * k/(1+k)), 0.5f)

                // time correction: replace 2 - f with 1 + dtr - f (and 1 - f with dtr - f)
                
                xt = Quaternion.slerp(Quaternion.lerp(x2, x1, 2 * (1 + dtr - f)/(1 + k)), 
                                      Quaternion.lerp(x2, xt, 2 * k/(1 + k)), 
                                      0.5);
                
                
                
                target.set(xt);
            }
            x2 = x1;
            x1 = xt;
        }
    }

    
    private VectorSmoother pos = new VectorSmoother();
    //private VectorSmoother dir2 = new VectorSmoother();
    private MatrixSmoother dir = new MatrixSmoother();
    
    double lastdt = -1;
    public void reset()
    {
        pos.reset();
        dir.reset();
        lastdt = -1;
    }
    
    private final double minStepRatio = 0.7;
    private final double maxStepRatio = 1.4;
    private TorusWorld tw;
    
    public CameraSmoother(TorusWorld tw) {
    	this.tw = tw;
    }
    
    public void smoothCamera(Camera cam, double dt)
    {
    	//Don't need, causes glitchiness. - actually, we do, to prevent instability
        if (dt > 0.8f / F && !tw.isAnythingMoving()) // only reset if framerates are low
            reset();

        double dtr = 1;
        if (lastdt > 0)
        {
            dtr = dt / lastdt;
            // bound variance in time step increase..
            // Animation will be a little slower/faster for a couple of frames, but this will 
            // prevent ugly "skipping" due to other processes taking CPU between frames 
            // (like the VM), causing loss of accuracy in the integrator).
            if (dtr > maxStepRatio)
            {
                dt = lastdt * maxStepRatio;
                dtr = maxStepRatio;
            } else
            if (dtr < minStepRatio)
            {
                dt = lastdt * minStepRatio;
                dtr = minStepRatio;
            }
        }
        lastdt = dt;
        
        pos.smoothVector(cam.getMutablePosition(), dt, dtr);
        
        Matrix3f mat = new Matrix3f();
        cam.computeRotationMatrix(mat);
        dir.smoothMatrix(mat, dt, dtr);
        cam.fromRotationMatrix(mat);
    }
}


/*
private class DirectionSmoother
{
    private Quaternion x1 = new Quaternion();
    private Quaternion x2 = new Quaternion();
    
    int step = 0;
    
    public void reset()
    {
        step = 0;
    }
    
    public void smoothVector(Vector3f target, double dt, double dtr)
    {
        step++;
        target.normalize();
        Vector3f axis = new Vector3f(0, 0, 1);
        
        Vector3f norm = Vector3f.cross(axis, target);
        float cos = Vector3f.dot(axis, target);
        float angle = (float) Math.acos(cos);
        if (norm.length() < 0.01f)
            norm.set(0, 1, 0);
        
        Quaternion xt = new Quaternion();
        xt.fromAngleAxis(angle, norm);
        
        if (step > 2) // if we have enough data
        {
            double k = K * dt * dt;
            double f = F * dt;
//          res = (2 - f) * x1 - (1 - f) * x2 + k * (xt - x1)
//          res = (2 - f - k) * x1 + (f - 1) * x2 + k * xt
//          res = (2 - f - k) * x1 + (1 - (2 - f - k) - k) * x2 + k * xt
//          2 * res = 2 * (2 - f - k) * x1 + (2 - 2 * (2 - f - k) - 2 * k) * x2 + 2 * k * xt
//          2 * res = 2 * (2 - f - k) * x1 + (2 - 2 * (2 - f - k) - 2 * k) * x2 + 2 * k * xt
//          2 * res = slerp(x2, x1, 2 * (2 - f - k)) + slerp(x2, xt, 2 * k);
//          res = slerp(slerp(x2, x1, 2 * (2 - f - k)), slerp(x2, xt, 2 * k), 0.5f);

            xt = Quaternion.slerp(Quaternion.slerp(x2, x1, 2 * (1 + dtr - f - k)), 
                                  Quaternion.slerp(x2, xt, 2 * k), 
                                  0.5f);
            
            target.set(axis);
            xt.rotate(target);
        }
        x2 = x1;
        x1 = xt;
    }
}
*/