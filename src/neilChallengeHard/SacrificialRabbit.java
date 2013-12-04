package neilChallengeHard;

import java.io.PrintStream;
import java.util.AbstractList;

import starjava.Agent;
import starjava.SmellHandler;
import application.Carnivore;
import application.DemoApp;
import application.EcoObject;
import application.Grass;
import application.Herbivore;

public class SacrificialRabbit extends Herbivore {

	private DemoApp app;

	private Controller controller;

	// remember if the rabbit is colliding with grass
	private boolean collidingWithGrass = false;
	
	// remember if the rabbit is eating
	private boolean isEating = false;

	// remember how long the rabbit has been running away
	private int runAwayTime = 0;
	private static final int MAX_RUN_AWAY_TIME = 5;

	private GuardDog dog;
	
	// used for debugging
	private int lastBabyWho = -1;


	/**
	 * This is the constructor for rabbits.  It is the setup code for rabbits.
	 * 
	 * @param app
	 * @param controller
	 */
	public SacrificialRabbit(DemoApp app, Controller controller, GuardDog dog) {
		super(app, controller, "Rabbit", "animals/Rabbit-default");

		this.app = app;
		this.controller = controller;
		this.dog = dog;
		
		
		setSize(.5);

		addCollisionHandler(SacrificialRabbitGrassCollision.collision);
		
	}

	/*
	 * (non-Javadoc)
	 * @see application.Herbivore#execute()
	 */
	@Override
	public void doAnimalActions() {

		goToSlaughter();

		// call the super class's execute function so that the rabbits
		// can do what herbivores and animals do
		super.doAnimalActions();
	}

	/**
	 * 
	 */

	public void goToSlaughter() {
		setHeading(getHeadingTowards(dog.getX(), dog.getY()));
		forward(1);
	}

	@Override
	public void outputStatusInfo(PrintStream os) {
		super.outputStatusInfo(os);

		os.format("%nSacrificialRabbit:%n");
		os.format("dog: %1d%n", dog.getWho());
	}
}
