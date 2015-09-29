package kids;

import application.Grass;
import starjava.CollisionHandler;

public class RabbitGrassCollision extends CollisionHandler<Rabbit, Grass> {

	public static RabbitGrassCollision collision = new RabbitGrassCollision();

	public RabbitGrassCollision() {
		super(Grass.class);
	}

	@Override
	public void collided(Rabbit collider, Grass collidee, double time) {

		/***** Focus on this *****/

		// make the rabbit remember that is colliding with grass
		collider.setCollidingWithGrass(true);

		// only have the rabbit eat if it's hungry
		if (collider.isHungry()) {
			if (collidee.getEnergy() > .2 * collidee.getMAX_ENERGY()) {
				// only eat this grass if it is more than 20% of its
				// max size
				collider.eat(collidee);
				collider.setEating(true);
			} else {
				if (collider.isReallyHungry()) {
					// if the rabbit is really hungry, then have it
					// eat the grass even if it is less than 20% of
					// its max size.
					collider.eat(collidee);
					collider.setEating(true);
				} else {
					collider.setCollidingWithGrass(false);
				}
			}
		}

		/***** Focus on this (end) *****/
	}
}