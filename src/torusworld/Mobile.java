package torusworld;

import java.awt.Color;
import java.util.Comparator;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

import starjava.Agent;
import starlogoc.StarLogo;
//import starlogoc.TurtleManager;
import torusworld.math.Cylinder;
import torusworld.math.Vector3f;
import torusworld.model.AnimationData;
import torusworld.model.ModelManager;

public class Mobile {
	public MobilePosition.Basic pos = new MobilePosition.Basic();

	public MobilePosition.SpaceLand spaceLandPosition = new MobilePosition.SpaceLand();
	private boolean forceSpaceLandPositionUpdate = false;

	private MobileInterpolator interpolator = new MobileInterpolator();

	/** Debug Boolean */
	public static boolean DEBUG = false;

	/** Display say test for 5 seconds before fading */
	public static final int DEFAULT_SAY_TEXT_TIME = 5000;
	/** The duration during which say text fades out. */
	public static final int SAY_FADE_TIME = 2000;

	public int who, incarnationCount = -1;
	public float moveDistance;

	public float yHeightAboveZero;
	public Color color;
	/** scaling factor (1.0 = normal size, 2.0 = double size, etc.) */
	public float xScale = 1.f;
	public float yScale = 1.f;
	public float zScale = 1.f;

	/** Text this turtle is saying */
	public String lastSayText = "";

	/** Time at which text will begin to fade */
	private long sayTextFadeTime = 0;

	/**
	 * Amount of fade of the fading text: 1 = opaque, 0 = transparent. Never
	 * negative.
	 */
	private float sayTextFade = 0.f;

	/** Perpendicular distance from camera (used for z-sorting) */
	public float distFromCamera;

	private String shapeName = null;
	private AnimationData animData;
	public boolean shown = false;
	public boolean rotatable = true;
	public boolean monitored = false;
	public boolean showSkin = true;;

	public int stepNum;

	private boolean animDataOutdated = true;

	private Agent agent;

	/**
	 * Creates a new mobile object. initFromVM() should be called soon.
	 */
	public Mobile(int who, Agent agent) {
		this.who = who;
		this.agent = agent;
	}

	/**
	 * Reads non-interpolated parameters from VM (like color, scale, shown,
	 * shape, etc..)
	 * 
	 * @modifies this
	 */
	private void readVMParameters() {
		// TurtleManager tm = sl.getTurtleManager();
		shown = agent.isVisible();
		rotatable = agent.isRotateable();
		// I don't think we use this.
		// Njm TODO
		monitored = false;
		showSkin = agent.getShowSkin();

		xScale = (float) (agent.getXScale() * agent.getSize());
		yScale = (float) (agent.getYScale() * agent.getSize());
		zScale = (float) (agent.getZScale() * agent.getSize());

		color = agent.getColor();

		String newShape = agent.getShape();
		if (shapeName == null || !shapeName.equals(newShape)) {
			shapeName = newShape;
			animDataOutdated = true;
		}
		// shapeName = "animals/turtle-default";

		// String newSayText = agent.getSayText();
		// // newSayText = "";
		// if (newSayText != null && !newSayText.equals("")) {
		// lastSayText = newSayText;
		// sayTextFade = 1.0f;
		// sayTextFadeTime = System.currentTimeMillis()
		// + DEFAULT_SAY_TEXT_TIME;
		// sl.turtleDoneSaying(who);
		// }
	}

	/**
	 * Initializes this agent using the TurtleManager VM state. Should be called
	 * the first time an agent is born.
	 * 
	 * @modifies this, this.pos
	 */
	public void initFromVM() {
		shapeName = null;
		readVMParameters();

		pos.setFromVM(agent);
		interpolator.init(pos);

		forceSpaceLandPositionUpdate = true;
	}

	private MobilePosition.Basic tempPos = new MobilePosition.Basic();

	public void updateFromVM() {
		// TurtleManager tm = sl.getTurtleManager();
		readVMParameters();
		tempPos.setFromVM(agent);
		interpolator.setNext(tempPos);

		// int numBounces = tm.getNumBounces();
		// if (numBounces > 0) {
		// for (int i = 0; i < numBounces; i++) {
		// tempPos.setFromVMBounce(tm, i);
		// interpolator.addBounce(tempPos);
		// }
		// interpolator.processBounces();
		// }

		forceSpaceLandPositionUpdate = true;
	}

	public void updatePositioning() {
		// System.out.println("x: " + pos.x + ", z:" + pos.z + ", h="
		// +pos.height);
		yHeightAboveZero = SLTerrain.getPointHeight(pos.x, pos.z) + pos.height;
		Vector3f normal = SLTerrain.getNormal(pos.x, pos.z);
		getBoundingCylinder(spaceLandPosition.boundingCyl);
		spaceLandPosition.localCoordSys.fromHeadingAndYAxis(normal,
				(float) Math.toRadians(pos.heading));
		spaceLandPosition.localCoordSys.setOrigin(new Vector3f(pos.x,
				yHeightAboveZero, pos.z), new Vector3f(0,
				-spaceLandPosition.boundingCyl.bottom, 0));
		forceSpaceLandPositionUpdate = false;

	}

	/**
	 * Updates the positioning of the Mobile, given the interpolation parameter
	 * 0 <= t <= 1.
	 */
	public void updatePositioning(float t) {
		if (agent.shouldNotInterpolate()) {
			t = 1;
		}
		interpolator.interpolate(t, pos);

		if (forceSpaceLandPositionUpdate || isMoving()
				|| interpolator.isRotating())
			updatePositioning();

		if (animData != null)
			animData.update();
	}

	public AnimationData getAnimationData() {
		return animData;
	}

	public boolean isMoving() {
		return interpolator.isMoving();
	}

	public static class CameraDistanceComparator implements Comparator<Mobile> {
		public int compare(Mobile m1, Mobile m2) {
			if (m1.distFromCamera < m2.distFromCamera)
				return -1;
			else if (m1.distFromCamera > m2.distFromCamera)
				return 1;
			else
				return 0;
		}

		public boolean equals(Object o) {
			return (o == this);
		}
	}

	/**
	 * Sets returnCyl to the current bounding cylinder.
	 * 
	 * @returns returnCyl
	 */
	public Cylinder getBoundingCylinder(Cylinder returnCyl) {
		if (animData == null) {
			returnCyl.radius = 0;
			returnCyl.top = 0;
			returnCyl.bottom = 0;
			return returnCyl;
		}
		animData.getBoundingCylinder().computeScaledCylinder(xScale, yScale,
				zScale, returnCyl);
		return returnCyl;
	}

	// Must be called when TorusWorld.display() is in the stack trace
	public void updateAnimationData(GL gl, GLU glu) {
		if (animDataOutdated && shapeName != null) {
			animData = ModelManager.getInstance().newAnimationData(shapeName,
					gl, glu);
			Cylinder c = animData.getBoundingCylinder();
			agent.setModelBoundingCylinder(c.radius, c.top - c.bottom, 0);
			// StarLogo.setBoundingCylinder(who, c.radius, c.top - c.bottom, 0);
			animDataOutdated = false;
			updatePositioning();
		}
	}

	public void render(GL gl, GLU glu, int detail, boolean lightsOn) {
		if (animData != null)
			animData.render(gl, detail, color, showSkin);
	}

	public void setStanding() {
		if (animData != null) {
			animData.setStanding();
			Cylinder c = animData.getBoundingCylinder();
			agent.setModelBoundingCylinder(c.radius, c.top - c.bottom, 0);
			// remember, in the VM agent y=0 is the point that touches the
			// ground
			// StarLogo.setBoundingCylinder(who, c.radius, c.top - c.bottom, 0);
		}
	}

	public void setMoving() {
		if (animData != null) {
			animData.setMoving();
			Cylinder c = animData.getBoundingCylinder();
			agent.setModelBoundingCylinder(c.radius, c.top - c.bottom, 0);
			// StarLogo.setBoundingCylinder(who, c.radius, c.top - c.bottom, 0);
		}
	}

	/** Updates the tranparency of fading text based on the system timer. */
	public void updateSayTextFade() {
		if (sayTextFade > 0.0f) {
			long time = System.currentTimeMillis();
			if (time > sayTextFadeTime)
				sayTextFade = Math.max(0.0f, (float) (sayTextFadeTime
						+ SAY_FADE_TIME - time)
						/ (float) SAY_FADE_TIME);
			else
				sayTextFade = 1.0f;
		}
	}

	/**
	 * Amount of fade of the fading text: 1 = opaque, 0 = transparent. Never
	 * negative.
	 */
	public float sayTextFade() {
		return sayTextFade;
	}

	public String lastSayText() {
		return lastSayText;
	}

}
