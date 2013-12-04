package torusworld.math;

/**
 * 
 * Class used to decode MD3 Normals, maybe useful in other places
 *
 */
public final class CompactNormal 
{
	static float Table[][] = new float[65536][3];
	
	static
	{
		int lat, lng;
		float scale = (float) Math.PI / 128.0f;
		
		for (lng = 0; lng < 256; lng++)
        {
		    float cosLng = (float) Math.cos(scale * lng);
            float sinLng = (float) Math.sin(scale * lng);
			for (lat = 0; lat < 256; lat++)
			{
				float sinLatitude = (float) Math.sin(scale * lat);
				Table[lng*256+lat][0] =  cosLng * sinLatitude;
				Table[lng*256+lat][1] =  sinLng * sinLatitude;
				Table[lng*256+lat][2] = (float) Math.cos(scale * lat);
			}
        }
	}
	
	static public float DecodeNormal(short normal, int coord)
	{
		return Table[(int) (normal & 0xFFFF)][coord];
	}
}
