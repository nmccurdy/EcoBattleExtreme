/******************************************************************
 * Copyright 2003 by the Massachusetts Institute of Technology.  
 * All rights reserved.
 *
 * Developed by Mitchel Resnick, Andrew Begel, Eric Klopfer, 
 * Michael Bolin, Molly Jones, Matthew Notowidigdo, Sebastian Ortiz,
 * Michael Mandel, Tim Garnett, Max Goldman, Julie Kane, 
 * Russell Zahniser, Weifang Sun, and Robert Tau. 
 *
 * Previous versions also developed by Bill Thies, Vanessa Colella, 
 * Brian Silverman, Monica Linden, Alice Yang, and Ankur Mehta.
 *
 * Developed at the Media Laboratory, MIT, Cambridge, Massachusetts,
 * with support from the National Science Foundation and the LEGO Group.
 *
 * Permission to use, copy, or modify this software and its documentation
 * for educational and research purposes only and without fee is hereby
 * granted, provided that this copyright notice and the original authors'
 * names appear on all copies and supporting documentation.  If
 * individual files are separated from this distribution directory
 * structure, this copyright notice must be included.  For any other uses
 * of this software, in original or modified form, including but not
 * limited to distribution in whole or in part, specific prior permission
 * must be obtained from MIT.  These programs shall not be used,
 * rewritten, or adapted as the basis of a commercial software or
 * hardware product without first obtaining appropriate licenses from
 * MIT.  MIT makes no representations about the suitability of this
 * software for any purpose.  It is provided "as is" without express or
 * implied warranty.
 *
 *******************************************************************/

package starlogoc;

import java.awt.Color;
import java.awt.Transparency;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.PackedColorModel;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Hashtable;

import utility.Utility;
//import java.awt.color.*;

public class Colors {

    static ColorModel cm, tcm;
    public static final int numcolors = 140;
    public static final int numprimarycolors = 14;
    static short[] colormap;
    static final int innercolorsbits = 5; 
    static final int innercolors = 1 << innercolorsbits;
    static final double innercolorsdouble = innercolors + 0.0;
    
    //Contains the color names
    static String[] colorStrings = null;
    
    public static final int BLACK = 0;
    public static final int WHITE = 9;
    public static final int GRAY = 5;
    public static final int RED = 15;
    public static final int ORANGE = 25;
    public static final int BROWN = 35;
    public static final int YELLOW = 45;
    public static final int GREEN = 55;
    public static final int LIME = 65;
    public static final int TURQUOISE = 75;
    public static final int CYAN = 85;
    public static final int SKY = 95;
    public static final int BLUE = 105;
    public static final int VIOLET = 115;
    public static final int MAGENTA = 125;
    public static final int PINK = 135;
    
    static Hashtable<String, Double> colornamehash;
    static Hashtable<Double, String> colornumhash;

    static boolean colornamehashsetup = false;

    /*
     * trailingZeroTable[i] is the number of trailing zero bits in the binary
     * representaion of i.
     */
    final static byte trailingZeroTable[] = {
      -25, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
	4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
	5, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
	4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
	6, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
	4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
	5, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
	4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
	7, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
	4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
	5, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
	4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
	6, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
	4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
	5, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
	4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0};

    /*Fills the hashtables colornamehash and colornumhash with the names of the
     *primary colors and their corresponding slnum values
     */
    
    static synchronized void setupColorNameHash() {
	if (!colornamehashsetup) {
	    colorStrings = new String[numprimarycolors];
	    colorStrings[0] = "gray";
	    colorStrings[1] = "red";
	    colorStrings[2] = "orange";
	    colorStrings[3] = "brown";
	    colorStrings[4] = "yellow";
	    colorStrings[5] = "green";
	    colorStrings[6] = "lime";
			colorStrings[7] = "turquoise";
	    colorStrings[8] = "cyan";
	    colorStrings[9] = "sky";
	    colorStrings[10] = "blue";
	    colorStrings[11] = "purple";
	    colorStrings[12] = "magenta";
	    colorStrings[13] = "pink";
	    assert(numprimarycolors == 14);

	    colornamehash = new Hashtable<String, Double>(numprimarycolors + 2);
	    colornumhash = new Hashtable<Double, String>(numprimarycolors + 2);
	    
	    colornamehash.put("black", new Double(0));
	    colornumhash.put(new Double(0), "black");
	    
	    colornamehash.put("white", new Double(9));
	    colornumhash.put(new Double(9), "white");
	    for(int i = 0; i < numprimarycolors; i++) {
		colornamehash.put(colorStrings[i], new Double((i * 10) + 5));
		colornumhash.put(new Double((i * 10) + 5), colorStrings[i]);
	    }
	    colornamehashsetup = true;
	}
    }

    /*
     * Returns the string value of a double or int, truncated to 3 decimal
     * places in the first instace
     * @param num the number to be truncated
     * @return its string representation
     */
    public static String doubleToString (double num) {
	if ((num - (int)num) == 0)
	    return Integer.toString((int)num);
	else
	    return Double.toString((double)((int)(1000*num))/1000);
    }

    /*
     * Finds the number value of a color name in the hash table
     * @param name the color's name
     * @return it's double value
     */
    public static int colorNameToNumber(String name) {
	if (colornamehash == null) { setupColorNameHash(); }
	Double d = colornamehash.get(name);
	if (d == null) return 0;
	return d.intValue();
    }

    /*
     * Finds a color's name based on its value in the hash table
     * @param col the color's double value
     * @return it's name
     */
    public static String colorNumToName(double col) {
	if (colornumhash == null) { setupColorNameHash(); }
	String s = colornumhash.get(new Double(Math.round(col)));
	if (s == null) return doubleToString(col);
	else return s;
    }

    /*
     * Takes a color, and extracts its RGBA values
     * @param c the color to be analyzed
     * @return a float array with its RGBA values (from 0 to 1)
     */
    public static float[] colorToFloatArray(Color c) {
	return new float[]{ ((float)c.getRed()) / 256.0f, 
			    ((float)c.getGreen()) / 256.0f, 
			    ((float)c.getBlue()) / 256.0f, 
			    1.0f };
    }

// 	GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
// 	GraphicsDevice[] gs = ge.getScreenDevices();
// 	for (int j = 0; j < gs.length; j++) { 
// 	    GraphicsDevice gd = gs[j];
// 	    GraphicsConfiguration[] gc = gd.getConfigurations();
// 	    for (int i=0; i < gc.length; i++) {
// 		System.out.println("Graphics config " + i + " is " + gc[i]);
// 		ColorModel model = gc[i].getColorModel();
// 		System.out.println("color model is " + model);
// 	    }
// 	}

	//TODO each of the arrays reds, greens, and blues has 140 elements

  static int[] reds = { 0, 28, 56, 85, 113, 142, 170, 199, 227, 255,
		  76, 115, 153, 191, 230, 255, 255, 255, 255, 255,
		  76, 115, 153, 191, 230, 255, 255, 255, 255, 255,
		  48, 72, 96, 120, 144, 160, 179, 198, 217, 236,
		  76, 115, 153, 191, 230, 255, 255, 255, 255, 255,
		  24, 36, 48, 60, 73, 81, 116, 151, 186, 221,
		  0, 0, 0, 0, 0, 0, 51, 102, 153, 204,
		  0, 0, 0, 0, 0, 0, 51, 102, 153, 204,
		  0, 0, 0, 0, 0, 0, 51, 102, 153, 204,
		  0, 0, 0, 0, 0, 0, 51, 102, 153, 204,
		  0, 0, 0, 0, 0, 0, 51, 102, 153, 204,
		  38, 57, 76, 95, 115, 127, 153, 179, 204, 230,
		  76, 115, 153, 191, 230, 255, 255, 255, 255, 255,
		  76, 115, 153, 191, 230, 255, 255, 255, 255, 255 };

  static int[] greens = {0, 28, 56, 85, 113, 142, 170, 199, 227, 255,
		 0, 0, 0, 0, 0, 0, 51, 102, 153, 204,
		 25, 38, 51, 63, 76, 85, 119, 153, 187, 221,
		 33, 50, 67, 84, 100, 112, 140, 169, 198, 227,
		 76, 115, 153, 191, 230, 255, 255, 255, 255, 255,
		 59, 88, 118, 148, 177, 197, 209, 220, 232, 244,
		 76, 115, 153, 191, 230, 255, 255, 255, 255, 255,
		 76, 115, 153, 191, 230, 255, 255, 255, 255, 255,
		 76, 115, 153, 191, 230, 255, 255, 255, 255, 255,
		 38, 57, 76, 95, 115, 127, 153, 179, 204, 230,
		 0, 0, 0, 0, 0, 0, 51, 102, 153, 204,
		 0, 0, 0, 0, 0, 0, 51, 102, 153, 204,
		 0, 0, 0, 0, 0, 0, 51, 102, 153, 204,
		 0, 0, 0, 0, 0, 0, 51, 102, 153, 204 };

  static int[] blues = {0, 28, 56, 85, 113, 142, 170, 199, 227, 255,
		0, 0, 0, 0, 0, 0, 51, 102, 153, 204,
		0, 0, 0, 0, 0, 0, 51, 102, 153, 204,
		24, 36, 48, 60, 72, 80, 115, 150, 185, 220,
		0, 0, 0, 0, 0, 0, 51, 102, 153, 204,
		0, 0, 0, 0, 0, 0, 51, 102, 153, 204,
		0, 0, 0, 0, 0, 0, 51, 102, 153, 204,
		59, 89, 118, 148, 178, 198, 209, 221, 232, 244,
		76, 115, 153, 191, 230, 255, 255, 255, 255, 255,
		76, 115, 153, 191, 230, 255, 255, 255, 255, 255,
		76, 115, 153, 191, 230, 255, 255, 255, 255, 255,
		76, 115, 153, 191, 230, 255, 255, 255, 255, 255,
		76, 115, 153, 191, 230, 255, 255, 255, 255, 255,
		38, 57, 76, 95, 115, 127, 153, 179, 204, 230 };

    static byte[] redbytes, greenbytes, bluebytes;

    static public Color[] colorarray;
    static public float[][] colors;
    
    static public int[] colorarrayints, colorarrayintsBufPC;//sm
    

   static {
      try {
	  setupColorArray();
	  colorarrayints = new int[numcolors << innercolorsbits];
	  setupColorArrayIntegers(colorarrayints, 
				  new int[]{8, 8, 8, 8},
				  new int[]{0xff0000, 0xff00, 0xff, 0xff000000},
				  true);
	  colormap = readMap();
	  setupColorNameHash();
	  tcm = new DirectColorModel(32, 0xFF0000, 0x00FF00, 0x0000FF, 0xFF000000);
	  cm =  (Utility.macosxp) ? tcm : new DirectColorModel(32, 0xFF0000, 0x00FF00, 0x0000FF);
      } catch (Exception e) { e.printStackTrace(); }  
  }

  static void setupColorArray() {
    colorarray = new Color[(numcolors << innercolorsbits) + 1];
    colorarray[4480] = colorarray[4479];
    colors = new float[(numcolors << innercolorsbits) + 1][3];

    redbytes = new byte[numcolors << innercolorsbits + 1];
    greenbytes = new byte[numcolors << innercolorsbits + 1];
    bluebytes = new byte[numcolors << innercolorsbits + 1];

    for (int color = 0; color < (numcolors / 10); color++) {
      for (int shade = 0; shade < 9; shade++) {
	int lowcolor = (color * 10) + shade;
	int highcolor = lowcolor + 1;

	//For each shade of each color, get the RGB components
	int lowred = reds[lowcolor], lowgreen = greens[lowcolor],
	    lowblue = blues[lowcolor];
	//Also look at the components of the shade above
	int highred = reds[highcolor], highgreen = greens[highcolor],
	  highblue = blues[highcolor];
	//Take the difference of the two shades (+ 1)
	int diffred = highred - lowred + 1, diffgreen = highgreen - lowgreen + 1,
	  diffblue = highblue - lowblue + 1;
	//Take that difference and divide by 32
	double incred = ((double)diffred) / innercolorsdouble;
	double incgreen = ((double)diffgreen) / innercolorsdouble;
	double incblue = ((double)diffblue) / innercolorsdouble;

	//For each hue of 32
	for(int hue = 0; hue < innercolors; hue++) {
	  //Take index = sl color num shifted by 5, fill those 5 spots with hue
	  int index = (lowcolor << innercolorsbits) + hue;
	  //Set red to sl color's red value, and add the increment to the next value,
	  //multiplied by hue s.t. the increment varies from 0 to its acual value
	  int red = (int)((double)lowred + (incred * (double)hue));
	  int green = (int)((double)lowgreen + (incgreen * (double)hue));
	  int blue = (int)((double)lowblue + (incblue * (double)hue));
	  //color array at index containing color, shade, and saturation to the proper color
	  colorarray[index] = new Color(red, green, blue);
	  
	  //Basically, there are 140 colors, with 32 gradations of 'hue' between them
	  
	  //The colors array contains the RGB values, in rows, of each colorarray column
	  colors[index][0] = ((float)red) / 256.0f;
	  colors[index][1] = ((float)green) / 256.0f;
	  colors[index][2] = ((float)blue) / 256.0f;
	  
	  //The above information is also stored in corresponding byte arrays
	  redbytes[index] = (byte)(red & 0xff);
	  greenbytes[index] = (byte)(green & 0xff);
	  bluebytes[index] = (byte)(blue & 0xff);
	}
      }

      //For each color, when shade = 9 the color goes to white
      int shade = 9;
      int lowcolor = (color * 10) + shade;
      //int highcolor = lowcolor + 1;
      int lowred = reds[lowcolor], lowgreen = greens[lowcolor],
	lowblue = blues[lowcolor];
      int highred = 255, highgreen = 255, highblue = 255;
      int diffred = highred - lowred + 1, diffgreen = highgreen - lowgreen + 1,
	diffblue = highblue - lowblue + 1;
      double incred = ((double)diffred) / innercolorsdouble;
      double incgreen = ((double)diffgreen) / innercolorsdouble;
      double incblue = ((double)diffblue) / innercolorsdouble;

      for(int hue = 0; hue < innercolors; hue++) {
	int index = (lowcolor << innercolorsbits) + hue;
	int red = (int)((double)lowred + (incred * (double)hue));
	int green = (int)((double)lowgreen + (incgreen * (double)hue));
	int blue = (int)((double)lowblue + (incblue * (double)hue));
	colorarray[index] = new Color(red, green, blue);
	
	colors[index][0] = ((float)red) / 256.0f;
	colors[index][1] = ((float)green) / 256.0f;
	colors[index][2] = ((float)blue) / 256.0f;
	  
	redbytes[index] = (byte)(red & 0xff);
	greenbytes[index] = (byte)(green & 0xff);
	bluebytes[index] = (byte)(blue & 0xff);
      }
    }
    
    //Extra space added below to prevent overflow
    redbytes[redbytes.length-1] = 0;
    greenbytes[greenbytes.length-1] = 0;
    bluebytes[bluebytes.length-1] = 0;
  }

    public static void setupColorArrayBufPC(PackedColorModel cm) {
	if (colorarrayintsBufPC == null) {
	    colorarrayintsBufPC = new int[numcolors << innercolorsbits];
	    setupColorArrayIntegers(colorarrayintsBufPC,
				    cm.getComponentSize(),
				    cm.getMasks(),
				    (cm.getTransparency() == 
				     Transparency.TRANSLUCENT));
	}
    }

    public static void setupColorArrayIntegers(int[] colorArrayOfInts, 
					       int[] colorBits, 
					       int[] colorMasks,
					       boolean addAlpha) {
	//	System.out.println("setup color array integers");
	int redBits = colorBits[0];
	int greenBits = colorBits[1];
	int blueBits = colorBits[2];
	//int alphaBits = (addAlpha) ? colorBits[3] : 0;
// 	System.out.println("colorbits: (r,g,b,a) = (" + redBits + ", " +
// 			   greenBits + ", " + blueBits + ", " + alphaBits + ")");

	int redMask = colorMasks[0];
	int greenMask = colorMasks[1];
	int blueMask = colorMasks[2];
	int alphaMask = (addAlpha) ? colorMasks[3] : 0;
// 	System.out.println("colormasks: (r,g,b,a) = (" + Integer.toString(redMask,16) + ", " +
// 			   Integer.toString(greenMask,16) + ", " + 
// 			   Integer.toString(blueMask,16) + ", " + 
// 			   Integer.toString(alphaMask,16) + ")");

	int redShift = trailingZeroCnt(redMask);
	int greenShift = trailingZeroCnt(greenMask);
	int blueShift = trailingZeroCnt(blueMask);
	int alphaShift = (addAlpha) ? trailingZeroCnt(alphaMask) : 0;
// 	System.out.println("colorshifts: (r,g,b,a) = (" + redShift + ", " +
// 			   greenShift + ", " + blueShift + ", " + alphaShift + ")");

	if (addAlpha) {
	    for(int i = 0; i < colorarray.length-1; i++) {
		Color col = colorarray[i];
		int red = col.getRed() >>> (8 - redBits);
		int green = col.getGreen() >>> (8 - greenBits);
		int blue = col.getBlue() >>> (8 - blueBits);
		colorArrayOfInts[i] = (0xff << alphaShift) | (red << redShift) 
		    | (green << greenShift) | (blue << blueShift);
	    }
	} else {
	    for(int i = 0; i < colorarray.length-1; i++) {
		Color col = colorarray[i];
		//System.out.println("orig color: " + col);
		int red = col.getRed() >>> (8 - redBits);
		int green = col.getGreen() >>> (8 - greenBits);
		int blue = col.getBlue() >>> (8 - blueBits);
		colorArrayOfInts[i] = (red << redShift) | (green << greenShift) |
		    (blue << blueShift);
		//System.out.println("new color: " + Integer.toString(colorArrayOfInts[i], 2));
	    }
	}
    }

  public static short[] readMap() {
    try {
      URL u = Colors.class.getResource("colormap.yuv.bin");
      InputStream is = null;
      try {
	is = u.openStream();
	DataInputStream dis = new DataInputStream(new BufferedInputStream(is, 4096));
	short[] colormap = new short[dis.readInt()];
	for(int i = 0; i < colormap.length; i++) {
	  colormap[i] = dis.readShort();
	}
	return colormap;
      }
      catch(IOException e) { e.printStackTrace(); }
    }
    catch(Exception f) { f.printStackTrace(); }
    return null;
  }


  public static void writeMap() {
    short[] colormap = createInverseColorMap();
    try {
      File f = new File("c:\\colormap.bin");
      DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f)));
      dos.writeInt(colormap.length);
      for(int i = 0; i < colormap.length; i++) {
	dos.writeShort(colormap[i]);
      }
      dos.flush();
      dos.close();
    }
    catch(IOException e) {
    	System.err.println("writeMap problem");
    }
  }

  public static short[] createInverseColorMap() {
    short[] colormap = new short[innercolors * 64 * 32];
    double[] stary, staru, starv;
    stary = new double[colorarrayints.length];
    staru = new double[colorarrayints.length];
    starv = new double[colorarrayints.length];

    for(int i = 0; i < colorarrayints.length; i++) {
      int color = colorarrayints[i];
      int sr = ((color >> 16) & 0xFF);
      int sg = ((color >> 8) & 0xFF);
      int sb = color & 0xFF;
      stary[i] = sr; //0.299 * sr + 0.587 * sg + 0.114 * sb;
      staru[i] = sg; //-0.147 * sr + 0.289 * sg + 0.436 * sb;
      starv[i] = sb; //0.615 * sr - 0.515 * sg - 0.1 * sb;
    }

    for(int r = 0; r < 255; r += 8) {
      for(int g = 0; g < 255; g += 4) {
	for(int b = 0; b < 255; b += 8) {
	  double y = r; //0.299 * r + 0.587 * g + 0.114 * b;
	  double u = g; //-0.147 * r + 0.289 * g + 0.436 * b;
	  double v = b; //0.615 * r - 0.515 * g - 0.1 * b;
	  double mindist = Double.MAX_VALUE;
	  int minelem = 0;
	  for(int i = 0; i < colorarrayints.length; i++) {
	    double sy = stary[i];
	    double su = staru[i];
	    double sv = starv[i];
	    double dy = sy - y, du = su - u, dv = sv - v;
	    double dist = dy * dy + du * du + dv * dv;
	    if (dist < mindist) {
	      mindist = dist;
	      minelem = i;
	    }
	  }
	  colormap[(((r >> 3) & 0x1F) << 11) |
		   (((g >> 2) & 0x3F) << 5) |
		  (((b >> 3) & 0x1F))] = (short)minelem;
	}
      }
    }
    return colormap;
  }

	//TODO Changed to handle white color cmcheng
  public static double mapColorToStarLogoColor(int rgb) {
    int r = (rgb >> 16) & 0xFF;
    int g = (rgb >> 8) & 0xFF;
    int b = rgb & 0xFF;
		
		//white
		if (r >= 250 && g >= 250 && b >= 250) {
			return 9.0;
		}
		
    int index = (((r >> 3) & 0x1F) << 11) |
		   (((g >> 2) & 0x3F) << 5) |
		  (((b >> 3) & 0x1F));
    double ret = (double)colormap[index] / innercolorsdouble;
    //assert ret >= 0.0 || ret < numcolors : "Color out of bounds."; 
    return ret;
  }

	//TODO Changed to handle white color cmcheng
    public static double mapColorToStarLogoColor(int r, int g, int b) {
		
		//white
		if (r >= 250 && g >= 250 && b >= 250) {
			return 9.0;
		}
		
    int index = (((r >> 3) & 0x1F) << 11) |
		   (((g >> 2) & 0x3F) << 5) |
		  (((b >> 3) & 0x1F));
    double ret = (double)colormap[index] / innercolorsdouble;
    //assert ret >= 0.0 || ret < numcolors : "Color out of bounds."; 
    return ret;
  }

   public static int trailingZeroCnt(int val) {
        // Loop unrolled for performance
        int byteVal = val & 0xff;
        if (byteVal != 0)
            return trailingZeroTable[byteVal];

        byteVal = (val >>> 8) & 0xff;
        if (byteVal != 0)
            return trailingZeroTable[byteVal] + 8;

        byteVal = (val >>> 16) & 0xff;
        if (byteVal != 0)
            return trailingZeroTable[byteVal] + 16;

        byteVal = (val >>> 24) & 0xff;
        return trailingZeroTable[byteVal] + 24;
    }

   /**
    * Convert an SL color number (e.g. 55 for green) to the equivalent Java Color
    */
   public static Color slColorToColor(double color) {
	   float[] colorVect =	colorarray[(int)(color*32)].getRGBComponents(null);
   		Color c = new Color(colorVect[0], colorVect[1], colorVect[2]);
   		return c;
   }

}





