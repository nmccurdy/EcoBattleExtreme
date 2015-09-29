package neil;

import starjava.CollisionHandler;
import application.Herbivore;

public class CoyoteHerbivoreCollision extends CollisionHandler<Coyote, Herbivore> {

	public static CoyoteHerbivoreCollision collision = new CoyoteHerbivoreCollision();

	public CoyoteHerbivoreCollision() {
		super(Herbivore.class);
	}

	@Override
	public void collided(Coyote collider, Herbivore collidee, double time) {

		/***** Focus on this *****/

		if (collider.shouldLookForFood()) {
			collider.doDamage(collidee);
		}
		if (collidee.isCorpse()) {
			collider.eat(collidee);
		}

		/***** Focus on this (end) *****/
	}
}