package kids;

import java.io.PrintStream;
import java.util.AbstractList;

import starjava.Agent;
import starjava.SmellHandler;
import application.Carnivore;
import application.DemoApp;
import application.Herbivore;

public class Coyote extends Carnivore {

	private DemoApp app;

	private Controller controller;

	// reproduction is based off of age
	private int age;
	private static final int REPRO_CYCLE = 100;
	private static final double REPRO_CHANCE_PER_REPRO_CYCLE = .2;

	private static final int MAX_RUNAWAY_TIME = 5;

	private int runAwayFromFenceTime = 0;

	// used for debugging
	private int lastBabyWho = -1;

	public Coyote(DemoApp app, Controller controller) {
		super(app, controller, "animals/dog-default");

		this.app = app;
		this.controller = controller;

		addCollisionHandler(CoyoteCarnivoreCollision.collision);
		addCollisionHandler(CoyoteHerbivoreCollision.collision);

		// place the animal randomly inside the boundaries governed by the
		// controller
		double width = controller.getRightBoundary()
				- controller.getLeftBoundary();
		double height = controller.getTopBoundary()
				- controller.getBottomBoundary();

		setXY(controller.getLeftBoundary() + Math.random() * width,
				controller.getBottomBoundary() + Math.random() * height);

		setSize(2);

		// set the age to a random age.
		// we have to remember to explicity set the age for the babies to
		// 0 otherwise we will be creating reproductive-age babies
		age = (int) (Math.random() * REPRO_CYCLE);

	}

	public Coyote(DemoApp app, Controller controller, Coyote parent) {
		super(app, controller, "animals/dog-default", parent);

		this.app = app;
		this.controller = controller;

		addCollisionHandler(CoyoteCarnivoreCollision.collision);
		addCollisionHandler(CoyoteHerbivoreCollision.collision);

		// set the age to a random age.
		// we have to remember to explicity set the age for the babies to
		// 0 otherwise we will be creating reproductive-age babies
		age = (int) (Math.random() * REPRO_CYCLE);
	}

	@Override
	public void doAnimalActions() {
		setAccelerationX(0);
		setAccelerationY(0);
		setAccelerationZ(0);

		reproduce();
		move();

		// call the super class so that the coyote can do carnivore and animal
		// kind of things
		super.doAnimalActions();
	}

	private void reproduce() {
		age++;
		if (age % REPRO_CYCLE == 0) {
			if (Math.random() < REPRO_CHANCE_PER_REPRO_CYCLE) {
				createNewBaby();
			}
		}
	}

	private void createNewBaby() {
		Coyote baby = new Coyote(app, controller, this);
		app.addCollidableAgent(baby);
		app.addExecutable(baby);

		// if you don't give energy to the baby, it will die
		// immediately. A percentage of your energy is
		// removed in the process.
		giveEnergyToBaby(baby, .25);

		baby.age = 0;

		// remember this baby for debugging purposes
		lastBabyWho = baby.getWho();

		app.addDrawableAgent(baby);
	}

	private void move() {
		// Basically what all of this code does is make the coyotes
		// chase the rabbits(as long as the coyotes are hungry and as long
		// as they aren't running away or attacking another carnivore.)

		if (runAwayFromFenceTime > 0) {
			right(Math.random() * 20);
			left(Math.random() * 20);
			setDesiredVelocity(1);
			runAwayFromFenceTime--;
		} else {
			// if the coyote reaches the edge of the world, have it turn around.
			// the || in the line below is the Java way of saying OR.
			if (getX() <= controller.getLeftBoundary() + 1
					|| getX() >= controller.getRightBoundary() - 1
					|| getY() <= controller.getBottomBoundary()
					|| getY() >= controller.getTopBoundary()) {
				left(180);
				right(Math.random() * 20);
				left(Math.random() * 20);
				setDesiredVelocity(1);
				runAwayFromFenceTime = MAX_RUNAWAY_TIME;
			} else {
				if (!fightOrFlight()) {
					if (shouldLookForFood()) {
						if (!huntRabbits()) {
							// walk around randomly since the coyote can't smell
							// any
							// food.
							left(Math.random() * 30);
							right(Math.random() * 30);
							setDesiredVelocity(1);
						}
					}
				}
			}
		}
	}

	private boolean huntRabbits() {
		AbstractList<Agent> animals = smell(15, new SmellHandler() {
			@Override
			public boolean smellCondition(Agent smellee) {
				return smellee instanceof Herbivore
						&& smellee.getX() >= controller.getLeftBoundary()
						&& smellee.getX() <= controller.getRightBoundary();
			}
		});

		if (animals.size() > 0) {
			app.sortByClosestTo(getX(), getY(), animals);
			Agent closest = animals.get(0);
			setHeading(getHeadingTowards(closest.getX(), closest.getY()));
			setDesiredVelocity(4);

			return true;
		} else {
			return false;
		}
	}

	public boolean shouldLookForFood() {
		return getEnergy() < .95 * getMAX_ENERGY();
	}

	/*
	 * Deal with other carnivores. Either attack them or run away from them
	 * depending on the amount of energy you have.
	 */
	public boolean fightOrFlight() {
		AbstractList<Agent> carnivores = smell(5, new SmellHandler() {
			@Override
			public boolean smellCondition(Agent smellee) {
				return smellee instanceof Carnivore
						&& smellee.getX() >= controller.getLeftBoundary()
						&& smellee.getX() <= controller.getRightBoundary();
			}
		});

		if (carnivores.size() > 0) {
			Agent enemy = carnivores.get(0);

			setHeading(getHeadingTowards(enemy.getX(), enemy.getY()));
			if (getEnergy() < .9 * getMAX_ENERGY()) {
				// run away
				left(180);
			}

			setDesiredVelocity(2);

			return true;
		} else {
			return false;
		}
	}

	@Override
	public void outputStatusInfo(PrintStream os) {
		super.outputStatusInfo(os);

		os.format("%nCoyote:%n");
	}
}
