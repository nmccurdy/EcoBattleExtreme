package gonzalFord;

import java.util.AbstractList;

import starjava.Agent;
import starjava.CollisionHandler;
import starjava.SmellHandler;
import sun.font.CreatedFontTracker;
import application.DemoApp;
import application.Herbivore;

public class CoyoteHerbivoreCollision extends
		CollisionHandler<Coyote, Herbivore> {

	public static CoyoteHerbivoreCollision collision = new CoyoteHerbivoreCollision();

	public CoyoteHerbivoreCollision() {
		super(Herbivore.class);
	}

	@Override
	public void collided(Coyote collider, Herbivore collidee, double time) {

		/***** Focus on this *****/

		if (collidee instanceof Rabbit) {
			if (collider.shouldLookForFood()) {
				AbstractList<Agent> animals = collider.smell(15,
						new SmellHandler() {
							@Override
							public boolean smellCondition(Agent smellee) {
								return smellee instanceof Rabbit;
							}
						});

				if (animals.size() > 4) {
					collider.eat(collidee);
				}
			}
		} else {
			collider.eat(collidee);
		}

		/***** Focus on this (end) *****/
	}
}