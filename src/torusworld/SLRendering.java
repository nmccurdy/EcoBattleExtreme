package torusworld;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.prefs.Preferences;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.text.DecimalFormat;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

import com.sun.opengl.util.GLUT;

//import terraineditor.Region;
import torusworld.math.Cylinder;
import torusworld.math.Frustum;
import torusworld.math.Vector3f;
import torusworld.model.Model;


public class SLRendering
{
	public static boolean editingTerrain = false; 
	
    public static final boolean verticalSync = true;
    
    public static final float skyBoxSize = 2000.0f;
    public static final int halfSize = 303 / 2;

    public static float deathFadeAlpha = 0;

    public static boolean showTerrain = true;
    public static boolean showSky = false;
    public static boolean lighting = true;
    
    public static boolean showTerrainSelection = false;
//    public static Region terrainSelection; 
     
    private static boolean showAxes = false;
    private static boolean showCylinders = false;
    
    private static final float[] lightAmbient = {0.4f, 0.4f, 0.4f, 1.0f};
    private static final float[] lightDiffuse = {1.0f, 1.0f, 1.0f, 1.0f};
    private static final float[] lightPosition = {0.866f, 0.5f, 0.0f, 0.0f};

    private static float lowResDistance = 70.0f;
    private static float medResDistance = 30.0f;

    private static float currentFPS = 30.0f;
    private static float currentVMPS = 0.0f;

    private static int countFPS = 0;
    private static int countVMPS = 0;
    private static float lastvmps = 0.0f, curvmps = 0.0f;
    private static long startTime = System.currentTimeMillis();
    private static long lastVMIntervalTime = 0;

    private static GL gl;
    private static GLU glu;
    private static GLUT glut;
        
    private static Perimeter fence;
    private static Sky sky;
    private static Frustum frustum;
    
    
    public static void setGL(GL _gl) { gl = _gl; }
    public static GL getGL() { return gl; }
    public static GLU getGLU() { return glu; }
    public static GLUT getGLUT() { return glut; }
    
    static void init(GL _gl, GLU _glu, GLUT _glut)
    {
        gl = _gl;
        glu = _glu;
        glut = _glut;
        
        fence = new Perimeter(gl);
        sky = new Sky();
        sky.loadTextures();
        
        gl.setSwapInterval(verticalSync ? 1 : 0);
        gl.glEnable(GL.GL_TEXTURE_2D);
        
        // This Will Clear The Background Color To Black
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);
        
        // This Enables the Depth Function
        gl.glClearDepth(1.0);
        gl.glDepthFunc(GL.GL_LEQUAL);
        gl.glEnable(GL.GL_DEPTH_TEST);
        
        gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);
        gl.glEnable(GL.GL_CULL_FACE);
        gl.glFrontFace(GL.GL_CCW);
        gl.glCullFace(GL.GL_BACK);
        
        gl.glColorMaterial(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT_AND_DIFFUSE);
        gl.glEnable(GL.GL_COLOR_MATERIAL);
        
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
        
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
        
        
        gl.glLightfv(GL.GL_LIGHT1, GL.GL_AMBIENT, lightAmbient,0);
        gl.glLightfv(GL.GL_LIGHT1, GL.GL_DIFFUSE, lightDiffuse,0);
        gl.glEnable(GL.GL_LIGHT1);
        
        lighting = true;
        gl.glLoadIdentity();
        
        for (char c = 32; c < 128; c++)
            glutRomanCharWidths[c - 32] = glut.glutStrokeWidth(GLUT.STROKE_ROMAN, c);
        readPreferences();
        Preferences.userRoot().node("StarLogo TNG").node("Rendering").addPreferenceChangeListener(
            new PreferenceChangeListener()
            {
                public void preferenceChange(PreferenceChangeEvent evt)
                {
                    readPreferences();
                }
            });
    }
    
    public static float getCurrentFPS() { return currentFPS; }
    
    
    private static DecimalFormat formatters[] = { new DecimalFormat("0"),
                                                  new DecimalFormat("0.0"),
                                                  new DecimalFormat("0.00")};
    
    public static String fpsString()
    {
        float fps = currentFPS;
        DecimalFormat formatter = formatters[(fps >= 10.f) ? 0 : ((fps >= 1.0f) ? 1 : 2)];
        return formatter.format(fps);
    }

    public static String vmpsString()
    {
        DecimalFormat formatter = formatters[1];
        return formatter.format(currentVMPS);
    }

    public static void updateFence() 
    { 
        fence.update(); 
    }
    
    public static void updateCounters(boolean ranVM)
    {
        long curTime = System.currentTimeMillis();
        
        if (ranVM) countVMPS++;
        
        if (curTime - lastVMIntervalTime >= 800) {
            lastvmps = curvmps;
            curvmps = countVMPS * 1000.0f / (curTime - lastVMIntervalTime);
            currentVMPS = 0.5f * (lastvmps+curvmps);
            lastVMIntervalTime = curTime;
            countVMPS = 0;
        }
        
        countFPS++;
        if(curTime - startTime >= 500) // Average over last 500ms
        {
            // exponentially weigh out old fps
            currentFPS = currentFPS * 0.1f + countFPS * 900.0f / (curTime - startTime);
            startTime = curTime;
            countFPS = 0;
        }
    }
    
    private static void drawAxes(int size) 
    {
        if (!showAxes) return;
        gl.glLineWidth(size);
        gl.glBegin(GL.GL_LINES);
        gl.glColor3f(1.0f, 0.0f, 0.0f);
        gl.glVertex3f(0.0f, 0.0f, 0.0f);
        gl.glVertex3f(size*3.0f, 0.0f, 0.0f);
        
        gl.glColor3f(1.0f, 1.0f, 0.0f);
        gl.glVertex3f(0.0f, 0.0f, 0.0f);
        gl.glVertex3f(0.0f, size*3.0f, 0.0f);
        
        gl.glColor3f(0.0f, 0.0f, 1.0f);
        gl.glVertex3f(0.0f, 0.0f, 0.0f);
        gl.glVertex3f(0.0f, 0.0f, size*3.0f);
        gl.glEnd();
        gl.glLineWidth(1);
    }
    
    private static void readPreferences()
    {
         Preferences prefs = Preferences.userRoot().node("StarLogo TNG").node("Rendering");
         int lod_level = prefs.getInt("SpaceLand LOD Quality", 8);
        // 1 = low,  10 = max
        if (lod_level < 1) lod_level = 1;
        if (lod_level > 10) lod_level = 10;
        lowResDistance = 20.0f * lod_level;
        medResDistance = 10.0f * lod_level;
    }
    
    
    private static void fadeToBlack(int camera)
    {
        Camera cam = SLCameras.getCamera(camera);
        if(deathFadeAlpha > 0.8f) deathFadeAlpha = 0.8f;
        gl.glColor4f(0.0f,0.0f,0.0f, deathFadeAlpha);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        gl.glEnable(GL.GL_BLEND);
        
        gl.glPushMatrix();
        gl.glLoadIdentity();
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        
        glu.gluOrtho2D(0, cam.getViewport()[2], cam.getViewport()[3], 0);
        gl.glDepthFunc(GL.GL_ALWAYS);
        gl.glDisable(GL.GL_TEXTURE_2D);
        gl.glDisable(GL.GL_CULL_FACE);
        
        gl.glRecti(cam.getViewport()[0], cam.getViewport()[1],
                   cam.getViewport()[0]+cam.getViewport()[2], cam.getViewport()[1]+cam.getViewport()[3]);
        
        gl.glEnable(GL.GL_CULL_FACE);
        gl.glDepthFunc(GL.GL_LEQUAL);
        gl.glPopMatrix();
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glPopMatrix();
    }

    
    public static void renderScene(int camera, int width, int height, Turtles turtles) 
    {
        boolean ortho = (camera == SLCameras.ORTHOGRAPHIC_CAMERA);
//      adjustFog(camera);
      
      // Enables Blending Function
      // Fog blends, objects obscure each other
      // This replaces whats already there
      // source dest
      gl.glEnable(GL.GL_BLEND);
      gl.glBlendFunc(GL.GL_ONE, GL.GL_ZERO);
      
//      gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_LINE);
      
      SLCameras.applyCamera(camera, width, height);
      gl.glLightfv(GL.GL_LIGHT1, GL.GL_POSITION, lightPosition,0);
      gl.glDisable(GL.GL_LIGHT0);
      gl.glDisable(GL.GL_LIGHT2);
      
      gl.glEnable(GL.GL_BLEND);
      
      // Calculate View Frustum from current projection and model view matrix
      
      if (!ortho)
          frustum = SLCameras.getCamera(camera).getFrustum();
      else
          frustum = null;
      
      if (!ortho && showSky)
          sky.createSkyBox(gl, 
                          SLTerrain.getMinX(), 0.0f, SLTerrain.getMinZ(), 
                          SLTerrain.getMaxX() - SLTerrain.getMinX(), 500, SLTerrain.getMaxZ() - SLTerrain.getMinZ()); 
      
     
      if (lighting)
          gl.glEnable(GL.GL_LIGHTING);
      else
          gl.glDisable(GL.GL_LIGHTING);
      
      gl.glDisable(GL.GL_BLEND);
      
      if (showTerrain)
          SLTerrain.renderTerrain();
      
      if(editingTerrain)
    	  SLTerrain.renderGrid();           
      
      renderMobileObjects(camera, turtles);

      if (!ortho)
          fence.draw();

//      if (showTerrainSelection)
//          SLTerrain.renderOverlay(terrainSelection); 
      

      gl.glDisable(GL.GL_LIGHTING);
      gl.glDisable(GL.GL_CULL_FACE);
      
      if (camera == SLCameras.TURTLE_EYE_CAMERA || camera == SLCameras.OVER_THE_SHOULDER_CAMERA)
        SLRendering.fadeToBlack(camera);
  }

    /** Draw turtles, called by drawIt() */
    private static void renderMobileObjects(int camera, Turtles turtles) 
    {
        Iterator<Mobile> iter = turtles.getTurtleIterator();
        
        // Matrix4f cameraMat = new Matrix4f(myCamera.getMatrix());
                
        // ArrayList<Mobile> zSortedTurtles = new ArrayList<Mobile>();
        
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        gl.glDisable(GL.GL_BLEND);
        
        gl.glAlphaFunc(GL.GL_GREATER, 0);
        gl.glDisable(GL.GL_ALPHA_TEST);
        
        gl.glCullFace(GL.GL_BACK);
        gl.glEnable(GL.GL_CULL_FACE);
        
        gl.glEnable(GL.GL_NORMALIZE);
        
        if (camera != SLCameras.TURTLE_EYE_CAMERA && 
            camera != SLCameras.ORTHOGRAPHIC_CAMERA)
            lastLockedMobile = null;
        
        // first, render all mobile objects
        // so that speech bubbles don't prevent
        // any from being rendered due to depth culling
        while(iter.hasNext())
        {
            Mobile m = iter.next();
            if (m.shown)
            	renderMobileObject(m, camera);
        }
        
        // iterate again, this time drawing speech bubbles
        // so that speech bubbles can overlay the scene 
        iter = turtles.getTurtleIterator();
        while(iter.hasNext())
        {
            Mobile m = iter.next();          
            if (m.shown)
                renderMobileObjectSayText(m, camera);
        }

    }

    private static Vector3f tempVector = new Vector3f();
    private static Mobile lastLockedMobile = null;
    private static void renderMobileObject(Mobile currentObject, int camera) 
    {
        currentObject.updateAnimationData(gl, glu);
        
        // Optimize rendering by checking with the frustum volume
        if (frustum != null)
        {
            currentObject.spaceLandPosition.getCenterPoint(tempVector);
            // we are using the sphere bounding the (bounding) cylinder
            if (!frustum.sphereInFrustum(tempVector.x, tempVector.y, tempVector.z,
                                         currentObject.spaceLandPosition.boundingCyl.getBoundingSphereRadius()))
                return;
        }
        
        if (camera == SLCameras.TURTLE_EYE_CAMERA && SLCameras.currentAgent == currentObject)
        {
            // if we have already entered the cylinder before, return.
            if (lastLockedMobile == currentObject)
            {
                return;
            }
            // This is the current agent for the turtle camera.
            // Normally we should never render this object, but it can happen that we are in a
            // smooth camera transition and we want to see the object until we are close enough.
            // So we check if the current camera position is inside the bounding cylinder.
            Vector3f pos = SLCameras.getCamera(camera).getPositionCopy();
            currentObject.spaceLandPosition.localCoordSys.globalToLocal(pos);
            if (currentObject.spaceLandPosition.boundingCyl.isPointInside(pos, 0.01f))
            {
                // "lock" the camera on this mobile, i.e. never display it again.
                lastLockedMobile = currentObject;
                return;
            }
        }
        
                                         
        
        gl.glPushMatrix();
        gl.glMultMatrixf(currentObject.spaceLandPosition.localCoordSys.toGLMatrix(), 0);

        if (showCylinders)
        {
            Cylinder c = currentObject.spaceLandPosition.boundingCyl;
            gl.glPushMatrix();
            gl.glColor3f(0.8f, 0.2f, 0.2f);
            // the 0.6f term from the don't ask below + 1.5f term inside drawturtle
//                gl.glTranslatef(0, (0.6f + 1.5f) * currentObject.yScale, 0);

            int steps = 32;
            float lx = c.radius, lz = 0; // x, z for angle = 0

            // glutWireCylinder is missing from this JOGL..
            gl.glBegin(GL.GL_LINES);
            for (int i = 0; i < steps; i++)
            {
                float angle = 2 * (float) Math.PI / (steps - 1) * ((i + 1) % steps);
                float x = c.radius * (float) Math.cos(angle), z = c.radius * (float) Math.sin(angle);

                gl.glVertex3f(0, c.top, 0);
                gl.glVertex3f(x, c.top, z);

                gl.glVertex3f(0, c.bottom, 0);
                gl.glVertex3f(x, c.bottom, z);

                gl.glVertex3f(x, c.top, z);
                gl.glVertex3f(x, c.bottom, z);

                gl.glVertex3f(lx, c.top, lz);
                gl.glVertex3f(x, c.top, z);

                gl.glVertex3f(lx, c.bottom, lz);
                gl.glVertex3f(x, c.bottom, z);

                lx = x;
                lz = z;
            }
            gl.glEnd();

            gl.glPopMatrix();
        }

        if (currentObject.monitored)
            drawMonitoredIndicator(currentObject);

        gl.glScalef(currentObject.xScale, currentObject.yScale, currentObject.zScale);
               
        SLRendering.drawAxes(1);
        
        int detail;
        if (camera != SLCameras.ORTHOGRAPHIC_CAMERA) {
            // TODO: take into account viewport size; also, we could use camera matrix
            tempVector.set(currentObject.pos.x, currentObject.yHeightAboveZero, currentObject.pos.z);
            tempVector.subtract(SLCameras.getCamera(camera).getMutablePosition());
            currentObject.distFromCamera = tempVector.length();
            
            if(currentObject.distFromCamera > lowResDistance * currentObject.xScale)
                detail = Model.LOW_DETAIL;
            else if(currentObject.distFromCamera > medResDistance * currentObject.xScale)
                detail = Model.MEDIUM_DETAIL;
            else
                detail = Model.HIGH_DETAIL;
        } else
            detail = Model.LOW_DETAIL;
        
        drawTurtle(currentObject, detail);
        
        
        gl.glPopMatrix();
        
        
    }
    
    
    private static void renderMobileObjectSayText(Mobile currentObject, int camera) 
    {
        // Optimize rendering by checking with the frustum volume
        if (frustum != null)
        {
            currentObject.spaceLandPosition.getCenterPoint(tempVector);
            // we are using the sphere bounding the (bounding) cylinder
            if (!frustum.sphereInFrustum(tempVector.x, tempVector.y, tempVector.z,
                                         currentObject.spaceLandPosition.boundingCyl.getBoundingSphereRadius()))
                return;
        }
        
        if (camera == SLCameras.TURTLE_EYE_CAMERA && SLCameras.currentAgent == currentObject)
        {
            // if we have already entered the cylinder before, return.
            if (lastLockedMobile == currentObject)
            {
                // draw subtitle
                drawTurtleSayText(SLCameras.currentAgent, camera);
                return;
            }
            // This is the current agent for the turtle camera.
            // Normally we should never render this object, but it can happen that we are in a
            // smooth camera transition and we want to see the object until we are close enough.
            // So we check if the current camera position is inside the bounding cylinder.
            Vector3f pos = SLCameras.getCamera(camera).getPositionCopy();
            currentObject.spaceLandPosition.localCoordSys.globalToLocal(pos);
            if (currentObject.spaceLandPosition.boundingCyl.isPointInside(pos, 0.01f))
            {
                // "lock" the camera on this mobile, i.e. never display it again.
                lastLockedMobile = currentObject;
                // draw subtitle
                drawTurtleSayText(SLCameras.currentAgent, camera);
                return;
            }
        }
        
        gl.glPushMatrix();
        gl.glMultMatrixf(currentObject.spaceLandPosition.localCoordSys.toGLMatrix(), 0);

        drawTurtleSayText(currentObject, camera);
             
        gl.glPopMatrix();
      
    }

    
    private static void drawTurtle(Mobile mobile, int detail) {
        if (mobile.isMoving())
            mobile.setMoving();
        else
            mobile.setStanding();
        
        // Enables Blending Function
        gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE);
        
        mobile.render(gl, glu, detail, SLRendering.lighting);
        
        gl.glDisable(GL.GL_TEXTURE_2D);
    }
    
    /**
     * Draws a an indicator for this mobile indicating the mobile is currently
     * being monitored. In this implementation, the indicator is a colored
     * down-pointing pyramid with the who number written on the top base.
     *
     * We're already in Mobile space when this is called, so (0,0,0) is at the
     * base of the Mobile.
     *
     * @param m -
     *            the Mobile over which the indicator will appear
     */
    public static void drawMonitoredIndicator(Mobile m) {
        
        // get Mobile's color
        float red = (float) (m.color.getRed()/256.0);
        float green = (float) (m.color.getGreen()/256.0);
        float blue = (float) (m.color.getBlue()/256.0);
        
        // disable fancy drawing stuff
        gl.glDisable(GL.GL_CULL_FACE);
        gl.glDisable(GL.GL_LIGHTING);
        
        float corner = 1f;
        float height = 2f;
        
        Cylinder cyl = m.spaceLandPosition.boundingCyl;
        gl.glPushMatrix();
        gl.glTranslatef(0, cyl.top, 0);
        
        billboard();
        
        gl.glBegin(GL.GL_TRIANGLES);
        gl.glColor3f(red, green, blue);
        gl.glVertex3f(-corner, height, 0);
        gl.glVertex3f(0, 0, 0);
        gl.glVertex3f(corner, height, 0);
        gl.glEnd();
        
        // calculate size of who # for display
        String whos = ((Integer)m.who).toString();
        double whoWidth = 0;
        for(int i = 0; i < whos.length(); i++) {
            char c = whos.charAt(i);
            float charWidth = 0.0f;
            charWidth = glutRomanCharWidths[c - 32];
            whoWidth += charWidth;
        }
        whoWidth *= .015;
        
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex3d(-whoWidth/2-.25, 2*height, 0);
        gl.glVertex3d(-whoWidth/2-.25, height, 0);
        gl.glVertex3d(whoWidth/2+.25, height, 0);
        gl.glVertex3d(whoWidth/2+.25, 2*height, 0);
        gl.glEnd();
        
        gl.glBegin(GL.GL_LINES);
        gl.glColor3f(0,0,0);
        
        gl.glVertex3d(-whoWidth/2-.25, 2*height, 0);
        gl.glVertex3d(-whoWidth/2-.25, height, 0);
        
        gl.glVertex3d(-whoWidth/2-.25, height, 0);
        gl.glVertex3f(-corner, height, 0);
        
        gl.glVertex3f(-corner, height, 0);
        gl.glVertex3f(0, 0, 0);
        
        gl.glVertex3f(0, 0, 0);
        gl.glVertex3f(corner, height, 0);
        
        gl.glVertex3f(corner, height, 0);
        gl.glVertex3d(whoWidth/2+.25, height, 0);
        
        gl.glVertex3d(whoWidth/2+.25, height, 0);
        gl.glVertex3d(whoWidth/2+.25, 2*height, 0);
        
        gl.glVertex3d(whoWidth/2+.25, 2*height, 0);
        gl.glVertex3d(-whoWidth/2-.25, 2*height, 0);
        
        gl.glEnd();
        
        
        // draw who # text
        gl.glPushMatrix();
        float textScale = (float) (.015);
        gl.glTranslated(-whoWidth/2, height+.25, .01);
        gl.glScalef(textScale, textScale, textScale);
        gl.glColor3f(0,0,0);
        glut.glutStrokeString(GLUT.STROKE_ROMAN, ((Integer)m.who).toString());
        gl.glPopMatrix();
        
        gl.glPopMatrix();
        
        // reenable fancy drawing stuff
        gl.glEnable(GL.GL_CULL_FACE);
        gl.glEnable(GL.GL_LIGHTING);
    }
    
    /** Radius of corner of say bubble */
    private static final float SAY_BUBBLE_CORNER_RADIUS = 6.5f;
    
    /** Thickness of outline of say bubble */
    private static final float SAY_BUBBLE_EDGE_THICKNESS = 1.0f;
    
    /** Line segments used in corner of say bubble */
    private static final int SAY_BUBBLE_EDGES_PER_CORNER = 10;
    
    /** Height above turtle the say bubble is drawn at
     *
     * zlateski - we don't use this approximation any more
     */
//    private static final float SAY_BUBBLE_OFFSET_Y = 10.0f;
    
    /**
     * Width of the triangle that goes from the turtle's mouth (approximately)
     * to the buttom edge of the say bubble
     */
    private static final float SAY_BUBBLE_POINTER_WIDTH = 3.3f;
    
    /** Offset of the turtles' mouths from their local origins (approximately)
     *
     * zlateski - we don't use this approximation any more
     */
    //  private static final float TURTLE_MOUTH_OFFSET_Y = 1.5f;
    
    /** Width of the say bubble (height is determined by text length) */
    private static final float SAY_BUBBLE_WIDTH = 75.0f;
    
    /** Height per line of text */
    private static final float SAY_BUBBLE_HEIGHT_PER_LINE = 5.95f;
    
    /** Space between text and left edge */
    private static final float SAY_BUBBLE_TEXT_LEFT_MARGIN = 3.5f;
    
    /** Space between text and top edge */
    private static final float SAY_BUBBLE_TEXT_TOP_MARGIN = 1.75f;
    
    /** Space between text and right edge */
    private static final float SAY_BUBBLE_TEXT_RIGHT_MARGIN = 3.5f;
    
    /** Space between text and bottom margin */
    private static final float SAY_BUBBLE_TEXT_BOTTOM_MARGIN = 2.25f;
    
    /** Scaling factor for text in say bubble */
    private static final float SAY_TEXT_SCALE = 0.05f;
    
    /** Look-up table for sin for drawing corners */
    private static final float[] trigTable = new float[SAY_BUBBLE_EDGES_PER_CORNER + 1];
    
    /** Initialize trigTable */
    static
    {
        for(int i = 0; i <= SAY_BUBBLE_EDGES_PER_CORNER; i++)
            trigTable[i] = (float)Math.sin((double)i / SAY_BUBBLE_EDGES_PER_CORNER * Math.PI / 2.0);
    }
    
    /**
     * Look-up table for widths of characters 32-127 in GLUT's Roman font
     * (initialized in init)
     */
    private static final float[] glutRomanCharWidths = new float[96];
    
    // NOTE: the newline character isn't present in this array, but when it is
    // accessed, the breakTextIntoLines method changes newlines into an emtpy String so 
    // no access violation occurs. 
    private static float glutLineWidth(String line) {
        float width = 0.0f;

        for(int i = 0; i < line.length(); i++)
            width += glutRomanCharWidths[line.charAt(i) - 32];
        
        return width * SAY_TEXT_SCALE;
    }
    
    /**
     * Draws a turtle's 'Say' bubble. When this function is called, we are
     * already at the turtle's position in space. In order to draw the speech
     * bubble correctly (facing the camera), we have to determine its
     * orientation relative to the camera and rotate it accordingly.
     */
    private static void drawTurtleSayText(Mobile turtle, int camera) 
    {
        if ((turtle.lastSayText().equals("") || turtle.sayTextFade() < 0.00001f))
            return;

        
        gl.glDisable(GL.GL_TEXTURE_2D);
        //gl.glDisable(GL.GL_DEPTH_TEST);
        gl.glDisable(GL.GL_LIGHTING);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        
        gl.glPushMatrix();
        billboard();

        Cylinder cyl = turtle.spaceLandPosition.boundingCyl;
        
        float maxLineWidth = (SAY_BUBBLE_WIDTH - SAY_BUBBLE_TEXT_LEFT_MARGIN - SAY_BUBBLE_TEXT_RIGHT_MARGIN) / SAY_TEXT_SCALE;
        
        // Break up words so that they don't overflow the width
        ArrayList<String> lines = breakTextIntoLines(turtle.lastSayText, maxLineWidth);
        
        int numLines = lines.size();
        
        float width = 0; 
        for(String line : lines)
        	width = Math.max(width, glutLineWidth(line)); 
        
        width += SAY_BUBBLE_TEXT_LEFT_MARGIN + SAY_BUBBLE_TEXT_RIGHT_MARGIN;
        width = Math.max(width, 2 * SAY_BUBBLE_CORNER_RADIUS);
        
        float height = Math.max(SAY_BUBBLE_TEXT_TOP_MARGIN + numLines * SAY_BUBBLE_HEIGHT_PER_LINE + SAY_BUBBLE_TEXT_BOTTOM_MARGIN, 2 * SAY_BUBBLE_CORNER_RADIUS);
        
        Vector3f cam2top = new Vector3f(turtle.pos.x,
                turtle.yHeightAboveZero + 2.6f + cyl.top,
                turtle.pos.z);
        cam2top.subtract(SLCameras.getCamera(camera).getMutablePosition());
        
        
        // 2.6f - as said - "don't ask"
        
        gl.glTranslatef(0.0f, cyl.top, 0.0f);
        
        // Current turtle exception
        // for over the shoulder camera
        if ((camera == SLCameras.OVER_THE_SHOULDER_CAMERA) && (turtle == SLCameras.currentAgent)) {
            cam2top.y -= 2.f;
            gl.glTranslated(.0f, -2.f, 0.f);
        }
        
        
        // Current turtle exception
        // for eye camera
        if ((camera == SLCameras.TURTLE_EYE_CAMERA) && (turtle == SLCameras.currentAgent))
        {
            drawSubTitleText(camera, lines, turtle.sayTextFade(), width, height);
            
            gl.glPopMatrix();
            gl.glEnable(GL.GL_DEPTH_TEST);
            gl.glEnable(GL.GL_LIGHTING);
            return;
        }
        
        
        float distance = Vector3f.dot(SLCameras.getCamera(camera).getMutableDirection(), cam2top);
        
        float xOff = Vector3f.dot((Vector3f.cross(SLCameras.getCamera(camera).getMutableDirection(), 
                                                  SLCameras.getCamera(camera).getMutableUpVector())),
                                  cam2top);
        float yOff = Vector3f.dot(SLCameras.getCamera(camera).getMutableUpVector(), cam2top);
        
        float maxY;
        float maxX;
        
        if (camera == SLCameras.ORTHOGRAPHIC_CAMERA) {
            maxY = halfSize;
            maxX = halfSize;
        } else {
            maxY = (float)(Math.tan(Math.toRadians(SLCameras.perspFOV/2))*distance);
            maxX = (maxY/SLCameras.getCamera(camera).getViewport()[3])*SLCameras.getCamera(camera).getViewport()[2];
        }
        
        
        
        //System.out.println("xOff " + xOff + " yOff " + yOff);
        //System.out.println("maxX " + maxX + " maxY " + maxY);
        
        // Check weather mobile's top is visible
        // if it is not, just return
        if ((xOff > maxX) ||
                (xOff < -maxX) ||
                (yOff > maxY) ||
                (yOff < -maxY)) {
            
            gl.glPopMatrix();
            gl.glEnable(GL.GL_DEPTH_TEST);
            gl.glEnable(GL.GL_LIGHTING);
            
            return;
        }
        
        int i;
        
        gl.glDepthRange(0, 0); // RADU: always on top
        gl.glEnable(GL.GL_BLEND);
        gl.glEnable(GL.GL_ALPHA_TEST);
        
        float bubble_y_offset = height / 4.f;
        
        float totalHeight = height + bubble_y_offset;
        
        float scale = 1.0f;
        
        // Special case, we want readable bubbles in 2d view
        if (camera == SLCameras.ORTHOGRAPHIC_CAMERA)
            scale = 2.0f;
        
        //System.out.println("width: " + width + " height: " + height);

        // width doesn't change between it's initializaiton to SAY_BUBBLE_WIDTH and here
        scale = Math.min(scale, Math.abs(maxX-xOff)/(SAY_BUBBLE_WIDTH/2));
        scale = Math.min(scale, Math.abs(maxX+xOff)/(SAY_BUBBLE_WIDTH/2));
        scale = Math.min(scale, Math.abs(maxY-yOff)/(totalHeight));
        
        //System.out.println("Scale: " + scale);
        
        gl.glScalef(scale, scale, scale);
        
        gl.glTranslated(0.f, bubble_y_offset, 0.f);
                
        gl.glBegin(GL.GL_TRIANGLE_FAN);
        
        float alpha = 0.75f * turtle.sayTextFade();
        
        gl.glColor4f(1.0f, 1.0f, .75f, alpha);
        
        for(i = 0; i <= SAY_BUBBLE_EDGES_PER_CORNER; i++) // Draw lower-right
            // corner of inside
        {
            gl.glVertex3f(width / 2.0f + SAY_BUBBLE_CORNER_RADIUS * (trigTable[i] - 1),
                    SAY_BUBBLE_CORNER_RADIUS * (1 - trigTable[SAY_BUBBLE_EDGES_PER_CORNER - i]),
                    0.0f);
        }
        
        for(i = 0; i <= SAY_BUBBLE_EDGES_PER_CORNER; i++) // Draw upper-right
            // corner of inside
        {
            gl.glVertex3f(width / 2.0f + SAY_BUBBLE_CORNER_RADIUS * (trigTable[SAY_BUBBLE_EDGES_PER_CORNER - i] - 1),
                    height + SAY_BUBBLE_CORNER_RADIUS * (trigTable[i] - 1),
                    0.0f);
        }
        
        for(i = 0; i <= SAY_BUBBLE_EDGES_PER_CORNER; i++) // Draw upper-left
            // corner of inside
        {
            gl.glVertex3f(-width / 2.0f + SAY_BUBBLE_CORNER_RADIUS * (1 - trigTable[i]),
                    height + SAY_BUBBLE_CORNER_RADIUS * (trigTable[SAY_BUBBLE_EDGES_PER_CORNER - i] - 1),
                    0.0f);
        }
        
        for(i = 0; i <= SAY_BUBBLE_EDGES_PER_CORNER; i++) // Draw lower-left
            // corner of inside
        {
            gl.glVertex3f(-width / 2.0f + SAY_BUBBLE_CORNER_RADIUS * (1 - trigTable[SAY_BUBBLE_EDGES_PER_CORNER - i]),
                    SAY_BUBBLE_CORNER_RADIUS * (1 - trigTable[i]),
                    0.0f);
        }
        
        // Close off triangle fan
        gl.glVertex3f(width / 2.0f - SAY_BUBBLE_CORNER_RADIUS, 0.0f, 0.0f);
        
        gl.glEnd();
        
        gl.glBegin(GL.GL_TRIANGLE_STRIP);
        
        gl.glColor4f(0.0f, 0.0f, 0.0f, alpha);
        
        for(i = 0; i <= SAY_BUBBLE_EDGES_PER_CORNER; i++) // Draw lower-right
            // corner of border
        {
            gl.glVertex3f(width / 2.0f + SAY_BUBBLE_CORNER_RADIUS * (trigTable[i] - 1),
                    SAY_BUBBLE_CORNER_RADIUS * (1 - trigTable[SAY_BUBBLE_EDGES_PER_CORNER - i]),
                    0.0f);
            gl.glVertex3f(width / 2.0f - SAY_BUBBLE_CORNER_RADIUS + (SAY_BUBBLE_CORNER_RADIUS + SAY_BUBBLE_EDGE_THICKNESS) * trigTable[i],
                    SAY_BUBBLE_CORNER_RADIUS - (SAY_BUBBLE_CORNER_RADIUS + SAY_BUBBLE_EDGE_THICKNESS) * trigTable[SAY_BUBBLE_EDGES_PER_CORNER - i],
                    0.0f);
        }
        
        for(i = 0; i <= SAY_BUBBLE_EDGES_PER_CORNER; i++) // Draw upper-right
            // corner of border
        {
            gl.glVertex3f(width / 2.0f + SAY_BUBBLE_CORNER_RADIUS * (trigTable[SAY_BUBBLE_EDGES_PER_CORNER - i] - 1),
                    height + SAY_BUBBLE_CORNER_RADIUS * (trigTable[i] - 1),
                    0.0f);
            gl.glVertex3f(width / 2.0f - SAY_BUBBLE_CORNER_RADIUS + (SAY_BUBBLE_CORNER_RADIUS + SAY_BUBBLE_EDGE_THICKNESS) * trigTable[SAY_BUBBLE_EDGES_PER_CORNER - i],
                    height - SAY_BUBBLE_CORNER_RADIUS + (SAY_BUBBLE_CORNER_RADIUS + SAY_BUBBLE_EDGE_THICKNESS) * trigTable[i],
                    0.0f);
        }
        
        for(i = 0; i <= SAY_BUBBLE_EDGES_PER_CORNER; i++) // Draw upper-left
            // corner of border
        {
            gl.glVertex3f(-width / 2.0f + SAY_BUBBLE_CORNER_RADIUS * (1 - trigTable[i]),
                    height + SAY_BUBBLE_CORNER_RADIUS * (trigTable[SAY_BUBBLE_EDGES_PER_CORNER - i] - 1),
                    0.0f);
            gl.glVertex3f(-width / 2.0f + SAY_BUBBLE_CORNER_RADIUS - (SAY_BUBBLE_CORNER_RADIUS + SAY_BUBBLE_EDGE_THICKNESS) * trigTable[i],
                    height - SAY_BUBBLE_CORNER_RADIUS + (SAY_BUBBLE_CORNER_RADIUS + SAY_BUBBLE_EDGE_THICKNESS) * trigTable[SAY_BUBBLE_EDGES_PER_CORNER - i],
                    0.0f);
        }
        
        for(i = 0; i <= SAY_BUBBLE_EDGES_PER_CORNER; i++) // Draw lower-left
            // corner of border
        {
            gl.glVertex3f(-width / 2.0f + SAY_BUBBLE_CORNER_RADIUS * (1 - trigTable[SAY_BUBBLE_EDGES_PER_CORNER - i]),
                    SAY_BUBBLE_CORNER_RADIUS * (1 - trigTable[i]),
                    0.0f);
            gl.glVertex3f(-width / 2.0f + SAY_BUBBLE_CORNER_RADIUS - (SAY_BUBBLE_CORNER_RADIUS + SAY_BUBBLE_EDGE_THICKNESS) * trigTable[SAY_BUBBLE_EDGES_PER_CORNER - i],
                    SAY_BUBBLE_CORNER_RADIUS - (SAY_BUBBLE_CORNER_RADIUS + SAY_BUBBLE_EDGE_THICKNESS) * trigTable[i],
                    0.0f);
        }
        
        // Close off triangle strip
        gl.glVertex3f(width / 2.0f - SAY_BUBBLE_CORNER_RADIUS, 0.0f, 0.0f);
        gl.glVertex3f(width / 2.0f - SAY_BUBBLE_CORNER_RADIUS, -SAY_BUBBLE_EDGE_THICKNESS, 0.0f);
        
        gl.glEnd();
        
        // Draw triangle from turtle's mouth to bottom edge
        // If it's not eye view
        
        if (!((camera == SLCameras.TURTLE_EYE_CAMERA) && (turtle == SLCameras.currentAgent))) {
            gl.glBegin(GL.GL_TRIANGLES);
            
            gl.glVertex3f(-SAY_BUBBLE_POINTER_WIDTH / 2.0f, -SAY_BUBBLE_EDGE_THICKNESS, 0.0f);
            gl.glVertex3f(0.0f, -bubble_y_offset, 0.0f);
            gl.glVertex3f(SAY_BUBBLE_POINTER_WIDTH / 2.0f, -SAY_BUBBLE_EDGE_THICKNESS, 0.0f);
            
            gl.glEnd();
        }
        
        drawSayText(lines, turtle.sayTextFade(), width, height);
        
        gl.glPopMatrix();
        
        gl.glDisable(GL.GL_BLEND);
        gl.glDisable(GL.GL_ALPHA_TEST);
        gl.glEnable(GL.GL_LIGHTING);
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glDepthRange(0, 1); // back to default
        
    }
    
    /**
     * Say bubble as a subtitle.
     *
     * @param lines
     * @param alpha
     * @param width
     * @param height
     */
    private static void drawSubTitleText(int camera, ArrayList<String> lines, float alpha, float width, float height) {
        
        gl.glColor4f(1.0f, 0.0f, 0.0f, alpha);
        
        gl.glPushMatrix();
        
        gl.glLoadIdentity();
        
        gl.glTranslatef(0.f,0.f,-1.0f);
        
        float maxY = (float)(Math.tan(Math.toRadians(SLCameras.perspFOV/2.f)));
        float maxX = (maxY/SLCameras.getCamera(camera).getViewport()[3])*SLCameras.getCamera(camera).getViewport()[2];
        
        gl.glTranslatef(maxX/3.f, -maxY, 0.f);
        
        maxX *= 2.f/3.f;
        
        float scale = (2*maxX)/width;
        
        gl.glScalef(scale, scale, scale);
        
        gl.glTranslatef(-width / 2.0f , height - SAY_BUBBLE_TEXT_TOP_MARGIN - SAY_BUBBLE_HEIGHT_PER_LINE, 0.0f);
        gl.glScalef(SAY_TEXT_SCALE, SAY_TEXT_SCALE, 1.0f);
        
        for(int i = 0; i < lines.size(); i++) {
            gl.glPushMatrix();
            
            glut.glutStrokeString(GLUT.STROKE_ROMAN , lines.get(i));
            
            gl.glPopMatrix();
            
            gl.glTranslatef(0.0f, -119.05f, 0.0f);
        }
        
        gl.glPopMatrix();
        
    };
    
    /**
     * Rotate the current model view matrix so that the positive z-axis points
     * towards the camera and the positive y-axis is parallel to the camera's up
     * vector
     */
    static private float[] billboardMatModelView = new float[16];
    private static void billboard() {
        
        gl.glGetFloatv(GL.GL_MODELVIEW_MATRIX, billboardMatModelView,0);
        
        billboardMatModelView[0] = 1.0f;
        billboardMatModelView[1] = 0.0f;
        billboardMatModelView[2] = 0.0f;
        
        billboardMatModelView[4] = 0.0f;
        billboardMatModelView[5] = 1.0f;
        billboardMatModelView[6] = 0.0f;
        
        billboardMatModelView[8] = 0.0f;
        billboardMatModelView[9] = 0.0f;
        billboardMatModelView[10] = 1.0f;
        
        gl.glLoadMatrixf(billboardMatModelView,0);
    }
    
    /**
     * Takes a String and breaks it up into lines so that no line is wider than
     * a given total width, using GLUT's Roman font, fitting as many words as
     * possible onto each line.<br>
     * <br>
     * The input is only broken on word boundaries unless a word by itself is
     * too long, in which case as many characters as possible are placed on the
     * line. Newlines '\n' are also honored
     *
     * @param text
     *            The text to break into lines
     * @param maxLineWidth
     *            The maximum width of each line
     * @return An ArrayList of Strings, where the total width of each string is
     *         at most maxLineWidth
     */
    private static ArrayList<String> breakTextIntoLines(String text, float maxLineWidth) {
        ArrayList<String> lines = new ArrayList<String>();
        
        if(text.equals(""))
            return lines;
        
        float lineWidth = 0.0f;
        float wordWidth = 0.0f;
        int wordStart = 0;
        boolean wordBeganLine = true;
        
        StringBuffer currentLine = new StringBuffer();
        
        for(int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            
            float charWidth = 0.0f;
            
            if(c >= 32 && c < 128)
                charWidth = glutRomanCharWidths[c - 32];
            else
                assert c == '\n' : "Trying to print unknown char: " + c;
            
            wordWidth += charWidth;
            
            if(c == '\n') {
                currentLine.append(text.subSequence(wordStart, i));
                lines.add(currentLine.toString());
                currentLine.setLength(0);
                wordStart = i + 1;
                wordBeganLine = true;
                wordWidth = 0.0f;
                lineWidth = 0.0f;
            } else if(lineWidth + charWidth > maxLineWidth) {
                if(wordBeganLine) {
                    currentLine.append(text.subSequence(wordStart, i));
                    wordStart = i;
                    wordWidth = charWidth;
                }
                
                lines.add(currentLine.toString());
                currentLine.setLength(0);
                wordBeganLine = true;
                lineWidth = wordWidth;
            } else {
                if(c == ' ') {
                    currentLine.append(text.subSequence(wordStart, i + 1));
                    
                    wordStart = i + 1;
                    wordBeganLine = false;
                    wordWidth = 0.0f;
                }
                
                lineWidth += charWidth;
            }
        }
        
        currentLine.append(text.subSequence(wordStart, text.length()));
        lines.add(currentLine.toString());
        
        return lines;
    }
    
    /**
     * Draws text starting in the upper-left corner of the say bubble using
     * GLUT's Roman font (ASCII 32 [space] through 127 [tilde] only). The
     * maximum top character in the font is 119.05 units; the bottom descends
     * 33.33 units.
     */
    private static void drawSayText(ArrayList<String> lines, float alpha, float width, float height) {
        gl.glColor4f(0.0f, 0.0f, 0.0f, alpha);
        
        gl.glPushMatrix();
        gl.glTranslatef(-width / 2.0f + SAY_BUBBLE_TEXT_LEFT_MARGIN, height - SAY_BUBBLE_TEXT_TOP_MARGIN - SAY_BUBBLE_HEIGHT_PER_LINE, 0.0f);
        gl.glScalef(SAY_TEXT_SCALE, SAY_TEXT_SCALE, 1.0f);
        
        for(int i = 0; i < lines.size(); i++) {
            gl.glPushMatrix();
            
            glut.glutStrokeString(GLUT.STROKE_ROMAN, lines.get(i));
            
            gl.glPopMatrix();
            
            gl.glTranslatef(0.0f, -119.05f, 0.0f);
        }
        
        gl.glPopMatrix();
    }


}
