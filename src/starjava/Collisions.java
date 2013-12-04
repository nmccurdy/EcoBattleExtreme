package starjava;

import java.util.AbstractList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

public class Collisions {

	private class ListOfCollisionHandlers {
		LinkedList<CollisionHandler> list;

		public ListOfCollisionHandlers(CollisionHandler handler) {
			list = new LinkedList<CollisionHandler>();
			list.add(handler);
		}

		public void add(CollisionHandler handler) {
			list.add(handler);
		}

		public AbstractList<CollisionHandler> getMatchingHandlers(
				Object collidee) {
			return list;

			// returning the matching subset is actually slower. might want to
			// try
			// creating a specialized "iterator" that only interates through
			// matching
			// ones.

			// Iterator<CollisionHandler> itr = list.iterator();
			//
			// LinkedList<CollisionHandler> results = null;
			// while (itr.hasNext()) {
			// CollisionHandler handler = itr.next();
			//
			// if (handler.getCollideeClass().isInstance(collidee)) {
			// if (results == null) {
			// results = new LinkedList<CollisionHandler>();
			// }
			// results.add(handler);
			// }
			// }
			//
			// return results;
			//
		}
	}

	private HashMap<Agent, ListOfCollisionHandlers> collisionHandlers;

	private Application app;

	private boolean useAccurateCollisionHandler = false;
	private int collisionCount = 0;

	public Collisions(Application app) {
		this.app = app;

		collisionHandlers = new HashMap<Agent, ListOfCollisionHandlers>();
	}

	public void makeFast() {
		useAccurateCollisionHandler = false;
	}

	public void makeAccurate() {
		useAccurateCollisionHandler = true;
	}

	public void addCollision(Agent collider, CollisionHandler handler) {
		ListOfCollisionHandlers handlers = collisionHandlers.get(collider);
		if (handlers == null) {
			handlers = new ListOfCollisionHandlers(handler);

			collisionHandlers.put(collider, handlers);
		} else {
			handlers.add(handler);
		}
	}

	public void removeCollisionHandlers(Agent Agent) {
		collisionHandlers.remove(Agent);
	}

	public void callCollisionCallbacks(double time) {
		collisionCount++;

		if (useAccurateCollisionHandler) {
			callCollisionCallbacksAccurate(time);
		} else {
			if (collisionCount % 2 == 0) {
				callCollisionCallbacksFast();
			} else {
				callCollisionCallbacksFastBackwards();
			}
		}
	}

	private void callCollisionCallbacksFast() {
		AbstractList<Agent> copy = app.getEntitiesCopy();

		// System.out.println(copy.size());

		Iterator<Agent> itr = copy.iterator();

		while (itr.hasNext()) {
			Agent collider = itr.next();
			itr.remove();

			Iterator<Agent> itr2 = copy.iterator();

			while (collider.isAlive() && itr2.hasNext()) {
				Agent collidee = itr2.next();
				// System.out.println("checking for collision between " +
				// collider.getWho() + " and " + collidee.getWho());

				if (collidee.isAlive()) {
					// if ((collider instanceof Animal && ((Animal)
					// collider).isMonitored)
					// || (collidee instanceof Animal && ((Animal)
					// collidee).isMonitored)) {
					// System.out.println("Collider " + collider.getWho()
					// + ":" + (int) collider.getX() + ","
					// + (int) collider.getY() + " collidee"
					// + collidee.getWho() + ":"
					// + (int) collidee.getX() + ","
					// + (int) collidee.getY());
					// }
					if (doTheyCollide(collider, collidee)) {
						// if ((collider instanceof Animal && ((Animal)
						// collider).isMonitored)
						// || (collidee instanceof Animal && ((Animal)
						// collidee).isMonitored)) {
						// System.out.println("yes");
						// }
						resolveCollision(collider, collidee, 1.0);
					} else {
						// System.out.println("no");
					}
				}
			}
		}
	}

	private void callCollisionCallbacksFastBackwards() {
		AbstractList<Agent> copy = app.getEntitiesCopy();

		// System.out.println(copy.size());

		ListIterator<Agent> itr = copy.listIterator(copy.size());

		while (itr.hasPrevious()) {
			Agent collider = itr.previous();
			itr.remove();

			if (collider.isAlive()) {

				ListIterator<Agent> itr2 = copy.listIterator(copy.size());

				while (itr2.hasPrevious()) {
					Agent collidee = itr2.previous();
					// System.out.println("checking for collision between " +
					// collider.getWho() + " and " + collidee.getWho());

					if (collidee.isAlive()) {
						// if ((collider instanceof Animal && ((Animal)
						// collider).isMonitored)
						// || (collidee instanceof Animal && ((Animal)
						// collidee).isMonitored)) {
						// System.out.println("Collider " + collider.getWho()
						// + ":" + (int) collider.getX() + ","
						// + (int) collider.getY() + " collidee"
						// + collidee.getWho() + ":"
						// + (int) collidee.getX() + ","
						// + (int) collidee.getY());
						// }
						if (doTheyCollide(collider, collidee)) {
							// if ((collider instanceof Animal && ((Animal)
							// collider).isMonitored)
							// || (collidee instanceof Animal && ((Animal)
							// collidee).isMonitored)) {
							// System.out.println("yes");
							// }
							resolveCollision(collider, collidee, 1.0);
						} else {
							// System.out.println("no");
						}
					}
				}
			}
		}
	}

	// note: currently might do double collision checks. need to make this
	// smarter
	private void callCollisionCallbacksAccurate(double time) {
		final int MAX_DEPTH = 20;

		int depth = 0;
		double lastTime = time;
		boolean collisions = true;
		Agent firstCollider = null;
		Agent firstCollidee = null;
		while (collisions && depth < MAX_DEPTH) {
			// System.out.println("depth " + depth);
			depth++;
			collisions = false;
			double earliestTime = 2;
			AbstractList<Agent> copy = app.getEntitiesCopy();

			Iterator<Agent> itr = copy.iterator();

			while (itr.hasNext()) {
				Agent collider = itr.next();
				itr.remove();

				if (collider.isAlive() && collider.wasPositionChanged()) {

					Iterator<Agent> itr2 = copy.iterator();

					while (itr2.hasNext()) {
						Agent collidee = itr2.next();
						// System.out.println("checking for collision between "
						// + collider.getWho() + " and " + collidee.getWho());

						if (collidee.isAlive()) {

							double collisionTime = getCollisionTime(collider,
									collidee) + time;
							if (collisionTime >= time
									&& collisionTime < earliestTime
									&& collisionTime <= 1
									&& !(collisionTime == time || ((collider == firstCollider && collidee == firstCollidee) || collider == firstCollidee
											&& collidee == firstCollider))) {
								// System.out.println("collider x: " +
								// collider.getLastX());
								// System.out.println("collider y: " +
								// collider.getLastY());
								// System.out.println("collidee x: " +
								// collidee.getLastX());
								// System.out.println("collidee y: " +
								// collidee.getLastY());
								// System.out.println("time: " + collisionTime);
								earliestTime = collisionTime;
								firstCollider = collider;
								firstCollidee = collidee;
								collisions = true;
							}
						}
					}
				}
			}
			// System.out.println("\nEnd of Loop\n");
			if (collisions) {
				lastTime = time;
				time = earliestTime;
				resolveCollision(firstCollider, firstCollidee, time - lastTime);
				firstCollider.setPositionChanged(false);
				firstCollidee.setPositionChanged(false);

				// System.out.println("\nx1: " + firstCollider.getX());
				// System.out.println("y1: " + firstCollider.getY());
				// System.out.println("x2: " + firstCollidee.getX());
				// System.out.println("y2: " + firstCollidee.getY() + "\n");
			}
			for (Agent e : app.getEntities()) {
				if (e != firstCollider && e != firstCollidee) {
					e.setLastXY(e.getLastX() + (e.getX() - e.getLastX())
							* (time - lastTime),
							e.getLastY() + (e.getY() - e.getLastY())
									* (time - lastTime));
				}
			}
		}
		for (Agent e : app.getEntities()) {
			e.setPositionChanged(false);
		}
		// System.out.println("Done");
	}

	public void resolveCollision(Agent collider, Agent collidee, double time) {
		// double colliderX = collider.getLastX() + time
		// * (collider.getX() - collider.getLastX());
		// double colliderY = collider.getLastY() + time
		// * (collider.getY() - collider.getLastY());
		// double collideeX = collidee.getLastX() + time
		// * (collidee.getX() - collidee.getLastX());
		// double collideeY = collidee.getLastY() + time
		// * (collidee.getY() - collidee.getLastY());
		ListOfCollisionHandlers colliderHandlers = collisionHandlers
				.get(collider);
		ListOfCollisionHandlers collideeHandlers = collisionHandlers
				.get(collidee);
		AbstractList<CollisionHandler> colliderHandlerList = null;
		AbstractList<CollisionHandler> collideeHandlerList = null;
		if (colliderHandlers != null) {
			colliderHandlerList = colliderHandlers
					.getMatchingHandlers(collidee);
		}
		if (collideeHandlers != null) {
			collideeHandlerList = collideeHandlers
					.getMatchingHandlers(collider);
		}
		if (colliderHandlerList != null) {
			for (CollisionHandler collisionHandler : colliderHandlerList) {
				if (collisionHandler.getCollideeClass().isInstance(collidee)) {
					if (collider.isAlive() && collidee.isAlive()) {
						collisionHandler.collided(collider, collidee, time);
					}
				}
			}
		}

		if (collideeHandlerList != null) {
			for (CollisionHandler collisionHandler : collideeHandlerList) {
				if (collisionHandler.getCollideeClass().isInstance(collider)) {
					// make sure that this collision handler wasn't already
					// called by collider
					boolean alreadyCalled = false;
					if (colliderHandlerList != null) {
						for (CollisionHandler collisionHandler2 : colliderHandlerList) {
							if (collisionHandler == collisionHandler2) {
								alreadyCalled = true;
								break;
							}
						}
					}
					if (!alreadyCalled) {
						if (collider.isAlive() && collidee.isAlive()) {
							collisionHandler.collided(collidee, collider, time);
						}
					}
				}
			}
		}
	}

	public double getCollisionTime(Agent e1, Agent e2) {
		double vx = (e1.getX() - e1.getLastX()) - (e2.getX() - e2.getLastX());
		double vy = (e1.getY() - e1.getLastY()) - (e2.getY() - e2.getLastY());
		// System.out.println("x1: " + e1.getLastX());
		// System.out.println("y1: " + e1.getLastY());
		// System.out.println("x2: " + e2.getLastX());
		// System.out.println("y2: " + e2.getLastY());
		double dx = e1.getLastX() - e2.getLastX();
		double dy = e1.getLastY() - e2.getLastY();
		double r = e1.getBoundingCylinderRadius()
				+ e2.getBoundingCylinderRadius();
		// System.out.println("vx: " + vx);
		// System.out.println("vy: " + vy);
		// System.out.println("dx: " + dx);
		// System.out.println("dy: " + dy);
		// System.out.println("r: " + r);
		double c = Math.pow(dx, 2) + Math.pow(dy, 2) - Math.pow(r, 2);
		// System.out.println("c: " + c);
		if (c < 0) {
			return 0;
		}
		double a = Math.pow(vx, 2) + Math.pow(vy, 2);
		// System.out.println("a: " + a);
		if (a == 0) {
			return -1;
		}
		double b = 2 * (dx * vx + dy * vy);
		// System.out.println("b: " + b);
		double root = Math.pow(b, 2) - 4 * a * c;
		// System.out.println("root: " + root);
		if (root < 0) {
			return -1;
		}
		double t = (-b - Math.sqrt(root)) / (2 * a);
		// System.out.println("time: " + t + "\n");
		return t;
	}

	public boolean doTheyCollide(Agent Agent1, Agent Agent2) {
		double dx = Agent1.getX() - Agent2.getX();
		double dy = Agent1.getY() - Agent2.getY();
		// NJM not sure what the .5 is about...
		double dh = (Agent1.getAltitude() - Agent2.getAltitude()) * .5;
		// (deeheight-(SLNUM_TO_FLOAT(vtder->height_above_terrain)+SLNUM_TO_FLOAT(get_patch_height((int)SLNUM_TO_FLOAT(vtder->xcor),
		// (int)SLNUM_TO_FLOAT(vtder->ycor)))))*.5;
		double r = Agent1.getBoundingCylinderRadius()
				+ Agent2.getBoundingCylinderRadius();

		// if ((Agent1 instanceof Animal && ((Animal) Agent1).isMonitored)
		// || (Agent2 instanceof Animal && ((Animal) Agent2).isMonitored)) {
		//
		// System.out.println("r:" + r + "dx:" + dx + "dy:" + dy + "dh:" + dh);
		// System.out.println("a1: " + Agent1.getBoundingCylinderTop() + "," +
		//
		// Agent1.getBoundingCylinderBottom() + " a2:"
		// + +Agent2.getBoundingCylinderTop() + ","
		// + Agent2.getBoundingCylinderBottom());
		// }

		// printf("Collision heights: heightDiff: %f, derh-deel: %f, derl-deeh:
		// %f\n",
		// dh,
		// (SLNUM_TO_FLOAT(vtder->bounding_height_high) - deelow),
		// (SLNUM_TO_FLOAT(vtder->bounding_height_low) - deehigh));
		if (((dx * dx + dy * dy) <= (r * r)) // ) {
				// NJM, getBoundingTop, Bottom return 0 sometimes. have to look
				// into
				// this.
				&& ((dh <= (Agent2.getBoundingCylinderTop() - Agent1
						.getBoundingCylinderBottom())) && (dh > (Agent2
						.getBoundingCylinderBottom() - Agent1
						.getBoundingCylinderTop())))) {
			return true;
		} else {
			return false;
		}
	}
}
