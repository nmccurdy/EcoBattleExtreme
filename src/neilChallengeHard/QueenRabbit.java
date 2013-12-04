package neilChallengeHard;

import java.io.PrintStream;
import java.util.AbstractList;
import java.util.Collections;
import java.util.Comparator;

import starjava.Agent;
import starjava.SmellHandler;
import application.Animal;
import application.Carnivore;
import application.DemoApp;
import application.EcoObject;
import application.Grass;
import application.Herbivore;

public class QueenRabbit extends Herbivore {

	private DemoApp app;

	private Controller controller;

	// remember if the rabbit is colliding with grass
	private boolean collidingWithGrass = false;

	// remember if the rabbit is eating
	private boolean isEating = false;

	// remember how long the rabbit has been running away
	private int runAwayTime = 0;
	private static final int MAX_RUN_AWAY_TIME = 5;

	// age is used for reproduction
	private int age;
	private static final int REPRO_CYCLE = 50;
	private static final double REPRO_CHANCE_PER_REPRO_CYCLE = .25;

	private int numDogs;

	// used for debugging
	private int lastBabyWho = -1;

	/**
	 * This is the constructor for rabbits. It is the setup code for rabbits.
	 * 
	 * @param app
	 * @param controller
	 */
	public QueenRabbit(DemoApp app, Controller controller, int numDogs,
			double x, double y) {
		super(app, controller, "Rabbit", "animals/Rabbit-default");

		this.app = app;
		this.controller = controller;
		this.numDogs = numDogs;

		this.setXY(x, y);

		addCollisionHandler(QueenRabbitGrassCollision.collision);

		// find out if we're on the right or left of the screen. we
		// want the queen to be as far away from the fence as possible

		setSize(2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see application.Herbivore#execute()
	 */
	@Override
	public void doAnimalActions() {
		// System.out.println("Queen is alive! " + getWho() + " " +
		// getEnergy());
		reproduce();

		// call the super class's execute function so that the rabbits
		// can do what herbivores and animals do
		super.doAnimalActions();
	}

	/**
	 * Only reproduce if it's the right time in the repro cycle
	 */
	private void reproduce() {
		reproduceQueen();
		reproduceDog();
		reproduceRabbit();
	}

	private void reproduceQueen() {
		if (!app.doesFenceExist()) {
			if (getEnergy() > 98) {
				// smell for other queens.  if there are less than 4 queens, move away
				// from center of mass of the queens
				
				AbstractList<Agent> queens = smell(15, new SmellHandler() {
					@Override
					public boolean smellCondition(Agent smellee) {
						return smellee instanceof QueenRabbit;
					}
				});

				int potentialQueens = 8;
				
				if (Math.abs(getX()) >= 45) {
					potentialQueens = 5;
				}
				
				if (Math.abs(getY()) >= 45) {
					potentialQueens =5;
				}
				
				if (Math.abs(getX()) >= 45 &&
					Math.abs(getY()) >= 45) {
					potentialQueens = 3;
				}
				
				
				if (queens.size() < potentialQueens) {
					double xCenter = getX();
					double yCenter = getY();

					if (queens.size() > 0) {
						// find center of mass
						
						double sumX = 0;
						double sumY = 0;
						for (Agent queen : queens) {
							sumX += queen.getX();
							sumY += queen.getY();
						}
						
						xCenter = sumX/queens.size();
						yCenter = sumY/queens.size();
					}
					
					// create a new queen and have it walk away from
					// the center of mass

					createQueen(xCenter, yCenter);
				}				
			}
		}
	}

	private void createQueen(double xAwayFrom, double yAwayFrom) {
		
		QueenRabbit babyQueen = new QueenRabbit(app, controller, numDogs, xAwayFrom, yAwayFrom);
		app.addCollidableAgent(babyQueen);
		app.addExecutable(babyQueen);
	
		giveEnergyToBaby(babyQueen, .90);
		babyQueen.setHeading(getHeadingTowards(xAwayFrom, yAwayFrom));
		babyQueen.right(180);
		
		babyQueen.forward(10);
	}
	
	
	private void reproduceRabbit() {
		if (getEnergy() > 98) {
			// create a sacrificial rabbit whenever there is a hungry guard dog

			AbstractList<Agent> hungryDogs = smell(10, new SmellHandler() {
				@Override
				public boolean smellCondition(Agent smellee) {
					return smellee instanceof GuardDog
							&& (((GuardDog) (smellee)).isFoodComing() == false)
							&& ((EcoObject) (smellee)).getEnergy() < .75 * Carnivore.MAX_ENERGY;
				}
			});

			if (hungryDogs.size() > 0) {

				// sort hungryDogs by hungriest

				Collections.sort(hungryDogs, new Comparator<Agent>() {
					public int compare(Agent a1, Agent a2) {

						// sort smallest to biggest energy
						return Double.compare(((Animal) a1).getEnergy(),
								((Animal) a2).getEnergy());
					}
				});

				GuardDog luckyDog = (GuardDog) hungryDogs.get(0);
				luckyDog.setIsFoodComing(true);
				createSacrificialRabbit(luckyDog);

				// System.out.println("Created baby: " + getEnergy());

			}
		}
	}

	private void reproduceDog() {
		if (getEnergy() > 98) {

			// count number of dogs and make sure that we haven't lost any.
			// reproduce
			// if necessary

			AbstractList<Agent> dogs = smell(10, new SmellHandler() {
				@Override
				public boolean smellCondition(Agent smellee) {
					return smellee instanceof GuardDog;
				}
			});

			if (dogs.size() < numDogs) {
				// go through dogs list to see which dog is
				// missing
				boolean[] dogsPresent = new boolean[controller.getNumDogs()];

				// initialize to false
				for (int i = 0; i < dogsPresent.length; i++) {
					dogsPresent[i] = false;
				}

				// mark which dogs are present
				for (Agent dog : dogs) {
					dogsPresent[((GuardDog) dog).getDogNum()] = true;
				}

				// find out who is missing

				int packMiddle = dogsPresent.length / 2;

				// start looking for vacancies in the middle of the
				// pack because we want to refill those first.
				for (int i = 0; i < packMiddle; i++) {
					int dogNum = packMiddle + i;
					if (dogsPresent[dogNum] == false) {
						createGuardDog(dogNum);
						// we only want to create one at a time
						// in case the queen runs out of energy
						break;
					}

					dogNum = packMiddle - 1 - i;
					if (dogNum >= 0) {
						if (dogsPresent[dogNum] == false) {
							createGuardDog(dogNum);
							// we only want to create one at a time
							// in case the queen runs out of energy
							break;
						}
					}
				}
			}
		}
	}

	private void createGuardDog(int dogNum) {
		GuardDog dog = new GuardDog(app, controller, this, dogNum);
		app.addCollidableAgent(dog);
		app.addExecutable(dog);

		// energy has to be given to the baby or else it will die
		giveEnergyToBaby(dog, .9);

		dog.copyPositionAndHeading(this);
	}

	/**
	 * Create a new baby rabbit and give it some of the parent's energy
	 */
	private void createSacrificialRabbit(GuardDog luckyDog) {
		SacrificialRabbit baby = new SacrificialRabbit(app, controller,
				luckyDog);
		app.addCollidableAgent(baby);
		app.addExecutable(baby);

		// energy has to be given to the baby or else it will die
		giveEnergyToBaby(baby, .5);

		baby.copyPositionAndHeading(this);

		// let the dog know which rabbit is his so that the other
		// dogs don't steal it.
		luckyDog.setMyRabbit(baby);
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

		// if the Rabbit reaches the edge of the world, have it turn around.
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
		AbstractList<Agent> anyGrasses = smell(10, new SmellHandler() {
			@Override
			public boolean smellCondition(Agent smellee) {
				return smellee instanceof Grass
						&& smellee.getX() >= controller.getLeftBoundary()
						&& smellee.getX() <= controller.getRightBoundary();
			}
		});

		if (anyGrasses.size() > 0) {
			app.sortByClosestTo(getX(), getY(), anyGrasses);

			Agent first = anyGrasses.get(0);
			setHeading(getHeadingTowards(first.getX(), first.getY()));
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
						&& smellee.getX() >= controller.getLeftBoundary()
						&& smellee.getX() <= controller.getRightBoundary()
						&& ((EcoObject) (smellee)).getEnergy() > .30 * Grass.MAX_ENERGY;
			}
		});

		if (grasses.size() > 0) {
			// sort the grass
			app.sortByClosestTo(getX(), getY(), grasses);

			Agent closest = grasses.get(0);
			setHeading(getHeadingTowards(closest.getX(), closest.getY()));
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

			// Find the average location of the coyotes that were smelled so
			// that
			// we can make the rabbit run away from that location.

			double sumX = 0;
			double sumY = 0;

			// Find the sum of the X and Y locations of all the smelled rabbits.
			for (Agent enemy : enemies) {
				sumX = sumX + enemy.getX();
				sumY = sumY + enemy.getY();
			}

			// Find the average by dividing the sums by the number of rabbits
			double avgX = sumX / enemies.size();
			double avgY = sumY / enemies.size();

			// Find the heading (direction) that our rabbit would have to travel
			// in
			// to get to the coyotes.
			double heading = this.getHeadingTowards(avgX, avgY);

			// Make our rabbit head in the exact opposite direction.
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
			return energy < 100;
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

		os.format("%nRabbit:%n");
		os.format("collidingWithGrass: %1b%n", collidingWithGrass);
		os.format("isEating: %1b%n", isEating);
		os.format("age: %1d%n", age);
		os.format("last baby id: %1d%n", lastBabyWho);
	}
}
