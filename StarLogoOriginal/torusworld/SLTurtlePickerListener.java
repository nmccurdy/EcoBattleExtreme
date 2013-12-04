package torusworld;

import java.util.EventListener;

public interface SLTurtlePickerListener extends EventListener {
	
	/**
	 * Fires when the mouse cursor hovers over a mobile
	 * @param hovered the Mobile that is being hovered
	 */
	public void mobileHovered(Mobile hovered); 
	
	/**
	 * Fires when no Mobile object is hovered by the mouse cursor
	 */
	public void nothingHovered(); 

}
