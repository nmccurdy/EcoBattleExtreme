package application;

import starjava.CollisionHandler;

public final class GrassGrassCollision extends CollisionHandler<Grass, Grass> {

	public static GrassGrassCollision collision = new GrassGrassCollision();
	
	public GrassGrassCollision() {
		super(Grass.class);
	}
	
	@Override
	public void collided(Grass collider, Grass collidee, double time) {

		/***** Focus on this *****/
		
		// make the younger one move out of the way.  the younger one
		// will die if it can't find a free space within a few steps.
		
		if (collider.getAge() > collidee.getAge()) {
			collidee.moveToFreeSpace();
		} else {
			collider.moveToFreeSpace();
		}
		
		/***** Focus on this (end) *****/
	}
}