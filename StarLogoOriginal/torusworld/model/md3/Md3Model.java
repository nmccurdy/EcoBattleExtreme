package torusworld.model.md3;

//import net.java.games.gluegen.runtime.*; 
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;

import javax.media.opengl.GL;

import torusworld.TorusWorld;
import torusworld.math.Cylinder;
import torusworld.math.Matrix4f;
import torusworld.math.Vector3f;
import torusworld.model.Model;
import utility.Tokenizer;

//import java.awt.Color;
//import net.java.games.jogl.util.*;

/** @modelguid {C763D1ED-D110-4094-B2E7-74632DF9F6FA} */
public class Md3Model extends Model
{
    public static final int LOWER = 0;
    public static final int UPPER = 1;
    public static final int HEAD = 2;
    // public static final int WEAPON = 3;

    private String category;
    private String modelName;
    private String skinName;

    // model structure
    private Md3SubModel head;
    private Md3SubModel upper;
    private Md3SubModel lower;
    // private Md3SubModel weaponModel;

    private static final int START_TORSO_ANIMATION = 6; // DO NOT CHANGE !!!!
    private static final int START_LEGS_ANIMATION = 13; // REQUIRED FOR BONES
                                                        // ALIGNMENT
    private static final int MAX_ANIMATIONS = 25;

    private String suffix;

    HashSet<Md3SubModel> models = new HashSet<Md3SubModel>();

    // HashMap from animation name to the bounding cylinder
    // It is initially empty and populated with a new mapping only when one is
    // initially computed, which occurs on demand
    private HashMap<String, Cylinder> animationToCylinder = new HashMap<String, Cylinder>();

    Cylinder boundingCylStanding, boundingCylMoving;

    private ArrayList<AnimationSet> animations = new ArrayList<AnimationSet>();

    // These default values are duplicated in readConfigFile
    private String standingAnimation = "TORSO_STAND2-LEGS_IDLE";
    private String movingAnimation = "TORSO_STAND2-LEGS_WALK";

    private boolean blending = false, alphatest = false, backcull = true,
            usecolor = false;

    private float height;

    public Md3Model(String category, String modelName, String skintype,
            int resolution)
    {
        assert category != null && modelName != null && skintype != null;

        setModelShape(category, modelName, skintype);

        head = new Md3SubModel();
        upper = new Md3SubModel();
        lower = new Md3SubModel();
        //weaponModel = new Model3D();

        switch (resolution)
        {
            case 1: suffix = "_1";
                    break;
            case 2: suffix = "_2";
                    break;
            default: suffix = "";
                    break;
        }

    }

    public void setModelShape(String category, String modelName, String skintype)
    {
        this.category = category;
        this.modelName = modelName;
        this.skinName = skintype;
    }

    public Md3SubModel getSubModel(int part)
    {
        if (part == LOWER) return lower;
        if (part == UPPER) return upper;
        if (part == HEAD) return head;
        System.out.println("Md3Model: getSubModel called with invalid part "+part);
        return null;
    }

    public boolean init(GL gl)
    {
        return loadModel();
    }

    public void setTexture(String texture)
    {
        // nothing
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

    public boolean getUseColor()
    {
        return usecolor;
    }

    public boolean loadModel()
    {
        // build file names.
        // String prefix = path + "md3-" + model + "/models/players/" + model;
        String prefix = TorusWorld.directory(category, modelName) + "/";
        String lowerModel = prefix + "lower" + suffix + ".md3";
        String upperModel = prefix + "upper" + suffix + ".md3";
        String headModel = prefix + "head" + suffix + ".md3";
        String lowerSkin = prefix + "lower" + suffix + "_" + skinName + ".skin";
        String upperSkin = prefix + "upper" + suffix + "_" + skinName + ".skin";
        String headSkin = prefix + "head" + suffix + "_" + skinName + ".skin";
        String fullName = TorusWorld.getFullName(category, modelName, skinName);

        Matrix4f transformation = readConfigFile(prefix + "model.slb");

        if (!head.loadMD3(headModel, transformation) ||
            !upper.loadMD3(upperModel, transformation) ||
            !lower.loadMD3(lowerModel, transformation))
        {
            System.out.println("Unable to load one of the HEAD/UPPER/LOWER model of " + fullName);
            return false;
        }

        if (!lower.loadSkin(lowerSkin, prefix) ||
            !upper.loadSkin(upperSkin, prefix) ||
            !head.loadSkin(headSkin, prefix))
        {
            System.out.println("Unable to load the HEAD/UPPER/LOWER skin!");
            return false;
        }

        // Add the path and file name prefix to the animation.cfg file
        String configFile = prefix + "animation" + suffix + ".cfg";

        // Load the animation config file
        if (!loadAnimations(configFile))
        {
            System.out.println("Unable to load the Animation Config File!");
            return false;
        }

        // Link the lower body to the upper body
        lower.link(upper, "tag_torso");

        // Link the upper body to the head
        upper.link(head, "tag_head");

        // Compute the radius of the bounding sphere for when the model in
        // moving and standing
        boundingCylStanding = getBoundingCylinder(standingAnimation, false, false);
        boundingCylMoving = getBoundingCylinder(movingAnimation, false, false);

        return true;
    }

    /**
     * Point and transformations commands are executed in the order that they
     * appear. Indicated transformations are performed at load time.
     * 
     * @return The transformation indicated by the configuration file
     */
    private Matrix4f readConfigFile(String fileName)
    {
        Matrix4f transformation = new Matrix4f();

        // MD3 has Z up, so remedy this
        transformation.rotateX(-90.0f, false);
        transformation.rotateY(-90.0f, false);

        // Shrink MD3 models
        transformation.scale(0.1f, 0.1f, 0.1f);

        // Move the MD3 models up so that most models aren't sinking into the
        // ground
        transformation.translate(0.0f, 0.2f, 0.0f);

        File file = new File(fileName);
        if (!file.exists())
            return transformation;

        try
        {
            LineNumberReader lnr = new LineNumberReader(new FileReader(file));
            String line;

            // Check the first non-blank line
            while ((line = lnr.readLine()) != null && line.trim().equals(""))
                ;
            if (!line.trim().startsWith("StarLogoTNG"))
                return transformation;

            // Get the version number
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

            // Ignore the language
            while ((line = lnr.readLine()) != null && line.trim().equals(""))
                ;

            // These default values are duplicated at the top of the class
            String standingAnimationTorso = "TORSO_STAND2";
            String standingAnimationLegs = "LEGS_IDLE";
            String movingAnimationTorso = "TORSO_STAND2";
            String movingAnimationLegs = "LEGS_WALK";
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
                    else if (items.get(0).equalsIgnoreCase("usecolor"))
                        usecolor = true;
                } else if (items.size() >= 2)
                {
                    try
                    {
                        if (items.get(0).equalsIgnoreCase(
                                "moving-animation-torso"))
                            movingAnimationTorso = items.get(1);
                        else if (items.get(0).equalsIgnoreCase(
                                "moving-animation-legs"))
                            movingAnimationLegs = items.get(1);
                        else if (items.get(0).equalsIgnoreCase(
                                "standing-animation-torso"))
                            standingAnimationTorso = items.get(1);
                        else if (items.get(0).equalsIgnoreCase(
                                "standing-animation-legs"))
                            standingAnimationLegs = items.get(1);
                        else if (items.get(0).equalsIgnoreCase("rotate-x"))
                        {
                            float angle = Float.parseFloat(items.get(1));
                            transformation.rotateX(angle, false);
                        } else if (items.get(0).equalsIgnoreCase("rotate-y"))
                        {
                            float angle = Float.parseFloat(items.get(1));
                            transformation.rotateY(angle, false);
                        } else if (items.get(0).equalsIgnoreCase("rotate-z"))
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
                            System.out
                                    .println("Unrecognized model configuration command: "
                                            + items.get(0));
                    } catch (NumberFormatException e)
                    {

                    }
                } else if (items.size() >= 1)
                    System.out
                            .println("Unrecognized model configuration command: "
                                    + items.get(0));
            }
            movingAnimation = movingAnimationTorso + "-" + movingAnimationLegs;
            standingAnimation = standingAnimationTorso + "-" + standingAnimationLegs;
            return transformation;
        } catch (Exception e)
        {
            e.printStackTrace();
            return transformation;
        }
    }

    // Computes and returns the radius of the bounding sphere of the current
    // animation
    // This should only be called by getBoundingRadius
    private Cylinder computeBoundingCylinder(String animation)
    {
        String torso = animation.substring(0, animation.lastIndexOf('-') - 1);
        String legs = animation.substring(animation.lastIndexOf('-') + 1, animation.length());
        return computeBoundingCylinderForLink(torso, legs, lower, new Matrix4f());
    }

    private Cylinder computeBoundingCylinderForLink(String torsoAnimation, String legsAnimation,
                                                    Md3SubModel model, Matrix4f transform)
    {
        String modelAnimation = "";
        if (model == lower) modelAnimation = legsAnimation;
        if (model == upper) modelAnimation = torsoAnimation;

        /*String name = "?";
        if (model == lower) name = "lower";
        if (model == upper) name = "upper";
        if (model == head) name = "head";
        System.out.println("computeBoundingCylinderForLink " + name);*/

        int startFrame = 0, endFrame = 1;
        if (model.animations.size() > 0 && modelAnimation != "")
        {
            AnimationInfo animInfo = null;
            int i;
            for (i = 0; i < model.animations.size(); i++)
            {
                animInfo = model.animations.get(i);
                if (animInfo.name.equals(modelAnimation))
                    break;
            }
            assert i < model.animations.size() : "Could not find animation " + modelAnimation;
            startFrame = animInfo.startFrame;
            endFrame = animInfo.endFrame;
        }

        float radius = 0, top = -1e10f, bottom = 1e10f;

        // it's too slow to look at all the frames, so limit the number of frames we look at.
        int step = 1;
        while ((endFrame - startFrame) / step > 10) 
            step *= 10;
        
        for (int j = startFrame; j < endFrame; j += step)
        {
            Cylinder c = model.computeBoundingCylinderForFrame(transform, j);
            radius = Math.max(radius, c.radius);
            top = Math.max(top, c.top);
            bottom = Math.min(bottom, c.bottom);

            for (int k = 0; k < model.header.numTags; k++)
            if (model.Links[k] != null)
            {
                float[] matrix = model.Tags[j * model.header.numTags + k].rotation;
                float[][] matrix4 = new float[4][4];
                
                for (int y = 0; y < 3; y++)
                    for (int x = 0; x < 3; x++)
                        matrix4[y][x] = matrix[y * 3 + x];

                Vector3f translate = model.Tags[j * model.header.numTags + k].position;
                matrix4[0][3] = translate.x;
                matrix4[1][3] = translate.y;
                matrix4[2][3] = translate.z;
                matrix4[3][3] = 1.0f;

                Matrix4f newTransform = transform.mult(new Matrix4f(matrix4));

                c = computeBoundingCylinderForLink(torsoAnimation, legsAnimation, model.Links[k], newTransform);
                radius = Math.max(radius, c.radius);
                top = Math.max(top, c.top);
                bottom = Math.min(bottom, c.bottom);
            }
        }

        return new Cylinder(radius, top, bottom);
    }


    private boolean loadAnimations(String configFile)
    {
        AnimationInfo[] animations = new AnimationInfo[MAX_ANIMATIONS];

        InputStream is = null;
        
        try
        {
            is = new FileInputStream(configFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            String line = "";
            int nr = 0;
            int torsoOffset = 0;

            StringTokenizer tokenizer;

            while ((line = reader.readLine()) != null)
            {
                // skip blank lines
                if (line.length() == 0 || !Character.isDigit(line.charAt(0)))
                    continue;

                // start parsing the animation information
                tokenizer = new StringTokenizer(line);

                animations[nr] = new AnimationInfo();
                animations[nr].startFrame = Integer.parseInt(tokenizer.nextToken());
                animations[nr].endFrame = animations[nr].startFrame + Integer.parseInt(tokenizer.nextToken());
                animations[nr].loopingFrames = Integer.parseInt(tokenizer.nextToken());
                animations[nr].framesPerSecond = Integer.parseInt(tokenizer.nextToken());

                // set the animation information
                tokenizer.nextToken();
                // set the animation name
                animations[nr].name = tokenizer.nextToken();

                // add the animation to the appropriate model.
                if (line.indexOf("BOTH") >= 0)
                {
                    upper.animations.add(animations[nr]);
                    lower.animations.add(animations[nr]);
                    this.animations.add(new AnimationSet(animations[nr].name, animations[nr].name));
                } else if (line.indexOf("TORSO") >= 0)
                {
                    upper.animations.add(animations[nr]);
                } else if (line.indexOf("LEGS") >= 0)
                {
                    // If the torso offset hasn't been set, set it
                    if (torsoOffset == 0)
                        torsoOffset = animations[START_LEGS_ANIMATION].startFrame -
                                      animations[START_TORSO_ANIMATION].startFrame;

                    animations[nr].startFrame -= torsoOffset;
                    animations[nr].endFrame -= torsoOffset;

                    lower.animations.add(animations[nr]);
                }

                nr++;

            }
        } catch (IOException e)
        {
            throw new RuntimeException("Could not load animations.");
        }

        int i, j;
        for (i = 0; i < lower.animations.size(); i++)
          if (lower.animations.get(i).name.indexOf("BOTH") < 0)
            for (j = 0; j < upper.animations.size(); j++)
              if (upper.animations.get(j).name.indexOf("BOTH") < 0)
                  this.animations.add(new AnimationSet(upper.animations.get(j).name, 
                                                       lower.animations.get(i).name));
        return true;
    }

    public String[] getAvailableAnimations()
    {
        String[] anims = new String[animations.size()];
        for (int i = 0; i < anims.length; i++)
            anims[i] = animations.get(i).torsoAnimation + "-" +
                       animations.get(i).legsAnimation;
        return anims;
    }

    public void deinit(GL gl)
    {
        if (lower != null) lower.unload(gl);
        if (upper != null) upper.unload(gl);
        if (head != null) head.unload(gl);
        
        lower = null;
        upper = null;
        head = null;
    }

    public String getModelName()
    {
        return modelName;
    }

    public String getSkinName()
    {
        return skinName;
    }

    public Cylinder getBoundingCylinder(String animation, boolean isStanding,
            boolean isMoving)
    {
        if (isStanding)
            return boundingCylStanding;
        else if (isMoving)
            return boundingCylMoving;
        else
        {
            Cylinder c = animationToCylinder.get(animation);
            if (c != null)
                return c;
            c = computeBoundingCylinder(animation);
            animationToCylinder.put(animation, c);
            return c;
        }
    }

    public ArrayList<AnimationInfo> getAnimations(int part)
    {
        if (part == LOWER)
            return lower.animations;
        else if (part == UPPER)
            return upper.animations;
        else if (part == HEAD)
            return head.animations;
        
        System.out.println("Md3Model.getAnimations bad part " + part);
        return null;
    }

    public ArrayList<AnimationSet> getAnimations()
    {
        return animations;
    }

    public String standingAnimation()
    {
        return standingAnimation;
    }

    public String movingAnimation()
    {
        return movingAnimation;
    }

    public float getHeight()
    {
        return height;
    }

}
