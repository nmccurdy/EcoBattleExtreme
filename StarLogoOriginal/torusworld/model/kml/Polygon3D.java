/*
A triangle.
*/

package torusworld.model.kml;

class Polygon3D
{
    // material to use for this polygon
    public String materialName;
    
    // vertex coordinates: (x1,y1,z1,x2,y2,z2,x3,y3,z3)
    public float vertices[] = new float[9];
    // texture coordinates for vertices if non-null: (x1,y1,x2,y2,x3,y3)
    public float texture[] = null;
    // normal vectors for vertices if non-null: (x1,y1,z1,x2,y2,z2,x3,y3,z3)
    public float normal[] = new float[9];
    // Specifies a normal for the entire face: (x, y, z)
    //public float faceNormal[] = null;
    public float faceNormal[] = null;
    
    public Polygon3D clone()
    {
    	Polygon3D p = new Polygon3D();
    	p.materialName = this.materialName;
    	p.vertices = this.vertices.clone();
    	p.texture = this.texture.clone();
    	p.normal = this.normal.clone();
    	p.faceNormal = this.faceNormal.clone();
    	
    	return p; 
    }
        
}
