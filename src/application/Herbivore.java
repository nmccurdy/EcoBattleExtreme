package application;


public class Herbivore extends Animal {

	// these are the constants that govern how a herbivore behaves
	private final static double MAX_ENERGY = 100;
	private final static double PERCENT_MAX_ENERGY_TRANSFERRED = .8;
	private final static double ENERGY_PER_SLEEP = .15;
	private final static double DAMAGE_MULTIPLIER = .1875;

	public Herbivore(DemoApp app, Controller controller, String breedIcon) {
		super(app, controller, breedIcon);
		
	}


	public Herbivore(DemoApp app, Controller controller, String breedIcon, Herbivore parent) {
		super(app, controller, breedIcon, parent);
		
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
	
	public double getMAX_ENERGY() {
		return MAX_ENERGY * app.getWorldMultiplier();
	}


	double getDamageMultiplier() {
		return DAMAGE_MULTIPLIER;
	}


	double getENERGY_PER_SLEEP() {
		return ENERGY_PER_SLEEP * app.getWorldMultiplier();
	}


	@Override
	public double getPERCENT_MAX_ENERGY_TRANSFERRED() {
		return PERCENT_MAX_ENERGY_TRANSFERRED;
	}


	@Override
	public double getDefense() {
		return 0;
	}

}
