package application;

import java.awt.Color;
import java.io.PrintStream;

import starjava.Agent;

public final class Grass extends Agent implements EcoObject {

	private DemoApp app;

	// grass has an age which basically just determines when it will
	// reproduce
	private int age;

	// like everything else in the ecosystem, grass has an energy
	// which will be transferred to those who eat it.
	private double energy;

	// this variable keeps track of the number of moves that the grass has
	// made while it tries to find a free location to grow in
	private int numMoves = 0;

	private double originalXCor = 0;
	
	// these are the constants that are used to govern how rapidly the grass
	// grows and how much energy the grass contains.
	public static final double MAX_ENERGY = 300;

	private static final double PERCENT_ENERGY_TRANSFERRED = 1;
	private static final double ENERGY_PER_BITE = 2;
	private static final double GRASS_SIZE = 5.0;
	private static final int REPRO_STEPS = 50;
	private static final double REPRO_MIN_ENERGY = 4.0;
	private static final double GROWTH_AMOUNT = 2;

	private static final int MAX_MOVES = 5;

	public static final double GRASS_SPACING = GRASS_SIZE * 2.0;

	public Grass(DemoApp app) {
		super(app, "basic-shapes/cube-default");

		this.app = app;

		addCollisionHandler(GrassGrassCollision.collision);

		energy = MAX_ENERGY / 2;
		age = (int) (Math.random() * 100);

		setSize(GRASS_SIZE);
		adjustGrassImage();
		setColor(Color.green);
		setDoNotInterpolate(true);
	}

	@Override
	public void execute() {

		increaseAge();
		reproduce();
		growGrass();
		adjustGrassImage();
	}

	private void reproduce() {
		// only reproduce if the age is a multiple
		// of REPRO_STEPS
		if (age % REPRO_STEPS == 0) {
			if (energy > REPRO_MIN_ENERGY) {

				// First we create a baby grass
				Grass baby = new Grass(app);
				app.addCollidableAgent(baby);
				app.removeDrawableAgent(baby); //hack
				app.addExecutable(baby);

				// Now we make the baby grass be at the same location as the
				// parent.
				baby.copyPositionAndHeading(this);

				// set the initial size and age.

				baby.age = 0;
				baby.energy = 1.0;

				baby.originalXCor = getX();
				// set the location of the grass

				baby.moveToFreeSpace();
				
				app.addDrawableAgent(baby);
			}
		}
	}

	private void growGrass() {
		increaseEnergy(GROWTH_AMOUNT);
	}

	private void adjustGrassImage() {
		// stick most of the grass below the terrain, but then bump a little bit
		// above the terrain based on the grass size.

		// figure out the scale, first

		final double SCALE = 0.5;
		final double ZERO_ADJUSTMENT = 1.3;

		double grassHeight = energy * SCALE / MAX_ENERGY;
		double zeroHeight = -(GRASS_SIZE + ZERO_ADJUSTMENT);
		setHeightAboveTerrain(zeroHeight + grassHeight);
	}

	private void increaseAge() {
		age = age + 1;
	}

	public int getAge() {
		return age;
	}

	public double getLength() {
		return energy;
	}

	public void moveToFreeSpace() {
		numMoves = numMoves + 1;

		if (numMoves > MAX_MOVES) {
			die();
		} else {
			// choose one of the cardinal directions
			switch ((int) (Math.random() * 4)) {
			case 0:
				left(90);
				break;
			case 1:
				left(180);
				break;
			case 2:
				left(270);
				break;
			case 3:
				break;
			}

			forward(GRASS_SPACING);

			
			
			// by keeping the heading always set to 0, there is less
			// distracting eyecandy when starlogo renders the grass.
			setHeading(0);

			// don't let grass get too close to the edge.
			if (Math.abs(getX()) > 50 - GRASS_SPACING / 2.0
					|| Math.abs(getY()) > 50 - GRASS_SPACING / 2.0) {
				die();
			}
			
			// check to see if crossed fence
			
			if (app.doesFenceExist()) {
				if (originalXCor < 0 && getX() > 0 ||
						originalXCor > 0 && getX() < 0) {
					die();
				}
			}

		}
	}

	@Override
	public double eat(EcoObject prey) {
		// grass doesn't eat anyone.
		return 0;
	}

	@Override
	public final double beingEatenBy(Animal eater) {
		energy = energy - ENERGY_PER_BITE;

		if (energy <= 0) {
			die();
			return (energy + ENERGY_PER_BITE) * PERCENT_ENERGY_TRANSFERRED;
		} else {
			return ENERGY_PER_BITE * PERCENT_ENERGY_TRANSFERRED;
		}
	}

	@Override
	public final double getEnergy() {
		return energy;
	}

	@Override
	public void outputStatusInfo(PrintStream os) {
		super.outputStatusInfo(os);

		os.format("%nGrass:%n");
		os.format("energy: %1.2f%n", energy);
		os.format("age: %1d%n", age);
	}

	private void increaseEnergy(double amount) {
		energy = energy + amount;

		if (energy > MAX_ENERGY) {
			energy = MAX_ENERGY;
		}
	}

}
