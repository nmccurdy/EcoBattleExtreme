package torusworld.model.md3;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Iterator;
import torusworld.Texture;
import torusworld.TextureManager;
import torusworld.math.Cylinder;
import torusworld.math.Matrix4f;

import java.util.HashMap;

import javax.media.opengl.GL;


/* Each Md3SubModel corresponds to a .md3 file */

final class Md3SubModel 
{
	public Md3Header header;
    public Md3Tag[] Tags; // numTags * numFrames
    private Md3FrameInfo[] FrameInfos; // numFrames
	private Md3Surface[] Surfaces; // numSurfaces
    
    public Md3SubModel[] Links; // numTags
    
    private Matrix4f transformation;
    
    private HashMap<Integer, Integer> displayLists = new HashMap<Integer, Integer> ();

    ArrayList<Texture> textures = new ArrayList<Texture>();
    ArrayList<AnimationInfo> animations = new ArrayList<AnimationInfo>();
	
    public Md3SubModel() {}
    
    public void addTexture(Texture texture) 
    {
    	textures.add(texture);
    }

    public Iterator<Texture> getTextures() 
    {
    	return textures.iterator();
    }
    public int numTextures() 
    {
    	return textures.size();
    }
    
    public boolean loadMD3(String file, Matrix4f transformation)
    {
        int fileSize, i;
        byte[] fileContents;
        this.transformation = transformation;
        
        try {
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);
            fileSize = bis.available();
            fileContents = new byte[fileSize];
            bis.read(fileContents, 0, fileSize);
            bis.close();
            fis.close();
            } catch (IOException ioe) {
                return false;
            }
        
        ByteBuffer buffer = ByteBuffer.wrap(fileContents).order(ByteOrder.LITTLE_ENDIAN); //nativeOrder());
        
        header = new Md3Header(buffer);
        if (!header.fileID.equals("IDP3") || header.version != 15) 
        {
            System.err.println("Invalid MD3 file format in " + file + "!");
            return false;
        }
        
        buffer.position(header.ofsFrames);
        FrameInfos = new Md3FrameInfo[header.numFrames];
        for (i = 0; i < header.numFrames; i++)
            FrameInfos[i] = new Md3FrameInfo(buffer);
        
        buffer.position(header.ofsTags);
        Tags = new Md3Tag[header.numTags * header.numFrames];
        for (i = 0; i < header.numTags * header.numFrames; i++)
            Tags[i] = new Md3Tag(buffer, transformation);
        
        buffer.position(header.ofsSurfaces);
        Surfaces = new Md3Surface[header.numSurfaces];
        for (i = 0; i < header.numSurfaces; i++)
            Surfaces[i] = new Md3Surface(buffer);
        
        Links = new Md3SubModel[header.numTags];

        for (i = 0; i < header.numTags; i++)
            Links[i] = null;
        
        buffer = null;
        fileContents = null;
        System.gc();
        
        return true;
    }

    /* This function is used to load a .skin file for the .md3 model associated
    with it. The .skin file stores the textures that need to go with each
    object and subject in the .md3 files. For instance, in our Lara Croft model,
    her upper body model links to 2 texture; one for her body and the other for
    her face/head. The .skin file for the lara_upper.md3 model has 2
    textures:
   
    u_torso,models/players/laracroft/default.bmp
    u_head,models/players/laracroft/default_h.bmp
   
    Notice the first word, then a comma. This word is the name of the
    object
    in the .md3 file. Remember, each .md3 file can have many sub-objects.
    The next bit of text is the Quake3 path into the .pk3 file where the
    texture for that model is stored Since we don't use the Quake3 path
    because we aren't making Quake, I just grab the texture name at the
    end of the string and disregard the rest. of course, later this is
    concatenated to the original MODEL_PATH that we passed into load our
    character.
    So, for the torso object it's clear that default.bmp is assigned to it, where
    as the head model with the pony tail, is assigned to default_h.bmp.
    Simple enough.
    What this function does is go through all the lines of the .skin file, and then
    goes through all of the sub-objects in the .md3 file to see if their name is
    in that line as a sub string. We use our cool IsInString() function for that.
    If it IS in that line, then we know that we need to grab it's texture file at
    the end of the line. I just parse backwards until I find the last '/' character,
    then copy all the characters from that index + 1 on (I.E. "default.bmp"). */

    public boolean loadSkin(String strSkin, String texPath)
    {
        // System.out.println(strSkin);
        // Make sure valid data was passed in
        if (strSkin == null)
            return false;

        InputStream is = null;
        try
        {
            is = new FileInputStream(strSkin);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            String strLine;
           

            // Go through every line in the .skin file
            while ((strLine = reader.readLine()) != null)
              for (int i = 0; i < header.numSurfaces; i++)
                if (strLine.indexOf(Surfaces[i].header.name) >= 0)
                {
                    if (strLine.endsWith("nodraw"))
                    {
                        Surfaces[i].texture = null;
                        continue;
                    }                    
                        
                    String fullPath = texPath + strLine.substring(strLine.lastIndexOf("/") + 1);
                    Texture texture = TextureManager.getTexture(fullPath);
                    if (texture == null) /* must load texture */
                    {
                        try
                        {
                            texture = TextureManager.createTexture(fullPath,
                                                new File(fullPath).toURI().toURL(), false);
                            
                            if (texture != null) addTexture(texture);
                        } catch (Exception e)
                        {
                            System.out.println("Unable to load texture: " + e);
                            e.printStackTrace();
                        }
                    }
                    Surfaces[i].texture = texture;
                    Surfaces[i].flipTexCoords(); // because of the old implementation, textures are "pre-flipped"
                }
            reader.close();

        } catch (IOException e)
        {
            throw new RuntimeException("Could not load skin.");
        }
        return true;
    }
    
    public void link(Md3SubModel child, String tagName)
    {
        if (child == null || tagName == null)
            return;

        for (int i = 0; i < header.numTags; i++)
            if (Tags[i].name.equals(tagName))
            {
                Links[i] = child;
                return;
            }
    }
    
    public void unload(GL gl)
    {
        int i;
        if (header == null) return;
        
        for (i = 0; i < textures.size(); i++)
            TextureManager.removeTexture(textures.get(i));
        
        
        for (Iterator<Integer> it = displayLists.values().iterator(); it.hasNext();)
            gl.glDeleteLists(it.next().intValue(), 1);
        
        for (i = 0; i < header.numSurfaces; i++)
        {
            Surfaces[i].unload();
            Surfaces[i] = null;
        }
        
        textures.clear();
        displayLists.clear();
        animations.clear();
        
        Surfaces = null;
        Tags = null;
        FrameInfos = null;
    }
	
    public void renderFrame(GL gl, int frame1, int frame2, float t, int intFrames)
    {
        int i, ifr;
        Integer val;
        
        if (frame2 != frame1 + 1) intFrames = 0;
        ifr = Math.round(t * (intFrames + 1));
        t = 1.f / (intFrames + 1) * ifr;
        
        /* only interpolate if the frames are consecutive in the animation */
        if (ifr == 0)
            val = -frame1-666;
        else
            if (ifr == intFrames + 1) val = -frame2-666;
        else
            val = frame1 + frame2 * header.numFrames + ifr * header.numFrames * header.numFrames;
        
        
        if (!displayLists.containsKey(val))
        {
            int l =  gl.glGenLists(1);
            gl.glNewList(l, GL.GL_COMPILE);
//            System.out.println("new display list " + l + " for frame " + frame1 + " " + frame2 + " "  + ifr);
            gl.glPushMatrix();
            gl.glMultMatrixf(transformation.toGLMatrix(), 0);
            for (i = 0; i < header.numSurfaces; i++)
                Surfaces[i].renderFrame(gl, frame1, frame2, t);
            gl.glPopMatrix();
            gl.glEndList();
            gl.glCallList(l);
            displayLists.put(val, l);
        } else
            gl.glCallList(displayLists.get(val).intValue());
    }
    
    public Cylinder computeBoundingCylinderForFrame(Matrix4f transformation, int frame)
    {
        float radius = 0, top = -1e10f, bottom = 1e10f;
        for (int i = 0; i < header.numSurfaces; i++)
        {
            Cylinder c = Surfaces[i].computeBoundingCylinderForFrame(
                                transformation.mult(this.transformation), frame);
            radius = Math.max(radius, c.radius);
            top = Math.max(top, c.top);
            bottom = Math.min(bottom, c.bottom);
        }
        return new Cylinder(radius, top, bottom);
    }


}
