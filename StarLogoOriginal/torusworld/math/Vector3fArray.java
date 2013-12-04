package torusworld.math;

import java.util.ArrayList;


public class Vector3fArray {
	public ArrayList<Vector3f> pointArray;

	public Vector3fArray() {
		pointArray = new ArrayList<Vector3f>();	
	}

	public ArrayList<Vector3f> getArray() {
		return pointArray;	
	}

	public Vector3f get(int i) {
		return pointArray.get(i);
	}

	public void add(Vector3f component) {
		pointArray.add(component);
	}

	public int size() {
		return pointArray.size();
	}	

}
