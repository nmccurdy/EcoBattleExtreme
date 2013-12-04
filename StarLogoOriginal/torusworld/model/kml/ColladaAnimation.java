package torusworld.model.kml;

import java.util.ArrayList;

import torusworld.math.Matrix4f;

enum transformType
{
	TRANSLATE, ROTATE, TRANFORM
}

enum interploationType
{
	LINEAR, BEZIER
}


public class ColladaAnimation {
	
	ArrayList<ColladaAnimation> childrenAnimations = new ArrayList<ColladaAnimation>();
	
	String animationID;
	
	public Float[] keyFrames;
	public int numFrames;
	
	public Matrix4f[] transformation; 
	public Float[][] translation;
	public Float[] rotation; 
	
	public String[] interpolationMethods; 
	              
	public ColladaAnimation(String animationID)
	{
		this.animationID = animationID; 
	}	
}
