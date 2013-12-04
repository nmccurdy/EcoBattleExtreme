package torusworld;

import java.util.EventListener;
/**
 * This listener class is used to receive notifications about 
 * events that occur from withiin TorusWorld. The intendended
 * effect of calling the methods listed below is to cause the 
 * VM to run in some way. This may not always be the case, though.
 * The reason for this interface's existence is to keep the 
 * torusworld package from depending on any other package (outside
 * of the starlogo-c folder). 
 */
public interface TWListener extends EventListener {
	
	/**
	 * Fires when an agent is dragged around in torusworld
	 * @param who the ID of the agent being dragged
	 * @param x the new x coordinate
	 * @param y the new y coordinate
	 */
	public void agentDragged(int who, int x, int y); 
	
	/**
	 * Fired when an agent should be added to torusworld
	 * @param breed the breed that should be added
	 * @param x the new agent's x coordinate
	 * @param y the new agent's y coordinate
	 */
	public void agentAdded(String breed, int x, int y);
	
	// maybe add more events later (when necessary)

}
