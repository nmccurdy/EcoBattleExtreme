package torusworld.math;

public final class Vector3f implements Cloneable {
    public float x;
    public float y;
    public float z;

    public Vector3f() 
    {
    }

    public Vector3f(float _x, float _y, float _z) 
    {
        set(_x, _y, _z);
    }
    
    public Vector3f(Vector3f vec)
    {
        set(vec);
    }

	public Vector3f(float[] coords)
	{
        set(coords[0], coords[1], coords[2]);
	}
	
	public Vector3f clone() 
	{
		return new Vector3f(x, y, z);
	}

	public float[] toFloatArray()
	{
		return new float[]{x, y, z};
	}

    public void fillFloatArray(float [] array, int offset)
    {
        array[offset] = x;
        array[offset + 1] = y;
        array[offset + 2] = z;
    }

    public void set(float _x, float _y, float _z)
    {
        x = _x;
        y = _y;
        z = _z;
    }
    
    public void set(Vector3f vec)
    {
        x = vec.x;
        y = vec.y;
        z = vec.z;
    }

    
    // Static operators

    
    // Sum
	/** Returns sum of two vectors */
    public static Vector3f add(Vector3f v1, Vector3f v2) 
    {
        return new Vector3f(v1.x + v2.x, v1.y + v2.y, v1.z + v2.z);
    }
    
    /** Computes sum of two vectors into vector res (returns res) */
    public static void add(Vector3f v1, Vector3f v2, Vector3f res)
    {
        res.set(v1.x + v2.x, v1.y + v2.y, v1.z + v2.z);
    }
    
    // Difference
    /** Returns difference of two vectors */
    public static Vector3f subtract(Vector3f v1, Vector3f v2) 
    {
        return new Vector3f(v1.x - v2.x, v1.y - v2.y, v1.z - v2.z);
    }
    
    /** Computes sum of two vectors into vector res (returns res) */
    public static void subtract(Vector3f v1, Vector3f v2, Vector3f res)
    {
        res.set(v1.x - v2.x, v1.y - v2.y, v1.z - v2.z);
    }

    // Negation
    /** Returns -v */
    public static Vector3f negate(Vector3f v) 
    {
        return new Vector3f(-v.x,-v.y,-v.z);
    }

    /** Computes -v into res */
    public static void negate(Vector3f v, Vector3f res)
    {
        res.set(-v.x, -v.y, -v.z);
    }

    // Multiplication by scalar 
    /** Returns a new vector that is v * scalar */
    public static Vector3f mult(Vector3f v, float scalar) 
    {
        return new Vector3f(v.x*scalar, v.y*scalar, v.z*scalar);
    }

    /** Computes v * scalar into res */
    public static void mult(Vector3f v, float scalar, Vector3f res) 
    {
        res.set(v.x*scalar, v.y*scalar, v.z*scalar);
    }

    // Cross product
    /** Returns cross product of this vector with another vector (does not modify self) */
    public static Vector3f cross(Vector3f v1, Vector3f v2) 
    {
        return new Vector3f(
            ((v1.y * v2.z) - (v1.z * v2.y)),
            ((v1.z * v2.x) - (v1.x * v2.z)),
            ((v1.x * v2.y) - (v1.y * v2.x)));
    }
    
    /** Computes cross product of two vectors into res */
    public static void cross(Vector3f v1, Vector3f v2, Vector3f res) 
    {
        res.set(
            ((v1.y * v2.z) - (v1.z * v2.y)),
            ((v1.z * v2.x) - (v1.x * v2.z)),
            ((v1.x * v2.y) - (v1.y * v2.x)));
    }

    // Dot product
    /** Returns dot product of two vectors */
    public static float dot(Vector3f v1, Vector3f v2) 
    {
        return v1.x*v2.x + v1.y*v2.y + v1.z*v2.z;
    }

    // Angle
    /** Returns the angle between two vectors **/
	public static float angleBetween (Vector3f v1, Vector3f v2)
	{
		return (float) Math.acos(dot(v1, v2)/(v1.length() * v2.length()));
	}
    
    
    // Operators on this
    
    /** Returns length of this vector. */
    public float length() 
    {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    /** Returns the squared length of this vector */
    public float lengthSquared() 
    {
        return x * x + y * y + z * z;
    }


    /** Scales this vector by a scalar */
    public void mult(float scalar)
    {
    	x *= scalar;
    	y *= scalar;
    	z *= scalar;
    }

    /** Adds a vector to this vector */
    public void add(Vector3f vec) 
    {
        x += vec.x;
        y += vec.y;
        z += vec.z;
    }
    
    /** Adds a vec * alpha to this vector */
    public void addScaled(Vector3f vec, float alpha) 
    {
        x += vec.x * alpha;
        y += vec.y * alpha;
        z += vec.z * alpha;
    }

    /** Negats this vector */
    public void negate() 
    {
        x = -x;
        y = -y;
        z = -z;
    }

    /** Subtracts a vector from this vector */
    public void subtract(Vector3f vec) 
    {
        x -= vec.x;
        y -= vec.y;
        z -= vec.z;
    }
    

    /** Normalizes vector to unit length, changes self */
    public void normalize()
	{
        float length = length();

        if(length > 0)
        {
            float invLength = 1.0f / length;
            
            x *= invLength;
            y *= invLength;
            z *= invLength;
        }
    }
	
    /** Tests if this vector equals another */
    public boolean equals(Vector3f otherVector) 
    {
		if (otherVector.x == this.x &&
		    otherVector.y == this.y &&
		    otherVector.z == this.z)
			return true;
		return false;
    }
    
    public boolean equals(Vector3f otherVector, float eps) 
    {
        float d;
        d = otherVector.x - x;
        if (d < -eps || d > eps) return false;
        d = otherVector.y - y;
        if (d < -eps || d > eps) return false;
        d = otherVector.z - z;
        if (d < -eps || d > eps) return false;
        return true;
    }

    public String toString() 
    {
        return "[X=" + x + ", Y=" + y + ", Z=" + z + "]";
    }
}
