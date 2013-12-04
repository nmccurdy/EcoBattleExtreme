package application;

public interface EcoObject {
	public double eat(EcoObject prey);
	public double beingEatenBy(Animal eater);
	public double getEnergy();
}
