package torusworld.model;

import java.awt.Color;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import javax.swing.JOptionPane;

import torusworld.TorusWorld;
import torusworld.model.kml.KMLAnimationData;
import torusworld.model.kml.KMLModel;
import torusworld.model.md3.Md3AnimationData;
import torusworld.model.md3.Md3Model;
import torusworld.model.obj.OBJAnimationData;
import torusworld.model.obj.OBJModel;

public class ModelManager
{
    private static final boolean DEBUG = false;
    TorusWorld tw;

    public HashMap<String, Model> models;
    public HashMap<String, Model> mediumModels;
    public HashMap<String, Model> lowModels;

    private HashMap<String, Model> simpleModels;

    // Lighting Fields
    private float[] lightAmbient = { 0.5f, 0.5f, 0.5f, 1.0f };
    private float[] lightDiffuse = { 0.5f, 0.5f, 0.5f, 1.0f };
    private float[] lightPosition = { 0.0f, 30.0f, -30.0f, 0.0f };

    // private float[] materialLightAmbient = {0.5f, 0.5f, 0.5f, 1.0f};

    // Material Fields
    // private float[] mSpec = {0.4f, 0.6f, 0.8f, 0.9f};

    // private int numModels = 1;

    float transX = 50.0f;
    float transZ = 50.0f;

    private boolean update = true;

    private boolean lights = false;

    private static ModelManager instance = null;

    private ModelManager()
    {
        super();
        models = new HashMap<String, Model>();
        simpleModels = new HashMap<String, Model>();
        mediumModels = new HashMap<String, Model>();
        lowModels = new HashMap<String, Model>();
        // if (DEBUG) System.out.println("Model Manager initialized.");
    }

    public static ModelManager getInstance()
    {
        if (instance == null)
            instance = new ModelManager();
        return instance;
    }

    public void dispose()
    {
    }

    public void initModels(GL gl)
    {
        // Lights
        gl.glLightfv(GL.GL_LIGHT2, GL.GL_AMBIENT, lightAmbient, 0);
        gl.glLightfv(GL.GL_LIGHT2, GL.GL_DIFFUSE, lightDiffuse, 0);
        gl.glLightfv(GL.GL_LIGHT2, GL.GL_POSITION, lightPosition, 0);

        // Enable Light1
        // gl.glEnable(GL.GL_LIGHTING);
        //		
        // gl.glEnable(GL.GL_COLOR_MATERIAL);
        // gl.glEnable(GL.GL_LIGHT2);

    }

    // public void initModel(GL gl, String category, String model, String skin)
    // {
    // Model initModel = models.get(TorusWorld.getFullName(category, model,
    // skin));
    // initModel.init(tw);
    // }

    public void deinit(GL gl)
    {
        unloadModels(gl);
        gl.glDisable(GL.GL_COLOR_MATERIAL);
        gl.glDisable(GL.GL_LIGHT2);
    }

    public void renderMobile(GL gl, AnimationData animationData, int detail,
            Color color, boolean useSkin)
    {
/*        if (lights)
            gl.glEnable(GL.GL_LIGHT2);*/
        animationData.render(gl, detail, color, useSkin);
/*        if (lights)
            gl.glDisable(GL.GL_LIGHT2);*/
    }

    public AnimationData newAnimationData(String fullName, GL gl, GLU glu)
    {
        Model low = getModel(fullName, Model.LOW_DETAIL, gl, glu);
        if (low instanceof CubeModel)
            return (AnimationData) low;  // for CubeModel
        
        if (low instanceof OBJModel)
        {
            return new OBJAnimationData((OBJModel) low);
        }
        
        if (low instanceof KMLModel)
        {
        	return new KMLAnimationData((KMLModel) low);
        }
        // low must either be null or a Md3Model
        
        Model medium = getModel(fullName, Model.MEDIUM_DETAIL, gl, glu);
        Model high = getModel(fullName, Model.HIGH_DETAIL, gl, glu);
        // Each model is either null or a Md3Model, and at least one of them is not null
        assert low == null || low instanceof Md3Model;
        assert medium == null || medium instanceof Md3Model;
        assert high == null || high instanceof Md3Model;
        assert low != null || medium != null || high != null; 
        
        return new Md3AnimationData((Md3Model) low, (Md3Model) medium,
                (Md3Model) high);
    }

    private Model getModel(String fullName, int detail, GL gl, GLU glu)
    {
        if (!hasLoadedModel(fullName))
            loadModel(fullName, gl, glu);
        Model model;
        model = simpleModels.get(fullName);
        if (model != null)
        {
        	/*
        	if (model instanceof KMLModel)
        	{
        		((KMLModel) model).testColor();
        	}
        	*/
            return model;
        }
        HashMap<String, Model> map;
        if (detail == Model.LOW_DETAIL)
            map = lowModels;
        else if (detail == Model.MEDIUM_DETAIL)
            map = mediumModels;
        else if (detail == Model.HIGH_DETAIL)
            map = models;
        else
        {
            assert false : "Unknown level of detail";
            map = null;
        }
        return map.get(fullName);
    }
    
    /**
     * Tries to load a model given a directory path.
     * If it can't load it, it will load a cube instead.
     * 
     * @param fullName
     * @param gl
     * @param glu
     */
    public void loadModel(String fullName, GL gl, GLU glu)
    {        
        try
        {
            String category = TorusWorld.getCategoryName(fullName);
            String model = TorusWorld.getModelName(fullName);
            String skin = TorusWorld.getSkinName(fullName);
            String loadModelName = TorusWorld.getFullName(category, model,
                                                          skin);
            if (OBJModel.isValid(category, model, skin))
            {
                if (DEBUG)
                    System.out.println("Loading OBJ model " + model);
                OBJModel currentModel = new OBJModel(category, model, skin);
                if (currentModel.init(gl))
                {
                    if (DEBUG)
                        System.out.println("Successfully loaded OBJ model");
                    simpleModels.put(TorusWorld.getFullName(category, model,
                            skin), currentModel);
                    return;
                } else
                    throw new RuntimeException("Failed to load OBJ Model");
            } 
            else if (KMLModel.isValid(category, model, skin))
            {
            	if (DEBUG)
            		System.out.println("Loading KML model " + model);
                    KMLModel currentModel = new KMLModel(category, model, skin);
            	if (currentModel.init(gl))
            	{
            		if (DEBUG)
                        System.out.println("Successfully loaded KML model");
                    simpleModels.put(TorusWorld.getFullName(category, model,
                            skin), currentModel);
                    return;
            	}
            }
            else
            {
                // boolean Medium = false, Low = false;
                // for (int i = 0; i < modelSets.size(); i++) {
                // ModelSet ms = modelSets.get(i);
                // if (model.equalsIgnoreCase(ms.modelName) &&
                // skin.equalsIgnoreCase(ms.skinName) ){
                // Medium = ms.hasMedium;
                // Low = ms.hasLow;
                // }
                // }

                Md3Model currentModelLow = null;
                Md3Model currentModelMedium = null;
                Md3Model currentModel = null;

                currentModel = new Md3Model(category, model, skin, 0);
                if (currentModel.init(gl))
                {
                    if (DEBUG)
                        System.out.println("Successfully loaded MD3 model "
                                + model);
                    if (DEBUG)
                        System.out.println("Model height is "
                                + currentModel.getHeight());
                    // ((Md3Model) currentModel).setScale(new
                    // Vector3f(0.1f,0.1f,0.1f));
                    models.put(loadModelName, currentModel);
                } else
                    throw new RuntimeException("Failed to load MD3 Model");


                if (new File(TorusWorld.directory(category, model) + "/lower_1.md3").exists())
                {
                    // if (new
                    // File(((System.getProperty("application.home")!=null)?System.getProperty("application.home"):System.getProperty("user.dir"))+"/models/"+
                    currentModelMedium = new Md3Model(category, model, skin, 1);
                    if (currentModelMedium.init(gl))
                    {
                        if (DEBUG)
                            System.out
                                    .println("Successfully loaded MD3 medium model "
                                            + model);
                        if (DEBUG)
                            System.out.println("Model height is "
                                    + currentModel.getHeight());
                        // ((Md3Model) currentModelMedium).setScale(new
                        // Vector3f(0.1f,0.1f,0.1f));
                        mediumModels.put(loadModelName, currentModelMedium);
                    } else
                    {
                        System.out.println("Failed to load MD3 medium model " + model);
                    }
                }

                if (new File(TorusWorld.directory(category, model)
                        + "/lower_2.md3").exists())
                {
                    // if (new
                    // File(((System.getProperty("application.home")!=null)?System.getProperty("application.home"):System.getProperty("user.dir"))+"/models/"+model+"/lower_2.md3").exists())
                    // {
                    currentModelLow = new Md3Model(category, model, skin, 2);
                    if (currentModelLow.init(gl))
                    {
                        if (DEBUG)
                            System.out
                                    .println("Successfully loaded MD3 low model "
                                            + model);
                        if (DEBUG)
                            System.out.println("Model height is "
                                    + currentModel.getHeight());
                        // ((Md3Model) currentModelLow).setScale(new
                        // Vector3f(0.1f,0.1f,0.1f));
                        lowModels.put(loadModelName, currentModelLow);
                    } else
                    {
                        System.out.println("Failed to load MD3 low model " + model);
                    }
                }

            }
        } catch (Exception e)
        {
            JOptionPane.showMessageDialog(null, "Could not load model " + fullName + ".\n" + e.getMessage(), "Error loading model", JOptionPane.ERROR_MESSAGE);
            System.out.println(e.getMessage());
            if (DEBUG)
                e.printStackTrace();
            simpleModels.put(fullName, new CubeModel());
        }
    }

    public void unloadModels(GL gl)
    {
        for (Iterator<Model> i = models.values().iterator(); i.hasNext();)
        {
            i.next().deinit(gl);
        }
        models.clear();

        for (Iterator<Model> i = mediumModels.values().iterator(); i.hasNext();)
        {
            i.next().deinit(gl);
        }
        mediumModels.clear();

        for (Iterator<Model> i = lowModels.values().iterator(); i.hasNext();)
        {
            i.next().deinit(gl);
        }
        lowModels.clear();
    }

    public void unloadModel(GL gl, String fullUnloadModelName)
    {
        Model m = models.remove(fullUnloadModelName);
        if (m != null)
        {
            m.deinit(gl);
            m = null;
        }
    }

    public void pausePlay()
    {
        if (update)
            update = false;
        else
            update = true;
    }

    public void toggleLighting()
    {
        if (lights)
            lights = false;
        else
            lights = true;
    }

    public String[] getAvailableAnimations(String category, String model,
            String skin)
    {
        String ModelName = TorusWorld.getFullName(category, model, skin);
        return models.get(ModelName).getAvailableAnimations();
    }

    // public void setAnimation(String category, String model, String skin,
    // String Animation){
    // String ModelName = TorusWorld.getFullName(category, model, skin);
    // Model m;
    // m = models.get(ModelName);
    // if (m!=null) m.setAnimation(Animation);
    // m = mediumModels.get(ModelName);
    // if (m!=null) m.setAnimation(Animation);
    // m = lowModels.get(ModelName);
    // if (m!=null) m.setAnimation(Animation);
    // }

    private boolean hasLoadedModel(String fullName)
    {
        return models.containsKey(fullName)
                || simpleModels.containsKey(fullName);
    }
}
