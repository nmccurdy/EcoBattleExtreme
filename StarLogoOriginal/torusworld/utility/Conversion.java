package torusworld.utility;

/** @modelguid {9C67D3AB-2C75-4FE2-8ACA-A802E5BEA81B} */
public class Conversion {
    /** @modelguid {686B8448-D76F-4FB0-B1F1-A91DAB2C6286} */
	public final static short byte2short(byte[] bytes, int index) {
		int s1 = (bytes[index] & 0xFF);
		int s2 = (bytes[index + 1] & 0xFF) << 8;
		return (short)(s1 | s2);
	}

    /** @modelguid {6648D1E4-28AE-4509-BDA8-38514386380D} */
	public final static int byte2int(byte[] bytes, int index) {
		int i1 = (bytes[index] & 0xFF);
		int i2 = (bytes[index + 1] & 0xFF) << 8;
		int i3 = (bytes[index + 2] & 0xFF) << 16;
		int i4 = (bytes[index + 3] & 0xFF) << 24;
		return (i1 | i2 | i3 | i4);
	}

    /** @modelguid {31C83722-CE6A-44CC-AA4A-E545899D6232} */
	public final static float byte2float(byte[] bytes, int index) {
		return Float.intBitsToFloat(byte2int(bytes, index));
	}
	
    /** @modelguid {7C4EB17B-07A8-407A-890C-E40DA5CFFDEA} */
	public final static String byte2String(byte[] b) {
		for (int i = 0; i < b.length; i++) {
			if (b[i] == (byte) 0) {
				return new String(b, 0, i);
			}
		}
		return new String(b);
		
	}
}
