package miller;

import java.util.AbstractList;

import starjava.Agent;
import starjava.CollisionHandler;
import starjava.SmellHandler;
import application.Herbivore;
import miller.Rabbit;

public class CoyoteHerbivoreCollision extends
		CollisionHandler<Coyote, Herbivore> {

	public static CoyoteHerbivoreCollision collision = new CoyoteHerbivoreCollision();

	public CoyoteHerbivoreCollision() {
		super(Herbivore.class);
	}

	@Override
	public void collided(Coyote collider, Herbivore collidee, double time) {

		/***** Focus on this *****/

		// private boolean senceInvadingRabbits() {
		// AbstractList<Agent> animals = smell(15, new SmellHandler() {
		//
		// public boolean smellCondition(Agent smellee) {
		// return smellee instanceof Herbivore
		// && !(smellee instanceof Rabbit);}

		if (collidee instanceof Rabbit) {
			if (collider.shouldLookForFood()) {
				collider.eat(collidee);
			}
		} else {

			// if (Coyote.huntInvadingRabbits() == false)
			collider.eat(collidee);
		}

		/***** Focus on this (end) *****/

	}
}
// }

// private AbstractList<Agent> smell(int i, SmellHandler smellHandler) {
// // TODO Auto-generated method stub
// return null;
// }