package torusworld.model.md3;

import torusworld.math.Vector3f;
import torusworld.utility.Conversion;
import java.nio.ByteBuffer;

class Md3FrameInfo
{
    Vector3f mins, maxs, position;
	float scale;
	String creator;

    public Md3FrameInfo(ByteBuffer buffer)
    {
        mins = new Vector3f();
        maxs = new Vector3f();
        position = new Vector3f();
        readFromBuffer(buffer);
    }

    void readFromBuffer(ByteBuffer buffer)
    {
        mins.x = buffer.getFloat();
        mins.y = buffer.getFloat();
        mins.z = buffer.getFloat();
        maxs.x = buffer.getFloat();
        maxs.y = buffer.getFloat();
        maxs.z = buffer.getFloat();
        position.x = buffer.getFloat();
        position.y = buffer.getFloat();
        position.z = buffer.getFloat();
        scale = buffer.getFloat();

        byte[] creatorBuff = new byte[16];
        buffer.get(creatorBuff);
        creator = Conversion.byte2String(creatorBuff);
    }
}
