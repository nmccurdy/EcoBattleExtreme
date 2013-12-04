package torusworld.model.md3;

import torusworld.utility.Conversion;
import java.nio.ByteBuffer;

class Md3SurfaceHeader
{
    String surfaceID;
    String name;
    int numFrames;
    int numSkins;
    int numVertices;
    int numTriangles;
    int ofsTriangles;
    int ofsSkins;
    int ofsTexCoords;
    int ofsVertices;
    int ofsEnd;

    public Md3SurfaceHeader(ByteBuffer buffer)
    {
        readFromBuffer(buffer);
    }

    public void readFromBuffer(ByteBuffer buffer)
    {
        byte[] meshBuffer = new byte[4];
        buffer.get(meshBuffer);
        surfaceID = Conversion.byte2String(meshBuffer);

        if (!surfaceID.equals("IDP3"))
            System.out.println("Invalid surface magic in md3 surface header");

        byte[] nameBuffer = new byte[68];
        buffer.get(nameBuffer);

        name = Conversion.byte2String(nameBuffer);
        numFrames = buffer.getInt();
        numSkins = buffer.getInt();
        numVertices = buffer.getInt();
        numTriangles = buffer.getInt();
        ofsTriangles = buffer.getInt();
        ofsSkins = buffer.getInt();
        ofsTexCoords = buffer.getInt();
        ofsVertices = buffer.getInt();
        ofsEnd = buffer.getInt();
    }
}
