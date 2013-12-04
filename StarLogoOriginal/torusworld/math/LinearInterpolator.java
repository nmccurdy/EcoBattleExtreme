package torusworld.math;
import torusworld.math.Interpolator;

public class LinearInterpolator extends Interpolator {
	float last, current;

	public void Init(float Position)
	{
		last = current = Position;
	}

	public void Set(float Position)
	{
		last = current;
		current = Position;
	}

	public float Get(float t)
	{
		return last + (current - last)*t;
	}

	public float GetLastPos()
	{
		return last;
	}

	public float GetEndPos()
	{
		return current;
	}

	public boolean isMoving()
	{
		return Math.abs(last - current) > 1e-5f;
	}

	public void TranslateInterpolator(double delta) 
	{
		last += delta;
		current += delta;
	}
}


