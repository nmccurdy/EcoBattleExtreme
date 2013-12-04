package neilChallengeHard;

import application.Grass;
import starjava.CollisionHandler;

public class QueenRabbitGrassCollision extends CollisionHandler<QueenRabbit, Grass> {

	public static QueenRabbitGrassCollision collision = new QueenRabbitGrassCollision();

	public QueenRabbitGrassCollision() {
		super(Grass.class);
	}

	@Override
	public void collided(QueenRabbit collider, Grass collidee, double time) {

		/***** Focus on this *****/

		// only have the rabbit eat if it's hungry
		if (collider.isHungry()) {
//			System.out.println("Queen is eating.");
			collider.eat(collidee);
			collider.setEating(true);
		} else {
			collider.setEating(false);
		}

		/***** Focus on this (end) *****/
	}
}