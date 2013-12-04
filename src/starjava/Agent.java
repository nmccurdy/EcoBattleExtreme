package starjava;

import java.awt.Color;
import java.io.PrintStream;
import java.util.AbstractList;

import starlogoc.StarLogo;
import torusworld.SLTerrain;

public class Agent implements Executable {

	private StarLogo sl;
//	private TurtleManagerJava tm;

	protected Application app;

	protected boolean alive = true;

	private int who;

	// caching vars to improve speed with collisisons
	private double xCached;
	private double yCached;
	private double lastX;
	private double lastY;
	private boolean altitudeChanged;
	private boolean positionChanged;
	private double altitudeCached;
	private boolean bcrChanged, bctChanged, bcbChanged;
	private double boundingCylinderRadiusCached;
	private double boundingCylinderTopCached;
	private double boundingCylinderBottomCached;

	private double heading = 0;

	private boolean penDown = false;

	private Color color = Color.blue;

	private double xScale = 1;

	private double yScale = 1;

	private double zScale = 1;
	private double modelRadius = .5;
	private double modelTop = 1;
	private double modelBottom = 0;
	private double size = 1;

	private double heightAboveTerrain;

	private boolean showSkin = true;

	private String shape;

	private boolean visible = true;
	
	private boolean monitored = false;
	
	private boolean doNotInterpolate = false;
	
	public Agent(Application app, String breedIcon) {
		this.app = app;
		this.sl = app.getStarLogo();
		this.shape = breedIcon;
		
		this.who = app.getNextWhoNumber();
				
//		System.out.println("Creating " + who);
//		sl.createTurtleWho(who, breed, breedIcon);

		bcrChanged = true;
		bctChanged = true;
		bcbChanged = true;

		altitudeChanged = true;
		positionChanged = false;
	}

	public boolean wasPositionChanged() {
		return positionChanged;
	}

	public void setPositionChanged(boolean b) {
		positionChanged = b;
	}
	
	private double maxXY(double x) {
		if (x > app.MAX_XY) {
			x = app.MAX_XY;
		}
		
		if (x < app.MIN_XY) {
			x = app.MIN_XY;
		}
		return x;
	}

	public void setXY(double x, double y) {
		x = maxXY(x);
		y = maxXY(y);

		altitudeChanged = true;
//		positionChanged = true;

		xCached = x;
		yCached = y;
		lastX = x;
		lastY = y;

	}

	public void setLastXY(double x, double y) {
		lastX = x;
		lastY = y;
	}

	public double getX() {
		return xCached;
		// return tm.getXcor(who);
	}

	public double getY() {
		return yCached;
		// return tm.getYcor(who);
	}

	public double getLastX() {
		return lastX;
	}
	
	public double getLastY() {
		return lastY;
	}
	
	public double getXAtTime(double t) {
		return getLastX() + (getX() - getLastX()) * t;
	}
	
	public double getYAtTime(double t) {
		return getLastY() + (getY() - getLastY()) * t;
	}
	
	public void setHeading(double degrees) {
		heading = degrees;
	}

	public double getHeading() {
		return heading;
	}

	public void right(double degrees) {
		setHeading((getHeading() + degrees) % 360.0);
	}

	public void left(double degrees) {
		setHeading((getHeading() - degrees) % 360.0);
	}

	public void setPenDown(boolean down) {
		penDown  = down;
	}

	public boolean isPenDown() {
		return penDown;
	}

	public void forward(double steps) {
		forward(steps, 1.0);
	}

	public void backward(double steps) {
		backward(steps, 1.0);
	}

	public void forward(double steps, double fraction) {
		double newX = getX() + (Math.sin(Math.toRadians(getHeading())) * steps * fraction);
		double newY = getY() + (Math.cos(Math.toRadians(getHeading())) * steps * fraction);

		walkToXY(newX, newY);
	}

	public void backward(double steps, double fraction) {
		double newX = getX() + (Math.sin(Math.toRadians(getHeading()-180)) * steps * fraction);
		double newY = getY() + (Math.cos(Math.toRadians(getHeading()-180)) * steps * fraction);

		walkToXY(newX, newY);
	}

	public void walkToXY(double x, double y) {
		positionChanged = true;
		
		x = maxXY(x);
		y = maxXY(y);
		
		xCached = x;
		yCached = y;		

		if (isPenDown()) {
			setPatchColor(x, y, getColor());
		}

	}

	public void setColor(Color color) {
		this.color  = color;
	}

	public void build(double x, double y, float amount) {
		dig(x, y, -amount);
	}

	public void dig(double x, double y, float amount) {
		PatchManagerJava pm = (PatchManagerJava) sl.getPatchManager();

		int patchX = pm.getPatchCoordX(x);
		int patchY = pm.getPatchCoordY(y);
		pm.getCurrentTerrain().setHeight(patchX, patchY,
				getHeight(x, y) - amount);
		pm.setModified(patchX, patchY);
	}

	public float getHeight(double x, double y) {
		PatchManagerJava pm = (PatchManagerJava) sl.getPatchManager();

		int patchX = pm.getPatchCoordX(x);
		int patchY = pm.getPatchCoordY(y);

		return pm.getCurrentTerrain().getHeight(patchX, patchY);
	}

	public Color getColor() {
		return color;
	}
	
//	public double getColorSL() {
//		return tm.getColorNumber(who);
//	}

	public boolean isColor(Color other) {
		return color.equals(other);
	}
	
	public void setPatchColor(double x, double y, Color color) {
		app.setPatchColor(x, y, color);
	}

	public double getXScale() {
		return xScale;
	}

	public double getYScale() {
		return yScale;
	}

	public double getZScale() {
		return zScale;
	}

	public void setXScale(float scale) {
		xScale = scale;
	}

	public void setYScale(float scale) {
		yScale = scale;
	}

	public void setZScale(float scale) {
		zScale = scale;
	}

	public double getBoundingCylinderRadius() {

		if (bcrChanged) {
			bcrChanged = false;
			boundingCylinderRadiusCached = calcBoundingCylinderRadius();
			if (boundingCylinderRadiusCached == 0) {
				// the first call returns 0 for some reason so need to force a second one
				bcrChanged = true;
				// let's default it to something reasonable
				boundingCylinderRadiusCached = 1;
			}
		}

		return boundingCylinderRadiusCached;
	}
	
	public void setModelBoundingCylinder(float radius, float top, float bottom) {
		this.modelRadius = radius/SLTerrain.getPatchSize();
		
		// not sure why this is *2, but that's what StarLogo does
		this.modelTop = top/(SLTerrain.getPatchSize() * 2);
		this.modelBottom = bottom/(SLTerrain.getPatchSize() * 2);
		
		bcrChanged = true;
		bctChanged = true;
		bcbChanged= true;
	}
	
	public double calcBoundingCylinderRadius(){
        return (modelRadius / 
                 Math.max(getXScale(), getZScale())) * size;

        
	}
	
	public double calcBoundingCylinderTop() {
	    return (modelTop / getYScale()) * size;
	}
	
	public double calcBoundingCylinderBottom() {
		return (modelBottom / getYScale()) * size;
	}
                

	public double getBoundingCylinderTop() {
		if (bctChanged || bcrChanged) {
			bctChanged = false;
			boundingCylinderTopCached = calcBoundingCylinderTop();

			if (boundingCylinderTopCached == 0) {
				// the first call returns 0 for some reason so need to force a second one
				bctChanged = true;
				// let's default it to something reasonable
				boundingCylinderTopCached = 1;
			}

		}
		return boundingCylinderTopCached;
	}

	public double getBoundingCylinderBottom() {
		if (bcbChanged || bcrChanged) {
			bcbChanged = false;
			boundingCylinderBottomCached = calcBoundingCylinderBottom();
		}

		return boundingCylinderBottomCached;
	}

	
	public void setSize(double size) {
		bcrChanged = true;
		bctChanged = true;
		bcbChanged = true;

		this.size = size;
	}

	public double getSize() {
		return size;
	}

	public void setHeightAboveTerrain(double height) {
		altitudeChanged = true;
		this.heightAboveTerrain = height;
	}

	public double getHeightAboveTerrain() {
		return heightAboveTerrain;
	}

	public double getAltitude() {

		if (altitudeChanged) {
			altitudeChanged = false;
			PatchManagerJava pm = (PatchManagerJava) sl.getPatchManager();

			int patchX = pm.getPatchCoordX(getX());
			int patchY = pm.getPatchCoordY(getY());

			altitudeCached = getHeightAboveTerrain()
					+ pm.getCurrentTerrain().getHeight(patchX, patchY);
		}

		return altitudeCached;
	}

	public void addCollisionHandler(CollisionHandler handler) {
		app.addCollision(this, handler);
	}

	public AbstractList<Agent> smell(double radius, SmellHandler smellHandler) {
		return app.smell(radius, this, smellHandler);
	}

	public void die() {
		app.killAgent(this);
	}

	public void copyPositionAndHeading(Agent other) {
		this.setXY(other.getX(), other.getY());
		this.setHeading(other.getHeading());
	}

	public int getWho() {
		return who;
	}

	public boolean isAlive() {
		return alive;
	}

	public void setAlive(boolean alive) {
		this.alive = alive;
	}

	public void setShowSkin(boolean showSkin) {
		this.showSkin  = showSkin;
	}

	public boolean getShowSkin() {
		return showSkin;
	}

	public double getHeadingTowards(double x, double y) {
		double vecX = x - this.getX();
		double vecY = y - this.getY();

		// convert vector to degrees

		double angle = Math.toDegrees(Math.atan2(vecY, vecX));

		// convert to starlogo directions

		angle = -(angle - 90);

		return angle;
	}

	public Color getPatchColorAhead() {
		double aheadX = getX() + Math.sin(Math.toRadians(getHeading()));
		double aheadY = getY() + Math.cos(Math.toRadians(getHeading()));
		
		return app.getPatchColor(aheadX, aheadY);
	}

	public void setShape(String shape) {
		this.shape = shape;
	}

	public String getShape() {
		return shape;
	}

	public void setVisible(boolean value) {
		visible = value;
	}
	
	public boolean isVisible() {
		return visible;
	}
	
	public boolean isRotateable() {
		// not sure what this is.
		
		return true;
	}
	
	public void setMonitored(boolean value) {
		monitored = value;
	}
	
	public boolean isMonitored() {
		return monitored;
	}
	
	@Override
	public void execute() {
	}

	public void setDoNotInterpolate(boolean value) {
		doNotInterpolate = value;
	}
	
	public boolean shouldNotInterpolate() {
		return doNotInterpolate;
	}
	
	public void outputStatusInfo(PrintStream os) {
		os.println(getClass().getName());
		os.println("Id: " + getWho());
		os.format("Pos: (%1.2f,%2.2f,%3.2f:%4.2f)%n", getX(), getY(),getAltitude(), getHeading());
	}

}
