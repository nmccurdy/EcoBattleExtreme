package torusworld.model.md3;

import torusworld.utility.Conversion;
import java.nio.ByteBuffer;

class Md3Skin 
{
	String name;
	
	public Md3Skin()
	{
		name = "";
	}
	
	public Md3Skin(ByteBuffer buffer)
	{
		readFromBuffer(buffer);
	}
	
	public void readFromBuffer(ByteBuffer buffer)
	{
		byte[] skinBuffer = new byte[68];
		buffer.get(skinBuffer);
		name = Conversion.byte2String(skinBuffer);
	}
}
