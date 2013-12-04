package application;


public class Carnivore extends Animal {

	// constants that govern behavior of carnivores
	public final static double MAX_ENERGY = 150;
	private final static double PERCENT_ENERGY_PER_FIGHT = .1;
	private final static double ENERGY_PER_SLEEP = .15;
	private final static double EXTRA_ENERGY_PER_STEP = .5;
	
	public Carnivore(DemoApp app, Controller controller, String breedName, String breedIcon) {
		super(app, controller, breedName, breedIcon);
	}


	/*
	 * If someone tries to eat a carnivore, the eater loses energy.
	 * It's not a good idea to try to eat a carnivore!
	 * 
	 * @see application.Animal#beingEatenBy(application.Animal)
	 */
	@Override
	public final double beingEatenBy(Animal eater) {
		// this is going to translate to fighting, so negative energy is
		// transferred if someone tries to eat you.
		return -getFightEnergy();
	}


	double getMAX_ENERGY() {
		return MAX_ENERGY;
	}


	double getPERCENT_ENERGY_PER_FIGHT() {
		return PERCENT_ENERGY_PER_FIGHT;
	}


	double getENERGY_PER_SLEEP() {
		return ENERGY_PER_SLEEP;
	}


	double getEXTRA_ENERGY_PER_STEP() {
		return EXTRA_ENERGY_PER_STEP;
	}

}
