package torusworld.model.md3;

import java.awt.Color;
import java.util.ArrayList;

import javax.media.opengl.GL;

import torusworld.math.Cylinder;
import torusworld.math.Quaternion;
import torusworld.math.Vector3f;
import torusworld.model.AnimationData;
import torusworld.model.Model;


//import torusworld.TextureManager;

public class Md3AnimationData implements AnimationData
{
    private Md3Model high;
    private Md3Model low;
    private Md3Model medium;

    // One of low, medium, or high. Guaranteed not to be null.
    private Md3Model md3Model;

    boolean isStanding = false;
    
    static final float MIN_FPS = 25; /* this dictates how many interpolation frames we generate */

    private class SingleAnimationData
    {
        int currentAnim;
        int currentFrame;
        int nextFrame;
        float fps = 25;
        float t = 0;
        long lastTime;
    }

    private SingleAnimationData headAnimation = new SingleAnimationData();
    private SingleAnimationData upperAnimation = new SingleAnimationData();
    private SingleAnimationData lowerAnimation = new SingleAnimationData();

    private int animationIndex = 0;
    private float speed = 1.0f;

    public Md3AnimationData(Md3Model newLow, Md3Model newMedium, Md3Model newHigh)
    {
        assert newLow != null || newMedium != null || newHigh != null : "Cannot have animation data without a model";
        low = newLow;
        medium = newMedium;
        high = newHigh;
        if (high != null)
            md3Model = high;
        else if (medium != null)
            md3Model = medium;
        else if (low != null)
            md3Model = low;

        setStanding();
    }

    /**
     * <code>setTorsoAnimation</code> sets the current animation of the upper
     * model. The string that denotes the animation is defined and denoted at
     * the top of this javadoc.
     * 
     */
    public void setTorsoAnimation(String animationName)
    {
        ArrayList<AnimationInfo> animations = md3Model
                .getAnimations(Md3Model.UPPER);
        for (int i = 0; i < animations.size(); i++)
        {
            if (animations.get(i).name.equals(animationName))
            {
                upperAnimation.currentAnim = i;
                upperAnimation.currentFrame = animations.get(i).startFrame;
                return;
            }
        }
    }

    /**
     * <code>setLegsAnimation</code> sets the current animation of the lower
     * model. The string that denotes the animation is defined and denoted at
     * the top of this javadoc.
     * 
     */
    public void setLegsAnimation(String animationName)
    {
        ArrayList<AnimationInfo> animations = md3Model
                .getAnimations(Md3Model.LOWER);
        for (int i = 0; i < animations.size(); i++)
        {
            if (animations.get(i).name.equals(animationName))
            {
                lowerAnimation.currentAnim = i;
                lowerAnimation.currentFrame = animations.get(i).startFrame;
                return;
            }
        }
    }

    private SingleAnimationData getAnimationData(int modelType)
    {
        if (modelType == Md3Model.HEAD)
            return headAnimation;
        else if (modelType == Md3Model.LOWER)
            return lowerAnimation;
        else if (modelType == Md3Model.UPPER)
            return upperAnimation;
        else
        {
            assert false : "Unknown model type";
            return null;
        }
    }

    private void updateAnimation(int modelType)
    {
        SingleAnimationData animData = getAnimationData(modelType);

        int startFrame = 0;
        int endFrame = 1;

        ArrayList<AnimationInfo> animations = md3Model.getAnimations(modelType);

        if (animations != null && animations.size() > 0)
        {
            // get the current animation information
            AnimationInfo animInfo = animations.get(animData.currentAnim);

            if (md3Model.getAnimations(modelType).size() != 0)
            {
                startFrame = animInfo.startFrame;
                endFrame = animInfo.endFrame;
            }

            // set the next frame.
            animData.nextFrame = (animData.currentFrame + 1) % endFrame;

            // if needed, loop the animation.
            if (animData.nextFrame == 0)
                animData.nextFrame = startFrame;

            // interpolate based on the frame rate.
            setCurrentTime(modelType);
        }
    }

    private int getType(Md3Model whichMd3Model, Md3SubModel model)
    {
        if (whichMd3Model.getSubModel(Md3Model.UPPER) == model)
            return Md3Model.UPPER;
        if (whichMd3Model.getSubModel(Md3Model.LOWER) == model)
            return Md3Model.LOWER;
        if (whichMd3Model.getSubModel(Md3Model.HEAD) == model)
            return Md3Model.HEAD;
        /*else if (whichMd3Model.getModel(Md3Model.WEAPON) == model)
            return Md3Model.WEAPON;*/
        assert false : "Could not find model";
        return -1;
    }

    /**
     * <code>drawLink</code> draws a link of the model. As the model is
     * rendered its link or child is then rendered, which contains any matrix
     * translation and/or rotations so the child correctly "sits" on the parent.
     */ 
    
    static float[] finalMatrix = new float[16];
    private void drawLink(GL gl, Md3Model whichMd3Model, Md3SubModel model)
    {
        renderModel(gl, whichMd3Model, model);

        Quaternion quat = new Quaternion();
        Quaternion nextQuat = new Quaternion();
        Quaternion interpolatedQuat = new Quaternion();
        float[] matrix;
        float[] nextMatrix;

        SingleAnimationData animData = getAnimationData(getType(whichMd3Model,
                model));

        // render each tag
        for (int i = 0; i < model.header.numTags; i++)
        if (model.Links[i] != null)
        {
            // interpolated between the two positions.
            Vector3f oldPosition = model.Tags[animData.currentFrame * model.header.numTags + i].position;

            Vector3f nextPosition = model.Tags[animData.nextFrame * model.header.numTags + i].position;
            
            // interpolate via p(t) = p0 + t(p1 - p0)
            Vector3f position = new Vector3f();
            position.x = oldPosition.x + animData.t * (nextPosition.x - oldPosition.x);
            position.y = oldPosition.y + animData.t * (nextPosition.y - oldPosition.y);
            position.z = oldPosition.z + animData.t * (nextPosition.z - oldPosition.z);

            // interpolate the rotations
            matrix = model.Tags[animData.currentFrame * model.header.numTags + i].rotation;

            nextMatrix = model.Tags[animData.nextFrame * model.header.numTags + i].rotation;

            quat.fromMatrix(matrix, 3);
            nextQuat.fromMatrix(nextMatrix, 3);

            // slerp to interpolate
            interpolatedQuat = Quaternion.slerp(quat, nextQuat, animData.t);
            interpolatedQuat.toMatrix(finalMatrix);
            finalMatrix[12] = position.x;
            finalMatrix[13] = position.y;
            finalMatrix[14] = position.z;

            // render the model
            gl.glPushMatrix();

            gl.glMultMatrixf(finalMatrix, 0);
            // render the children
            drawLink(gl, whichMd3Model, model.Links[i]);

            gl.glPopMatrix();
        }
    }

    /**
     * <code>setCurrentTime</code> uses the system clock to set the time for
     * animation interpolation.
     * 
     * @param model
     *            the model to set the time for.
     * @modelguid {F0F1A9A5-0FEF-4477-A4DA-01DDFA2B9167}
     */
    private void setCurrentTime(int modelType)
    {
        SingleAnimationData animData = getAnimationData(modelType);

        if (md3Model.getAnimations(modelType).size() == 0)
            return;

        long time = System.currentTimeMillis();
        // the number of milliseconds between the current time and the last
        // time.
        long elapsedTime = time - animData.lastTime;

        animData.fps = md3Model.getAnimations(modelType).get(animData.currentAnim).framesPerSecond;
        int animationSpeed = (int) ((float) animData.fps * speed);

        // Correct for manual changes in the system clock
        if (elapsedTime < 0)
        {
            animData.lastTime = time;
            elapsedTime = 0;
        }

        // check to see if we should go to the next frame
        if (elapsedTime >= (1000.0f / animationSpeed))
        {
            elapsedTime -= (1000.0f / animationSpeed);
            animData.currentFrame = animData.nextFrame;
            animData.lastTime = time;
        }

        // find the ratio between the first and second frame.
        float t = elapsedTime / (1000f / animationSpeed);
        if (t > 1.f) t = 1.f;
        animData.t = t;
    }


    private void renderModel(GL gl, Md3Model whichMd3Model, Md3SubModel model)
    {
        if (model.header == null)
            return;

        SingleAnimationData animData = getAnimationData(getType(whichMd3Model, model));

        model.renderFrame(gl, animData.currentFrame, animData.nextFrame, animData.t, (int) Math.floor(MIN_FPS / animData.fps) );
        
    }

    public String nextAnimation()
    {
        animationIndex++;
        if (animationIndex > md3Model.getAnimations().size() - 1)
            animationIndex = 0;
        setLegsAnimation(md3Model.getAnimations().get(animationIndex).legsAnimation);
        setTorsoAnimation(md3Model.getAnimations().get(animationIndex).torsoAnimation);
        return new String("Animation Sequence : " + animationIndex
                + " Torso : "
                + md3Model.getAnimations().get(animationIndex).torsoAnimation
                + ", Legs : "
                + md3Model.getAnimations().get(animationIndex).legsAnimation);
    }

    public String prevAnimation()
    {
        animationIndex--;
        if (animationIndex < 0)
            animationIndex = md3Model.getAnimations().size() - 1;
        setLegsAnimation(md3Model.getAnimations().get(animationIndex).legsAnimation);
        setTorsoAnimation(md3Model.getAnimations().get(animationIndex).torsoAnimation);
        return new String("Animation Sequence : " + animationIndex
                + " Torso : "
                + md3Model.getAnimations().get(animationIndex).torsoAnimation
                + ", Legs : "
                + md3Model.getAnimations().get(animationIndex).legsAnimation);
    }

    public String getCurrentAnimation()
    {
        return new String("Animation Sequence : " + animationIndex
                + " Torso : "
                + md3Model.getAnimations().get(animationIndex).torsoAnimation
                + ", Legs : "
                + md3Model.getAnimations().get(animationIndex).legsAnimation);
    }

    public void setAnimation(String animation)
    {
        String torso = animation.substring(0, animation.lastIndexOf('-') - 1);
        String legs = animation.substring(animation.lastIndexOf('-') + 1, animation.length());

        setLegsAnimation(legs);
        setTorsoAnimation(torso);
        ArrayList<AnimationSet> animations = md3Model.getAnimations();

        for (int i = 0; i < animations.size(); i++)
            if (animations.get(i).legsAnimation.equalsIgnoreCase(legs) && 
                animations.get(i).torsoAnimation.equalsIgnoreCase(torso))
                animationIndex = i;
    }

    /**
     * <code>render</code> handles rendering the model to the screen. This
     * occurs recursively starting with the legs, then torso and lastly the
     * head. Due to the make up of the MD3 vertices, culling is turned to front.
     * 
     */
    public void render(GL gl, int detail, Color color, boolean useSkin)
    {
        Md3Model whichMd3Model = high;

        assert (detail == Model.LOW_DETAIL || detail == Model.MEDIUM_DETAIL || detail == Model.HIGH_DETAIL) : "Unknown level of detail";

        if (whichMd3Model == null
                || (detail != Model.HIGH_DETAIL && medium != null))
            whichMd3Model = medium;
        if (whichMd3Model == null
                || (detail == Model.LOW_DETAIL && low != null))
            whichMd3Model = low;

        /* Suppose BLENDING off, ALPHATEST off, BACKCULL on (CCW) */
        if (md3Model.getBlending())
            gl.glEnable(GL.GL_BLEND);
        if (md3Model.getAlphaTest())
            gl.glEnable(GL.GL_ALPHA_TEST);
        if (!md3Model.getBackCull())
            gl.glDisable(GL.GL_CULL_FACE);
        else
            gl.glFrontFace(GL.GL_CW);

        // scale by a desired factor
        // gl.glScalef(scale.x, scale.y, scale.z);
        
        if (!md3Model.getUseColor() && useSkin)
            gl.glColor4f(1.f, 1.f, 1.f, (float) (color.getAlpha()/255.0));
        else
        {
            if (useSkin)
                gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_DECAL);
            gl.glColor4fv(color.getRGBComponents(null), 0);
        }
        
        if (useSkin) {
        	gl.glEnable(GL.GL_BLEND);
            gl.glEnable(GL.GL_TEXTURE_2D);
        }
        else {
            gl.glDisable(GL.GL_TEXTURE_2D);
            gl.glEnable(GL.GL_BLEND);
        }


        // start rendering with the legs first.
        drawLink(gl, whichMd3Model, whichMd3Model.getSubModel(Md3Model.LOWER));

        // set culling back to GL.GL_BACK
        if (md3Model.getBlending())
            gl.glDisable(GL.GL_BLEND);
        if (md3Model.getAlphaTest())
            gl.glDisable(GL.GL_ALPHA_TEST);
        if (!md3Model.getBackCull())
            gl.glEnable(GL.GL_CULL_FACE);
        else
            gl.glFrontFace(GL.GL_CCW);
        if (md3Model.getUseColor() && useSkin)
            gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE,
                    GL.GL_MODULATE);
    }

    public void update()
    {
        updateAnimation(Md3Model.LOWER);
        updateAnimation(Md3Model.HEAD);
        updateAnimation(Md3Model.UPPER);
    }

    public void setStanding()
    {
        if (!isStanding)
        {
            isStanding = true;
            setAnimation(md3Model.standingAnimation());
        }
    }

    public void setMoving()
    {
        if (isStanding)
        {
            isStanding = false;
            setAnimation(md3Model.movingAnimation());
        }
    }

    public Cylinder getBoundingCylinder()
    {
        return md3Model.getBoundingCylinder(null, isStanding, !isStanding);
    }

    public float getAnimationSpeed()
    {
        return speed;
    }

    public void setAnimationSpeed(float newSpeed)
    {
        speed = newSpeed;
    }
}
