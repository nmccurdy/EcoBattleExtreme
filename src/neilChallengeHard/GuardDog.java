package neilChallengeHard;

import java.io.PrintStream;
import java.util.AbstractList;

import starjava.Agent;
import starjava.SmellHandler;
import application.Carnivore;
import application.DemoApp;
import application.Herbivore;

public class GuardDog extends Carnivore {

	private DemoApp app;

	private Controller controller;

	// reproduction is based off of age
	private boolean isFoodComing = false;

	private int foodWaitTime = 0;

	private double originalXOffset;
	private double originalYOffset;

	private SacrificialRabbit myRabbit = null;

	private int dogNum;

	private QueenRabbit queen;

	public GuardDog(DemoApp app, Controller controller, QueenRabbit queen,
			int dogNum) {
		super(app, controller, "Coyote", "animals/dog-default");

		this.app = app;
		this.controller = controller;
		this.queen = queen;
		this.dogNum = dogNum;

		addCollisionHandler(GuardDogHerbivoreCollision.collision);
		addCollisionHandler(GuardDogCarnivoreCollision.collision);

		int radius = 5;

		setXY(queen.getX(), queen.getY());

		double angle = dogNum * 360.0 / controller.getNumDogs();

		originalXOffset = radius * Math.cos(Math.toRadians(angle));
		originalYOffset = radius * Math.sin(Math.toRadians(angle));

		setSize(2);

	}

	@Override
	public void doAnimalActions() {

		if (isFoodComing) {
			foodWaitTime++;

			if (foodWaitTime > 20) {
				// give up if the food didn't arrive
				isFoodComing = false;
				foodWaitTime = 0;
			}
		}

		move();
		// call the super class so that the coyote can do carnivore and animal
		// kind of things
		super.doAnimalActions();
	}

	private void move() {
		if (!fight()) {
			if (queen.isAlive()) {
				double x = queen.getX() + originalXOffset;
				double y = queen.getY() + originalYOffset;
				// if (!destroyRabbit()) {
				if (Math.abs(getX() - x) > 1 || Math.abs(getY() - y) > 1) {
					setHeading(getHeadingTowards(
							queen.getX() + originalXOffset, queen.getY()
									+ originalYOffset));
					forward(1);
				}
			}
			// }
		}

		// // if the coyote reaches the edge of the world, have it turn around.
		// // the || in the line below is the Java way of saying OR.
		// if (getX() <= controller.getLeftBoundary() + 1
		// || getX() >= controller.getRightBoundary() - 1
		// || getY() <= controller.getBottomBoundary()
		// || getY() >= controller.getTopBoundary()) {
		// left(180);
		// right(Math.random() * 20);
		// left(Math.random() * 20);
		// forward(1);
		// }
	}

	/*
	 * Deal with other carnivores. Either attack them or run away from them
	 * depending on the amount of energy you have.
	 */
	public boolean fight() {
		AbstractList<Agent> carnivores = smell(5, new SmellHandler() {
			@Override
			public boolean smellCondition(Agent smellee) {
				return smellee instanceof Carnivore
						&& !(smellee instanceof GuardDog)
						&& smellee.getX() >= controller.getLeftBoundary()
						&& smellee.getX() <= controller.getRightBoundary();
			}
		});

		if (carnivores.size() > 0) {
			Agent enemy = carnivores.get(0);

			setHeading(getHeadingTowards(enemy.getX(), enemy.getY()));
			forward(1);

			return true;
		} else {
			return false;
		}
	}

	public boolean destroyRabbit() {
		AbstractList<Agent> animals = smell(15, new SmellHandler() {
			@Override
			public boolean smellCondition(Agent smellee) {
				return smellee instanceof Herbivore
						&& !(smellee instanceof QueenRabbit)
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

	public void setIsFoodComing(boolean value) {
		isFoodComing = value;
		foodWaitTime = 0;
	}

	public boolean isFoodComing() {
		return isFoodComing;
	}

	public int getDogNum() {
		return dogNum;
	}

	public void setMyRabbit(SacrificialRabbit rabbit) {
		this.myRabbit = rabbit;
	}

	public SacrificialRabbit getMyRabbit() {
		return myRabbit;
	}

	@Override
	public void outputStatusInfo(PrintStream os) {
		super.outputStatusInfo(os);

		os.format("%nGuardDog:%n");
		os.format("dogNum: %1d%n", dogNum);
		os
				.format("myRabbit: %1d%n", (myRabbit != null ? myRabbit
						.getWho() : 0));
		os.format("isFoodComing: %1b%n", isFoodComing);

	}
}
