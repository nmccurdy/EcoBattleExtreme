package torusworld.model.kml;

import java.util.ArrayList;
import java.util.HashMap;

import torusworld.math.Matrix4f;

public class ColladaNode {

	public String nodeID;
	public HashMap<String, ColladaNode> childrenNodes = new HashMap<String, ColladaNode>();
	public Matrix4f local_transform; 
	public ArrayList<String> geoList = new ArrayList<String>();
	
	public ColladaNode (String nodeID)
	{
		this.nodeID = nodeID;
	}
	
}
