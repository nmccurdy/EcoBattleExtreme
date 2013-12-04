package torusworld;

import starlogoc.Colors;
import starlogoc.StarLogo;
import starlogoc.PatchManager;
import starlogoc.TerrainData;
import starlogoc.TerrainDataPatch;
import javax.media.opengl.GL;

//import com.sun.medialib.mlib.Image;
import com.sun.opengl.util.texture.TextureData;

import terraineditor.Region;
import torusworld.math.*;

import java.awt.Color;
import java.nio.ShortBuffer;
import java.util.Iterator;


/*
 * The terrain is split up into square groups of patches of GROUP_SIZE * GROUP_SIZE;
 * each group has its own call list.
 * 
 * There is a main call list which just calls all the patches call lists, this is
 * used to render
 * 
 * PatchManager tells us exactly which patches have changed and we only update those,
 * and possibly some neighbours because of wall base changes.
 * 
 * currentUpdateNumber is used to identify which groups have already been updated
 * doring the current update without having to clear a huge matrix each time.
 * 
 *                                                                      Radu
 */

public class SLTerrain
{
    static final int GROUP_SIZE = 8; // each CallList corresponds to GROUP_SIZE * GROUP_SIZE patches
                                     // probably best values are 1 to 10, depending on calllist overhead
                                     // and on locality of updates
    
    static int height = 0, width = 0;
    static int groupHeight, groupWidth;
    static int lastUpdate[][]; // last currentUpdateNumber when the calllist was updated, width * height
    static float heights[][]; // last heights, width * height * 4
    static float normals[][]; // last normals, width * height * 5 * 3
    static int callLists; // base of the (contiguous) calllists
    static float patchSize, halfPatchSize;
    
    static int currentUpdateNumber;
    
    static TerrainData data;
    
    public static float getPatchSize() { return patchSize; }
    
    public static int getWidth() { return width; }
    
    public static int getHeight() { return height; }

    private static final int H_NW = TerrainDataPatch.HEIGHT_NW;
    private static final int H_SW = TerrainDataPatch.HEIGHT_SW;
    private static final int H_SE = TerrainDataPatch.HEIGHT_SE;
    private static final int H_NE = TerrainDataPatch.HEIGHT_NE;
    
    /**
     * call this whenever the whole terrain changes (load project/map/whatever)
     */
    public static void init(TerrainData _data)
    {
        if (height > 0) uninit();
        data = _data;
        
        width = data.getWidth();
        height = data.getHeight();
        patchSize = data.getPatchSize();
        System.out.println("SLTerain::init " + width + " " + height);
        halfPatchSize = 0.5f * patchSize;
        
        groupWidth = (width - 1) / GROUP_SIZE + 1;
        groupHeight = (height - 1) / GROUP_SIZE + 1;
        
        lastUpdate = new int[groupWidth][groupHeight];
        heights = new float[width][height * 4];
        normals = new float[width][height * 5 * 3];
        
        currentUpdateNumber = 0;

        GL gl = SLRendering.getGL();
        callLists  = gl.glGenLists(groupHeight * groupWidth + 1);
        
        // create the "main" call list that executes all the group call lists
        gl.glNewList(callLists + groupHeight * groupWidth, GL.GL_COMPILE);
        for (int i = 0; i < groupHeight * groupWidth; i++)
            gl.glCallList(callLists + i);
        gl.glEndList();
        
        //for (int i = 0; i < height; i += 1) {
        //	for (int j = 0; j < width; j += 1) {
        //		data.setColor(j, i, Color.WHITE); //Change color to white
        //	}
        //}
        //End of Changes
        
        //System.out.println("Patch color is " + data.getColor(4, 4));
        TextureManager.createTexture(data.getAssociatedName(), data.getAssociated());
        updateWholeTerrain();
    }
    
    public static void uninit()
    {
        if (height == 0) return;
        SLRendering.getGL().glDeleteLists(callLists, groupHeight * groupWidth + 1);
        height = 0;
        lastUpdate = null;
        heights = null;
    }
    
    /*
     * Suppose the terrain is 101x101, and patchSize is 1.
     * Patches are numbered from 0,0 to 100,100
     * 
     * Point 0,0 is the center of patch 50,50.
     * Point -50.5,-50.5 is the corner of patch 0,0
     * Point 50.5,50.5 is the corner of patch 100,100
     */
    
    public static float getMinX()
    {
        return - patchSize * width * 0.5f;
    }
    
    public static float getMinZ()
    {
        return - patchSize * height * 0.5f;
    }
    
    public static float getMaxX()
    {
        return patchSize * width * 0.5f;
    }
    
    public static float getMaxZ()
    {
        return patchSize * height * 0.5f;
    }
    
    public static float fromLogoX(double logoX)
    {
        return (float) logoX * patchSize + getMinX();
    }
    
    public static float fromLogoZ(double logoY)
    {
        return (float) logoY * patchSize + getMinZ();
    }
    
    public static float fromLogoHeight(double height)
    {
        return (float) height * patchSize;
    }
    
    public static float getPatchCenterX(int px)
    {
        return patchSize * (px - width * 0.5f + 0.5f);
    }

    public static float getPatchCenterZ(int py)
    {
        return patchSize * (py - height * 0.5f + 0.5f);
    }
    
    public static int getPatchX(float x)
    {
        int px;
        px = (int) Math.floor((x - getMinX()) / patchSize);
        if (px < 0) px = 0;
        if (px >= width) px = width-1;
        return px;
    }

    public static int getPatchY(float z)
    {
        int py;
        py = (int) Math.floor((z - getMinZ()) / patchSize);
        if (py < 0) py = 0;
        if (py >= height) py = height-1;
        return py;
    }
    
    /**
     * Finds the patch's x coordinate in spaceland coordinates 
     * (i.e. where 0,0 represesnts the patch in the middle of spaceland)
     * 
     * @param x The x coordinate of the point in spaceland
     * @return the patch's x coordinate in the spaceland coordinate 
     * system.
     */
    public static int getSpacelandPatchX(float x) {
    	return getPatchX(x)-width/2;
    }
    
    /**
     * Finds the patch's y coordinate in spaceland coordinates 
     * (i.e. where 0,0 represesnts the patch in the middle of spaceland)
     * 
     * @param z The z coordinate of the point in spaceland
     * @return the patch's y coordinate in the spaceland coordinate 
     * system.
     */
    public static int getSpacelandPatchY(float z) {
    	return -getPatchY(z)+height/2;
    }

    public static float getPointHeight(float x, float z)
    {
        int px = getPatchX(x), py = getPatchY(z); 
        
        x = (x - getPatchCenterX(px)) / patchSize + 0.5f;
        z = (z - getPatchCenterZ(py)) / patchSize + 0.5f;
        if (x < 0) x = 0.f;
        if (z < 0) z = 0.f;
        if (x > 1) x = 1.f;
        if (z > 1) z = 1.f;
        
        return heights[px][4*py + H_SE] * x * z +
               heights[px][4*py + H_NE] * x * (1-z) +
               heights[px][4*py + H_NW] * (1-x) * (1-z) +
               heights[px][4*py + H_SW] * (1-x) * z;
/*  return (float)data.getHeight((int)(x/data.getPatchSize()), (int)(z/data.getPatchSize()));*/
    }

    
    /* note: Most of the math crap is here because we want to avoid doing 
     * "new Vector3f" many times since the overhead is very large
     * 
     * I know most of this stuff would have been a lot easier if we used Vector3f 
     * functions, so please don't change it
     *      Radu
     */
    
    private static final int normalOffset(int py, int n)
    {
        return 5 * 3 * py + 3 * n;
    }
    
    private static final void setNormal(int px, int py, int n, float x, float y, float z)
    {
        float scale = (float) (1.0 / Math.sqrt(x*x + y*y + z*z));
        int ofs = normalOffset(py, n);
        
        normals[px][ofs + 0] = x * scale;
        normals[px][ofs + 1] = y * scale;
        normals[px][ofs + 2] = z * scale;
    }
    
    private static final void computePatchCornerNormal(int px, int py, int n, float x1, float z1, float h1, float x2, float z2, float h2)
    {
        setNormal(px, py, n, (h1 * z2) - (z1 * h2),
                             (z1 * x2) - (x1 * z2),
                             (x1 * h2) - (h1 * x2));
    }

    static float tempHeights[] = new float[4];
    private static void computePatchCornerNormals(int px, int py)
    {
        float h[] = tempHeights;
        data.getHeights(px, py, tempHeights);
        computePatchCornerNormal(px, py, H_NW,  0,  1, h[H_SW] - h[H_NW],
                                                1,  0, h[H_NE] - h[H_NW]);
        
        computePatchCornerNormal(px, py, H_SW,  1,  0, h[H_SE] - h[H_SW],
                                                0, -1, h[H_NW] - h[H_SW]);
        
        computePatchCornerNormal(px, py, H_SE,  0, -1, h[H_NE] - h[H_SE],
                                               -1, 0, h[H_SW] - h[H_SE]);
        
        computePatchCornerNormal(px, py, H_NE, -1, 0, h[H_NW] - h[H_NE],
                                                0,  1, h[H_SE] - h[H_NE]);

        float x = 0, y = 0, z = 0;
        for (int i = 0; i < 4; i++)
        {
            x += normals[px][normalOffset(py, i)+0];
            y += normals[px][normalOffset(py, i)+1];
            z += normals[px][normalOffset(py, i)+2];
        }
        setNormal(px, py, 4, x, y, z);
    }
        
    public static Vector3f getNormal(float x, float z)
    {
        int px = getPatchX(x), py = getPatchY(z);
        Vector3f res = new Vector3f(0, 0, 0);

        x = x - getPatchCenterX(px);
        z = z - getPatchCenterZ(py);
        if (x < 0) x = 0.f;
        if (z < 0) z = 0.f;
        if (x > 1) x = 1.f;
        if (z > 1) z = 1.f;
        
        getSmoothNormals(px, py, null);
        
        res.addScaled(smoothNormals[H_SE], x * z);
        res.addScaled(smoothNormals[H_NE], x * (1-z));
        res.addScaled(smoothNormals[H_NW], (1-x) * (1-z));
        res.addScaled(smoothNormals[H_SW], (1-x) * z);

        res.normalize();
        return res;
    }
    
    static private Vector3f[] smoothNormals = {new Vector3f(), // NW 
        new Vector3f(), // SW
        new Vector3f(), // SE
        new Vector3f(), // NE
        new Vector3f()}; // avg
    
    private static void addNormalToVec(int px, int py, int n, Vector3f vec)
    {
        int ofs = normalOffset(py, n);
        vec.x += normals[px][ofs + 0];
        vec.y += normals[px][ofs + 1];
        vec.z += normals[px][ofs + 2];
    }
    
    private static boolean sameHeight(int px, int py, int n, int npx, int npy, int npn, TerrainDataPatch p)
    {
        float h1, h2;
        if (p != null)
        {
            h1 = p.heights[n];
            h2 = data.getHeight(npx, npy, npn) * patchSize;
        } else
        {
            h1 = heights[px][4*py + n];
            h2 = heights[npx][4*npy + npn];
        }
        
        return Math.abs(h1 - h2) < 0.001;
    }
    
    private static int getMatchingCorner(int px, int py, int dx, int dy, int n)
    {
        if (dy == 0 && dx != 0) return n ^ 3; // flip E/W
        if (dy != 0 && dx == 0) return n ^ 1; // flip N/S
        return n ^ 2; // flip both N/S and E/W
    }
    
    // If p != null, use p and data to read heights. If p == null, use heights[][]
    private static void smoothNormal(int px, int py, int n, TerrainDataPatch p, int dx, int dy)
    {
        if (px + dx < 0 || px + dx >= width || py + dy < 0 || py + dy >= height) return;
        int npn = getMatchingCorner(px, py, dx, dy, n);
        if (sameHeight(px, py, n, px + dx, py + dy, npn, p))
            addNormalToVec(px + dx, py + dy, npn, smoothNormals[n]);
    }
                    
    private static void getSmoothNormals(int px, int py, TerrainDataPatch p)
    {
        for (int i = 0; i < 4; i++)
        {
            smoothNormals[i].set(0, 0, 0);
            addNormalToVec(px, py, i, smoothNormals[i]);
        }
        
        smoothNormal(px, py, H_NW, p, -1, 0);
        smoothNormal(px, py, H_NW, p, -1, -1);
        smoothNormal(px, py, H_NW, p, 0, -1);
        
        smoothNormal(px, py, H_NE, p, +1, 0);
        smoothNormal(px, py, H_NE, p, +1, -1);
        smoothNormal(px, py, H_NE, p, 0, -1);
        
        smoothNormal(px, py, H_SE, p, +1, 0);
        smoothNormal(px, py, H_SE, p, +1, +1);
        smoothNormal(px, py, H_SE, p, 0, +1);
        
        smoothNormal(px, py, H_SW, p, -1, 0);
        smoothNormal(px, py, H_SW, p, -1, +1);
        smoothNormal(px, py, H_SW, p, 0, +1);
        
        smoothNormals[4].set(0, 0, 0);
        for (int i = 0; i < 4; i++)
        {
            smoothNormals[i].normalize();
            smoothNormals[4].add(smoothNormals[i]);
        }
        smoothNormals[4].normalize();
    }
    
    
    private static float tempColorVec[] = new float[4];
    private static TerrainDataPatch tempPatch = new TerrainDataPatch();

    /* note: using TRIANGLE_FAN is a lot slower because of many glBegin/glEnd pairs which
     * apparently have a lot of overhead.
     * 
     */
    private static void renderPatch(int px, int py, TerrainDataPatch p, boolean color)
    {
        GL gl = SLRendering.getGL();
        float centerX = getPatchCenterX(px), centerZ = getPatchCenterZ(py);
        
        float ax = centerX - halfPatchSize, ay = p.heights[0], az = centerZ - halfPatchSize; // NW
        float bx = centerX - halfPatchSize, by = p.heights[1], bz = centerZ + halfPatchSize; // SW
        float cx = centerX + halfPatchSize, cy = p.heights[2], cz = centerZ + halfPatchSize; // SE
        float dx = centerX + halfPatchSize, dy = p.heights[3], dz = centerZ - halfPatchSize; // NE
        
//        gl.glBegin(GL.GL_TRIANGLES);
        
        getSmoothNormals(px, py, p);
        
        /*
        // Uncomment this code to see the normals
        getSmoothNormals(px, py, p);
        gl.glEnd();
        gl.glDisable(GL.GL_LIGHTING);
        gl.glBegin(GL.GL_LINES);
        gl.glColor3f(1, 0, 0);
        gl.glVertex3f(ax, ay, az);
        gl.glVertex3f(ax + smoothNormals[H_NW].x, ay + smoothNormals[H_NW].y, az + smoothNormals[H_NW].z);
        
        gl.glVertex3f(bx, by, bz);
        gl.glVertex3f(bx + smoothNormals[H_SW].x, by + smoothNormals[H_SW].y, bz + smoothNormals[H_SW].z);
        
        gl.glVertex3f(cx, cy, cz);
        gl.glVertex3f(cx + smoothNormals[H_SE].x, cy + smoothNormals[H_SE].y, cz + smoothNormals[H_SE].z);
        
        gl.glVertex3f(dx, dy, dz);
        gl.glVertex3f(dx + smoothNormals[H_NE].x, dy + smoothNormals[H_NE].y, dz + smoothNormals[H_NE].z);
        
        gl.glVertex3f(centerX, 0.25f * (p.heights[0] + p.heights[1] + p.heights[2] + p.heights[3]), centerZ);
        gl.glVertex3f(centerX + smoothNormals[4].x, 0.25f * (p.heights[0] + p.heights[1] + p.heights[2] + p.heights[3]) + smoothNormals[4].y, centerZ + smoothNormals[4].z);

        
        gl.glEnd();
        
        gl.glEnable(GL.GL_LIGHTING);
        gl.glBegin(GL.GL_TRIANGLES);
        */
                     
        if (color) {
            //TODO Patch Colors
//            Colors.colorarray[(int)(StarLogo.slnumToDouble(p.color)*32)].getRGBComponents(tempColorVec);
            gl.glColor3fv(p.color.getColorComponents(null), 0);
        }
        
        // set up texture coordinates for this patch
        float tex_left = px/101.0f*505/512;
        float tex_right = (px+1)/101.0f*505/512;
        float tex_up = (100-py+1)/101.0f*505/512+7.0f/512;
        float tex_down = (100-py)/101.0f*505/512+7.0f/512;
        
        if (smoothNormals[0].equals(smoothNormals[1], 0.01f) &&
            smoothNormals[0].equals(smoothNormals[2], 0.01f) &&
            smoothNormals[0].equals(smoothNormals[3], 0.01f))
        {
            // Save 2 triangles if we can.
            // This might not seem like much, but in the very common case of flat
            // terrain this means 20.000 triangles instead of 40.000.
            gl.glNormal3f(smoothNormals[0].x, smoothNormals[0].y, smoothNormals[0].z);
            
            //TODO Here we now call the texture coordinates
            //We have added all of the gl.glTexCoord2f
            //We can calculate using the left,right,up,and down texture coordinates
            //assuming 101
            //The thing we have to call before this is TextureManager.bindTexture
            
            gl.glTexCoord2f(tex_left, tex_down);
            gl.glVertex3f(bx, by, bz);
            gl.glTexCoord2f(tex_right, tex_down);
            gl.glVertex3f(cx, cy, cz);
            gl.glTexCoord2f(tex_left, tex_up);
            gl.glVertex3f(ax, ay, az);
            
            gl.glTexCoord2f(tex_left, tex_up);
            gl.glVertex3f(ax, ay, az);
            gl.glTexCoord2f(tex_right, tex_down);
            gl.glVertex3f(cx, cy, cz);
            gl.glTexCoord2f(tex_right, tex_up);
            gl.glVertex3f(dx, dy, dz);
        }
        else
        {
            float centerY = 0.25f * (p.heights[0] + p.heights[1] + p.heights[2] + p.heights[3]);
            
            // set up center point texture coordinates
            float tex_cen_horiz = (tex_left + tex_right)/2.0f;
            float tex_cen_vert = (tex_up + tex_down)/2.0f;

            gl.glNormal3f(smoothNormals[4].x, smoothNormals[4].y, smoothNormals[4].z);
            gl.glTexCoord2f(tex_cen_horiz, tex_cen_vert);
            gl.glVertex3f(centerX, centerY, centerZ);
            gl.glNormal3f(smoothNormals[H_NW].x, smoothNormals[H_NW].y, smoothNormals[H_NW].z);
            gl.glTexCoord2f(tex_left, tex_up);
            gl.glVertex3f(ax, ay, az);
            gl.glNormal3f(smoothNormals[H_SW].x, smoothNormals[H_SW].y, smoothNormals[H_SW].z);
            gl.glTexCoord2f(tex_left, tex_down);
            gl.glVertex3f(bx, by, bz);
            
            gl.glNormal3f(smoothNormals[4].x, smoothNormals[4].y, smoothNormals[4].z);
            gl.glTexCoord2f(tex_cen_horiz, tex_cen_vert);
            gl.glVertex3f(centerX, centerY, centerZ);
            gl.glNormal3f(smoothNormals[H_SW].x, smoothNormals[H_SW].y, smoothNormals[H_SW].z);
            gl.glTexCoord2f(tex_left,tex_down);
            gl.glVertex3f(bx, by, bz);
            gl.glNormal3f(smoothNormals[H_SE].x, smoothNormals[H_SE].y, smoothNormals[H_SE].z);
            gl.glTexCoord2f(tex_right,tex_down);
            gl.glVertex3f(cx, cy, cz);
            
            gl.glNormal3f(smoothNormals[4].x, smoothNormals[4].y, smoothNormals[4].z);
            gl.glTexCoord2f(tex_cen_horiz, tex_cen_vert);
            gl.glVertex3f(centerX, centerY, centerZ);
            gl.glNormal3f(smoothNormals[H_SE].x, smoothNormals[H_SE].y, smoothNormals[H_SE].z);
            gl.glTexCoord2f(tex_right, tex_down);
            gl.glVertex3f(cx, cy, cz);
            gl.glNormal3f(smoothNormals[H_NE].x, smoothNormals[H_NE].y, smoothNormals[H_NE].z);
            gl.glTexCoord2f(tex_right, tex_up);
            gl.glVertex3f(dx, dy, dz);
            
            gl.glNormal3f(smoothNormals[4].x, smoothNormals[4].y, smoothNormals[4].z);
            gl.glTexCoord2f(tex_cen_horiz, tex_cen_vert);
            gl.glVertex3f(centerX, centerY, centerZ);
            gl.glNormal3f(smoothNormals[H_NE].x, smoothNormals[H_NE].y, smoothNormals[H_NE].z);
            gl.glTexCoord2f(tex_right, tex_up);
            gl.glVertex3f(dx, dy, dz);
            gl.glNormal3f(smoothNormals[H_NW].x, smoothNormals[H_NW].y, smoothNormals[H_NW].z);
            gl.glTexCoord2f(tex_left, tex_up);
            gl.glVertex3f(ax, ay, az);
        }
//        gl.glEnd();
    }

    private static void renderPatchWalls(int px, int py)
    {  	
    	GL gl = SLRendering.getGL();
        boolean gotpatch = false;
        float h0a, h1a, h0b, h1b, cx, cz;
       
        float tex_left = px/101.0f*505/512;
        float tex_right = (px+1)/101.0f*505/512;
        float tex_up = (100-py+1)/101.0f*505/512+7.0f/512;
        float tex_down = (100-py)/101.0f*505/512+7.0f/512;

        h0a = heights[px][4*py];
        if (px > 0) // west wall
        {
            h1a = heights[px][4*py + 1];
            /* if we are at the edge of the group, heights[] might not be up to date */
            h0b = px % GROUP_SIZE == 0 ? data.getHeight(px-1, py, 3) * patchSize : heights[px-1][4*py + 3];
            h1b = px % GROUP_SIZE == 0 ? data.getHeight(px-1, py, 2) * patchSize : heights[px-1][4*py + 2];
            
            if (Math.abs(h0a - h0b) > 0.0001 || Math.abs(h1a-h1b) > 0.0001)
            {
                gotpatch = true;
                data.getTerrainDataPatch(px, py, tempPatch);
                cx = getPatchCenterX(px);
                cz = getPatchCenterZ(py);
                
                // shift tex coords so the wall is the color of the higher patch
                if (h0a+h1a < h0b+h1b) {
                	tex_left -= .001f;
                } else {
                	tex_left += .001f;
                }
                
                gl.glColor3f(1.0f, 1.0f, 1.0f);
                gl.glNormal3f(h0a+h1a < h0b+h1b ? 1.f : -1.f, 0.f, 0.f);
                gl.glTexCoord2f(tex_left, tex_up);
                gl.glVertex3f(cx - halfPatchSize, h0a, cz - halfPatchSize);
                gl.glTexCoord2f(tex_left, tex_up);
                gl.glVertex3f(cx - halfPatchSize, h0b, cz - halfPatchSize);
                gl.glTexCoord2f(tex_left, tex_down);
                gl.glVertex3f(cx - halfPatchSize, h1b, cz + halfPatchSize);
                gl.glTexCoord2f(tex_left, tex_down);
                gl.glVertex3f(cx - halfPatchSize, h1a, cz + halfPatchSize);
            }
        }
        
        if (py > 0) // north wall
        {
            h1a = heights[px][4*py + 3];
            /* if we are at the edge of the group, heights[] might not be up to date */
            h0b = py % GROUP_SIZE == 0 ? data.getHeight(px, py-1, 1) * patchSize : heights[px][4*(py-1) + 1];
            h1b = py % GROUP_SIZE == 0 ? data.getHeight(px, py-1, 2) * patchSize : heights[px][4*(py-1) + 2];
            
            if (Math.abs(h0a - h0b) > 0.0001 || Math.abs(h1a-h1b) > 0.0001)
            {
                if (!gotpatch)
                    data.getTerrainDataPatch(px, py, tempPatch);
                cx = getPatchCenterX(px);
                cz = getPatchCenterZ(py);
                
                // reset tex_left in case we messed with it above
                tex_left = px/101.0f*505/512; 
                
                // shift tex coords so the wall is the color of the higher patch
                if (h0a+h1a < h0b+h1b) {
                	tex_up += .001f;
                } else {
                	tex_up -= .001f;
                }
                
                gl.glColor3f(1.0f, 1.0f, 1.0f);
                gl.glNormal3f(0.f, 0.f, h0a+h1a < h0b+h1b ? 1.f : -1.f);
                gl.glTexCoord2f(tex_left, tex_up);
                gl.glVertex3f(cx - halfPatchSize, h0a, cz - halfPatchSize);
                gl.glTexCoord2f(tex_left, tex_up);
                gl.glVertex3f(cx - halfPatchSize, h0b, cz - halfPatchSize);
                gl.glTexCoord2f(tex_right, tex_up);
                gl.glVertex3f(cx + halfPatchSize, h1b, cz - halfPatchSize);
                gl.glTexCoord2f(tex_right, tex_up);
                gl.glVertex3f(cx + halfPatchSize, h1a, cz - halfPatchSize);
            }
        }
    }

    /**
     * 
     * Updates the terrain call lists, using the changed patches buffer in
     * patch manager. 
     * 
     * Acquires sl.getLock(). Calls pman.resetNumChangedPatches.
     * 
     * @return true iff some patch was changed
     */
    public static boolean updateTerrainUsingPatchManager(PatchManager pman)
    {
        int nr;
        short px, py;
//        ShortBuffer buf;

        nr = pman.getNumChangedPatches();
        if (nr == 0) return false;
        
        
        
        if (nr >= pman.getTotalPatches() / 2)
        {
            updateWholeTerrain();
            pman.resetNumChangedPatches();
            return true;
        }
        currentUpdateNumber++;
        // first update the normals
//        buf.rewind();
        Iterator<PatchManager.PatchCoordinates> itr = pman.getChangedPatchesIterator();
        while (itr.hasNext()) {
        	PatchManager.PatchCoordinates coord = itr.next();
            computePatchCornerNormals(coord.x, coord.y);
 
        }

        // then update the call-lists
        itr = pman.getChangedPatchesIterator();
        while (itr.hasNext()) {
        	PatchManager.PatchCoordinates coord = itr.next();
            updateGroup(coord.x / GROUP_SIZE, coord.y / GROUP_SIZE, true);
        }
        
        pman.resetNumChangedPatches();
        return true;
    }
    
    
    public static void updateWholeTerrain()
    {
    	
    	//Whenever we redisplay, we have to draw the associated texture on.
    	TextureData td = data.getAssociated();
    	Texture assoc = TextureManager.getTexture(data.getAssociatedName());
    	if (assoc != null) {
    		assoc.updateTexture(td);
    	}
    	else {
    		throw new RuntimeException("We're trying to use a texture we didn't create");
    	}
    	
    	
    	
        currentUpdateNumber++;
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++)
                computePatchCornerNormals(i, j);
        
        for (int i = 0; i < groupWidth; i++)
            for (int j = 0; j < groupHeight; j++)
                updateGroup(i, j, false);
    }
    
    public static void renderTerrain()
    {
        // lame slow render
/*        int x, y;
        for (x = 0; x < height; x++)
            for (y = 0; y < width; y++)
            {
                data.getTerrainDataPatch(x, y, tempPatch);
                renderPatch(x, y, tempPatch, gl);
            }*/
        GL gl = SLRendering.getGL();
        //gl.glDisable(GL.GL_TEXTURE_2D);
        gl.glDisable(GL.GL_CULL_FACE);
        gl.glEnable(GL.GL_TEXTURE_2D);
        //TODO First update the texture according to our texture manager.
        //Then render again.
        TextureData td = data.getAssociated();
    	Texture assoc = TextureManager.getTexture(data.getAssociatedName());
    	if (assoc != null) {
    		if (data.textureChanged()) {
    			assoc.updateTexture(td);
    			data.setTextureChanged(false);
    		}
    	}
    	else {
    		throw new RuntimeException("We're trying to use a texture we didn't create");
    	}
        TextureManager.bindTextureGLNearest(data.getAssociatedName());
        gl.glCallList(callLists + groupHeight * groupWidth);
    }
    
    private static void updateGroup(int gx, int gy, boolean recurse) // recursive depth at most width+height
    {
        GL gl = SLRendering.getGL();
        int px, py, i;
        boolean updateSouth = false, updateEast = false;
        
        if (lastUpdate[gx][gy] == currentUpdateNumber) return;
        lastUpdate[gx][gy] = currentUpdateNumber;
        
        gl.glNewList(callLists + gx * groupHeight + gy, GL.GL_COMPILE);
        
        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glBegin(GL.GL_TRIANGLES);
        
        for (px = gx * GROUP_SIZE; px < (gx+1)*GROUP_SIZE && px < width; px++)
        {
            for (py = gy * GROUP_SIZE; py < (gy+1)*GROUP_SIZE && py < height; py++)
            {
                data.getTerrainDataPatch(px, py, tempPatch);
//                System.out.println("renering patch: " + px + ", " + py);
                renderPatch(px, py, tempPatch, true);
                
                // check if group to the south needs updating (because of its north vertical wall
                if (recurse && !updateSouth && py == (gy+1)*GROUP_SIZE - 1 && py < height - 1 &&
                    (tempPatch.heights[TerrainDataPatch.HEIGHT_SW] != heights[px][4*py + TerrainDataPatch.HEIGHT_SW] || 
                     tempPatch.heights[TerrainDataPatch.HEIGHT_SE] != heights[px][4*py + TerrainDataPatch.HEIGHT_SE]))
                    updateSouth = true;
                
                // check if group to the east needs updating (because of its west vertical wall)
                if (recurse && !updateEast && px == (gx+1)*GROUP_SIZE - 1 && px < width - 1 &&
                    (tempPatch.heights[TerrainDataPatch.HEIGHT_NE] != heights[px][4*py + TerrainDataPatch.HEIGHT_NE] ||
                     tempPatch.heights[TerrainDataPatch.HEIGHT_SE] != heights[px][4*py + TerrainDataPatch.HEIGHT_SE]))
                    updateEast = true;
                
                for (i = 0; i < 4; i++)
                    heights[px][4*py + i] = tempPatch.heights[i];
            }
        }
        gl.glEnd();
        gl.glBegin(GL.GL_QUADS);
        /* separated because we will have another begin/end because of different textures */
        for (px = gx * GROUP_SIZE; px < (gx+1)*GROUP_SIZE && px < width; px++)
            for (py = gy * GROUP_SIZE; py < (gy+1)*GROUP_SIZE && py < height; py++)
                renderPatchWalls(px, py);
        
        gl.glEnd();
        gl.glEndList();
        
        if (recurse && updateSouth)
            updateGroup(gx, gy+1, true);
        if (recurse && updateEast)
            updateGroup(gx+1, gy, true);
    }
    
    /**
     * Draws the specified rectangular part of the terrain using an inverting blend function - used
     * to display selection with on-the-fly editor.
     * The arguments are the patch coordinates of the region corners (with x1 <= x2 && y1 <= y2)
     */
    public static void renderOverlay(Region region) {
        int x1 = region.minX, y1 = region.minY;
        int x2 = region.maxX, y2 = region.maxY;
        
        if (x1 > x2 || y1 > y2) {
            return;
        }

        if (x1 < 0 || y1 < 0 || x2 >= width || y2 >= height) {
            System.out.println("SLTerrain::drawOverlay: out of bounds arguments: " + 
                               x1 + " " + y1 + " -> " + x2 + " " + y2);
        }
        GL gl = SLRendering.getGL();
        int[] depthFunc = new int[1];
        gl.glGetIntegerv(GL.GL_DEPTH_FUNC, depthFunc, 0);
        gl.glDepthFunc(GL.GL_EQUAL);
        
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_ONE_MINUS_DST_COLOR, GL.GL_ZERO);
        gl.glColor4f(1, 1, 1, 1);
        gl.glBegin(GL.GL_TRIANGLES);
        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                data.getTerrainDataPatch(x, y, tempPatch);
                renderPatch(x, y, tempPatch, false);
            }
        }
        gl.glEnd();
        
/*      // This would show areas that are obscured with a slight shade. Nice, but too slow..
 
        gl.glDepthFunc(GL.GL_NOTEQUAL);
        
        gl.glBlendFunc(GL.GL_ONE_MINUS_DST_COLOR, GL.GL_SRC_ALPHA);
        gl.glColor4f(0.1f, 0.1f, 0.1f, 0.9f);
        gl.glBegin(GL.GL_TRIANGLES);
        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                data.getTerrainDataPatch(x, y, tempPatch);
                renderPatch(x, y, tempPatch, false);
            }
        }
        gl.glEnd();*/
        
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        
        gl.glDepthFunc(depthFunc[0]);
        
    }
    
    /**
     * Renders a grid that outlines the patches. This is useful
     * for when we are in a "build" mode. It allows the user to 
     * see where individual patches are located. 
     */
    public static void renderGrid() {               
        GL gl = SLRendering.getGL(); 
        gl.glColor4f(0.5f, 0.5f, 0.5f, 0.5f);	// color of the grid -- gray 
        gl.glEnable(GL.GL_LINE_SMOOTH);	// turn on anti-aliasing
        gl.glBegin(GL.GL_LINES);
        float centerX, centerZ, ax, ay, az, bx, by, bz, cx, cy, cz, dx, dy, dz; 
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
            	// draws all 4 lines because we can change a patch's height 
            	// (i.e. there may be discontunities in spaceland). 
                data.getTerrainDataPatch(x, y, tempPatch);
                centerX = getPatchCenterX(x); centerZ = getPatchCenterZ(y);                
                ax = centerX - halfPatchSize; ay = tempPatch.heights[0]; az = centerZ - halfPatchSize; // NW
                bx = centerX - halfPatchSize; by = tempPatch.heights[1]; bz = centerZ + halfPatchSize; // SW
                cx = centerX + halfPatchSize; cy = tempPatch.heights[2]; cz = centerZ + halfPatchSize; // SE
                dx = centerX + halfPatchSize; dy = tempPatch.heights[3]; dz = centerZ - halfPatchSize; // NE
            	
                gl.glVertex3f(ax, ay+.01f, az);
                gl.glVertex3f(bx, by+.01f, bz);   
                
                gl.glVertex3f(bx, by+.01f, bz);
                gl.glVertex3f(cx, cy+.01f, cz);
                
                gl.glVertex3f(cx, cy+.01f, cz);
                gl.glVertex3f(dx, dy+.01f, dz);
                
                gl.glVertex3f(dx, dy+.01f, dz);
                gl.glVertex3f(ax, ay+.01f, az); 
            }
        }
        gl.glEnd();        
        gl.glDisable(GL.GL_LINE_SMOOTH);
    } 
}