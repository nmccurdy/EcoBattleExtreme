package torusworld.math;

// Abstract class used for float interpolation

public abstract class Interpolator {
	
	public abstract void Init(float Position); // set initial position
	public abstract void Set(float Position); // set the position that (from now on) corresponds to t=1
	public abstract float Get(float t);
	public abstract float GetEndPos(); // the last parameter to Set(), probably equal to Get(1)
	public abstract float GetLastPos(); // the before last parameter to Set
	public abstract boolean isMoving(); // is the object moving? i.e. were last two Set's equal ?
	public abstract void TranslateInterpolator(double delta); /* this function is used to add delta from
															   all internal state of the interpolator -
														       as if all the Set's so far have been + delta.
															   used for headings to substract or add 2*PI
															   before a new Set */
}

