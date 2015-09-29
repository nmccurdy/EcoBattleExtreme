package kids;

import starjava.CollisionHandler;
import application.Carnivore;
import application.Herbivore;

public class RabbitCarnivoreCollision extends CollisionHandler<Rabbit, Carnivore> {

	public static RabbitCarnivoreCollision collision = new RabbitCarnivoreCollision();

	public RabbitCarnivoreCollision() {
		super(Carnivore.class);
	}

	@Override
	public void collided(Rabbit collider, Carnivore collidee, double time) {

		/***** Focus on this *****/

		collider.doDamage(collidee);
		
		/***** Focus on this (end) *****/
	}
}