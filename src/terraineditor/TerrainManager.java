package terraineditor;

//import java.awt.*;
import java.awt.Color;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import starlogoc.*;
import torusworld.SLTerrain;
import utility.Tokenizer;

//TODO My imports 
import java.awt.Image;

public class TerrainManager
{
	private PatchManager pManager;
	public ArrayList<TurtlePlaceHolder> turtles = new ArrayList<TurtlePlaceHolder>();
	private double curColor = 55;
	private TerrainData td;
	
	public TerrainManager(PatchManager pm) {
		pManager = pm;
		pManager = pm;
		td = pm.getCurrentTerrain();
		
//		pManager.addListener(new PatchManagerListener() {
//			public void terrainChanged(int oldIndex) {
//				td = pManager.getCurrentTerrain(); 
//			}
//			
//			public void terrainCreated(int index) { }
//			public void terrainDeleted(String name, int index) { }
//			public void terrainRenamed(int index) { }
//		});
	}
	

	// **************************************************************************
	//  HEIGHTS
	// **************************************************************************

	public void setRegionHeight(Region r, float height, boolean colorPatches)
	{
		for (int x = r.minX; x <= r.maxX; x++) {
			for (int y = r.minY; y <= r.maxY; y++) {
				td.setHeight(x, y, height);
//                if (colorPatches) {
//                    td.setColor(x, y, curColor);
//                }
			}
		}
	}

	public void addRegionHeight(Region r, float height, boolean colorPatches)
	{
		for (int x = r.minX; x <= r.maxX; x++) {
			for (int y = r.minY; y <= r.maxY; y++) {
				td.addHeight(x, y, height);
//                if (colorPatches) {
//				  td.setColor(x, y, curColor);
//                }
			}
		}
	}
	
	public float getHeight(int patchX, int patchY) {
		return td.getHeight(patchX, patchY);
	}
	
	/**
	 * Creates a mound with the specified height at the center, then sloping
	 * along a cosine curve down to ground level.  Note that no vertex is
	 * guaranteed to be the specified height if the center of the mound
	 * is between vertices.
	 * 
	 * The algorithm assigns height by scaling a positive-ized cosine curve to have period
	 * = 2*[the radius of the circle enclosed by the bounding box].
	 * This way, the value goes from 1 at the center (currentRadius = 0)
	 * to 0 at a distance of radius away (currentRadius = radius).
	 * 
	 * Stretched mounds are handled by squishing this circular mound in the x direction
	 * by a factor of xScale.
	 */
	public void mound(Region r, float height, boolean colorPatches)
	{
		// circle parameters
		double centerY = (double)(r.minY + r.maxY+1)/2; //center in Y dimension
		double centerX = (double)(r.minX + r.maxX+1)/2; //center in X dimension
		double radius = centerY-r.minY;    //radius in Y dimension
		double xScale = (double)(r.maxY+1-r.minY)/(r.maxX+1-r.minX); //scale x values by this to make a square coord. system

		// for explanation of slope algorithm, see method comments above.
		double heightMultiplier = 0;
		double currentRadius = 0;
		boolean paintThisPatch = false;
		float[] heights = new float[4];
		int x2, y2;
		for (int x = r.minX; x <= r.maxX; x++) {
			for (int y = r.minY; y <= r.maxY; y++) {
				// skip offscreen vertices
				if (x < 0 || x >= td.getWidth() || y < 0 || y >= td.getHeight()) {
					continue;
				}
				paintThisPatch = false;
				for (int i = 0; i < 4; i++) {
					// raise vertices appropriately
					x2 = x;
					y2 = y;
					switch (i) {
					case 0: // NW
						break;
					case 1: // SW
						y2++;
						break;
					case 2: // SE
						y2++;
						x2++;
						break;
					case 3: // NE
						x2++;
						break;
					}
					currentRadius = Math.sqrt(((centerX-x2)*xScale)*((centerX-x2)*xScale)+(centerY-y2)*(centerY-y2));
					// skip vertices outside the circular region
					if (currentRadius > radius) {
						heights[i] = 0;
						continue; 
					} 
					heightMultiplier = (Math.cos(Math.PI*currentRadius/radius)+1)/2; //have to say (cos+1)/2 to get positive values between 0 and 1
					heights[i] = (float) (height*heightMultiplier);
					paintThisPatch = true;
				}
				td.addHeights(x, y, heights);
//				if (paintThisPatch && colorPatches) {
//					td.setColor(x, y, curColor);
//				}
			}
		}
	}
	
	public void pit(Region r, float height, boolean colorPatches)
	{
		mound(r, -height, colorPatches); //just make a negative mound
	}
	
	/**
	 * Changes a level -- this is here purely for backwards
	 * compatability and should be refactored out when necessary.
	 */
    public void setPatchTerrain(int newTerrainIndex) {
		if (pManager != null) {
		    pManager.setPatchTerrain(newTerrainIndex);
		    td = pManager.getTerrain(1-newTerrainIndex);
		    pManager.editorModifiedFlag = true;
		} else {
		    System.out.println("pManager is null!");
		}
    }
    
    /**
     * Changes the level -- called by SLLoadLevelComponent
     */
    public void changePatchTerrain(int newTerrainIndex) {
		if (pManager != null) {
		    pManager.setPatchTerrain(newTerrainIndex);
		    td = pManager.getTerrain(newTerrainIndex);
		    pManager.editorModifiedFlag = true;
		} 
    }


	// **************************************************************************
	//  TURTLES
	// **************************************************************************

	// add a turtle to this terrain
	public void addTurtle(int x, int y, String breed)
	{
		turtles.add(new TurtlePlaceHolder(x,y,breed));
	}

	public class TurtlePlaceHolder
	{
		public int x;
		public int y;
		public String breed;
		public TurtlePlaceHolder(int x, int y, String breed)
		{
			this.x = x;
			this.y = y;
			this.breed = breed;
		}
	}

	// **************************************************************************
	//  COLORS
	// **************************************************************************

	/**
	 * set color of given patch (requires StarLogoColor number cast as a double)
	 */
	private void setColor(int patchX, int patchY, double SLColor)
	{
//		if (patchX >=0 && patchY >= 0 && patchX < td.getWidth() && patchY < td.getHeight()) {
//			td.setColor(patchX, patchY, SLColor);
//		}
	}

	/**
	 * returns the SLColor of the given patch
	 */
	public double getColorSLC(int patchX, int patchY)
	{
//	    return td.getColorNumber(patchX, patchY);
		return 0;
	}

	/**
	 * returns the (actual Java) Color of the given patch
	 */
	public Color getColor(int patchX, int patchY)
	{
	    return Colors.colorarray[(int)(getColorSLC(patchX, patchY)*32.0)];
	}

	/**
	 * set the color of the given region
	 * (color passed as SLColor)
	 */
	public void setRegionColor(Region r, int c)
	{
		// convert to double once 
		double SLDouble = (double) c;

		// and set the region to that color
		setRegionColor(r, SLDouble);
	}

	/**
	 * set the color of the given region
	 * (color passed as a double-ized StarLogoColor)
	 */
	public void setRegionColor(Region r, double SLColor)
	{
		for (int x = r.minX; x <= r.maxX; x++) {
			for (int y = r.minY; y <= r.maxY; y++) {
				setColor(x, y, SLColor);
			}
		}
	}
	
	/**
	 * set the TerrainManager's current color
	 * @param SLColor - the int SLNUM representing the color
	 */
	public void setCurColor(int SLColor) {
		curColor = (double) SLColor;
	}

	// **************************************************************************
	//  TEXTURES
	// **************************************************************************

	private void setTexture(int x, int y, int subx, int suby, int texNum)
	{
	    //pManager.opengl_patch_texture_terrains[terrainIndex].put(getTextureArrayIndex(x,y,subx,suby), texNum);
	}

	public void setRegionTexture(Region r, int texNum)
	{
		// set up boundary values
		int subX1 = r.minX*3, subX2 = r.maxX*3+2; //sub-patch bounding box coordinates
		int subY1 = r.minY*3, subY2 = r.maxY*3+2; //sub-patch bounding box coordinates
		
		// set textures
		for (int x = subX1; x <= subX2; x++) {
			for (int y = subY1; y <= subY2; y++) {
				setTexture(x/3, y/3, x%3, y%3, texNum); //convert back to subdivided patch coords
			}
		}
	}

	public int getTexNum(int x, int y, int subx, int suby)
	{
	    return 0;//pManager.opengl_patch_texture_terrains[terrainIndex].get(getTextureArrayIndex(x,y,subx,suby));
	}


	// **************************************************************************
	//  SAVING & LOADING
	// **************************************************************************

	/**
	 * MAD -- this isn't necessary and should be refactored out
	 */
	public void save(File file) { }

	/**
	 * MAD -- this isn't really necessary anymore and needs to
	 * be refactored out
	 */
	public void load(File file) { }

	// this save method is called when the user wants to save the project
	public String save() {
		try {
			String data = "`terrains`\r\nversion 3\r\n";
			// save the active terrain index for backwards compatibility (this can be phased out)
			data += "edit-terrain-index " + ((pManager.getCurrentTerrainIndex()+1)%2) + "\r\n";
			
			// save all exisiting terrains
			for (int i = 0; i < pManager.getNumTerrains(); i++) {
				// if terrain doesn't exist, move on to the next one
				if (pManager.getTerrain(i) == null)
					continue;
				
				// get the terrain data object
				TerrainData tdata = pManager.getTerrain(i);
				TerrainData tdatacopy = pManager.getTerrainCopy(i);

				// get data string with heights info
				String heightsString = TerrainFileFormatUtils.getHeightsString(tdata);
				String heightsStringCopy = TerrainFileFormatUtils.getHeightsString(tdatacopy);
				
				// get data string with colors info
				String colorsString = TerrainFileFormatUtils.getColorsString(tdata);
				String colorsStringCopy = TerrainFileFormatUtils.getColorsString(tdatacopy);
				
				// get data string with camera info
				String cameraString = TerrainFileFormatUtils.getCamerasString();				
				
				// add this terrain as an entry in the string
 				data += "terrain " + i + "\r\n";
 				data += "dimensions " + td.getWidth() + " " + td.getHeight() + "\r\n"; 			
				data += "name " + TerrainFileFormatUtils.getGzippedString(pManager.getTerrainName(i)) + "\r\n"; 				 
				
				//TODO We need to define texture resolution. We're not going to use the name of the texture.
				data += "texture_resolution " + td.getAssociatedWidth() + " " + td.getAssociatedHeight() + "\r\n";
				
 				// add string-ized properties here
 				data += "terrain_property-heights \r\n";
				data += heightsString + "\r\n";
 				data += "terrain_property-colors \r\n";
				data += colorsString + "\r\n";
				data += "terrain_property_copy-heights \r\n";
				data += heightsStringCopy + "\r\n";
 				data += "terrain_property_copy-colors \r\n";
				data += colorsStringCopy + "\r\n";
				
				//TODO Adding the texture data to the data string as a TerrainData property
				String textureString = TerrainFileFormatUtils.getTextureString(tdata);
				String textureStringCopy = TerrainFileFormatUtils.getTextureString(tdatacopy);
				data += "terrain_property-texture \r\n";
				data += textureString + "\r\n";
				data += "terrain_property_copy-texture \r\n";
				data += textureStringCopy + "\r\n";
				
				// add camera position data
				data += "terrain_property-cameras \r\n";
				data += cameraString + "\r\n";		
				
				// mark end of this terrain entry
				data += "end-terrain\r\n";
			}
			// mark with the end of terrain section marker so we
			// can put more information below the terrain data
			data += "`"; 
			return data;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean load(String data) {
		System.out.println("LOADING from TerrainManager");
		String title = "`terrains`";
		int start = data.indexOf(title);
		if (start < 0)
			return false;
		int end = data.indexOf("`", start + title.length());
		if (end < 0)
			end = data.length();
		data = data.substring(start + title.length(), end);		
		Tokenizer tokenizer = new Tokenizer(data);
		if (!tokenizer.getNextToken().equals("version"))
			return false;
		
		String version = tokenizer.getNextToken();
//		pManager.clearAllLevels(); 
		System.out.println("Version: " + version);
		// code to load old terrain format and convert to new format
		if (version.equals("0")) {
			try {
				tokenizer.getNextToken(); // "edit-terrain-index"
				tokenizer.getNextToken(); // "index"
	
				// load terrain to be edited and terrain in torus world window
				for (int i = 0; i < 2; i++) {
					// parse data string into terrain and color data
					tokenizer.getNextToken(); // "terrain"
					int tIndex = Integer.parseInt(tokenizer.getNextToken());
					
					//TODO Need to tell patch managers about old terrains before creating them
					pManager.setUsingOldFormat(true);
					setOldColoringPreferences();
					
					// create a new terrain of the proper size to load the data into
					TerrainData tdata = pManager.getTerrain(tIndex);
					if (tdata == pManager.getCurrentTerrain()) {
						pManager.createTerrain(tIndex, 101, 101);
						pManager.setPatchTerrain(tIndex);
						pManager.editorModifiedFlag = true;
						tdata = pManager.getCurrentTerrain();
					}
					else {
						pManager.createTerrain(tIndex, 101, 101);
						tdata = pManager.getTerrain(tIndex);
					}
					
					// parse file string to get heights data string
					StringBuffer heightsDataSB = new StringBuffer();
					String t = tokenizer.getNextToken();
					while (!t.equals("color")) {
						heightsDataSB.append(t);
						t = tokenizer.getNextToken();
					}
					String heightsData = heightsDataSB.toString();
					
					// parse file string to get colors data string
					StringBuffer colorDataSB = new StringBuffer();
					String c = tokenizer.getNextToken();
					while (!c.equals("end-terrain")) {
						colorDataSB.append(c);
						c = tokenizer.getNextToken();
					}
					String colorData = colorDataSB.toString();
					
					TerrainFileFormatUtils.setHeightsFromStringVersion0(heightsData, tdata);
					TerrainFileFormatUtils.setColorsFromStringVersion0(colorData, tdata);
					
					// save terrain snapshot for rolling back changes
					pManager.saveTerrainSnapshot(pManager.getTerrainName(tIndex));
				}
				return true;
			}
			catch (Exception e) {
				System.out.println("Failed to load terrain format 0");
				e.printStackTrace();
			}
		}
		// load using the newer old terrain format
		else if (version.equals("1")) {
			try {
				tokenizer.getNextToken(); // "edit-terrain-index"
				tokenizer.getNextToken(); // index
	
				// load terrain to be edited and terrain in torus world window
				String token = tokenizer.getNextToken();
				while (token != null && token.equals("terrain")) {
					// parse data string into terrain data
					int tIndex = Integer.parseInt(tokenizer.getNextToken());

					//TODO Need to tell patch managers about old terrains before creating them
					pManager.setUsingOldFormat(true);
					setOldColoringPreferences();
					
					// create a new terrain of the proper size to load data into
					TerrainData tdata = pManager.getTerrain(tIndex);
					if (tdata == pManager.getCurrentTerrain()) {
						pManager.createTerrain(tIndex, 101, 101);
						pManager.setPatchTerrain(tIndex);
						pManager.editorModifiedFlag = true;
						tdata = pManager.getCurrentTerrain();
					}
					else {
						pManager.createTerrain(tIndex, 101, 101);
						tdata = pManager.getTerrain(tIndex);
					}
					
					// parse file string to get terrain data string
					StringBuffer terrainDataSB = new StringBuffer();
					String t = tokenizer.getNextToken();
					while (!t.equals("end-terrain")) {
						terrainDataSB.append(t);
						t = tokenizer.getNextToken();
					}
					String terrainData = terrainDataSB.toString();
					
					// fill in all the saved data to the new terrain
					TerrainFileFormatUtils.setTerrainFromString(terrainData, tdata);
					
					// save terrain snapshot for rolling back changes
					pManager.saveTerrainSnapshot(pManager.getTerrainName(tIndex));
					
					// move on to the next terrain if there is one
					token = tokenizer.getNextToken();
				}
				return true;
			}
			catch (Exception e) {
				System.out.println("Failed to load terrain format 1");
				e.printStackTrace();
			}
		}
		// load using the newest terrain format
		else if (version.equals("2")) {
			try {
				String token = tokenizer.getNextToken(); // may or may not be "edit-terrain-index"
				if(token.equals("edit-terrain-index")) {
					tokenizer.getNextToken();
					token = tokenizer.getNextToken();
				}	
				// load terrains - keep going as long as there's another terrain to load				
				while (token != null && token.equals("terrain")) {
					
					/* parse data string into terrain data for this terrain */
					
					// first get this terrain's index
					int tIndex = Integer.parseInt(tokenizer.getNextToken());

					// next should be dimensions
					assert (tokenizer.getNextToken().contains("dimensions"));
					int twidth = Integer.parseInt(tokenizer.getNextToken());
					int theight = Integer.parseInt(tokenizer.getNextToken());
					
					boolean nameParsed = false;
					String name = ""; 
					
					// last may or may not be name
					token = tokenizer.getNextToken(); 
					if(token.equals("name")) {
						name = TerrainFileFormatUtils.getGunzippedString(tokenizer.getNextToken()); 
						nameParsed = true; 
					}
					
					// create a new terrain of the proper size to load data into
					//TODO Need to tell patch managers about old terrains before creating them
					pManager.setUsingOldFormat(true);
					setOldColoringPreferences();
					
					TerrainData tdata, tdatacopy; 
					boolean backupCreated = false; 
					if(nameParsed) {
						pManager.createTerrain(tIndex, name, twidth, theight);
						token = tokenizer.getNextToken();
					}
					else 
						pManager.createTerrain(tIndex, twidth, theight);
					
					tdata = pManager.getTerrain(tIndex);
					tdatacopy = pManager.getTerrainCopy(tIndex);
					
					// now parse the remainder of the string for an 
					// unknown number of properies							
					while (!token.equals("end-terrain")) {
						// token must be a property name, so save it...
						String propertyName = token;
						// get associated data...
						StringBuffer propertyDataSB = new StringBuffer();
						token = tokenizer.getNextToken();
						while (!token.equals("end-terrain") && !token.contains("terrain_property")) {
							propertyDataSB.append(token);
							token = tokenizer.getNextToken();
						}
						String propertyDataString = propertyDataSB.toString();
						
						// and set that data in the terrain:
						if(propertyName.contains("terrain_property_copy")) {
							TerrainFileFormatUtils.setPropertyFromString(propertyName, propertyDataString, tdatacopy);
							backupCreated = true; 
						}
						else
							TerrainFileFormatUtils.setPropertyFromString(propertyName, propertyDataString, tdata);
					}
					// save terrain snapshot for rolling back changes
					if(backupCreated)
						pManager.saveTerrainSnapshot(pManager.getTerrainName(tIndex), tdatacopy);
					else
						pManager.saveTerrainSnapshot(pManager.getTerrainName(tIndex)); 
					
					// move on to the next terrain if there is one
					token = tokenizer.getNextToken();
				}
				return true;
			}
			catch (Exception e) {
				System.out.println("Failed to load terrain format 2");
				e.printStackTrace();
			}
		}
		else if (version.equals("3")) {
			try {
				String token = tokenizer.getNextToken(); // may or may not be "edit-terrain-index"
				if(token.equals("edit-terrain-index")) {
					tokenizer.getNextToken();
					token = tokenizer.getNextToken();
				}	
				// load terrains - keep going as long as there's another terrain to load				
				while (token != null && token.equals("terrain")) {
					
					/* parse data string into terrain data for this terrain */
					
					// first get this terrain's index
					int tIndex = Integer.parseInt(tokenizer.getNextToken());

					// next should be dimensions
					assert (tokenizer.getNextToken().contains("dimensions"));
					int twidth = Integer.parseInt(tokenizer.getNextToken());
					int theight = Integer.parseInt(tokenizer.getNextToken());
					
					boolean nameParsed = false;
					String name = ""; 
					
					// last may or may not be name
					token = tokenizer.getNextToken(); 
					if(token.equals("name")) {
						name = TerrainFileFormatUtils.getGunzippedString(tokenizer.getNextToken()); 
						nameParsed = true; 
					}
					
					//TODO This is what is causing the new loading version method
					//We need to read in the new dimensions.
					token = tokenizer.getNextToken();
					int tex_width = 1;
					int tex_height = 1;
					if (token.equals("texture_resolution")) {
						tex_width = Integer.parseInt(tokenizer.getNextToken());
						tex_height = Integer.parseInt(tokenizer.getNextToken());
					}
					
					// create a new terrain of the proper size to load data into
					TerrainData tdata, tdatacopy; 
					boolean backupCreated = false; 
					if(nameParsed) {
						pManager.createTerrain(tIndex, name, twidth, theight, tex_width, tex_height);
						token = tokenizer.getNextToken();
					}
					else 
						pManager.createTerrain(tIndex, twidth, theight);
					
					tdata = pManager.getTerrain(tIndex);
					tdatacopy = pManager.getTerrainCopy(tIndex);
					
					// now parse the remainder of the string for an 
					// unknown number of properies							
					while (!token.equals("end-terrain")) {
						// token must be a property name, so save it...
						String propertyName = token;
						// get associated data...
						StringBuffer propertyDataSB = new StringBuffer();
						token = tokenizer.getNextToken();
						while (!token.equals("end-terrain") && !token.contains("terrain_property")) {
							propertyDataSB.append(token);
							token = tokenizer.getNextToken();
						}
						String propertyDataString = propertyDataSB.toString();
						
						// and set that data in the terrain:
						if(propertyName.contains("terrain_property_copy")) {
							TerrainFileFormatUtils.setPropertyFromString(propertyName, propertyDataString, tdatacopy);
							backupCreated = true; 
						}
						else
							TerrainFileFormatUtils.setPropertyFromString(propertyName, propertyDataString, tdata);
					}
					// save terrain snapshot for rolling back changes
					if(backupCreated)
						pManager.saveTerrainSnapshot(pManager.getTerrainName(tIndex), tdatacopy);
					else
						pManager.saveTerrainSnapshot(pManager.getTerrainName(tIndex)); 
					
					// move on to the next terrain if there is one
					token = tokenizer.getNextToken();
				}
				return true;
			}
			catch (Exception e) {
				System.out.println("Failed to load terrain format 2");
				e.printStackTrace();
			}
		}
		else {
			System.out.println("Terrain format did not match any known.  Unable to load.");
			return false;
		}	
		return true;
	}
	
	private void setOldColoringPreferences() {
		Preferences prefs = Preferences.userRoot().node("StarLogo TNG").node("Coloring");
		prefs.putBoolean("SpaceLand PatchColoring", true);
        prefs.putBoolean("SpaceLand DrawingTools", false);
	}
	
	
	//TODO My new methods for associated textures
	
	//returns an int based on associated texture width
	//Note: This depends on the center being 0,0!!
	public int coordToTerrainX(float x) {
		return (int)(
				(x + SLTerrain.getMaxX()) * 
				td.getAssociatedWidth()/(2*SLTerrain.getMaxX())
				);
	}
	
	//returns an int based on associated texture height
	//Note: This depends on the center being 0,0!!
	public int coordToTerrainY(float y) {
		return (int)(
				(y + SLTerrain.getMaxZ()) *
				td.getAssociatedHeight()/(2*SLTerrain.getMaxZ())
				);
	}
	
	public void highlightRectangle(int x1, int y1, int x2, int y2, Color c) {
		td.highlightRectangle(x1, y1, x2, y2, c);
	}

	public void highlightImage(int x1, int y1, int x2, int y2, Image img) {
		td.highlightImage(x1, y1, x2, y2, img);
	}

	public void highlightEllipse(int x1, int y1, int x2, int y2, Color c) {
		td.highlightEllipse(x1, y1, x2, y2, c);
	}
	
	public void highlightPolygonLines(List<Integer> xList, List<Integer> yList, Color c) {
		td.highlightPolygonLines(xList, yList, c);
	}
	
	public void unhighlight() {
		td.unhighlight();
	}
	
	public void fillRectangle(int x1, int y1, int x2, int y2, Color c) {
		td.fillRectangle(x1, y1, x2, y2, c);
	}
	
	public void fillEllipse(int x1, int y1, int x2, int y2, Color c) {
		td.fillEllipse(x1, y1, x2, y2, c);
	}
	
	public void fillImage(int x1, int y1, int x2, int y2, Image img) {
		td.fillImage(x1, y1, x2, y2, img);
	}
	
	public void fillPolygon(List<Integer> xList, List<Integer> yList, Color c) {
		td.fillPolygon(xList, yList, c);
	}
	
}