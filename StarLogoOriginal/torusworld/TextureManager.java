    package torusworld;

import java.util.Map;
import java.util.HashMap;

import com.sun.opengl.util.texture.TextureData;
import com.sun.opengl.util.texture.TextureIO;
import java.net.URL;
import java.io.IOException;


public class TextureManager
{
    private static Map<String, Texture> textures = new HashMap<String, Texture>();

    //TODO New method to create a texture out of texturedata
    //Return null if the texture already exists
    public static Texture createTexture(String name, TextureData _data) {
    	if (textures.containsKey(name)) {
    		System.out.println("Texture Manager already had texture named: " + name);
    		return null;
    	}
    	Texture tex = new Texture(name,TextureIO.newTexture(_data),false);
    	textures.put(name, tex);
    	System.out.println("Added Texture: " + name);
    	return tex;
    }
    
    public static Texture createTexture(String name, URL resourceName)
    {
        return createTexture(name, resourceName, false);
    }
    
    public static Texture createTexture(String name, URL resourceName, boolean mipmap)
    {
        Texture texture = textures.get(name);
        if (texture != null)
        {
            System.out.println("TextureManager.createTexture: attempt to re-load texture " + name + " ignored.");
            return texture;
        }
        
        if (resourceName == null)
        {
            System.out.println("Error loading texture " + name + ": URL is null");
            return null;
        }
        
        try 
        {
            texture = new Texture(name, TextureIO.newTexture(resourceName, mipmap, null), mipmap);
        }
        catch (IOException e)
        {
            System.out.println("Error loading texture " + name + ": " + e);
        }
        
        textures.put(name, texture);
        return texture;
    }

    public static Texture getTexture(String name)
    {
        return textures.get(name);
    }

    public static void removeTexture(String name)
    {
        Texture tex = getTexture(name);
        if (tex == null)
        {
            System.out.println("TextureManager.removeTexture: texture " + name + " not in manager");
            return;
        }
        tex.dispose();
        textures.remove(name);
    }
    
    public static void removeTexture(Texture tex)
    {
        removeTexture(tex.name());
    }

    public static void bindTexture(String name)
    {
        Texture tex = getTexture(name);
        if (tex != null)
            tex.bind();
        else
            System.out.println("Texture " + name + " not in manager");
    }
    
    /**
     * Bind the texture using GL_NEAREST blending (to avoid blending)
     * @param name the name of the texture to bind
     */
    public static void bindTextureGLNearest(String name)
    {
        Texture tex = getTexture(name);
        if (tex != null)
            tex.bindGLNearest();
        else
            System.out.println("Texture " + name + " not in manager");
    }
    
}

