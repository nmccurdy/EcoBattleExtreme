package torusworld;


/**
 * Interpolates between last VM position, next VM position, additionally going through
 * any bounces encountered.
 * 
 */
public class MobileInterpolator
{
    private MobilePosition.Basic last = new MobilePosition.Basic();
    private MobilePosition.Basic next = new MobilePosition.Basic();
    
    private static final int MAX_BOUNCES = starlogoc.StarLogo.MAX_BOUNCES;
    private MobilePosition.Basic[] bounces = new MobilePosition.Basic[MAX_BOUNCES];
    private int numBounces;
    
    private float[] bounceT = new float[MAX_BOUNCES];
    
    public MobileInterpolator()
    {
        for (int i = 0; i < MAX_BOUNCES; i++)
            bounces[i] = new MobilePosition.Basic();
        numBounces = 0;
        bounceT[0] = 1e10f;
    }
    
    /**
     * Resets the interpolator to an initial position.
     * 
     * @modifies this
     */
    public void init(MobilePosition.Basic p)
    {
        last.set(p);
        next.set(p);
        numBounces = 0;
        bounceT[0] = 1e10f;
    }
    
    /**
     * Set the next destination. After method returns, t = 1 corresponds to
     * the given position, t = 0 to the last position. Clears bounces.
     * 
     * @modifies this
     * @effects clears bounces.
     */
    public void setNext(MobilePosition.Basic p)
    {
        last.set(next);
        next.set(p);
        numBounces = 0;
        bounceT[0] = 100;
    }
    
    /**
     * Add a bounce position. Bounces must be added after setNext().
     * processBounces() must be called after all bounces have been added. 
     *  
     * @modifies this
     */
    public void addBounce(MobilePosition.Basic p)
    {
        bounces[numBounces++].set(p);
    }
    
    /**
     * Process bounces added since last init or setNext.
     * 
     * @modifies this
     */
    public void processBounces()
    {
        if (numBounces <= 0) return;
        float dist = last.distance(bounces[0]);
        for (int i = 0; i < numBounces; i++)
        {
            bounceT[i] = dist;
            if (i < numBounces - 1)
                dist += bounces[i].distance(bounces[i+1]);
        }
        dist += bounces[numBounces-1].distance(next);
        
        // we now have partial distances in bounceT, and total distance in dist
        if (dist < 1e-3f) dist = 1e-3f;
        for (int i = 0; i < numBounces; i++)
            bounceT[i] /= dist;
        
        // Compute bounce heights ourselves, VM bounce heights are not consistent
        // with our interpolation. Might be inaccurate for very large movements,
        // but way better for short movements.
        
        for (int i = 0; i < numBounces; i++)
            bounces[i].height = last.height + (next.height - last.height) * bounceT[i];
    }
    
    
    /**
     * Inerpolates and places the result in res.
     * 
     * @modifies res
     */
    public void interpolate(float t, MobilePosition.Basic res)
    {
        if (numBounces <= 0)
        {
            last.interpolate(next, t, false, res);
            return;
        }
        MobilePosition.Basic prevPos = last;
        float prevT = 0;
        for (int i = 0; i < numBounces; i++)
        {
            if (t <= bounceT[i])
            {
                float deltaT = bounceT[i] - prevT;
                if (deltaT < 1e-3f) deltaT = 1e-3f; 
                prevPos.interpolate(bounces[i], (t - prevT) / deltaT, true, res);
                return;
            }
            prevPos = bounces[i];
            prevT = bounceT[i];
        }
        
        float deltaT = 1.f - prevT;
        if (deltaT < 1e-2f) deltaT = 1e-2f;
        prevPos.interpolate(next, (t - prevT) / deltaT, false, res);
    }
    
    /**
     * Returns true if the agent is currently moving.
     * Note: if agent is just changing heading, returns false.
     * 
     * @return true iff x, height, or z of agent are changing.
     */
    public boolean isMoving()
    {
        return (numBounces > 0 ||
               Math.abs(next.x - last.x) > 1e-5 ||
               Math.abs(next.height - last.height) > 1e-5 ||
               Math.abs(next.z - last.z) > 1e-5);
    }
    
    /**
     * Returns true if the agent is currently rotating.
     * 
     * @return true if heeading is changing.
     */
    public boolean isRotating()
    {
        return Math.abs(next.heading - last.heading) > 1e-5;
    }
}
