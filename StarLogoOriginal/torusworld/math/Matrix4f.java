package torusworld.math;

import java.text.DecimalFormat;

/** @modelguid {AD7D9A4E-40BB-476C-BCC3-DC77D41C0932} */
public final class Matrix4f
{
	/** Identity matrix */
	public static final Matrix4f IDENTITY = new Matrix4f();

    /** @modelguid {D52429A5-CDE0-42D8-B94D-D70D3312FE33} */
	private float matrix[][]; // Row major (first index is row, second index is column)

    /** Default constructor (identity matrix) */
	public Matrix4f() {
		matrix = new float[4][4];
		loadIdentity();
	}

    /** Copy constructor */
	public Matrix4f(Matrix4f mat) {
		copy(mat);
	}

	/** Construct from a 4x4 array of floats */
	public Matrix4f(float[][] mat)
	{
		matrix = mat;
	}

	/**
	 * Construct a matrix out of an OpenGL matrix, represented as a one-dimensional
	 * float array (column major)
	 */
	public Matrix4f(float[] mat)
	{
		matrix = new float[4][4];

		matrix[0][0] = mat[0]; matrix[0][1] = mat[4]; matrix[0][2] = mat[8];  matrix[0][3] = mat[12];
		matrix[1][0] = mat[1]; matrix[1][1] = mat[5]; matrix[1][2] = mat[9];  matrix[1][3] = mat[13];
		matrix[2][0] = mat[2]; matrix[2][1] = mat[6]; matrix[2][2] = mat[10]; matrix[2][3] = mat[14];
		matrix[3][0] = mat[3]; matrix[3][1] = mat[7]; matrix[3][2] = mat[11]; matrix[3][3] = mat[15];
	}

	/**
	 * Construct a matrix from 16 float values
	 */
	public Matrix4f(float m00, float m01, float m02, float m03,
	                float m10, float m11, float m12, float m13,
	                float m20, float m21, float m22, float m23,
	                float m30, float m31, float m32, float m33)
	{
		matrix = new float[4][4];

		matrix[0][0] = m00; matrix[0][1] = m01; matrix[0][2] = m02; matrix[0][3] = m03;
		matrix[1][0] = m10; matrix[1][1] = m11; matrix[1][2] = m12; matrix[1][3] = m13;
		matrix[2][0] = m20; matrix[2][1] = m21; matrix[2][2] = m22; matrix[2][3] = m23;
		matrix[3][0] = m30; matrix[3][1] = m31; matrix[3][2] = m32; matrix[3][3] = m33;
	}

    /** Copies a matrix */
	public void copy(Matrix4f matrix) {
		if (null == matrix) {
			loadIdentity();
		} else {
			this.matrix = new float [4][4];
			System.arraycopy(matrix.matrix[0], 0, this.matrix[0], 0, 4);
			System.arraycopy(matrix.matrix[1], 0, this.matrix[1], 0, 4);
			System.arraycopy(matrix.matrix[2], 0, this.matrix[2], 0, 4);
			System.arraycopy(matrix.matrix[3], 0, this.matrix[3], 0, 4);
		}
	}

	/**
	 * Converts the matrix into an OpenGL matrix, represented as a one-dimensional float
	 * array (column major)
	 */
	public float[] toGLMatrix()
	{
		return new float[]{matrix[0][0], matrix[1][0], matrix[2][0], matrix[3][0],
		                   matrix[0][1], matrix[1][1], matrix[2][1], matrix[3][1],
		                   matrix[0][2], matrix[1][2], matrix[2][2], matrix[3][2],
		                   matrix[0][3], matrix[1][3], matrix[2][3], matrix[3][3]};
	}

	public float get(int i, int j) {
		return matrix[i][j];
	}

	public float[] getColumn(int i)
	{
		return new float[] { matrix[0][i], matrix[1][i], matrix[2][i] };
	}

	public void setColumn(int i, float[] column)
	{
		matrix[0][i] = column[0];
		matrix[1][i] = column[1];
		matrix[2][i] = column[2];
		matrix[3][i] = column[3];
	}

	public void set(int i, int j, float value)
	{
		matrix[i][j] = value;
	}

	public void set(float[][] matrix)
	{
		this.matrix = matrix;
	}

	public void set(float[] matrix) // OpenGL matrix (column major)
	{
		this.matrix[0][0] = matrix[0]; this.matrix[0][1] = matrix[4]; this.matrix[0][2] = matrix[8];  this.matrix[0][3] = matrix[12];
		this.matrix[1][0] = matrix[1]; this.matrix[1][1] = matrix[5]; this.matrix[1][2] = matrix[9];  this.matrix[1][3] = matrix[13];
		this.matrix[2][0] = matrix[2]; this.matrix[2][1] = matrix[6]; this.matrix[2][2] = matrix[10]; this.matrix[2][3] = matrix[14];
		this.matrix[3][0] = matrix[3]; this.matrix[3][1] = matrix[7]; this.matrix[3][2] = matrix[11]; this.matrix[3][3] = matrix[15];
	}

	/** Sets this matrix to the identity matrix */
	public void loadIdentity()
	{
		matrix[0][0] = 1.0f; matrix[0][1] = 0.0f; matrix[0][2] = 0.0f; matrix[0][3] = 0.0f;
		matrix[1][0] = 0.0f; matrix[1][1] = 1.0f; matrix[1][2] = 0.0f; matrix[1][3] = 0.0f;
		matrix[2][0] = 0.0f; matrix[2][1] = 0.0f; matrix[2][2] = 1.0f; matrix[2][3] = 0.0f;
		matrix[3][0] = 0.0f; matrix[3][1] = 0.0f; matrix[3][2] = 0.0f; matrix[3][3] = 1.0f;
	}

	/** Returns the transpose of this matrix (does not modify self) */
	public Matrix4f transpose()
	{
		return new Matrix4f(matrix[0][0], matrix[1][0], matrix[2][0], matrix[3][0],
		                    matrix[0][1], matrix[1][1], matrix[2][1], matrix[3][1],
		                    matrix[0][2], matrix[1][2], matrix[2][2], matrix[3][2],
		                    matrix[0][3], matrix[1][3], matrix[2][3], matrix[3][3]);
	}
	
	/** Transposes this matrix */
	public void transposeSelf()
	{
		float m01 = matrix[0][1];
		float m02 = matrix[0][2];
		float m03 = matrix[0][3];
		float m12 = matrix[1][2];
		float m13 = matrix[1][3];
		float m23 = matrix[2][3];
		
		matrix[0][1] = matrix[1][0];
		matrix[0][2] = matrix[2][0];
		matrix[0][3] = matrix[3][0];
		matrix[1][2] = matrix[2][1];
		matrix[1][3] = matrix[3][1];
		matrix[2][3] = matrix[2][3];
		matrix[1][0] = m01;
		matrix[2][0] = m02;
		matrix[3][0] = m03;
		matrix[2][1] = m12;
		matrix[3][1] = m13;
		matrix[3][2] = m23;
	}

	/** Scales this matrix by a constant */
	public void mult(float scalar) {
		matrix[0][0] *= scalar; matrix[0][1] *= scalar; matrix[0][2] *= scalar; matrix[0][3] *= scalar;
		matrix[1][0] *= scalar; matrix[1][1] *= scalar; matrix[1][2] *= scalar; matrix[1][3] *= scalar;
		matrix[2][0] *= scalar; matrix[2][1] *= scalar; matrix[2][2] *= scalar; matrix[2][3] *= scalar;
		matrix[3][0] *= scalar; matrix[3][1] *= scalar; matrix[3][2] *= scalar; matrix[3][3] *= scalar;
	}
	
	/** Sets this matrix to a 3-dimensional scaling matrix */
	public void scale(float x, float y, float z) {
		float [][] mat = new float[4][4];
		mat[0][0] = x;
		mat[1][1] = y;
		mat[2][2] = z;
		mat[3][3] = 1.0f;
		matrix = mult(new Matrix4f(mat)).matrix;
	}

	/** Returns the matrix product of this with another matrix (does not modify self) */
	public Matrix4f mult(Matrix4f in2)
	{
		return new Matrix4f(matrix[0][0] * in2.matrix[0][0] + matrix[0][1] * in2.matrix[1][0] + matrix[0][2] * in2.matrix[2][0] + matrix[0][3] * in2.matrix[3][0],
		                    matrix[0][0] * in2.matrix[0][1] + matrix[0][1] * in2.matrix[1][1] + matrix[0][2] * in2.matrix[2][1] + matrix[0][3] * in2.matrix[3][1],
		                    matrix[0][0] * in2.matrix[0][2] + matrix[0][1] * in2.matrix[1][2] + matrix[0][2] * in2.matrix[2][2] + matrix[0][3] * in2.matrix[3][2],
		                    matrix[0][0] * in2.matrix[0][3] + matrix[0][1] * in2.matrix[1][3] + matrix[0][2] * in2.matrix[2][3] + matrix[0][3] * in2.matrix[3][3],

		                    matrix[1][0] * in2.matrix[0][0] + matrix[1][1] * in2.matrix[1][0] + matrix[1][2] * in2.matrix[2][0] + matrix[1][3] * in2.matrix[3][0],
		                    matrix[1][0] * in2.matrix[0][1] + matrix[1][1] * in2.matrix[1][1] + matrix[1][2] * in2.matrix[2][1] + matrix[1][3] * in2.matrix[3][1],
		                    matrix[1][0] * in2.matrix[0][2] + matrix[1][1] * in2.matrix[1][2] + matrix[1][2] * in2.matrix[2][2] + matrix[1][3] * in2.matrix[3][2],
		                    matrix[1][0] * in2.matrix[0][3] + matrix[1][1] * in2.matrix[1][3] + matrix[1][2] * in2.matrix[2][3] + matrix[1][3] * in2.matrix[3][3],

		                    matrix[2][0] * in2.matrix[0][0] + matrix[2][1] * in2.matrix[1][0] + matrix[2][2] * in2.matrix[2][0] + matrix[2][3] * in2.matrix[3][0],
		                    matrix[2][0] * in2.matrix[0][1] + matrix[2][1] * in2.matrix[1][1] + matrix[2][2] * in2.matrix[2][1] + matrix[2][3] * in2.matrix[3][1],
		                    matrix[2][0] * in2.matrix[0][2] + matrix[2][1] * in2.matrix[1][2] + matrix[2][2] * in2.matrix[2][2] + matrix[2][3] * in2.matrix[3][2],
		                    matrix[2][0] * in2.matrix[0][3] + matrix[2][1] * in2.matrix[1][3] + matrix[2][2] * in2.matrix[2][3] + matrix[2][3] * in2.matrix[3][3],

		                    matrix[3][0] * in2.matrix[0][0] + matrix[3][1] * in2.matrix[1][0] + matrix[3][2] * in2.matrix[2][0] + matrix[3][3] * in2.matrix[3][0],
		                    matrix[3][0] * in2.matrix[0][1] + matrix[3][1] * in2.matrix[1][1] + matrix[3][2] * in2.matrix[2][1] + matrix[3][3] * in2.matrix[3][1],
		                    matrix[3][0] * in2.matrix[0][2] + matrix[3][1] * in2.matrix[1][2] + matrix[3][2] * in2.matrix[2][2] + matrix[3][3] * in2.matrix[3][2],
		                    matrix[3][0] * in2.matrix[0][3] + matrix[3][1] * in2.matrix[1][3] + matrix[3][2] * in2.matrix[2][3] + matrix[3][3] * in2.matrix[3][3]);
	}

	/** Returns the matrix product of this with a vector (does not modify self) */
	public Vector3f mult(Vector3f vec)
	{
		return new Vector3f(matrix[0][0] * vec.x + matrix[0][1] * vec.y + matrix[0][2] * vec.z,
		                    matrix[1][0] * vec.x + matrix[1][1] * vec.y + matrix[1][2] * vec.z,
		                    matrix[2][0] * vec.x + matrix[2][1] * vec.y + matrix[2][2] * vec.z);
	}
	
	/** Sets the input to the product of this matrix with the input */
	public void rotate(Vector3f vec) {
		float x = matrix[0][0] * vec.x + matrix[0][1] * vec.y + matrix[0][2] * vec.z;
		float y = matrix[1][0] * vec.x + matrix[1][1] * vec.y + matrix[1][2] * vec.z;
		float z = matrix[2][0] * vec.x + matrix[2][1] * vec.y + matrix[2][2] * vec.z;
		vec.x = x;
		vec.y = y;
		vec.z = z;
	}
	
	/** Sets the input to the product of this matrix with the input */
	public void transform(Vector3f vec) {
		float x = matrix[0][0] * vec.x + matrix[0][1] * vec.y + matrix[0][2] * vec.z + matrix[0][3];
		float y = matrix[1][0] * vec.x + matrix[1][1] * vec.y + matrix[1][2] * vec.z + matrix[1][3];
		float z = matrix[2][0] * vec.x + matrix[2][1] * vec.y + matrix[2][2] * vec.z + matrix[2][3];
		vec.x = x;
		vec.y = y;
		vec.z = z;
	}

	public void add(Matrix4f matrix) {
		this.matrix[0][0] += matrix.matrix[0][0]; this.matrix[0][1] += matrix.matrix[0][1]; this.matrix[0][2] += matrix.matrix[0][2]; this.matrix[0][3] += matrix.matrix[0][3];
		this.matrix[1][0] += matrix.matrix[1][0]; this.matrix[1][1] += matrix.matrix[1][1]; this.matrix[1][2] += matrix.matrix[1][2]; this.matrix[1][3] += matrix.matrix[1][3];
		this.matrix[2][0] += matrix.matrix[2][0]; this.matrix[2][1] += matrix.matrix[2][1]; this.matrix[2][2] += matrix.matrix[2][2]; this.matrix[2][3] += matrix.matrix[2][3];
		this.matrix[3][0] += matrix.matrix[3][0]; this.matrix[3][1] += matrix.matrix[3][1]; this.matrix[3][2] += matrix.matrix[3][2]; this.matrix[3][3] += matrix.matrix[3][3];
	}

	/** Returns the determinant of the upper-left 3x3 submatrix */
	public float determinant()
	{
		return (matrix[0][0] * (matrix[1][1] * matrix[2][2] - matrix[1][2] * matrix[2][1]) +
		        matrix[0][1] * (matrix[1][2] * matrix[2][0] - matrix[1][0] * matrix[2][2]) +
		        matrix[0][2] * (matrix[1][0] * matrix[2][1] - matrix[1][1] * matrix[2][0]));
	}

	/**
	 * Returns the inverse of this matrix.  If it is singular, the identity matrix is
	 * returned.
	 */
	public Matrix4f inverse()
	{
		float det = determinant();

		if(Math.abs(det) < 0.000001f)
			return IDENTITY;

		float detInv = 1.0f / det;

		return new Matrix4f((matrix[1][1] * matrix[2][2] - matrix[1][2] * matrix[2][1]) * detInv,
		                    (matrix[1][2] * matrix[2][0] - matrix[1][0] * matrix[2][2]) * detInv,
		                    (matrix[1][0] * matrix[2][1] - matrix[1][1] * matrix[2][0]) * detInv,
		                     0.0f,
		                    (matrix[0][2] * matrix[2][1] - matrix[0][1] * matrix[2][2]) * detInv,
		                    (matrix[0][0] * matrix[2][2] - matrix[0][2] * matrix[2][0]) * detInv,
		                    (matrix[0][1] * matrix[2][0] - matrix[0][0] * matrix[2][1]) * detInv,
		                     0.0f,
		                    (matrix[0][1] * matrix[1][2] - matrix[0][2] * matrix[1][1]) * detInv,
		                    (matrix[0][2] * matrix[1][0] - matrix[0][0] * matrix[1][2]) * detInv,
		                    (matrix[0][0] * matrix[1][1] - matrix[0][1] * matrix[1][0]) * detInv,
		                     0.0f,
		                     0.0f,
		                     0.0f,
		                     0.0f,
		                     1.0f);
	}

	/** Sets this matrix to a translation matrix */
	public void setTranslation(float[] translation) {
		matrix[3][0] = translation[0];
		matrix[3][1] = translation[1];
		matrix[3][2] = translation[2];
	}

	public void setInverseTranslation(float[] translation) {
		matrix[3][0] = -translation[0];
		matrix[3][1] = -translation[1];
		matrix[3][2] = -translation[2];
	}

    /** Sets this matrix to a rotation matrix
     * @param angles Vector of angles.  angles.x is the amount to rotate about the
     *               x-axis, angles.y is the amount to rotate about the y-axis, and
     *               angles.z is the amount to rotate about the z-axis.
     */
	public void angleRotation(Vector3f angles) {
		float angle;
		float sr, sp, sy, cr, cp, cy;

		angle = (float)(angles.z * (Math.PI  / 180.0));
		sy = (float)Math.sin(angle);
		cy = (float)Math.cos(angle);
		angle = (float)(angles.y * (Math.PI / 180.0));
		sp = (float)Math.sin(angle);
		cp = (float)Math.cos(angle);
		angle = (float)(angles.x * (Math.PI / 180.0));
		sr = (float)Math.sin(angle);
		cr = (float)Math.cos(angle);

		// matrix = (Z * Y) * X
		matrix[0][0] = cp * cy;
		matrix[0][1] = sr * sp * cy + cr * -sy;
		matrix[0][2] = (cr * sp * cy + -sr * -sy);
		matrix[0][3] = 0.0f;

		matrix[1][0] = cp * sy;
		matrix[1][1] = sr * sp * sy + cr * cy;
		matrix[1][2] = (cr * sp * sy + -sr * cy);
		matrix[1][3] = 0.0f;

		matrix[2][0] = -sp;
		matrix[2][1] = sr * cp;
		matrix[2][2] = cr * cp;
		matrix[2][3] = 0.0f;

		matrix[3][0] = 0.0f;
		matrix[3][1] = 0.0f;
		matrix[3][2] = 0.0f;
		matrix[3][3] = 1.0f;
	}

    /** Sets this matrix to be a rotation matrix specified by a quaternion */
	public void setRotationQuaternion(Quaternion quat) {
		matrix[0][0] = (float) (1.0 - 2.0 * (quat.y * quat.y + quat.z * quat.z));
		matrix[0][1] = (float) (      2.0 * (quat.x * quat.y + quat.w * quat.z));
		matrix[0][2] = (float) (      2.0 * (quat.x * quat.z - quat.w * quat.y));
		matrix[0][3] = 0.0f;

		matrix[1][0] = (float) (      2.0 * (quat.x * quat.y - quat.w * quat.z));
		matrix[1][1] = (float) (1.0 - 2.0 * (quat.x * quat.x + quat.z * quat.z));
		matrix[1][2] = (float) (      2.0 * (quat.y * quat.z + quat.w * quat.x));
		matrix[1][3] = 0.0f;

		matrix[2][0] = (float) (      2.0 * (quat.x * quat.z + quat.w * quat.y));
		matrix[2][1] = (float) (      2.0 * (quat.y * quat.z - quat.w * quat.x));
		matrix[2][2] = (float) (1.0 - 2.0 * (quat.x * quat.x + quat.y * quat.y));
		matrix[2][3] = 0.0f;

		matrix[3][0] = 0.0f;
		matrix[3][1] = 0.0f;
		matrix[3][2] = 0.0f;
		matrix[3][3] = 1.0f;
	}

	/** Sets this matrix to the inverse of a rotation matrix
     * @param angles Array of angles.  angles[0] is the amount to rotate about the
     *               x-axis, angles[1] is the amount to rotate about the y-axis, and
     *               angles[2] is the amount to rotate about the z-axis.  Angles are
     *               all in radians.
     */
	public void setInverseRotationRadians(float[] angles) {
		double cr = Math.cos(angles[0]);
		double sr = Math.sin(angles[0]);
		double cp = Math.cos(angles[1]);
		double sp = Math.sin(angles[1]);
		double cy = Math.cos(angles[2]);
		double sy = Math.sin(angles[2]);

		double srsp = sr * sp;
		double crsp = cr * sp;

		matrix[0][0] = (float)(cp * cy);
		matrix[0][1] = (float)(srsp * cy - cr * sy);
		matrix[0][2] = (float)(crsp * cy + sr * sy);
		matrix[0][3] = 0.0f;

		matrix[1][0] = (float)(cp * sy);
		matrix[1][1] = (float)(srsp * sy + cr * cy);
		matrix[1][2] = (float)(crsp * sy - sr * cy);
		matrix[1][3] = 0.0f;

		matrix[2][0] = (float)(-sp);
		matrix[2][1] = (float)(sr * cp);
		matrix[2][2] = (float)(cr * cp);
		matrix[2][3] = 0.0f;

		matrix[3][0] = 0.0f;
		matrix[3][1] = 0.0f;
		matrix[3][2] = 0.0f;
		matrix[3][3] = 1.0f;
	}

	/** Sets this matrix to the inverse of a rotation matrix
     * @param angles Array of angles.  angles[0] is the amount to rotate about the
     *               x-axis, angles[1] is the amount to rotate about the y-axis, and
     *               angles[2] is the amount to rotate about the z-axis.  Angles are
     *               all in degrees.
     */
	public void setInverseRotationDegrees(float[] angles) {
		float vec[] = new float[3];
		vec[0] = (float)(angles[0] * 180.0 / Math.PI);
		vec[1] = (float)(angles[1] * 180.0 / Math.PI);
		vec[2] = (float)(angles[2] * 180.0 / Math.PI);
		setInverseRotationRadians(vec);
	}

	public void inverseTranslateVect(float[] vector3f) {
		vector3f[0] = vector3f[0] - matrix[3][0];
		vector3f[1] = vector3f[1] - matrix[3][1];
		vector3f[2] = vector3f[2] - matrix[3][2];
	}

	public void inverseRotateVect(float[] vec) {
		float x = vec[0] * matrix[0][0] + vec[1] * matrix[0][1] + vec[2] * matrix[0][2];
		float y = vec[0] * matrix[1][0] + vec[1] * matrix[1][1] + vec[2] * matrix[1][2];
		float z = vec[0] * matrix[2][0] + vec[1] * matrix[2][1] + vec[2] * matrix[2][2];
		
		vec[0] = x;
		vec[1] = y;
		vec[2] = z;
	}

	public Vector3f inverseRotate(Vector3f v) {
		return new Vector3f(v.x * matrix[0][0] + v.y * matrix[1][0] + v.z * matrix[2][0],
		                    v.x * matrix[0][1] + v.y * matrix[1][1] + v.z * matrix[2][1],
		                    v.x * matrix[0][2] + v.y * matrix[1][2] + v.z * matrix[2][2]);
	}

	private void rotate(float angle, boolean isInRadians, int i1, int i2) {
		angle = -angle;

		if(!isInRadians)
			angle = (float)Math.toRadians(angle);

		float [][] mat = new float[4][4];

		mat[0][0] = 1.0f;
		mat[1][1] = 1.0f;
		mat[2][2] = 1.0f;
		mat[3][3] = 1.0f;
		
		float sin = (float)Math.sin(angle);
		float cos = (float)Math.cos(angle);

		mat[i1][i1] = cos;
		mat[i1][i2] = sin;
		mat[i2][i1] = -sin;
		mat[i2][i2] = cos;

		matrix = new Matrix4f(mat).mult(this).matrix;
	}
	
	public void rotateZ(float angle, boolean isInRadians) {
		rotate(angle, isInRadians, 0, 1);
	}
	
	public void rotateY(float angle, boolean isInRadians) {
		rotate(angle, isInRadians, 0, 2);
	}
	
	public void rotateX(float angle, boolean isInRadians) {
		rotate(angle, isInRadians, 1, 2);
	}
	
	public void translate(float x, float y, float z) {
		matrix[0][3] += x;
		matrix[1][3] += y;
		matrix[2][3] += z;
	}
	
	private static DecimalFormat decimalFormat = new DecimalFormat(" 0.00;-0.00");

	public String toString()
	{
		String result = "";

		for(int i = 0; i < 4; i++)
		{
			result += "[";

			for(int j = 0; j < 4; j++)
				result += decimalFormat.format(matrix[i][j]) + " ";

			result += "]\n";
		}

		return result;
	}
}
