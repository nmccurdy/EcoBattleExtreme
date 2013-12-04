package torusworld.math;

public class CoordinateSystem
{
    public Vector3f origin = new Vector3f();
    public Vector3f xAxis = new Vector3f();
    public Vector3f yAxis = new Vector3f();
    public Vector3f zAxis = new Vector3f();
    
    public void set(CoordinateSystem sys)
    {
        origin.set(sys.origin);
        xAxis.set(sys.xAxis);
        yAxis.set(sys.yAxis);
        zAxis.set(sys.zAxis);
    }
    
    // Creates the axes of the coordinate system based on a XZ angle and a Y vector;
    // returns this
    public CoordinateSystem fromHeadingAndYAxis(Vector3f yaxis, float angle)
    {
        yAxis.set(yaxis);
        xAxis.set((float) Math.cos(angle), 0, (float) Math.sin(angle));
        Vector3f.cross(xAxis, yAxis, zAxis);
        zAxis.normalize();
        Vector3f.cross(yAxis, zAxis, xAxis);
        return this;
    }
    
    public CoordinateSystem setOrigin(Vector3f global, Vector3f local)
    {
        origin.set(global);
        origin.addScaled(xAxis, local.x);
        origin.addScaled(yAxis, local.y);
        origin.addScaled(zAxis, local.z);
        return this;
    }
    
    public void globalToLocal(Vector3f point)
    {
        point.subtract(origin);
        float x = Vector3f.dot(point, xAxis);
        float y = Vector3f.dot(point, yAxis);
        float z = Vector3f.dot(point, zAxis);
        point.set(x, y, z);
    }
    
    // Inverse Transpose of 3x3 matrix is equal in our case (rigid body transforms)
    public void globalToLocalDirection(Vector3f dir)
    {
        float x = Vector3f.dot(dir, xAxis);
        float y = Vector3f.dot(dir, yAxis);
        float z = Vector3f.dot(dir, zAxis);
        dir.set(x, y, z);
    }

    
    public void localToGlobal(Vector3f point)
    {
        float x = point.x, y = point.y, z = point.z;
        point.set(origin);
        point.addScaled(xAxis, x);
        point.addScaled(yAxis, y);
        point.addScaled(zAxis, z);
    }
    
    public void localToGlobalDirection(Vector3f dir)
    {
        float x = dir.x, y = dir.y, z = dir.z;
        dir.set(0, 0, 0);
        dir.addScaled(xAxis, x);
        dir.addScaled(yAxis, y);
        dir.addScaled(zAxis, z);
    }
    
    public float[] toGLMatrix()
    {
        // GL matrices are in column-major (see doc for glMultMatrixf)
        return new float[]{
                        xAxis.x, xAxis.y, xAxis.z, 0,
                        yAxis.x, yAxis.y, yAxis.z, 0,
                        zAxis.x, zAxis.y, zAxis.z, 0,
                        origin.x, origin.y, origin.z, 1};
    }


}
