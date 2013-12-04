package torusworld.model.md3;

import torusworld.utility.Conversion;
import java.nio.ByteBuffer;

class Md3Header
{
	String fileID;
	int version;
	String file;
	int numFrames;
	int numTags;
	int numSurfaces;
	int numSkins;
	int ofsFrames;
	int ofsTags;
	int ofsSurfaces;
	int ofsEOF;

	public Md3Header(ByteBuffer buffer)
	{
		readFromBuffer(buffer);
	}

	public void readFromBuffer(ByteBuffer buffer)
	{
		byte idBuffer[] = new byte[4];
        buffer.get(idBuffer);

		fileID = Conversion.byte2String(idBuffer);
        if (!fileID.equals("IDP3"))
            System.out.println("Warning: Invalid magic in md3 header");

		version = buffer.getInt();

		byte fileBuffer[] = new byte[68];
        buffer.get(fileBuffer);
		file = Conversion.byte2String(fileBuffer);

		numFrames = buffer.getInt();
		numTags = buffer.getInt();
		numSurfaces = buffer.getInt();
		numSkins = buffer.getInt();
		ofsFrames = buffer.getInt();
		ofsTags = buffer.getInt();
		ofsSurfaces = buffer.getInt();
		ofsEOF = buffer.getInt();
	}

}
