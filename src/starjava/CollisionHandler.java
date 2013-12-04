package starjava;

public abstract class CollisionHandler<T1, T2> {

	protected Class collideeClass;
	
	public CollisionHandler(Class collideeClass) {
		this.collideeClass = collideeClass;
	}
	
	public Class getCollideeClass() {
		return collideeClass;
	}
	
	
	public abstract void collided(T1 collider, T2 collidee, double time);
}
