package neilChallengeHard;

import application.Grass;
import starjava.CollisionHandler;

public class SacrificialRabbitGrassCollision extends CollisionHandler<SacrificialRabbit, Grass> {

	public static SacrificialRabbitGrassCollision collision = new SacrificialRabbitGrassCollision();

	public SacrificialRabbitGrassCollision() {
		super(Grass.class);
	}

	@Override
	public void collided(SacrificialRabbit collider, Grass collidee, double time) {

		/***** Focus on this *****/

		//collider.eat(collidee);
		
		/***** Focus on this (end) *****/
	}
}