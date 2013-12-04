package torusworld;

import java.util.EventListener;

/**
 * This class defined a bunch of events that occur within the
 * SLGUI class. It describes changes that can happen as a result
 * of pressing the various UI buttons. 
 * 
 * @author Michael
 */
public interface SLGUIListener extends EventListener {

	/**
	 * Fires when the camera view is changd 
	 * @param newCamera either PERSPECTIVE, AGENT VIEW or AGENT EYE
	 */
	public void cameraChanged(int newCamera); 
	
	/**
	 * Fires when the main view and the mini-view are swapped
	 * @param isOrtho true iff the new main view is the orthro camera
	 */
	public void viewSwapped(boolean isOrtho); 
	
	/**
	 * Fires when the user presses the Reset Camera button
	 */
	public void cameraReset(); 
	
	/**
	 * Fires when the edit terrain button is pressed
	 * @param isEditing true iff we are entering editing mode
	 */
	public void editToggled(boolean isEditing); 
	
	/**
	 * Fires when one of the terrain editing buttons is pressed
	 * @param operation a number corresponding to the operation performed
	 */
	public void terrainEdited(int operation);
	
	/**
	 * Fires when the user changes the color of a region of patches
	 * @param newColor the new color for the region
	 */
	public void colorChanged(int newColor); 
	
	/**
	 * Fires when the user wishes to see the next agent (only 
	 * applicable for AGENT EYE and AGENT VIEW cameras)
	 */
	public void nextAgent(); 
	
	/**
	 * Fires when the user wishes to see the previous agent (only 
	 * applicable for AGENT EYE and AGENT VIEW cameras)
	 */
	public void previousAgent(); 
	
	
}
