package torusworld;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.media.opengl.DebugGL;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;

import starlogoc.GameManager;
import starlogoc.PatchManager;
import starlogoc.StarLogo;
//import terraineditor.Region;
import terraineditor.TerrainManager;
//import torusworld.gui.SLDrawingComponent;
import torusworld.math.Vector3f;
import torusworld.model.ModelManager;

import com.sun.opengl.util.GLUT;
//TODO My imports


public class TorusWorld extends GLCanvas implements GLEventListener, MouseListener, MouseWheelListener, KeyListener,
                MouseMotionListener, ActionListener, ImageProducer 
{
	private static final long serialVersionUID = 1L;

	/** Half size for the camera (used to initialize the cameras) */
    private static final int halfSize = 303 / 2;
    
    private MapView mapView;
    private MiniViewport miniViewport;

    /** If true, rebuild vertex arrays */
    private boolean slblocksChangedTerrain = false;

    /** If ture, the VM has run just before this frame; used to update counters */
    private boolean ranVM = false;
    private boolean terrainUpdatedSinceLastVMRun = true;
    
    /** Drawable Constants, used by any class that renders something into SpaceLand */
    private GL gl;
    private GLU glu;
    private GLUT glut;
    private GLAutoDrawable glDrawable;
    private Frame parent;  

    /** System Constants */
    private boolean texture = true;
    private int height;
    private int width;

    /** StarLogo Fields */
    private StarLogo sl;          
    private PatchManager pManager;
    private GameManager gManager;
    private Turtles turtles;
    
    private KeyboardInput keyboardInput;
    
    /**
     * Whether or not TorusWorld will accept keyboard input for moving the
     * camera, etc.
     */
    private boolean keyboardInputEnabled = true;

    private int lastMousePressedButton, lastMousePressedMods;
    
    /** 
     * tracks whether or not the mouse is moving over Spaceland, so
     * the framerate can dial back if not 
     * */
    private boolean mouseIsPressed = false;
    private long lastMouseMoveTime = 0;
    // milliseconds of inactivity before dialing down framerate
	private final long MOUSE_INACTIVITY_THRESHOLD = 1000; 

    
    private float centerY;

    //public float fogRatio = 0.f;//0.001f;

    private boolean printFPS = false;

    private ModelManager modelManager = null;

    private boolean initialized = false;

    private boolean takeScreenShot = false; //initiates a screenshot to be saved to a file
    
    private BufferedImage screenshot = null;  //the last screenshot taken or null if no screenshot is taken
    private HashSet<ImageReceiver> screenshotReceivers; //the subscribed screenshot receivers, if non-empty a new screenshot will be generated and sent 

    private int frameNum = 0;
    
    //the stuff for on the fly editing
    private TerrainManager tm;
    private int startx;
    private int starty;
//    static private Region editreg;    

    /** the id of the turtle that you see through (-1 for init) */
    private int turtleCount = -1;

    private MobilePosition.SpaceLand turtleViewPosition = new MobilePosition.SpaceLand();
    
    // lastUpdateT stores the last t used to update the animations
    // used to delay terrain update
    private float lastUpdateT = 1;
    private static final float TERRAIN_UPDATE_T = 0.9f;
    
    private SLGUI slgui;
    
    private Mobile hoveredAgent; 
    private Mobile draggedAgent; 
    
    private String breedToAdd = ""; 
    
    // tells us if we can drag agents or if we should disallow it
    private boolean canDrag = false; 
    
    private EventListenerList listeners = new EventListenerList(); 

    
    //TODO State variables for the drawing tools, updated by SLDrawingComponent
    public boolean drawingActive; //Whether we're currently drawing
    public int drawingControlSelected; //Which drawing tool is selected (according to SLDrawingComponent)
    public Color drawingColor;
    public int pencilSize;
    public Image drawingImage;
    
    //internal drawing variables
    private boolean drawingStarted; //Whether we're in the middle of drawing a rect, circ, image
    private int drawStartX, drawStartY = 0; //Point clicked on for start of drawing a rect, circ, image
    private int drawEndX, drawEndY = 0; //Point of mouse release of drawing a rect, circ, image
    private List<Integer> xList, yList; //Points clicked on for Polygon, if 0 points, we're not currently drawing
    
    //TODO temp variables inserted to play with the camera
    private float tempamt = 0.0f; //temp variable for amount of rotate in animation
    private int tempdir = 0; //which direction we're currently animating in
    
    private float timeSinceMove = 0.0f; //time since we shifted the camera upwards
    private boolean havePrevious = false; //whether there is previous data for translations
    private float previousTranslation = 0.0f; //amt of the previous move
    private float previousRelativeTranslation = 0.0f;
    private float previousAngle = 0.0f;
    private long previousTime = System.currentTimeMillis();
    
    private float prevTranslation = 0.0f;
    private float scaleTranslation = 1/50.0f;
    
    /** Constructor I */
    public TorusWorld(StarLogo _sl, Frame parentFrame, Turtles turtles)
    {
        parent = parentFrame; 
        sl = _sl;
        this.turtles = turtles;
        pManager = sl.getPatchManager();
        tm = new TerrainManager(pManager);
        gManager = GameManager.getGameManager();             
        modelManager = ModelManager.getInstance();
        glut = new GLUT();                
        camSmoother = new CameraSmoother(this);
//        JoystickInput.init();
        
        // TODO: Setup GL stuff -- is this being used at all? 
        GLCapabilities glCaps = new GLCapabilities();
        glCaps.setRedBits(8);
        glCaps.setBlueBits(8);
        glCaps.setGreenBits(8);
        glCaps.setAlphaBits(8);
        glCaps.setDoubleBuffered(true);
        
        addGLEventListener(this);
//        SLAnimator.init(this, sl, this);
        
        screenshotReceivers = new HashSet<ImageReceiver>();
        
        xList = new ArrayList<Integer>();
        yList = new ArrayList<Integer>();
        
        // register with the FocusRequester so that other modules
        // can ask TorusWorld to request keyboard focus.
        TorusWorldFocusRequester.registerTW(this);
    }
    
	public void addSLGUIListener(SLGUIListener l) {
		slgui.addSLGUIListener(l);
	}

	public void removeSLGUIListener(SLGUIListener l) {
		slgui.removeSLGUIListener(l);
	}
    
    public Dimension getMinimumSize() {
        return new Dimension(100, 100);
    }
    
    /**
     * Convience function to get the current StarLogo
     */
    public StarLogo getStarLogo() {
    	return sl; 
    }
    
    /**
     * Another getter to get the current Turtles
     */
    public Turtles getTurtles() {
    	return turtles; 
    }

    /** Initialization: main callback for JOGL, only called once */
    public void init(GLAutoDrawable drawable)
    {
        gl = drawable.getGL();
        glu = new GLU();
        glDrawable = drawable;
        glDrawable.setGL(new DebugGL(drawable.getGL()));
        
        SLRendering.init(gl, glu, glut);
        modelManager.initModels(gl);
        SLTerrain.init(pManager.getCurrentTerrain());
        SLRendering.updateFence();

//        SLAudio.init();

        centerY = SLTerrain.getPointHeight(0.0f, 0.0f);

        slgui = new SLGUI(gl, glu, glut, width, height, parent, pManager.getCurrentTerrain());  
        mapView = slgui.getMapView(); 
        miniViewport = slgui.getMiniViewport(); 

        SLCameras.init(halfSize, centerY, SLRendering.skyBoxSize);

        // Enable Fog
        //        setupFog();

        keyboardInput = new KeyboardInput(drawable);

        // Init Input Even Listener
        drawable.addKeyListener(this);
        drawable.addMouseListener(this);
        drawable.addMouseMotionListener(this);
        drawable.addMouseWheelListener(this);
        
        // adds a listener to the SLTurtlePicker
        SLTurtlePicker.addListener(new SLTurtlePickerListener() {
			public void mobileHovered(Mobile hovered) {
				setCursor(new Cursor(Cursor.HAND_CURSOR));
				hoveredAgent = hovered; 
			}
			
			public void nothingHovered() {
				hoveredAgent = null; 
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
        });
        
        // listen to the SLGUI
        slgui.addSLGUIListener(new SLGUIListener() {
			public void cameraChanged(int newCamera) {
				if (SLCameras.currentCamera == SLCameras.PERSPECTIVE_CAMERA)
                    camSmoother.reset();
                SLCameras.currentCamera = newCamera;
                gManager.setPressedCameraViewButton(SLCameras.currentCamera);
			}

			public void cameraReset() {
				if(SLCameras.currentCamera == SLCameras.PERSPECTIVE_CAMERA)
	    			SLCameras.getPerspectiveCamera().resetCameraView();
			}

			public void colorChanged(int newColor) {
				colorRegion(newColor);
			}

			public void editToggled(boolean isEditing) { }

			public void nextAgent() {
				int who = gManager.whoNumberForCamera();      
				who = turtles.getNextWho(who);                
                gManager.setTurtleWhoNumberForCamera(who);
                SLCameras.currentAgent = turtles.getTurtleWho(who); 
			}

			public void previousAgent() {
				int who = gManager.whoNumberForCamera();
				who = turtles.getPrevWho(who);
                gManager.setTurtleWhoNumberForCamera(who);
                SLCameras.currentAgent = turtles.getTurtleWho(who);
			}

			public void terrainEdited(int operation) {
 				handleTerrain(operation);
			}

			public void viewSwapped(boolean isOrtho) {
				SLCameras.orthoView = isOrtho;
                gManager.setOverhead(SLCameras.orthoView);
			}
			
        }); 

        initialized = true;
    }
    
    public boolean isInitialized() {
    	return initialized;
    }
    
    /**
     * Causes the TWListener l to listen to events fired by 
     * this class
     */
    public void addTWListener(TWListener l) {
    	listeners.add(TWListener.class, l);
    }
    
    /**
     * Causes TWListener l to stop receiving events from this class
     */
    public void removeTWListener(TWListener l) {
    	listeners.remove(TWListener.class, l);
    }

    // End Initialization

    //    /** Setup fog: called once */
    //    private void setupFog()
    //    {
    //        gl.glEnable(GL.GL_FOG);
    //        float[] fogColor = { 0.8f, 0.8f, 0.8f, 1.0f };
    //        gl.glFogi(GL.GL_FOG_MODE, GL.GL_EXP);
    //        gl.glFogf(GL.GL_FOG_DENSITY, fogRatio);
    //        gl.glFogfv(GL.GL_FOG_COLOR, fogColor, 0);
    //    }
    //
    //    /**
    //     * Called whenever fog variables are changed. Sets fog for specific camera
    //     * modes
    //     */
    //    public void adjustFog(int camera)
    //    {
    //        gl.glFogf(GL.GL_FOG_DENSITY, fogRatio);
    //        if (camera == SLCameras.ORTHOGRAPHIC_CAMERA || fogRatio < 0.0001f)
    //            gl.glDisable(GL.GL_FOG);
    //        else
    //            gl.glEnable(GL.GL_FOG);
    //    }

    /** Begin JOGL Core: Main callback if window resized or other window overlaps */
    public void reshape(GLAutoDrawable drawable, int x, int y, int _width, int _height)
    {
        gl = drawable.getGL(); // TODO: why this? - note that SLRendering.gl is not updated
        //glu = drawable.getGLU();

        // Reset The Current Viewport And Perspective Transformation
        width = _width < 1 ? 1 : _width;
        height = _height < 1 ? 1 : _height;                   
      
        slgui.updatePositions(width, height);   // update GUI positions   
    }

    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) { }

    /**
     * Updates the terrain structure. Updates GL stuff, so should be called
     * from the same thread as rendering.
     * Important: Will probably acquire sl.getLock()
     */
    private synchronized void updateTerrain()
    {
        if (!initialized)
            return;
        if (pManager.editorModifiedFlag)
        {
            SLTerrain.init(pManager.getCurrentTerrain());
            mapView.initTerrain(pManager.getCurrentTerrain());
            //This default set has been turned off for on-the-fly changes
            //SLCameras.getPerspectiveCamera().setDefaultOrientation(halfSize, centerY);
            SLRendering.updateFence();
        } else if (!terrainUpdatedSinceLastVMRun || slblocksChangedTerrain)
        {
            if (SLTerrain.updateTerrainUsingPatchManager(pManager))
            {
                if (mapView.isVisible())
                    mapView.updateTerrain();
                SLRendering.updateFence();
            }
        }
        pManager.editorModifiedFlag = false;
        slblocksChangedTerrain = false;
        terrainUpdatedSinceLastVMRun = true;
    }

    public static CameraSmoother camSmoother;

    /** Called every frame */

    private static long lastFrameTime = System.nanoTime();

    /**
     * Renders the world.
     *
     * If necessary, updates the terrain, acquiring the sl.getLock().
     * Afterwards, it acquires this and renders the frame.
     */
    public void display(GLAutoDrawable drawable)
    {
        if (width <= 1 || height <= 1 || !initialized)
            return;
        gl = drawable.getGL();
        SLRendering.setGL(gl);
        frameNum++;

        if (mustUpdateTerrain()) {
            synchronized (sl.getLock())
            {
                // Normally, one would do the check again (mustUpdateTerrain is synced);
                // But no other thread can "unset" the need to update terrain (just set it). 

                // note: updateTerrain is synchronized (acquires this)
                updateTerrain();
            }
        }
        
//        if (slgui.isEditingTerrain() && editreg != null) {
//            SLRendering.showTerrainSelection = true;
//            SLRendering.terrainSelection = editreg;
//        } else {
//            SLRendering.showTerrainSelection = false;
//        }

        synchronized (this)
        {
            // Important: inside this block, no path can result in sl.getLock()
            // being acquired!!!
        	
            long curTime = System.nanoTime();
            double deltaTime = (curTime - lastFrameTime) * 1e-9f;
            lastFrameTime = curTime;

            gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
            updateState();

            SLRendering.updateCounters(ranVM);

            if (SLCameras.currentAgent == null)
                SLRendering.deathFadeAlpha += deltaTime * 0.5f;
            else
                SLRendering.deathFadeAlpha = 0;

            updateTurtleCameras(deltaTime);
            if (SLCameras.currentCamera == SLCameras.PERSPECTIVE_CAMERA)
                SLCameras.getConstrainer(SLCameras.currentCamera).enforceConstraints();

//            SLAudio.setCameraListener(SLCameras.getMainViewCamera());

            //TODO Here is the place we can fix the mini viewport thingy
            if (SLCameras.orthoView)
            {
                if (slgui.isMiniViewShown() && miniViewport.isVisible())
                {
                	// put something in the mini-view (the old main view)
                    SLRendering.renderScene(SLCameras.currentCamera, miniViewport.getTexWidth(),
                                            miniViewport.getTexHeight(), turtles);
                    if (SLCameras.currentCamera == SLCameras.PERSPECTIVE_CAMERA)
                        SLCameraMovement.frameRendered();
                    miniViewport.copyTexture();
                    gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
                }
                // render the main view orthographically
                SLRendering.renderScene(SLCameras.ORTHOGRAPHIC_CAMERA, width, height, turtles);
            } else
            {
                // just render the scene normally
            	SLRendering.renderScene(SLCameras.currentCamera, width, height, turtles);
                if (SLCameras.currentCamera == SLCameras.PERSPECTIVE_CAMERA) {
                    SLCameraMovement.frameRendered();
                }
            }
            SLMouseTracker.frameRendered(turtles);

            gl.glViewport(0, 0, width, height);

            print_help();// also prints score, miniviewport

            if (takeScreenShot || screenshotReceivers.size() > 0) {
            	screenshot = SLScreenShot.makeImage(gl, width, height);

            	if (screenshot != null && takeScreenShot) {
            		SLScreenShot.SaveScreen(screenshot, parent);
            		takeScreenShot = false;
            	}

            	if (screenshot != null && screenshotReceivers.size() > 0) {
            		deliverImages();
            	}
            }

//            JoystickInput.poll();
            ranVM = false;
        }
    }
    
    /**
     * Produces a snapshot of spaceland which will be sent to the given ir
     * note: the ir should remove itself from the screenshot receiver list when it receives a new image
     * otherwise it will cause a screenshot to occur for each frame (which may be useful for creating movies but would affect performance).
     */
    public void addImageReceiver(ImageReceiver ir) {
    	screenshotReceivers.add(ir);
    }
    
    /**
     * Inform all registered receivers of new screenshot 
     */
    public void deliverImages() {
    	for (ImageReceiver sr : screenshotReceivers) {
    		sr.receive(screenshot, this);
    	}
    }

    /**
     * causes ImageReceiver ir to be removed from the screebshot subscriber list
     * @param ir
     */
    public void removeImageReceiver(ImageReceiver ir) {
    	screenshotReceivers.remove(ir);
    }
    
    /**
     * Updates the state using PatchManager and GameManager.
     * 
     * @requires Thread already holds sl.getLock()
     */
    public synchronized void updateVMRelatedState()
    {
        if (!initialized)
            return;

        updateTurtlesFromVM();
        if (texture != !pManager.isPatchColorsOnly())
            texture = !pManager.isPatchColorsOnly();

        SLRendering.showTerrain = pManager.arePatchesShown();
        SLRendering.showSky = pManager.isSkyShown();

        if (gManager.viewIsOverShoulder())
            SLCameras.currentCamera = SLCameras.OVER_THE_SHOULDER_CAMERA;
        else if (gManager.viewIsTurtleEye())
            SLCameras.currentCamera = SLCameras.TURTLE_EYE_CAMERA;
        else
            SLCameras.currentCamera = SLCameras.PERSPECTIVE_CAMERA;

        SLCameras.orthoView = gManager.viewIsOrthographic();

        // update SLGUI VM Related State....
        slgui.updateVMRelatedState(gManager.isScoreShown(), gManager.isClockShown(), gManager.getScore(),
        		gManager.getClock(), gManager.isMiniViewShown(), gManager.getStatusMessage());
    }
    
    
    private void updateState()
    {
        if (!initialized)
            return;
        
        // update SLGUI stuff...
        slgui.updateState(gManager.getStatusMessage()); 

        // Use turtles instead of gManager because the Mobile array is not properly synced
        // with the VM (e.g. when turtles are created or destroyed through setup blocks)
        int whoNumberVM = turtles.whoNumberForCamera();
        if (turtleCount != whoNumberVM)
        {
            turtleCount = whoNumberVM;  // -1 if turtle died
            SLCameras.currentAgent = turtles.getTurtleWho(turtleCount);
        }
        Mobile m = SLCameras.currentAgent;
        if (m != null && m.getAnimationData() != null)
            turtleViewPosition = m.spaceLandPosition;

    }
    
    /*
     * Method responsible for updating the Agent View or Agent Eye cameras
     * based on the turtle's position in spaceland, and local coordinate system.
     * 
     * (Each turtle has a position in spaceland, a direction it faces, and up vector.
     * What it calls the negative z direction, the direction it faces may be different globally
     * 
     * Prior to this method, we're expecting the turtleViewPosition is the spacelandposition
     * of the current agent (in spaceland coords (-151,151) (0, inf) (-151,151) =
     * (left right)(terrain level, up)(patch pos y, patch neg y)
    */
    private void updateTurtleCameras(double deltaTime)
    {
        if (SLCameras.currentCamera != SLCameras.OVER_THE_SHOULDER_CAMERA
            && SLCameras.currentCamera != SLCameras.TURTLE_EYE_CAMERA)
            return;

        if (SLCameras.currentAgent == null)
            return;

        //Get the camera so we can update it to the agent's current position
        Camera camera = SLCameras.getCurrentCamera();
        Vector3f pos = camera.getMutablePosition();
        Vector3f dir = camera.getMutableDirection();
        Vector3f up = camera.getMutableUpVector();
        
        if (SLCameras.currentCamera == SLCameras.TURTLE_EYE_CAMERA) {
        	// position the eye at 90% of the cylinder top part (where the eyes would be)
        	pos.set(0, turtleViewPosition.boundingCyl.top * 0.9f, 0);
        	turtleViewPosition.localCoordSys.localToGlobal(pos);

        	dir.set(0, 0, -1);
        	turtleViewPosition.localCoordSys.localToGlobalDirection(dir);
        	dir.y = 0;
        	camera.setDirection(dir);
        	
        	Vector3f toTheSide = Vector3f.cross(dir, new Vector3f(0, 1,0));
        	toTheSide.normalize();
        	up = Vector3f.cross(toTheSide, dir);
        	up.normalize();
        	camera.setUpVector(up);
        	
        	//up.set(0,1,0);
        	camera.setUpVector(up);
        	//turtleViewPosition.localCoordSys.localToGlobalDirection(up);
        }
        
        
        if (SLCameras.currentCamera == SLCameras.OVER_THE_SHOULDER_CAMERA)
        {
        	// position the eye at 90% of the cylinder top part (where the eyes would be)
        	pos.set(0, turtleViewPosition.boundingCyl.top * 0.9f, 0);
            dir.set(0, 0, -1);
            up.set(0, 1, 1);
        	
            turtleViewPosition.localCoordSys.localToGlobal(pos);
            turtleViewPosition.localCoordSys.localToGlobalDirection(dir);
            turtleViewPosition.localCoordSys.localToGlobalDirection(up);
            
            //System.out.println("Agent Cam");
            //System.out.println(pos);
            //System.out.println(dir);
            //System.out.println(up);
            //System.out.println("End Agent Cam");
            
            //Getting the position of the agent's eye in spaceland coordinates
            //(This is also the position we want to make sure we can always see)
            Vector3f posOnAgent = new Vector3f(0, turtleViewPosition.boundingCyl.top * 0.5f, 0);
            turtleViewPosition.localCoordSys.localToGlobal(posOnAgent);
            Vector3f dirOfAgent = new Vector3f(dir.x, dir.y, dir.z);
            
            //Beginning of pre-animation changes
            //With SLTerrain, we can either get the height of a specific point SLTerrain.getPointHeight,
            //or the heights of the four corners SLTerrain.heights
            //We need to check the fat line of patches to make sure we can see the character 
            //Figure out the calculations for this will be tricky...
            
            //Translating the camera back to place the camera over the shoulder
            float radius = turtleViewPosition.boundingCyl.radius;
            camera.translate(0, turtleViewPosition.boundingCyl.top*.8f, 10 * radius+10);
            //camera.translate(0, 2.5f * radius, 20 * radius);            
            
            //Here is where we should check that the camera has a direct line of
            //sight to the agent. (There shouldn't be that many checks if the camera isn't too far away)
            //We should also have a timeout after switch, so the camera doesn't go too wild
            Vector3f cameraPos = camera.getPositionCopy();
            Vector3f diffVect = Vector3f.subtract(posOnAgent,cameraPos); //Agent Position - Camera Position
            
            //We need to keep track of the distance from the points we're checking to the agent
            //so we can calculate the correct rotation angle
            Vector3f diffVectWithoutY = new Vector3f(diffVect.x, 0.0f, diffVect.z); //Agent - Camera.
            float lengthDiffVectWithoutY = diffVectWithoutY.length();
            float cameraCheckPositionDistance = lengthDiffVectWithoutY;
            
            //Generate perpToDiff and normalize it so we can calculate the offset positions for our line of sight "tunnel"
            Vector3f perpToDiff = new Vector3f(-1*diffVect.z, 0, diffVect.x); //direction perpindicular to difference vector (without y)
            perpToDiff.normalize();
            
            //Generate two offset positions to generate our tunnel line of sight
            Vector3f offsetStartPoint1 = Vector3f.subtract(cameraPos, Vector3f.mult(perpToDiff,radius));
            Vector3f offsetStartPoint2 = Vector3f.add(cameraPos, Vector3f.mult(perpToDiff,radius));
            
            //Positions we're going to start checking towards the agent for needed rotations
            Vector3f cameraCheckPosition = new Vector3f(cameraPos.x, cameraPos.y, cameraPos.z);
            Vector3f cameraCheckPositionOffset1 = new Vector3f(offsetStartPoint1.x, cameraPos.y, offsetStartPoint1.z);
            Vector3f cameraCheckPositionOffset2 = new Vector3f(offsetStartPoint2.x, cameraPos.y, offsetStartPoint2.z);  
            
            //To calculate the needed rotation, we calculate the needed rotation from each point along the tunnel,
            //then take the highest needed rotation
            float rotationAngle = 0.0f;
            
            //We also need to calculate the needed translation for a given rotation angle
            float translationDistance = 0.0f;
            
            //Progress through our "tunnel" (camera position going to agent is the center of the tunnel
            //with two points perpindicular to tunnel direction going in the same direction
            int points_to_check = 25; //Number of points we're checking along the tunnel (for one line)
            Vector3f pos_increment_amt = Vector3f.mult(diffVect, 1.0f/points_to_check); //amt to increment position (in direction of tunnel)
            float dist_increment_amt = cameraCheckPositionDistance/points_to_check; //amt to increment dist (so we don't have to keep calculating distance)
            for (int i = 0; i < points_to_check; i++) {
            	//First get the highest height of the checkPosition and two offsets at this point in the tunnel
            	float terrainHeight = SLTerrain.getPointHeight(cameraCheckPosition.x, cameraCheckPosition.z);
                terrainHeight = Math.max(terrainHeight, SLTerrain.getPointHeight(cameraCheckPositionOffset1.x, cameraCheckPositionOffset1.z));
                terrainHeight = Math.max(terrainHeight, SLTerrain.getPointHeight(cameraCheckPositionOffset2.x, cameraCheckPositionOffset2.z));
                
                //Then compare this with the height of the tunnel. If the terrain is higher, find the needed rotation angle
                if (terrainHeight > cameraCheckPosition.y) {
                	//We have three different cases. Each case depends on a different ordering of terrainHeight, checkPosition.y, posOnAgent.y
                	//ordered from top to bottom
                	
                	float neededRotationAngle = 0.0f;//The rotation angle we need to point the camera at the agent after translating
                	double terrainOffset = 5.0; //The amount to look above terrain to make sure we see agent
                	if (cameraCheckPosition.y >= posOnAgent.y){ 
                		//This is where terrain height >= camera height >= vertical position of agent
                		double cameraTheta = Math.atan((cameraCheckPosition.y - posOnAgent.y)/cameraCheckPositionDistance);
                		double terrainTheta = Math.atan((terrainHeight - posOnAgent.y + terrainOffset)/cameraCheckPositionDistance);
                		neededRotationAngle = (float)(terrainTheta - cameraTheta);
                		if (neededRotationAngle > rotationAngle) {
                    		rotationAngle = neededRotationAngle;
                    		float newCameraHeight = (float)(lengthDiffVectWithoutY*Math.tan((double)terrainTheta)) + posOnAgent.y;
                    		translationDistance = newCameraHeight - cameraPos.y;
                    	}
                	}
                	else if (posOnAgent.y >= terrainHeight + terrainOffset) {
                		//This is where vertical position of agent >= terrain height >= camera height
                		double cameraTheta = Math.atan((posOnAgent.y - cameraCheckPosition.y)/cameraCheckPositionDistance); //positive value of angle, but angled downwards
                		double terrainTheta = Math.atan((posOnAgent.y - (terrainHeight + terrainOffset))/cameraCheckPositionDistance); //positive value of angle, but angled downwards
                		neededRotationAngle = (float)(cameraTheta - terrainTheta);
                		if (neededRotationAngle > rotationAngle) {
                    		rotationAngle = neededRotationAngle;
                    		float newCameraHeight = -1.0f*(float)(lengthDiffVectWithoutY*Math.tan((double)terrainTheta)) + posOnAgent.y;
                    		translationDistance = newCameraHeight - cameraPos.y;
                		}
                	}
                	else {
                		//This is where terrain height >= vertical position of agent >= camera height
                		//This is the only case where we need to break the rotation into two parts
                		double cameraTheta = Math.atan((posOnAgent.y - cameraCheckPosition.y)/cameraCheckPositionDistance);
                		double terrainTheta = Math.atan((terrainHeight - posOnAgent.y + terrainOffset)/cameraCheckPositionDistance);
                		neededRotationAngle = (float)(terrainTheta + cameraTheta);
                		if (neededRotationAngle > rotationAngle) {
                    		rotationAngle = neededRotationAngle;
                    		float newCameraHeight = (float)(lengthDiffVectWithoutY*Math.tan((double)terrainTheta)) + posOnAgent.y;
                    		translationDistance = newCameraHeight - cameraPos.y;
                    		
                    	}
                	}
                	
                }
            	
                //Then increment our positions along the tunnel, and the horizontal distance to the agent
                cameraCheckPosition = Vector3f.add(cameraCheckPosition, pos_increment_amt);
                cameraCheckPositionOffset1 = Vector3f.add(cameraCheckPositionOffset1, pos_increment_amt);
                cameraCheckPositionOffset2 = Vector3f.add(cameraCheckPositionOffset2, pos_increment_amt);
                cameraCheckPositionDistance -= dist_increment_amt;
            }
            
            //Now, we need to translate by a distance depending on the rotation angle
            //Check how much we're translating. If we're going too far above the agent, make it vertical instead
            float maxAltitude = 10 * radius + 40;
            
            //This is the height we're going to be above the agent after translation (relative to the agent)
            float relativeTranslation = camera.getPositionCopy().y + translationDistance - posOnAgent.y; //For time delay only
            
            
            //By this point, translationDistance should be >= 0.0
            //relativeTranslation possibly might be negative.
            //Ignore maxAltitude for the time being.
            //This should update the newTranslation consistently.
            
            //update the previous translation to be part of the distance to the new one
            prevTranslation = prevTranslation + scaleTranslation*(translationDistance - prevTranslation); 
            //
            //Calculate new rotationAngle
            //We translate the camera from it's position, and we know the horizontal distance to the agent
            //then we can subtract the angles and then this is the rotation angle
            //(I don't think we can linearly interpolate the angles)
            //(We have three separate cases for the recalculation of the angle
            //float newAngle = ; lengthDiffVectWithoutY, cameraPos.y, cameraPos.y + prevTranslation, posOnAgent.y
            //We need to consider different cases: (assume prevTranslation > 0)
            //case 1: cameraPos.y > posOnAgent.y, 
            //case 2: cameraPos.y + prevTranslation < posOnAgent,
            //case 3: cameraPos.y < posOnAgent.y < cameraPos.y + prevTranslation
            float newAngle = 0.0f;
            if (cameraPos.y > posOnAgent.y) {
            	float angleToCamera = (float)Math.atan((cameraPos.y - posOnAgent.y)/lengthDiffVectWithoutY);
            	float angleToTranslated = (float)Math.atan((cameraPos.y + prevTranslation - posOnAgent.y)/lengthDiffVectWithoutY);
            	newAngle = angleToTranslated - angleToCamera;
            }
            else if (cameraPos.y + prevTranslation < posOnAgent.y) {
            	//To avoid calculations with negative angles. Use posOnAgent.y - cameraPos.y to get the positive distance between the agent and camera
            	//When we add on the translation, this will make a larger angle. We're always translating up, so we always rotate downwards. So we should
            	//get a positive angle here to negate later
            	float angleToTranslated = (float)Math.atan((posOnAgent.y - cameraPos.y)/lengthDiffVectWithoutY);
            	float angleToCamera = (float)Math.atan((posOnAgent.y - cameraPos.y + prevTranslation)/lengthDiffVectWithoutY);
            	newAngle =  angleToCamera - angleToTranslated;
            }
            else {
            	float angleToCamera = (float)Math.atan((posOnAgent.y - cameraPos.y)/lengthDiffVectWithoutY);
            	float angleToTranslated = (float)Math.atan((cameraPos.y + prevTranslation - posOnAgent.y)/lengthDiffVectWithoutY);
            	newAngle = angleToCamera + angleToTranslated;
            }
            
            
            //So here we have calculated the updated translation distance,
            //and we just finished calculating the angle we need to rotate by
            //This is the spot where we can say if the prevTranslation is currently too high,
            //rotate to a position above instead. This will still use the same interpolation,
            //but have a substitute position instead of the virtual positions above the max height
            if (prevTranslation - posOnAgent.y < maxAltitude) {
            	camera.translateGlobal(0, prevTranslation, 0);
                camera.rotateVerticallyAround(camera.getPositionCopy(), -1*newAngle);
            }
            else {
            	camera.setPosition(new Vector3f(posOnAgent.x, posOnAgent.y + maxAltitude, posOnAgent.z));
            	camera.setDirection(new Vector3f(0, -1, 0));
            	
            	Vector3f newUp = new Vector3f(dirOfAgent.x, 0, dirOfAgent.z);
            	//Normalize the newUp vector 
            	newUp.normalize();
            	camera.setUpVector(newUp);//this should be the same as the agent's current direction
            	
            }
            
            
            /*
            if (translationDistance > 0 && relativeTranslation < maxAltitude) {
            	//Only translate/rotate if we're not going to go too high
            	
            	float partTranslation = prevTranslation + (translationDistance - prevTranslation);//We need to move 
            	
            	
            	camera.translateGlobal(0, translationDistance, 0);
            	camera.rotateVerticallyAround(camera.getPositionCopy(), -1*rotationAngle); //The position is changed, so we have to rotate around the current camera point
            }
            else if (relativeTranslation > maxAltitude){
            	//If we're going to go too high (> 8*radius above agent's y),
            	//Place camera directly above agent (8*radius above), and look straight down at agent
            	//Don't even worry about translation/rotation from current position, just set the camera's vectors immediately
            	//And the interpolation will take care of things for us
            	
            	camera.setPosition(new Vector3f(posOnAgent.x, posOnAgent.y + maxAltitude, posOnAgent.z));
            	camera.setDirection(new Vector3f(0, -1, 0));
            	
            	Vector3f newUp = new Vector3f(dirOfAgent.x, 0, dirOfAgent.z);
            	//Normalize the newUp vector 
            	newUp.normalize();
            	camera.setUpVector(newUp);//this should be the same as the agent's current direction
            	
            }
            */
            
            
            
            
            //End of changes before animation
            
            
            
            
            //Code to rotate the camera constantly.
            //update the temp dir
            if (tempamt < -1.7) {
            	tempdir = 1;
            }
            if (tempamt > 0) {
            	tempdir = 0;
            }
            //update the temp amt
            if (tempdir == 0) {
            	tempamt -= 0.002;
            }
            if (tempdir == 1) {
            	tempamt += 0.002;
            }
            //camera.rotateVerticallyAround(posOnAgent, -1.50f); This is about vertical
            //camera.rotateVerticallyAround(posOnAgent, tempamt); //I think in radians?
            //camera.rotateVerticallyAround(cameraPos, tempamt);
            
        }
        

        // Turned off Constraints for Agent View and Agent Eye camera because the 
        // constraints for these two cameras isn't that smart. In the future, we should
        // make a better system for following the turtle. 
        //SLCameras.getConstrainer(SLCameras.currentCamera).enforceConstraints();
        
        //Smoothens the movement of the camera. The CameraSmoother contains
        //a VectorSmoother and a MatrixSmoother, each containing information about
        //the previous state of the camera. Uses Verlet integration to modify
        //the camera to smoothen the movements.
        camSmoother.smoothCamera(camera, deltaTime);
    }

    private void print_help()
    {

        if (width <= 1 || height <= 1)
            return;
        int viewport[] = new int[4];

        gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        gl.glBlendFunc(GL.GL_ONE, GL.GL_ZERO);

        //gl.glPushMatrix();
        gl.glLoadIdentity();
        
        gl.glMatrixMode(GL.GL_PROJECTION);
        //gl.glPushMatrix();
        gl.glLoadIdentity();

        gl.glGetIntegerv(GL.GL_VIEWPORT, viewport, 0);
        glu.gluOrtho2D(0, viewport[2], viewport[3], 0);
        gl.glDisable(GL.GL_DEPTH_TEST);
        gl.glDisable(GL.GL_TEXTURE_2D);
        gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        gl.glDisable(GL.GL_CULL_FACE);
        
        gl.glMatrixMode(GL.GL_MODELVIEW);

        if (printFPS)
        {
            gl.glRasterPos2f(15, 25);
            glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, "FPS: " + SLRendering.fpsString()
                                                            + "   VMPS: "
                                                            + SLRendering.vmpsString());
        }

        slgui.draw(turtles); 
        
        gl.glEnable(GL.GL_DEPTH_TEST);
        //gl.glPopMatrix();
        //gl.glMatrixMode(GL.GL_MODELVIEW);
        //gl.glPopMatrix();

    }

    /**
     * Synchronizes the state of the agents with that of the VM.
     * 
     * @requires Thread should hold sl.getLock() while calling.
     */
    private void updateTurtlesFromVM()
    {
        turtles.updateLiveTurtles(false);
        
        //NJM fix this!
//        SLAudio.processSoundEvents(sl.getTurtleManager(), turtles);
    }

    /**
     * Updates 3D-world related state of the mobile objects. does not
     * update anything from VM.
     */
    public void updateMobileObjects(float t)
    {
        lastUpdateT = t;
        if ((turtles == null) || (turtles.numTurtles() == 0))
            return;

        Iterator<Mobile> iter = turtles.getTurtleIterator();
        while (iter.hasNext()) {
            Mobile m = iter.next();
            m.updatePositioning(t);
            m.updateSayTextFade();
        }
    }
    
    public boolean isAnythingMoving()
    {
    	// since this method is used to dial back CPU usage when
    	// nothing is moving, count mouse activity as movement too.
    	// This allows a period of inactivity before saying the mouse is not moving.
    	if (mouseIsPressed || lastMouseMoveTime + MOUSE_INACTIVITY_THRESHOLD > System.currentTimeMillis()) {
    		SLMouseTracker.setRealTime(true);
    		return true;
    	}
    	else {
    		// slow down mouse tracking until the mouse moves again
    		SLMouseTracker.setRealTime(false);
    	}
    	
        if ((turtles == null) || (turtles.numTurtles() == 0))
            return false;

        Iterator<Mobile> iter = turtles.getTurtleIterator();
        while (iter.hasNext()) {
            Mobile m = iter.next();
            if (m.isMoving())
                return true;
        }
        return false;
    }
    
    public void setDragging(boolean drag) {
    	this.canDrag = drag; 
    }
     
    public boolean canDrag() {
    	return this.canDrag; 
    }

    /**
     * Calls the collision checking VM code.
     *  
     * @requires Thread should hold sl.getLock() while calling.
     */
    public void checkTurtlesCollision(int curStep)
    {
//        // No turtles at all
//        if ((turtles == null) || (turtles.numTurtles() <= 1))
//            return;
//        StarLogo.updateCollisions();
    }

    public void keyPressed(KeyEvent e) {
    	// Give running model a chance to handle keystroke
        keyboardInput.keyPressed(e);  
    }

    public void keyReleased(KeyEvent e) {
        // Give running model a chance to handle keystroke
        keyboardInput.keyReleased(e); 
    }

    public void keyTyped(KeyEvent e) { }

    // Doesn't work
    public void actionPerformed(ActionEvent e) {
//        sl.handleMenuEvent(e);
    }

    //TODO cmcheng

    public void mouseDragged(MouseEvent e) {
    	lastMouseMoveTime = System.currentTimeMillis();
        if(slgui.handleMouseEvent(e))
        	return;

        if(((canDrag && draggedAgent != null) || slgui.isEditingTerrain()) && !SwingUtilities.isRightMouseButton(e)){
        	oftTerrainMouseDrag(e);
        	return;
        }
                
        //TODO Change for mouse dragged. Also lock camera if we're in drawing mode
        if (drawingActive && !SwingUtilities.isRightMouseButton(e)) {
        	//Can call the executeDrawingToolsMethod here
        	drawingToolsMouseDragged(e);
        	return;
        }
                
        if (SLCameraMovement.isStarted())
        {
            int x, y;
            // compute the x,y we have to send to SLCameraMovement functions
            // If the camera movement is started and we're in orthoview,
            // we're moving in the miniview
            if (SLCameras.orthoView)
            {
                x = miniViewport.screenToRenderedX(e.getX());
                y = miniViewport.screenToRenderedY(e.getY());
            } else
            {
                x = e.getX();
                y = height - 1 - e.getY();
            }

            int mods = e.getModifiers() | lastMousePressedMods;
            boolean control = (mods & (InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK)) != 0;
            boolean alt = (mods & InputEvent.ALT_MASK) != 0;

            if (lastMousePressedButton == MouseEvent.BUTTON1 && !control && !alt)
                SLCameraMovement.pan(x, y);
            else if (lastMousePressedButton == MouseEvent.BUTTON3
                     || (lastMousePressedButton == MouseEvent.BUTTON1 && control))
                SLCameraMovement.rotate(x, y);
            else if (lastMousePressedButton == MouseEvent.BUTTON2
                     || (lastMousePressedButton == MouseEvent.BUTTON1 && alt))
                SLCameraMovement.zoom(x, y);
        }                      
    }

    public void mouseMoved(MouseEvent e)
    {
    	lastMouseMoveTime = System.currentTimeMillis();
        if (!initialized)
            return;
        // if we are over a GUI element, don't do anything else
        if (slgui.handleMouseEvent(e))
        {
            SLTurtlePicker.noPick();
            return;
        }

        if (SLCameras.orthoView)
            miniViewport.mousePos(e.getX(), e.getY());
        else
            mapView.mousePos(e.getX(), e.getY());

        if (!(SLCameras.orthoView && miniViewport.mouseInViewport())
            && !(!SLCameras.orthoView && mapView.mouseInViewport()))
            SLMouseTracker.update(SLCameras.getMainViewCamera(), e.getX(), height - 1 - e.getY());
        
        //TODO My change to mouse moved
        if (drawingActive) {
        	drawingToolsMouseMoved(e);
        	return;
        }
        
    }

    public void mouseClicked(MouseEvent e)
    {
    	mouseIsPressed = false;
    	if(slgui.handleMouseEvent(e))
    		return; 
    	
        /*
         * Selects/deselects the most appropriate agent along the ray. Currently
         * "most appropriate" means "closest to the origin of the ray."
         */
        if (hoveredAgent != null)
        {
            if (!hoveredAgent.monitored)
            {
                // select the agent intersected by the ray
                sl.selectedAgent(hoveredAgent.who);

                // shortcut code to make the monitor indicator show up
                // immediately rather than next frame
                hoveredAgent.monitored = true;
            } else
            {
                // select the agent intersected by the ray
//                sl.deselectedAgent(hoveredAgent.who);

                // shortcut code to make the montitor indicator show up
                // immediately rather than next frame
                hoveredAgent.monitored = false;
            }
        }//TODO Added drawingActive Clause for MouseClicked
        else if(!breedToAdd.equals("") && !slgui.isEditingTerrain() && !drawingActive) {
        	Vector3f p = SLMouseTracker.getPoint();
     		int x, y;      		
     		x = SLTerrain.getSpacelandPatchX(p.x); 
     		y = SLTerrain.getSpacelandPatchY(p.z);
     		
     		TWListener[] ls = listeners.getListeners(TWListener.class);
 			for (int i = 0; i < ls.length; i++) {
 				ls[i].agentAdded(breedToAdd, x, y);  	    
        }
    }
        else if(drawingActive && !SwingUtilities.isRightMouseButton(e)) {
        	drawingToolsMouseClicked(e);
        }
    }
    
    public void mousePressed(MouseEvent e)
    {   	
    	mouseIsPressed = true;
    	boolean miniViewShown = slgui.isMiniViewShown();  
        lastMousePressedButton = e.getButton();
        lastMousePressedMods = e.getModifiers();
        
        if (slgui.handleMouseEvent(e))
            return;
        
        else if(((canDrag && hoveredAgent != null) || slgui.isEditingTerrain()) && !SwingUtilities.isRightMouseButton(e)){
        	oftTerrainMousePress(e);        	
	        return;
        }
        
        //TODO Change for mousepressed
        else if(drawingActive && drawingStarted == false && !SwingUtilities.isRightMouseButton(e)) {
        	//Can activate drawingToolsHere        	
        	drawingToolsMousePressed(e);
        	return;
        }
        
        else if (SLCameras.currentCamera == SLCameras.PERSPECTIVE_CAMERA
            && hoveredAgent == null)
        {
            if (!SLCameras.orthoView)
            {
                if (!mapView.mouseInViewport(e.getX(), e.getY()))
                    SLCameraMovement.tryStart(e.getX(), height - 1 - e.getY());
            } else if (miniViewShown && miniViewport.isVisible() && miniViewport.mouseInViewport(e.getX(), e.getY()))
                SLCameraMovement.tryStart(miniViewport.screenToRenderedX(e.getX()), miniViewport
                    .screenToRenderedY(e.getY()));
        }        
    }

    public void mouseReleased(MouseEvent e)
    {   		
    	mouseIsPressed = false;
        lastMousePressedButton = MouseEvent.NOBUTTON;
        lastMousePressedMods = 0;
        
        SLCameraMovement.stop();
      
        if (SLCameras.currentCamera == SLCameras.PERSPECTIVE_CAMERA && !SLCameras.orthoView
            && e.isShiftDown())
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        
        if(slgui.handleMouseEvent(e))
        	return;
                
        if(((canDrag && draggedAgent != null) || slgui.isEditingTerrain()) && !SwingUtilities.isRightMouseButton(e)){
        	oftTerrainMouseRelease(e);
        	return;
        }
        //TODO Here we finalise the drawing thing
        if (drawingActive && !SwingUtilities.isRightMouseButton(e)) {
        	//Can do something else here
        	drawingToolsMouseReleased(e);
        	return;
        }
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseWheelMoved(MouseWheelEvent e)
    {
    	lastMouseMoveTime = System.currentTimeMillis();
    	boolean miniViewShown = slgui.isMiniViewShown(); 
    	
    	if(slgui.isEditingTerrain()){
    		oftTerrainMouseWheel(e);
    		return;
    	}

        if (SLCameras.currentCamera == SLCameras.PERSPECTIVE_CAMERA)
        {
            if (!SLCameras.orthoView)
            {
                if (!mapView.mouseInViewport(e.getX(), e.getY()))
                    SLCameraMovement.tryScheduleWheel(e.getX(), height - 1 - e.getY(), e
                        .getWheelRotation());
            } else if (miniViewShown && miniViewport.isVisible() && miniViewport.mouseInViewport(e.getX(), e.getY()))
                SLCameraMovement.tryScheduleWheel(miniViewport.screenToRenderedX(e.getX()),
                                                  miniViewport.screenToRenderedY(e.getY()), e
                                                      .getWheelRotation());
        }
    }

    private synchronized boolean mustUpdateTerrain() {
        if (pManager.editorModifiedFlag || slblocksChangedTerrain)
            return true;
        if (terrainUpdatedSinceLastVMRun)
            return false;
        return lastUpdateT > TERRAIN_UPDATE_T;
    }

    public synchronized boolean wasTerrainUpdated() {
        return terrainUpdatedSinceLastVMRun;
    }

    public synchronized void setRanVM() {
        ranVM = true;
        terrainUpdatedSinceLastVMRun = false;
    }

    public synchronized void setSLBlocksChangedTerrain() {
        slblocksChangedTerrain = true;
    }

    public synchronized void enableKeyboardInput(boolean enabled) {
        keyboardInputEnabled = enabled;
    }

    public synchronized boolean isKeyboardInputEnabled() {
        // Automatically disable keyboard camera controls when
        // following a turtle
        return keyboardInputEnabled;
    }

    // Some utility functions.. why are they in this class again?
    // TODO: when starlogoblocks is gone, remove these and refactor
    // calls to StarLogoShape

    /** This function is duplicated in starlogoblocks.StarLogoShape */
    public static String getCategoryName(String name)
    {
        // System.out.println(name);
        int pos = name.lastIndexOf("/");

        return name.substring(0, pos);
    }

    /** This function is duplicated in starlogoblocks.StarLogoShape */
    public static String getModelName(String name)
    {
        int pos1 = name.lastIndexOf("/");
        int pos2 = name.lastIndexOf("-");
        return name.substring(pos1 + 1, pos2);
    }

    /** This function is duplicated in starlogoblocks.StarLogoShape */
    public static String getSkinName(String name)
    {
        int pos = name.lastIndexOf("-");
        return name.substring(pos + 1);
    }

    /** This function is duplicated in starlogoblocks.StarLogoShape */
    public static boolean isValidFullName(String name)
    {
        return name != null && name.length() > 0 && name.indexOf("/") >= 0
               && name.lastIndexOf("-") > name.lastIndexOf("/");
    }

    /** This function is duplicated in starlogoblocks.StarLogoShape */
    public static String directory(String category, String modelName)
    {
        if (System.getProperty("application.home") != null)
        {
            return System.getProperty("application.home") + "/models/" + category + "/" + modelName;
        }
        return System.getProperty("user.dir") + "/models/" + category + "/" + modelName;
    }

    /** This function is duplicated in starlogoblocks.StarLogoShape */
    public static String getFullName(String category, String modelName, String skinName) {
        return category + "/" + modelName + "-" + skinName;
    }

    // starts to highlight a region
    private void oftTerrainMousePress(MouseEvent e) {
//    	draggedAgent = hoveredAgent;  // maybe drag agent
//    	editreg = null;
//        int x = miniViewport.screenToRenderedX(e.getX());
//        int y = miniViewport.screenToRenderedY(e.getY());
//        
//        Vector3f point = SLMouseTracker.getPoint();
//        x = SLTerrain.getPatchX(point.x);
//       	y = SLTerrain.getPatchY(point.z);      
//       	startx = x; starty = y;
//       	editreg = new Region(startx, starty, startx, starty);
//        
//        // Set mouse tracker to real time (but inefficient) operation
//        SLMouseTracker.setRealTime(true);
    }
    
    // highlights a region
    private void oftTerrainMouseDrag(MouseEvent e) {
    	
    	boolean miniViewShown = slgui.isMiniViewShown(); 
    	// update the mouse position
    	
    	if (!(SLCameras.orthoView && miniViewShown && miniViewport.mouseInViewport())
                && !(!SLCameras.orthoView && miniViewShown && mapView.mouseInViewport()))
            SLMouseTracker.update(SLCameras.getMainViewCamera(), e.getX(), height - 1 - e.getY());
    		
 		Vector3f p = SLMouseTracker.getPoint();
 		int x, y; 
 		
 		// drag an agent...
 		if(draggedAgent != null && SLCameras.currentCamera == SLCameras.PERSPECTIVE_CAMERA) {          	 			
         	int who = draggedAgent.who;         	
//     		editreg = null; 	// don't highlight terrain
     		// temporarily set to realtime for smoother results
     		SLMouseTracker.setRealTime(true);
     		x = SLTerrain.getSpacelandPatchX(p.x); 
     		y = SLTerrain.getSpacelandPatchY(p.z);
     		
     		TWListener[] ls = listeners.getListeners(TWListener.class);
 			for (int i = 0; i < ls.length; i++)
 				ls[i].agentDragged(who, x, y);  	    	
         }  
//    	 else if(slgui.isEditingTerrain()) {	// ...or highlight the region selected 
//    		 x = SLTerrain.getPatchX(p.x); 
//    		 y = SLTerrain.getPatchY(p.z);
//    		 editreg = new Region(startx,starty,x, y);
//    	 }
    }
    
    // Set mouse tracker back to efficient mode
    private void oftTerrainMouseRelease(MouseEvent e) {
    	draggedAgent = null; 	// release the dragged agent
        SLMouseTracker.setRealTime(false);
    }
    
    // raise, lower, pit or mound the area
    private void oftTerrainMouseWheel(MouseWheelEvent e) {
//    	if(editreg != null) {
//			int clicks = e.getWheelRotation();
//			int op = clicks < 0 ? 0 : 1; 			
//			clicks = Math.abs(clicks); 
//			if(e.isShiftDown())
//				op += 2; 
//			
//			for(int i = 0; i < clicks; i++)
//				handleTerrain(op); 
//    	}
    }
    
    // handles editing the terrain. i is an integer corresponding
    // to the desired operation. 
    private void handleTerrain(int i){
//    	if(editreg != null){
//	    	switch(i) {
//	    		case 0:
//	    			tm.addRegionHeight(editreg, 1, false);
//	    			break;
//	    		case 1:
//	    			tm.addRegionHeight(editreg, -1, false);
//	    			break;
//	    		case 2:
//	    			tm.mound(editreg, 1, false);
//	    			break;
//	    		case 3:
//	    			tm.pit(editreg, 1, false);
//	    			break;
//	    		case 4:
//	    			tm.setRegionHeight(editreg, 0, false);
//	    			break;
//	    		default:
//	    			break;	    		        		 
//    		}
//	    	pManager.editorModifiedFlag = (i>=0 && i <=4);
//    	}	
    } 
    
    // colors the highlighted region with the given color
    private void colorRegion(int col) {
//    	if(editreg != null){
//			tm.setRegionColor(editreg, col);
//			pManager.editorModifiedFlag = true;
//		}
    }  
    
    /**
     * Sets the breed that we'll add to the TorusWorld window
     */
    public void setSelectedBreed(String breed, String shape) {    	
    	breedToAdd = breed; 
    	if(slgui != null)	// special case for startup
    		slgui.setBreedToAdd(breed);
    }
    
    
    
    //TODO My changes for drawing tools
    
    /**
     * When mouse is pressed and drawing window is active.
     * For rect,circ,imag - set starting location
     * For penc - draw
     */
    public void drawingToolsMousePressed(MouseEvent e) {
    	
//    	// update mouse pointer location
//		boolean miniViewShown = slgui.isMiniViewShown(); 
//		if (!(SLCameras.orthoView && miniViewShown && miniViewport.mouseInViewport())
//                && !(!SLCameras.orthoView && miniViewShown && mapView.mouseInViewport()))
//            SLMouseTracker.update(SLCameras.getMainViewCamera(), e.getX(), height - 1 - e.getY());
//		
//		if (drawingControlSelected == SLDrawingComponent.RECT_TOOL ||
//    			drawingControlSelected == SLDrawingComponent.CIRC_TOOL || 
//    			drawingControlSelected == SLDrawingComponent.IMAG_TOOL) {
//    		//The mousetracker keeps track of where the mouse is pointing,
//    		//and based on camera, it figures out where in the world we are pointing (in framerendered)
//    		//We get the point we're pointing at (point.y comes out at you)
//    		Vector3f point = SLMouseTracker.getPoint();
//    		float x = point.x;
//    		float y = point.z;
//
//    		//We need to convert from space coordinates to terrain coordinates
//    		drawStartX = tm.coordToTerrainX(x);
//    		drawStartY = tm.coordToTerrainY(y);
//
//    		drawEndX = tm.coordToTerrainX(x);
//    		drawEndY = tm.coordToTerrainY(y);
//    		
//    		drawingStarted = true;
//    		// This should happen so it doesn't lag. Instead of updating every 5 frames, update every 1 frame
//    		SLMouseTracker.setRealTime(true);
//    	}
//    	else if (drawingControlSelected == SLDrawingComponent.PENC_TOOL) {
//    		
//    		//Figure out where the mouse is currently pointing.
//    		Vector3f point = SLMouseTracker.getPoint();
//    		int currentX = tm.coordToTerrainX(point.x);
//    		int currentY = tm.coordToTerrainY(point.z);
//    		
//    		tm.fillEllipse(currentX - pencilSize/2, currentY - pencilSize/2, 
//    				currentX + pencilSize/2, currentY + pencilSize/2, drawingColor);
//    		//drawingStarted = true;
//    		SLMouseTracker.setRealTime(true);
//    	}
    }
    
    /**
     * When we're dragging the mouse and drawing window is active
     * For rect,circ,imag - highlight from starting point to ending point
     * For penc - draw
     * @param e
     */
    public void drawingToolsMouseDragged(MouseEvent e) {
//    	//If we're entering this method, we're in drawing Mode.
//    	
//		//First get the coordinates of the mouse, set as the end
//    	// update the mouse position
//    	if (drawingControlSelected == SLDrawingComponent.RECT_TOOL ||
//    			drawingControlSelected == SLDrawingComponent.CIRC_TOOL || 
//    			drawingControlSelected == SLDrawingComponent.IMAG_TOOL) {
//    		boolean miniViewShown = slgui.isMiniViewShown(); 
//    		if (!(SLCameras.orthoView && miniViewShown && miniViewport.mouseInViewport())
//    				&& !(!SLCameras.orthoView && miniViewShown && mapView.mouseInViewport()))
//    			SLMouseTracker.update(SLCameras.getMainViewCamera(), e.getX(), height - 1 - e.getY());
//		
//    		//Figure out where the mouse is currently pointing.
//    		Vector3f point = SLMouseTracker.getPoint();
//    		float x = point.x;
//    		float y = point.z;
//    		drawEndX = tm.coordToTerrainX(x);
//    		drawEndY = tm.coordToTerrainY(y);
//    	}
//    	
//    	if (drawingControlSelected == SLDrawingComponent.RECT_TOOL) {
//           	//Then ask the terrain manager to highlight the given rectangle
//           	tm.highlightRectangle(drawStartX, drawStartY, drawEndX, drawEndY, drawingColor);
//    	}
//    	else if (drawingControlSelected == SLDrawingComponent.CIRC_TOOL) {
//    		tm.highlightEllipse(drawStartX, drawStartY, drawEndX, drawEndY, drawingColor);
//    	}
//    	else if (drawingControlSelected == SLDrawingComponent.IMAG_TOOL) {
//    		tm.highlightImage(drawStartX, drawStartY, drawEndX, drawEndY, drawingImage);
//    	}
//    	else if (drawingControlSelected == SLDrawingComponent.PENC_TOOL) {
//    		boolean miniViewShown = slgui.isMiniViewShown(); 
//    		if (!(SLCameras.orthoView && miniViewShown && miniViewport.mouseInViewport())
//                    && !(!SLCameras.orthoView && miniViewShown && mapView.mouseInViewport()))
//                SLMouseTracker.update(SLCameras.getMainViewCamera(), e.getX(), height - 1 - e.getY());
//    		//drawingStarted = false;
//    		//Figure out where the mouse is currently pointing.
//    		Vector3f point = SLMouseTracker.getPoint();
//    		int currentX = tm.coordToTerrainX(point.x);
//    		int currentY = tm.coordToTerrainY(point.z);
//    		
//    		tm.fillEllipse(currentX - pencilSize/2, currentY - pencilSize/2, 
//    				currentX + pencilSize/2, currentY + pencilSize/2, drawingColor);
//    	}
    	
    }
    
    /**
     * Method called when mouse is released and drawing window is active.
     * for rect, circ, imag - paint 
     * @param e
     */
    public void drawingToolsMouseReleased(MouseEvent e) {
//    	//First get the coordinates of the mouse, set as the end
//    	// update the mouse position
//    	if (drawingControlSelected == SLDrawingComponent.RECT_TOOL ||
//    			drawingControlSelected == SLDrawingComponent.CIRC_TOOL || 
//    			drawingControlSelected == SLDrawingComponent.IMAG_TOOL) {
//    		boolean miniViewShown = slgui.isMiniViewShown(); 
//    		if (!(SLCameras.orthoView && miniViewShown && miniViewport.mouseInViewport())
//    				&& !(!SLCameras.orthoView && miniViewShown && mapView.mouseInViewport()))
//    			SLMouseTracker.update(SLCameras.getMainViewCamera(), e.getX(), height - 1 - e.getY());
//		
//    		//Figure out where the mouse is currently pointing.
//    		Vector3f point = SLMouseTracker.getPoint();
//    		float x = point.x;
//    		float y = point.z;
//    		drawEndX = tm.coordToTerrainX(x);
//    		drawEndY = tm.coordToTerrainY(y);
//    		drawingStarted = false;
//    	}
//    	
//    	if (drawingControlSelected == SLDrawingComponent.RECT_TOOL) {
//    		tm.fillRectangle(drawStartX, drawStartY, drawEndX, drawEndY, drawingColor);
//    		SLMouseTracker.setRealTime(false);
//    	}
//    	else if (drawingControlSelected == SLDrawingComponent.CIRC_TOOL) {
//    		tm.fillEllipse(drawStartX, drawStartY, drawEndX, drawEndY, drawingColor);
//    		SLMouseTracker.setRealTime(false);
//    	}
//    	else if (drawingControlSelected == SLDrawingComponent.IMAG_TOOL) {
//    		if (drawingImage != null) {
//    			tm.fillImage(drawStartX, drawStartY, drawEndX, drawEndY, drawingImage);
//    		}
//    		SLMouseTracker.setRealTime(false);
//    	}
//    	else if (drawingControlSelected == SLDrawingComponent.PENC_TOOL) {
//    		boolean miniViewShown = slgui.isMiniViewShown(); 
//    		if (!(SLCameras.orthoView && miniViewShown && miniViewport.mouseInViewport())
//    				&& !(!SLCameras.orthoView && miniViewShown && mapView.mouseInViewport()))
//    			SLMouseTracker.update(SLCameras.getMainViewCamera(), e.getX(), height - 1 - e.getY());
//    		// djwendel - actually, you may still be in this mode, and highlighting looks
//    		// weird slow, so keep real time as true
//    		//SLMouseTracker.setRealTime(false);
//    	}
    	
    	
    }
    
    
    /**
     * Method to handle mouse clicks when the drawing window is active
     * poly - add point to list, and highlight polygon lines if not done
     * 	-if click is same as last point in list, paint the polygon.
     * @param e
     */
    public void drawingToolsMouseClicked(MouseEvent e) {
//    	if (drawingControlSelected == SLDrawingComponent.POLY_TOOL) {
//    		
//    		boolean miniViewShown = slgui.isMiniViewShown(); 
//    		if (!(SLCameras.orthoView && miniViewShown && miniViewport.mouseInViewport())
//                    && !(!SLCameras.orthoView && miniViewShown && mapView.mouseInViewport())) {
//                SLMouseTracker.update(SLCameras.getMainViewCamera(), e.getX(), height - 1 - e.getY());
//                //System.out.println("x " + e.getX());
//                //System.out.println("y " + e.getY());
//                //System.out.println("h " + height);
//    		}
//    		
//    		//Figure out where the mouse is currently pointing.
//    		Vector3f point = SLMouseTracker.getPoint();
//    		int currentX = tm.coordToTerrainX(point.x);
//    		int currentY = tm.coordToTerrainY(point.z);
//    		
//    		//First if the list is empty, add starting point.
//    		//(xList and yList are updated at the same time, so don't need to check yList)
//    		if (xList.isEmpty()) {
//    			xList.add(new Integer(currentX));
//    			yList.add(new Integer(currentY));
//    			SLMouseTracker.setRealTime(true);
//    		}
//    		else {
//    			int lastX = xList.get(xList.size() - 1);
//    			int lastY = yList.get(yList.size() - 1);
//    			
//    			if (currentX == lastX && currentY == lastY) {
//    				//If this is the same as the last x or y, fill polygon and erase list
//    				tm.fillPolygon(xList, yList, drawingColor);
//    				xList.clear();
//    				yList.clear();
//    				SLMouseTracker.setRealTime(false);
//    			}
//    			else {
//    				//Otherwise, add to the list of coordinates and draw all lines up until now
//    				xList.add(new Integer(currentX));
//    				yList.add(new Integer(currentY));
//    				tm.highlightPolygonLines(xList, yList, drawingColor);
//    				
//    			}
//    		}
//    		
//    		
//           	
//    	}
    }
    
    /**
     * method to handle mouse moves when drawing window is active
     * poly - highlight polygon lines to where mouse is pointing
     * penc - highlight pencil circle of size and color
     * @param e
     */
    public void drawingToolsMouseMoved(MouseEvent e) {
//    	if (drawingControlSelected == SLDrawingComponent.POLY_TOOL) {
//    		if (xList.isEmpty()) {
//    			return;
//    		}
//    		
//    		boolean miniViewShown = slgui.isMiniViewShown(); 
//    		if (!(SLCameras.orthoView && miniViewShown && miniViewport.mouseInViewport())
//                    && !(!SLCameras.orthoView && miniViewShown && mapView.mouseInViewport()))
//                SLMouseTracker.update(SLCameras.getMainViewCamera(), e.getX(), height - 1 - e.getY());
//    		
//    		//Figure out where the mouse is currently pointing.
//    		Vector3f point = SLMouseTracker.getPoint();
//    		int currentX = tm.coordToTerrainX(point.x);
//    		int currentY = tm.coordToTerrainY(point.z);
//    		
//    		//Add this to the list, draw the polygon lines, then remove it
//    		xList.add(new Integer(currentX));
//			yList.add(new Integer(currentY));
//			tm.highlightPolygonLines(xList, yList, drawingColor);
//			xList.remove(xList.size() - 1);
//			yList.remove(yList.size() - 1);
//    		
//    		
//    	}
//    	if (drawingControlSelected == SLDrawingComponent.PENC_TOOL) {
//    		boolean miniViewShown = slgui.isMiniViewShown(); 
//    		if (!(SLCameras.orthoView && miniViewShown && miniViewport.mouseInViewport())
//                    && !(!SLCameras.orthoView && miniViewShown && mapView.mouseInViewport()))
//                SLMouseTracker.update(SLCameras.getMainViewCamera(), e.getX(), height - 1 - e.getY());
//    		
//    		//Figure out where the mouse is currently pointing.
//    		Vector3f point = SLMouseTracker.getPoint();
//    		int currentX = tm.coordToTerrainX(point.x);
//    		int currentY = tm.coordToTerrainY(point.z);
//    		
//    		tm.highlightEllipse(currentX - pencilSize/2, currentY - pencilSize/2, 
//    				currentX + pencilSize/2, currentY + pencilSize/2, drawingColor);
//    	}
    }
    
    public void stopDrawing() {
    	this.drawingStarted = false;
    	tm.unhighlight();
    	xList.clear();
    	yList.clear();
    }
    
    
    
    
}
