package torusworld.model.obj;

import java.io.LineNumberReader;
import java.io.Reader;

import java.util.*;
import javax.media.opengl.GL;

public class MTLLibrary
{
    private HashMap<String, MTLMaterial> materials = new HashMap<String, MTLMaterial>();
    
    /**
     * @return true if successful
     */
    
    public boolean read(Reader r)
    {
        MTLMaterial currentMaterial = null;
        try
        {
            String line;
            LineNumberReader lnr = new LineNumberReader(r);
            while ((line = lnr.readLine()) != null)
            {
                if (line.trim().length() == 0 || line.charAt(0) == '#') continue;
                String[] tokens = line.split(" +");
                
                if (tokens[0].equals("newmtl"))
                {
                    currentMaterial = new MTLMaterial(tokens[1]);
                    materials.put(tokens[1], currentMaterial);
                }
                
                // if no usemtl statement was ever read, ignore anything else
                if (currentMaterial == null) continue;
                currentMaterial.parseMTLLine(tokens);
            }
        } catch (Exception e)
        {
            System.err.println("Error reading MTL file: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    public void loadTextures(String texPath)
    {
        for (MTLMaterial mat : materials.values())
            mat.loadTextures(texPath);
    }
    
    public void unloadTextures()
    {
        for (MTLMaterial mat : materials.values())
            mat.unloadTextures();
    }
    
    
    /**
     * Applies a material to the given GL context
     */
    public void applyMaterial(String name, GL gl)
    {
        MTLMaterial mat = materials.get(name);
        if (mat != null)
            mat.apply(gl);
        else
            gl.glDisable(GL.GL_TEXTURE_2D);
    }
    
    /**
     * @return the material in the library with the given name (null if it doesn't exist)
     */
    public MTLMaterial getMaterial(String name)
    {
        return materials.get(name);
    }
}
