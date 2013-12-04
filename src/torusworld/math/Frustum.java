package torusworld.math;

import torusworld.TorusWorld;

public class Frustum {
    	
    private float m_Frustum[][];
    private MatrixMath Maths;
    	
    // We create an enum of the sides so we don't have to call each side 0 or 1.
    // This way it makes it more understandable and readable when dealing with frustum sides.
    public static final int RIGHT	= 0;		// The RIGHT side of the frustum
    public static final int LEFT	= 1;		// The LEFT	 side of the frustum
    public static final int BOTTOM	= 2;		// The BOTTOM side of the frustum
    public static final int TOP	    = 3;		// The TOP side of the frustum
    public static final int BACK	= 4;		// The BACK	side of the frustum
    public static final int FRONT	= 5;			// The FRONT side of the frustum
    
    // Like above, instead of saying a number for the ABC and D of the plane, we
    // want to be more descriptive.
    public static final int A = 0;				// The X value of the plane's normal
    public static final int B = 1;				// The Y value of the plane's normal
    public static final int C = 2;				// The Z value of the plane's normal
    public static final int D = 3;				// The distance the plane is from the origin
    
    ///////////////////////////////// NORMALIZE PLANE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
    /////
    /////	This normalizes a plane (A side) from a given frustum.
    /////
    ///////////////////////////////// NORMALIZE PLANE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
    
    public Frustum(){
    	Maths = new MatrixMath();
    	m_Frustum= new float[6][4];
    }
    
    public void normalizePlane(float frustum[][], int side)
    {
    	// Here we calculate the magnitude of the normal to the plane (point A B C)
    	// Remember that (A, B, C) is that same thing as the normal's (X, Y, Z).
    	// To calculate magnitude you use the equation:  magnitude = sqrt( x^2 + y^2 + z^2)
    	float magnitude = (float)Math.sqrt( frustum[side][A] * frustum[side][A] +
    								   frustum[side][B] * frustum[side][B] +
    								   frustum[side][C] * frustum[side][C] );
    
    	// Then we divide the plane's values by it's magnitude.
    	// This makes it easier to work with.
    	frustum[side][A] /= magnitude;
    	frustum[side][B] /= magnitude;
    	frustum[side][C] /= magnitude;
    	frustum[side][D] /= magnitude;
    }
    
    
    ///////////////////////////////// CALCULATE FRUSTUM \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
    /////
    /////	This extracts our frustum from the projection and modelview matrix.
    /////
    ///////////////////////////////// CALCULATE FRUSTUM \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
    
    public void calculateFrustum(double proj[], double modl[])
    {
    							
    	float   clip[];								// This will hold the clipping planes
    	clip = new float[16];
    
    	// Now that we have our modelview and projection matrix, if we combine these 2 matrices,
    	// it will give us our clipping planes.  To combine 2 matrices, we multiply them.
    
    	clip[ 0] = (float) (modl[ 0] * proj[ 0] + modl[ 1] * proj[ 4] + modl[ 2] * proj[ 8] + modl[ 3] * proj[12]);
    	clip[ 1] = (float) (modl[ 0] * proj[ 1] + modl[ 1] * proj[ 5] + modl[ 2] * proj[ 9] + modl[ 3] * proj[13]);
    	clip[ 2] = (float) (modl[ 0] * proj[ 2] + modl[ 1] * proj[ 6] + modl[ 2] * proj[10] + modl[ 3] * proj[14]);
    	clip[ 3] = (float) (modl[ 0] * proj[ 3] + modl[ 1] * proj[ 7] + modl[ 2] * proj[11] + modl[ 3] * proj[15]);
    
    	clip[ 4] = (float) (modl[ 4] * proj[ 0] + modl[ 5] * proj[ 4] + modl[ 6] * proj[ 8] + modl[ 7] * proj[12]);
    	clip[ 5] = (float) (modl[ 4] * proj[ 1] + modl[ 5] * proj[ 5] + modl[ 6] * proj[ 9] + modl[ 7] * proj[13]);
    	clip[ 6] = (float) (modl[ 4] * proj[ 2] + modl[ 5] * proj[ 6] + modl[ 6] * proj[10] + modl[ 7] * proj[14]);
    	clip[ 7] = (float) (modl[ 4] * proj[ 3] + modl[ 5] * proj[ 7] + modl[ 6] * proj[11] + modl[ 7] * proj[15]);
    
    	clip[ 8] = (float) (modl[ 8] * proj[ 0] + modl[ 9] * proj[ 4] + modl[10] * proj[ 8] + modl[11] * proj[12]);
    	clip[ 9] = (float) (modl[ 8] * proj[ 1] + modl[ 9] * proj[ 5] + modl[10] * proj[ 9] + modl[11] * proj[13]);
    	clip[10] = (float) (modl[ 8] * proj[ 2] + modl[ 9] * proj[ 6] + modl[10] * proj[10] + modl[11] * proj[14]);
    	clip[11] = (float) (modl[ 8] * proj[ 3] + modl[ 9] * proj[ 7] + modl[10] * proj[11] + modl[11] * proj[15]);
    
    	clip[12] = (float) (modl[12] * proj[ 0] + modl[13] * proj[ 4] + modl[14] * proj[ 8] + modl[15] * proj[12]);
    	clip[13] = (float) (modl[12] * proj[ 1] + modl[13] * proj[ 5] + modl[14] * proj[ 9] + modl[15] * proj[13]);
    	clip[14] = (float) (modl[12] * proj[ 2] + modl[13] * proj[ 6] + modl[14] * proj[10] + modl[15] * proj[14]);
    	clip[15] = (float) (modl[12] * proj[ 3] + modl[13] * proj[ 7] + modl[14] * proj[11] + modl[15] * proj[15]);
    	
    	// Now we actually want to get the sides of the frustum.  To do this we take
    	// the clipping planes we received above and extract the sides from them.
    
    	// This will extract the RIGHT side of the frustum
    	m_Frustum[RIGHT][A] = clip[ 3] - clip[ 0];
    	m_Frustum[RIGHT][B] = clip[ 7] - clip[ 4];
    	m_Frustum[RIGHT][C] = clip[11] - clip[ 8];
    	m_Frustum[RIGHT][D] = clip[15] - clip[12];
    
    	// Now that we have a normal (A,B,C) and a distance (D) to the plane,
    	// we want to normalize that normal and distance.
    
    	// Normalize the RIGHT side
    	normalizePlane(m_Frustum, RIGHT);
    
    	// This will extract the LEFT side of the frustum
    	m_Frustum[LEFT][A] = clip[ 3] + clip[ 0];
    	m_Frustum[LEFT][B] = clip[ 7] + clip[ 4];
    	m_Frustum[LEFT][C] = clip[11] + clip[ 8];
    	m_Frustum[LEFT][D] = clip[15] + clip[12];
    
    	// Normalize the LEFT side
    	normalizePlane(m_Frustum, LEFT);
    
    	// This will extract the BOTTOM side of the frustum
    	m_Frustum[BOTTOM][A] = clip[ 3] + clip[ 1];
    	m_Frustum[BOTTOM][B] = clip[ 7] + clip[ 5];
    	m_Frustum[BOTTOM][C] = clip[11] + clip[ 9];
    	m_Frustum[BOTTOM][D] = clip[15] + clip[13];
    
    	// Normalize the BOTTOM side
    	normalizePlane(m_Frustum, BOTTOM);
    
    	// This will extract the TOP side of the frustum
    	m_Frustum[TOP][A] = clip[ 3] - clip[ 1];
    	m_Frustum[TOP][B] = clip[ 7] - clip[ 5];
    	m_Frustum[TOP][C] = clip[11] - clip[ 9];
    	m_Frustum[TOP][D] = clip[15] - clip[13];
    
    	// Normalize the TOP side
    	normalizePlane(m_Frustum, TOP);
    
    	// This will extract the BACK side of the frustum
    	m_Frustum[BACK][A] = clip[ 3] - clip[ 2];
    	m_Frustum[BACK][B] = clip[ 7] - clip[ 6];
    	m_Frustum[BACK][C] = clip[11] - clip[10];
    	m_Frustum[BACK][D] = clip[15] - clip[14];
    
    	// Normalize the BACK side
    	normalizePlane(m_Frustum, BACK);
    
    	// This will extract the FRONT side of the frustum
    	m_Frustum[FRONT][A] = clip[ 3] + clip[ 2];
    	m_Frustum[FRONT][B] = clip[ 7] + clip[ 6];
    	m_Frustum[FRONT][C] = clip[11] + clip[10];
    	m_Frustum[FRONT][D] = clip[15] + clip[14];
    
    	// Normalize the FRONT side
    	normalizePlane(m_Frustum, FRONT);
    }
    
    // The code below will allow us to make checks within the frustum.  For example,
    // if we want to see if a point, a sphere, or a cube lies inside of the frustum.
    // Because all of our planes point INWARDS (The normals are all pointing inside the frustum)
    // we then can assume that if a point is in FRONT of all of the planes, it's inside.
    
    ///////////////////////////////// POINT IN FRUSTUM \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
    /////
    /////	This determines if a point is inside of the frustum
    /////
    ///////////////////////////////// POINT IN FRUSTUM \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
    
    boolean pointInFrustum( float x, float y, float z )
    {
    	// If you remember the plane equation (A*x + B*y + C*z + D = 0), then the rest
    	// of this code should be quite obvious and easy to figure out yourself.
    	// In case don't know the plane equation, it might be a good idea to look
    	// at our Plane Collision tutorial at www.GameTutorials.com in OpenGL Tutorials.
    	// I will briefly go over it here.  (A,B,C) is the (X,Y,Z) of the normal to the plane.
    	// They are the same thing... but just called ABC because you don't want to say:
    	// (x*x + y*y + z*z + d = 0).  That would be wrong, so they substitute them.
    	// the (x, y, z) in the equation is the point that you are testing.  The D is
    	// The distance the plane is from the origin.  The equation ends with "= 0" because
    	// that is true when the point (x, y, z) is ON the plane.  When the point is NOT on
    	// the plane, it is either a negative number (the point is behind the plane) or a
    	// positive number (the point is in front of the plane).  We want to check if the point
    	// is in front of the plane, so all we have to do is go through each point and make
    	// sure the plane equation goes out to a positive number on each side of the frustum.
    	// The result (be it positive or negative) is the distance the point is front the plane.
    
    	// Go through all the sides of the frustum
    	for(int i = 0; i < 6; i++ )
    	{
    		// Calculate the plane equation and check if the point is behind a side of the frustum
    		if(m_Frustum[i][A] * x + m_Frustum[i][B] * y + m_Frustum[i][C] * z + m_Frustum[i][D] <= 0)
    		{
    			// The point was behind a side, so it ISN'T in the frustum
    			return false;
    		}
    	}
    
    	// The point was inside of the frustum (In front of ALL the sides of the frustum)
    	return true;
    }

    public boolean sphereInFrustum(float x, float y, float z, float r)
    {
    	// Test if any part of a sphere centered at (x, y, z) with radius r lies within the frustum.
    	// The sphere is completely outside the frustum if and only if the center is behind one of the
    	// planes by a distance that is at least the radius.
    
    	for (int i = 0; i < 6; i++)
    	    if (m_Frustum[i][A] * x + m_Frustum[i][B] * y + m_Frustum[i][C] * z + m_Frustum[i][D] <= -r)
    	        return false;
    
    	return true;
    }

	/**
	 * Calculate the bounding coordinates of the view frustum
	 * by repeatedly calculating intersections of the bounding planes
	 **/
	Vector3f[] getIntersections() throws FrustumIntersectionException
	{
		double a[][], b[], x[];

		a = new double[20][20];
		b = new double[20];
		x = new double[20];

		int    i, N_eqn = 3;

		Vector3f intersections[] = new Vector3f[8];
		for (i =0 ; i < 8; i++){
		   intersections[i] = new Vector3f(0,0,0);
		}
		try {
			// Intersect Bottom, Right and Back
			//Bottom
			a[0][0] = m_Frustum[BOTTOM][A];
			a[0][1] = m_Frustum[BOTTOM][B];
			a[0][2] = m_Frustum[BOTTOM][C];
			b[0]	= - m_Frustum[BOTTOM][D];
			//Right
			a[1][0] = m_Frustum[RIGHT][A];
			a[1][1] = m_Frustum[RIGHT][B];
			a[1][2] = m_Frustum[RIGHT][C];
			b[1]	= - m_Frustum[RIGHT][D];
			//Back
			a[2][0] = m_Frustum[BACK][A];
			a[2][1] = m_Frustum[BACK][B];
			a[2][2] = m_Frustum[BACK][C];
			b[2]	= - m_Frustum[BACK][D];
			if ( Maths.GaussianElim( a, b, x, N_eqn ))
			{
				intersections[0].x = (float)x[0];
				intersections[0].y = (float)x[1];
				intersections[0].z = (float)x[2];
			}

			// Intersect Bottom, Left and Back
			//Bottom
			a[0][0] = m_Frustum[BOTTOM][A];
			a[0][1] = m_Frustum[BOTTOM][B];
			a[0][2] = m_Frustum[BOTTOM][C];
			b[0]	= - m_Frustum[BOTTOM][D];
			//Left
			a[1][0] = m_Frustum[LEFT][A];
			a[1][1] = m_Frustum[LEFT][B];
			a[1][2] = m_Frustum[LEFT][C];
			b[1]	= - m_Frustum[LEFT][D];
			//Back
			a[2][0] = m_Frustum[BACK][A];
			a[2][1] = m_Frustum[BACK][B];
			a[2][2] = m_Frustum[BACK][C];
			b[2]	= - m_Frustum[BACK][D];

			if ( Maths.GaussianElim( a, b, x, N_eqn ))
			{
				intersections[1].x = (float)x[0];
				intersections[1].y = (float)x[1];
				intersections[1].z = (float)x[2];
			}

			// Intersect Top, Right and Back
			//Top
			a[0][0] = m_Frustum[TOP][A];
			a[0][1] = m_Frustum[TOP][B];
			a[0][2] = m_Frustum[TOP][C];
			b[0]	= - m_Frustum[TOP][D];
			//Right
			a[1][0] = m_Frustum[RIGHT][A];
			a[1][1] = m_Frustum[RIGHT][B];
			a[1][2] = m_Frustum[RIGHT][C];
			b[1]	= - m_Frustum[RIGHT][D];
			//Back
			a[2][0] = m_Frustum[BACK][A];
			a[2][1] = m_Frustum[BACK][B];
			a[2][2] = m_Frustum[BACK][C];
			b[2]	= - m_Frustum[BACK][D];

			if ( Maths.GaussianElim( a, b, x, N_eqn ))
			{
				intersections[2].x = (float)x[0];
				intersections[2].y = (float)x[1];
				intersections[2].z = (float)x[2];
			}

			// Intersect Top, Left and Front
			//Top
			a[0][0] = m_Frustum[TOP][A];
			a[0][1] = m_Frustum[TOP][B];
			a[0][2] = m_Frustum[TOP][C];
			b[0]	= - m_Frustum[TOP][D];
			//Left
			a[1][0] = m_Frustum[LEFT][A];
			a[1][1] = m_Frustum[LEFT][B];
			a[1][2] = m_Frustum[LEFT][C];
			b[1]	= - m_Frustum[LEFT][D];
			//Back
			a[2][0] = m_Frustum[BACK][A];
			a[2][1] = m_Frustum[BACK][B];
			a[2][2] = m_Frustum[BACK][C];
			b[2]	= - m_Frustum[BACK][D];

			if ( Maths.GaussianElim( a, b, x, N_eqn ))
			{
				intersections[3].x = (float)x[0];
				intersections[3].y = (float)x[1];
				intersections[3].z = (float)x[2];
			}

			// Intersect Bottom, Right and Front
			//Bottom
			a[0][0] = m_Frustum[BOTTOM][A];
			a[0][1] = m_Frustum[BOTTOM][B];
			a[0][2] = m_Frustum[BOTTOM][C];
			b[0]	= - m_Frustum[BOTTOM][D];
			//Right
			a[1][0] = m_Frustum[RIGHT][A];
			a[1][1] = m_Frustum[RIGHT][B];
			a[1][2] = m_Frustum[RIGHT][C];
			b[1]	= - m_Frustum[RIGHT][D];
			//Front
			a[2][0] = m_Frustum[FRONT][A];
			a[2][1] = m_Frustum[FRONT][B];
			a[2][2] = m_Frustum[FRONT][C];
			b[2]	= - m_Frustum[FRONT][D];

			if ( Maths.GaussianElim( a, b, x, N_eqn ))
			{
				intersections[4].x = (float)x[0];
				intersections[4].y = (float)x[1];
				intersections[4].z = (float)x[2];
			}

			// Intersect Bottom, Left and Front
			//Bottom
			a[0][0] = m_Frustum[BOTTOM][A];
			a[0][1] = m_Frustum[BOTTOM][B];
			a[0][2] = m_Frustum[BOTTOM][C];
			b[0]	= - m_Frustum[BOTTOM][D];
			//Left
			a[1][0] = m_Frustum[LEFT][A];
			a[1][1] = m_Frustum[LEFT][B];
			a[1][2] = m_Frustum[LEFT][C];
			b[1]	= - m_Frustum[LEFT][D];
			//Front
			a[2][0] = m_Frustum[FRONT][A];
			a[2][1] = m_Frustum[FRONT][B];
			a[2][2] = m_Frustum[FRONT][C];
			b[2]	= - m_Frustum[FRONT][D];

			if ( Maths.GaussianElim( a, b, x, N_eqn ))
			{
				intersections[5].x = (float)x[0];
				intersections[5].y = (float)x[1];
				intersections[5].z = (float)x[2];
			}

			// Intersect Top, Right and Front
			//Top
			a[0][0] = m_Frustum[TOP][A];
			a[0][1] = m_Frustum[TOP][B];
			a[0][2] = m_Frustum[TOP][C];
			b[0]	= - m_Frustum[TOP][D];
			//Right
			a[1][0] = m_Frustum[RIGHT][A];
			a[1][1] = m_Frustum[RIGHT][B];
			a[1][2] = m_Frustum[RIGHT][C];
			b[1]	= - m_Frustum[RIGHT][D];
			//Front
			a[2][0] = m_Frustum[FRONT][A];
			a[2][1] = m_Frustum[FRONT][B];
			a[2][2] = m_Frustum[FRONT][C];
			b[2]	= - m_Frustum[FRONT][D];

			if ( Maths.GaussianElim( a, b, x, N_eqn ))
			{
				intersections[6].x = (float)x[0];
				intersections[6].y = (float)x[1];
				intersections[6].z = (float)x[2];
			}

			// Intersect Top, Left and Front
			//Top
			a[0][0] = m_Frustum[TOP][A];
			a[0][1] = m_Frustum[TOP][B];
			a[0][2] = m_Frustum[TOP][C];
			b[0]	= - m_Frustum[TOP][D];
			//Left
			a[1][0] = m_Frustum[LEFT][A];
			a[1][1] = m_Frustum[LEFT][B];
			a[1][2] = m_Frustum[LEFT][C];
			b[1]	= - m_Frustum[LEFT][D];
			//Front
			a[2][0] = m_Frustum[FRONT][A];
			a[2][1] = m_Frustum[FRONT][B];
			a[2][2] = m_Frustum[FRONT][C];
			b[2]	= - m_Frustum[FRONT][D];

			if ( Maths.GaussianElim( a, b, x, N_eqn ))
			{
				intersections[7].x = (float)x[0];
				intersections[7].y = (float)x[1];
				intersections[7].z = (float)x[2];
			}

		
		} catch (Exception e) {
			throw new FrustumIntersectionException();
		}
		return intersections;
	}

	void getBounds(Vector3f min, Vector3f max,TorusWorld tw){
		//Calculate bounding coordinates of the view frustum
		try {
			Vector3f intersections[] = new Vector3f[8];
			intersections = getIntersections();

			max.x = max.y = max.z = -Float.MAX_VALUE;
			min.x = min.y = min.z = Float.MAX_VALUE;
			//min.setXYZ(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);
			//max.setXYZ(Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE);

			for (int i = 0 ; i < 8 ; i++ )
			{
				if (min.x > intersections[i].x) min.x = intersections[i].x;
				if (max.x < intersections[i].x) max.x = intersections[i].x;
				if (min.y > intersections[i].y) min.y = intersections[i].y;
				if (max.y < intersections[i].y) max.y = intersections[i].y;
				if (min.z > intersections[i].z) min.z = intersections[i].z;
				if (max.z < intersections[i].z) max.z = intersections[i].z;
			}
		} catch (Exception e){
			//System.out.println("Test:" + e);
		}
	}
}

class FrustumIntersectionException extends Exception {
	private static final long serialVersionUID = 0;
	
	FrustumIntersectionException(){}
	FrustumIntersectionException(String s) { super(s); }
}
