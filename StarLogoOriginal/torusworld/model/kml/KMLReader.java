package torusworld.model.kml;

import java.io.File;
import java.io.IOException;
import org.jdom.*;
import org.jdom.input.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.media.opengl.GL;
import torusworld.math.Matrix4f;
import torusworld.math.Vector3f;
import torusworld.model.kml.ColladaElem;

/**
 * KML reads from doc.kml files, and then use the link inside to read the .dae file
 * DAE file is the COLLADA model file, w
 * 
 */

public class KMLReader {

	private static final boolean DEBUG = false;
	/**
	 * Fields
	 */
	private String daePath;
	private Document kmlDoc;
	private Document daeDoc;
	public ArrayList<Polygon3D> polygonMesh = new ArrayList<Polygon3D>();
	public COLLADALibrary textureLibrary = null;
	private boolean hasAnimation; 
	private Element up_axis;
	private double scale = 0.0;
    
	//node related mapping
	private HashMap<String, HashMap<String, String>> globalMaterialMap = new HashMap<String, HashMap<String, String>>();
    private ColladaNode sceneHierarchy;
    private HashMap<String, ArrayList<Matrix4f>> geoTransformMap = new HashMap<String, ArrayList<Matrix4f>>();
    
    //Skin controller mapping
    private HashMap<String, HashMap<String, Matrix4f>> skin_bindMatrixMap = new HashMap<String, HashMap<String, Matrix4f>>();
	private HashMap<String, ArrayList<HashMap<String, Float>>> skin_vertexWeightMap = new HashMap<String, ArrayList<HashMap<String, Float>>>();
   
	//Animation data
	
	/**
	 * Creates new instance of DAEReader 
	 */
	public KMLReader()
	{
	}
	
	/**
	 * read methods reads from a dae file and instantiate the list of polygons
	 * creates texture lib from the dae file
	 * 
	 * @param file
	 * @param prefix
	 * @return
	 * @throws Exception
	 */
	public boolean read(String file, String prefix)
	{
		//jdom xml parser used to read the .dae file
		SAXBuilder parser = new SAXBuilder();

		try
		{
			if (!file.endsWith("kml"))
			{
				throw new IOException("not valid file");
			}
			
			File KMLFile = new File(file);
			
			if (!KMLFile.isFile())
			{
				throw new IOException("not valid file");
			}
			
			this.kmlDoc = parser.build(KMLFile);
			this.daePath= fileNameDAE(prefix);
			
			if (!this.daePath.endsWith("dae"))
			{
				throw new IOException("not valid file");
			}
			
			File DAEFile = new File(daePath);
		
			this.daeDoc = parser.build(DAEFile); 
			
			this.textureLibrary = new COLLADALibrary(this.daeDoc, this.daePath);
			
			return findMesh();
		}
		
		catch (Exception e)
		{
			e.printStackTrace();
			return false; 
		}
	}
	
	
	/**
	 * @return String fileName, the link of the .dae file embedded in the doc.kml file
	 */
	public String fileNameDAE(String prefix)
	{
		
		try
		{
			Element KML = kmlDoc.getRootElement();
			Namespace kmlns = KML.getNamespace();
			
			Element PlaceMark = KML.getChild("Folder", kmlns).getChild("Placemark", kmlns);
			
			String daeLink = PlaceMark.getChild("Model", kmlns).getChild("Link", kmlns).getChild("href", kmlns).getValue();
			
			return prefix + daeLink;
		}
		
		catch (Exception e)
		{
			e.printStackTrace();
			return null; 
		}
		
	}
	
	/**
	 * Parse through the DAE xml to and instantiates all the Polygon3Ds
	 * @return boolean once parsing is finished
	 */
	
	public boolean findMesh() 
	{
		if (this.daeDoc == null)
		{
			return false;	
		}
		
		else 
		{
			try 
			{
				Element Collada = daeDoc.getRootElement();
				Namespace ns = Collada.getNamespace();
				
				up_axis = (Element) Collada.getChild(ColladaElem.asset, ns).getChild("up_axis", ns);
				
				scale = Collada.getChild(ColladaElem.asset, ns).getChild("unit", ns).getAttribute("meter").getDoubleValue();
				
				//get library_geometry
				Element lib_geo = (Element) Collada.getChild(ColladaElem.lib_geometries, ns);
				//get local to global reference of materials
				Element lib_visual_scene = (Element) Collada.getChild(ColladaElem.lib_visual_scenes, ns);
				Element lib_nodes = (Element) Collada.getChild(ColladaElem.lib_nodes, ns);
				
				Element lib_animations = (Element) Collada.getChild(ColladaElem.lib_animations, ns);
				
				//get a list of geometry -> polygons
				if(lib_nodes!=null)
					this.getMaterialLocalGlobalMapping(lib_nodes, ns);
				if(lib_visual_scene!=null)
					this.getMaterialLocalGlobalMapping(lib_visual_scene, ns);

				HashMap<String, ArrayList<Polygon3D>> map_geo_polygons = this.parsePolygons(lib_geo, ns);
				if (lib_animations != null)
				{
					this.hasAnimation = true; 
				}
				
				//if there is animation, then we have to parse through library_scene to get all nodes / transformation / skeleton info
				if (hasAnimation)
				{
					this.findAnimations();
					this.findSkinControl();
					//TODO: skeleton node parsing code goes here
					this.parseAnimLibVisualScene(lib_visual_scene, ns);
				}
				
				//else 
				else 
				{
					this.parseNodeHierarchy(lib_nodes, lib_visual_scene, ns);
					this.transformPolygons(map_geo_polygons);
				}
				
				
				//System.out.print(globalMaterialMap);
				if(DEBUG)
				System.out.print(this.geoTransformMap);
				
				
				if(DEBUG)
				System.out.println("number of polygons = " + polygonMesh.size());
				return true;
			}
			
			catch(Exception e)
			{
				e.printStackTrace();
				return false; 
			}
			
		}	
	}
	
	

	/**
	 * This method parse the skeleton node structure to be used for animation framing, need to implement
	 * @param libVisualScene
	 * @param ns
	 */

	private void parseAnimLibVisualScene(Element libVisualScene, Namespace ns) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Transform the polygons and add it to Polygon3D 
	 * @param map_geo_polygons
	 * @return the list of polygons to be rendered in StarLogo 
	 */
	private void transformPolygons(HashMap<String, ArrayList<Polygon3D>> map_geo_polygons) {
		
		
		Iterator<String> key_itr = this.geoTransformMap.keySet().iterator();
		
		while (key_itr.hasNext())
		{
			String geoID = key_itr.next();
			ArrayList<Matrix4f> transforms = geoTransformMap.get(geoID);
			ArrayList<Polygon3D> polygons = map_geo_polygons.get(geoID);
			
			for (int i = 0; i < transforms.size() ; i++)
			{
				ArrayList<Polygon3D> polylist = this.transformPolygonsHelper(transforms.get(i), polygons);
				this.polygonMesh.addAll(polylist);
			}
		}
	}

	//return a list of transformed polygons without altering the original polygon
	
	private ArrayList<Polygon3D> transformPolygonsHelper(Matrix4f matrix4f, ArrayList<Polygon3D> polygons) {
		
		ArrayList<Polygon3D> plist = new ArrayList<Polygon3D>();
		
		for (int i = 0; i < polygons.size(); i++)
        {
            Polygon3D p = polygons.get(i).clone();
            
            for (int j = 0; j < 3; j++)
            {
                Vector3f v = new Vector3f(p.vertices[3 * j], p.vertices[3 * j + 1],
                                          p.vertices[3 * j + 2]);
                matrix4f.transform(v);
                p.vertices[3 * j] = v.x;
                p.vertices[3 * j + 1] = v.y;
                p.vertices[3 * j + 2] = v.z;

                if (p.normal != null)
                {
                    v = new Vector3f(p.normal[3 * j + 0], p.normal[3 * j + 1], p.normal[3 * j + 2]);
                    matrix4f.rotate(v);
                    v.normalize();
                    p.normal[3 * j + 0] = v.x;
                    p.normal[3 * j + 1] = v.y;
                    p.normal[3 * j + 2] = v.z;
                }
            }

            if (p.faceNormal != null)
            {
                Vector3f normal = new Vector3f(p.faceNormal[0], p.faceNormal[1], p.faceNormal[2]);
                matrix4f.rotate(normal);
                normal.normalize();
                p.faceNormal[0] = normal.x;
                p.faceNormal[1] = normal.y;
                p.faceNormal[2] = normal.z;
            }
            
            plist.add(p);
        }
		return plist;
	}

	@SuppressWarnings("unchecked")
	private HashMap<String, ArrayList<Polygon3D>> parsePolygons(Element lib_geo, Namespace ns) throws Exception
	{
		try 
		{
			
			HashMap<String, ArrayList<Polygon3D>> map_geo_polygons = new HashMap<String, ArrayList<Polygon3D>>();
			
			Iterator<Element> geometries =  lib_geo.getChildren("geometry", ns).iterator();
			
			//get the list of geometries inside the .dae file
			while (geometries.hasNext())
			{
				
				Element geometry = geometries.next();
				String geoID = geometry.getAttributeValue(ColladaAttribute.id);
				map_geo_polygons.put(geoID, new ArrayList<Polygon3D>());
				
				HashMap<String, String> localMap = globalMaterialMap.get(geoID);
				if(localMap==null){
					System.out.println("id "+geoID);
					//continue;
				}
				
				Element mesh = geometry.getChild("mesh", ns);
					
				HashMap<String, Float[]> sources = new HashMap<String, Float[]>();
				HashMap<String, String> vertices_ref = new HashMap<String, String>();
				
				//first get the list of sources, which are float arrays
				this.getMeshArrays(geometry, //posArr, normArr, uvArr, 
								   sources, 
								   vertices_ref, 
								   ns);
				
				//after parsing the arrays, we parse the list of triangles using the
				Iterator<Element> triangles_itr = mesh.getChildren("triangles", ns).iterator();
				
				while(triangles_itr.hasNext())
				{
					Element triangles = triangles_itr.next();
					
					
					ArrayList<Polygon3D> polygons = this.parseTriangles(triangles, ns, 
																		//posArr, normArr, uvArr
																		sources, vertices_ref, geoID, localMap);
					
					map_geo_polygons.get(geoID).addAll(polygons);
					
				}	
			}
			
			return map_geo_polygons;
		}
		
		catch (Exception e)
		{
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public ArrayList<Polygon3D> getPolygons()
	{
		return this.polygonMesh;
	}
	
	/**
	 * Helper method that parse one particular <triangles> element in the dae file. It uses the <p> index array to find 
	 * the vertices/normals/texture vectors in the float arrays provided. 
	 * 
	 * @param triangles
	 * @param ns
	 * @param localMap 
	 * @param posArr
	 * @param normArr
	 * @param uvArr
	 * @return
	 */

	@SuppressWarnings("unchecked")
	protected ArrayList<Polygon3D> parseTriangles(Element triangles, Namespace ns, 
												  //ArrayList<Float> posArr, ArrayList<Float> normArr, ArrayList<Float> uvArr
												  HashMap<String, Float[]> sources, HashMap<String, String> vertices_ref, String geometryID, HashMap<String, String> localMap)
	{
		
		ArrayList<Polygon3D> polygons = new ArrayList<Polygon3D>();
		
		try
		{
			
			String mat = localMap.get(triangles.getAttributeValue("material"));
			
			Iterator<Element> inputs_itr = triangles.getChildren(ColladaElem.input, ns).iterator();
			int count = Integer.parseInt(triangles.getAttributeValue("count"));
			
			int vOffSet = -1; 
			int nOffSet = -1; 
			int tOffSet = -1; 
			Float[] pos = null; 
			Float[] norm = null;
			Float[] tex = null;
			
			boolean hasTexture = false; 
			
			//the logic to match pos/norm/texture array to the right source arrays
			while (inputs_itr.hasNext())
			{
				Element input = inputs_itr.next();
				String semantic = input.getAttributeValue("semantic");
				String source = input.getAttributeValue("source").substring(1);
				
				int offset = Integer.parseInt(input.getAttributeValue("offset"));
				
				if (semantic.toUpperCase().equals("VERTEX"))
				{
					Iterator<String> it = vertices_ref.keySet().iterator();
					while (it.hasNext())
					{
						String key = it.next(); 
						if (key.equals("POSITION"))
						{
							pos = sources.get(vertices_ref.get(key));
							vOffSet = offset;
						 
						}
							
						else if (key.equals("NORMAL"))
						{
							norm = sources.get(vertices_ref.get(key));
							nOffSet = offset; 
							 
						}
					}
				}
				
				else if (semantic.toUpperCase().equals("NORMAL"))
				{
					norm = sources.get(source);
					nOffSet = offset; 
				}
				
				else if (semantic.toUpperCase().equals("TEXCOORD"))
				{
					tex = sources.get(source);
					tOffSet = offset; 
					hasTexture = true; 
				}
			}
			
			// parse the indices that describe the triangles
			String[] p = triangles.getChild("p", ns).getValue().split(" ");
			int stride = (Math.max(vOffSet, Math.max(nOffSet, tOffSet))+1);
			
			if (p.length !=  count*stride*3)
			{
				throw new Exception("count of triangles doesn't match up");
			}
			
			if (vOffSet == -1 || nOffSet == -1)
			{
				throw new Exception("no vertex or normal specified");
			}
			
			//pos / normal arrays are arranged in XYZ XYZ XYZ format, text coord are arranged in ST ST format
			//triangles use indices to extract coordinates from input arrays, format is : fx, nx, tx, fy, ny, tx, fz, nz, tz ...  
			
			for (int i = 0; i < count ; i ++)
			{
				Polygon3D pg = new Polygon3D();
				pg.materialName = mat;			
				pg.texture = new float[6];
				
				
				for (int j = 0; j < 3; j++ )
				{
					
					int pos_idx = Integer.parseInt(p[i*stride*3 + j*stride + vOffSet]);
					int norm_idx = Integer.parseInt(p[i*stride*3 + j*stride + nOffSet]);
						
					
					for (int k = 0; k < 3; k++)
					{
		
						pg.vertices[j*3+k] = pos[ 3* pos_idx + k ];
						pg.normal[j*3+k] = norm[ 3* norm_idx + k ];	
					}
					
					if (hasTexture)
					{
						int text_idx = Integer.parseInt(p[i*stride*3 + j*stride + tOffSet]);
						
						for (int k = 0; k < 2; k++)
						{
							pg.texture[j*2+k] = tex[ 2 * text_idx + k ];
						}
					}
				}
				
				//treating faceNormal
				float[] faceNormal = new float[3];
	            
				float[] v1 = new float[3];
				System.arraycopy(pg.vertices, 0, v1, 0, 3);
				float[] v2 = new float[3];
				System.arraycopy(pg.vertices, 3, v2, 0, 3);
				float[] v3 = new float[3];
				System.arraycopy(pg.vertices, 6, v3, 0, 3);
				
	            float[] vector1 = new float[3];
	            float[] vector2 = new float[3];
	            for (int m = 0; m <= 2; m++)
	            {
	                vector1[m] = v1[m] - v2[m];
	                vector2[m] = v1[m] - v3[m];
	            }
	            faceNormal[0] = vector1[1] * vector2[2] - vector1[2] * vector2[1];
	            faceNormal[1] = vector1[2] * vector2[0] - vector1[0] * vector2[2];
	            faceNormal[2] = vector1[0] * vector2[1] - vector1[1] * vector2[0];
	            float mag = (float) Math.sqrt(faceNormal[0] * faceNormal[0] + faceNormal[1]
	                                          * faceNormal[1] + faceNormal[2]
	                                          * faceNormal[2]);
	            faceNormal[0] /= mag;
	            faceNormal[1] /= mag;
	            faceNormal[2] /= mag;
				pg.faceNormal = faceNormal;
				
				polygons.add(pg);	
			}
			
			return polygons; 
		}
		
		catch (Exception e)
		{
			e.printStackTrace();
			return polygons;
		}
		
	}
	/**
	 * Parse the position / normal / texture array provided in the dae file. A <geometry> item contains at most one each of each array
	 * with the tag <source>
	 * 
	 * @param geometry
	 * @param posArr
	 * @param normArr
	 * @param uvArr
	 * @param ns
	 */
	
	@SuppressWarnings({ "unchecked" })
	protected void getMeshArrays(Element geometry, 
								//ArrayList<Float> posArr, ArrayList<Float> normArr, ArrayList<Float> uvArr, 
								HashMap<String, Float[]> sourcesArr, 
								HashMap<String,String> vert_ref,
								Namespace ns)
	{
		try
		{
			
			Element mesh = geometry.getChild("mesh", ns);
			
			if (mesh == null)
			{
				throw new Exception("StarLogo currently does not support COLLADA parsing other than 3d meshes");
			}
			
			//get source arrays
			Iterator<Element> sources_itr = mesh.getChildren(ColladaElem.source, ns).iterator();
			
			//adding all the sources into the sources Hashmap
			while(sources_itr.hasNext())
			{
				Element source = sources_itr.next();
				
				String sourceID =  source.getAttributeValue(ColladaAttribute.id);
				
				Float[] fArr = this.parseFloatArray(source.getChild(ColladaElem.float_array, ns), ns);
				
				sourcesArr.put(sourceID, fArr);
			}
			
			//get vertex pointers
			//HashMap<String, String> vertices_ref= new HashMap<String,String>();
			Element vert = mesh.getChild("vertices", ns);
			
			
			Iterator<Element> inputs_itr = vert.getChildren(ColladaElem.input, ns).iterator();
			while(inputs_itr.hasNext())
			{
				Element input = inputs_itr.next();
				//i.e. < "POSITION", sourceID> 
				vert_ref.put(input.getAttributeValue("semantic"), 
							input.getAttributeValue("source").substring(1));
			}
			
		}
		
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
 
	//helper function for finding global map
	@SuppressWarnings("unchecked")
	private void digNode(HashMap<String, HashMap<String, String>> map, Element node, Namespace ns)
	{
		List<Element> nodes = node.getChildren(ColladaElem.node, ns);
		List<Element> geo_instances = node.getChildren("instance_geometry", ns);
		
		if (nodes.size() == 0 && geo_instances.size() == 0)
		{
			return;
		}
		
		else 
		{
			Iterator<Element> nodes_itr = nodes.iterator();
			Iterator<Element> geo_itr = geo_instances.iterator();
			
			
			while(geo_itr.hasNext())
			{
				Element geo_inst = geo_itr.next();
				String geoID = geo_inst.getAttributeValue("url").substring(1);
				//a local map of material reference for this particular geometry
				HashMap<String, String> localMap = new HashMap<String, String>(); 
				Iterator<Element> mat_inst_itr = geo_inst.getChild("bind_material", ns).getChild("technique_common", ns).getChildren("instance_material", ns).iterator();
				
				while (mat_inst_itr.hasNext())
				{   
					Element mat_inst = mat_inst_itr.next();
					String matSymbol = mat_inst.getAttributeValue("symbol");
					String matID = mat_inst.getAttributeValue("target").substring(1);
					localMap.put(matSymbol, matID);
				}	
				map.put(geoID, localMap);
			}
			
			// iterates over children
			while (nodes_itr.hasNext())
			{
				Element n = nodes_itr.next();
				digNode(map, n, ns );
			}

		}
				
	}
	
	/**
	 * get the local to global material name reference
	 * @param lib_nodes
	 * @param ns
	 */
	@SuppressWarnings("unchecked")
	protected void getMaterialLocalGlobalMapping(Element lib_nodes, Namespace ns)
	{

		//HashMap<String, HashMap<String, String>> map = new  HashMap<String, HashMap<String, String>>();
	
	
		//map the material symbol in triangles with the actual instance of the material
		
		Iterator<Element> nodes_itr = lib_nodes.getChildren(ColladaElem.node, ns).iterator();
		
		while (nodes_itr.hasNext())
		{
			Element node = nodes_itr.next();
			
			this.digNode(globalMaterialMap, node, ns);
		}
		
		Iterator<Element> scene_itr = lib_nodes.getChildren("visual_scene", ns).iterator();
		
		while (scene_itr.hasNext())
		{
			Element scene = scene_itr.next();
			
			//this is a layer of models / camera
			//HashMap<String, ColladaNode> nodes = new HashMap<String, ColladaNode>();
			
			Iterator<Element> scene_node_itr = scene.getChildren(ColladaElem.node, ns).iterator();
			
			while(scene_node_itr.hasNext())
			{
				Element sceneNode = scene_node_itr.next();
				
				this.digNode(globalMaterialMap, sceneNode, ns);
			}
		}	
		
		//this.globalMaterialMap = map;
		
	}
	
	/**
	 * Parse the node hiearchy of this Collada Scene, the resulting hierarchy is used to find transformation matrices
	 * @param lib_nodes
	 * @param lib_visual_scene
	 * @param ns
	 * @throws Exception 
	 */
	
	@SuppressWarnings({ "unchecked" })
	protected ColladaNode parseNodeHierarchy(Element lib_nodes, Element lib_visual_scene, Namespace ns) throws Exception
	{
		
		try
		{
			//this.sceneHierarchy = new HashMap<String, HashMap<String, ColladaNode>>();
			//this.geoTransformMap = new HashMap<String, Matrix4f>();
			//this.globalMaterialMap = new  HashMap<String, HashMap<String, String>>();
			
			Iterator<Element> scene_itr = lib_visual_scene.getChildren("visual_scene", ns).iterator();
			ColladaNode n1 = null; 
			
			while (scene_itr.hasNext())
			{
				Element scene = scene_itr.next();
				
				//this is a layer of models / camera
				//HashMap<String, ColladaNode> nodes = new HashMap<String, ColladaNode>();
				
				
				Iterator<Element> node_itr = scene.getChildren(ColladaElem.node, ns).iterator();
				while(node_itr.hasNext())
				{
					Element n1Elem = node_itr.next();
					
					if (n1Elem.getAttributeValue(ColladaAttribute.name).toLowerCase().equals("camera"))
					{
						continue;
					}
					
					else
					{
						//parent node, sometimes doesn't have a name
						String modelID2 = n1Elem.getAttributeValue(ColladaAttribute.id);
						n1 = new ColladaNode( modelID2 == null? n1Elem.getAttributeValue(ColladaAttribute.name) : modelID2);
						Element matrixElem2 = n1Elem.getChild("matrix", ns);
						//n1.local_transform = ( matrixElem == null? Matrix4f.IDENTITY : getMatrix4f(matrixElem, ns));
						
						this.digNodeHierarchy(n1, n1Elem, lib_nodes, ns, Matrix4f.IDENTITY);
						
					}		
				}
			}	
			return n1;
		}
		
		catch (Exception e)
		{
			
			e.printStackTrace();
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	private ColladaNode parseNodeInstance(String nodeID, 
										  Element lib_nodes,
										  Namespace ns, 
										  Matrix4f globalTransform) {
		
		
		Iterator<Element> itr = lib_nodes.getChildren(ColladaElem.node, ns).iterator();
		
		while (itr.hasNext())
		{
			Element nodeElem = itr.next();
			if (nodeElem.getAttributeValue(ColladaAttribute.id).equals(nodeID))
			{
				ColladaNode node = new ColladaNode(nodeElem.getAttributeValue(ColladaAttribute.id));
				digNodeHierarchy(node, nodeElem, lib_nodes, ns, globalTransform);
				return node;
			}	
		}

		return null;
	}

	//helper function to dig through a node
	@SuppressWarnings({ "unchecked" })
	private void digNodeHierarchy(ColladaNode node, Element nodeElem, Element lib_nodes,
			Namespace ns, Matrix4f globalTransform) {
	
		Element matElem = nodeElem.getChild("matrix",ns);
		node.local_transform = (matElem == null? Matrix4f.IDENTITY : getMatrix4f(matElem, ns));
		
		Matrix4f newGlobalTransform = globalTransform.mult(node.local_transform);
		
		
		Iterator<Element> children_inst = nodeElem.getChildren("instance_node", ns).iterator();
		
		//now dig the rest of the nodes recursively 
		node.childrenNodes = new HashMap<String, ColladaNode>();
		
		
		while (children_inst.hasNext())
		{
			Element child_inst = children_inst.next(); 
			String c_id = child_inst.getAttributeValue("url").substring(1);
			ColladaNode child = this.parseNodeInstance(c_id, lib_nodes, ns, newGlobalTransform);
			node.childrenNodes.put(c_id, child);
			return; 	
		}
		
		
		{
		
			Iterator<Element> geo_itr = nodeElem.getChildren("instance_geometry", ns).iterator();
			Iterator<Element> nodes_itr = nodeElem.getChildren(ColladaElem.node, ns).iterator();
			
			if (!geo_itr.hasNext() && !nodes_itr.hasNext())
			{
				return;
			}
			
			while (geo_itr.hasNext())
			{
				//map geometry and its global transformation matrix
				
				Element geo_inst = geo_itr.next();
				String geoID = geo_inst.getAttributeValue("url").substring(1);
				node.geoList.add(geoID);
				
				if (geoTransformMap.containsKey(geoID))
				{
					geoTransformMap.get(geoID).add(newGlobalTransform);
				}
				
				else 
				{
					geoTransformMap.put(geoID, new ArrayList<Matrix4f>());
					geoTransformMap.get(geoID).add(newGlobalTransform);
				}
				
				// map local material reference to global
				
				HashMap<String, String> localMap = new HashMap<String, String>(); 
				
				Iterator<Element> mat_inst_itr = geo_inst.getChild("bind_material", ns).getChild("technique_common", ns).getChildren("instance_material", ns).iterator();
				
				while (mat_inst_itr.hasNext())
				{
					Element mat_inst = mat_inst_itr.next();
					String matSymbol = mat_inst.getAttributeValue("symbol");
					String matID = mat_inst.getAttributeValue("target").substring(1);
					localMap.put(matSymbol, matID);
				}	
				
				this.globalMaterialMap.put(geoID, localMap);	
			}
			
			while (nodes_itr.hasNext())
			{
				Element childElem = nodes_itr.next();
				ColladaNode child = new ColladaNode(childElem.getAttributeValue(ColladaAttribute.id));
				this.digNodeHierarchy(child, childElem, lib_nodes, ns, newGlobalTransform);
				node.childrenNodes.put(child.nodeID, child);
			}
		}
	}

	private Matrix4f getMatrix4f(Element matrixElem, Namespace ns) {
		
		try
		{
			Matrix4f matrix;
			
			StringTokenizer st = new StringTokenizer(matrixElem.getValue());
			float[] values = new float[16];
			int i = 0;
			while (st.hasMoreTokens())
			{
				String tok = st.nextToken();
				if (!(tok.equals("\\s") || tok.equals("\n")))
				{
					values[i] = Float.parseFloat(tok);
					i++;
				}
			}
		
			matrix = new Matrix4f(values).transpose();
			
			return matrix;
		}
		
		catch(Exception e)
		{
			
			e.printStackTrace();
			return null;
		}
	}

	
	/**
	 * Skinning Support
	 * @return boolean, did we find skinning controls 
	 */
	@SuppressWarnings("unchecked")
	public boolean findSkinControl()
	{
		if (this.daeDoc == null)
		{
			return false; 	
		}
			
		else 
		{
			
			try
			{
				Element Collada = daeDoc.getRootElement();
				Namespace ns = Collada.getNamespace();
				
				//get library_geometry
				Element lib_controllers = (Element) Collada.getChild(ColladaElem.lib_controllers, ns);
				
				//KML dae files do not have skinning information, but collada files exported from maya does
				if (lib_controllers == null)
					return false;
				
				else 
				{
					Iterator<Element> controller_it = lib_controllers.getChildren(ColladaElem.controller, ns).iterator();
					while (controller_it.hasNext())
					{
						Element controller = controller_it.next();
					
						Iterator<Element> skins_it = controller.getChildren(ColladaElem.skin, ns).iterator();
						
						
						//each controller must contain one and only one control element, either a skin or a morph. 
						while (skins_it.hasNext())
						{
							Element skin = skins_it.next();
							
							this.parseSkin(skin, ns);
							
						}
						
						//Iterator<Element> morph_it = controller.getChildren(ColladaElem.morph, ns).iterator();					
					}
				}
				
				return true; 
			}
			
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return false; 
	}
	
	/**
	 * Parse through a skin control element
	 * @param skin
	 * @param ns
	 */
	@SuppressWarnings("unchecked")
	private void parseSkin(Element skin, Namespace ns)
	{
		try
		{
			String skinID = skin.getAttributeValue(ColladaAttribute.source).substring(1);
			
			// maps source id to source (arrays where the joint and weights are stored
			HashMap<String, Element> sourcesMap = new HashMap<String, Element>(); 
			
			
			//parse all the sources and put them in the map for later parsing
			Iterator<Element> sources_itr = skin.getChildren(ColladaElem.source, ns).iterator();
			while (sources_itr.hasNext())
			{
				Element source = sources_itr.next();
				
				String sourceID =  source.getAttributeValue(ColladaAttribute.id);

				sourcesMap.put(sourceID, source);
			}
			
			/////////////////////////
			//joints
			Element joints = skin.getChild(ColladaElem.joints, ns);
			
			Iterator<Element> jnts_inputs_itr = joints.getChildren(ColladaElem.input, ns).iterator();
			
			//maps joint name -> initial bind matrix
			HashMap<String, Matrix4f> bindMatrixMap = new HashMap<String, Matrix4f>();
			
			//first put the bind shape matrix in the bindMatrixMap
			Element bind_shape_matrix = skin.getChild(ColladaElem.bind_shape_matrix, ns);
			Matrix4f bind_mat = this.getMatrix4f(bind_shape_matrix, ns);
			bindMatrixMap.put(ColladaElem.bind_shape_matrix, bind_mat);
			
			String[] jointsNameArr = null;
			Matrix4f[] bindPoseMatrices = null; 
			
			while  (jnts_inputs_itr.hasNext())
			{
				Element input = jnts_inputs_itr.next();
				
				String inputSemantic = input.getAttributeValue(ColladaAttribute.semantic);
				String inputSource = input.getAttributeValue(ColladaAttribute.source).substring(1);
				
				
				if (inputSemantic.equals("JOINT"))
				{
					Element jointsSource = sourcesMap.get(inputSource);
					jointsNameArr = (String[]) this.parseSourceElement(jointsSource, ns);
				}
				
				else if (inputSemantic.equals("INV_BIND_MATRIX"))
				{
					Element bindPoseSource = sourcesMap.get(inputSource);
					
					bindPoseMatrices = (Matrix4f[]) this.parseSourceElement(bindPoseSource, ns);
				}
			}
			
			
			if (jointsNameArr.length == bindPoseMatrices.length)
			{
				for (int i = 0; i < jointsNameArr.length; i++)
				{
					bindMatrixMap.put(jointsNameArr[i], bindPoseMatrices[i]);
				}
			}
			
			
			else
			{
				throw new Exception("uneven mapping of joints and bind matrices");
			}
			
			//put this local bindMatrixMap in the global map with skinID as key
			this.skin_bindMatrixMap.put(skinID, bindMatrixMap);
			
			/////////////////////////
			//vertex weights
			Element vertex_weights = skin.getChild(ColladaElem.vertex_weights, ns);
			
			Iterator<Element> wgt_inputs_itr = vertex_weights.getChildren(ColladaElem.input, ns).iterator();
			
			//default offset for reading the joint/weight mapping
			int jointOffset = 0; 
			int weightOffset = 1; 
			Float[] weightsArr = null;
			
			while (wgt_inputs_itr.hasNext())
			{
				Element input = wgt_inputs_itr.next();
				
				String inputSemantic = input.getAttributeValue(ColladaAttribute.semantic);
				String inputSource = input.getAttributeValue(ColladaAttribute.source).substring(1);
			
				
				if (inputSemantic.equals("JOINT"))
				{
					//Element jointsSource = sourcesMap.get(inputSource);
					jointOffset = Integer.parseInt(input.getAttributeValue(ColladaAttribute.offset));
				}
				
				else if (inputSemantic.equals("WEIGHT"))
				{
					Element weightSource = sourcesMap.get(inputSource);
					weightOffset = Integer.parseInt(input.getAttributeValue(ColladaAttribute.offset));
					
					weightsArr = (Float[]) this.parseSourceElement(weightSource, ns);
				}
			}
			
			Integer[] vcount = this.parseIntArray(vertex_weights.getChild(ColladaElem.vcount, ns), ns);
			Integer[] v = this.parseIntArray(vertex_weights.getChild(ColladaElem.v, ns), ns);
			
			int vertexCount = Integer.parseInt(vertex_weights.getAttributeValue(ColladaAttribute.count));
			/////////////////////////////////
			/////////////////////////////////
			ArrayList<HashMap<String, Float>> vertexWeightsMap = new ArrayList<HashMap<String, Float>>(vertexCount); 
			/////////////////////////////////                       
			
			if (vcount.length != vertexCount)
			{
				throw new Exception ("vertex count does not match");
			}
			
			else {
			
				int c = 0;
				int stride = Math.max(jointOffset, weightOffset) + 1;
				
				for (int i = 0; i < vertexCount; i++)
				{
					//jntcount describes how many joints are associated with the ith vertex 
					int jntCount = vcount[i];
					HashMap<String, Float> jointWeightMap = new HashMap<String, Float>(); 
					//get joint->weight mapping for the ith vertex
					for (int j = c; j < jntCount; j++)
					{
						//weight is associated with the bind matrix if index is -1
						if (v[j*stride + jointOffset] == -1)
						{
							jointWeightMap.put(ColladaElem.bind_shape_matrix, weightsArr[v[j*stride+weightOffset]]);
						}
						else
						{
							jointWeightMap.put(jointsNameArr[v[j*stride+jointOffset]], weightsArr[v[j*stride+weightOffset]]);
						}
					}
					
				    c+= jntCount * stride; 
				    vertexWeightsMap.set(i, jointWeightMap);
				}
			}
			
			this.skin_vertexWeightMap.put(skinID, vertexWeightsMap);
		}
		
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * This method parses the <source> data object, depending on the parameter type, will return different object arrays
	 * @param Source
	 * @param ns
	 * @return String[] if data array is Name_array
	 * @return Float[] if data array is a float_array
	 * @return Matrix4f[] if data array is a float_array of 4x4 matrices
	 */
	
	private Object[] parseSourceElement(Element Source, Namespace ns)
	{
		try
		{
			Element sourceArr = (Element) Source.getChildren().get(0);
			Element accessor = Source.getChild(ColladaElem.technique_common, ns).getChild(ColladaElem.accessor);
			
			String arrType = sourceArr.getName();
			
			if (arrType.equals(ColladaElem.name_array))
			{
				return this.parseNameArray(sourceArr, ns);
			}
			
			else if (arrType.equals(ColladaElem.float_array))
			{
				List<Element> params =  accessor.getChildren(ColladaElem.param, ns);
				
				if (params.size() == 1)
				{
					String paramType = params.get(0).getAttributeValue(ColladaAttribute.type);
					
					if (paramType.equals("float4x4"))
					{
						return this.parseMat4fArray(sourceArr, ns);
					}
					
					else 
					{
						return this.parseFloatArray(sourceArr, ns); 
					}
				}
				
				else if (params.size() > 1)
				{
					
					return this.parseFloatArray(sourceArr, ns, params.size());
				}
				//return an array of matrices
				 
				else
				{
					throw new Exception ("cannot handle this type of parameter");
				}
			}
			
			else
			{
				
				throw new Exception("cannot handle this type of source array: " + arrType);
			}
		}
		
		catch (Exception e)
		{
			e.printStackTrace();
			return null; 
		}
		
	}
	
	/** 
	 * @param floatSource
	 * @param ns
	 */
	private Float[] parseFloatArray(Element floatArray, Namespace ns)
	{
		try
		{
			
			String[] strArr = floatArray.getValue().split(" ");
			
			int count = strArr.length;
			Float[] floatArr = new Float[count];
			
			for (int j = 0; j < strArr.length; j++ )
			{
				floatArr[j] = Float.parseFloat(strArr[j]);
			}

			return floatArr;
		}
		
		catch (Exception e)
		{
			e.printStackTrace();
			return null; 
		}
	}
	
	/**
	 * 
	 * @param floatArray
	 * @param ns
	 * @param stride
	 * @return a double float array of array[][stride], 
	 * because a float array can be arranged in X,Y,X,Y, 
	 * or X,Y,Z,X,Y,Z, or other patterns
	 */
	private Float[][] parseFloatArray(Element floatArray, Namespace ns,  int stride)
	{
		try
		{
			
			String[] strArr = floatArray.getValue().split(" ");
			
			int count = strArr.length / stride;
			Float[][] floatArr = new Float[count][stride];
			
			for (int i = 0; i < count; i++ )
			{
				for (int j = 0; j < stride ; j++)
				{
					floatArr[i][j] = Float.parseFloat (strArr[i*stride + j ]);
				}
			}

			return floatArr;
		}
		
		catch (Exception e)
		{
			e.printStackTrace();
			return null; 
		}
	}	
	
	/**
	 * @param intArray
	 * @param ns
	 * @return Integer[]
	 */
	private Integer[] parseIntArray(Element intArray, Namespace ns)
	{
		try
		{
			
			String[] strArr = intArray.getValue().split(" ");
			
			int count = strArr.length;
			Integer[] intArr = new Integer[count];
			
			for (int j = 0; j < strArr.length; j++ )
			{
				intArr[j] = Integer.parseInt(strArr[j]);
			}

			return intArr;
		}
		
		catch (Exception e)
		{
			e.printStackTrace();
			return null; 
		}
		 
	}
	/**
	 * Returns a string array of the String names
	 * @param nameArray
	 * @param ns
	 * @return String[] 
	 */
	private String[] parseNameArray(Element nameArray, Namespace ns)
	{
		
		try
		{
			String[] strArr = nameArray.getValue().split(" ");
			
			return strArr; 
		}
		
		catch (Exception e)
		{
			e.printStackTrace();
			return null; 
		} 
	}
	
	/**
	 * Return an array of matrix4fs
	 * @param matArr
	 * @param ns
	 * @return Matrix4f[]
	 */
	private Matrix4f[] parseMat4fArray(Element matArr, Namespace ns) {
		
		
		try
		{
			int floatCount = Integer.parseInt(matArr.getAttributeValue(ColladaAttribute.count));
			
			Matrix4f[] mats = new Matrix4f[floatCount/16];
			
			
			StringTokenizer st = new StringTokenizer(matArr.getValue());
			float[] values = new float[floatCount];
			
			int i = 0;
			while (st.hasMoreTokens())
			{
				String tok = st.nextToken();
				if (!(tok.equals("\\s") || tok.equals("\n")))
				{
					values[i] = Float.parseFloat(tok);
					i++;
				}
			}
			
			for (int j = 0; j < floatCount/16 ; j++)
			{
				mats[j] = new Matrix4f(	values[j],values[j+1],values[j+2],values[j+3],
                    	values[j+4],values[j+5],values[j+6],values[j+7],
                        values[j+8],values[j+9],values[j+10],values[j+11],
                        values[j+12],values[j+13],values[j+14],values[j+15]);

			}
			
			return mats; 
		}
		
		catch (Exception e)
		{
			e.printStackTrace();
			return null; 
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public ArrayList<ColladaAnimation> findAnimations()
	{
		if (this.daeDoc == null)
		{
			return null; 	
		}
			
		else 
		{
			
			try
			{
				Element Collada = daeDoc.getRootElement();
				Namespace ns = Collada.getNamespace();
				
				//get library_geometry
				Element lib_animations = (Element) Collada.getChild(ColladaElem.lib_animations, ns);
				
				Iterator<Element> animations_itr = lib_animations.getChildren(ColladaElem.animation, ns).iterator();
				
				ArrayList<ColladaAnimation> animations = new ArrayList<ColladaAnimation>();
				
				while (animations_itr.hasNext())
				{
					Element animation = animations_itr.next();
					
					animations.add(this.parseAnimation(animation, ns));
				}
				
				return animations; 
		
			}
			
			catch(Exception e)
			{
				e.printStackTrace();
				return null;
			}
	
		}
	}
	
	private ColladaAnimation parseAnimation(Element animation, Namespace ns) {
		
		///////////////////////
		//Initialize animation element
		ColladaAnimation anim = new ColladaAnimation(animation.getAttributeValue(ColladaAttribute.id));
		
		HashMap<String, Element> sourceMap = new HashMap<String, Element>();
		Iterator<Element> sources_itr = animation.getChildren(ColladaElem.source, ns).iterator();
		
		while (sources_itr.hasNext())
		{
			Element source = sources_itr.next(); 
			sourceMap.put(source.getAttributeValue(ColladaAttribute.id), source);
		}
		
		Element sampler = animation.getChild(ColladaElem.sampler, ns);
		Iterator<Element> inputs_itr = sampler.getChildren(ColladaElem.input, ns).iterator();
		
		while (inputs_itr.hasNext())
		{
			Element input = inputs_itr.next();
			String semantic = input.getAttributeValue(ColladaAttribute.semantic);
			Element source = sourceMap.get(input.getAttributeValue(ColladaAttribute.source).substring(0));
			
			if (semantic.equals("INPUT"))
			{
				anim.keyFrames = this.parseFloatArray(source.getChild(ColladaElem.float_array), ns);
				anim.numFrames = anim.keyFrames.length;
			}
			
			else if (semantic.equals("OUTPUT"))
			{
				//anim.transformation = this.parseSourceElement(source, ns);
				
				Element accessor = source.getChild(ColladaElem.technique_common, ns).getChild(ColladaElem.accessor);
				List<Element> params =  accessor.getChildren(ColladaElem.param, ns);
				
				Element sourceArr = source.getChild(ColladaElem.float_array, ns);
				
				
				if (params.size() == 1)
				{
					String paramName = params.get(0).getAttributeValue(ColladaAttribute.name);
					String paramType = params.get(0).getAttributeValue(ColladaAttribute.type);
					
					if (paramName.equals("TRANSFORM") && paramType.equals("float4x4"))
					{
						anim.transformation = this.parseMat4fArray(sourceArr, ns);
					}
						
					else if (paramName.equals("ANGLE") && paramType.equals("float")) 
					{
						anim.rotation = this.parseFloatArray(sourceArr, ns); 
						
					}
				}
								
				else 
				{	
					int stride = Integer.parseInt(accessor.getAttributeValue(ColladaAttribute.stride));
					anim.translation = this.parseFloatArray(sourceArr, ns, stride);
				}
					//return an array of matrices
			}
			
			else if (semantic.equals("INTERPOLATION"))
			{
				anim.interpolationMethods = (String[]) this.parseSourceElement(source, ns);
			}
		}
		
		
		//////////////////////
		//recursive iterations to dig for children animations
		Iterator<Element> childrenAnimations_itr = animation.getChildren(ColladaElem.animation, ns).iterator();
		
		while (childrenAnimations_itr.hasNext())
		{
			Element childrenAnimation = childrenAnimations_itr.next();
			
			anim.childrenAnimations.add(this.parseAnimation(childrenAnimation, ns));
		}
		
		////////////////////////
		
		return anim;
		
	}
	
	public double getScaleFactor() {
		return scale*3.0;
	}
	
	public String rotationString() {
		String up = up_axis.getText();
		if(up.equals("X_UP"))
			return "";
		else if(up.equals("Y_UP"))
			return "rotate-x 90";
		else if(up.equals("Z_UP"))
			return "rotate-z 270\n rotate-y 180";
		else
			return "";
	}
	
	/**
	 * Inner class COLLADALibrary use the dae document to construct the texture library
	 * @author yansuiw
	 *
	 */
	
	public class COLLADALibrary {
		
		private Document daeDoc;
		private HashMap<String, ColladaMaterial> materials = new HashMap<String, ColladaMaterial>();
	    private String daePath;
	    
	    public COLLADALibrary(Document daeDoc, String daePath)
	    {
	    	this.daeDoc = daeDoc;
	    	this.daePath = daePath;
	    }
	    
	    /**
	     * 
	     * @return true if successful
	     */
	    
	    @SuppressWarnings("unchecked")
		public boolean getTextureLib(){
	    	
	    	if (this.daeDoc == null)
			{
				return false;	
			}
			
	    	
			else 
			{
				try 
				{
					Element Collada = daeDoc.getRootElement();
					Namespace ns = Collada.getNamespace();
					
					//if there's no library_images node, no texture images, therefore cannot create texture lib
					//if (Collada.getChild("library_images", ns) == null)
					//{
					//	return false; 
					//}
					
					
					Element lib_images = (Element) Collada.getChild("library_images", ns);
					Element lib_materials = (Element) Collada.getChild("library_materials", ns);
					Element lib_effects = (Element) Collada.getChild("library_effects", ns);
					
					
					/*
					 * Map_ImgURL maps imageName to image URL
					 * Map_EffMat maps effectName to materialName
					 * Map_MatIDSymbol maps the unique ID of a material to the local symbol of the material (within local instances of geometry)
					 */
					HashMap<String, String> Map_ImgURL = new HashMap<String, String>();
					HashMap<String, String> Map_EffMat = new HashMap<String,String>();
					// map the list of images to specific URLs
					if (lib_images != null)
					{
						Iterator<Element> images_itr = lib_images.getChildren("image", ns).iterator();
						
						while (images_itr.hasNext())
						{
							Element img = images_itr.next();
							String imageID = img.getAttributeValue(ColladaAttribute.id);
							String imageURL = img.getChild("init_from", ns).getValue();
							Map_ImgURL.put(imageID, imageURL);
						}
					}
										
					//map material names to their specific effects
					Iterator<Element> mats_itr = lib_materials.getChildren("material", ns).iterator();
					while (mats_itr.hasNext())
					{
						Element mat = mats_itr.next();
						String matID = mat.getAttributeValue(ColladaAttribute.id);
						//String matName = mats.get(i).getAttributeValue(ColladaAttribute.name);
						String effect = mat.getChild("instance_effect", ns).getAttributeValue("url");
						effect = effect.substring(1);
						Map_EffMat.put(effect, matID);
					}
					
					
					//parse through the list of effects to create COLLADAmaterials, then use the name mapping from 
					//Map_EffMat to to find the name of material. 
					
					Iterator<Element> effects_itr = lib_effects.getChildren().iterator();
					while (effects_itr.hasNext())
					{
						Element eff = effects_itr.next();
						
						String matName = Map_EffMat.get(eff.getAttributeValue(ColladaAttribute.id));
						ColladaMaterial cm = new ColladaMaterial(matName);
						cm.parseColladaMaterial(eff, ns, Map_ImgURL);
						materials.put(matName, cm);
						
					}
					
					return true;
				}
				
				catch(Exception e)
				{
					e.printStackTrace();
					return false; 
				}
			}
	    }
	    
	    
	    public void loadTextures()
	    {
	    	File daeFile = new File(this.daePath);
	    	String texPath = daeFile.getParent() + "/";
	    	
	        for (ColladaMaterial mat : materials.values())
	            mat.loadTextures(texPath);
	    }
	    
	    public void unloadTextures()
	    {
	        for (ColladaMaterial mat : materials.values())
	            mat.unloadTextures();
	    }
	    
	    
	    /**
	     * Applies a material to the given GL context
	     */
	    public void applyMaterial(String name, GL gl)
	    {
	    	ColladaMaterial mat = materials.get(name);
	        if (mat != null)
	            mat.apply(gl);
	        else
	            gl.glDisable(GL.GL_TEXTURE_2D);
	    }
	    
	    /**
	     * @return the material in the library with the given name (null if it doesn't exist)
	     */
	    public ColladaMaterial getMaterial(String name)
	    {
	        return materials.get(name);
	    }
				
	}

	/**
	 * Attempts to find the center of the model and shift all polygons
	 * in order to make the model centered without needing translate transforms
	 * in the model.slb file
	 */
	public void prepModel() {
		
		// try to find the center of the model
		float xmin = 1e20f, xmax = -1e20f, ymin = 1e20f, ymax = -1e20f, zmin = 1e20f, zmax = -1e20f;
		for (Polygon3D poly : this.polygonMesh) {
			for (int i=0; i < 3; i++) {
				if (poly.vertices[i*3] < xmin) {
					xmin = poly.vertices[i*3];
				}
				if (poly.vertices[i*3] > xmax) {
					xmax = poly.vertices[i*3];
				}
				if (poly.vertices[i*3+1] < ymin) {
					ymin = poly.vertices[i*3+1];
				}
				if (poly.vertices[i*3+1] > ymax) {
					ymax = poly.vertices[i*3+1];
				}
			}
		}
		
		// shift all traingles to be "centered"
		float SCALED_SIZE = 5;
		float maxSpan = Math.max((xmax - xmin), (ymax - ymin));
		float scaleFactor = (float) (SCALED_SIZE / maxSpan);
		float centerx = (xmin + xmax) / 2f, centery = (ymin + ymax) / 2f; 
		for (Polygon3D poly : this.polygonMesh) {
			for (int i=0; i < 3; i++) {
				poly.vertices[i*3] = poly.vertices[i*3] - centerx;
				poly.vertices[i*3+1] = poly.vertices[i*3+1] - centery;
			}
		}	
		
		// scales the model based on a maximum distance in the xy plane
		for (Polygon3D poly : this.polygonMesh) {
			for (int i=0; i < 3; i++) {
				poly.vertices[i*3] = poly.vertices[i*3] * scaleFactor;
				poly.vertices[i*3+1] = poly.vertices[i*3+1] * scaleFactor;
				poly.vertices[i*3+2] = poly.vertices[i*3+2] * scaleFactor;
			}
		}
	}

	
}
