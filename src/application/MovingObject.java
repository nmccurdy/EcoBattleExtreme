package application;

import starjava.Agent;

public class MovingObject implements MovingObjectInterface {

	private static final double GRAVITY = 1;
	private static final double FRICTION_AIR = .05;
	private static final double FRICTION_GROUND = 1;
	public static final double NO_VELOCITY = -1000;

	private double distancePerVelocity = .2;
	private double mass = 0;
	private double maxEnergy = 0;
	
	private double velocityX = 0;
	private double velocityY = 0;
	private double accelerationX = 0;
	private double accelerationY = 0;
	
	private double desiredVelocity = 0;

	private double velocityZ = 0;
	private double accelerationZ = 0;
	
	private double desiredVelocityZ = 0;

	private double velocityBeforeAccelX = 0;
	private double velocityBeforeAccelY = 0;
	private double velocityBeforeAccelZ = 0;
	
	private final int MASS_AVG_FACTOR = 30;

	public MovingObject(double maxEnergy, double distancePerVelocity) {
		this.maxEnergy = maxEnergy;
		this.distancePerVelocity = distancePerVelocity;
	}

	
	public void moveObject(Agent agent) {
		double newX = agent.getX() + velocityX * distancePerVelocity;
		double newY = agent.getY() + velocityY * distancePerVelocity;

		agent.walkToXY(newX, newY);
		
		double height = agent.getHeightAboveTerrain();
		height += velocityZ * distancePerVelocity;

		if (height <= 0) {
			height = 0;
			velocityZ = 0;
		}
		agent.setHeightAboveTerrain(height);
	}

	public void adjustVelocityXY(double heightAboveTerrain, double heading) {
		
		double vAngle = Math.atan2(velocityY, velocityX);
		
		double frictionX = Math.abs(Math.cos(vAngle));
		double frictionY = Math.abs(Math.sin(vAngle));
		
//		System.out.println("a" + velocityX + "," + velocityY + " " + frictionX + "," + frictionY);
//		System.out.println(velocityX + "," + velocityY + " " + vAngle + " " + frictionX + " " + frictionY);
		velocityX = getVelocityAfterFriction(heightAboveTerrain, velocityX, frictionX);
		velocityY = getVelocityAfterFriction(heightAboveTerrain, velocityY, frictionY);

		
		velocityBeforeAccelX = velocityX;
		velocityBeforeAccelY = velocityY;

//		System.out.println("b" + velocityX + "," + velocityY);

//		System.out.println(velocityX + "," + velocityY);

		// calculate the appropriate acceleration if we have
		// a target velocity
		if (desiredVelocity != NO_VELOCITY) {
			double desiredVelocityX = Math.sin(Math.toRadians(heading)) * desiredVelocity;
			double desiredVelocityY = Math.cos(Math.toRadians(heading)) * desiredVelocity;
			accelerationX = desiredVelocityX - velocityX;
			accelerationY = desiredVelocityY - velocityY;
		}

		velocityX += accelerationX;
		velocityY += accelerationY;

//		System.out.println(velocityX + "," + velocityY);

	}


	private double getVelocityAfterFriction(double heightAboveTerrain, double velocity, double friction) {
		int negative;
		// friction should happen in opposite direction of velocity
		if (velocity >= 0) {
			negative = 1;
		} else {
			negative = -1;
		}

		if (heightAboveTerrain >= 1) {
			velocity -= negative * FRICTION_AIR * friction;
		} else {
			velocity -= negative * FRICTION_GROUND * friction;
		}

		// friction shouldn't cause object to reverse direction
		if (negative == 1 && velocity < 0) {
			velocity = 0;
		}

		if (negative == -1 && velocity > 0) {
			velocity = 0;
		}
		
		return velocity;
	}

	public void adjustZVelocity() {
		int negative;

		// friction should happen in opposite direction of velocity
		if (velocityZ >= 0) {
			negative = 1;
		} else {
			negative = -1;
		}

		// air friction creates a terminal velocity that is proportional to v^2
		velocityZ -= negative * FRICTION_AIR * (velocityZ * velocityZ);

		// friction shouldn't cause object to reverse direction
		if (negative == 1 && velocityZ < 0) {
			velocityZ = 0;
		}

		if (negative == -1 && velocityZ > 0) {
			velocityZ = 0;
		}

		
		velocityZ -= GRAVITY;

		velocityBeforeAccelZ = velocityZ;

		if (desiredVelocityZ != NO_VELOCITY) {
			accelerationZ = desiredVelocityZ - velocityZ;
		}
		
		velocityZ += accelerationZ;
		
	}

	public double getEnergyNeeded() {
		return getEnergyNeeded(Math.sqrt(getAccelerationMagnitude2()));
	}
	
	public double getEnergyNeeded(double acceleration) {
		double massMultiplier = ((mass / maxEnergy) * 2);
		double accelerationPenalty = Math.pow(acceleration, 2);
		
		return massMultiplier * accelerationPenalty;
	}
	

	public void adjustMass(double energy) {
		mass = (mass * MASS_AVG_FACTOR + energy) / (MASS_AVG_FACTOR + 1);
	}

	
	// to be overridden by animals
	public void doActions() {

	}


	public double getMass() {
		return mass;
	}

	/* (non-Javadoc)
	 * @see application.MovingObjectInterface#getAcceleration()
	 */
	public double getAccelerationX() {
		return accelerationX;
	}
	

	public double getAccelerationY() {
		return accelerationY;
	}

	/* (non-Javadoc)
	 * @see application.MovingObjectInterface#setAcceleration(double)
	 */
	public void setAccelerationX(double acceleration) {
		this.accelerationX = acceleration;
	}

	public void setAccelerationY(double acceleration) {
		this.accelerationY = acceleration;
	}

	/* (non-Javadoc)
	 * @see application.MovingObjectInterface#getAccelerationZ()
	 */
	public double getAccelerationZ() {
		return accelerationZ;
	}

	/* (non-Javadoc)
	 * @see application.MovingObjectInterface#setAccelerationZ(double)
	 */
	public void setAccelerationZ(double accelerationZ) {
		this.accelerationZ = accelerationZ;
	}

	/* (non-Javadoc)
	 * @see application.MovingObjectInterface#getVelocity()
	 */
	public double getVelocityX() {
		return velocityX;
	}

	public double getVelocityY() {
		return velocityY;
	}

	/* (non-Javadoc)
	 * @see application.MovingObjectInterface#getVelocityZ()
	 */
	public double getVelocityZ() {
		return velocityZ;
	}

	/* (non-Javadoc)
	 * @see application.MovingObjectInterface#setDesiredVelocity(double)
	 */
	public void setDesiredVelocity(double velocity) {
		this.desiredVelocity = velocity;
	}

	/* (non-Javadoc)
	 * @see application.MovingObjectInterface#setDesiredVelocityZ(double)
	 */
	public void setDesiredVelocityZ(double velocity) {
		this.desiredVelocityZ = velocity;
	}


	public void stopAllAcceleration() {
		accelerationX = 0;
		accelerationY = 0;
		accelerationZ = 0;
		
		velocityX = velocityBeforeAccelX;
		velocityY = velocityBeforeAccelY;
		velocityZ = velocityBeforeAccelZ;
		
		
	}


	@Override
	public double getVelocityMagnitude2() {
		return (velocityX*velocityX) + (velocityY * velocityY) + (velocityZ * velocityZ);
	}

	public double getAccelerationMagnitude2() {
		return (accelerationX*accelerationX) + (accelerationY * accelerationY) + (accelerationZ * accelerationZ);
	}

}