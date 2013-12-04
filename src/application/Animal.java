package application;

import java.io.PrintStream;

import starjava.Application;
import starjava.Agent;

/**
 * This is the class that specifies general properties about animals. All of the
 * animals that we create will be based off of this Animal class.
 * 
 * @author Neil
 * 
 */
public abstract class Animal extends Agent implements EcoObject {

	protected double energy;

	private Controller controller;
	private DemoApp app;
	
	// useful for debugging
	public boolean isMonitored = false;

	private double maxEnergyObtained = 0;
	
	/*
	 * This is the "constructor" for Animal. You can think of this as the code
	 * that goes in the Setup block in StarLogo. One difference is that we have
	 * to explicitly tell StarJava about the collisions.
	 */
	public Animal(DemoApp app, Controller controller, String breedName,
			String breedIcon) {

		// Animal is actually a subclass of Agent. The first thing
		// we need to do is call the constructor of this "super class".
		// The super class is referenced by using the keyword super.
		super(app, breedIcon);

		this.app = app;
		this.controller = controller;
		
		setShowSkin(false);
		setColor(controller.getColor());
		
		if (app.getCounter() > 1) {
			// don't allow people to cheat by constructing new animals w/o going
			// through "reproduce" process
			energy = 0;
		} else {
			// start energy at between 50% and 100% of max
			energy = (getMAX_ENERGY()/2.0) + (Math.random() * (getMAX_ENERGY()/2.0));
		}		
		
		setDoNotInterpolate(true);
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
		double energyGained = prey.beingEatenBy(this);
		increaseEnergy(energyGained);

		return energyGained;
	}

	/*
	 * The only way to increase energy is to call this function, and it can only
	 * be called by children that are in this package (e.g. herbivores and
	 * carnivores)
	 */
	final void increaseEnergy(double energyGained) {
		energy += energyGained;

		if (energy > getMAX_ENERGY()) {
			energy = getMAX_ENERGY();
		}
		
		if (energy > maxEnergyObtained) {
			maxEnergyObtained = energy;
		}
	}

	@Override
	public double beingEatenBy(Animal eater) {
		// this needs to be overriden
		return 0;
	}

	@Override
	public double getEnergy() {
		return energy;
	}

	@Override
	public void outputStatusInfo(PrintStream os) {
		super.outputStatusInfo(os);

		os.format("%nAnimal:%n");
		os.format("energy: %1.2f%n", energy);

		isMonitored = true;
	}

	/*
	 * Decrease energy based off of height above ground and make sure that animals 
	 * who leave the boundaries set by the controllers
	 * die immediately
	 * 
	 * @see starjava.Agent#execute()
	 */
	@Override
	public final void execute() {
		
		setDoNotInterpolate(false);
		
		// decrease energy by energy_per_sleep.  Increase energy consumed
		// if the animal is above or below the terrain.  Digging and flying
		// take extra energy.
		double factor = Math.abs(getHeightAboveTerrain()) + 1;
		energy -= getENERGY_PER_SLEEP() * factor * factor;

		if (energy < 0 || getX() < controller.getLeftBoundary()
				|| getX() > controller.getRightBoundary()
				|| getY() < controller.getBottomBoundary()
				|| getY() > controller.getTopBoundary()) {
			die();
		} else {
			doAnimalActions();
			super.execute();
		}
	}

	
	// to be overridden by animals
	public void doAnimalActions() {
		
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
		increaseEnergy(-babyEnergy);

		baby.increaseEnergy(babyEnergy);
	}

	/*
	 * Prevent people from cheating. You are not allowed to call the die()
	 * function yourself.
	 * 
	 * @see starjava.Agent#die()
	 */
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

	/*
	 * Prevent people from cheating. You are not allowed to teleport. Moving
	 * takes energy so you have to call the the forward and backward functions.
	 * 
	 * @see starjava.Agent#setXY(double, double)
	 */
	@Override
	public final void setXY(double x, double y) {
		if (app.getCounter() > 1) {
		java.lang.StackTraceElement[] trace = (new Throwable()).getStackTrace();
		if (trace.length >= 2) {
			String className = trace[1].getClassName();
			String funcName = trace[1].getMethodName();

			if (!(funcName.indexOf("<init>") >= 0 || className
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
			super.setXY(x,y);
		}
	}

	/*
	 * The only way to fight is to call this function
	 */

	public final void fight(Animal opponent) {
		opponent.increaseEnergy(-getFightEnergy());
	}

	@Override
	public final void backward(double steps) {
		decreaseEnergyForSteps(steps);
		super.backward(steps);
	}

	@Override
	public final void forward(double steps) {
		decreaseEnergyForSteps(steps);
		super.forward(steps);
	}

	/*
	 * The amount of energy consumed per step increases at a rate proportional
	 * to the square of the number of steps.
	 * 
	 */
	private void decreaseEnergyForSteps(double steps) {
		double rawStepEnergy = Math.max(Math.abs(steps), 1.0);  
		energy -= (rawStepEnergy * rawStepEnergy) * getEXTRA_ENERGY_PER_STEP();		
	}
	/*
	 * Every animal has a way to set the amount of damage that it does to an
	 * opponent when fighting.
	 */

	public final double getFightEnergy() {
		return energy * getPERCENT_ENERGY_PER_FIGHT();
	}

	double getMAX_ENERGY() {
		return 0;
	}

	double getPERCENT_ENERGY_PER_FIGHT() {
		return 0;
	}

	double getENERGY_PER_SLEEP() {
		return 0;
	}

	double getEXTRA_ENERGY_PER_STEP() {
		return 0;
	}

	double getMaxEnergyObtained() {
		return maxEnergyObtained;
	}

}
