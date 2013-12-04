package torusworld.model.md3;

import torusworld.math.Vector3f;
import torusworld.math.Matrix4f;
import torusworld.utility.Conversion;
import java.nio.ByteBuffer;

class Md3Tag 
{
	String name;
	Vector3f position;
	float[] rotation;
    
    public Md3Tag(ByteBuffer buffer, Matrix4f transformation)
    {
        readFromBuffer(buffer, transformation);
    }
    
    public void readFromBuffer(ByteBuffer buffer, Matrix4f transformation)
    {
        byte[] nameBuffer = new byte[64];
        buffer.get(nameBuffer);
        
        name = Conversion.byte2String(nameBuffer);
        position = new Vector3f();
        position.x = buffer.getFloat();
        position.y = buffer.getFloat();
        position.z = buffer.getFloat();
        transformation.rotate(position);
        
        rotation = new float[9];
        buffer.asFloatBuffer().get(rotation); 
        buffer.position(buffer.position() + 9 * 4);
    }
}