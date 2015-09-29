package application;

import java.awt.Color;
import java.io.PrintStream;

import kids.Coyote;

import starjava.Agent;

/**
 * This is the class that specifies general properties about animals. All of the
 * animals that we create will be based off of this Animal class.
 * 
 * @author Neil
 * 
 */
public abstract class Animal extends Agent implements EcoObject,
		MovingObjectInterface {

	private static final double BIRTH_PENALTY = .2;
	double energy = 0;
	DemoApp app;
	Controller controller;
	boolean isCorpse = false;
	boolean isMonitored = false;

	MovingObject movingObject;

	private double prevHeightAboveTerrain = 0;
	private double prevX = 0;
	private double prevY = 0;

	/*
	 * This is the "constructor" for Animal. You can think of this as the code
	 * that goes in the Setup block in StarLogo. One difference is that we have
	 * to explicitly tell StarJava about the collisions.
	 */
	public Animal(DemoApp app, Controller controller,
			String breedIcon) {

		// Animal is actually a subclass of Agent. The first thing
		// we need to do is call the constructor of this "super class".
		// The super class is referenced by using the keyword super.
		super(app, breedIcon);


		this.app = app;
		this.controller = controller;

		movingObject = new MovingObject(getMAX_ENERGY(), 1.0/Math.sqrt(app.getWorldMultiplier()));

		setShowSkin(false);
		setColor(controller.getColor());

		if (app.getCounter() > 1) {
			System.out
					.println("Cheater!  You need to call the constructor that takes a parent animal as a parameter.");
		} else {
			// start energy at between 50% and 100% of max
			energy = (getMAX_ENERGY() / 2.0)
					+ (Math.random() * (getMAX_ENERGY() / 2.0));
		}
	}

	public Animal(DemoApp app, Controller controller,
			String breedIcon, Animal parent) {

		// Animal is actually a subclass of Agent. The first thing
		// we need to do is call the constructor of this "super class".
		// The super class is referenced by using the keyword super.
		super(app, breedIcon);


		this.app = app;
		this.controller = controller;

		movingObject = new MovingObject(getMAX_ENERGY(), 1.0/Math.sqrt(app.getWorldMultiplier()));

		setShowSkin(false);
		setColor(controller.getColor());

		energy = 0;
		copyPositionAndHeading(parent);

		if (!this.getClass().getName().equals(parent.getClass().getName())) {
			System.out.println("Cheater!  Trying to create: "
					+ this.getClass().getName() + " from "
					+ parent.getClass().getName());
		}
	}

	@Override
	public final void execute() {

		if (energy < 0 || getX() < controller.getLeftBoundary()
				|| getX() > controller.getRightBoundary()
				|| getY() < controller.getBottomBoundary()
				|| getY() > controller.getTopBoundary()) {
			turnIntoCorpse();
		}

		if (isCorpse() && getMass() <= 0) {
			die();
		} else {
			setDesiredVelocity(MovingObject.NO_VELOCITY);
			setDesiredVelocityZ(MovingObject.NO_VELOCITY);

			if (!isCorpse()) {
				doAnimalActions();
			}

			movingObject.adjustZVelocity();
			movingObject
					.adjustVelocityXY(getHeightAboveTerrain(), getHeading());

			if (!isCorpse()) {
				adjustEnergy();
			}

			movingObject.adjustMass(getEnergy());
			adjustPhysicalSize();

			movingObject.moveObject(this);

			super.execute();
		}
	}

	private void turnIntoCorpse() {
		energy = 0;
		movingObject.stopAllAcceleration();
		isCorpse = true;
		setColor(Color.black);
	}

	private void adjustPhysicalSize() {
		final int numSizes = 7;
		final double fudge = .1;

		int massInt = (int) ((getMass() / getMAX_ENERGY()) * numSizes);

		double sizeCalc = .5 + ((double) massInt / numSizes) * 2;
		double currentSize = getSize();
		if (Math.abs(currentSize - sizeCalc) > fudge) {
			setSize(sizeCalc);
		}
	}

	@Override
	public void outputStatusInfo(PrintStream os) {
		super.outputStatusInfo(os);

		os.format("%nAnimal:%n");
		os.format("energy: %1.2f%n", energy);
		os.format("mass: %1.2f%n", getMass());
		os.format("vX: %1.2f, accelX: %1.2f%n", getVelocityX(),
				getAccelerationX());
		os.format("vY: %1.2f, accelY: %1.2f%n", getVelocityY(),
				getAccelerationY());
		os.format("vZ: %1.2f, accelZ: %1.2f%n", getVelocityZ(),
				getAccelerationZ());

		isMonitored = true;
	}

	@Override
	public final void die() {
		java.lang.StackTraceElement[] trace = (new Throwable()).getStackTrace();
		if (trace.length >= 2) {
			String className = trace[1].getClassName();

			if (!className.startsWith("application")) {
				System.out.println("Cheater!  Trying to call die from: "
						+ trace[1].getClassName() + "."
						+ trace[1].getMethodName() + "()");
			} else {
				super.die();
			}
		} else {
			System.out.println("Cheater?");
		}
	}

	@Override
	public final void setXY(double x, double y) {
		if (app.getCounter() > 1) {
			java.lang.StackTraceElement[] trace = (new Throwable())
					.getStackTrace();
			if (trace.length >= 2) {
				String className = trace[1].getClassName();
				String funcName = trace[1].getMethodName();
				if (!(className.startsWith("application") || className
						.startsWith("starjava"))) {
					System.out.println("Cheater!  Trying to call setXY from: "
							+ trace[1].getClassName() + "."
							+ trace[1].getMethodName() + "()");
				} else {
					super.setXY(x, y);
				}
			} else {
				System.out.println("Cheater?");
			}
		} else {
			// you can set the x,y wherever you want if it's the
			// very beginning of the game
			super.setXY(x, y);
		}
	}

	public final void doDamage(Animal opponent) {
		opponent.increaseEnergy(-getDamageDone(opponent));
	}

	@Override
	public final void backward(double steps) {
		// do nothing
	}

	@Override
	public final void forward(double steps) {
		// do nothing
	}

	/*
	 * Every animal has a species-specific damage multiplier.
	 */

	public final double getDamageDone(Animal opponent) {

		// find the direction vector that points towards the opponent

		double dirX = opponent.getX() - getPrevX();
		double dirY = opponent.getY() - getPrevY();
		double dirZ = opponent.getHeightAboveTerrain()
				- getPrevHeightAboveTerrain();

		double magnitude = Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);

		if (magnitude != 0) {
			dirX = dirX / magnitude;
			dirY = dirY / magnitude;
			dirZ = dirZ / magnitude;
		}
		// find the velocities that are in that direction

		double velocityInDir = getVelocityX() * dirX + getVelocityY() * dirY
				+ getVelocityZ() * dirZ;
		double opponentVelocityInDir = opponent.getVelocityX() * dirX
				+ opponent.getVelocityY() * dirY + opponent.getVelocityZ()
				* dirZ;

		double combinedVelocity = Math.max(velocityInDir
				- opponentVelocityInDir, 0);

		// System.out.println(getClass().getName() + getWho() + ", "
		// + opponent.getClass().getName() + opponent.getWho());
		//
		// System.out.println(getVelocityX() + ", " + getVelocityY());
		// System.out.println(opponent.getVelocityX() + ", "
		// + opponent.getVelocityY());
		//
		// System.out.println(getPrevX() + "," + getPrevY() + " "
		// + opponent.getX() + "," + opponent.getY() + " " + velocityInDir
		// + " " + opponentVelocityInDir + " " + combinedVelocity);

		double damage = Math.max(getMass() * combinedVelocity
				* getDamageMultiplier() - opponent.getDefense(), 0);

		// System.out.println("damage: " + damage);

		return damage;
	}

	public abstract double getDefense();

	public boolean isCorpse() {
		return isCorpse;
	}

	double getMAX_ENERGY() {
		return 0;
	}

	double getDamageMultiplier() {
		return 0;
	}

	double getENERGY_PER_SLEEP() {
		return 0;
	}

	double getEXTRA_ENERGY_PER_STEP() {
		return 0;
	}

	/*
	 * Whenever eating, the amount of energy acquired is determined by the prey
	 * that is being eaten. The beingEatenBy function reduces the prey's energy
	 * by a corresponding amount.
	 * 
	 * @see application.EcoObject#eat(application.EcoObject)
	 */
	@Override
	public final double eat(EcoObject prey) {
		if (!isCorpse()) {
			double energyGained = prey.beingEatenBy(this);
			increaseEnergy(energyGained);

			return energyGained;
		} else {
			return 0;
		}
	}

	@Override
	public double beingEatenBy(EcoObject eater) {
		if (eater instanceof Herbivore || !isCorpse()) {
			return 0;
		} else {
			die();
			return getPERCENT_MAX_ENERGY_TRANSFERRED() * getMass();
		}
	}

	/*
	 * Decrease energy based off of height above ground and make sure that
	 * animals who leave the boundaries set by the controllers die immediately
	 * 
	 * @see starjava.Agent#execute()
	 */

	public final void doActions() {
		doAnimalActions();
	}

	public void doAnimalActions() {
		// to be overridden;
	}

	/*
	 * The only way that energy can be given to a new animal that is created is
	 * by calling this function. Some energy is removed from the parent when it
	 * is given to the baby.
	 */
	public final void giveEnergyToBaby(Animal baby, double percent) {
		// give some of your energy to your baby

		percent = Math.min(.5, percent);

		double myEnergy = getEnergy();

		double babyEnergy = myEnergy * percent;

		// reduce your energy by the amount that you are giving to the baby
		increaseEnergy(-babyEnergy - BIRTH_PENALTY * getEnergy());

		baby.increaseEnergy(babyEnergy);
	}

	protected final void increaseEnergy(double energyGained) {
		energy += energyGained;

		if (energy > getMAX_ENERGY()) {
			energy = getMAX_ENERGY();
		}

	}

	public double getEnergy() {
		return energy;
	}

	private void adjustEnergy() {
		// decrease energy by energy_per_sleep. Increase energy consumed

		double energyUsed = getENERGY_PER_SLEEP()
				+ movingObject.getEnergyNeeded();

		if (energyUsed > energy) {
			movingObject.stopAllAcceleration();

			// still charge for sleeping
			energyUsed = getENERGY_PER_SLEEP();
		}

		energy -= energyUsed;
	}

	@Override
	public double getAccelerationX() {
		return movingObject.getAccelerationX();
	}

	@Override
	public double getAccelerationY() {
		return movingObject.getAccelerationY();
	}

	@Override
	public double getAccelerationZ() {
		return movingObject.getAccelerationZ();
	}

	@Override
	public double getMass() {
		return movingObject.getMass();
	}

	@Override
	public double getVelocityX() {
		return movingObject.getVelocityX();
	}

	@Override
	public double getVelocityY() {
		return movingObject.getVelocityY();
	}

	@Override
	public double getVelocityZ() {
		return movingObject.getVelocityZ();
	}

	@Override
	public void setAccelerationX(double acceleration) {
		movingObject.setAccelerationX(acceleration);
	}

	@Override
	public void setAccelerationY(double acceleration) {
		movingObject.setAccelerationY(acceleration);
	}

	@Override
	public void setAccelerationZ(double accelerationZ) {
		movingObject.setAccelerationZ(accelerationZ);
	}

	@Override
	public void setDesiredVelocity(double velocity) {
		movingObject.setDesiredVelocity(velocity);
	}

	@Override
	public void setDesiredVelocityZ(double velocity) {
		movingObject.setDesiredVelocityZ(velocity);
	}

	@Override
	public double getVelocityMagnitude2() {
		return movingObject.getVelocityMagnitude2();
	}

	public abstract double getPERCENT_MAX_ENERGY_TRANSFERRED();

	@Override
	public void setHeightAboveTerrain(double height) {
		prevHeightAboveTerrain = getHeightAboveTerrain();
		super.setHeightAboveTerrain(height);
	}

	public double getPrevHeightAboveTerrain() {
		return prevHeightAboveTerrain;
	}

	// getLastX() is for a different purpose and the x,y value
	// isn't set unless the accurate collision handler is running
	public double getPrevX() {
		return prevX;
	}

	public double getPrevY() {
		return prevY;
	}

	@Override
	public final void walkToXY(double x, double y) {
		prevX = getX();
		prevY = getY();

		super.walkToXY(x, y);
	}

}
