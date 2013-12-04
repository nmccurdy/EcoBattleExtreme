package miller;

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
	private boolean collidingWithGrass = false;
	private boolean isEating = false;
	private int runAwayTime = 0;
	private static final int MAX_RUN_AWAY_TIME = 5;
	public Rabbit(DemoApp app, Controller controller) {
		super(app, controller, "Rabbit", "animals/Rabbit-default");
		this.app = app;
		this.controller = controller;
		addCollisionHandler(RabbitGrassCollision.collision);
		addCollisionHandler(RabbitHerbivoreCollision.collision);
		double width = controller.getRightBoundary()
				- controller.getLeftBoundary();
		double height = controller.getTopBoundary()
				- controller.getBottomBoundary();
		setXY(controller.getLeftBoundary() + Math.random() * width,
				controller.getBottomBoundary() + Math.random() * height);
		setSize(.5);
	}

	public void doAnimalActions() {
		reproduce();
		move();
		super.doAnimalActions();
	}

	private void reproduce() {
		if(app.doesFenceExist()==true){
		if (getEnergy() > .7 * MAX_ENERGY) {
			AbstractList<Agent> otherRabbits = smell(15, new SmellHandler() {
				public boolean smellCondition(Agent smellee) {
					return smellee instanceof Rabbit;
				}
			});

			if (otherRabbits.size() < 20) {
				AbstractList<Agent> grasses = smell(15, new SmellHandler() {
					public boolean smellCondition(Agent smellee) {
						return smellee instanceof Grass
								&& ((Grass) smellee).getEnergy() > .7 * Grass.MAX_ENERGY;
					}
				});
				if (grasses.size() > 0) {
					createNewBaby();
				}
			}
		}}else{if (getEnergy() > .7 * MAX_ENERGY) {
			AbstractList<Agent> otherRabbits = smell(10, new SmellHandler() {
				public boolean smellCondition(Agent smellee) {
					return smellee instanceof Rabbit;
				}
			});

			if (otherRabbits.size() < 20) {
				AbstractList<Agent> grasses = smell(10, new SmellHandler() {
					public boolean smellCondition(Agent smellee) {
						return smellee instanceof Grass
								&& ((Grass) smellee).getEnergy() > .7 * Grass.MAX_ENERGY;
					}
				});
				if (grasses.size() > 0) {
					createNewBaby();
				}
			}
		}}
	}

	private void createNewBaby() {
		Rabbit baby = new Rabbit(app, controller);
		app.addCollidableAgent(baby);
		app.addExecutable(baby);
		giveEnergyToBaby(baby, .25);
		baby.copyPositionAndHeading(this);
		baby.getWho();
	}

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
		left(Math.random() * 90);
		right(Math.random() * 90);
		forward(1);
	}

	private boolean moveTowardsAnyGrass() {
		AbstractList<Agent> anyGrasses = smell(10, new SmellHandler() {
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
		AbstractList<Agent> grasses = smell(15, new SmellHandler() {
			public boolean smellCondition(Agent smellee) {
				return smellee instanceof Grass
						&& smellee.getX() >= controller.getLeftBoundary()
						&& smellee.getX() <= controller.getRightBoundary()
						&& ((EcoObject) (smellee)).getEnergy() > .5 * Grass.MAX_ENERGY;
			}
		});
		if (grasses.size() > 0) {
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
		if (app.doesFenceExist() == true) {
			runningAway();
		} else {
			warTimeFear();
		}
		return isMonitored;
	}

	private boolean runningAway() {
		// if (app.doesFenceExist() == true) {}else{}
		AbstractList<Agent> enemies = smell(8, new SmellHandler() {

			public boolean smellCondition(Agent smellee) {
				return smellee instanceof Carnivore
						&& !(smellee instanceof Coyote)
						&& !(smellee instanceof MotherCoyote)
						&& smellee.getX() >= controller.getLeftBoundary()
						&& smellee.getX() <= controller.getRightBoundary();
			}
		});
		if (enemies.size() > 0) {
			double sumX = 0;
			double sumY = 0;
			for (Agent enemy : enemies) {
				sumX = sumX + enemy.getX();
				sumY = sumY + enemy.getY();
			}
			double avgX = sumX / enemies.size();
			double avgY = sumY / enemies.size();
			double heading = this.getHeadingTowards(avgX, avgY);
			setHeading(heading - 180);
			right(Math.random() * 20);
			left(Math.random() * 20);
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
				forward(1);
				return true;
			} else {
				return false;
			}
		}
	}

	private boolean warTimeFear() {
		// if (app.doesFenceExist() == true) {}else{}
		AbstractList<Agent> enemies = smell(15, new SmellHandler() {

			public boolean smellCondition(Agent smellee) {
				return smellee instanceof Carnivore
						&& !(smellee instanceof Coyote)
						&& !(smellee instanceof MotherCoyote)
						&& smellee.getX() >= controller.getLeftBoundary()
						&& smellee.getX() <= controller.getRightBoundary();
			}
		});
		if (enemies.size() > 0) {
			double sumX = 0;
			double sumY = 0;
			for (Agent enemy : enemies) {
				sumX = sumX + enemy.getX();
				sumY = sumY + enemy.getY();
			}
			double avgX = sumX / enemies.size();
			double avgY = sumY / enemies.size();
			double heading = this.getHeadingTowards(avgX, avgY);
			setHeading(heading - 180);
			right(Math.random() * 20);
			left(Math.random() * 20);
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

	public void outputStatusInfo(PrintStream os) {
		super.outputStatusInfo(os);
		os.format("%nRabbit:%n");
		os.format("collidingWithGrass: %1b%n", collidingWithGrass);
		os.format("isEating: %1b%n", isEating);
	}
}