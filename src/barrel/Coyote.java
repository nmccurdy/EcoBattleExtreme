package barrel;

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
	private static final int REPRO_CYCLE = 50;
	private static final double REPRO_CHANCE_PER_REPRO_CYCLE = .5;

	private double xDestination = 0;

	private boolean reachedDestination = false;

	// used for debugging
	private int lastBabyWho = -1;

	public Coyote(DemoApp app, Controller controller, double xDest) {
		super(app, controller, "Coyote", "animals/dog-default");

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

		setXY(controller.getLeftBoundary() + Math.random() * width, controller
				.getBottomBoundary()
				+ Math.random() * height);

		setSize(2);

		if (xDest == 0) {
			if (controller.getLeftBoundary() < 0) {
				xDestination = 20;
			} else {
				xDestination = -20;
			}
		} else {
			this.xDestination = xDest;
		}

		// set the age to a random age.
		// we have to remember to explicity set the age for the babies to
		// 0 otherwise we will be creating reproductive-age babies
		age = (int) (Math.random() * REPRO_CYCLE);

	}

	@Override
	public void doAnimalActions() {
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
		Coyote baby = new Coyote(app, controller, xDestination);
		app.addCollidableAgent(baby);
		app.addExecutable(baby);

		// if energy you don't giveto the baby, it will die
		// immediately. A percentage of your energy is
		// removed in the process.
		giveEnergyToBaby(baby, 2);

		baby.age = 4;
		baby.copyPositionAndHeading(this);

		// remember this baby for debugging purposes
		lastBabyWho = baby.getWho();
	}

	private void move() {
		// Basically what all of this code does is make the coyotes
		// chase the rabbits(as long as the coyotes are hungry and as long
		// as they aren't running away or attacking another carnivore.)

		if (!fightOrFlight()) {
			if (shouldLookForFood()) {
				if (!huntRabbits()) {

					// head towards other side if necessary
					if (getX() >= xDestination - 1
							&& getX() <= xDestination + 1) {
						reachedDestination = true;
					}

					if (app.doesFenceExist() == false && !reachedDestination) {
						setHeading(getHeadingTowards(xDestination, getY()));
						forward(1);
					} else {
						// walk around randomly since the coyote can't smell any
						// food.
						left(Math.random() * 30);
						right(Math.random() * 30);
						forward(1);
					}

				}
			}
		}

		// if the coyote reaches the edge of the world, have it turn around.
		// the || in the line below is the Java way of saying OR.
		if (getX() <= controller.getLeftBoundary() + 1
				|| getX() >= controller.getRightBoundary() - 1
				|| getY() <= controller.getBottomBoundary()
				|| getY() >= controller.getTopBoundary()) {
			left(180);
			right(Math.random() * 20);
			left(Math.random() * 20);
			forward(1);
		}
	}

	private boolean huntRabbits() {
		AbstractList<Agent> animals = smell(15, new SmellHandler() {
			@Override
			public boolean smellCondition(Agent smellee) {
				return smellee instanceof Herbivore
						&& !(smellee instanceof Rabbit)
						&& smellee.getX() >= controller.getLeftBoundary()
						&& smellee.getX() <= controller.getRightBoundary();
			}
		});

		if (animals.size() > 0) {
			app.sortByClosestTo(getX(), getY(), animals);
			Agent closest = animals.get(0);
			setHeading(getHeadingTowards(closest.getX(), closest.getY()));
			forward(2);

			return true;
		} else {
			return false;
		}
	}

	public boolean shouldLookForFood() {
		if (app.doesFenceExist() == true) {
			return energy < .65 * MAX_ENERGY;
		} else {
			return energy < 2 * MAX_ENERGY;
		}

	}

	/*
	 * Deal with other carnivores. Either attack them or run away from them
	 * depending on the amount of energy you have.
	 */
	public boolean fightOrFlight() {
		AbstractList<Agent> carnivores = smell(10, new SmellHandler() {
			@Override
			public boolean smellCondition(Agent smellee) {
				return smellee instanceof Carnivore
						&& !(smellee instanceof Coyote)
						&& smellee.getX() >= controller.getLeftBoundary()
						&& smellee.getX() <= controller.getRightBoundary();
			}
		});

		if (carnivores.size() > 0) {
			Carnivore enemy = (Carnivore) carnivores.get(0);

			if (enemy.getEnergy() < 0.2 * getEnergy()) {
				setHeading(getHeadingTowards(enemy.getX(), enemy.getY()));
			}

			if (getEnergy() < .4* MAX_ENERGY) {
				// run away
				left(180);
			}

			forward(2);

			setHeading(getHeadingTowards(enemy.getX(), enemy.getY()));
			if (getEnergy() < 70 * MAX_ENERGY) {
				// run away
				left(360);
			}

			forward(2);

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
