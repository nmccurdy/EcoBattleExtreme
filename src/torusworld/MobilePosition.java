package torusworld;

import starjava.Agent;
import starlogoc.TurtleManager;
import torusworld.math.CoordinateSystem;
import torusworld.math.Cylinder;
import torusworld.math.Vector3f;

public class MobilePosition
{
    /**
     * Holds the basic positioning parameters - those that we can read from 
     * the VM
     */
    static public class Basic
    {
        /** X position of the mobile, scaled to SpaceLand coords */
        public float x = 0;
        /** Height above ground of the mobile, scaled to SpaceLand coords */
        public float height = 0;
        /** Z position of the mobile, scaled to SpaceLand coords */
        public float z = 0;
        /** Moible Heading in degrees */
        public float heading = 0;
        
        /**
         * Sets the value of this to the value of p.
         * 
         * @modifies this
         */
        public void set(Basic p)
        {
            x = p.x;
            height = p.height;
            z = p.z;
            heading = p.heading;
        }
        
        /**
         * Reads the VM position from TurtleManager.
         * 
         * @modfies this
         */ 
        public void setFromVM(Agent agent)
        {
        	// NJM Hack!!! fix this
            x = SLTerrain.fromLogoX(agent.getX() + 50.5);
            z = -SLTerrain.fromLogoZ(agent.getY() + 50.5);
            height = SLTerrain.fromLogoHeight(agent.getHeightAboveTerrain());        
//            x = (float) agent.getX();
//            z = (float) agent.getY();
//            height = (float) agent.getHeightAboveTerrain();        

        	heading = (float) agent.getHeading();
        }
        
        /**
         * Reads the VM-reported position of a bounce from TurtleManager.
         * 
         * @modifies this
         */
        public void setFromVMBounce(TurtleManager tm, int bounce_num)
        {
//            x = SLTerrain.fromLogoX(tm.getBounceXcor(bounce_num));
//            z = SLTerrain.fromLogoZ(tm.getBounceYcor(bounce_num));
//            height = SLTerrain.fromLogoHeight(tm.getBounceHeight(bounce_num));
//            heading = (float) tm.getBounceHeading(bounce_num);
        }
        
        /**
         * Returns the distance between this and p.
         * 
         * @modifes this
         */
        public float distance(Basic p)
        {
            double dx = x - p.x;
            double dy = height - p.height;
            double dz = z - p.z;
            return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        }
        
        /**
         * Interpolates linearly between this and p, placing the result in res.
         * If bounce_heading is true, then heading is not interpolated; in this case,
         * res.heading is always equal to this.heading;
         * 
         * @modifes res
         */
        public void interpolate(Basic p, float t, boolean bounce_heading, Basic res)
        {
            res.x = x * (1.f - t) + p.x * t;
            res.height = height * (1.f - t) + p.height * t;
            res.z = z * (1.f - t) + p.z * t;
            
            if (bounce_heading)
            {
                res.heading = heading;
                return;
            }
            
            float adjustedHeading = heading;
            // adjust heading so that we take the shortest rotation
            if (p.heading - adjustedHeading > 180.f) adjustedHeading += 360.f;
            if (p.heading - adjustedHeading < -180.f) adjustedHeading -= 360.f;
            res.heading = adjustedHeading * (1.f - t) + p.heading * t;
        }
    }
    

    /**
     * Holds a local coordinate system and a bounding cylinder for the agent, as 
     * it appears in SpaceLand
     */
    static public class SpaceLand
    {
        public CoordinateSystem localCoordSys = new CoordinateSystem();
        public Cylinder boundingCyl = new Cylinder();
        
        /**
         * Sets the value of this to the value of p.
         * 
         * @modifies this
         */
        public void set(SpaceLand p)
        {
            localCoordSys.set(p.localCoordSys);
            boundingCyl.set(p.boundingCyl);
        }
        
        /**
         * Sets res to the global point corresponding to the local origin.
         * 
         * @modifes res
         */
        public void getCenterPoint(Vector3f res)
        {
            res.set(0, 0, 0);
            localCoordSys.localToGlobal(res);
        }
    }
}
