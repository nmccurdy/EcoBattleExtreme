/*
A triangle.
*/

package torusworld.model.obj;

class Polygon3D
{
    // material to use for this polygon
    public String materialName;
    
    // vertex coordinates: (x1,y1,z1,x2,y2,z2,x3,y3,z3)
    public float vertices[] = new float[9];
    // texture coordinates for vertices if non-null: (x1,y1,x2,y2,x3,y3)
    public float texture[] = null;
    // normal vectors for vertices if non-null: (x1,y1,z1,x2,y2,z2,x3,y3,z3)
    public float normal[] = null;
    // Specifies a normal for the entire face: (x, y, z)
    public float faceNormal[] = null;
        
}
