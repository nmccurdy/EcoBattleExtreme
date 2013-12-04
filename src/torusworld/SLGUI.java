package torusworld;

import java.awt.Color;
import java.awt.Frame;
import java.awt.MouseInfo;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

import com.sun.opengl.util.GLUT;
import com.sun.opengl.util.texture.TextureData;

import starlogoc.TerrainData;
//import terraineditor.ColorDialog;
import torusworld.gui.GUI;
import torusworld.gui.GUIButton;
import torusworld.gui.GUIElement;
import torusworld.gui.GUIRadioGroup;
import torusworld.gui.GUIStatusBar;
import torusworld.gui.GUITextButton;
import torusworld.gui.GUITextManager;
import torusworld.gui.GUITextureButton;
import torusworld.gui.GUITools;
import torusworld.math.Vector3f;

//TODO My imports
import java.io.*;
import java.net.*;
import javax.swing.*;
import torusworld.gui.GUIImageFilter;
import java.awt.Graphics2D;

public class SLGUI
{
	private int height; // height of Spaceland -- width not needed
    private GUI gui;
    private GUITextButton perspButton, tEyeButton, tViewButton;
    private GUIRadioGroup camRadioGroup;
    private GUITextButton orthoButton; 
    private GUITextButton editButton;
    private GUITextButton camResetButton;
    private GUITextButton undoButton;
    private GUITextButton redoButton;
    
    private List<GUITextButton> topButtons;
    
    private GUITextureButton westButton, eastButton, upButton, downButton, /*northButton, southButton,*/ forwardButton, backButton;
    private List<GUIElement> perspCameraControls;

	private GUITextureButton raiseButton, lowerButton, moundButton, pitButton, levelButton, colorButton;
	private List<GUIElement> terrainControls;
    
    private GUITextureButton nextAgentButton, prevAgentButton;
    private Vector<GUIElement> agentCameraControls;
    
    private GUIStatusBar statusBar;
    
    private String rightStatusString = "";
    
    private boolean miniViewShown;    
    private boolean editTerrain; 
    private String breedToAdd = ""; 
    
//    private ColorDialog cd;     
    private MapView mapView;
    private MiniViewport miniViewport;
    
    private EventListenerList listeners = new EventListenerList();
    
    //TODO Variables that i've created.
    private boolean patchColorAvailable = true;
    
    /**
     * 	Creates the SpaceLand GUI.
     * 
     *  @param width SpaceLand window/panel width
     *  @param height SpaceLand window/panel height
     *  @param owner The owner of this GUI (used for the ColorDialog)
     */
    public SLGUI(GL gl, GLU glu, GLUT glut, int width, int height, Frame owner, TerrainData data) {
    	this.height = height; 
    	
    	gui = new GUI(gl, glu, glut);
		 
    	initCameraSelectButtons();
    	initPerspectiveControls();
    	initAgentCameraControls();
    	initTerrainControls();
    	
//    	cd = new ColorDialog(owner);
//    	cd.setVisible(false);
//    	
//    	cd.addOkCancelListener(new OkCancelListener() {
//			public void ok() {
//				// tell TorusWorld to color the region
//				SLGUIListener[] ls = listeners.getListeners(SLGUIListener.class);
//            	for (int j = 0; j < ls.length; j++)
//                    ls[j].colorChanged(cd.getChosenSLColor());
//			}
//			public void cancel() { }
//    	});

    	mapView = new MapView(15, height - 133, 100, 100);
        miniViewport = new MiniViewport(15, height - 133, 100, 100);
		 
    	statusBar = new GUIStatusBar();
    	gui.addElements(statusBar);
		 
    	updatePositions(width, height);
    	
    	miniViewport.init();           
        mapView.initTerrain(data);
    }    
        
    public void updateVMRelatedState(boolean scoreShown, boolean clockShown, double score, 
    		double clock, boolean miniViewShown, String statusMessage) {
    	// figure out the right status string (the score or the clock)
        String rightStatusString = "";
        if (scoreShown) {
            String scoreString = new DecimalFormat("#.##").format(score);
            if (scoreString.length() > 15)
                scoreString = new DecimalFormat("#.######E0").format(score);
            rightStatusString = "Score: " + scoreString;
        }

        if (clockShown) {
            String clockString = new DecimalFormat("#.00").format(clock);
            if (clockString.length() > 15)
                clockString = new DecimalFormat("#.######E0").format(clock);
            
            if (!rightStatusString.equals(""))
                rightStatusString += "     ";
            rightStatusString += "Clock: " + clockString;
        }
        
        this.miniViewShown = miniViewShown; 		// update mini-view visibility status
        statusBar.setRightText(rightStatusString);	// set the right text
        statusBar.setStatus(statusMessage);			// set the status message
    }
    
    public void updateState(String statusMessage)
    {
    	//TODO Need to read in the preferences and update our variables
    	readToolPreferences();
    	
    	
        orthoButton.setEnabled(SLCameras.orthoView);
        camRadioGroup.setSelection(SLCameras.currentCamera);
        
        for (GUIElement elem : perspCameraControls)
            elem.setVisible(SLCameras.currentCamera == SLCameras.PERSPECTIVE_CAMERA && !SLCameras.orthoView);
        
        for (GUIElement elem : agentCameraControls)
            elem.setVisible(SLCameras.currentCamera != SLCameras.PERSPECTIVE_CAMERA && !SLCameras.orthoView);
        
        for (GUIElement elem : terrainControls)
            elem.setVisible(editTerrain);
        
        String rightString = rightStatusString;
        if (rightString.equals(""))
            rightString = "FPS: " + SLRendering.fpsString() + "  VMPS: " + SLRendering.vmpsString();
        
        statusBar.setRightTextIfEmpty(rightString);
        statusBar.setTempStatus(gui.getCurrentDescription());

        // set the status bar to show the x,y coordiantes
        // of the hovered patch when we are in editing mode
        if(!breedToAdd.equals("")) {
        	Vector3f point = SLMouseTracker.getPoint();
	    	int x = SLTerrain.getSpacelandPatchX(point.x); 
	    	int y = SLTerrain.getSpacelandPatchY(point.z); 
	    	statusBar.setStatus("Click to add "+breedToAdd+" at X: "+x+", Y: "+y);
        }
        else if(editTerrain) {
	    	Vector3f point = SLMouseTracker.getPoint();
	    	int x = SLTerrain.getSpacelandPatchX(point.x); 
	    	int y = SLTerrain.getSpacelandPatchY(point.z); 
	    	float z = SLTerrain.getPointHeight(point.x,point.z);
	    	statusBar.setStatus("X: "+x+", Y: "+y+", Z: "+z);
        }
        else
        	statusBar.setStatus(statusMessage); // clear mouse coordinate info
        
    }
    
    
    //TODO Listen for the preference changes.
    private void readToolPreferences() {
    	Preferences prefs = Preferences.userRoot().node("StarLogo TNG").node("Coloring");
    	patchColorAvailable = prefs.getBoolean("SpaceLand PatchColoring", true);
    }
    /* NOT SURE IF I NEED THIS
    Preferences.userRoot().node("StarLogo TNG").node("Rendering").addPreferenceChangeListener(
            new PreferenceChangeListener()
            {
                public void preferenceChange(PreferenceChangeEvent evt)
                {
                    readPreferences();
    }
            });
    */
    
    /**
     * Updates the positioning of the SpaceLand GUI Elements.
     * 
     *  @param width SpaceLand window/panel width
     *  @param height SpaceLand window/panel height
     */
    public void updatePositions(int width, int height) 
    {
    	//update height
    	this.height = height; 
    	
    	// update position and size of map/mini-view
    	int mViewSize = (width < height ? width : height) / 4;    	
        mapView.setPos(mViewSize / 6, height - mViewSize - mViewSize / 6 - 35);
        mapView.setSize(mViewSize, mViewSize);
        miniViewport.setPos(mViewSize / 6, height - mViewSize - mViewSize / 6 - 35);
        miniViewport.setSize(mViewSize, mViewSize);
        
        int spacing = Math.min(width / 40, 20);
        
        // update labels for buttons
        if (width < 345)
        {
            perspButton.setText("3D");
            tEyeButton.setText("1st");
            tViewButton.setText("3rd");
            orthoButton.setText("Swap");
            editButton.setText("Edit");
            camResetButton.setText("Reset");
        } else
        {
            perspButton.setText("Aerial");
            tEyeButton.setText("Agent Eye");
            tViewButton.setText("Agent View");
            orthoButton.setText("Swap Views");
            editButton.setText("Edit Terrain");
            camResetButton.setText("Reset Camera"); 
        }            

        // Set top buttons size
        for (GUITextButton button : topButtons)
            button.setTextSize(width < 500 ? GUITextManager.TextSize.SMALL : GUITextManager.TextSize.NORMAL);

        // Position top buttons
        int size = GUITools.EqualizeWidths(perspButton, tEyeButton, tViewButton);
        int x = width - 4 * size - 7 * spacing;
        
        x += GUITools.AlignButtons(x, 10, true, spacing, perspButton, tEyeButton, tViewButton);
        x += spacing * 3 / 2;
        camResetButton.setPosition(x, 10);        
        x += camResetButton.getWidth();
        
        orthoButton.setPosition(miniViewport.getX()-3, miniViewport.getY()+10+miniViewport.getViewHeight()); 
             
        // Set camera buttons size
        int csize = Math.min(Math.min(width, height) / 20, 32);
        for (GUIElement butt: perspCameraControls)
            ((GUIButton) butt).setWidthHeight(csize, csize);
        for(GUIElement but: terrainControls)
        	((GUIButton) but).setWidthHeight(csize, csize);
               
        int csize2 = csize*2/3;
        forwardButton.setWidthHeight(csize2, csize2);
        backButton.setWidthHeight(csize2, csize2);
        
        undoButton.setWidthHeight(csize*2, csize);
        redoButton.setWidthHeight(csize*2, csize);
        
        // Position camera buttons
        // Arrows go on a circle
        int radius = csize * 3/2;
        int radi = 7/2 * radius;
        int cx = width - radius * 2, cy = height - radius * 5/2;
        
        upButton.setCenterPosition(cx, cy - radius);
        downButton.setCenterPosition(cx, cy + radius);
        westButton.setCenterPosition(cx - radius, cy);
        eastButton.setCenterPosition(cx + radius, cy);
            
        forwardButton.setCenterPosition(cx, cy - radius/4);
        backButton.setCenterPosition(cx, cy + radius/4);
        
        int ecx = cx-radi-9; // edit group center x-coord
        int ecy = cy+radius+6; // edit button center y-coord
        editButton.setCenterPosition(ecx,ecy);
        undoButton.setCenterPosition(ecx-5*csize-7, ecy);
        redoButton.setCenterPosition(ecx-3*csize, ecy);
        raiseButton.setCenterPosition(ecx+csize/2+1, ecy-2*csize-1);
        lowerButton.setCenterPosition(ecx+csize/2+1, ecy-csize);
        moundButton.setCenterPosition(ecx-csize/2-1, ecy-2*csize-1);
        pitButton.setCenterPosition(ecx-csize/2-1, ecy-csize);
        levelButton.setCenterPosition(ecx-csize/2-1, ecy-3*csize-2);
        //colorButton.setCenterPosition(ecx+csize/2+1, ecy-3*csize-2);
        
//        cd.setBounds(cx-2*radi,cy, cd.getWidth(), cd.getHeight());
        
        int csize3 = Math.min(Math.min(width, height) / 15, 48);

        for (GUIElement butt: agentCameraControls)
            ((GUIButton) butt).setWidthHeight(csize3, csize3);
        
        prevAgentButton.setCenterPosition(cx - csize3*2/3, cy);
        nextAgentButton.setCenterPosition(cx + csize3*2/3, cy);
        
        statusBar.setTextSize(width < 300 ? GUITextManager.TextSize.SMALL : 
                              width < 800 ? GUITextManager.TextSize.NORMAL : 
                                            GUITextManager.TextSize.LARGE);
        statusBar.setScreenSize(width, height);
    }
    
    /**
     * Draws the GUI.
     */
    public void draw(Turtles turtles)
    {
    	if (miniViewShown)
        {
            if (!SLCameras.orthoView)
                mapView.drawIt(height, turtles);
            else
                miniViewport.drawIt();
        }

        gui.draw();
    }
    
    /**
     * Checks e against all GUI elements in this.
     * @param e the MouseEvent generated
     * @return true iff e has been handled
     */
    public boolean handleMouseEvent(MouseEvent e) {
        return gui.handleMouseEvent(e);
    }

    /** 
     * @return true iff the miniView is currently shown
     */
	public boolean isMiniViewShown() {		
		return miniViewShown;
	}
	
	/**
	 * @return true iff we are editing the terrain
	 */
	public boolean isEditingTerrain() {
		return editTerrain; 
	}
	
	public void setBreedToAdd(String breed) {
		breedToAdd = breed; 
	}
	
	// hack methods to get the mapview and the miniviewport
	// these should be factored out of this class somehow...
	public MapView getMapView() {
		return mapView; 
	}
	
	public MiniViewport getMiniViewport() {
		return miniViewport; 
	}
	
	/**
	 * Causes l to listen to changes in this class
	 */
	public void addSLGUIListener(SLGUIListener l) {
		listeners.add(SLGUIListener.class, l); 
	}
	
	/**
	 * Removes l from the event listeners in this class
	 */
	public void removeSLGUIListener(SLGUIListener l) {
		listeners.remove(SLGUIListener.class, l); 
	}
		
    private void initCameraSelectButtons()
    {
        perspButton = new GUITextButton("", GUIButton.BEHAVIOR_RADIO);
        perspButton.setDescription("Switch camera to Perpective Mode");
        tEyeButton = new GUITextButton("", GUIButton.BEHAVIOR_RADIO);
        tEyeButton.setDescription("Switch camera to Agent Eye Mode");
        tViewButton = new GUITextButton("", GUIButton.BEHAVIOR_RADIO);
        tViewButton.setDescription("Switch camera to Agent Over The Shoulder Mode");
        
        
        camRadioGroup = new GUIRadioGroup();
        camRadioGroup.addButton(perspButton);
        camRadioGroup.addButton(tEyeButton);
        camRadioGroup.addButton(tViewButton);
        // the ids just happen to coincide with SLCameras.currentCamera values :)
        camRadioGroup.setSelection(SLCameras.currentCamera);
        camRadioGroup.setChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                	SLGUIListener[] ls = listeners.getListeners(SLGUIListener.class); 
                	for (int j = 0; j < ls.length; j++)
                        ls[j].cameraChanged(camRadioGroup.getSelection()); 	
                }
            });
                                        
        orthoButton = new GUITextButton("", GUIButton.BEHAVIOR_TOGGLE);
        orthoButton.setDescription("Toggle Overhead view");
        orthoButton.setEnabled(SLCameras.orthoView);
        orthoButton.setChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                	SLGUIListener[] ls = listeners.getListeners(SLGUIListener.class); 
                	for (int j = 0; j < ls.length; j++)
                        ls[j].viewSwapped(orthoButton.isEnabled());  	
                }
            });
        
        camResetButton = new GUITextButton("", GUIButton.BEHAVIOR_CLICK); 
        camResetButton.setDescription("Resets the perspective camera"); 
        camResetButton.setEnabled(false); 
        camResetButton.setActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SLGUIListener[] ls = listeners.getListeners(SLGUIListener.class); 
            	for (int j = 0; j < ls.length; j++)
                    ls[j].cameraReset();   					 
			}
        },0);
        
        editButton = new GUITextButton("", GUIButton.BEHAVIOR_TOGGLE);
        editButton.setDescription("Edit Terrain");
        editButton.setEnabled(false);
        editButton.setChangeListener(new ChangeListener(){
        	public void stateChanged(ChangeEvent e) {
        		editTerrain = editButton.isEnabled();
        		SLRendering.editingTerrain = editTerrain; // TODO: HAAAAACK (to draw grid)
        		
        		SLGUIListener[] ls = listeners.getListeners(SLGUIListener.class); 
            	for (int j = 0; j < ls.length; j++)
                    ls[j].editToggled(editTerrain);
        	}
        });
        
        gui.addElements(perspButton, tEyeButton, tViewButton, orthoButton, editButton, camResetButton);
        topButtons = new ArrayList<GUITextButton>();
        topButtons.add(perspButton);
        topButtons.add(tEyeButton);
        topButtons.add(tViewButton);
        topButtons.add(orthoButton);
        topButtons.add(editButton);
        topButtons.add(camResetButton); 

    }
    
    private void initPerspectiveControls()
    {
        final int AUTOREPEAT_OFS = 100; // offset for autorepeat ids
        ActionListener cameraController = new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    int id = e.getID();
                    boolean repeat = (id >= AUTOREPEAT_OFS);
                    if (repeat)
                        id -= AUTOREPEAT_OFS;
                    
                    SLCameras.getPerspectiveControl().processCommand(id, repeat);
                }
            };

        TextureManager.createTexture("gui.leftarrow",  SLGUI.class.getResource("textures/s_left.png"));
        TextureManager.createTexture("gui.northarrow",  SLGUI.class.getResource("textures/s_up.png"));
        TextureManager.createTexture("gui.forwardarrow",  SLGUI.class.getResource("textures/forwardarrow.png"));
        TextureManager.createTexture("gui.backarrow",  SLGUI.class.getResource("textures/backarrow.png"));
        westButton = new GUITextureButton(GUIButton.BEHAVIOR_AUTOREPEAT, "gui.leftarrow", 0, false, false);
        westButton.setDescription("Move camera to the left");
        eastButton = new GUITextureButton(GUIButton.BEHAVIOR_AUTOREPEAT, "gui.leftarrow", 0, true, false);
        eastButton.setDescription("Move camera to the right");
        upButton = new GUITextureButton(GUIButton.BEHAVIOR_AUTOREPEAT, "gui.leftarrow", 1, true, false);
        upButton.setDescription("Move camera up");
        downButton = new GUITextureButton(GUIButton.BEHAVIOR_AUTOREPEAT, "gui.leftarrow", 1, false, false);
        downButton.setDescription("Move camera down");
        
        forwardButton = new GUITextureButton(GUIButton.BEHAVIOR_AUTOREPEAT, "gui.forwardarrow", 0, false, false);
        forwardButton.setDescription("Move camera closer");
        backButton = new GUITextureButton(GUIButton.BEHAVIOR_AUTOREPEAT, "gui.backarrow", 0, false, false);
        backButton.setDescription("Move camera farther");

        westButton.setActionListener(cameraController, CameraController.MOVE_WEST, CameraController.MOVE_WEST + AUTOREPEAT_OFS);
        eastButton.setActionListener(cameraController, CameraController.MOVE_EAST, CameraController.MOVE_EAST + AUTOREPEAT_OFS);
        upButton.setActionListener(cameraController, CameraController.MOVE_UP, CameraController.MOVE_UP + AUTOREPEAT_OFS);
        downButton.setActionListener(cameraController, CameraController.MOVE_DOWN, CameraController.MOVE_DOWN + AUTOREPEAT_OFS);
        forwardButton.setActionListener(cameraController, CameraController.MOVE_FORWARD, CameraController.MOVE_FORWARD + AUTOREPEAT_OFS);
        backButton.setActionListener(cameraController, CameraController.MOVE_BACK, CameraController.MOVE_BACK + AUTOREPEAT_OFS);

        perspCameraControls = new ArrayList<GUIElement>();
        perspCameraControls.add(westButton);
        perspCameraControls.add(eastButton);
        perspCameraControls.add(upButton);
        perspCameraControls.add(downButton);
        perspCameraControls.add(forwardButton);
        perspCameraControls.add(backButton);
        gui.addElements(perspCameraControls);
    }
    
    private void initTerrainControls(){
    	ActionListener terrainController = new ActionListener(){
    		public void actionPerformed(ActionEvent e){    			
    			if(e.getID() == 5){
    				// pop up the color chooser right under the mouse
    				int x = (int)MouseInfo.getPointerInfo().getLocation().getX();
    				int y = (int)MouseInfo.getPointerInfo().getLocation().getY();
//    				cd.setLocation((int)(x-cd.getWidth()*.5),(int)(y-cd.getHeight()*.5));
//    				cd.setVisible(editTerrain); 
    			}
    			else {    		
    				SLGUIListener[] ls = listeners.getListeners(SLGUIListener.class);
                	for (int j = 0; j < ls.length; j++)
                        ls[j].terrainEdited(e.getID()); 
    			}
    		}
    	};
        
    	
    	TextureManager.createTexture("gui.raiseterrain", SLGUI.class.getResource("textures/s_raise.png"));
        TextureManager.createTexture("gui.lowerterrain", SLGUI.class.getResource("textures/s_lower.png"));
        TextureManager.createTexture("gui.moundterrain", SLGUI.class.getResource("textures/s_mound.png"));
    	TextureManager.createTexture("gui.pitterrain", SLGUI.class.getResource("textures/s_pit.png"));
    	TextureManager.createTexture("gui.levelterrain", SLGUI.class.getResource("textures/s_level.png"));
    	TextureManager.createTexture("gui.colorterrain", SLGUI.class.getResource("textures/s_color.png"));
    	
    	raiseButton = new GUITextureButton(GUIButton.BEHAVIOR_CLICK, "gui.raiseterrain", 0, false, false);
    	raiseButton.setDescription("Raise height of region (scroll-up)");
    	lowerButton = new GUITextureButton(GUIButton.BEHAVIOR_CLICK, "gui.lowerterrain", 0, false, false);
    	lowerButton.setDescription("Lower height of region (scroll-down)");
    	moundButton = new GUITextureButton(GUIButton.BEHAVIOR_CLICK, "gui.moundterrain", 0, false, false);
    	moundButton.setDescription("Create mound on region (SHIFT+scroll-up)");
    	pitButton = new GUITextureButton(GUIButton.BEHAVIOR_CLICK, "gui.pitterrain", 0, false, false);
    	pitButton.setDescription("Create pit in region (SHIFT+scroll-down)");
    	levelButton = new GUITextureButton(GUIButton.BEHAVIOR_CLICK, "gui.levelterrain", 0, false, false);
   	    levelButton.setDescription("Level Region");
    	colorButton = new GUITextureButton(GUIButton.BEHAVIOR_CLICK, "gui.colorterrain", 0, false, false);
    	colorButton.setDescription("Color Region");
    	
    	undoButton = new GUITextButton("Undo", GUIButton.BEHAVIOR_CLICK);
        undoButton.setDescription("Undo Terrain Edit");
        undoButton.setEnabled(false);
        
        redoButton = new GUITextButton("Redo", GUIButton.BEHAVIOR_CLICK);
        redoButton.setDescription("Redo Terrain Edit");
        redoButton.setEnabled(false);
    	
    	raiseButton.setActionListener(terrainController, 0);
    	lowerButton.setActionListener(terrainController, 1);
    	moundButton.setActionListener(terrainController, 2);
    	pitButton.setActionListener(terrainController, 3);
    	levelButton.setActionListener(terrainController, 4);
    	colorButton.setActionListener(terrainController, 5);
    	
    	terrainControls = new ArrayList<GUIElement>();
    	terrainControls.add(raiseButton);
    	terrainControls.add(lowerButton);
    	terrainControls.add(moundButton);
    	terrainControls.add(pitButton);
    	terrainControls.add(levelButton);
    	//terrainControls.add(colorButton);
    	//terrainControls.add(undoButton);
    	//terrainControls.add(redoButton);
    	gui.addElements(terrainControls);
    }
    
    private void initAgentCameraControls()
    {
        ActionListener agentCameraController = new ActionListener()
        {
            public void actionPerformed(ActionEvent e) {
            	SLGUIListener[] ls = listeners.getListeners(SLGUIListener.class);
            	if (e.getID() == 0) {
            		for (int j = 0; j < ls.length; j++)
            			ls[j].nextAgent(); 
            	}
            	else {
            		for (int j = 0; j < ls.length; j++)
            			ls[j].previousAgent(); 
            	}                            
            }
        };
    
        TextureManager.createTexture("gui.nextturtle",  SLGUI.class.getResource("textures/nextturtle.png"));
        TextureManager.createTexture("gui.prevturtle",  SLGUI.class.getResource("textures/prevturtle.png"));
        
        nextAgentButton = new GUITextureButton(GUIButton.BEHAVIOR_CLICK, "gui.nextturtle");
        nextAgentButton.setDescription("Switch to next agent");
        prevAgentButton = new GUITextureButton(GUIButton.BEHAVIOR_CLICK, "gui.prevturtle");
        prevAgentButton.setDescription("Switch to previous agent");
        
        nextAgentButton.setActionListener(agentCameraController, 0);
        prevAgentButton.setActionListener(agentCameraController, 1);
    
        agentCameraControls = new Vector<GUIElement>();
        agentCameraControls.addElement(nextAgentButton);
        agentCameraControls.addElement(prevAgentButton);
        gui.addElements(agentCameraControls);
    }
    
}
