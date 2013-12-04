package neilChallengeHard;

import starjava.CollisionHandler;
import application.Herbivore;

public class GuardDogHerbivoreCollision extends
		CollisionHandler<GuardDog, Herbivore> {

	public static GuardDogHerbivoreCollision collision = new GuardDogHerbivoreCollision();

	public GuardDogHerbivoreCollision() {
		super(Herbivore.class);
	}

	@Override
	public void collided(GuardDog collider, Herbivore collidee, double time) {

		/***** Focus on this *****/
		if (!(collidee instanceof QueenRabbit)) {
			if (collidee instanceof SacrificialRabbit) {
				// only eat a sacrificial rabbit if it is yours
					if (collider.getMyRabbit() == collidee) { 
						collider.eat(collidee);
						collider.setIsFoodComing(false);
					}
	//			}
			} else {
				// just eat it
				collider.eat(collidee);
			}
		}
		/***** Focus on this (end) *****/
	}
}