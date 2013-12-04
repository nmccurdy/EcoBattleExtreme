package application;


public class Herbivore extends Animal {

	// these are the constants that govern how a herbivore behaves
	public final static double MAX_ENERGY = 100;
	private final static double PERCENT_MAX_ENGERY_TRANSFERRED = .75;
	private final static double ENERGY_PER_SLEEP = .15;
	private final static double EXTRA_ENERGY_PER_STEP = .5;
	private final static double PERCENT_ENERGY_PER_FIGHT = .1;

	public Herbivore(DemoApp app, Controller controller, String breedName, String breedIcon) {
		super(app, controller, breedName, breedIcon);
		
	}


	/*
	 * If the herbivore is being eaten by a carnivore, it dies
	 * and gives a proportion of its energy to the carnivore.
	 * 
	 * If the eater is an herbivore, treat the eating as a
	 * sign of agression.
	 * 
	 * @see application.Animal#beingEatenBy(application.Animal)
	 */
	@Override
	public final double beingEatenBy(Animal eater) {
		if (eater instanceof Herbivore) {
			// fight if it's an herbivore that's trying to 
			// eat us
			
			return -getFightEnergy();
		} else {
			die();
			return PERCENT_MAX_ENGERY_TRANSFERRED * getMaxEnergyObtained();
		}
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
