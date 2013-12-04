package torusworld.model.obj;

import java.io.File;

import torusworld.Texture;
import torusworld.TextureManager;

import javax.media.opengl.GL;

class MTLMaterial 
{
    private String name;
    private float colorKd[] = {1, 1, 1};
    private String mapKd = "";
    private Texture mapKdTexture = null;
    
    /**
     * Creates a new material with the given name
     */
    public MTLMaterial(String name)
    {
        this.name = name;
    }
    
    /**
     * Parses a line in the MTL file corresponding to this material.
     * 
     * Currently recognizes Kd and map_Kd tags. Other tags are ignored.
     */
    public void parseMTLLine(String[] line_tokens)
    {
        if (line_tokens[0].equals("Kd"))
        {
            for (int i = 0; i < 3; i++)
                colorKd[i] = Float.parseFloat(line_tokens[i+1]);
        } else
        if (line_tokens[0].equals("map_Kd"))
        {
            mapKd = line_tokens[1];
            int slash_pos = Math.max(mapKd.lastIndexOf("/"), mapKd.lastIndexOf("\\"));
            if (slash_pos >= 0)
                mapKd = mapKd.substring(slash_pos+1);
//            System.out.print("Material " + name + " map_Kd line: ");
//            for (String token: line_tokens)
//                System.out.print(token + "  ");
//            System.out.println("   final tex name: " + mapKd);
        }
    }
    
    /**
     * Loads the texture(s) associated with this material.
     * Currently this can only be map_Kd
     */
    public void loadTextures(String texPath)
    {
        if (mapKd.equals("")) return;
        String fullPath = texPath + mapKd;
        this.mapKdTexture = TextureManager.getTexture(fullPath);

        if (this.mapKdTexture != null) return;  // texture already loaded
        
        try
        {
            mapKdTexture = TextureManager.createTexture(fullPath,
                                new File(fullPath).toURI().toURL(), false);
        } catch (Exception e)
        {
            System.out.println("Unable to load texture: " + e);
            e.printStackTrace();
        }
    }

    /**
     * Removes (from TextureManager) any textures associated with this material.
     */
    public void unloadTextures()
    {
        TextureManager.removeTexture(mapKdTexture);
        mapKdTexture = null;
    }
    
    /**
     * Applies this material to the given GL context.
     */
    public void apply(GL gl)
    {
        gl.glColor3f(colorKd[0], colorKd[1], colorKd[2]);
        if (mapKdTexture != null)
        {
            gl.glEnable(GL.GL_TEXTURE_2D);
            mapKdTexture.bind();
        } else
            gl.glEnable(GL.GL_TEXTURE_2D);
    }
    
    
    /**
     * Returns the name of this material.
     */
    public String getName()
    {
        return name;
    }
    
    /**
     * @return true if there is a loaded texture associated with this material.
     */
    public boolean hasTexture()
    {
        return mapKdTexture != null;
    }
}