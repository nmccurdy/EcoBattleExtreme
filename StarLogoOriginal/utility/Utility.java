package utility;

import java.applet.Applet;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Vector;

public class Utility {
	public static boolean macintoshp = false;
	public static boolean macosxp = false;
	public static boolean microsoftvmp = false;
	public static boolean solarisp = false;
	public static boolean linuxp = false;
	public static boolean unixp = false;
	public static boolean netscapep = false;
	public static boolean pcp = false;
	public static boolean winxpp = false;
	public static boolean winmep = false;
	public static boolean win98p = false;
	public static boolean win95p = false;
	public static boolean win2kp = false;
	public static boolean winntp = false;

	public static Applet applet = null;

	public static boolean swingavailablep = false;

	public static Locale language = new Locale("en", Locale.getDefault().getCountry());

	static {
		String jv = System.getProperty("java.vendor");
		String os = System.getProperty("os.name");
		String ver = System.getProperty("os.version");
		if (os != null) {
			if (os.startsWith("Windows")) {
				pcp = true;
				if (os.startsWith("Windows 95")) {
					win95p = true;
				} else if (os.startsWith("Windows 98")) {
					win98p = true;
				} else if (os.startsWith("Windows Me")) {
					winmep = true;
				} else if (os.startsWith("Windows NT")) {
					winntp = true;
				} else if (os.startsWith("Windows 2000")) {
					if (ver != null && ver.equals("5.0")) {
						win2kp = true;
					} else {
						winxpp = true;
					}
				} else if (os.startsWith("Windows XP")) {
					winxpp = true;
				} 
				if (jv != null) {
					if (jv.startsWith("Microsoft")) {
						microsoftvmp = true;
					} else if (jv.startsWith("Netscape")) {
						netscapep = true;
					}
				}
			} else if (os.startsWith("Linux")) {
				linuxp = true;
				unixp = true;
			} else if (os.startsWith("Solaris") || os.startsWith("SunOS")) {
				solarisp = true;
				unixp = true;
			} else if (os.startsWith("Mac")) {
				macintoshp = true;
				if (os.startsWith("Mac OS X")) {
					macosxp = true;
				}
			} else {
				unixp = true;
			}
		} else {
			unixp = true;
		}
		try {
			Class.forName("javax.swing.JPanel").newInstance();
			if (!macosxp) javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
			swingavailablep = true;
		} catch (Exception e) { }
	}

	public static String getLanguageAsString() {
		return language.getDisplayLanguage();
	}

	public static String aString(Object o) {
		if (o instanceof String) return (String)o;
		if (o instanceof Character) {
			char[] foo = { ((Character)o).charValue() };
			return new String(foo);
		}
		return printToString(o);
	}

	public static String printToString(Object o) {
		return printToString(o, false);
	}

	@SuppressWarnings("unchecked")
	public static String printToString(Object o, boolean hexp) {
		if (o == null) return "<Java null>";
		if (o instanceof Object[])
			return objectArrayToString((Object[])o, '[', ']', hexp);
		if (o instanceof Vector)
			return vectorToString((Vector)o, hexp);
		if (o instanceof int[])
			return intArrayToString((int[])o, '[', ']', hexp);
		if ((o instanceof Number) &&
				((Number)o).doubleValue()==((Number)o).intValue())
			return Integer.toString(((Number)o).intValue());
		//    if (o instanceof String)
		//return "\"" + (String)o + "\"";
		return o.toString();
	}

	public static String objectArrayToString(Object[] o, char start,
			char end, boolean hexp) {
		StringBuffer sb = new StringBuffer();
		sb.append(start);
		if (o.length > 0) {
			for(int i = 0; i< o.length-1; i++) {
				sb.append(printToString(o[i], hexp));
				sb.append(' ');
			}
			sb.append(printToString(o[o.length-1], hexp));
		}
		sb.append(end);
		return sb.toString();
	}

	public static String vectorToString(Vector<?> v, boolean hexp) {
		StringBuffer sb = new StringBuffer();
		sb.append('<');
		for(Enumeration<?> e = v.elements(); e.hasMoreElements();) {
			sb.append(printToString(e.nextElement(), hexp));
			sb.append(' ');
		}
		sb.append('>');
		return sb.toString();
	}

	public static String intArrayToString(int[] o, char start,
			char end, boolean hexp) {
		StringBuffer sb = new StringBuffer();
		sb.append(start);
		if (o.length > 0) {
			for(int i = 0; i< o.length-1; i++) {
				if (hexp) {
					sb.append("0x" + Integer.toString(o[i], 16) + " ");
					sb.append(' ');
				} else {
					sb.append(Integer.toString(o[i]));
					sb.append(' ');
				}
			}
			sb.append(Integer.toString(o[o.length-1], (hexp) ? 16 :  10));
		}
		sb.append(end);
		return sb.toString();
	}

	public static Object prim_urltostring(Object v0) {
		//System.out.println("top of prim_urltostring. v0 = " + v0);
		try {
			//System.out.println("url to string: " + Utility.printToString(v0));
			URL u = new URL(aString(v0));
			//byte[] buffer=null; String content=null;

			try { 
				InputStream is = null;
				try {
					//System.out.println("before openstream. u = " + u);
					is = u.openStream();
					//System.out.println("after openstream");
					return readStream(new InputStreamReader(is), -1);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				finally { if (is != null) is.close(); }
			}
			catch (IOException f) {}
		}
		catch (MalformedURLException e) {}
		return null;
	}

	public static Object prim_urlwithencodingtostring(Object v0, Object v1) {
		//System.out.println("top of prim_urltostring. v0 = " + v0);
		try {
			//System.out.println("url to string: " + Utility.printToString(v0));
			String urlString = aString(v0);
			urlString = urlString.replace("file://", "file:///");
			URL u = new URL(urlString);
			String encoding = aString(v1);
			//byte[] buffer=null; String content=null;

			try { 
				InputStream is = null;
				try {
					//System.out.println("before openstream. u = " + u);
					is = u.openStream();
					//System.out.println("after openstream");
					return readStream(new InputStreamReader(is, Charset.forName(encoding)), 
							-1);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				finally { if (is != null) is.close(); }
			}
			catch (IOException f) {}
		}
		catch (MalformedURLException e) {}
		return null;
	}


	public static Object prim_urltobytes(Object v0) {
		//System.out.println("top of prim_urltobytes. v0 = " + v0);
		try {
			//System.out.println("url to bytes: " + Utility.printToString(v0));
			URL u = new URL(aString(v0));
			//byte[] buffer=null; String content=null;

			try { 
				InputStream is = null;
				try {
					//System.out.println("before openstream. u = " + u);
					is = u.openStream();
					//System.out.println("after openstream");
					return readStream(is, -1);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				finally { if (is != null) is.close(); }
			}
			catch (IOException f) {}
		}
		catch (MalformedURLException e) {}
		return null;
	}

	public static String readStream(java.io.Reader ir, int length) {
		BufferedReader in = null;
		if (length < 0) length = 4096;
		try {
			in = new BufferedReader(ir, length);
			StringBuffer buffer = new StringBuffer(length);
			char[] chars = new char[length];
			do {
				int result = 0;
				do {
					result = in.read(chars, 0, length);
				} while (result == 0);
				if (result < 0) break;
				buffer.append(chars, 0, result);
			} while(true);
			return buffer.toString();
		}
		catch (IOException e) { 
		}
		finally { if (in != null) try { in.close(); } catch (IOException f) {} }
		return null;
	}

	public static byte[] readStream(InputStream ir, int length) {
		BufferedInputStream in = null;
		if (length < 0) length = 4096;
		try {
			in = new BufferedInputStream(ir, length);
			byte[] file = new byte[length];
			int filepos = 0;
			byte[] buffer = new byte[length];
			do {
				int result = 0;
				do {
					result = in.read(buffer, 0, length);
				} while (result == 0);
				if (result < 0) break;
				System.arraycopy(buffer, 0, file, filepos, result);
				filepos += result;
				if (filepos > file.length) {
					byte[] temp = new byte[file.length * 2];
					System.arraycopy(file, 0, temp, 0, file.length);
					file = temp;
				}
			} while(true);
			byte[] toreturn = new byte[filepos];
			System.arraycopy(file, 0, toreturn, 0, filepos);
			return file;
		}
		catch (IOException e) {
		}
		finally { if (in != null) try { in.close(); } catch (IOException f) {} }
		return null;
	}
	public static Object prim_filetostring(Object v0) {
		if (applet == null) {
			String filename = aString(v0);

			//byte[] buffer=null; String content=null;
			try {
				//System.out.println("prim file made new file from " + filename);
				File f = new File(filename);
				//System.out.println("new file is " + f.toString());
				//try { 
				//System.out.println("canonical name is " + f.getCanonicalPath());
				//f = f.getCanonicalFile();
				//System.out.println("absolute name is " + f.getAbsolutePath());
				//}	
				//catch (IOException e) { e.printStackTrace(); }
				if (!f.canRead()) System.err.println(printToString(v0) + " is not readable");
				FileReader fir = new FileReader(f);
				return readStream(fir, (int)f.length());
			}
			catch (FileNotFoundException e) {
			}
			return null;
		}
		else {
			String filename = "./" + aString(v0);
			try {
				return prim_urltostring(new URL(new URL("file", "", 
						System.getProperty((pcp) ? "user.home" : "user.dir")
						+"/"), filename));
			}
			catch (MalformedURLException e) {}
			return null;
		}
	}

	public static Object prim_filewithencodingtostring(Object v0, Object v1) {
		if (applet == null) {
			String filename = aString(v0);
			String encoding = aString(v1);

			// FIXME this is a hack replacing "%20" with " " in the path. (not sure why this
			// isn't taken care of by the string conversion in the first place)
			while (filename.indexOf("%20")>-1) {
				filename=filename.substring(0, filename.indexOf("%20"))+" "+filename.substring(filename.indexOf("%20")+3);
			}

			//byte[] buffer=null; String content=null;
			try {
				//System.out.println("prim file made new file from " + filename);
				File f = new File(filename);
				//System.out.println("new file is " + f.toString());
				//try { 
				//System.out.println("canonical name is " + f.getCanonicalPath());
				//f = f.getCanonicalFile();
				//System.out.println("absolute name is " + f.getAbsolutePath());
				//}	
				//catch (IOException e) { e.printStackTrace(); }
				if (!f.canRead()) System.err.println(printToString(v0) + " is not readable");
				//System.out.println("read from file " + f + " in encoding " + encoding);
				//Charset cs = Charset.forName(encoding);
				//System.out.println("encoding is " + cs + " display " + cs.displayName() + 
				//		     " name " + cs.name());
				java.io.Reader fir = new InputStreamReader(new FileInputStream(f), 
						Charset.forName(encoding));
				return readStream(fir, (int)f.length());
			}
			catch (FileNotFoundException e) {
			}
			return null;
		}
		else {
			String filename = "./" + aString(v0);
			try {
				return prim_urltostring(new URL(new URL("file", "", 
						System.getProperty((pcp) ? "user.home" : "user.dir")
						+"/"), filename));
			}
			catch (MalformedURLException e) {}
			return null;
		}
	}

	public static Object prim_filetobytes(Object v0) {
		if (applet == null) {
			String filename = aString(v0);

			//byte[] buffer=null; String content=null;
			try {
				//System.out.println("prim file made new file from " + filename);
				File f = new File(filename);
				//System.out.println("new file is " + f.toString());
				//try { 
				//System.out.println("canonical name is " + f.getCanonicalPath());
				//f = f.getCanonicalFile();
				//System.out.println("absolute name is " + f.getAbsolutePath());
				//}	
				//catch (IOException e) { e.printStackTrace(); }
				if (!f.canRead()) System.err.println(printToString(v0) + " is not readable");
				FileInputStream fir = new FileInputStream(f);
				return readStream(fir, (int)f.length());
			}
			catch (FileNotFoundException e) {
			}
			return null;
		}
		else {
			String filename = "./" + aString(v0);
			try {
				return prim_urltobytes(new URL(new URL("file", "", 
						System.getProperty((pcp) ? "user.home" : "user.dir")
						+"/"), filename));
			}
			catch (MalformedURLException e) {}
			return null;
		}
	}
}
