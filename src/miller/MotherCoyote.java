package miller;

import java.io.PrintStream;
import java.util.AbstractList;

import starjava.Agent;
import starjava.SmellHandler;
import application.Carnivore;
import application.DemoApp;
import application.Herbivore;

public class MotherCoyote extends Carnivore {

	private DemoApp app;

	private Controller controller;

	public MotherCoyote(DemoApp app, Controller controller) {
		super(app, controller, "Coyote", "animals/dog-default");

		this.app = app;
		this.controller = controller;

		addCollisionHandler(MotherCoyoteCarnivoreCollision.collision);
		addCollisionHandler(MotherCoyoteHerbivoreCollision.collision);

		double width = controller.getRightBoundary()
				- controller.getLeftBoundary();
		double height = controller.getTopBoundary()
				- controller.getBottomBoundary();

		setXY(controller.getLeftBoundary() + Math.random() * width,
				controller.getBottomBoundary() + Math.random() * height);

		setSize(2);
	}

	public void doAnimalActions() {
		reproduce();
		move();
		super.doAnimalActions();
	}

	private void reproduce() {
		if (app.doesFenceExist() == true) {
			if (getEnergy() > 1 * MAX_ENERGY) {
				createNewBaby();
			}

		} else {
			if (getEnergy() > .5 * MAX_ENERGY) {
				createNewBaby();
			}
		}
	}

	private void createNewBaby() {
		Coyote baby = new Coyote(app, controller);
		giveEnergyToBaby(baby, .25);
		baby.copyPositionAndHeading(this);
		app.addCollidableAgent(baby);
		app.addExecutable(baby);
		baby.getWho();
	}

	private void move() {

		if (!noFightButFlight()) {
			if (shouldLookForFood()) {
				// if (!huntInvadingRabbits()) {
				if (!huntLocalRabbits()) {
					// if (app.doesFenceExist() == true)
					left(Math.random() * 30);
					right(Math.random() * 30);
					forward(1);
					// } else {
					// forward(1);
				}
			}
		}
		// }

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

	@SuppressWarnings("unused")
	private boolean huntInvadingRabbits() {
		AbstractList<Agent> animals = smell(121, new SmellHandler() {

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

	private boolean huntLocalRabbits() {
		if (app.doesFenceExist() == true) {
			AbstractList<Agent> animals = smell(15, new SmellHandler() {
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

				return isMonitored;

			} else {
				// if (this.huntInvadingRabbits() == false) {
				if (energy < .25 * MAX_ENERGY) {

					AbstractList<Agent> NeedAnimalList = smell(3,
							new SmellHandler() {

								public boolean smellCondition(Agent smellee) {
									return smellee instanceof Herbivore
											&& !(smellee instanceof Rabbit)
											&& smellee.getX() >= controller
													.getLeftBoundary()
											&& smellee.getX() <= controller
													.getRightBoundary();
								}
							});

					if (NeedAnimalList.size() > 0) {
						app.sortByClosestTo(getX(), getY(), NeedAnimalList);
						Agent closest = NeedAnimalList.get(0);
						setHeading(getHeadingTowards(closest.getX(),
								closest.getY()));
						forward(2);

						return isMonitored;
					}
					// } else {
					// huntInvadingRabbits();
				}
			}

			return isMonitored;
		}
		return isMonitored;
	}

	// return isMonitored;
	// }

	public boolean shouldLookForFood() {
		if (app.doesFenceExist() == true) {
			return energy < .95 * MAX_ENERGY;
		} else {
			return energy < .99 * MAX_ENERGY;
		}
	}

	/*
	 * Deal with other carnivores. Either attack them or run away from them
	 * depending on the amount of energy you have.
	 */
	public boolean noFightButFlight() {
		AbstractList<Agent> carnivores = smell(10, new SmellHandler() {

			public boolean smellCondition(Agent smellee) {
				return smellee instanceof Carnivore
						&& !(smellee instanceof MotherCoyote)
						&& !(smellee instanceof Coyote)
						&& smellee.getX() >= controller.getLeftBoundary()
						&& smellee.getX() <= controller.getRightBoundary();
			}
		});

		if (carnivores.size() > 0) {
			Agent enemy = carnivores.get(0);

			// setHeading(getHeadingTowards(enemy.getX(), enemy.getY()));
			// if (getEnergy() < .95 * MAX_ENERGY) {
			left(180);
			// }

			forward(2);

			return true;
		} else {
			return false;
		}
	}

	public void outputStatusInfo(PrintStream os) {
		super.outputStatusInfo(os);

		os.format("%nCoyote:%n");
	}
}
