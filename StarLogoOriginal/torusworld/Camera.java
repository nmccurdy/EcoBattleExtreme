package torusworld;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

import torusworld.math.Matrix3f;
import torusworld.math.Matrix4f;
import torusworld.math.Frustum;
import torusworld.math.Quaternion;
import torusworld.math.Vector3f;

public class Camera
{
    /** Up Vector */
    private Vector3f up = new Vector3f(0.f, 1.f, 0.f); /* should be perpendicular to direction */
    private Vector3f position = new Vector3f(0.f, 0.f, 0.f);
    private Vector3f direction = new Vector3f(0.f, 0.f, 1.f); /* should be unit-length */
    
    private double projMatrix[] = new double[16];
    private double modlMatrix[] = new double[16];
    private int viewport[] = new int[4];
    private Frustum frustum = new Frustum();    
    
    private Vector3f defaultPosition, defaultDirection, defaultUp; 

    public Camera(float halfSize, float centerY)
    {
        setDefaultOrientation(halfSize, centerY);
    }
        
    public void setDefaultView(Vector3f pos, Vector3f dir, Vector3f up) {
    	defaultPosition = new Vector3f(pos); 
    	defaultDirection = new Vector3f(dir); 
    	defaultDirection.normalize();
    	defaultUp = new Vector3f(up);
    	defaultUp.normalize(); 
    }
    
    public void resetCameraView() {
    	position = new Vector3f(defaultPosition);
    	direction = new Vector3f(defaultDirection);
    	up = new Vector3f(defaultUp);    	
    }


    public void setDefaultOrientation(float halfSize, float centerY)
    {
    	position = new Vector3f(0.0f, halfSize, halfSize);
    	direction = new Vector3f(0.0f, centerY - halfSize, - halfSize);
    	direction.normalize();
    	up = new Vector3f(0.0f, halfSize, centerY-halfSize);
		up.normalize();
		setDefaultView(position, direction, up); 
    }

    //*********************************************** setter and getter methods **************************************
    
    public double[] getProjMatrix() { return projMatrix; }
    public double[] getModlMatrix() { return modlMatrix; }
    public int[] getViewport() { return viewport; }
    
    public Frustum getFrustum() { return frustum; }
    
	public void setPosition(Vector3f pos) { position = pos; }

    public Vector3f getPositionCopy()
    {
    	return new Vector3f(position);
    }
    
    public Vector3f getMutablePosition() { return position; }

	public void setDirection(Vector3f dir)
    {
		direction = dir;
        direction.normalize();
    }

    public Vector3f getDirectionCopy()
    {
        return new Vector3f(direction);
    }

    public Vector3f getMutableDirection()
    {
        return direction;
    }


    /* note: this shouldn't really be used, unless it is to restore some old camera position */
    public void setUpVector(Vector3f upvec) { up = upvec; }

    public Vector3f getMutableUpVector() { return up; }

    public Vector3f getUpVectorCopy()
    {
        return new Vector3f(up);
    }
    
    public Vector3f getRightVector()
    {
    	return Vector3f.cross(direction, up);
    }
	
	public void computeUpVector() /* computes an up vector pointing up but perpendicular to lookvector */
	{
		up = Vector3f.cross(Vector3f.cross(direction, new Vector3f(0.f, 1.f, 0.f)),
                            direction);
		up.normalize();
	}

	public void applyCamera()
	{
        GL gl = SLRendering.getGL();
        GLU glu = SLRendering.getGLU();
		gl.glLoadIdentity();
		glu.gluLookAt(position.x, position.y, position.z,
		              position.x+direction.x, position.y+direction.y, position.z+direction.z,
		              up.x,      up.y,      up.z);
        gl.glGetDoublev(GL.GL_PROJECTION_MATRIX, projMatrix, 0);
        gl.glGetDoublev(GL.GL_MODELVIEW_MATRIX, modlMatrix, 0);
        gl.glGetIntegerv(GL.GL_VIEWPORT, viewport, 0);
        frustum.calculateFrustum(projMatrix, modlMatrix);
	}
	
	/** Translates the camera by amounts relative to the world */
	
	public void translateGlobal(float dx, float dy, float dz)
	{
		position.x += dx;
		position.y += dy;
		position.z += dz;
	}

	/**
	 * Translates the camera by the amount (dx, dy, dz), relative to the camera's
	 * orientation
	 */
	public void translate(float dx, float dy, float dz)
	{
		position.add(Vector3f.mult(getRightVector(), dx));
        position.add(Vector3f.mult(up, dy));
        position.add(Vector3f.mult(direction, -dz));
	}
	
	/**
	 * Rotates the camera<br>
	 * @param drx Degrees to rotate on the x-axis
	 * @param dry Degrees to rotate on the y-axis
	 * @param drz Degrees to rotate on the z-axis
	 */
	public void rotate(float drx, float dry, float drz)
	{
		Matrix4f rotationMatrix = new Matrix4f();
		Quaternion quat = new Quaternion(), q2 = new Quaternion();
		
		quat.fromAngleAxis(dry, up);
        q2.fromAngleAxis(drx, getRightVector());
		quat = quat.mult(q2);
		//q2.fromAngleAxis(drz, direction);
		//quat = quat.mult(q2);
		
		rotationMatrix.setRotationQuaternion(quat);
		rotationMatrix.rotate(direction);

		computeUpVector();
	}
    
    // rotates view around a point
    // If camera is looking towards the point, after the rotation, the camera 
    // will still look towards that point, but from a different angle 
    public void rotateHorizontallyAround(Vector3f point, float angle)
    {
        
        Matrix4f rotationMatrix = new Matrix4f();
        Quaternion quat = new Quaternion();
        
        position.subtract(point);
        Vector3f target = Vector3f.add(position, direction);
        
        quat.fromAngleAxis(angle, new Vector3f(0, 1, 0));
        
        rotationMatrix.setRotationQuaternion(quat);
        rotationMatrix.rotate(position);
        rotationMatrix.rotate(target);
        
        direction = Vector3f.subtract(target, position);
        position.add(point);
        computeUpVector();
    }
    
    public void rotateVerticallyAround(Vector3f point, float angle)
    {
        
        Matrix4f rotationMatrix = new Matrix4f();
        Quaternion q1 = new Quaternion();
        
        position.subtract(point);
        Vector3f target = Vector3f.add(position, direction);
        
        // For up/down, rotate around vector perpendicular to direction on xz plane
        q1.fromAngleAxis(angle, new Vector3f(direction.z, 0, - direction.x));
        rotationMatrix.setRotationQuaternion(q1);
        rotationMatrix.rotate(position);
        rotationMatrix.rotate(target);
        
        direction = Vector3f.subtract(target, position);
        position.add(point);
        computeUpVector();
    }
    
    // wrapper for gluUnproject on the camera matrices
    private double tempArray[] = new double[3];
    public void unProject(double x, double y, double z, Vector3f result)
    {
        SLRendering.getGLU().gluUnProject(x, y, z, modlMatrix, 0, projMatrix, 0, 
                                          viewport, 0, tempArray, 0);
        result.x = (float) tempArray[0];
        result.y = (float) tempArray[1];
        result.z = (float) tempArray[2];
    }
    
    public void project(double x, double y, double z, Vector3f result)
    {
        SLRendering.getGLU().gluProject(x, y, z, modlMatrix, 0, projMatrix, 0, 
                                        viewport, 0, tempArray, 0);
        result.x = (float) tempArray[0];
        result.y = (float) tempArray[1];
        result.z = (float) tempArray[2];
    }
    
    public Matrix3f computeRotationMatrix(Matrix3f matrix)
    {
        matrix.setLine(0, getRightVector());
        matrix.setLine(1, up);
        direction.negate();
        matrix.setLine(2, direction);
        direction.negate();
        return matrix;
    }
    
    public void fromRotationMatrix(Matrix3f matrix)
    {
        matrix.getLine(1, up);
        matrix.getLine(2, direction);
        direction.negate();
    }
}
