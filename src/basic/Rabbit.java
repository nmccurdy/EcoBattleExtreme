package basic;

import java.io.PrintStream;
import java.util.AbstractList;

import starjava.Agent;
import starjava.SmellHandler;
import application.Carnivore;
import application.DemoApp;
import application.EcoObject;
import application.Grass;
import application.Herbivore;

public class Rabbit extends Herbivore {

	private DemoApp app;

	private Controller controller;

	// remember if the turtle is colliding with grass
	private boolean collidingWithGrass = false;
	
	// remember if the turtle is eating
	private boolean isEating = false;

	// remember how long the turtle has been running away
	private int runAwayTime = 0;
	private static final int MAX_RUN_AWAY_TIME = 5;

	// age is used for reproduction
	private int age;
	private static final int REPRO_CYCLE = 50;
	private static final double REPRO_CHANCE_PER_REPRO_CYCLE = .25;

	// used for debugging
	private int lastBabyWho = -1;


	/**
	 * This is the constructor for turtles.  It is the setup code for turtles.
	 * 
	 * @param app
	 * @param controller
	 */
	public Rabbit(DemoApp app, Controller controller) {
		super(app, controller, "Rabbit", "animals/rabbit-default");

		this.app = app;
		this.controller = controller;

		addCollisionHandler(RabbitGrassCollision.collision);
		addCollisionHandler(RabbitHerbivoreCollision.collision);


		double width = controller.getRightBoundary()
				- controller.getLeftBoundary();
		double height = controller.getTopBoundary()
				- controller.getBottomBoundary();

		setXY(controller.getLeftBoundary() + Math.random() * width, controller
				.getBottomBoundary()
				+ Math.random() * height);

		setSize(.5);
		
		// we have to remember to explicity set the age for the babies to
		// 0 otherwise we will be creating reproductive-age babies
		age = (int) (Math.random() * REPRO_CYCLE);

	}

	/*
	 * (non-Javadoc)
	 * @see application.Herbivore#execute()
	 */
	@Override
	public void doAnimalActions() {
		reproduce();
		move();

		// call the super class so that the coyote can do carnivore and animal
		// kind of things
		super.doAnimalActions();
	}

	/**
	 * Only reproduce if it's the right time in the repro cycle
	 */
	private void reproduce() {
		age++;
		if (age % REPRO_CYCLE == 0) {
			if (Math.random() < REPRO_CHANCE_PER_REPRO_CYCLE) {
				createNewBaby();
			}
		}
	}

	/**
	 * Create a new baby turtle and give it some of the parent's energy
	 */
	private void createNewBaby() {
		Rabbit baby = new Rabbit(app, controller);
		app.addCollidableAgent(baby);
		app.addExecutable(baby);

		// energy has to be given to the baby or else it will die
		giveEnergyToBaby(baby, .25);


		baby.age = (int)(Math.random() * 10);
		baby.copyPositionAndHeading(this);

		lastBabyWho = baby.getWho();
	}

	/**
	 * 
	 */
	private void move() {
		if (!runningAwayFromEnemy()) {
			if (shouldLookForFood()) {
				if (!collidingWithGrass) {
					if (!moveTowardsLongGrass()) {
						if (isReallyHungry()) {
							if (!moveTowardsAnyGrass()) {
								moveRandomly();
							}
						} else {
							moveRandomly();
						}
					}
				}
			}
		}

		collidingWithGrass = false;
		isEating = false;

		// if the Turtle reaches the edge of the world, have it turn around.
		// the || in the line below is the Java way of saying OR.
		if (getX() <= controller.getLeftBoundary() + 1
				|| getX() >= controller.getRightBoundary() - 1
				|| getY() <= controller.getBottomBoundary()
				|| getY() >= controller.getTopBoundary()) {
			left(180);
			right(Math.random() * 20);
			left(Math.random() * 20);
			forward(1);
			runAwayTime = MAX_RUN_AWAY_TIME;
		}
	}

	private void moveRandomly() {
		left(Math.random() * 60);
		right(Math.random() * 60);
		forward(1);
	}

	private boolean moveTowardsAnyGrass() {
		// look for any grass
		AbstractList<Agent> anyGrasses = smell(10,
				new SmellHandler() {
					@Override
					public boolean smellCondition(
							Agent smellee) {
						return smellee instanceof Grass
						&& smellee.getX() >= controller.getLeftBoundary()
						&& smellee.getX() <= controller.getRightBoundary();
					}
				});

		if (anyGrasses.size() > 0) {
			app.sortByClosestTo(getX(), getY(), anyGrasses);

			Agent first = anyGrasses.get(0);
			setHeading(getHeadingTowards(first.getX(),
					first.getY()));
			right(Math.random() * 20);
			left(Math.random() * 20);

			forward(1);
			
			return true;
		} else {
			return false;
		}
	}

	private boolean moveTowardsLongGrass() {
		AbstractList<Agent> grasses = smell(10, new SmellHandler() {
			@Override
			public boolean smellCondition(Agent smellee) {
				return smellee instanceof Grass
						&& smellee.getX() >= controller
								.getLeftBoundary()
						&& smellee.getX() <= controller
								.getRightBoundary()
						&& ((EcoObject) (smellee)).getEnergy() > .30 * Grass.MAX_ENERGY;
			}
		});

		if (grasses.size() > 0) {
			// sort the grass
			app.sortByClosestTo(getX(), getY(), grasses);

			Agent closest = grasses.get(0);
			setHeading(getHeadingTowards(closest.getX(), closest
					.getY()));
			right(Math.random() * 20);
			left(Math.random() * 20);
			forward(1);

			return true;
		} else {
			return false;
		}
	}

	private boolean runningAwayFromEnemy() {
		AbstractList<Agent> enemies = smell(8, new SmellHandler() {
			@Override
			public boolean smellCondition(Agent smellee) {
				return smellee instanceof Carnivore
				&& smellee.getX() >= controller.getLeftBoundary()
				&& smellee.getX() <= controller.getRightBoundary();
			}
		});

		if (enemies.size() > 0) {
			// run away from the center of mass of the enemies

			// Find the average location of the turtles that were smelled so
			// that
			// we can make the bear run away from that location.

			double sumX = 0;
			double sumY = 0;

			// Find the sum of the X and Y locations of all the smelled turtles.
			for (Agent enemy : enemies) {
				sumX = sumX + enemy.getX();
				sumY = sumY + enemy.getY();
			}

			// Find the average by dividing the sums by the number of turtles
			double avgX = sumX / enemies.size();
			double avgY = sumY / enemies.size();

			// Find the heading (direction) that our bear would have to travel
			// in
			// to get to the turtles.
			double heading = this.getHeadingTowards(avgX, avgY);

			// Make our bear head in the exact opposite direction.
			setHeading(heading - 180);
			right(Math.random() * 20);
			left(Math.random() * 20);

			// how fast we run away depends on how much energy we have
			double energyPercent = getEnergy() / MAX_ENERGY;
			if (energyPercent > .70) {
				forward(3);
			} else if (energyPercent > .5) {
				forward(2);
			} else {
				forward(1);
			}

			runAwayTime = MAX_RUN_AWAY_TIME;

			return true;
		} else {
			runAwayTime--;
			if (runAwayTime > 0) {
				// if you were running away from a carnivore but can
				// no longer smell it, keep walking away for awhile.
				forward(1);

				return true;
			} else {
				return false;
			}
		}
	}

	public boolean isHungry() {
		if (isEating) {
			return energy < 98;
		} else {
			return energy < 80;
		}
	}

	public boolean shouldLookForFood() {
		return energy < 85;

	}

	public void setCollidingWithGrass(boolean collidingWithGrass) {
		this.collidingWithGrass = collidingWithGrass;
	}

	public boolean isReallyHungry() {
		if (isEating) {
			return energy < 50;
		} else {
			return energy < 30;
		}
	}

	public boolean isEating() {
		return isEating;
	}

	public void setEating(boolean isEating) {
		this.isEating = isEating;
	}

	@Override
	public void outputStatusInfo(PrintStream os) {
		super.outputStatusInfo(os);

		os.format("%nTurtle:%n");
		os.format("collidingWithGrass: %1b%n", collidingWithGrass);
		os.format("isEating: %1b%n", isEating);
		os.format("age: %1d%n", age);
		os.format("last baby id: %1d%n", lastBabyWho);
	}
}
