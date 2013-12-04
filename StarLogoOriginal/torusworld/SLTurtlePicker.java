package torusworld;

import java.util.Iterator;

import javax.swing.event.EventListenerList;

import torusworld.math.Cylinder;
import torusworld.math.Vector3f;

public class SLTurtlePicker
{
    private static Mobile pickedTurtle = null;
    private static EventListenerList listeners = new EventListenerList(); 
    
    //private static Vector3f rayOrigin = new Vector3f(), rayDirection = new Vector3f(), o = new Vector3f();
    
    public static void noPick()
    { 
        if (pickedTurtle != null)
        {
            pickedTurtle = null;
            SLTurtlePickerListener[] ls = listeners.getListeners(SLTurtlePickerListener.class); 
        	for (int j = 0; j < ls.length; j++)
                ls[j].nothingHovered();            
        }
    }

    public static void update(Vector3f point, Turtles turtles)
    {
        // first check if the point is not on the ground
        // (we don't want to select things THROUGH the ground)
        float ty = SLTerrain.getPointHeight(point.x, point.z);
       if (Math.abs(point.y - ty) < 0.3)
        {
            noPick();
            return;
        }

        float minDist = Float.MAX_VALUE; 	// smallest distance to center of cylinder
        Mobile minTurtle = null; 			// the Mobile of that intersection
        
        Vector3f cylpoint = new Vector3f();

        // loop over all the Mobiles to see if the ray intersects any of them
        Iterator<Mobile> iter = turtles.getTurtleIterator();
        while(iter.hasNext()) 
        {
            Mobile m = iter.next();

            if (m == null || m.getAnimationData() == null) continue;
            
            Cylinder cyl = m.spaceLandPosition.boundingCyl;
            cylpoint.set(point);

            m.spaceLandPosition.localCoordSys.globalToLocal(cylpoint);

            if (cyl.isPointInside(cylpoint, 0.1f))
            {
                // if the point happens to be inside several cylinders,
                float dist = cyl.getSquaredDistanceToCenter(cylpoint);
                if (dist < minDist)
                {
                    minDist = dist;
                    minTurtle = m;
                }
            }
        }
        if (minTurtle == null)
        {
            noPick();
            return;
        }
        
        pickedTurtle = minTurtle;
        SLTurtlePickerListener[] ls = listeners.getListeners(SLTurtlePickerListener.class); 
    	for (int j = 0; j < ls.length; j++)
            ls[j].mobileHovered(pickedTurtle);                    
    }
    
    /**
     * Adds SLTurtlePickerListner l to this
     */
    public static void addListener(SLTurtlePickerListener l) {
    	listeners.add(SLTurtlePickerListener.class, l); 
    }
    
    /**
     * Removes SLTurtlePickerListner l from this 
     */
    public static void removeListener(SLTurtlePickerListener l) {
    	listeners.remove(SLTurtlePickerListener.class, l);
    }

}
