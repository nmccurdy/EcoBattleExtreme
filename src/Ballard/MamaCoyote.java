package Ballard;

import java.io.PrintStream;
import java.util.AbstractList;

import starjava.Agent;
import starjava.SmellHandler;
import application.Carnivore;
import application.DemoApp;
import application.Herbivore;

public class MamaCoyote extends Coyote {
	private int age;
	private static final int REPRO_CYCLE = 20;
	private static final double REPRO_CHANCE_PER_REPRO_CYCLE = .90;
	private static final Coyote Coyote = null;

	// used for debugging
	private int lastBabyWho = -1;

	public MamaCoyote(DemoApp app, Controller controller) {
		super(app, controller);
		setSize(10);
	}

	@Override
	protected void reproduce() {
		age++;
		if (age % REPRO_CYCLE == 0) {
			if (Math.random() < REPRO_CHANCE_PER_REPRO_CYCLE) {
				createNewBaby();

				{
				}
			}
		}

	}

	@Override
	protected void createNewBaby() {

		Coyote baby = new Coyote(app, controller);
		app.addCollidableAgent(baby);
		app.addExecutable(baby);
		// //
		// // if you don't give energy to the baby, it will die
		// // immediately. A percentage of your energy is
		// // removed in the process.
		giveEnergyToBaby(baby, .25);
		{
			//
			// baby.age = 0;{
			baby.copyPositionAndHeading(this);
			{

				// remember this baby for debugging purposes
				lastBabyWho = baby.getWho();
			}
		}

	}

	@Override
	public void outputStatusInfo(PrintStream os) {
		super.outputStatusInfo(os);

		os.format("%nMama Coyote:%n");
	}

	protected void move() {
		// Basically what all of this code does is make the coyotes
		// chase the rabbits(as long as the coyotes are hungry and as long
		// as they aren't running away or attacking another carnivore.)

		if (!fightOrFlight()) {
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
	}

	@Override
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
			if (getEnergy() < .9 * MAX_ENERGY) {
				// run away
				left(180);
			}

			forward(3);

			return true;
		} else {
			return false;
		}
	}
}