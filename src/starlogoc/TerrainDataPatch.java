package starlogoc;

import java.awt.Color;


public class TerrainDataPatch {
    
	/*
     each patch contains:
	  slnum *heap; // pointer to the heap where this patch's variables are stored
  	  slnum color; // SL Color of the patch
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
    
    public static final int HEIGHT_NW = 0; 
    public static final int HEIGHT_SW = 1; 
    public static final int HEIGHT_SE = 2; 
    public static final int HEIGHT_NE = 3; 
    
    public Color color;
    public int texture;
    public float heights[] = new float[4];
    public float tex_coords[] = new float[4];
    public long north_color;
    public int north_texture;
    public float north_tx0, north_tx1;
    public long west_color;
    public int west_texture;
    public float west_tx0, west_tx1;
    
    public TerrainDataPatch() {
    	this.color = Color.green;
    	for (int i=0; i < 4; i++) {
    		heights[i] = 0;
    	}
    }
    
    public TerrainDataPatch(TerrainDataPatch tdp) {
    	this.color = tdp.color;
    	this.texture = tdp.texture;
    	for (int i=0; i < 4; i++) {
    		this.heights[i] = tdp.heights[i];
    		this.tex_coords[i] = tdp.tex_coords[i];
    	}
    	this.north_color= tdp.north_color;
    	this.north_texture = tdp.north_texture;
    	this.north_tx0 = tdp.north_tx0;
    	this.north_tx1 = tdp.north_tx1;
    	this.west_color = tdp.west_color;
    	this.west_texture = tdp.west_texture;
    	this.west_tx0 = tdp.west_tx0;
    	this.west_tx1 = tdp.west_tx1;
    }
}






