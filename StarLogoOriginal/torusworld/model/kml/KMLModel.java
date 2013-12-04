package torusworld.model.kml;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.ArrayList;

import javax.media.opengl.GL;

import torusworld.TorusWorld;
import torusworld.math.Cylinder;
import torusworld.math.Matrix4f;
import torusworld.math.Vector3f;
import torusworld.model.Model;
import torusworld.model.kml.*;
import utility.Tokenizer;

public class KMLModel extends Model {
	 	
	private String category;
 	private String modelName;
 	
    private Cylinder boundingCyl;
    private ArrayList<Polygon3D> polygons = new ArrayList<Polygon3D>();
    private int callLists = 0;
    
    private boolean initialized = false;
    private KMLReader.COLLADALibrary ColladaLib = null;
    
    private ArrayList<ColladaAnimation> animations = null;
    
    // These default values are duplicated in readConfigFile
    private boolean blending = false, alphatest = false, backcull = true;
            

    public KMLModel(String newCategory, String newModelName, String newSkinName)
    {
        category = newCategory;
        modelName = newModelName;
    }

    public String getModelName()
    {
        return modelName;
    }

    public String getSkinName()
    {
        return category;
    }

    public String[] getAvailableAnimations()
    {
        return new String[0];
    }

    private static String fileNameKML(String category, String modelName)
    {
    	return TorusWorld.directory(category, modelName) + "/doc.kml";
    }
    
//    private static String fileNameDAE(String category, String modelName)
//    {
//        return TorusWorld.directory(category, modelName) + "/models/" + modelName + ".dae";
//    }

    //Returns whether an OBJ file for a model with the name modelName exists.  Does not check the file itself for validity.
    public static boolean isValid(String category, String modelName, String skinName)
    {
        String fileName = fileNameKML(category, modelName);
        return new File(fileName).exists();
    }

    public boolean getBlending()
    {
        return blending;
    }

    public boolean getAlphaTest()
    {
        return alphatest;
    }

    public boolean getBackCull()
    {
        return backcull;
    }


    public boolean init(GL gl)
    {
    	
        String prefix = TorusWorld.directory(category, modelName) + "/";
        
        try
        {
        	Matrix4f transformation = readConfigFile(prefix + "model.slb");
            String fileName = fileNameKML(category, modelName);
            KMLReader kmlReader = new KMLReader();
            
            // read the model file
            if (!kmlReader.read(fileName, prefix)) 
                return false;
            
            // attempt to center and scale the model
            kmlReader.prepModel();
            
            // read the model's polygons
            polygons = kmlReader.getPolygons();
            
            if (!kmlReader.textureLibrary.getTextureLib())
            	return false;
            this.ColladaLib = kmlReader.textureLibrary;
            
            kmlReader.textureLibrary.loadTextures();
            
            transform(transformation);
            calcCollisionData();
        } catch (Exception e)
        {
        	e.printStackTrace();
            return false;
        } 
        
        callLists = gl.glGenLists(ColladaLib == null ? 1 : 2);
        
        gl.glNewList(callLists + 0, GL.GL_COMPILE);
        preRender(gl, false);
        gl.glEndList();
        
        //TO-DO: new callList if texture library isn't null
        
        
        if (ColladaLib != null)
        {
            gl.glNewList(callLists + 1, GL.GL_COMPILE);
            preRender(gl, true);
            gl.glEndList();
        }
		
        
        initialized = true;
        return true;

        }
    
    public void deinit(GL gl)
    {
        initialized = false;
      
        ColladaLib.unloadTextures();
        gl.glDeleteLists(callLists, ColladaLib == null ? 1 : 2);
    }
    
    private void transform(Matrix4f transformation)
    {
        for (int i = 0; i < polygons.size(); i++)
        {
            Polygon3D p = polygons.get(i);
            for (int j = 0; j < 3; j++)
            {
                Vector3f v = new Vector3f(p.vertices[3 * j], p.vertices[3 * j + 1],
                                          p.vertices[3 * j + 2]);
                transformation.transform(v);
                p.vertices[3 * j] = v.x;
                p.vertices[3 * j + 1] = v.y;
                p.vertices[3 * j + 2] = v.z;

                if (p.normal != null)
                {
                    v = new Vector3f(p.normal[3 * j + 0], p.normal[3 * j + 1], p.normal[3 * j + 2]);
                    transformation.rotate(v);
                    v.normalize();
                    p.normal[3 * j + 0] = v.x;
                    p.normal[3 * j + 1] = v.y;
                    p.normal[3 * j + 2] = v.z;
                }
            }

            if (p.faceNormal != null)
            {
                Vector3f normal = new Vector3f(p.faceNormal[0], p.faceNormal[1], p.faceNormal[2]);
                transformation.rotate(normal);
                normal.normalize();
                p.faceNormal[0] = normal.x;
                p.faceNormal[1] = normal.y;
                p.faceNormal[2] = normal.z;
            }
        }
    }

    private void calcCollisionData()
    {
        float boundingRadiusSquared = 0.0f;
        float boundingUp = -1e10f, boundingDown = 1e10f;

        for (int i = 0; i < polygons.size(); i++)
        {
            Polygon3D p = polygons.get(i);

            float distanceSquared = p.vertices[0] * p.vertices[0] + p.vertices[2] * p.vertices[2];

            if (distanceSquared > boundingRadiusSquared)
                boundingRadiusSquared = distanceSquared;

            distanceSquared = p.vertices[3] * p.vertices[3] + p.vertices[5] * p.vertices[5];

            if (distanceSquared > boundingRadiusSquared)
                boundingRadiusSquared = distanceSquared;

            distanceSquared = p.vertices[6] * p.vertices[6] + p.vertices[8] * p.vertices[8];

            if (distanceSquared > boundingRadiusSquared)
                boundingRadiusSquared = distanceSquared;

            // calculate lower and upper bounds on heights
            if (p.vertices[1] < boundingDown)
                boundingDown = p.vertices[1];
            if (p.vertices[1] > boundingUp)
                boundingUp = p.vertices[1];
            if (p.vertices[4] < boundingDown)
                boundingDown = p.vertices[4];
            if (p.vertices[4] > boundingUp)
                boundingUp = p.vertices[4];
            if (p.vertices[7] < boundingDown)
                boundingDown = p.vertices[7];
            if (p.vertices[7] > boundingUp)
                boundingUp = p.vertices[7];
        }

        boundingCyl = new Cylinder((float) (Math.sqrt(boundingRadiusSquared)), boundingUp,
                                   boundingDown);
    }

    public Cylinder getBoundingCylinder()
    {
        return boundingCyl;
    }
    
  //Point and transformations commands are executed in the order that they appear
    //Indicated transformations are performed load-time
    private Matrix4f readConfigFile(String fileName)
    {
        Matrix4f transformation = new Matrix4f();

        File file = new File(fileName);
        if (!file.exists())
            return transformation;

        try
        {
            LineNumberReader lnr = new LineNumberReader(new FileReader(file));
            String line;

            //Check the first non-blank line
            while ((line = lnr.readLine()) != null && line.trim().equals(""))
                ;
            if (!line.trim().startsWith("StarLogoTNG"))
                return transformation;

            //Get the version number
            int version = -1;
            while ((line = lnr.readLine()) != null)
            {
                try
                {
                    line = line.trim();
                    if (!line.equals(""))
                    {
                        version = Integer.parseInt(line);
                        break;
                    }
                } catch (NumberFormatException e)
                {

                }
            }
            if (line == null || version < 200)
                return transformation;

            //Ignore the language
            while ((line = lnr.readLine()) != null && line.trim().equals(""))
                ;

            while ((line = lnr.readLine()) != null)
            {
                line = line.trim();
                Tokenizer tokenizer = new Tokenizer(line);
                ArrayList<String> items = tokenizer.tokenize();
                
                if (items.size() == 1)
                {
                    if (items.get(0).equalsIgnoreCase("blending"))
                        blending = true;
                    else if (items.get(0).equalsIgnoreCase("alphatest"))
                        alphatest = true;
                    else if (items.get(0).equalsIgnoreCase("nobackcull"))
                        backcull = false;
                }
                
                else if (items.size() >= 2)
                {
                    try
                    {
                        if (items.get(0).equalsIgnoreCase("moving-animation-torso")
                            || items.get(0).equalsIgnoreCase("moving-animation-legs")
                            || items.get(0).equalsIgnoreCase("standing-animation-torso")
                            || items.get(0).equalsIgnoreCase("standing-animation-legs"))
                            System.out.println("Model configuration command " + items.get(0)
                                               + " not applicable to OBJ models");
                        
                        else if (items.get(0).equalsIgnoreCase("rotate-z"))
                        {
                            float angle = Float.parseFloat(items.get(1));
                            transformation.rotateX(angle, false);
                        } else if (items.get(0).equalsIgnoreCase("rotate-y"))
                        {
                            float angle = Float.parseFloat(items.get(1));
                            transformation.rotateY(angle, false);
                        } else if (items.get(0).equalsIgnoreCase("rotate-x"))
                        {
                            float angle = Float.parseFloat(items.get(1));
                            transformation.rotateZ(angle, false);
                        } else if (items.get(0).equalsIgnoreCase("translate"))
                        {
                            if (items.size() >= 4)
                            {
                                float x = Float.parseFloat(items.get(1));
                                float y = Float.parseFloat(items.get(2));
                                float z = Float.parseFloat(items.get(3));
                                transformation.translate(x, y, z);
                            }
                        } else if (items.get(0).equalsIgnoreCase("scale"))
                        {
                            float x, y, z;
                            if (items.size() == 2)
                            {
                                x = y = z = Float.parseFloat(items.get(1));
                            } else if (items.size() >= 4)
                            {
                                x = Float.parseFloat(items.get(1));
                                y = Float.parseFloat(items.get(2));
                                z = Float.parseFloat(items.get(3));
                            } else
                                continue;
                            transformation.scale(x, y, z);
                        } else
                            System.out.println("Unrecognized model configuration command: "
                                               + items.get(0));
                    } catch (NumberFormatException e)
                    {

                    }
                } else if (items.size() >= 1)
                    System.out.println("Unrecognized model configuration command: " + items.get(0));
            }
            return transformation;
        } catch (Exception e)
        {
            e.printStackTrace();
            return transformation;
        }
    }
    
    /**
     * Renders the model using glBegin/glEnd calls.
     * 
     */
    private void preRender(GL gl, Boolean useSkin)
    {
        String currentMatName = "";
        
        if (polygons.size() == 0)
            System.out.println("Warning: empty model " + modelName);

	        gl.glDisable(GL.GL_TEXTURE_2D);
	        gl.glBegin(GL.GL_TRIANGLES);
	        
	        //gl.glColor3f(1.0f, 1.0f, 1.0f);
	        for (int i = 0; i < polygons.size(); i++)
	        {
	            Polygon3D p = polygons.get(i);
	            
	            if (useSkin && ColladaLib != null && !p.materialName.equals(currentMatName))
	            {
	                gl.glEnd();
	                ColladaLib.applyMaterial(p.materialName, gl);
	                gl.glBegin(GL.GL_TRIANGLES);
	                
	                currentMatName = p.materialName;
	            }
	            
	            
	            if (p.faceNormal != null && p.normal == null)
	                gl.glNormal3f(p.faceNormal[0], p.faceNormal[1], p.faceNormal[2]);

	            for (int j = 0; j < 3; j++)
	            {
	                if (p.normal != null)
	                    gl.glNormal3f(p.normal[j * 3], p.normal[j * 3 + 1], p.normal[j * 3 + 2]);
	                if (p.texture != null && useSkin)
	                    gl.glTexCoord2f(p.texture[j * 2], p.texture[j * 2 + 1]);
	                gl.glVertex3f(p.vertices[j * 3], p.vertices[j * 3 + 1], p.vertices[j * 3 + 2]);
	                
	            }
	        }
	        gl.glEnd();
	    }

	    public void render(GL gl, Color color, boolean useSkin)
	    {
	        if (!initialized) return;
	        
	        gl.glEnable(GL.GL_BLEND);
	       
	        /* Suppose BLENDING off, ALPHATEST off, BACKCULL on (CCW) */
	        /*
	        if (this.getBlending())
	            gl.glEnable(GL.GL_BLEND);
	        if (this.getAlphaTest())
	            gl.glEnable(GL.GL_ALPHA_TEST);
	        if (!this.getBackCull())
	            gl.glDisable(GL.GL_CULL_FACE);
	        else
	            gl.glFrontFace(GL.GL_CW);
			*/
	        //
	        
	        
	        gl.glColor4fv(color.getRGBComponents(null), 0);
	        
	        gl.glCallList(callLists + ((useSkin && ColladaLib != null) ? 1 : 0));
	        
	        gl.glEnable(GL.GL_TEXTURE_2D);
	    }

	    /*
	    public void testColor()
	    {
	    	float[] test = this.ColladaLib.getMaterial("material_0_16ID").getDiffuseColor();
	    	System.out.println(test[0] + "" +  test[1] + "" + test[2] + "" + test[3]);
	    }
	    */
}
