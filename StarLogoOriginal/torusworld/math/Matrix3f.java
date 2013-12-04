package torusworld.math;

/** @modelguid {912E511C-3758-45D2-B51E-8ED2D3389171} */
public final class Matrix3f {
    /** @modelguid {EB94CCA9-3E05-4CD6-9DDF-1AF41B95AD3A} */
    private float[][] matrix;

    /** @modelguid {5BE38F99-A775-4CF0-8431-08EC9932949E} */
    public Matrix3f() {
        matrix = new float[3][3];
        loadIdentity();
    }
    
    /** @modelguid {889C8480-A240-498D-A91D-23622E46DCEE} */
    public Matrix3f(Matrix3f mat) {
    	copy(mat);
    }
    
    //Reads row-by-row
    public Matrix3f(float [] contents) {
    	matrix = new float[3][3];
    	for(int i = 0; i < 3; i++) {
    		for(int j = 0; j < 3; j++) {
    			matrix[i][j] = contents[i * 3 + j];
    		}
    	}
    }
    
    /** @modelguid {164D7451-3216-47D7-BCAC-EBD8A37ACA82} */
    public void copy(Matrix3f matrix) {
        if(null == matrix) {
            loadIdentity();
        } else {
            for(int i = 0; i < 3; i++) {
                for(int j = 0; j < 3; j++) {
                 this.matrix[i][j] = matrix.matrix[i][j];
              }
            }
        }
    }

    /** @modelguid {5C2C9CBC-1199-47D2-95F1-EA7E3A3F80D6} */
    public float get(int i, int j) {
        if (i < 0 || i > 2 || j < 0 || j > 2) {
            System.out.println("Invalid matrix index.");
            throw new RuntimeException("Invalid indices into matrix.");
        }
        return matrix[i][j];
    }

    /** @modelguid {7EE192DB-A8DE-434E-9A56-C868BC30A41B} */
    public Vector3f getColumn(int i) {
        if (i < 0 || i > 2) {
            System.out.println("Invalid column index.");
            throw new RuntimeException("Invalid column index. " + i);
        }
        return new Vector3f(matrix[0][i], matrix[1][i], matrix[2][i]);
    }
    
    /** @modelguid {9DD0CBBC-99C2-4356-85D1-B3A7426F27CE} */
    public void setColumn(int i, Vector3f column) 
    {
        matrix[0][i] = column.x;
        matrix[1][i] = column.y;
        matrix[2][i] = column.z;
    }

    public void getColumn(int i, Vector3f column)
    {
        column.set(matrix[0][i], matrix[1][i], matrix[2][i]);
    }
    
    public void setLine(int i, Vector3f column) 
    {
        matrix[i][0] = column.x;
        matrix[i][1] = column.y;
        matrix[i][2] = column.z;
    }
    
    public void getLine(int i, Vector3f column)
    {
        column.set(matrix[i][0], matrix[i][1], matrix[i][2]);
    }

    /** @modelguid {FFD68BCC-823D-442A-A575-F641C92DCCE0} */
    public void set(int i, int j, float value) {
        if (i < 0 || i > 2 || j < 0 || j > 2) {
            System.out.println("Invalid matrix index.");
            throw new RuntimeException("Invalid indices into matrix.");
        }
        matrix[i][j] = value;
    }
    
    /** @modelguid {39D0B8B1-CB34-4427-A608-26DFC42C60E0} */
    public void set(float[][] matrix) {
        if(matrix.length != 3 || matrix[0].length != 3) {
            return;
        }
        
        this.matrix = matrix;
    }
    
    /** @modelguid {763186E9-7DCD-4E62-A083-F47632943932} */
	public void set(float[] matrix) {
		if (matrix.length != 9) {
			throw new RuntimeException("Array must be of size 9.");
		}
		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 3; j++) {
				this.matrix[i][j] = matrix[j*3 + i];
			}
		}
	}
    

    /** @modelguid {D1D456A7-A7EA-4EB2-8E91-03188308EDDE} */
    public void set(Quaternion quaternion) {
		matrix[0][0] = (float) (1.0 - 2.0 * quaternion.y * quaternion.y - 2.0 * quaternion.z * quaternion.z);
		matrix[1][0] = (float) (2.0 * quaternion.x * quaternion.y + 2.0 * quaternion.w * quaternion.z);
		matrix[2][0] = (float) (2.0 * quaternion.x * quaternion.z - 2.0 * quaternion.w * quaternion.y);

		matrix[0][1] = (float) (2.0 * quaternion.x * quaternion.y - 2.0 * quaternion.w * quaternion.z);
		matrix[1][1] = (float) (1.0 - 2.0 * quaternion.x * quaternion.x - 2.0 * quaternion.z * quaternion.z);
		matrix[2][1] = (float) (2.0 * quaternion.y * quaternion.z + 2.0 * quaternion.w * quaternion.x);

		matrix[0][2] = (float) (2.0 * quaternion.x * quaternion.z + 2.0 * quaternion.w * quaternion.y);
		matrix[1][2] = (float) (2.0 * quaternion.y * quaternion.z - 2.0 * quaternion.w * quaternion.x);
		matrix[2][2] = (float) (1.0 - 2.0 * quaternion.x * quaternion.x - 2.0 * quaternion.y * quaternion.y);

    }
    

    /** @modelguid {FC342599-1B25-43CE-9011-2516EF85590D} */
	public void loadIdentity() {
		matrix[0][1] = matrix[0][2] = matrix[1][0] = matrix[1][2] = matrix[2][0] = matrix[2][1] = 0;
		matrix[0][0] = matrix[1][1] = matrix[2][2] = 1;
	}
	

    /** @modelguid {41CDA4CF-FDB3-4ED8-AC1D-D58BD432DEC2} */
	public void multiply(float scalar) {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				matrix[i][j] *= scalar;
			}
		}
	}

    /** @modelguid {02320A80-C450-492B-979E-89BB4C0A4A5C} */
    public Matrix3f mult(Matrix3f mat) {
        if (null == mat) {
            System.out.println("Source matrix is " + "null, null result returned.");
            return null;
        }
        Matrix3f product = new Matrix3f();
        for (int iRow = 0; iRow < 3; iRow++) {
            for (int iCol = 0; iCol < 3; iCol++) {
                product.set(
                    iRow,
                    iCol,
                    matrix[iRow][0] * mat.get(0, iCol)
                        + matrix[iRow][1] * mat.get(1, iCol)
                        + matrix[iRow][2] * mat.get(2, iCol));
            }
        }
        return product;
    }
    

    /** @modelguid {D8D8CB51-7E85-4872-AADA-0E4D7CE3497E} */
    public void mult(Matrix3f mat, Matrix3f product) {
    	if (null == mat) {
    		System.out.println("Source matrix is " + "null, null result returned.");
    		return;
    	}
    	
    	for (int iRow = 0; iRow < 3; iRow++) {
    		for (int iCol = 0; iCol < 3; iCol++) {
    			product.set(
    					iRow,
						iCol,
						matrix[iRow][0] * mat.get(0, iCol)
						+ matrix[iRow][1] * mat.get(1, iCol)
						+ matrix[iRow][2] * mat.get(2, iCol));
    		}
    	}
    }


    /** @modelguid {71032C55-AB2F-4878-8F7C-674CCC482371} */
    public Vector3f mult(Vector3f vec) {
        if (null == vec) {
            System.out.println("Source vector is" + " null, null result returned.");
            return null;
        }
        Vector3f product = new Vector3f();
        product.x =
            matrix[0][0] * vec.x + matrix[0][1] * vec.y + matrix[0][2] * vec.z;
        product.y =
            matrix[1][0] * vec.x + matrix[1][1] * vec.y + matrix[1][2] * vec.z;
        product.z =
            matrix[2][0] * vec.x + matrix[2][1] * vec.y + matrix[2][2] * vec.z;

        return product;
    }
    
    /** @modelguid {48E39B17-162A-4163-B84F-8CD8E9FC6B51} */
    public void mult(Vector3f vec, Vector3f product) {
    	if(null == vec) {
    		System.out.println("Source vector is" + " null, null result returned.");
    		return;
    	}
    	
    	if(null == product) {
    		product = new Vector3f();
    	}
    	
    	product.x =
    		matrix[0][0] * vec.x + matrix[0][1] * vec.y + matrix[0][2] * vec.z;
    	product.y =
    		matrix[1][0] * vec.x + matrix[1][1] * vec.y + matrix[1][2] * vec.z;
    	product.z =
    		matrix[2][0] * vec.x + matrix[2][1] * vec.y + matrix[2][2] * vec.z;
    	
    }
    

    /** @modelguid {92C5E1D6-3488-4580-8ED8-5AB90E7AD55E} */
	public void add(Matrix3f matrix) {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				this.matrix[i][j] += matrix.get(i,j);
			}
		}
	}

    /** @modelguid {86569A78-B5E2-42C0-A148-8966EA6E840E} */
    public void fromAxisAngle(Vector3f axis, float radian) {
        Vector3f normAxis = axis.clone();
        normAxis.normalize();
        float cos = (float)Math.cos(radian);
        float sin = (float)Math.sin(radian);
        float oneMinusCos = 1.0f - cos;
        float x2 = normAxis.x * axis.x;
        float y2 = normAxis.y * axis.y;
        float z2 = normAxis.z * axis.z;
        float xym = normAxis.x * axis.y * oneMinusCos;
        float xzm = normAxis.x * axis.z * oneMinusCos;
        float yzm = normAxis.y * axis.z * oneMinusCos;
        float xSin = normAxis.x * sin;
        float ySin = normAxis.y * sin;
        float zSin = normAxis.z * sin;

        matrix[0][0] = x2 * oneMinusCos + cos;
        matrix[0][1] = xym - zSin;
        matrix[0][2] = xzm + ySin;
        matrix[1][0] = xym + zSin;
        matrix[1][1] = y2 * oneMinusCos + cos;
        matrix[1][2] = yzm - xSin;
        matrix[2][0] = xzm - ySin;
        matrix[2][1] = yzm + xSin;
        matrix[2][2] = z2 * oneMinusCos + cos;
    }
    
    public void transform(Vector3f vec) 
    {
        float x = matrix[0][0] * vec.x + matrix[0][1] * vec.y + matrix[0][2] * vec.z;
        float y = matrix[1][0] * vec.x + matrix[1][1] * vec.y + matrix[1][2] * vec.z;
        float z = matrix[2][0] * vec.x + matrix[2][1] * vec.y + matrix[2][2] * vec.z;
        vec.x = x;
        vec.y = y;
        vec.z = z;
    }

    
    /** @modelguid {30FA6AAE-591B-4B8A-AC81-1BE6FC8590E6} */
    public String toString() {
        String result = "torusworld.math.Matrix3f\n[\n";
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                result += " " + matrix[i][j] + " ";
            }
            result += "\n";
        }
        result += "]";
        return result;
    }
    
    public static void rotatePointAroundAxis(Vector3f point, Vector3f axisDirection, float angle)
    {
        Matrix3f tempMat = new Matrix3f();
        tempMat.fromAxisAngle(axisDirection, -angle);
        tempMat.transform(point);
    }
    
    public static void rotatePointAroundAxis(Vector3f point, Vector3f axisOrigin, Vector3f axisDirection, float angle)
    {
        point.subtract(axisOrigin);
        rotatePointAroundAxis(point, axisDirection, angle);
        point.add(axisOrigin);
    }
}
