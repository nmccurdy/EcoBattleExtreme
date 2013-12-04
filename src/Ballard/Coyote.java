package Ballard;

import java.io.PrintStream;
import java.util.AbstractList;

import neilChallenge.Rabbit;

import starjava.Agent;
import starjava.SmellHandler;
import application.Carnivore;
import application.DemoApp;
import application.Herbivore;

public class Coyote extends Carnivore {

	protected DemoApp app;

	protected Controller controller;

	// reproduction is based off of age
	// protected int age;
	// private static final int REPRO_CYCLE = 100;
	// private static final double REPRO_CHANCE_PER_REPRO_CYCLE = .5;

	// used for debugging
	protected int lastBabyWho = -1;

	public Coyote(DemoApp app, Controller controller) {
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

		setXY(controller.getLeftBoundary() + Math.random() * width,
				controller.getBottomBoundary() + Math.random() * height);

		setSize(2);

		// set the age to a random age.
		// we have to remember to explicity set the age for the babies to
		// 0 otherwise we will be creating reproductive-age babies
		// age = (int) (Math.random() * REPRO_CYCLE);

	}

	@Override
	public void doAnimalActions() {
		reproduce();
		move();

		// call the super class so that the coyote can do carnivore and animal
		// kind of things
		super.doAnimalActions();
	}

	protected void reproduce() {
		// age++;
		// if (age % REPRO_CYCLE == 0) {
		// if (Math.random() < REPRO_CHANCE_PER_REPRO_CYCLE) {
		// createNewBaby();
		// }
		// }
		// }
		if (.5 * MAX_ENERGY == getEnergy()) {
			createNewBaby();
			{
			}
		}
	}

	protected void createNewBaby() {
		Coyote baby = new Coyote(app, controller);
		app.addCollidableAgent(baby);
		app.addExecutable(baby);

		// if you don't give energy to the baby, it will die
		// immediately. A percentage of your energy is
		// removed in the process.
		giveEnergyToBaby(baby, .25);

		// baby.age = 0;
		baby.copyPositionAndHeading(this);

		// remember this baby for debugging purposes
		lastBabyWho = baby.getWho();
	}

	protected void move() {
		// Basically what all of this code does is make the coyotes
		// chase the rabbits(as long as the coyotes are hungry and as long
		// as they aren't running away or attacking another carnivore.)

		if (!fightOrFlight()) {
			// if (!huntEnemyRabbits()) {
			if (shouldLookForFood()) {
				if (!huntRabbits()) {
					// walk around randomly since the coyote can't smell any
					// food.
					left(Math.random() * 30);
					right(Math.random() * 30);
					forward(1);
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

	protected boolean huntRabbits() {
		if (app.doesFenceExist() == true) {
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
				forward(2);

				return true;
			} else {
				return false;
			}
		}
		return isMonitored;
	}

	private boolean huntEnemyRabbits() {
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
			setHeightAboveTerrain(closest.getHeightAboveTerrain());
			setHeading(getHeadingTowards(closest.getX(), closest.getY()));
			forward(2);

			return true;
		} else {
			return false;
		}
	}

	public boolean shouldLookForFood() {
		return energy < .40 * MAX_ENERGY;
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
			if (getEnergy() < .6 * MAX_ENERGY) {
				// run away
				left(180);
			}

			forward(3);

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
