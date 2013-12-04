package graack;

import starjava.CollisionHandler;
import application.Herbivore;

public class RabbitHerbivoreCollision extends CollisionHandler<Rabbit, Herbivore> {

	public static RabbitHerbivoreCollision collision = new RabbitHerbivoreCollision();

	public RabbitHerbivoreCollision() {
		super(Herbivore.class);
	}

	@Override
	public void collided(Rabbit collider, Herbivore collidee, double time) {

		/***** Focus on this *****/

		if (!(collidee instanceof Rabbit)) {
			collider.fight(collidee);
		}
		
		/***** Focus on this (end) *****/
	}
}