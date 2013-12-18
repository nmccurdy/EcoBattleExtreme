package starlogoc;

import java.awt.event.ActionEvent;

//import java.util.Map;
//import java.util.List;

public interface StarLogoCObserver {
//    public void globalVariableChanged(int index);
//	public void runForSomeTimeBlockDone(long id);
//	
	/**
	 * Select an agent from SpaceLand
	 * @param who
	 * @return true if the monitorp flag of the agent was false and subsequently set successfully
	 */
	public boolean selectedAgent(int who);
	
//	/**
//	 * Deselect an agent from SpaceLand
//	 * @param who
//	 * @return true if the monitorp flag of the agent was true and subsequently cleared successfully
//	 */
//	public boolean deselectedAgent(int who);
//	
//	/** Indicates that there are no living turtles.  Called every frame. */
//	public void noTurtlesAlive();
//	
//	/** Indicates that there are turtles to export */
//	public void turtlesToExport();
//	
//	/** Add a breed in the next export. */
//	public void addExportBreed(long slnum, String breedname);
//	
//	/** Indicates that there are keys to export */
//	public void keysToExport();
//	
//	/** A VM clock tick. */
//	public void vmTicked(double slTime);
//	
//	/** Returns true if any forever/runforsometime blocks are running. */
//	public boolean isAnythingRunning();
//	
//	/**
//	 * Informs SpaceLand's animator when it has focus to help with animation.
//	 */
//	public void setSpaceLandFocus(boolean hasFocus);
//	
//	public void handleMenuEvent(ActionEvent e);
//        
//        public int getSpeedSliderPosition();
}
