package neilChallengeHard;

import starjava.CollisionHandler;
import application.Carnivore;

public final class GuardDogCarnivoreCollision extends CollisionHandler<GuardDog, Carnivore> {

	public static GuardDogCarnivoreCollision collision = new GuardDogCarnivoreCollision();

	public GuardDogCarnivoreCollision() {
		super(Carnivore.class);
	}

	@Override
	public void collided(GuardDog collider, Carnivore collidee, double time) {

		/***** Focus on this *****/

		if (!(collidee instanceof GuardDog)) {
			collider.fight(collidee);
		}

		/***** Focus on this (end) *****/
	}
}