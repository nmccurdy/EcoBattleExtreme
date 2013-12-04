package torusworld.math;

import java.util.ArrayList;


public final class BoundingBoxMath {

	public static Vector3f makeVectorFromPoints(Vector3f point1, Vector3f point2) {
		return new Vector3f(point1.x-point2.x, point1.y-point2.y, point1.z-point2.z);
	}

	public static Vector3f findNormalOfSide(ArrayList<?> polygonVectors) {
		Vector3f vector1 = makeVectorFromPoints((Vector3f)polygonVectors.get(2), (Vector3f)polygonVectors.get(0));
		Vector3f vector2 = makeVectorFromPoints((Vector3f)polygonVectors.get(1), (Vector3f)polygonVectors.get(0));
	
		Vector3f normalVector = Vector3f.cross(vector1, vector2);
		normalVector.normalize();
	
		return normalVector;
	}

	public static float findPlaneDistance(Vector3f normalVector, Vector3f pointVector) {
		
		float distance = 0;

		distance = -((normalVector.x * pointVector.x) +
			     (normalVector.y * pointVector.y) +
			     (normalVector.z * pointVector.z));
	
		return distance;
	}


	public static boolean IntersectedPlane(ArrayList<?> polygonVectors, ArrayList<?> lineVectors, Vector3f normalVector, float originDistance) {

		float distance1=0, distance2=0;
		originDistance = findPlaneDistance(normalVector, (Vector3f)polygonVectors.get(0));
		distance1 = ((normalVector.x * ((Vector3f)lineVectors.get(0)).x) +
			     (normalVector.y * ((Vector3f)lineVectors.get(0)).y) +
			     (normalVector.z * ((Vector3f)lineVectors.get(0)).z)) + originDistance;
		
		distance2 = ((normalVector.x * ((Vector3f)lineVectors.get(1)).x) +
			     (normalVector.y * ((Vector3f)lineVectors.get(1)).y) +
			     (normalVector.z * ((Vector3f)lineVectors.get(1)).z)) + originDistance;
// should it be >=
		if((distance1 * distance2) > 0) 
			return false;
		return true;
	}

	public static double findAngleBetweenVectors(Vector3f vector1, Vector3f vector2) {
		float dotProduct = Vector3f.dot(vector1, vector2);
		float vectorsMagnitude = vector1.length() * vector2.length();
		double angle = Math.acos(dotProduct/vectorsMagnitude);
		if(new Double(angle).isNaN())
		 	return 0;
		return angle;
	}

	public static Vector3f findIntersectionPoint(Vector3f normalVector, ArrayList<?> lineVectors, double distance) {
		Vector3f vectorPoint, lineVectorDirection;
		float x, y, z;

		double Numerator = 0.0, Denominator = 0.0, dist = 0.0;

		lineVectorDirection = makeVectorFromPoints((Vector3f)lineVectors.get(1), (Vector3f)lineVectors.get(0));
		lineVectorDirection.normalize();
		Numerator = - (normalVector.x * ((Vector3f)lineVectors.get(0)).x +
			       normalVector.y * ((Vector3f)lineVectors.get(0)).y + 
			       normalVector.z * ((Vector3f)lineVectors.get(0)).z + 
			       distance);

		Denominator = Vector3f.dot(normalVector, lineVectorDirection);
		if (Denominator == 0.0) 
			return (Vector3f)lineVectors.get(0);

		dist = Numerator/Denominator;

		x = (float)(((Vector3f)lineVectors.get(0)).x + (lineVectorDirection.x * dist));
		y = (float)(((Vector3f)lineVectors.get(0)).y + (lineVectorDirection.y * dist));
		z = (float)(((Vector3f)lineVectors.get(0)).z + (lineVectorDirection.z * dist));
		vectorPoint = new Vector3f(x, y, z);	
		return vectorPoint;
	}

	public static boolean InsidePolygon(Vector3f vectorIntersection, ArrayList<?> polygonVectors, long verticeCount) {
		double MATCH_FACTOR = 0.9999;
		double Angle = 0.0;
		Vector3f vectorA, vectorB;
		for (int i = 0; i < verticeCount; i++) {
//System.out.println(vectorIntersection);
//System.out.println((Vector3f)polygonVectors.get(i));
//System.out.println((Vector3f)polygonVectors.get(new Long((i+1)%verticeCount).intValue()));
//System.out.println();
			vectorA = makeVectorFromPoints((Vector3f)polygonVectors.get(i), vectorIntersection);
			vectorB = makeVectorFromPoints((Vector3f)polygonVectors.get(new Long((i+1) % verticeCount).intValue())
											, vectorIntersection);
//System.out.println(vectorIntersection);
//System.out.println(vectorA.toString());
//System.out.println(vectorB.toString());
//System.out.println();
//System.out.println(findAngleBetweenVectors(vectorA, vectorB));
			if ((vectorA.x == 0 && vectorA.y == 0 && vectorA.z == 0) ||
			    (vectorB.x == 0 && vectorB.y == 0 && vectorB.z == 0))
				return true;
			Angle += findAngleBetweenVectors(vectorA, vectorB);
			
		}
		if(Angle >= (MATCH_FACTOR * (2.0 * Math.PI))) {
			return true;
		}
		return false;
		

	}

	public static boolean checkIntersectedPolygon(ArrayList<?> polygonVectors, ArrayList<?> lineVectors, int verticeCount) {
	  	Vector3f normalVector = findNormalOfSide(polygonVectors); 
		float originDistance = 0;

		if (!IntersectedPlane(polygonVectors, lineVectors, normalVector, originDistance)) {
			return false;
		}
		Vector3f vectorIntersection = findIntersectionPoint(normalVector, lineVectors, originDistance);
		if(InsidePolygon(vectorIntersection, polygonVectors, verticeCount)) {
			return true;
		}
		return false;
	}




}
