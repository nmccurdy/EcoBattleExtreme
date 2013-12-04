package miller;

import starjava.CollisionHandler;
import application.Herbivore;
import miller.Rabbit;

public class MotherCoyoteHerbivoreCollision extends
		CollisionHandler<MotherCoyote, Herbivore> {

	public static MotherCoyoteHerbivoreCollision collision = new MotherCoyoteHerbivoreCollision();

	public MotherCoyoteHerbivoreCollision() {
		super(Herbivore.class);
	}

	@Override
	public void collided(MotherCoyote collider, Herbivore collidee, double time) {

		if (collidee instanceof Rabbit) {
			if (collider.shouldLookForFood()) {
				collider.eat(collidee);
			}
		} else {

			collider.eat(collidee);
		}
	}
}