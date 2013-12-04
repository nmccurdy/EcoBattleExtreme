package terraineditor;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import starlogoc.Colors;
import starlogoc.StarLogo;
import starlogoc.TerrainData;
import torusworld.Camera;
import torusworld.SLCameras;
import torusworld.math.Vector3f;
/**
 * This class has utility methods for reading and writting terrain
 * data to disk.  Its get__String and set__FromString methods use 
 * LITTLE_ENDIAN byte order to provide platform independence for 
 * saving and loading.
 * 
 * To add another property to a terrain save file, simply add a method
 * that returns a save string, and then expand the setPropertyFromString method
 * to handle setting that new property.  This should allow the current format
 * to be forward-compatible.  Special backward-compatible loading methods
 * are here too (indicated by a version number in their names), but no more
 * should be written.
 * 
 * Terrain property information should follow this format:
 * 
 * terrain_property-name long_string_with_no_delimeters 
 * (e.g. terrain_property-colors ArKJHjhkJHkjhKhjfsdkjhgKJLHkjsdhfasKJHFkjh)
 * 
 * @author Daniel
 *
 */
public class TerrainFileFormatUtils {

	/******************************************************************
	 * These methods return a string to be saved to a file
	 ******************************************************************/
	
	/**
	 * converts the heights of the given TerrainData to a base64 String
	 * by gzipping an array of the heights and then encoding it in base64
	 * @param td 
	 */
	public static String getHeightsString(TerrainData td) {
		// 4 float heights per patch
		byte[] heightsBytes = new byte[4*td.getWidth()*td.getHeight()*(Float.SIZE/8)];
		ByteBuffer heightsByteBuff = ByteBuffer.wrap(heightsBytes);
		heightsByteBuff.order(ByteOrder.LITTLE_ENDIAN); // be consistent across platforms
		FloatBuffer heightsFloatBuff = heightsByteBuff.asFloatBuffer();
		
		float[] heights = new float[4];
		heightsFloatBuff.position(0);
		for (int x = 0; x < td.getWidth(); x++) {
			for (int y = 0; y < td.getHeight(); y++) {
				td.getHeights(x,y,heights); // reuse heights array for speed
				heightsFloatBuff.put(heights); // add the heights to the buffer
			}
		}
		
		heightsBytes = gzipByteArray(heightsBytes);
		return terraineditor.Base64.encode(heightsBytes);
	}
	
	/**
	 * converts the colors of the given TerrainData to a base64 String
	 * by gzipping an array of the colors and then encoding it in base64
	 * @param td 
	 */
	public static String getColorsString(TerrainData td) {
		// 1 SLNUM (double) color per patch
//		byte[] colorsBytes = new byte[td.getWidth()*td.getHeight()*(Double.SIZE/8)];
//		ByteBuffer colorsByteBuff = ByteBuffer.wrap(colorsBytes);
//		colorsByteBuff.order(ByteOrder.LITTLE_ENDIAN); // be consistent across platforms
//		DoubleBuffer colorsDoubleBuff = colorsByteBuff.asDoubleBuffer();
//
//		colorsDoubleBuff.position(0);
//		for (int x = 0; x < td.getWidth(); x++) {
//			for (int y = 0; y < td.getHeight(); y++) {
//				colorsDoubleBuff.put(td.getColorNumber(x,y));
//			}
//		}
//		
//		colorsBytes = gzipByteArray(colorsBytes);
//		return terraineditor.Base64.encode(colorsBytes);
		return "";
	}

	/**
	 * converts the camepra positions to a base64 String
	 * by gzipping an array of the colors and then encoding it in base64
	 */
	public static String getCamerasString() {
		byte[] cameraBytes = new byte[(3*9+3)*(Float.SIZE/8)];
		ByteBuffer cameraByteBuff = ByteBuffer.wrap(cameraBytes);
		cameraByteBuff.order(ByteOrder.LITTLE_ENDIAN); // be consistent across platforms
		FloatBuffer cameraFloatBuff = cameraByteBuff.asFloatBuffer();

    	Vector3f v; 
    	
		for (int i=0;i<3;i++) {
			Camera camera = SLCameras.getCamera(i);
			
			v = camera.getMutableDirection();
			cameraFloatBuff.put(v.x);
			cameraFloatBuff.put(v.y);
			cameraFloatBuff.put(v.z);

			v = camera.getMutablePosition();
			cameraFloatBuff.put(v.x);
			cameraFloatBuff.put(v.y);
			cameraFloatBuff.put(v.z);

			v = camera.getMutableUpVector();
			cameraFloatBuff.put(v.x);
			cameraFloatBuff.put(v.y);
			cameraFloatBuff.put(v.z);
		}
		
		cameraFloatBuff.put((float)SLCameras.currentCamera);
		cameraFloatBuff.put((float)SLCameras.getCurrentAgentWho());
		cameraFloatBuff.put(SLCameras.orthoView?1.0f:0.0f);
		
		cameraBytes = gzipByteArray(cameraBytes);
		return terraineditor.Base64.encode(cameraBytes);
	}
	
	//TODO Returns associated texture as a string, for the save method
	//Haven't used intbuffer before, so this could create some unexpected issues
	public static String getTextureString(TerrainData td) {
		
		//We first convert everything into bytes (ints, floats, etc.)
		//Then we encode that byte array.
		
		byte[] textureBytes = new byte[td.getAssociatedWidth()*td.getAssociatedHeight()*(Integer.SIZE/8)];
		ByteBuffer textureByteBuff = ByteBuffer.wrap(textureBytes);
		textureByteBuff.order(ByteOrder.LITTLE_ENDIAN); // be consistent across platforms
		IntBuffer textureIntBuff = textureByteBuff.asIntBuffer();
		
		textureIntBuff.position(0);
		for (int x = 0; x < td.getAssociatedWidth(); x++) {
			for (int y = 0; y < td.getAssociatedHeight(); y++) {
				textureIntBuff.put(td.getImagePixel(x, y));
			}
		}
		
		textureBytes = gzipByteArray(textureBytes);
		return terraineditor.Base64.encode(textureBytes);
		
	}
	
	/**
	 * Gzips the name and encodes it as base64
	 */
	public static String getGzippedString(String name) {
		return terraineditor.Base64.encode(gzipByteArray(name.getBytes())); 
	}
	
	/**
	 * Gets the name as a string
	 */
	public static String getGunzippedString(String name) {
		return new String(convertToByteBuffer(name).array()); 
	}
	
	public static String escape(String s) {
        return s.replaceAll("&", "&amp;")
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("\"", "&quot;");
    }
	
	
	/**************************************************************************
	 * These methods are used when loading a file from a String
	 **************************************************************************

	/** 
	 * This is the method that all loading should start with - having
	 * a generic "property loader" allows for the addition of more
	 * properties to the format while retaining compatibility.
	 * @param propertyName - the name of the property, e.g. "terrain_property-heights"
	 * @param dataString - the string that the data is stored in
	 * @param td - the TerrainData object whose properties will be set according to the supplied data
	 */
	public static void setPropertyFromString(String propertyName, String dataString, TerrainData td) {
		// depending on the property name, call the appropriate method
		if (propertyName.equals("terrain_property-heights") || propertyName.equals("terrain_property_copy-heights")) {
			setHeightsFromString(dataString, td);
		}
		else if (propertyName.equals("terrain_property-colors") || propertyName.equals("terrain_property_copy-colors")) {
			setColorsFromString(dataString, td);			
		}
		else if (propertyName.equals("terrain_property-cameras")) {
			setCamerasFromString(dataString, td);			
		}//TODO MODIFICATION
		else if (propertyName.equals("terrain_property-texture") || propertyName.equals("terrain_property_copy-texture")) {
			setTextureFromString(dataString, td);
		}		
/*		else if (propertyName.equals("")) { // follow this template for other properties
			
		}*/
		else { 
			// future versions might add more properties, so just print a message if
			// the current one isn't recognized.
			System.out.println("Property name not recognized while loading terrain: "+propertyName);
		}		
	}

	/**
	 * sets the heights of a terrain from the data string
	 * @param data
	 * @param td
	 */
	private static void setHeightsFromString(String data, TerrainData td) {
		// decompress data to prepare for reading it
		FloatBuffer floatBuff = convertToByteBuffer(data).asFloatBuffer();
		
		// now set the terrain data
		float[] heights = new float[4];
		floatBuff.position(0);
		for (int x = 0; x < td.getWidth(); x++) {
			for (int y = 0; y < td.getHeight(); y++) {
				floatBuff.get(heights); //reuse heights array for speed
				td.setHeights(x, y, heights);
			}
		}
	}
	
	//TODO
	/** cmcheng
	 * CHANGED: Sets texture different colors, patches are all white.
	 * sets the colors of a terrain from the data string
	 * @param data
	 * @param td
	 */
	private static void setColorsFromString(String data, TerrainData td) {
		// decompress data to prepare for reading it
		DoubleBuffer doubleBuff = convertToByteBuffer(data).asDoubleBuffer();
		
		// now set the terrain data
		/*doubleBuff.position(0);
		for (int x = 0; x < td.getWidth(); x++) {
			for (int y = 0; y < td.getHeight(); y++) {
				td.setColor(x, y, doubleBuff.get());
			}
		}*/
		doubleBuff.position(0);
		for (int x = 0; x < td.getWidth(); x++) {
			for (int y = 0; y < td.getHeight(); y++) {
				//Convert the color to a Java Color
				double unconvertedColor = doubleBuff.get();
				long l = StarLogo.doubleToSlnum(unconvertedColor);
		    	float[] colorVect =	Colors.colorarray[(int)(StarLogo.slnumToDouble(l)*32)].getRGBComponents(null);
		    	Color converted = new Color(colorVect[0], colorVect[1], colorVect[2]);
				
				td.paintPatch(x, y, converted);
			}
		}
	}

	/**
	 * sets the cameras from the data string
	 * @param data
	 * @param td
	 */
	private static void setCamerasFromString(String data, TerrainData td) {
		// decompress data to prepare for reading it
		FloatBuffer floatBuff = convertToByteBuffer(data).asFloatBuffer();
		SLCameras.loadPositionsFromFloatBuffer(floatBuff);		
	}
	
	//TODO TFFS Modification
	private static void setTextureFromString(String data, TerrainData td) {
		// decompress data to prepare for reading it
		IntBuffer intBuff = convertToByteBuffer(data).asIntBuffer();
		
		// now set the terrain data for the texture
		intBuff.position(0);
		for (int x = 0; x < td.getAssociatedWidth(); x++) {
			for (int y = 0; y < td.getAssociatedHeight(); y++) {
				td.setImagePixel(x, y, intBuff.get());
			}
		}
	}
	
	public static void setHeightsFromStringVersion0(String heightsData, TerrainData td) {
		// get byte info from string for heights
		byte byteArray[] = terraineditor.Base64.decode(heightsData);
		byteArray = gunzipByteArray(byteArray);
		float floatArray[] = new float[byteArray.length/4];

		// wrap the source byte array in a byte buffer
		ByteBuffer byteBuf = ByteBuffer.wrap(byteArray);
		byteBuf.order(ByteOrder.BIG_ENDIAN);

		// create a view of the byte buffer as a float buffer
		FloatBuffer floatBuf = byteBuf.asFloatBuffer();

		// now fill in the heights of the new terrain by adapting 
		// from the saved old format terrain.
		// Scale by 1/patchSize so it comes in looking correct (old
		// terrains had 3x3 patches but 1 height scale - new are 3 height).
		floatBuf.position(0);
		floatBuf.get(floatArray);
		float[] heights = new float[4];
		for (int x = 0; x < 101; x++) {
			for (int y = 0; y < 101; y++) {
				heights[0] = floatArray[getHeightArrayIndex(x,y,0,0)] / td.getPatchSize();
				heights[1] = floatArray[getHeightArrayIndex(x,y,0,3)] / td.getPatchSize();
				heights[2] = floatArray[getHeightArrayIndex(x,y,3,3)] / td.getPatchSize();
				heights[3] = floatArray[getHeightArrayIndex(x,y,3,0)] / td.getPatchSize();
				td.setHeights(x,y,heights);
			}
		}

	}
	
	//TODO
	/**
	 * Changed. Previously modified patches but now modifies textures
	 * @param colorData
	 * @param td
	 */
	public static void setColorsFromStringVersion0(String colorData, TerrainData td) {
		// get byte info from string for color
		byte[] byteArray = terraineditor.Base64.decode(colorData);
		byteArray = gunzipByteArray(byteArray);
		long longArray[] = new long[byteArray.length/8];

		// wrap the source byte array to the byte buffer
		ByteBuffer byteBuf = ByteBuffer.wrap(byteArray);

		// create a view of the byte buffer as a float buffer
		LongBuffer longBuf = byteBuf.asLongBuffer();

		// now load the color data into the terrain (this used to 
		// be stored in a separate array, which is why this is a 
		// separate step
		longBuf.position(0);
		longBuf.get(longArray);
		
		/* Old code
		for (int x = 0; x < 101; x++) {
			for (int y = 0; y < 101; y++) {
				td.setColor(x,y,StarLogo.slnumToDouble(longArray[getColorArrayIndex(x,y)]));
			}
		}*/
		
		//New Code
		for (int x = 0; x < 101; x++) {
			for (int y = 0; y < 101; y++) {
				
				//Convert the color to a Java Color
				double unconvertedColor = StarLogo.slnumToDouble(longArray[getColorArrayIndex(x,y)]);
				long l = StarLogo.doubleToSlnum(unconvertedColor);
		    	float[] colorVect =	Colors.colorarray[(int)(StarLogo.slnumToDouble(l)*32)].getRGBComponents(null);
		    	Color converted = new Color(colorVect[0], colorVect[1], colorVect[2]);
				
				td.paintPatch(x, y, converted);
				
		}
	}
	
	}
	
	/**
	 * Converts a 8 byte array of unsigned bytes to an long
	 * @param b an array of 4 unsigned bytes
	 * @return a long representing the unsigned int
	 */
	public static final long eightBytesToLong(byte[] b) 
	{
	    long l = 0;
	    l |= b[0] & 0xFF;
	    l <<= 8;
	    l |= b[1] & 0xFF;
	    l <<= 8;
	    l |= b[2] & 0xFF;
	    l <<= 8;
	    l |= b[3] & 0xFF;
	    l <<= 8;
	    l |= b[4] & 0xFF;
	    l <<= 8;
	    l |= b[5] & 0xFF;
	    l <<= 8;
	    l |= b[6] & 0xFF;
	    l <<= 8;
	    l |= b[7] & 0xFF;
	    return l;
	}
	
	//TODO
	/**
	 * Changed. Modifies texture now instead of patches
	 * @param terrainData
	 * @param td
	 */
	public static void setTerrainFromString(String terrainData, TerrainData td) {
		byte byteArray[] = terraineditor.Base64.decode(terrainData);
		byteArray = gunzipByteArray(byteArray);

		//New
		for (int i = 0; i < byteArray.length/8; i++) {
			byte[] temp = {
					byteArray[i*8 + 0],
					byteArray[i*8 + 1],
					byteArray[i*8 + 2],
					byteArray[i*8 + 3],
					byteArray[i*8 + 4],
					byteArray[i*8 + 5],
					byteArray[i*8 + 6],
					byteArray[i*8 + 7]
			};
			long slColor = eightBytesToLong(temp);
			
			//i is the index of the patch
			int patchX = (i%td.getWidth());
			int patchY = (i/td.getWidth());
			float[] colorVect =	Colors.colorarray[(int)(StarLogo.slnumToDouble(slColor)*32)].getRGBComponents(null);
	    	Color converted = new Color(colorVect[0], colorVect[1], colorVect[2]);
			
	    	td.paintPatch(patchX, patchY, converted);
		}
		
		//Old
		// fill in the data of the loaded terrain  
		//td.patches.position(0);
		//td.patches.put(byteArray);
	}
	
	
	/************************************************************
	 * Private methods for zipping and unzipping byte arrays
	 ************************************************************/

	private static ByteBuffer convertToByteBuffer(String data) {
		byte byteArray[] = terraineditor.Base64.decode(data);
		byteArray = gunzipByteArray(byteArray);
		ByteBuffer byteBuff = ByteBuffer.wrap(byteArray);
		byteBuff.order(ByteOrder.LITTLE_ENDIAN);
		return byteBuff;
	}
	
	private static int readIntFromByteArray(byte[] out, int offset) {
		// big endian
		int val = ((((int)out[offset]) & 0xFF) << 24) |
			((((int)out[offset+1]) & 0xFF) << 16) | 
			((((int)out[offset+2]) & 0xFF) << 8) |
			(((int)out[offset+3]) & 0xFF);
		//System.out.println("read int 0x" + Integer.toString(val, 16));
		return val;
	}

	private static int getEntrySize(byte[] extra) {
		int end = extra.length - 4;
		return readIntFromByteArray(extra, end);
	}

	private static void setEntrySize(ByteArrayOutputStream extra, int size) {
		extra.write((byte)((size >> 24) & 0xff));
		extra.write((byte)((size >> 16) & 0xff));
		extra.write((byte)((size >> 8) & 0xff));
		extra.write((byte) (size & 0xff));
	}
	
	public static byte[] gzipByteArray(byte[] bytes) {
		ByteArrayOutputStream baos;
		try {
			GZIPOutputStream gzout = new GZIPOutputStream(baos = new ByteArrayOutputStream());
			gzout.write(bytes);
			gzout.finish();
			gzout.flush();
			setEntrySize(baos, bytes.length);
			gzout.close();
			return baos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Error compressing terrain data.");
		}
	}

	private static byte[] gunzipByteArray(byte[] compbytes) {
		int length = getEntrySize(compbytes);
		try {
			GZIPInputStream zip = new GZIPInputStream(new ByteArrayInputStream(compbytes));
      
			byte[] bytes = new byte[length];
      
			int count = 0;
			do {
				int numread = zip.read(bytes, count, length - count);
				if (numread < 0) break;
				count += numread;
			} while(count < length);

			return bytes;
		}
		catch (IOException e) { 
			e.printStackTrace();
			throw new RuntimeException("Error decompressing terrain file.");
		}
	}

	/**************************************************************
	 * Indexing methods to support Version 0 legacy terrain format.
	 * DO NOT use these methods for any new methods.
	 **************************************************************/

	private static int getColorArrayIndex(int patchX, int patchY)
	{
		return (patchX + patchY*101)*4;
	}
 
	private static int getHeightArrayIndex(int x, int y, int subx, int suby)
	{
		if (subx == 3) {
			x++;
			subx = 0;
		}
		if (suby == 3) {
			y++;
			suby = 0;
		}
		int indexVal = (y*(101+1)+x)*18+suby*3+subx; // old terrains were 101x101
		// check bounds and return -1 if out of bounds
		if (indexVal < 0 || indexVal > 189108) {
				assert false;
		}
		return indexVal;
	}


}
