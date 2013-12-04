package starlogoc;

import java.awt.Color;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import starlogoc.Variable.VariableScope;
import starlogoc.Variable.VariableType;
import terraineditor.TerrainFileFormatUtils;

public class TurtleManager {
	
////	private static final String saveTitle = "`agents`";
////	private static final String saveTurtlesTitle = "`savedTurtles`";
////	private static final String saveEnd = "`";
////	
////	private final HashMap<Integer, ArrayList<TurtleState>> savedTurtles = new HashMap<Integer, ArrayList<TurtleState>>(); 
////	
////	private final int maxTurtles;
////	private final int maxBounces;
////	private int currentTurtle;
////	
////	// Cached multiplications
////	private int turtleStateOffset;
////	private int turtleBouncesOffset;
////	//private int turtleCollisionsOffset; 
////	
////	//njm
////	protected final ByteBuffer turtles;
////	private final ByteBuffer pastTurtles;
////	
////	// Indices into turtles buffer
////
////	// NJM protected
////	//VisibleTurtle
////	protected static final int XCOR                     =  0; // slnum
////	protected static final int YCOR                     =  8; // slnum
////	protected static final int HEIGHT_ABOVE_TERRAIN     =  16; // slnum
////	protected static final int HEADING                  =  24; // slnum
////	protected static final int COLOR                    =  32; // slnum
////	protected static final int SHAPE                    =  40; // slnum string heap pointer
////	protected static final int XSCALE                   =  48; // slnum
////	protected static final int YSCALE					  =  56; // slnum
////	protected static final int ZSCALE                   =  64; // slnum
////	protected static final int SAYTEXT                  =  72; // slnum string heap pointer
////	protected static final int BOUNDING_CYLINDER_RADIUS =  80; // slnum
//////	protected static final int HEIGHT                   =  88; // slnum (roughly Mobile's Y coordinate)
////	protected static final int BOUNDING_CYLINDER_BOTTOM = 96; // slnum (distance from origin)
////	protected static final int BOUNDING_CYLINDER_TOP    = 104; // slnum (distance from origin)
//////	protected static final int OWNER					  = 112; //slnum
////	protected static final int TRANSPARENCY   		  = 120;
////
////	// NJM protected
////	//ShownTurtle
////	protected static final int SHOWNP     = 128; // char
////	protected static final int PENDOWNP   = 129; // char
////	protected static final int ALIVEP     = 130; // char
////	protected static final int ROTATABLEP = 131; // char
////    protected static final int MONITORP   = 132; // char
////    protected static final int SHOWSKINP  = 133; // char
////    //extra space...
////    
////	private static final int BREED          =  136; // slnum
////	private static final int XDELTA         = 144; // double
////	private static final int YDELTA         = 152; // double
////	//private static final int NAME         = 160; // double (temporary)
////	private static final int WHO            = 168; // int
////	private static final int HATCHED_WHO    = 172; // int
////    //private static final int SLOT           = 176; // int
////    private static final int INCARNATION_COUNT = 180; // int
////    private static final int NUM_BOUNCES    = 184; // int
////	//private static final int NUM_COLLISIONS = 188; // short
////	//private static final int COLLISIONS     = 192; // pointer
////	//private static final int PREV_OFFSET    = 196; // int
////	//private static final int NEXT_OFFSET    = 200; // int
////	//private static final int HEAP_OFFSET    = 204; // int
////	//private static final int BOUNCES        = 208; // pointer
////    private static final int LEVEL    		= 212; // int
////    //private static final int REFCOUNT		= 216; // int 	// don't use this! use the incRefCount
////    														// and decRefCount functions
////	private final ByteBuffer exportTurtles;
////    
////	private int numTurtlesOwn;
////	private LongBuffer turtleHeap;
////	private LongBuffer pastTurtleHeap;
////    private LongBuffer turtleBounces;
////	// Indices into turtle bounces buffer
////	private static final int BOUNCE_XCOR    = 0; // slnum
////	private static final int BOUNCE_YCOR    = 1; // slnum
////	private static final int BOUNCE_HEIGHT  = 2; // slnum
////	private static final int BOUNCE_HEADING = 3; // slnum
////    
////    private LongBuffer turtleSounds;
////    private static final int SOUND_WHO = 0; // slnum
////    private static final int SOUND_NAME = 1; // slnum
////    private static final int SOUND_XCOR = 2; // slnum
////    private static final int SOUND_YCOR = 3; // slnum
////    private static final int SOUND_HEIGHT_ABOVE_TERRAIN = 4; // slnum
////	
////	private ByteBuffer turtleCollisions;
////	
////	private List<Variable> varList = new ArrayList<Variable>();
//	//NJM
////	protected static final int sizeOfTurtle = StarLogo.sizeOfTurtle();
////	private static final int sizeOfVisibleTurtle = StarLogo.sizeOfVisibleTurtle();
////	private static final int sizeOfShownTurtle = StarLogo.sizeOfShownTurtle();
////	private static final int sizeOfExportTurtle =  StarLogo.sizeOfExportTurtle();
////	private static final int sizeOfBounce = StarLogo.sizeOfBounce();
////	private static final int sizeOfSound = StarLogo.sizeOfSound();
//	//private int sizeOfCollision;
//	
//	// Saved stacks. 
////	private LongBuffer turtleStates;
////	private final int sizeOfState = 1000 * 8;  // 30 stacks of 100 slnums each
////	private LongBuffer turtleStateLens;
////	private final int sizeOfStateLen = 60 * 8;     // matches the value in VM
////	
//	/* WE DO NOT SUPPORT ORIENTATION INFORMATION FOR COLLISIONS YET
//	 public static final int COLLIDE_FRONT  = 0x01000000;
//	 public static final int COLLIDE_BACK   = 0x02000000;
//	 public static final int COLLIDE_RIGHT  = 0x04000000;
//	 public static final int COLLIDE_LEFT   = 0x08000000;
//	 public static final int COLLIDE_TOP    = 0x10000000;
//	 public static final int COLLIDE_BOTTOM = 0x20000000;
//	 */
//	
//	// StarLogo instance
//	private StarLogo sl;
//	
//	/**
//	 * Creates a new TurtleManager with maximum maxTurtles turtles.
//	 **/
//	public TurtleManager(int maxTurtles, int maxBounces, int maxSounds, StarLogo sl) {
//		this.sl = sl;
////		this.maxTurtles = maxTurtles;
////		this.maxBounces = maxBounces;
//		
////		turtles = ByteBuffer.allocateDirect(maxTurtles * sizeOfTurtle).order(ByteOrder.nativeOrder());
////		pastTurtles = ByteBuffer.allocateDirect(maxTurtles * sizeOfTurtle).order(ByteOrder.nativeOrder());
////		exportTurtles = ByteBuffer.allocateDirect(maxTurtles * sizeOfExportTurtle).order(ByteOrder.nativeOrder());
////		turtleBounces = ByteBuffer.allocateDirect(maxTurtles * sizeOfBounce * maxBounces).
////                        order(ByteOrder.nativeOrder()).asLongBuffer();
////		turtleCollisions = ByteBuffer.allocateDirect(maxTurtles * 8).
////                           order(ByteOrder.nativeOrder());
////		turtleSounds = ByteBuffer.allocateDirect(maxSounds * sizeOfSound).
////                       order(ByteOrder.nativeOrder()).asLongBuffer();
////		turtleStates = ByteBuffer.allocateDirect(maxTurtles * sizeOfState).order(ByteOrder.nativeOrder()).asLongBuffer();
////		turtleStateLens = ByteBuffer.allocateDirect(maxTurtles * sizeOfStateLen).order(ByteOrder.nativeOrder()).asLongBuffer();
////		
////		init(turtles, pastTurtles, exportTurtles, turtleCollisions, maxTurtles, 
////			 turtleBounces, maxBounces, turtleSounds, maxSounds, turtleStates, turtleStateLens);
////		setNumTurtlesOwn(0);
////		//pointToFirstLiveTurtle(); //can't initialize this yet!
//		
//	}
//	
////	private native void init(ByteBuffer turtles,
////			ByteBuffer pastTurtles,
////			ByteBuffer exportTurtles,
////			ByteBuffer turtleCollisions,
////			int maxTurtles,
////			LongBuffer turtleBounces,
////			int maxBounces,
////            LongBuffer turtleSounds,
////            int maxSounds,
////            LongBuffer turtleStates,
////            LongBuffer turtleStateLens);
//	
////	public void setNumTurtlesOwn(int newNumTurtlesOwn) {
////		numTurtlesOwn = newNumTurtlesOwn;
////		turtleHeap = ByteBuffer.
////		allocateDirect(maxTurtles * newNumTurtlesOwn * 8).
////		order(ByteOrder.nativeOrder()).
////		asLongBuffer();
////		pastTurtleHeap = ByteBuffer.
////		allocateDirect(maxTurtles * newNumTurtlesOwn * 8).
////		order(ByteOrder.nativeOrder()).
////		asLongBuffer();
////		setNumTurtlesOwn(newNumTurtlesOwn, turtleHeap, pastTurtleHeap);
////	}
//	
////	private native void setNumTurtlesOwn(int newNumTurtlesOwn,
////			LongBuffer turtleHeap, LongBuffer pastTurtleHeap);
////	
////	public native int numTurtles();
////	
//	/**
//	 * TurtleManager will get info from the first live turtle in the VM.
//	 **/
//	public void pointToFirstLiveTurtle() {
//		currentTurtle = getFirstLiveTurtle();
//		System.out.println("current Turtle: " + currentTurtle);
//		if (currentTurtle >= 0)
//			updateOffsets();
//	}
//	
//	private native int getFirstLiveTurtle();
//	
//	/**
//	 * TurtleManager will get info from the next live turtle in the VM.
//	 **/
//	public void pointToNextLiveTurtle() {
//		System.out.println("prev Turtle: " + currentTurtle);
//
//		currentTurtle = getNextLiveTurtle(currentTurtle);
//		System.out.println("current Turtle: " + currentTurtle);
//		if (currentTurtle >= 0)
//			updateOffsets();
//	}
//	
//	private native int getNextLiveTurtle(int currentTurtle);
//	
//	/**
//	 * Creates a turtle with a given who number
//	 */
//	public native void createTurtleWho(int who, long breedSlnum, long breedIconSlnum);
//
//	/**
//	 * Cache the values associated with the current agent of interest.
//	 */
//	private void updateOffsets() {
//		turtleStateOffset = currentTurtle * sizeOfTurtle;
//		turtleBouncesOffset = currentTurtle * (sizeOfBounce >> 3) * maxBounces;
//		//visibleTurtleOffset = currentTurtle * (sizeOfVisibleTurtle >> 3);		
//		//shownTurtleOffset = currentTurtle * sizeOfShownTurtle;
//	}
//	
//	/**
//	 * @return true if there is another valid live turtle in the list.
//	 **/
//	public boolean isValidLiveTurtle() {
//		return (currentTurtle != -1);
//	}
//	
//	/**
//	 * @return turtle's shownp
//	 **/
//	public boolean getShownp() {
//		return (turtles.get(turtleStateOffset + SHOWNP) == 1);
//	}
//	
//	/**
//	 * @return turtle's monitorp
//	 */
//	public boolean getMonitorp() {
//		return (turtles.get(turtleStateOffset + MONITORP) == 1);
//	}
//	
//	/**
//	 * @return turtle's rotatablep
//	 **/
//	public boolean getRotatablep() {
//		return (turtles.get(turtleStateOffset + ROTATABLEP) == 1);
//	}
//    
//    /**
//     * @return turtle's showskinp
//     */
//    public boolean getShowSkinp()
//    {
//        return (turtles.get(turtleStateOffset + SHOWSKINP) == 1);
//    }
//
//	/**
//	 * @return turtle's xcor in VM coordinates
//	 **/
//	public double getVmX() {
//		return StarLogo.slnumToDouble(turtles.getLong(turtleStateOffset + XCOR));
//	}
//	
//	/**
//	 * @return turtle's ycor in VM coordinates
//	 **/
//	public double getVmY() {
//		return StarLogo.slnumToDouble(turtles.getLong(turtleStateOffset + YCOR));
//	}
//	
//	/**
//	 * @return turtle's height above the ground
//	 **/
//	public double getHeight() {
//		return StarLogo.slnumToDouble(turtles.getLong(turtleStateOffset + HEIGHT_ABOVE_TERRAIN));
//	}
//	
//	/**
//	 * @return turtle's heading
//	 **/
//	public double getHeading() {
//		return StarLogo.slnumToDouble(turtles.getLong(turtleStateOffset + HEADING));
//	}
//	
//    public int getNumBounces()
//    {
//        return turtles.getInt(turtleStateOffset + NUM_BOUNCES);
//    }
//    
//    public int getLevel()
//    {
//        return turtles.getInt(turtleStateOffset + LEVEL);
//    }
//    
//    public void setLevel(int who, int newLevel) {
//		turtles.putInt(who * sizeOfTurtle + LEVEL, newLevel);
//	}  
//    
//	/**
//	 * @return the xcor at which the agent bounced.
//	 */
//	public double getBounceXcor(int index) {
//		return StarLogo.slnumToDouble(turtleBounces.get(turtleBouncesOffset + index * (sizeOfBounce >> 3) + BOUNCE_XCOR));
//	}
//	
//	public double getBounceYcor(int index) {
//		return StarLogo.slnumToDouble(turtleBounces.get(turtleBouncesOffset + index * (sizeOfBounce >> 3) + BOUNCE_YCOR));
//	}
//	
//	public double getBounceHeight(int index) {
//		return StarLogo.slnumToDouble(turtleBounces.get(turtleBouncesOffset + index * (sizeOfBounce >> 3) + BOUNCE_HEIGHT));
//	}
//	
//	public double getBounceHeading(int index) {
//		return StarLogo.slnumToDouble(turtleBounces.get(turtleBouncesOffset + index * (sizeOfBounce >> 3) + BOUNCE_HEADING));
//	}
//    
//    public int getSoundWho(int index)
//    {
//        return StarLogo.slnumToInt(turtleSounds.get(index * (sizeOfSound >> 3) + SOUND_WHO));
//    }
//    
//    public String getSoundName(int index)
//    {
//        return StarLogo.getStringFromHeap(turtleSounds.get(index * (sizeOfSound >> 3) + SOUND_NAME));
//    }
//    
//    public double getSoundXcor(int index) {
//        return StarLogo.slnumToDouble(turtleSounds.get(index * (sizeOfSound >> 3) + SOUND_XCOR));
//    }
//    
//    public double getSoundYcor(int index) {
//        return StarLogo.slnumToDouble(turtleSounds.get(index * (sizeOfSound >> 3) + SOUND_YCOR));
//    }
//    
//    public double getSoundHeightAboveTerrain(int index) {
//        return StarLogo.slnumToDouble(turtleSounds.get(index * (sizeOfSound >> 3) + SOUND_HEIGHT_ABOVE_TERRAIN));
//    }
//	
////	/*
////	* Set the collisions for an individual turtle.
////	* @param whos is an array of who numbers in numerical order that collided with this turtle
////	* @param directions is an array parallel to whos of statically
////	* defined directions corresponding to the face of this turtle
////	* that collided.
////	*/
////	public void addCollisions(List whos, List directions) {
////	Iterator whoIterator = whos.iterator();
////	Iterator directionIterator = directions.iterator();
////	int index = turtleCollisionsOffset;
////	while (whoIterator.hasNext()) {
////	turtleCollisions.put(index, 
////	((Integer)whoIterator.next()).intValue() |
////	((Integer)directionIterator.next()).intValue());
////	index++;
////	}
//	
////	// Send the number of collisions to the turtle
////	setNumCollisions(currentTurtle, whos.size());
////	}
////	private native void setNumCollisions(int currentTurtle, int numCollisions);
//	
//	public void resetCollisions() {
//		turtleCollisions.clear();
//	}
//
////	public void addCollision(int whoCollider, int whoCollidee) {
////	// Put the turtle collision in the appropriate place in the bit buffer
////	// row = collider, col = collidee
////	//setCollisionBit(whoCollider * maxTurtles + whoCollidee);
////	}
//	
//	public native void clearCollisions(); //call this before recalculating collisions
//	public native void addCollision(int whoCollider, int whoCollidee);
//	
////	public native void setCollisionBit(int index);
//
//    public native int getNumSounds();
//    public native void clearSounds();
//	
//	/**
//	 * @return turtle's color number to work directly with starlogoc.Colors
//	 **/
//	public double getColorNumber() {
//		return StarLogo.slnumToDouble(turtles.getLong(turtleStateOffset + COLOR));
//	}
//	
//	public double getTransparencyNumber() {
//		return StarLogo.slnumToDouble(turtles.getLong(turtleStateOffset + TRANSPARENCY));
//	}
//	
//	/**
//	 * @return turtle's Color
//	 **/
//	public Color getColor() {
//		Color base = Colors.colorarray[(int)(getColorNumber()*32.0)];
//		double alpha = (255 - getTransparencyNumber()*2.55);
//		Color returnColor = new Color(base.getRed(), base.getGreen(), base.getBlue(), (int)alpha);
//		return returnColor;
//	}
//	
//	/**
//	 * @return turtle's shape
//	 **/
//	public String getShape() {
//		return StarLogo.getStringFromHeap(turtles.getLong(turtleStateOffset + SHAPE));
//	}
//	
//	/**
//	 * @return turtle's xscale
//	 **/
//	public double getXScale()
//	{
//		return StarLogo.slnumToDouble(turtles.getLong(turtleStateOffset + XSCALE));
//	}
//	
//	/**
//	 * @return turtle's yscale
//	 **/
//	public double getYScale()
//	{
//		return StarLogo.slnumToDouble(turtles.getLong(turtleStateOffset + YSCALE));
//	}
//	
//	/**
//	 * @return turtle's zscale
//	 **/
//	public double getZScale()
//	{
//		return StarLogo.slnumToDouble(turtles.getLong(turtleStateOffset + ZSCALE));
//	}
//	
//	public String getSayText()
//	{
//		return StarLogo.getStringFromHeap(turtles.getLong(turtleStateOffset + SAYTEXT));
//	}
//	
//	/**
//	 * @return turtle's who number
//	 **/
//    public int getWho() {
//        return turtles.getInt(turtleStateOffset + WHO);
//    }
//
//    public int getIncarnationCount() {
//        return turtles.getInt(turtleStateOffset + INCARNATION_COUNT);
//    }
//	
//    /** Replace oldVar with newVar, or do nothing if oldVar doesn't exist. */
//    public void renameVariable(Variable oldVar, Variable newVar) {
//        int index = varList.indexOf(oldVar);
//        if (index != -1) {
//            varList.set(index, newVar);
//            
//            // now go and fix up the saved turtle state lists
//            // note that this is quite slow as it loops over all agents
//            // and updates their variable lists
//            for(Integer i : savedTurtles.keySet()) {
//            	ArrayList<TurtleState> arr = savedTurtles.get(i);
//            	for(TurtleState ts : arr) {
//            		if(ts.variables.containsKey(oldVar)) {
//            			String value = ts.variables.get(oldVar);
//            			ts.variables.remove(oldVar);
//            			ts.variables.put(newVar, value);
//            		}
//            	}
//            }            
//        }
//    }
//	
//	public void reallocateVariables(List<Variable> newVarList) {
//		// Shortcircuit if no changes have been made
//		if (varList.equals(newVarList))
//			return;
//		
//		LongBuffer newTurtleHeap = ByteBuffer.
//		allocateDirect(maxTurtles * newVarList.size() * 8).
//		order(ByteOrder.nativeOrder()).
//		asLongBuffer();
//		
//		LongBuffer newPastTurtleHeap = ByteBuffer.
//		allocateDirect(maxTurtles * newVarList.size() * 8).
//		order(ByteOrder.nativeOrder()).
//		asLongBuffer();
//		
//		// Generate a list that maps new var positions to old positions.
//		// -1 signals that this is a new variable that will be initialized to 0.
//		List<Integer> positions = new ArrayList<Integer>(); // Indexes new positions to old positions
//		for (int newPosition = 0; newPosition < newVarList.size(); newPosition++)
//			positions.add(new Integer(varList.indexOf(newVarList.get(newPosition))));
//		
//		int oldPosition;
//		for (int i = 0; i < maxTurtles; i++) {
//			for (int newPosition = 0; newPosition < positions.size(); newPosition++) {				 
//				oldPosition = positions.get(newPosition).intValue();
//				if (oldPosition == -1) {
//					newTurtleHeap.put(0);
//					newPastTurtleHeap.put(0);
//				} else {
//					newTurtleHeap.put(turtleHeap.get(i * numTurtlesOwn + oldPosition));
//					newPastTurtleHeap.put(pastTurtleHeap.get(i * numTurtlesOwn + oldPosition));
//				}
//			}
//		}
//		
//		varList = new ArrayList<Variable>(newVarList);
//		numTurtlesOwn = newVarList.size();
//		turtleHeap = newTurtleHeap;
//		pastTurtleHeap = newPastTurtleHeap;
//		setTurtleHeap(numTurtlesOwn, turtleHeap, pastTurtleHeap);
//	}
//	private native void setTurtleHeap(int numTurtlesOwn, LongBuffer turtleHeap, LongBuffer pastTurtleHeap);
//	
//	/* The following methods function independently of the current live turtle pointed to by the 
//	 * TurtleManager.  These methods were created for use by the agent monitor.
//	 * 
//	 * WARNING - Be sure to synchronize set methods with runVM() to avoid concurrent modification
//	 * of these buffers.
//	 */
//	public boolean isMonitored(int who) {
//		return (turtles.get(who * sizeOfTurtle + MONITORP) == 1);
//	}
//	
//	public void setMonitored(int who, boolean monitored) {
//		turtles.put(who * sizeOfTurtle + MONITORP,
//				(monitored ? (byte)1 : (byte)0));
//	}
//	
//	public int getHatchedWho(int who) {
//		return turtles.getInt(who * sizeOfTurtle + HATCHED_WHO);
//	}
//	
//	public boolean isAlive(int who) {
//		return (turtles.get(who * sizeOfTurtle + ALIVEP) == 1); 
//	}
//	
//	public void setAlive(int who, boolean alive) {
//		turtles.put(who * sizeOfTurtle + ALIVEP, (alive ? (byte)1 : (byte)0)); 
//	}
//	
//	public boolean isPendown(int who) {
//		return (turtles.get(who * sizeOfTurtle + PENDOWNP) == 1);
//	}
//	
//	public void setPendown(int who, boolean pendown) {
//		turtles.put(who * sizeOfTurtle + PENDOWNP,
//				(pendown ? (byte)1 : (byte)0));
//	}
//	
//	public boolean isInvisible(int who) {
//		return (turtles.get(who * sizeOfTurtle + SHOWNP) == 0);
//	}
//
//	public void setInvisible(int who, boolean invisible) {
//		turtles.put(who * sizeOfTurtle + SHOWNP,
//				(invisible ? (byte)0 : (byte)1));
//	}
//	
//	// NJM protected
//    protected double visibleGetDouble(int who, int index)
//    {
//        return StarLogo.slnumToDouble(turtles.getLong(who * sizeOfTurtle + index));
//    }
//    
//    private void visibleSetDouble(int who, int index, double value)
//    {
//        turtles.putLong(who * sizeOfTurtle + index, StarLogo.doubleToSlnum(value));
//    }
//
//    
//    static final double SCREEN_HALF_WIDTH = 50.5;
//    static final double SCREEN_HALF_HEIGHT = 50.5;
//
//	public double getXcor(int who) {
//		return visibleGetDouble(who, XCOR) - SCREEN_HALF_WIDTH;
//	}
//	
//	public void setXcor(int who, double xcor) {
//        visibleSetDouble(who, XCOR, xcor + SCREEN_HALF_WIDTH);
//	}
//	public double getYcor(int who) {
//		return SCREEN_HALF_HEIGHT - visibleGetDouble(who, YCOR);
//	}
//	
//	public void setYcor(int who, double ycor) {
//        visibleSetDouble(who, YCOR, SCREEN_HALF_HEIGHT - ycor);
//	}
//
//	public double getHeading(int who) {
//		return visibleGetDouble(who, HEADING);
//	}
//	
//	public void setHeading(int who, double heading) {
//        double r;
//        visibleSetDouble(who, HEADING, heading);
//        r = heading * Math.PI / 180.0;
//		turtles.putDouble(who * sizeOfTurtle + XDELTA, Math.sin(r));
//		turtles.putDouble(who * sizeOfTurtle + YDELTA, -1 * Math.cos(r));
//	}
//	
//	public double getXScale(int who) {
//		return visibleGetDouble(who, XSCALE);
//	}
//	
//	public double getYScale(int who) {
//		return visibleGetDouble(who, YSCALE);
//	}
//	
//	public double getZScale(int who) {
//		return visibleGetDouble(who, ZSCALE);
//	}
//	
//	public void setXScale(int who, double x) {
//		setScaleAndUpdateBoundingCylinder(who, XSCALE, x);
//	}	
//	
//	public void setYScale(int who, double y) {
//		setScaleAndUpdateBoundingCylinder(who, YSCALE, y);
//	}	
//	
//	public void setZScale(int who, double z) {
//		setScaleAndUpdateBoundingCylinder(who, ZSCALE, z);
//	}
//	
//	/**
//	 * Sets the given dimensional scale (i.e. XSCALE, YSCALE, or ZSCALE) to the given value 
//	 * for the given agent.
//	 * @param who - the who ID number of the agent
//	 * @param scaleDimension - the scale to change (either XSCALE, YSCALE, or ZSCALE)
//	 * @param value - the value to set the dimensional scale to
//	 */
//	private void setScaleAndUpdateBoundingCylinder(int who, int scaleDimension, double value) {
//        double radius, top, bottom;
//        
//        // un-scale the current bounding cylinder (so it represents this agent's shape unscaled)
//        radius = visibleGetDouble(who, BOUNDING_CYLINDER_RADIUS) / 
//                 Math.max(visibleGetDouble(who, XSCALE), visibleGetDouble(who, ZSCALE));        
//        top = visibleGetDouble(who, BOUNDING_CYLINDER_TOP) / visibleGetDouble(who, YSCALE);
//        bottom = visibleGetDouble(who, BOUNDING_CYLINDER_BOTTOM) / visibleGetDouble(who, YSCALE);
//            
//        // now actually set the given dimension scale        
//        visibleSetDouble(who, scaleDimension, value);
//        
//        // re-scale the bounding cylinder taking the new scale into account
//        visibleSetDouble(who, BOUNDING_CYLINDER_RADIUS, radius * Math.max(visibleGetDouble(who, XSCALE), visibleGetDouble(who, ZSCALE)));
//        visibleSetDouble(who, BOUNDING_CYLINDER_TOP, top * visibleGetDouble(who, YSCALE));
//        visibleSetDouble(who, BOUNDING_CYLINDER_BOTTOM, bottom * visibleGetDouble(who, YSCALE));
//		
//	}
//	
//	public double getSize(int who) {
//		// Converting from uniform scaling to 3d scaling - changed scale to xscale, not sure where
//		// this method is used. May want to create seperate methods for each dimension
//		return visibleGetDouble(who, XSCALE);
//	}
//	
//	public void setSize(int who, double size) {
//        double radius, top, bottom;
//        
//        radius = visibleGetDouble(who, BOUNDING_CYLINDER_RADIUS) / 
//                 Math.max(visibleGetDouble(who, XSCALE), visibleGetDouble(who, ZSCALE));
//        
//        top = visibleGetDouble(who, BOUNDING_CYLINDER_TOP) / visibleGetDouble(who, YSCALE);
//        bottom = visibleGetDouble(who, BOUNDING_CYLINDER_BOTTOM) / visibleGetDouble(who, YSCALE);
//            
//                
//		// Converting from uniform scaling to 3d scaling - changed scale to xscale, not sure where
//		// this method is used. May want to create seperate methods for each dimension
//        visibleSetDouble(who, XSCALE, size);
//        // radu: this is not right; we have to scale everything - this function is used by the monitor
//        // without the following two lines, changing object sizes in monitor only scales by x axis
//        visibleSetDouble(who, YSCALE, size);
//        visibleSetDouble(who, ZSCALE, size);
//        
//        visibleSetDouble(who, BOUNDING_CYLINDER_RADIUS, radius * size);
//        visibleSetDouble(who, BOUNDING_CYLINDER_TOP, top * size);
//        visibleSetDouble(who, BOUNDING_CYLINDER_BOTTOM, bottom * size);
//    }
//    
//	public double getHeightAboveTerrain(int who) {
//		return visibleGetDouble(who, HEIGHT_ABOVE_TERRAIN);
//	}
//	
//	public void setHeightAboveTerrain(int who, double height) {
//        visibleSetDouble(who, HEIGHT_ABOVE_TERRAIN, height);
//	}
//	
//	public String getShape(int who) {
//		return StarLogo.getStringFromHeap(turtles.getLong(who * sizeOfTurtle + SHAPE));
//	}
//	
//	public void setShape(int who, String shape) {
//		turtles.putLong(who * sizeOfTurtle + SHAPE, StarLogo.addToHeap(shape));
//	}
//	
//	public String getBreed(int who) {
//		return StarLogo.getStringFromHeap(turtles.getLong(who * sizeOfTurtle + BREED));
//	}
//	
//	public void setBreed(int who, String breed) {
//		turtles.putLong(who * sizeOfTurtle + BREED, StarLogo.addToHeap(breed));
//	}
//
//	public double getColorNumber(int who) {
//		return StarLogo.slnumToDouble(turtles.getLong(who * sizeOfTurtle + COLOR));
//	}
//	
//	public void setColorNumber(int who, double color) {
//		turtles.putLong(who * sizeOfTurtle + COLOR, StarLogo.doubleToSlnum(color));
//	}
//	
//	public List<Variable> getVariableList() {
//		return Collections.unmodifiableList(varList);
//	}
//	
//	public double getNumberVariableValue(int who, Variable variableName) {
//		int i = varList.indexOf(variableName);
//		if (i != -1)
//			return StarLogo.slnumToDouble(turtleHeap.get(who * numTurtlesOwn + i));
//		return Double.NaN;
//	}
//	
//	public boolean getBooleanVariableValue(int who, Variable variableName) {
//		int i = varList.indexOf(variableName);
//		if (i != -1)
//			return StarLogo.slnumToBoolean(turtleHeap.get(who * numTurtlesOwn + i));
//		return false;
//	}
//	
//	public String getStringVariableValue(int who, Variable variableName) {
//		int i = varList.indexOf(variableName);
//		if (i != -1)
//			return StarLogo.getStringFromHeap(turtleHeap.get(who * numTurtlesOwn + i));
//		return "";
//	}
//	
//	public void setVariableValue(int who, Variable variableName, double value) {
//		int i = varList.indexOf(variableName);
//		if (i != -1)
//			turtleHeap.put(who * numTurtlesOwn + i, StarLogo.doubleToSlnum(value));
//	}
//	
//	public void setVariableValue(int who, Variable variableName, boolean value) {
//		int i = varList.indexOf(variableName);
//		if (i != -1)
//			turtleHeap.put(who * numTurtlesOwn + i, StarLogo.booleanToSlnum(value));
//	}
//	
//	public void setVariableValue(int who, Variable variableName, String value) {
//		int i = varList.indexOf(variableName);
//		if (i != -1)
//			turtleHeap.put(who * numTurtlesOwn + i, StarLogo.addToHeap(value));
//	}
//	
//	/**
//	 * Returns a XML string representing the turtles here
//	 * The Turtles schema is very simple. A turtle only needs
//	 * its who number, breed and breed icon strings to be saved 
//	 * as attributes. Everything else is data and optional to 
//	 * create a turtle (though necessary in order to maintain
//	 * a turtle's state across saves and loads of projects). 
//	 */
//	public String getSaveString() {
//		StringBuffer save = new StringBuffer();
//		StringBuffer save2 = new StringBuffer();
//		save.append("\r\n");
//		save.append(saveTitle);
//        save.append("\r\n");
//		save2.append("<?xml version=\"1.0\" encoding=\"UTF-16\"?>");
//        save2.append("<AGENTS>");
//        
//        // we're reading all of the agent state, so lock it
//        synchronized(sl.getLock()) {
//	        
//	        pointToFirstLiveTurtle();
//	        while(isValidLiveTurtle()) {
//	        	int who = getWho(); 
//	        	save2.append("<AGENT who=\""+who+"\" breed=\""+TerrainFileFormatUtils.escape(getBreed(who))+"\" shape=\""+getShape(who)+"\">");
//	        	save2.append("<LEVEL>"+getLevel()+"</LEVEL>");
//	        	save2.append("<INVISIBLE>"+isInvisible(who)+"</INVISIBLE>");
//	        	save2.append("<PENDOWN>"+isPendown(who)+"</PENDOWN>");
//	        	save2.append("<XCOR>"+getXcor(who)+"</XCOR>");
//	        	save2.append("<YCOR>"+getYcor(who)+"</YCOR>");
//	        	save2.append("<HEADING>"+getHeading(who)+"</HEADING>");
//	        	save2.append("<SIZE>"+getSize(who)+"</SIZE>");
//	        	save2.append("<COLOR>"+getColorNumber(who)+"</COLOR>");
//	        	save2.append("<HEIGHT>"+getHeightAboveTerrain(who)+"</HEIGHT>");
//	        	save2.append("<XSCALE>"+getXScale(who)+"</XSCALE>");
//	        	save2.append("<YSCALE>"+getYScale(who)+"</YSCALE>");
//	        	save2.append("<ZSCALE>"+getZScale(who)+"</ZSCALE>");
//	        	for(Variable v : varList) {
//	        		if(v.getScope() == Variable.VariableScope.AGENT && !v.isList()) {
//	        			String name = TerrainFileFormatUtils.escape(v.getName()); 
//	        			switch(v.getType()) {
//	        				case BOOLEAN :
//	        					save2.append("<VARIABLE scope=\"agent\" type=\"boolean\" name=\""+name+"\">"+getBooleanVariableValue(who, v)+"</VARIABLE>"); 
//	        					break; 
//	        				case NUMBER :
//	        					save2.append("<VARIABLE scope=\"agent\" type=\"number\" name=\""+name+"\">"+getNumberVariableValue(who, v)+"</VARIABLE>");
//	        					break;
//	        				case STRING :
//	        					String value = getStringVariableValue(who, v); 
//	        					if(value == null)
//	        						value = "";
//	        					value = TerrainFileFormatUtils.escape(value); 
//	        					save2.append("<VARIABLE scope=\"agent\" type=\"string\" name=\""+name+"\">"+value+"</VARIABLE>");
//	        					break;
//	        			}
//	        		}
//	        	}
//	        	// add more data elements here        	
//	        	save2.append("</AGENT>");
//	        	pointToNextLiveTurtle();
//	        }
//        }
//        save2.append("</AGENTS>");
//        save.append(TerrainFileFormatUtils.getGzippedString(save2.toString()));
//        save.append(saveEnd);
//        save.append("\r\n");
//		return save.toString(); 
//	}
//	
//	/**
//	 * Loads from the save string. This assumes that the correct breeds have
//	 * been created and that of the agents from the previous project have
//	 * been killed. 
//	 */
//	public void loadSaveString(String allContents) {
//		
//		String contents = new String(allContents);
//		
//		int start = contents.indexOf(saveTitle);
//        if (start < 0)
//            return;
//        int end = contents.indexOf(saveEnd, start + saveTitle.length());
//        if (end < 0)
//            end = contents.length();
//        contents = contents.substring(start + saveTitle.length(), end);
//
//        // only gunzip if it was gzipped to begin with
//        if (!contents.contains("<?xml version"))
//        	contents = TerrainFileFormatUtils.getGunzippedString(contents);
//
//        // we need this line to remove the blank spaces before the prologue
//        contents = contents.substring(contents.indexOf("<"));
//                
//        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//        DocumentBuilder builder;
//        Document doc;
//        String breed;
//		String breedIcon; 
//		String dataName; 
//		String dataValue;
//		String name;
//		String scope;
//		String type;
//		VariableScope vscope; 
//		VariableType vtype; 
//
//        try {
//            builder = factory.newDocumentBuilder();
//            doc = builder.parse(new InputSource(new StringReader(contents)));
//            Element root = doc.getDocumentElement();
//            if (root.getNodeName().equals("AGENTS")) {
//            	
//            	// we're writing all-new agent state, so lock the VM
//                synchronized(sl.getLock()) {
//        	                   	
//	            	for(int i = 0; i < root.getChildNodes().getLength(); i++) {
//	            		Node n = root.getChildNodes().item(i); 	// should be "AGENT"
//	            		if(n.getNodeName().equals("AGENT")) { 	// this should happen
//	            			// these attributes better exist
//	            			int who = Integer.parseInt(n.getAttributes().getNamedItem("who").getNodeValue());
//	            			breed = n.getAttributes().getNamedItem("breed").getNodeValue();
//	            			breedIcon = n.getAttributes().getNamedItem("shape").getNodeValue();      
//	            			createTurtleWho(who, StarLogo.getBreedSlnum(breed), StarLogo.addToHeap(breedIcon));
//	            			// now set the rest of the data elements
//	            			for(int j = 0; j < n.getChildNodes().getLength(); j++) {            				
//	            				Node d = n.getChildNodes().item(j); 	
//	            				dataName = d.getNodeName(); 
//	            				dataValue = d.getTextContent(); 
//	            				
//	            				if(dataName.equals("LEVEL")) {
//	            					setLevel(who, Integer.parseInt(dataValue)); 
//	            				}
//	            				else if(dataName.equals("INVISIBLE")) {
//	            					setInvisible(who, dataValue.equals("true"));
//	            				}
//	            				else if(dataName.equals("PENDOWN")) {            					
//	            					setPendown(who, dataValue.equals("true"));
//	            				}
//	            				else if(dataName.equals("XCOR")) {
//	            					setXcor(who, Double.parseDouble(dataValue));
//	            				}
//	            				else if(dataName.equals("YCOR")) {
//	            					setYcor(who, Double.parseDouble(dataValue));
//	            				}
//	            				else if(dataName.equals("HEADING")) {
//	            					setHeading(who, Double.parseDouble(dataValue));
//	            				}
//	            				else if(dataName.equals("SIZE")) {
//	            					setSize(who, Double.parseDouble(dataValue));
//	            				}
//	            				else if(dataName.equals("COLOR")) {
//	            					setColorNumber(who, Double.parseDouble(dataValue));
//	            				}
//	            				else if(dataName.equals("HEIGHT")) {
//	            					setHeightAboveTerrain(who, Double.parseDouble(dataValue));
//	            				}
//	            				else if(dataName.equals("XSCALE")) {
//	            					setXScale(who, Double.parseDouble(dataValue));
//	            				}
//	            				else if(dataName.equals("YSCALE")) {
//	            					setYScale(who, Double.parseDouble(dataValue));
//	            				}
//	            				else if(dataName.equals("ZSCALE")) {
//	            					setZScale(who, Double.parseDouble(dataValue));
//	            				}
//	            				else if(dataName.equals("VARIABLE")) {
//	            					name = d.getAttributes().getNamedItem("name").getNodeValue();
//	            					scope = d.getAttributes().getNamedItem("scope").getNodeValue();
//	            					type = d.getAttributes().getNamedItem("type").getNodeValue();
//	           					         					
//	            					if(scope.equals("agent"))
//	            						vscope = VariableScope.AGENT;
//	            					else
//	            						continue; // must be agent variable
//	            					
//	            					if(type.equals("number"))
//	            						vtype = VariableType.NUMBER;
//	            					else if(type.equals("boolean"))
//	            						vtype = VariableType.BOOLEAN; 
//	            					else // assume string
//	            						vtype = VariableType.STRING;
//	            					
//	            					Variable v = new Variable(name, vtype, vscope, false);
//	        						
//	        						// This assumes that the variables have been compiled and are a part of the varList
//				        			switch(v.getType()) {
//				        				case BOOLEAN :
//				        					setVariableValue(who, v, Boolean.parseBoolean(dataValue));  
//				        					break; 
//				        				case NUMBER :
//				        					setVariableValue(who, v, Double.parseDouble(dataValue)); 
//				        					break;
//				        				case STRING :
//				        					setVariableValue(who, v, dataValue); 
//				        					break;
//				        			}
//	            				}
//	            				// add more checks here if more data elements are added
//	            			}
//	            		}
//	            	}
//                }
//            }
//        }
//        catch(Exception e) {
//        	e.printStackTrace(); 
//        }
//	}
//	
//	private native void incRefCount(int who);
//    private native void decRefCount(int who);
//    private native void addToHead(int who); // fixes a turtle's next pointer when restored
//    
//    /**
//     * Saves all agents on the level given by levelIndex
//     */
//    public void saveTurtleState(int levelIndex) {
//    	// first decRefCount for each turtle in the old saved state
//    	if(!savedTurtles.containsKey(levelIndex))
//    		savedTurtles.put(levelIndex, new ArrayList<TurtleState>());
///*    	for(TurtleState ts : savedTurtles.get(levelIndex)) 
//    		decRefCount(ts.who); djwendel-not using ref_counts because they take slots out of play!
//*/    	savedTurtles.get(levelIndex).clear(); 
//    	
//    	pointToFirstLiveTurtle();
//        while(isValidLiveTurtle()) {
//        	int who = getWho(); 
//        	if(getLevel() == levelIndex) {
//        		// do this so we don't reuse a slot if this turtle dies
//        		//incRefCount(who); djwendel-not using ref_counts because they take slots out of play!
//        		TurtleState ts = new TurtleState();
//        		// now set turtle state
//        		ts.who = who; 
//        		ts.level = levelIndex; 
//        		ts.breed = getBreed(who);
//        		ts.shape = getShape(who);
//        		ts.invisible = isInvisible(who); 
//        		ts.pendown = isPendown(who);
//        		ts.xcor = getXcor(who); 
//        		ts.ycor = getYcor(who);
//        		ts.heading = getHeading(who); 
//        		ts.size = getSize(who);
//        		ts.color = getColorNumber(who);
//        		ts.height = getHeightAboveTerrain(who);
//        		ts.xscale = getXScale(who);
//        		ts.yscale = getYScale(who);
//        		ts.zscale = getZScale(who);
//        		ts.variables = new HashMap<Variable, String>();
//        		String value = null; 
//        		for(Variable v : varList) {
//        			if(v.getScope() == Variable.VariableScope.AGENT && !v.isList()) {
//            			if(v.getType() == Variable.VariableType.BOOLEAN)
//            				value = getBooleanVariableValue(who, v) ? "true" : "false"; 
//            			if(v.getType() == Variable.VariableType.NUMBER)
//            				value = ""+getNumberVariableValue(who, v); 
//            			if(v.getType() == Variable.VariableType.STRING)
//            				value = getStringVariableValue(who, v);
//            			if(value == null)
//                			value = "";
//            			ts.variables.put(v, value); 
//            		}            		
//        		}
//        		savedTurtles.get(levelIndex).add(ts); 
//        	}
//        	pointToNextLiveTurtle(); 
//        }
//    }
//    
//    /**
//     * Restores all agents on the level with the given index.
//     */
//    public void restoreTurtleState(int levelIndex) {
//    	if(!savedTurtles.containsKey(levelIndex))
//    		return; 
//    	for(TurtleState ts : savedTurtles.get(levelIndex)) {
//    		setInvisible(ts.who, ts.invisible); 
//    		setLevel(ts.who, ts.level); 
//    		setShape(ts.who, ts.shape);
//    		setBreed(ts.who, ts.breed);
//    		setPendown(ts.who, ts.pendown); 
//    		setXcor(ts.who, ts.xcor);
//    		setYcor(ts.who, ts.ycor); 
//    		setHeading(ts.who, ts.heading); 
//    		setSize(ts.who, ts.size); 
//    		setColorNumber(ts.who, ts.color); 
//    		setXScale(ts.who, ts.xscale); // must come after setSize
//    		setYScale(ts.who, ts.yscale); // must come after setSize
//    		setZScale(ts.who, ts.zscale); // must come after setSize
//    		setHeightAboveTerrain(ts.who, ts.height); 
//    		if(!isAlive(ts.who)) {
//    			setAlive(ts.who, true);
//    			addToHead(ts.who); 
//    		}
//    		for(Variable v : ts.variables.keySet()) {
//    			String value = ts.variables.get(v);
//    			if(v.getScope() == Variable.VariableScope.AGENT && !v.isList()) {
//        			if(v.getType() == Variable.VariableType.BOOLEAN)
//        				setVariableValue(ts.who, v, Boolean.parseBoolean(value)); 
//        			if(v.getType() == Variable.VariableType.NUMBER)
//        				setVariableValue(ts.who, v, Double.parseDouble(value)); 
//        			if(v.getType() == Variable.VariableType.STRING)
//        				setVariableValue(ts.who, v, value);
//        		}	
//    		}
//    	}
//    }
//    
//    /** 
//     * Writes the saved turtles to an XML string for saving 
//     */
//    public String getSavedTurlesString() {
//		StringBuffer save = new StringBuffer();
//		StringBuffer save2 = new StringBuffer();
//		save.append("\r\n");
//		save.append(saveTurtlesTitle);
//        save.append("\r\n");
//		save2.append("<?xml version=\"1.0\" encoding=\"UTF-16\"?>");
//        save2.append("<SAVEDTURTLES>");
//        
//        for(Integer i : savedTurtles.keySet()) {
//        	ArrayList<TurtleState> state = savedTurtles.get(i);
//        	save2.append("<LEVELINDEX i=\""+i+"\">"); 
//        	for(TurtleState ts : state) {        	
//        		save2.append("<TURTLESTATE>");
//        		save2.append("<WHO>"+ts.who+"</WHO>");
//            	save2.append("<LEVEL>"+ts.level+"</LEVEL>");
//            	save2.append("<BREED>"+ts.breed+"</BREED>");
//            	save2.append("<SHAPE>"+ts.shape+"</SHAPE>");         	
//            	save2.append("<INVISIBLE>"+ts.invisible+"</INVISIBLE>");
//            	save2.append("<PENDOWN>"+ts.pendown+"</PENDOWN>");            	
//            	save2.append("<XCOR>"+ts.xcor+"</XCOR>");
//            	save2.append("<YCOR>"+ts.ycor+"</YCOR>");
//            	save2.append("<HEADING>"+ts.heading+"</HEADING>");
//            	save2.append("<SIZE>"+ts.size+"</SIZE>");
//            	save2.append("<COLOR>"+ts.color+"</COLOR>");
//            	save2.append("<HEIGHT>"+ts.height+"</HEIGHT>");
//            	save2.append("<XSCALE>"+ts.xscale+"</XSCALE>");
//            	save2.append("<YSCALE>"+ts.yscale+"</YSCALE>");
//            	save2.append("<ZSCALE>"+ts.zscale+"</ZSCALE>");	
//            	for(Variable v : ts.variables.keySet()) {
//            		if(v.getScope() == Variable.VariableScope.AGENT && !v.isList()) {
//            			String name = TerrainFileFormatUtils.escape(v.getName()); 
//            			String type = null; 
//            			String value = ts.variables.get(v);
//            			value = TerrainFileFormatUtils.escape(value); 
//            			switch(v.getType()) {
//            				case BOOLEAN :
//            					type = "boolean";
//            					break; 
//            				case NUMBER :
//            					type = "number";
//            					break;
//            				case STRING :
//            					type = "string";            
//            					break;
//            			}            			
//    					save2.append("<VARIABLE scope=\"agent\" type=\""+type+"\" name=\""+name+"\">"+value+"</VARIABLE>");            			
//            		}
//            	}            	
//            	save2.append("</TURTLESTATE>");
//        	}
//        	save2.append("</LEVELINDEX>"); 
//        }      
//        save2.append("</SAVEDTURTLES>");
//        save.append(TerrainFileFormatUtils.getGzippedString(save2.toString()));
//        save.append(saveEnd);
//        save.append("\r\n");
//		return save.toString(); 
//	}
//    
//    public void loadSavedTurltesString(String allContents) {		
//		String contents = new String(allContents);		
//		int start = contents.indexOf(saveTurtlesTitle);
//        if (start < 0)
//            return;
//        int end = contents.indexOf(saveEnd, start + saveTurtlesTitle.length());
//        if (end < 0)
//            end = contents.length();
//        contents = contents.substring(start + saveTurtlesTitle.length(), end);
//
//        // only gunzip if it was gzipped to begin with
//        if (!contents.contains("<?xml version"))
//        	contents = TerrainFileFormatUtils.getGunzippedString(contents);
//
//        // we need this line to remove the blank spaces before the prologue
//        contents = contents.substring(contents.indexOf("<"));
//        
//        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//        DocumentBuilder builder;
//        Document doc;
//        try {
//            builder = factory.newDocumentBuilder();
//            doc = builder.parse(new InputSource(new StringReader(contents)));
//            Element root = doc.getDocumentElement();
//            if (root.getNodeName().equals("SAVEDTURTLES")) {            	         	
//            	for(int i = 0; i < root.getChildNodes().getLength(); i++) {
//            		Node n = root.getChildNodes().item(i);
//            		if(n.getNodeName().equals("LEVELINDEX")) {
//            			// fix up old turtle state so that the ref counts are correct
//            			int levelIndex = Integer.parseInt(n.getAttributes().getNamedItem("i").getNodeValue());
//            			if(!savedTurtles.containsKey(levelIndex))
//            	    		savedTurtles.put(levelIndex, new ArrayList<TurtleState>());
//            	    	//for(TurtleState ts : savedTurtles.get(levelIndex)) 
//            	    		//decRefCount(ts.who); 
//            	    	savedTurtles.get(levelIndex).clear();
//            	    	
//            			// now set the rest of the data elements
//            			for(int j = 0; j < n.getChildNodes().getLength(); j++) {            				
//            				Node d = n.getChildNodes().item(j); 	
//            				if(d.getNodeName().equals("TURTLESTATE")) {
//            					TurtleState ts = new TurtleState(); 
//            					ts.variables = new HashMap<Variable, String>();
//            					for(int k = 0; k < d.getChildNodes().getLength(); k++) {
//            						Node e = d.getChildNodes().item(k);
//            						String dataName = e.getNodeName(); 
//                    				String dataValue = e.getTextContent();
//                    				if(dataName.equals("LEVEL")) {
//                    					ts.level = Integer.parseInt(dataValue); 
//                    				}
//                    				else if(dataName.equals("WHO")) {
//                    					ts.who = Integer.parseInt(dataValue);
//                    					//incRefCount(ts.who);
//                    				}
//                    				else if(dataName.equals("BREED")) {
//                    					ts.breed = dataValue; 
//                    				}
//                    				else if(dataName.equals("SHAPE")) {
//                    					ts.shape = dataValue; 
//                    				}
//                    				else if(dataName.equals("INVISIBLE")) {
//                    					ts.invisible = dataValue.equals("true");
//                    				}
//                    				else if(dataName.equals("PENDOWN")) {            					
//                    					ts.pendown = dataValue.equals("true");
//                    				}
//                    				else if(dataName.equals("XCOR")) {
//                    					ts.xcor = Double.parseDouble(dataValue);
//                    				}
//                    				else if(dataName.equals("YCOR")) {
//                    					ts.ycor = Double.parseDouble(dataValue);
//                    				}
//                    				else if(dataName.equals("HEADING")) {
//                    					ts.heading = Double.parseDouble(dataValue);
//                    				}
//                    				else if(dataName.equals("SIZE")) {
//                    					ts.size = Double.parseDouble(dataValue);
//                    				}
//                    				else if(dataName.equals("COLOR")) {
//                    					ts.color = Double.parseDouble(dataValue);
//                    				}
//                    				else if(dataName.equals("HEIGHT")) {
//                    					ts.height = Double.parseDouble(dataValue);
//                    				}
//                    				else if(dataName.equals("XSCALE")) {
//                    					ts.xscale = Double.parseDouble(dataValue);
//                    				}
//                    				else if(dataName.equals("YSCALE")) {
//                    					ts.yscale = Double.parseDouble(dataValue);
//                    				}
//                    				else if(dataName.equals("ZSCALE")) {
//                    					ts.zscale = Double.parseDouble(dataValue);
//                    				}
//                    				else if(dataName.equals("VARIABLE")) {
//                    					String name = e.getAttributes().getNamedItem("name").getNodeValue();
//                    					String scope = e.getAttributes().getNamedItem("scope").getNodeValue();
//                    					String type = e.getAttributes().getNamedItem("type").getNodeValue();
//                    					VariableScope vscope; 
//                    					VariableType vtype; 
//                    					         					
//                    					if(scope.equals("agent"))
//                    						vscope = VariableScope.AGENT;
//                    					else
//                    						continue; 
//                    					
//                    					if(type.equals("number"))
//                    						vtype = VariableType.NUMBER;
//                    					else if(type.equals("boolean"))
//                    						vtype = VariableType.BOOLEAN; 
//                    					else
//                    						vtype = VariableType.STRING;
//                    					
//                    					Variable v = new Variable(name, vtype, vscope, false);
//                						ts.variables.put(v, dataValue);       			        			
//                    				}                    				
//            					}            					
//            					savedTurtles.get(levelIndex).add(ts);             					
//            				}
//            			}
//            		}
//            	}
//            }
//        }
//        catch(Exception e) {
//        	e.printStackTrace(); 
//        }
//	}
//    
//    // record class for a saved turtle's state
//    private class TurtleState {
//    	int who, level;  
//    	String breed, shape;
//    	boolean invisible, pendown; 
//    	double xcor, ycor, heading, size, color, height, xscale, yscale, zscale; 
//    	HashMap<Variable, String> variables = new HashMap<Variable, String>();
//    }
}
