package torusworld.math;

// Represents a cylinder around the Y axis, with arbitrary top and bottom Y coords
public class Cylinder
{
    // Radius of the cylinder
    public float radius;
    // Top and Bottom Y coordinates of the cylinder
    public float top, bottom;

    public Cylinder()
    {
        this(0, 0, 0);
    }
    
    public Cylinder(float rad, float topY, float bottomY)
    {
        radius = rad;
        top = topY;
        bottom = bottomY;
    }
    
    public void set(Cylinder cyl)
    {
        radius = cyl.radius;
        top = cyl.top;
        bottom = cyl.bottom;
    }

    // Returns the radius of a bounding sphere containing this cylinder.
    // The sphere is centered around 0,0
    public float getBoundingSphereRadius()
    {
        return (float) Math.sqrt(getBoundingSphereRadiusSquared());
    }

    public float getBoundingSphereRadiusSquared()
    {
        float d1 = (float) Math.max(top * top, bottom * bottom);
        return d1 + radius * radius;
    }

    // Computes a scaled cylinder given the 3 scaling factors
    public void computeScaledCylinder(float xScale, float yScale, float zScale, Cylinder output)
    {
        output.radius = radius * (float) Math.max(xScale, zScale);
        output.top = top * yScale;
        output.bottom = bottom * yScale;
    }

    // Returns true iff point is inside the cylinder
    public boolean isPointInside(Vector3f point, float error)
    {
        if (point.y < bottom - error || point.y > top + error) return false;
        return point.x * point.x + point.z * point.z <= (radius + error) * (radius + error);
    }

    // Returns the square of the distance between point and the cylinder's center
    public float getSquaredDistanceToCenter(Vector3f point)
    {
        float dy = point.y - 0.5f * (top + bottom);
        return dy * dy + point.x * point.x + point.z * point.z;
    }

    /*
     * Not used yet
     * Warning: Never tested! Probably has a couple of typos here and there!
     
    // Intersects the half-line at origin along direction. Returns the "t" parameter.
    // If there is no intersection, returns a negative value.
    public float intersectRay(Vector3f origin, Vector3f direction)
    {
        float tcaps = -1, tsides = -1;

        // check the sides of the cylinder
        float a = direction.x * direction.x + direction.z * direction.z;
        float b = 2 * (origin.x * direction.x + origin.z * direction.z);
        float c = origin.x * origin.x + origin.z * origin.z - radius * radius;
        float d = b * b - 4 * a * c;

        if (d >= 0)
        {
            d = (float) Math.sqrt(d);
            // we need the minimum t
            float t = .5f * (-b - d);
            // but it must be positive
            if (t < 0) t = .5f * (-b + d);
            if (t >= 0)
            {
                // See if we intersect it in the top-bottom range
                float y = origin.y + t * direction.y;
                if (y <= top && y >= bottom)
                    tsides = t;
            }
        }

        // check the caps of the cylinder
        if (direction.y > 0.0001 || direction.y < -0.0001)
        {
            float t1, t2;
            // intersect with Y = top plane
            t1 = (top - origin.y) / (direction.y);
            // check if intersection is within cylinder (we can use the above equation)
            if (t1 >= 0 && a * t1*t1 + b * t1 + c > 0) t1 = -1;
           
            // intersect with Y = top plane
            t2 = (bottom - origin.y) / (direction.y);
            if (t1 >= 0 && a * t2*t2 + b * t2 + c > 0) t2 = -1;

            // select minimum positive
            if (t1 >= 0 && (t2 < 0 || t1 < t2))
                tcaps = t1;
            else
                tcaps = t2;
        }

        // return minimum positive
        if (tcaps >= 0 && (tsides < 0 || tcaps < tsides))
            return tcaps;
        else
            return tsides;
    } */
}
