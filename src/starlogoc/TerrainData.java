package starlogoc;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL;

import com.sun.opengl.util.texture.TextureData;

//IMPORTANT: Texture has
//0,0 in the upper left hand corner


public class TerrainData {
	/*
     each patch contains:
  	  slnum color; // SL Color of the patch
	  slnum *heap; // pointer to the heap where this patch's variables are stored
	  int texture; // the index of the texture of this patch, -1 for no texture
	  float corner_heights[4]; // NW, SW, SE, NE i.e. UL, LL, LR, UR 
	  Tex_coord tex_coords[2]; // NW, SE texture coordinates
		float x;
		float y;
	  Patch_side north_side; 
		slnum color; // SL Color of the side
		int texture; // index of the texture of this side, -1 for no texture
		float tex_x[2]; // horizontal texture coordinates (y values are calculated by renderer)
	  Patch_side west_side;
		slnum color; // SL Color of the side
		int texture; // index of the texture of this side, -1 for no texture
		float tex_x[2]; // horizontal texture coordinates (y values are calculated by renderer)
	 */
	
	TerrainDataPatch[] myPatches;
	/* 
	 * Note that the following offsets are measured in bytes 
	 */
//	public static final int COLOR_OFFSET = getColorOffset();
//	public static final int HEAP_OFFSET = getHeapOffset();
//	public static final int TEXTURE_OFFSET = getTextureOffset();
//	public static final int CORNER_HEIGHTS_OFFSET = getCornerHeightsOffset();
//	public static final int TEX_COORDS_OFFSET = getTexCoordsOffset();
//	public static final int NORTH_OFFSET = getNorthSideOffset();
//	public static final int WEST_OFFSET = getWestSideOffset();
//	public static final int SIDE_COLOR_OFFSET = getSideColorOffset();
//	public static final int SIDE_TEXTURE_OFFSET = getSideTextureOffset();
//	public static final int SIDE_TEX_X_COORDS_OFFSET = getSideTexXCoordsOffset();
//	public static final int PATCH_SIZE = getPatchSizeInBytes();

	public ByteBuffer patches;
	private int height, width;
	private int index;
	private float patchSize;
	private static StarLogo sl;
	private int numPatchesOwn;
	private LongBuffer patchHeap;
//	private List<Variable> varList = new ArrayList<Variable>();

	//TODO 
	/*
	 * Texture associated with this TerrainData
	 * 
	 * We keep texture as a BufferedImage: associated_bimg (associtaed buffered image)
	 * The size of this image is associated_width and associated_height, defining the resolution of this texture
	 * associated_name is the name of this texture that's stored in texturemanager
	 * 
	 * We keep a spare BufferedImage to use when we're highlighting in the drawing toolbar.
	 * Using_temp = true ==> we're using the temporary image to show when the user is highlighting.
	 * 
	 */
	private boolean using_temp;
	private int associated_width;
	private int associated_height;
	private String associated_name;
	private BufferedImage associated_bimg;
	private BufferedImage associated_bimg_temp;
	public static final String associated_stem = "associated_";
	
	private boolean texture_changed; // set to true when a change is made, so renderer can update the texture
	
	//Probably don't need these
	public static final int TEX_COLOR_TYPE = GL.GL_BGR;
	public static final int TEX_DATA_TYPE = GL.GL_3_BYTES;
	
	/*
	 * Native terrain functions
	 */
//	private static native int getPatchSizeInBytes();
//	private static native int getHeapOffset();
//	private static native int getColorOffset();
//	private static native int getTextureOffset();
//	private static native int getCornerHeightsOffset();
//	private static native int getTexCoordsOffset();
//	private static native int getNorthSideOffset();
//	private static native int getWestSideOffset();
//
//	private static native int getSideColorOffset();
//	private static native int getSideTextureOffset();
//	private static native int getSideTexXCoordsOffset();
//	
//	private native void initTerrain(int index, int width, int height, ByteBuffer patches);
//	private native void setPatchHeap(int index, int numPatchesOwn, LongBuffer patchHeap);
//    private native void clearPatchHeights(int index);
	
    public boolean anti_aliasing = false;
    
    public TerrainData(int index, int width, int height, StarLogo thesl,
    		int assoc_width, int assoc_height) {
    	this.index = index; // this terrain's index in the terrains array
		this.height = height;
		this.width = width;
		sl = thesl;
		patchSize = 3;
		
		myPatches = new TerrainDataPatch[width*height];
		for (int i=0; i < width*height; i++) {
			myPatches[i] = new TerrainDataPatch();
		}
//		patches = ByteBuffer.
//		allocateDirect(width * height * getPatchSizeInBytes()).
//		order(ByteOrder.nativeOrder());
		
//		initTerrain(index, width, height, patches);

		associated_width = assoc_width;
		associated_height = assoc_height;
		associated_bimg = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(512,512, BufferedImage.OPAQUE);
		associated_bimg_temp = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(512,512, BufferedImage.OPAQUE);
		
		//We create textures on the fly since SLTerrain depends on it.
		associated_name = associated_stem + this.index;
    }
    
	
	/**
	 * Copy constructor. Doesn't call the native C code. 
	 * @param td
	 */
	public TerrainData(TerrainData td) {
		this.index = td.index; 
		this.height = td.height; 
		this.width = td.width; 
		this.patchSize = td.patchSize; 

		myPatches = new TerrainDataPatch[this.width * this.height];
		for (int i=0; i < this.width*this.height; i++) {
			myPatches[i] = new TerrainDataPatch(td.myPatches[i]);
		}
		
//		int orig = td.patches.position();
//		td.patches.rewind();
//		this.patches = ByteBuffer.allocateDirect(this.width * this.height * PATCH_SIZE).order(ByteOrder.nativeOrder());
//		this.patches.put(td.patches); 
//		td.patches.position(orig); 
		//TODO My edit to this constructor
		//We can use the same texture (since we constantly write over it, but we need to create new
		//objects for the two BufferedImages and TextureData (maybe not)
		this.associated_width = td.getAssociatedWidth();
		this.associated_height = td.getAssociatedHeight();
		this.associated_name = td.getAssociatedName();
		
		//Copy the two bufferedimages
		this.associated_bimg = new BufferedImage(this.associated_width,this.associated_height,BufferedImage.TYPE_3BYTE_BGR);
		this.associated_bimg_temp = new BufferedImage(this.associated_width,this.associated_height,BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D g2d = this.associated_bimg.createGraphics();
		g2d.drawImage(td.get_bimg(), null, 0, 0);
		Graphics2D g2d_temp = this.associated_bimg_temp.createGraphics();
		g2d_temp.drawImage(td.get_bimg_temp(), null, 0, 0);
		
	}
	
	/**
	 * <em>Note:</em> this class does not support bounds checking.
	 * @return the number of patches
	 **/
	public int getNumPatches() {
		return width * height;
	}

	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}

	public float getPatchSize() {
		return patchSize;
	}
	
	public void setPatchSize(float newSize) {
		patchSize = newSize;
	}
	
//	private int getPatch(int patchX, int patchY) {
//		return (patchY * width + patchX)* PATCH_SIZE;
//	}

	private TerrainDataPatch getPatch(int x, int y) {
		return myPatches[y*width + x];
	}
	
	/**
	 * Use this method if you need to interact with starlogoc.Colors directly.
	 * @return the StarLogo color number of patch
	 **/
//	public double getColorNumber(int patchX, int patchY) {
//		return StarLogo.slnumToDouble(patches.getLong(getPatch(patchX, patchY)+COLOR_OFFSET));
//	}

	/**
	 * Use this method if you need to interact with slnum of the color directly.
	 * @return the StarLogo color number of patch
	 **/
//	public long getColorSLNUM(int patchX, int patchY) {
//		return patches.getLong(getPatch(patchX, patchY)+COLOR_OFFSET);
//	}
//TODO HERE
	/**
	 * Use this method if you only need the Color of patch.
	 * @return the Color of patch
	 **/
//	public Color getColor(int patchX, int patchY) {
//		return Colors.colorarray[(int)(getColorNumber
//			(patchX, patchY)*32.0)];
//	}

	public Color getColor(int x, int y) {
		return getPatch(x,y).color;
	}
	
	private static float[] colorVector = new float[]{0.0f, 0.0f, 0.0f};


//	public float[] getColorVector(int patchX, int patchY) {
//		int index = (int)(getColorNumber(patchX, patchY)*32.0);
//		colorVector = Colors.colors[index];
//		return colorVector;
//	}

	public void setColor(int patchX, int patchY, Color color) {
		getPatch(patchX, patchY).color = color;
	}

	/**
	 * @return the height of patch as the average of the corner heights
	 **/
	public float getHeight(int patchX, int patchY) {
		float heights[] = getPatch(patchX, patchY).heights;
		float height = 0;
		for (int i = 0; i < 4; i++) {
			height += heights[i];
		}
		height /= 4.0;
		return height;
	}
    
    public float getHeight(int patchX, int patchY, int corner)
    {	
        return getPatch(patchX, patchY).heights[corner];
    }

    public void setHeight(int patchX, int patchY, int corner, float height)
    {
    	getPatch(patchX, patchY).heights[corner] = height;
    }
    
//    public float[] getHeights(int patchX, int patchY) {
//        int base = getPatch(patchX, patchY) + CORNER_HEIGHTS_OFFSET;
//        patches.position(base);
//        float[] heights = {0,0,0,0};
//        patches.asFloatBuffer().get(heights);
//        return heights;
//    }

    public void getHeights(int patchX, int patchY, float[] heights) 
    {
       heights = getPatch(patchX, patchY).heights;
    }

	public void setHeight(int patchX, int patchY, float height) {
		float[] heights =  {height, height, height, height};
		getPatch(patchX, patchY).heights = heights;
	}
	
	/**
	 * 
	 * @param patchX
	 * @param patchY
	 * @param heights - the heights of the 4 corners in this order: NW, SW, SE, NE
	 */
	public void setHeights(int patchX, int patchY, float[] heights) {
		if (heights.length != 4)
			throw new RuntimeException("Bad array passed to setHeights. 4 elements expected.");
		getPatch(patchX, patchY).heights = heights;
	}

	public void addHeight(int patchX, int patchY, float height)
	{
		float[] heights = getPatch(patchX, patchY).heights;
		for (int i = 0; i < 4; i++) {
			heights[i] = heights[i] + height;
		}
		setHeights(patchX, patchY, heights);
	}
	
	public void addHeights(int patchX, int patchY, float[] heights) {
		float[] heights2 = getPatch(patchX, patchY).heights;
		for (int i = 0; i < 4; i++) {
			heights2[i] = heights2[i] + heights[i];
		}
		setHeights(patchX, patchY, heights2);		
	}
	
	
	//FIXME: WE SHOULD COMMENT THESE OUT, THESE DO NOTHING
	/**
	 * @return the texture of patch
	 **/
	public int getTexture(int patchX, int patchY) {
		return getPatch(patchX, patchY).texture;
	}

	public void setTexture(int patchX, int patchY, int texture) {
		getPatch(patchX, patchY).texture = texture;
	}
    
    /**
     * fills the fields of a TerrainDataPatch
     */
    
    public void getTerrainDataPatch(int patchX, int patchY, TerrainDataPatch p)
    {
    	p = getPatch(patchX, patchY);
    }
    
//    /** Replace oldVar with newVar, or do nothing if oldVar doesn't exist. */
//    public void renameVariable(Variable oldVar, Variable newVar) {
//        int index = varList.indexOf(oldVar);
//        if (index != -1)
//            varList.set(index, newVar);
//    }

//	public void reallocateVariables(List<Variable> newVarList) {
//		// Shortcircuit if no changes have been made
//		if (varList.equals(newVarList))
//			return;
//	
//		LongBuffer newPatchHeap = ByteBuffer.
//			allocateDirect(getNumPatches() * newVarList.size() * 8).
//			order(ByteOrder.nativeOrder()).
//			asLongBuffer();
//
//		// Generate a list that maps new var positions to old positions.
//		// -1 signals that this is a new variable that will be initialized to 0.
//		List<Integer> positions = new ArrayList<Integer>(newVarList.size()); // Indexes new positions to old positions
//		for (int newPosition = 0; newPosition < newVarList.size(); newPosition++)
//			positions.add(new Integer(varList.indexOf(newVarList.get(newPosition))));
//	
//		int oldPosition;
//		for (int i = 0; i < getNumPatches(); i++) {
//			for (int newPosition = 0; newPosition < positions.size(); newPosition++) {
//				oldPosition = positions.get(newPosition).intValue();
//				if (oldPosition == -1) {
//					//System.out.println("new variable at pos: " + newPosition);
//					newPatchHeap.put(0);
//				} 
//				else {
//					//System.out.println("oldvar: " + oldPosition + " to newvar: " + newPosition);
//					newPatchHeap.put(patchHeap.get(i * numPatchesOwn + oldPosition));
//				}
//			}
//		}
//	
//		varList = new ArrayList<Variable>(newVarList);
//		numPatchesOwn = newVarList.size();
//		patchHeap = newPatchHeap;
//		setPatchHeap(index, numPatchesOwn, patchHeap);
//	}
	public void clearPatches() {
//NJM
		// do we need to clear patch heights?
		//		synchronized(sl.getLock()) {
//		    clearPatchHeights(index); //reset to 0 height
//		}
		// new format: color patches white, then paint green on the texture
		for (int x = 0; x < this.width; x++) {
			for (int y = 0; y < this.height; y++) {
				setColor(x, y, Color.WHITE);
			}
		}
		fillRectangle(0, 0, getAssociatedWidth(), getAssociatedHeight(), Colors.slColorToColor(Colors.GREEN));
	}

	public void scatterPC(double red, double green, double blue, double black) {
		// set up value ranges to make choosing a random color depending on the
		// weights provided easy
		double sum = red + green + blue + black;
		double randomNumber;
		green = red + green;
		blue = green + blue;
		
		// loop through each patch, choosing a (weighted) random color for it
		for (int x = 0; x < this.width; x++) {
			for (int y = 0; y < this.height; y++) {
				randomNumber = Math.random()*sum;
				if (randomNumber < red) {
					paintPatch(x, y, Colors.slColorToColor(Colors.RED));
				} else if (randomNumber < green) {
					paintPatch(x, y, Colors.slColorToColor(Colors.GREEN));
				} else if (randomNumber < blue) {
					paintPatch(x, y, Colors.slColorToColor(Colors.BLUE));
				} else {
					paintPatch(x, y, Colors.slColorToColor(Colors.BLACK));
				}
			}
		}		
	}

	
	//TODO Getters and setters for Associated Texture
	public TextureData getAssociated() {
		if (using_temp == true) {
			return new TextureData(GL.GL_RGB, GL.GL_RGB, true, associated_bimg_temp);
		}
		else {
			return new TextureData(GL.GL_RGB, GL.GL_RGB, true, associated_bimg);
		}
	}
	
	public int getAssociatedWidth() {
		return associated_width;
	}
	
	public int getAssociatedHeight() {
		return associated_height;
	}
	
	//Returns the name of the associated texture
	public String getAssociatedName() {
		return associated_name;
	}
	
	//To stop using the highlighted image, set using_temp = false
	public void unhighlight() {
		using_temp = false;
		texture_changed = true;
	}
	
	protected BufferedImage get_bimg() {
		return associated_bimg;
	}
	
	protected BufferedImage get_bimg_temp() {
		return associated_bimg_temp;
	}
	
	public void turnOnAntiAliasing(Graphics2D g2d) {
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
	}
	
	//TODO Highlighting methods
	/**
	 * Highlights the given rectangle specified by points (x1, y1) (x2, y2)
	 */
	public void highlightRectangle(int x1, int y1, int x2, int y2, Color c) {
		//Copy the what we have in the image to our temp image.
		Graphics2D g2d = associated_bimg_temp.createGraphics();
		//g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.drawImage(associated_bimg, null, 0, 0);
		
		int xmin = Math.min(x1, x2);
		int xmax = Math.max(x1, x2);
		int ymin = Math.min(y1, y2);
		int ymax = Math.max(y1, y2);
		
		//Set the color
		g2d.setColor(c);
		
		//Draw the the highlighted rectangle.
		g2d.fillRect(xmin, ymin, xmax - xmin, ymax - ymin);
		
		//Set the associated texturedata as the one with the temp info
		using_temp = true;
		
		// flag that renderer needs to update the texture
		texture_changed = true;
	}
	
	public void highlightImage(int x1, int y1, int x2, int y2, Image img) {
		//Copy the what we have in the image to our temp image.
		Graphics2D g2d = associated_bimg_temp.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.drawImage(associated_bimg, null, 0, 0);

		g2d.drawImage(img, x1, y1, x2 - x1, y2 - y1, null);
		
		//Set the associated texturedata as the one with the temp info
		using_temp = true;
		
		// flag that renderer needs to update the texture
		texture_changed = true;
	}

	
	public void highlightEllipse(int x1, int y1, int x2, int y2, Color c) {
		//Copy the what we have in the image to our temp image.
		Graphics2D g2d = associated_bimg_temp.createGraphics();
		//g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.drawImage(associated_bimg, null, 0, 0);

		int xmin = Math.min(x1, x2);
		int xmax = Math.max(x1, x2);
		int ymin = Math.min(y1, y2);
		int ymax = Math.max(y1, y2);
		
		//Set the color
		g2d.setColor(c);
		
		//Draw the the highlighted rectangle.
		g2d.fillOval(xmin, ymin, xmax - xmin, ymax - ymin);
		
		//Set the associated texturedata as the one with the temp info
		using_temp = true;
		
		// flag that renderer needs to update the texture
		texture_changed = true;
	}
	
	public void highlightPolygonLines(List<Integer> xList, List<Integer> yList, Color c) {
		Graphics2D g2d = associated_bimg_temp.createGraphics();
		//g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.drawImage(associated_bimg, null, 0, 0);
		g2d.setColor(c);
		
		for (int i = 0; i < xList.size() - 1; i++) {
			g2d.drawLine(xList.get(i), yList.get(i), xList.get(i+1), yList.get(i+1));
		}
		
		//Update our texturedata to reflect what is in our image
		using_temp = true;
		
		// flag that renderer needs to update the texture
		texture_changed = true;
	}	
	
	
	
	//TODO Fill Methods
	//Draws a filled rectangle at the specified coordinates
	public void fillRectangle(int x1, int y1, int x2, int y2, Color c) {
		//Draw our filled rectangle onto our image
		Graphics2D g2d = associated_bimg.createGraphics();
		
		int xmin = Math.min(x1, x2);
		int xmax = Math.max(x1, x2);
		int ymin = Math.min(y1, y2);
		int ymax = Math.max(y1, y2);
		
		//Set the color
		g2d.setColor(c);
		
		//Draw the the highlighted rectangle.
		g2d.fillRect(xmin, ymin, xmax - xmin, ymax - ymin);
		
		//Update our texturedata to reflect what is in our image
		using_temp = false;

		// flag that renderer needs to update the texture
		texture_changed = true;
	}
	
	public void fillEllipse(int x1, int y1, int x2, int y2, Color c) {
    	
		//Draw our filled rectangle onto our image
		Graphics2D g2d = associated_bimg.createGraphics();
		
		int xmin = Math.min(x1, x2);
		int xmax = Math.max(x1, x2);
		int ymin = Math.min(y1, y2);
		int ymax = Math.max(y1, y2);
		
		//Set the color
		g2d.setColor(c);
		
		//Draw the the highlighted oval.
		g2d.fillOval(xmin, ymin, xmax - xmin, ymax - ymin);

		//Update our texturedata to reflect what is in our image
		using_temp = false;

		// flag that renderer needs to update the texture
		texture_changed = true;
	}
	
	/**
	 * Takes in an image, scales it to fit the rect by x1,x2,y1,y2 and paints it in
	 */
	public void fillImage(int x1, int y1, int x2, int y2, Image img) {
		// djwendel - g2d will automatically flip image if x1 > x2, etc.
		// so just using the coordinates passed in gives the user
		// control over the orientation of the image.
		int xmin = x1;//Math.min(x1, x2);
		int xmax = x2;//Math.max(x1, x2);
		int ymin = y1;//Math.min(y1, y2);
		int ymax = y2;//Math.max(y1, y2);
		
		//I thought we needed this, although graphics2d will actually auto scale..
		//Image temp = img.getScaledInstance(xmax - xmin, ymax - ymin, Image.SCALE_SMOOTH);
		
		Graphics2D g2d = associated_bimg.createGraphics();
		g2d.drawImage(img, xmin, ymin, xmax - xmin, ymax - ymin, null);
		
		//Update our texturedata to reflect what is in our image
		using_temp = false;

		// flag that renderer needs to update the texture
		texture_changed = true;
	}
	
	public void fillPolygon(List<Integer> xList, List<Integer> yList, Color c) {
		Graphics2D g2d = associated_bimg.createGraphics();
		g2d.setColor(c);
		
		int[] xPoints = new int[xList.size()];
		int[] yPoints = new int[yList.size()];
		
		for (int i = 0; i < xList.size(); i++) {
			xPoints[i] = xList.get(i).intValue();
			yPoints[i] = yList.get(i).intValue();
		}
		
		g2d.fillPolygon(xPoints, yPoints, xPoints.length);
		g2d.drawPolygon(xPoints, yPoints, xPoints.length);
		
		//Update our texturedata to reflect what is in our image
		using_temp = false;
		
		// flag that renderer needs to update the texture
		texture_changed = true;
	}

	public void drawThickLine(int x1, int y1, int x2, int y2, Color c, int thickness) {
		Graphics2D g2d = associated_bimg.createGraphics();
		g2d.setColor(c);

        g2d.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
        g2d.drawLine(x1,y1,x2,y2);   //thick

        using_temp = false;
        
		// flag that renderer needs to update the texture
		texture_changed = true;
	}
	
	/**
	 * Gets a pixel from the bufferedimage
	 * Returns in bufferedimage format
	 */
	public int getImagePixel(int x, int y) {
		return associated_bimg.getRGB(x, y);
	}
	
	/**
	 * Sets a pixel to the bufferedimage (not the temp image (for highlighting))
	 * @param x
	 * @param y
	 * @param rgb
	 */
	public void setImagePixel(int x, int y, int rgb) {
		associated_bimg.setRGB(x, y, rgb);

		// flag that renderer needs to update the texture
		texture_changed = true;
	}
	
	/**
	 * Takes two coordinates as would be specified by the user when manipulating blocks (-50,50)
	 * Then returns the color at the point in the texture
	 */
	public Color getTextureColor(int x, int y) {
		
		Color c = new Color(associated_bimg.getRGB(x, y), false);
		return c;
		
	}
	
	
	
	public boolean xInsideTexture(int x) {
		return (x >= 0 && x <= associated_width);
	}
	
	public boolean yInsideTexture(int y) {
		return (y >= 0 && y <= associated_height);
	}
	
	/**
	 * Method to paint the area of a patch on the texture.
	 * Takes in a color to paint as well
	 */
	public void paintPatch(int patchX, int patchY, Color c) {
		//Patches go from 0 to width - 1 for x, and 0 to height - 1 for y
		//Buffered image goes from 0 to associated_width - 1 for x
		
		//scale ratio: associated_width/width = r
		//x paints from: x*r to (x+1)*r
		//y paints from: y*r to (y+1)*r
		double r = (double)associated_width/(double)width;
		
		this.fillRectangle((int)(patchX*r), (int)(patchY*r), 
				(int)((patchX + 1)*r), (int)((patchY + 1)*r), c);

		// flag that renderer needs to update the texture
		texture_changed = true;
	}
	
	/**
	 * @return true if the texture has been changed
	 */
	public boolean textureChanged() {
		return texture_changed;
	}
	
	/**
	 * Raises or clears the textureChanged flag
	 * @param changed - the new state to set textureChanged to
	 */
	public void setTextureChanged(boolean changed) {
		texture_changed = changed;
	}
	
}







