package application;

public interface EcoObject {
	public double eat(EcoObject prey);
	public double beingEatenBy(EcoObject eater);
	public double getEnergy();
}
