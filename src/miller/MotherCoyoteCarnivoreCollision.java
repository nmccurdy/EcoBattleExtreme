package miller;

import starjava.CollisionHandler;
import application.Carnivore;


public final class MotherCoyoteCarnivoreCollision extends CollisionHandler<MotherCoyote, Carnivore> {

	public static MotherCoyoteCarnivoreCollision collision = new MotherCoyoteCarnivoreCollision();

	public MotherCoyoteCarnivoreCollision() {
		super(Carnivore.class);
	}

	@Override
	public void collided(MotherCoyote collider, Carnivore collidee, double time) {

		/***** Focus on this *****/

		if (!(collidee instanceof Coyote)&&!(collidee instanceof MotherCoyote)) {
			collider.fight(collidee);
		}

		/***** Focus on this (end) *****/
	}
}