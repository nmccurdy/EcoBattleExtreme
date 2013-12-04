package starlogoc;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.event.EventListenerList;

public class PatchManager {

	public class PatchCoordinates {
		public int x;
		public int y;
		
		public PatchCoordinates(int x, int y) {
			super();
			this.x = x;
			this.y = y;
		}
	}
	
	protected LinkedList<PatchCoordinates> changedPatches;
	
	private TerrainData currentTerrain;
	private int currentIndex; 
	private int width, height; 
	private TerrainData[] terrains;
	private TerrainData[] terrainCopies; // not shared with C code! 
	private Map<Integer, String> names = new HashMap<Integer,String>();

	private boolean showSky = false;
	private boolean showPatches = true;
	
	private EventListenerList listeners = new EventListenerList(); 
	
	private int maxTerrains;
    
	private StarLogo sl;
	public boolean editorModifiedFlag;
//	public ShortBuffer changedPatches;

//	private native void initTerrains(ShortBuffer buf);
//	private native int getMaxTerrains();
//    private native void setPatchTerrain0(int index);
//    private native int getNumChangedPatches0();
//    private native void resetNumChangedPatches0();
//	private native boolean arePatchesShown0();
//	private native void setPatchesShown0(boolean shown);
//	private native boolean isSkyShown0();
//	private native void setSkyShown0(boolean shown);
	// Rendering patch colors only for slow systems?
//	public native boolean isPatchColorsOnly();

	//TODO New variables. We need these to determine whether we're
	//using the new or old format
	private boolean usingOldFormat = false;

	private boolean renderPathColorsOnly;
	
	public void setUsingOldFormat(boolean b) {
		usingOldFormat = b;
		sl.setUsingOldFormat(usingOldFormat);
	}
	
	/**
	 * Creates a new PatchManager with numPatches patches
	 **/
	public PatchManager(int width, int height, StarLogo sl) {		
		this.sl = sl;
		changedPatches = new LinkedList<PatchCoordinates>();
		
		// allocate a buffer to hold coordinates of changed patches
//		changedPatches = ByteBuffer.allocateDirect(width * height * 4).order(ByteOrder.nativeOrder()).asShortBuffer();		
		this.width = width; 
		this.height = height; 
		reset(); 
	}
	
	/**
	 * Resets this PM to it's initial state
	 */
	public void reset() {
		clearAllLevels();
//		initTerrains(changedPatches); // intialize terrain structures in VM
		maxTerrains = getMaxTerrains();
		terrains = new TerrainData[maxTerrains];
		terrainCopies = new TerrainData[maxTerrains];
		createTerrain(0, width, height);		
		setPatchTerrain(0);
		
		//TODO When we're resetting, assume we're using the old format.
		//Loading will change this to true
		setUsingOldFormat(false);
		Preferences prefs = Preferences.userRoot().node("StarLogo TNG").node("Coloring");
		prefs.putBoolean("SpaceLand PatchColoring", false);
        prefs.putBoolean("SpaceLand DrawingTools", true);

	}
	
	private int getMaxTerrains() {
		return 1;
	}

	public void addListener(PatchManagerListener l) {
		listeners.add(PatchManagerListener.class, l); 
	}
	
	public void removeListener(PatchManagerListener l) {
		listeners.remove(PatchManagerListener.class, l); 
	}
	
	public void createTerrain(int index, int width, int height) {
		createTerrain(index, "Level "+index, width, height); 
	}
	
	public void createTerrain(int index, String name, int width, int height) {
		
		/*if (index >= 0 && index < maxTerrains) {		
			terrains[index] = new TerrainData(index, width, height, sl);
			
			TerrainData tt = terrains[index];
			//Initialize all of the colors to WHITE
			for (int x = 0; x < this.width; x++) {
				for (int y = 0; y < this.height; y++) {
					tt.setColor(x, y, Color.WHITE);
				}
			}
			tt.fillRectangle(0, 0,
					tt.getAssociatedWidth(), tt.getAssociatedHeight(), Color.GREEN);
			
			terrainCopies[index] = new TerrainData(terrains[index]); 
			names.put(index, name);
			
			PatchManagerListener[] ls = listeners.getListeners(PatchManagerListener.class); 
        	for (int j = 0; j < ls.length; j++)
                ls[j].terrainCreated(index); 	
		}
		else {
			RuntimeException e = new RuntimeException("Illegal index for new terrain");
			e.printStackTrace();
			throw e;
		}*/
		
		createTerrain(index, name, width, height, 505, 505);
	}
	
	public void createTerrain(int index, String name, int width, int height,
			int tex_width, int tex_height) {
		if (index >= 0 && index < maxTerrains) {		
			terrains[index] = new TerrainData(index, width, height, sl, tex_width, tex_height);
			
			//TODO When we create a terrain, initialization depends on the format we're using
			//if (usingOldFormat == false) {
				//New format: White Patches and Green Texture
			//}
			/*else {
				//Old format: Green Patches and White Texture
				TerrainData tt = terrains[index];
				for (int x = 0; x < this.width; x++) {
					for (int y = 0; y < this.height; y++) {
						tt.setColor(x, y, Color.GREEN);
					}
				}
				tt.fillRectangle(0, 0, tt.getAssociatedWidth(), tt.getAssociatedHeight(), Color.WHITE);
			}*/			
			
			terrains[index].clearPatches();
			
			terrainCopies[index] = new TerrainData(terrains[index]); 
			names.put(index, name);
			
			PatchManagerListener[] ls = listeners.getListeners(PatchManagerListener.class); 
        	for (int j = 0; j < ls.length; j++)
                ls[j].terrainCreated(index); 	
		}
		else {
			RuntimeException e = new RuntimeException("Illegal index for new terrain");
			e.printStackTrace();
			throw e;
		}
		
	}

	public void setPatchTerrain(String name) {
		setPatchTerrain(getTerrainIndex(name));
	}
	
	public void setPatchTerrain(int index) {
		if (index >= maxTerrains || index < 0 || terrains[index] == null)
			return; 			
			
		if (currentTerrain != terrains[index]) {
//			synchronized(sl.getLock()) {
				editorModifiedFlag = true; 		// gets TorusWorld to redraw the terrain
				currentTerrain = terrains[index];
				currentIndex = index; 
				PatchManagerListener[] ls = listeners.getListeners(PatchManagerListener.class); 
	        	for (int j = 0; j < ls.length; j++)
	                ls[j].terrainChanged(index);
	        	
//				setPatchTerrain0(index);
//				setPatchesShown0(true);
//				setSkyShown0(true);
//			}
		}
	}
	
	public void renameVariable(Variable oldVar, Variable newVar) {
	    for (TerrainData terrain : terrains) {
	        if (terrain != null)
	            terrain.renameVariable(oldVar, newVar);
	    }
	}

//	public void reallocateVariables(List<Variable> newVarList) {
//		// tell each terrain to reallocate its variables
//		for (TerrainData terrain: terrains) {
//			if (terrain == null) continue;
//			terrain.reallocateVariables(newVarList);
//		}
//	}
//	
	public TerrainData getCurrentTerrain() {
		return currentTerrain;
	}
	
	public int getCurrentTerrainIndex() {
		return currentIndex; 
	}
	
	public TerrainData getTerrain(int index) {
		return terrains[index];
	}
	
	/**
	 * Gets the backup terrain by index. Use this with caution
	 * as the terrain copy represents the backup copy of a given 
	 * terrain.
	 */
	public TerrainData getTerrainCopy(int index) {
		return terrainCopies[index];
	}
	
	public String getTerrainName(int index) {
		return new String(names.get(index)); 
	}
	
	/**
	 * Gets the first unused terrain index. Returns -1 if none
	 * exists. 
	 */
	public int getFirstUnusedIndex() {
		boolean newIndex = true;
		for(int i = 0; i < maxTerrains; i++) {
			if(names.get(i) == null) {
				//Once the newest level number is found, it's checked if a level
				//by the name about to be assigned already exists
				for(int j = 0; j < i; j++) {
					if(names.get(j) != null && names.get(j).equals("Level " + i))
						newIndex = false;
				}
				if(newIndex)
					return i; 
				else
					newIndex = true;
			}
		}
		return -1; 
	}
	
	/**
	 * Gets the terrain index associated with the given terrain 
	 * name. Returns -1 if there is no terrain with the given name.
	 */
	public int getTerrainIndex(String name) {
		for(Integer i : names.keySet()) 
			if(names.get(i).equals(name))
				return i; 
		return -1; 
	}
	
	/**
	 * Renames a terrain if there is a terrain with index i
	 */
	public void renameTerrain(int i, String newName) {
		if(names.containsKey(i))
			names.put(i, newName); 
		
		PatchManagerListener[] ls = listeners.getListeners(PatchManagerListener.class); 
    	for (int j = 0; j < ls.length; j++)
            ls[j].terrainRenamed(i);
	}
	
	/**
	 * Deletes all of the levels in this
	 */
	public void clearAllLevels() {
		for(Integer i : names.keySet()) {	// have to re-implement delete to prevent 
			String name = names.get(i); 	// a concurrent modification exception
			terrains[i] = null;
			terrainCopies[i] = null; 
			PatchManagerListener[] ls = listeners.getListeners(PatchManagerListener.class); 
	    	for (int j = 0; j < ls.length; j++)
	            ls[j].terrainDeleted(name, i);
		}
		names.clear(); 
	}
	
	/**
	 * Deletes a terrain with a given name
	 */
	public void deleteTerrain(String name) {
		int i = getTerrainIndex(name); 
		if(i == -1)
			return; 
		names.remove(i); 
		terrains[i] = null; 
		terrainCopies[i] = null; 
		
    	// changes the patch terrain to be the first one in the 
    	// list -- guaranteed to work because we know there are
    	// at least 2 terrains
    	setPatchTerrain((Integer)names.keySet().toArray()[0]);
		
		PatchManagerListener[] ls = listeners.getListeners(PatchManagerListener.class); 
    	for (int j = 0; j < ls.length; j++)
            ls[j].terrainDeleted(name, i);
	}
	
	/**
	 * Saves a backup of the given terrain to memory
	 */
	public void saveTerrainSnapshot(String name) {
		int index = getTerrainIndex(name);
		if(index == -1)
			return; 
		terrainCopies[index] = new TerrainData(terrains[index]);
//		sl.getTurtleManager().saveTurtleState(index);
	}
	
	/**
	 * Sets the backup for a given terrain to be given by td
	 */
	public void saveTerrainSnapshot(String name, TerrainData td) {
		int index = getTerrainIndex(name);
		if(index == -1)
			return; 
		terrainCopies[index] = td;
//		sl.getTurtleManager().saveTurtleState(index);
	}
	
	/**
	 * Saves a backup of the current terrain to memory
	 */
	public void saveCurrentTerrainSnapshot() {
		terrainCopies[currentIndex] = new TerrainData(terrains[currentIndex]);
//		sl.getTurtleManager().saveTurtleState(currentIndex);
	}

	public void reloadTerrainFromSnapshot(String name) {
		int index = getTerrainIndex(name);
		if(index == -1)
			return; 		
		
//		sl.getTurtleManager().restoreTurtleState(index);
		
		// need to copy the patches buffer from the copy to
		// the original in order to keep the C code consistent
		// remember that the c code knows nothing of the terrainCopies
		ByteBuffer old = terrains[index].patches; 				
		ByteBuffer newP = terrainCopies[index].patches;
		terrains[index] = terrainCopies[index];
		int orig = newP.position();
		old.rewind();
		newP.rewind(); 
		old.put(newP); 
		old.position(orig);
		terrains[index].patches = old; 
		
		terrainCopies[index] = new TerrainData(terrains[index]);
		setPatchTerrain(index); 	
	}
	
	/**
	 * @return a Collection of all the terrian names
	 */
	public Collection<String> getLevelNames() {
		return Collections.unmodifiableCollection(names.values()); 
	}
	
	/** 
	 * Reloads the current level
	 */
	public void reloadCurrentTerrainSnapshot() {
		reloadTerrainFromSnapshot(names.get(currentIndex));
	}
	
	public int getNumTerrains() {
		return maxTerrains;
	}
	
    public int getNumChangedPatches() {
    	return changedPatches.size();
    }

    public void resetNumChangedPatches() {
    	changedPatches.clear();
    }
	
	/**
     * @effects synchronizes with StarLogo.getLock()
	 * @return the state of the VM flag governing whether or not to show the patches
	 */
	public boolean arePatchesShown() {
		return showPatches;
	}
	
	public boolean isSkyShown() {
		return showSky;
	}

	public boolean isPatchColorsOnly() {
		return renderPathColorsOnly;
	}
	
	public int getTotalPatches() {
		return width*height;
	}
	
	public Iterator<PatchCoordinates> getChangedPatchesIterator() {
		return changedPatches.iterator();
	}
}
	
