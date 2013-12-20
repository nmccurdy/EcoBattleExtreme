package starlogoc;

import java.util.Observable;
import java.util.Observer;

/**
 * This class provides an interface between SpaceWorld
 * and game elements such as joystick input and score.
 */
public class GameManager {
	// Currently all methods in GameManager probably could
	// be static, but it's not clear to me that it will always
	// be the case.  To be on the safe side, make the user get an
	// instance anyway.
	
//	/**
//	 * Enumeration of legal camera perspectives
//	 */
//	public enum Camera {USER_CONTROLLABLE, TURTLE_EYE, OVER_SHOULDER, ORTHOGRAPHIC};
	
	/** Static factory */
	private static GameManager gameManager;
	private boolean showMiniView = true;
	private String statusMessage = "";
	private GameManager(){}; // Suppress default constructor
	public static GameManager getGameManager() {
		if (gameManager == null)
			gameManager = new GameManager();
		return gameManager;
	}

	/** Get the score for the scoreboard from the VM */
//	public native boolean isScoreShown();
//	public native double getScore();
//	public native void setScore(double new_score);
//	public native void showScore();
//	public native void hideScore();
//	public native boolean isClockShown();
//	public native double getClock();
//	public native void showClock();
//	public native void hideClock();
//	public native void resetClock();
//    public native void showMiniView();
//    public native void hideMiniView();
//    public native boolean isMiniViewShown();   
//    public native String getStatusMessage(); 

	
	public boolean isScoreShown() {
		return false;
	}
	
	public double getScore() {
		return 0.0;
	}
	
	public void setScore(double new_score) {
		
	}
	
	public void showScore() {
		 
	}
	
	public void hideScore() {
		
	}
	
	public boolean isClockShown() {
		return false;
	}
	
	public double getClock() {
		return 0.0;
	}
	
	public void showClock() {
		
	}
	public void hideClock() {
		
	}

	public void resetClock() {
		
	}

	public void showMiniView() {
		showMiniView = true;
	}
    public void hideMiniView() {
    	showMiniView = false;
    }
    
    public boolean isMiniViewShown() {
    	return showMiniView;
    }
    
    public String getStatusMessage() {
    	return statusMessage;
    }
    
    public void setStatusMessage(String status) {
    	this.statusMessage = status;
    }
	/** Camera perspectives */
	public static final int AERIAL =  0;
	public static final int AGENT_EYE = 1;
	public static final int AGENT_SHOULDER = 2;
	
	private CameraViewObservable cameraViewObservable = new CameraViewObservable();
	
	private static class CameraViewObservable extends Observable {
		public void changedCameraView() {
			setChanged();
		}
	}
	
	private int currentView = AERIAL;
	private boolean isOverhead = false;
	
	public void addCameraViewListener(Observer o) {
		cameraViewObservable.addObserver(o);
		//System.out.println("Added observer for camera buttons");
	}
	
	public int getCameraView() {
//		if (isCameraViewChangedByBlocks()) {
//			currentView = getCameraViewOfBlocks();
//			if (cameraViewObservable.countObservers() > 0) {
//				//System.out.println("Notifying observers for camera buttons");
//				cameraViewObservable.changedCameraView();
//				cameraViewObservable.notifyObservers();
//			}
//		}
		return currentView;
//		return 0;
	}
//	private native boolean isCameraViewChangedByBlocks();
//	public native int getCameraViewOfBlocks();
	
	public boolean isOverhead() {
//		if (isOverheadChangedByBlocks()) {
//			isOverhead = getOverheadOfBlocks();
//			if (cameraViewObservable.countObservers() > 0) { 
//				cameraViewObservable.changedCameraView();
//				//System.out.println("Notifying observer that isOverhead = " + isOverhead);
//				cameraViewObservable.notifyObservers();
//			}
////		}
//		return isOverhead;
		return isOverhead;
	}
//	private native boolean isOverheadChangedByBlocks();
//	private native boolean getOverheadOfBlocks();
	
	public void setPressedCameraViewButton(int cameraView) {
//		setCameraView(cameraView);
		currentView = cameraView;
	}
//	private native void setCameraView(int cameraView);
	
	public void setOverhead(boolean isOverhead) {
		this.isOverhead = isOverhead;
//		setOverheadOfBlocks(isOverhead);
	}
//	private native void setOverheadOfBlocks(boolean isOverhead);
	
//	public native int whoNumberForCamera();
	
	public void setTurtleWhoNumberForCamera(int who) {
		// NJM
		// do we need this for first-person perspective?
	}
	
//	// methods to set camera view
//	/** this takes control from the VM */
//	public void turnTurtleEyeOn() {
//		turtleEyeOn = true;
//		overShoulderOn = false;
//		orthographicOn = false;
//		useButtonValue = true;
//	}
//	/** this returns control to the VM */
//	public void turnTurtleEyeOff() {
//		useButtonValue = false;
//	}
//	/** this takes control from the VM */
//	public void turnOverShoulderOn() {
//		turtleEyeOn = false;
//		overShoulderOn = true;
//		orthographicOn = false;
//		useButtonValue = true;
//	}
//	/** this returns control to the VM */
//	public void turnOverShoulderOff() {
//		useButtonValue = false;
//	}
//	
//	/** this takes control from the VM */
//	public void turnOrthographicOn() {
//		turtleEyeOn = false;
//		overShoulderOn = false;
//		orthographicOn = true;
//		useButtonValue = true;
//	}
//	
//	/** this returns control to the VM */
//	public void turnOrthographicOff() {
//		useButtonValue = false;
//	}
//
	// these methods are the ones that should be used to check camera view
	public boolean viewIsAerial() {
		return getCameraView() == AERIAL;
	}
	public boolean viewIsTurtleEye() {
		return getCameraView() == AGENT_EYE;
	}
	public boolean viewIsOverShoulder() {
		return getCameraView() == AGENT_SHOULDER;
	}
	public boolean viewIsOrthographic() {
		return isOverhead();
	}
}
