package kids;

import starjava.CollisionHandler;
import application.Carnivore;

public final class CoyoteCarnivoreCollision extends CollisionHandler<Coyote, Carnivore> {

	public static CoyoteCarnivoreCollision collision = new CoyoteCarnivoreCollision();

	public CoyoteCarnivoreCollision() {
		super(Carnivore.class);
	}

	@Override
	public void collided(Coyote collider, Carnivore collidee, double time) {

		/***** Focus on this *****/

		if (!(collidee instanceof Coyote)) {
			collider.doDamage(collidee);
		}
		
		if (collidee.isCorpse()) {
			collider.eat(collidee);
		}

		/***** Focus on this (end) *****/
	}
}