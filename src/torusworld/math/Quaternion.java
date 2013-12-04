package torusworld.math;

public final class Quaternion {
    public double x;
    public double y;
    public double z;
    public double w;

    public Quaternion() {
        x = 0;
        y = 0;
        z = 0;
        w = 0;
    }

    public Quaternion(double x, double y, double z, double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Quaternion(float[] angles) {
        fromAngles(angles);
    }

    public Quaternion(Quaternion q) {
        set(q);
    }
    
    public void set(Quaternion q) {
        this.x = q.x;
        this.y = q.y;
        this.z = q.z;
        this.w = q.w;
    }
    

     public void fromAngles(float[] angles)
     {
        double angle;
        double sr, sp, sy, cr, cp, cy;
        angle = angles[2] * 0.5f;
        sy = Math.sin(angle);
        cy = Math.cos(angle);
        angle = angles[1] * 0.5f;
        sp = Math.sin(angle);
        cp = Math.cos(angle);
        angle = angles[0] * 0.5f;
        sr = Math.sin(angle);
        cr = Math.cos(angle);

        double crcp = cr * cp;
        double srsp = sr * sp;

        x = (sr * cp * cy - cr * sp * sy);
        y = (cr * sp * cy + sr * cp * sy);
        z = (crcp * sy - srsp * cy);
        w = (crcp * cy + srsp * sy);
    }

    /** @modelguid {68356E96-18D8-4349-BF7C-E0B6C5AD0308} */
	public void fromRotationMatrix(float[] matrix) 
	{
			Matrix3f m = new Matrix3f();
			m.set(matrix);
			fromRotationMatrix(m);
	}

    public void fromRotationMatrix(Matrix3f matrix) {
        float[] m4x4 = new float[16];

        //create a 4x4 matrix from the 3x3 matrix
        m4x4[0] = matrix.get(0, 0);
        m4x4[1] = matrix.get(0, 1);
        m4x4[2] = matrix.get(0, 2);
        m4x4[3] = 0f;
        m4x4[4] = matrix.get(1, 0);
        m4x4[5] = matrix.get(1, 1);
        m4x4[6] = matrix.get(1, 2);
        m4x4[7] = 0f;
        m4x4[8] = matrix.get(2, 0);
        m4x4[9] = matrix.get(2, 1);
        m4x4[10] = matrix.get(2, 2);
        m4x4[11] = 0f;
        m4x4[12] = 0f;
        m4x4[13] = 0f;
        m4x4[14] = 0f;
        m4x4[15] = 1;

        //calculate the trace of the matrix.
        double diagonal = m4x4[0] + m4x4[5] + m4x4[10] + 1;
        double scale = 0.0f;

        // If the diagonal is greater than zero
        if (diagonal > 0.00000001f) {
            // Calculate the scale of the diagonal
            scale = Math.sqrt(diagonal) * 2f;

            // Calculate the x, y, z and w of the quaternion through the respective equation
            x = (m4x4[9] - m4x4[6]) / scale;
            y = (m4x4[2] - m4x4[8]) / scale;
            z = (m4x4[4] - m4x4[1]) / scale;
            w = 0.25 * scale;
        } else {
            // If the first element of the diagonal is the greatest value
            if (m4x4[0] > m4x4[5]
                && m4x4[0] > m4x4[10]) {
                // Find the scale according to the first element, and double that value
                scale = Math.sqrt(1.0f + m4x4[0] - m4x4[5] - m4x4[10]) * 2.0;

                // Calculate the x, y, z and w of the quaternion through the respective equation
                x = 0.25 * scale;
                y = (m4x4[4] + m4x4[1]) / scale;
                z = (m4x4[2] + m4x4[8]) / scale;
                w = (m4x4[9] - m4x4[6]) / scale;
            } else if (m4x4[5] > m4x4[10]) {
                // Find the scale according to the second element, and double that value
                scale = Math.sqrt(1.0f + m4x4[5] - m4x4[0] - m4x4[10]) * 2.0;

                // Calculate the x, y, z and w of the quaternion through the respective equation
                x = (m4x4[4] + m4x4[1]) / scale;
                y = 0.25 * scale;
                z = (m4x4[9] + m4x4[6]) / scale;
                w = (m4x4[2] - m4x4[8]) / scale;
            } else {
                // Find the scale according to the third element, and double that value
                scale = Math.sqrt(1.0f + m4x4[10] - m4x4[0] - m4x4[5]) * 2.0;

                // Calculate the x, y, z and w of the quaternion through the respective equation
                x = (m4x4[2] + m4x4[8]) / scale;
                y = (m4x4[9] + m4x4[6]) / scale;
                z = 0.25 * scale;
                w = (m4x4[4] - m4x4[1]) / scale;
            }
        }

    }
    
    public void fromAngleAxis(float angle, Vector3f axis) 
	{
        Vector3f normAxis = axis.clone();
        normAxis.normalize();
        double halfAngle = 0.5 * angle;
        double sin = Math.sin(halfAngle);
        w = Math.cos(halfAngle);
        x = sin * normAxis.x;
        y = sin * normAxis.y;
        z = sin * normAxis.z;
    }

    /** @modelguid {F1FB5F8D-6DD6-4FD8-9036-29F788E4514C} */
    public float toAngleAxis(Vector3f axis) {
        double sqrLength = x * x + y * y + z * z;
        double angle;
        if (sqrLength > 0.0) {
            angle = 2.0 * Math.acos(w);
            double invLength = 1.0 / Math.sqrt(sqrLength);
            axis.x = (float) (x * invLength);
            axis.y = (float) (y * invLength);
            axis.z = (float) (z * invLength);
        } else {
            angle = 0.0f;
            axis.x = 1.0f;
            axis.y = 0.0f;
            axis.z = 0.0f;
        }

        return (float) angle;
    }

    public static Quaternion slerp(Quaternion q1, Quaternion q2, double t) {
        // Create a local quaternion to store the interpolated quaternion
        double factor = 1;
        Quaternion res = new Quaternion();
        

        if (q1.x == q2.x && q1.y == q2.y && q1.z == q2.z && q1.w == q2.w) {
            res.set(q1);
            return res;
        }

        double result = (q1.x * q2.x) + (q1.y * q2.y) + 
                        (q1.z * q2.z) + (q1.w * q2.w);
        
        if (result < 0.0)
        {
            factor = -1; // use factor to compute using negated q2
            result = -result;
        }

        // Set the first and second scale for the interpolation
        double scale0 = 1-t;
        double scale1 = t;
        
        if (1 - result > 1e-3)
        {
            // Get the angle between the 2 quaternions, and then store the sin() of that angle
            double theta = Math.acos(result);
            double sinTheta = Math.sin(theta);

            // Calculate the scale for q1 and q2, according to the angle and it's sine value
            scale0 = Math.sin((1 - t) * theta) / sinTheta;
            scale1 = Math.sin((t * theta)) / sinTheta;
        }

        // Calculate the x, y, z and w values for the quaternion by using a special
        // form of linear interpolation for quaternions.
        scale0 *= factor;
        res.x = (scale0 * q1.x) + (scale1 * q2.x);
        res.y = (scale0 * q1.y) + (scale1 * q2.y);
        res.z = (scale0 * q1.z) + (scale1 * q2.z);
        res.w = (scale0 * q1.w) + (scale1 * q2.w);
        return res;
    }

    public static Quaternion lerp(Quaternion q1, Quaternion q2, double t) {
        // Create a local quaternion to store the interpolated quaternion
        double factor = 1;
        Quaternion res = new Quaternion();
        

        if (q1.x == q2.x && q1.y == q2.y && q1.z == q2.z && q1.w == q2.w) {
            res.set(q1);
            return res;
        }

        double result = (q1.x * q2.x) + (q1.y * q2.y) + 
                        (q1.z * q2.z) + (q1.w * q2.w);
        
        if (result < 0.0)
            factor = -1; // use factor to compute using negated q2

        // Set the first and second scale for the interpolation
        double scale0 = 1-t;
        double scale1 = t;
        
        scale0 *= factor;
        res.x = (scale0 * q1.x) + (scale1 * q2.x);
        res.y = (scale0 * q1.y) + (scale1 * q2.y);
        res.z = (scale0 * q1.z) + (scale1 * q2.z);
        res.w = (scale0 * q1.w) + (scale1 * q2.w);
        return res;
    }


    /** @modelguid {94350CED-C14D-4261-9570-08ACCC23A53A} */
    public Quaternion add(Quaternion q) {
        return new Quaternion(x + q.x, y + q.y, z + q.z, w + q.w);
    }

    public Quaternion subtract(Quaternion q) {
        return new Quaternion(x - q.x, y - q.y, z - q.z, w - q.w);
    }

    public Quaternion mult(Quaternion q) {
        return new Quaternion(
            w * q.x + x * q.w + y * q.z - z * q.y,
            w * q.y + y * q.w + z * q.x - x * q.z,
            w * q.z + z * q.w + x * q.y - y * q.x,
            w * q.w - x * q.x - y * q.y - z * q.z);
    }

    public Quaternion mult(float scalar) {
        return new Quaternion(scalar * x, scalar * y, scalar * z, scalar * w);
    }

    public float dot(Quaternion q) {
        return (float) (w * q.w + x * q.x + y * q.y + z * q.z);
    }

    public float norm() {
        return (float) (w * w + x * x + y * y + z * z);
    }

    public Quaternion inverse() {
        double norm = w * w + x * x + y * y + z * z;
        if (norm > 0.0) {
            double invNorm = 1.0 / norm;
            return new Quaternion(
                -x * invNorm,
                -y * invNorm,
                -z * invNorm,
                w * invNorm);
        } else {
            // return an invalid result to flag the error
            return null;
        }
    }

    public void negate() {
        x *= -1;
        y *= -1;
        z *= -1;
        w *= -1;
    }

	public void fromMatrix(float[] matrix, int rowColumnCount) 
	{
		if (matrix == null || (rowColumnCount != 3) && (rowColumnCount != 4)) 
		{
			throw new RuntimeException(
				"matrix cannot be null, while"
				+ "rowColumnCount must be 3 or 4.");
		}

		// Point the matrix pointer to the matrix passed in, assuming it's a 4x4 matrix
		float[] tempMatrix = matrix;

		// Create a 4x4 matrix to convert a 3x3 matrix to a 4x4 matrix (If rowColumnCount == 3)
		float[] m4x4 = new float[16];

		// If the matrix is a 3x3 matrix (which it is for Quake3), then convert it to a 4x4
		if (matrix.length == 9) 
		{
			// Set the 9 top left indices of the 4x4 matrix to the 9 indices in the 3x3 matrix.
			// It would be a good idea to actually draw this out so you can visualize it.
			m4x4[0] = matrix[0];
			m4x4[1] = matrix[1];
			m4x4[2] = matrix[2];
			m4x4[3] = 0f;
			m4x4[4] = matrix[3];
			m4x4[5] = matrix[4];
			m4x4[6] = matrix[5];
			m4x4[7] = 0f;
			m4x4[8] = matrix[6];
			m4x4[9] = matrix[7];
			m4x4[10] = matrix[8];
			m4x4[11] = 0f;

			// Since the bottom and far right indices are zero, set the bottom right corner to 1.
			// This is so that it follows the standard diagonal line of 1's in the identity matrix.
			m4x4[12] = 0f;
			m4x4[13] = 0f;
			m4x4[14] = 0f;
			m4x4[15] = 1;

			// Set the matrix pointer to the first index in the newly converted matrix
			tempMatrix = m4x4;
		}

		//calculate the trace of the matrix.
		double diagonal = tempMatrix[0] + tempMatrix[5] + tempMatrix[10] + 1;
		double scale = 0.0f;

		// If the diagonal is greater than zero
		if (diagonal > 0.00000001f) 
		{
			// Calculate the scale of the diagonal
			scale = Math.sqrt(diagonal) * 2.0;

			// Calculate the x, y, z and w of the quaternion through the respective equation
			x = (tempMatrix[9] - tempMatrix[6]) / scale;
			y = (tempMatrix[2] - tempMatrix[8]) / scale;
			z = (tempMatrix[4] - tempMatrix[1]) / scale;
			w = 0.25 * scale;
		} 
		else 
		{
			// If the first element of the diagonal is the greatest value
			if (tempMatrix[0] > tempMatrix[5]
				&& tempMatrix[0] > tempMatrix[10]) 
			{
				// Find the scale according to the first element, and double that value
				scale = Math.sqrt(1.0 + tempMatrix[0] - tempMatrix[5] - tempMatrix[10]) * 2.0;

				// Calculate the x, y, z and w of the quaternion through the respective equation
				x = 0.25 * scale;
				y = (tempMatrix[4] + tempMatrix[1]) / scale;
				z = (tempMatrix[2] + tempMatrix[8]) / scale;
				w = (tempMatrix[9] - tempMatrix[6]) / scale;
			}
				// Else if the second element of the diagonal is the greatest value
			else if (tempMatrix[5] > tempMatrix[10]) 
			{
				// Find the scale according to the second element, and double that value
				scale = Math.sqrt(1.0f + tempMatrix[5] - tempMatrix[0] - tempMatrix[10]) * 2.0;

				// Calculate the x, y, z and w of the quaternion through the respective equation
				x = (tempMatrix[4] + tempMatrix[1]) / scale;
				y = 0.25f * scale;
				z = (tempMatrix[9] + tempMatrix[6]) / scale;
				w = (tempMatrix[2] - tempMatrix[8]) / scale;
			}
				// Else the third element of the diagonal is the greatest value
			else 
			{
				// Find the scale according to the third element, and double that value
				scale = Math.sqrt(1.0f + tempMatrix[10] - tempMatrix[0] - tempMatrix[5]) * 2.0;

				// Calculate the x, y, z and w of the quaternion through the respective equation
				x = (tempMatrix[2] + tempMatrix[8]) / scale;
				y = (tempMatrix[9] + tempMatrix[6]) / scale;
				z = 0.25 * scale;
				w = (tempMatrix[4] - tempMatrix[1]) / scale;
			}
		}

	}

    public void toMatrix(float[] matrix) 
    {
        matrix[0] = (float) (1.0 - 2.0 * (y * y + z * z));
        matrix[1] = (float) (2.0 * (x * y - w * z));
        matrix[2] = (float) (2.0 * (x * z + w * y));
        matrix[3] = 0.0f;

        // Second row
        matrix[4] = (float) (2.0 * (x * y + w * z));
        matrix[5] = (float) (1.0 - 2.0 * (x * x + z * z));
        matrix[6] = (float) (2.0 * (y * z - w * x));
        matrix[7] = 0.0f;

        // Third row
        matrix[8] = (float) (2.0 * (x * z - w * y));
        matrix[9] = (float) (2.0 * (y * z + w * x));
        matrix[10] = (float) (1.0 - 2.0 * (x * x + y * y));
        matrix[11] = 0.0f;

        // Fourth row
        matrix[12] = 0;
        matrix[13] = 0;
        matrix[14] = 0;
        matrix[15] = 1.0f;
    }

    public float[] toMatrix() 
	{
		float[] matrix = new float[16];
        toMatrix(matrix);
        return matrix;
    }
    
    public void rotate(Vector3f vec) 
    {
        double fx, fy, fz, tx, ty, tz;
        
        // formulas taken from toMatrix
        
        fx = 1.0 - 2.0 * (y * y + z * z);
        fy = 2.0 * (x * y - w * z);
        fz = 2.0 * (x * z + w * y);
        
        tx = fx * vec.x + fy * vec.y + fz * vec.z;

        fx = 2.0 * (x * y + w * z);
        fy = 1.0 - 2.0 * (x * x + z * z);
        fz = 2.0 * (y * z - w * x);

        ty = fx * vec.x + fy * vec.y + fz * vec.z;

        fx = 2.0 * (x * z - w * y);
        fy = 2.0 * (y * z + w * x);
        fz = 1.0 - 2.0 * (x * x + y * y);
        
        tz = fx * vec.x + fy * vec.y + fz * vec.z;
        vec.set((float) tx, (float) ty, (float) tz);
    }

    
    
    public String toString() 
	{
        return "Quaternion[x=" +x+" y="+y+" z="+z+" w="+w+"]";
    }
    
    
}
