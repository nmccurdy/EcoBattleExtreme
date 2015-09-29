package application;

public interface MovingObjectInterface {
	
	public abstract double getMass();

	public abstract double getAccelerationX();

	public abstract double getAccelerationY();

	public abstract void setAccelerationX(double acceleration);

	public abstract void setAccelerationY(double acceleration);
	
	public abstract double getAccelerationZ();

	public abstract void setAccelerationZ(double accelerationZ);

	public abstract double getVelocityX();

	public abstract double getVelocityY();

	public abstract double getVelocityZ();

	public abstract void setDesiredVelocity(double velocity);

	public abstract void setDesiredVelocityZ(double velocity);
	
	public abstract double getVelocityMagnitude2();

}