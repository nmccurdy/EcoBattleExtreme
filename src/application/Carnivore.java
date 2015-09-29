package application;


public class Carnivore extends Animal {

	// constants that govern behavior of carnivores
	private final static double MAX_ENERGY = 150;
	private final static double DAMAGE_MULTIPLIER = .25;
	private final static double ENERGY_PER_SLEEP = .15;
	private final static double EXTRA_ENERGY_PER_STEP = .5;
	private static final double PERCENT_MAX_ENERGY_TRANSFERRED = .8;
	private static final double DEFENSE_MULTIPLIER = .5;
	
	public Carnivore(DemoApp app, Controller controller, String breedIcon) {
		super(app, controller, breedIcon);
	}

	public Carnivore(DemoApp app, Controller controller, String breedIcon, Carnivore parent) {
		super(app, controller, breedIcon, parent);
	}
	
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
		return getMass() * DEFENSE_MULTIPLIER;
	}

}
