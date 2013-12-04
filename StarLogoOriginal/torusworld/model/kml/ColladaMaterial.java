package torusworld.model.kml;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import javax.media.opengl.GL;

import torusworld.Texture;
import torusworld.TextureManager;
import org.jdom.*;

public class ColladaMaterial {

    private String name;
    private float colorText[] = {1, 1, 1, 1};
    private String ColladaTextureURL = "";
    private Texture ColladaTexture = null;
    
    /**
     * Creates a new material with the given name
     */
    public ColladaMaterial(String name)
    {
        this.name = name;
    }
    
    /**
     * Parses a material tag in the .dae file corresponding to this material.
     * 
     *
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
    */
    
    
    /**
     * Every material links to an effect 
     * Parse one effect into a COLLADA material in the .dae file from 
     * 
     */
    public void parseColladaMaterial(Element effect, Namespace ns, HashMap<String, String> map_imgURL) 
    {
    	try{
    		Element diffuse = effect.getChild("profile_COMMON", ns).getChild("technique", ns).getChild("lambert", ns);
        	if(diffuse==null)
        	{
        		diffuse = effect.getChild("profile_COMMON", ns).getChild("technique", ns).getChild("phong", ns);
        	}
    		diffuse = diffuse.getChild("diffuse", ns);
        	
        	// diffuse color of texture is either a link to an texture map, or a solid color
        	if (diffuse.getChild("color", ns) != null)
        	{
        		String[] c = diffuse.getChild("color", ns).getValue().split(" ");
        		for (int i = 0; i < 4; i++)
        		{
        			colorText[i] = Float.parseFloat(c[i]);
        		}
        	}
        	
        	if (diffuse.getChild("texture", ns) != null)
        	{
        		String samplerName = diffuse.getChild("texture", ns).getAttributeValue("texture");
        		//there should be two params 
        		List<Element> newparams = effect.getChild("profile_COMMON", ns).getChildren("newparam", ns);
        		
        		if (newparams.size() == 2)
        		{
        			int imgURL_idx = 0; 
        			int sampler_idx = 1; 
        			
        			//String sampler_sid = newparams.get(sampler_idx).getAttributeValue("sid");
        			
        			if (!(newparams.get(sampler_idx).getAttributeValue("sid").equals(samplerName)))
        			{
        				imgURL_idx = 1;
        				sampler_idx = 0;
        			}
        			
        			String surfaceName = newparams.get(sampler_idx).getChild("sampler2D", ns).getChild("source",ns).getValue();
    				if (newparams.get(imgURL_idx).getAttributeValue("sid").equals(surfaceName))
    				{
    					this.ColladaTextureURL = map_imgURL.get(newparams.get(imgURL_idx).getChild("surface", ns).getChild("init_from",ns).getValue()).trim();
    				}
    				
    				else throw new Exception ("unhandled file format: can't find imageURL in effect");
        		}
        		
        		else throw new Exception ("unhandled file format: more than 2 newparams in an effect");
        	}        	
    	}
    	
    	catch (Exception e)
    	{
    		System.out.println(e);
    		e.printStackTrace();
    	}
    		
    }
    
    /**
     * Loads the texture(s) associated with this material.
     */
    public void loadTextures(String texPath)
    {
    	if (ColladaTextureURL.equals("")) return;
    	String fullPath = texPath + this.ColladaTextureURL;
    	
        this.ColladaTexture = TextureManager.getTexture(fullPath);

        if (this.ColladaTexture != null) return;  // texture already loaded
        
        try
        {
            ColladaTexture = TextureManager.createTexture(fullPath,
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
        TextureManager.removeTexture(ColladaTexture);
        ColladaTexture = null;
    }
    
    /**
     * Applies this material to the given GL context.
     */
    public void apply(GL gl)
    {
    	gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
       	gl.glEnable(GL.GL_BLEND);
        if (ColladaTexture != null)
        {
        	gl.glEnable(GL.GL_TEXTURE_2D);
        	gl.glEnable(GL.GL_ALPHA_TEST);
        	gl.glDisable(GL.GL_CULL_FACE);
        	gl.glTexParameterf(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_WRAP_S, gl.GL_REPEAT);
        	gl.glTexParameterf(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_WRAP_T, gl.GL_REPEAT);
            ColladaTexture.bind();
        } else {
        	gl.glColor4f(colorText[0], colorText[1], colorText[2], colorText[3]);
        	gl.glColorMaterial(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT_AND_DIFFUSE); //not sure if this line is necessary
        	gl.glEnable(GL.GL_COLOR_MATERIAL);
        	gl.glDisable(GL.GL_TEXTURE_2D);
        }
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
        return ColladaTexture != null;
    }
    
    public float[] getDiffuseColor()
    {
    	return this.colorText;
    }
}
