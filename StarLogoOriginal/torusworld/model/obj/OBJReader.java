package torusworld.model.obj;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.*;


class OBJReader
{
    private ArrayList<Polygon3D> polygons = new ArrayList<Polygon3D>();

    public OBJReader()
    {
    }

    //Not recommended
    public OBJReader(Reader r)
    {
        assert read(r);
    }

    private String curMaterial = "";


    // Returns true if read successful
    public boolean read(Reader r)
    {
        try
        {
            LineNumberReader lnr = new LineNumberReader(r);

            ArrayList<float[]> vList = new ArrayList<float[]>();
            ArrayList<float[]> vtList = new ArrayList<float[]>();
            ArrayList<float[]> vnList = new ArrayList<float[]>();
            ArrayList<int[]> faceVerts = new ArrayList<int[]>();
            String line;
            boolean computeNormals = false;
            for (line = lnr.readLine(); line != null; line = lnr.readLine())
            {
                String[] tokens = line.split(" +");
                Iterator<String> it = Arrays.asList(tokens).iterator();
                
                String word;
                if (it.hasNext())
                    word = it.next();
                else
                    word = "";
                if (word.equalsIgnoreCase("#StarLogoCommand"))
                {
                    if (it.hasNext() && (it.next()).equalsIgnoreCase("ComputeNormals"))
                        computeNormals = true;
                } else if (word.equals("v"))
                {
                    float v[] = new float[3];
                    if (!it.hasNext())
                        throw new IOException("Could not find three vertices after \"v\" token");
                    v[0] = Float.parseFloat(it.next());
                    if (!it.hasNext())
                        throw new IOException("Could not find three vertices after \"v\" token");
                    v[1] = Float.parseFloat(it.next());
                    if (!it.hasNext())
                        throw new IOException("Could not find three vertices after \"v\" token");
                    v[2] = Float.parseFloat(it.next());
                    vList.add(v);
                } else if (word.equals("vt"))
                {
                    float v[] = new float[2];
                    if (!it.hasNext())
                        throw new IOException(
                                              "Could not find two texture coordinates after \"vt\" token");
                    v[0] = Float.parseFloat(it.next());
                    if (!it.hasNext())
                        throw new IOException(
                                              "Could not find two texture coordinates after \"vt\" token");
                    v[1] = Float.parseFloat(it.next());
                    vtList.add(v);
                } else if (word.equals("vn"))
                {
                    float v[] = new float[3];
                    if (!it.hasNext())
                        throw new IOException(
                                              "Could not find three normal numbers after \"vn\" token");
                    v[0] = Float.parseFloat(it.next());
                    if (!it.hasNext())
                        throw new IOException(
                                              "Could not find three normal numbers after \"vn\" token");
                    v[1] = Float.parseFloat(it.next());
                    if (!it.hasNext())
                        throw new IOException(
                                              "Could not find three normal numbers after \"vn\" token");
                    v[2] = Float.parseFloat(it.next());
                    vnList.add(v);
                } else if (word.equals("usemtl"))
                    curMaterial = it.next();
                else if (word.equals("f"))
                {
                    faceVerts.clear();
                    // each face vertex could look like 1, 1/1, 1/1/1, or 1//1
                    int a;
                    for (a = 0; it.hasNext(); a++)
                    {
                        int v[] = new int[3]; //2 vertex, texture coord, normal
                        word = it.next();
                        String[] values = word.split("/");
                        int i;
                        for (i = 0; i < values.length && i < 3; i++) {
                        	if (!values[i].equals("")) {
                        		v[i] = Integer.parseInt(values[i]);
                        	} else {
                        		v[i] = 0;
                        	}
                        }
                        if (i < values.length)
                            throw new IOException(
                                                  "Found more then 3 values representing a face vertex");
                        faceVerts.add(v);
                    }
                    if (a < 3)
                        throw new IOException("Could not find three vertices after \"f\" token");
                    for (int i = 0; i < faceVerts.size(); i++)
                    {
                        int v[] = faceVerts.get(i);
                        if (v[0] > 0)
                            v[0]--;
                        else if (v[0] < 0)
                            v[0] += vList.size();
                        else
                            v[0] = -1;
                        if (v[1] > 0)
                            v[1]--;
                        else if (v[1] < 0)
                            v[1] += vtList.size();
                        else
                            v[1] = -1;
                        if (v[2] > 0)
                            v[2]--;
                        else if (v[2] < 0)
                            v[2] += vnList.size();
                        else
                            v[2] = -1;
                    }

                    float[] faceNormal = null;
                    if (computeNormals)
                    {
                        faceNormal = new float[3];
                        float[] v1 = vList.get(faceVerts.get(0)[0]);
                        float[] v2 = vList.get(faceVerts.get(1)[0]);
                        float[] v3 = vList.get(faceVerts.get(2)[0]);
                        float[] vector1 = new float[3];
                        float[] vector2 = new float[3];
                        for (int i = 0; i <= 2; i++)
                        {
                            vector1[i] = v1[i] - v2[i];
                            vector2[i] = v1[i] - v3[i];
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
                    }
                    for (int i = 0; i < faceVerts.size() - 2; i++)
                    {
                        Polygon3D p = new Polygon3D();
                        p.materialName = curMaterial;
                        System.arraycopy(vList.get(faceVerts.get(0)[0]), 0, p.vertices, 0, 3);
                        System.arraycopy(vList.get(faceVerts.get(i + 1)[0]), 0, p.vertices, 3, 3);
                        System.arraycopy(vList.get(faceVerts.get(i + 2)[0]), 0, p.vertices, 6, 3);
                        if (((int[]) faceVerts.get(0))[1] >= 0)
                        {
                            p.texture = new float[6];
                            System.arraycopy(vtList.get(faceVerts.get(0)[1]), 0, p.texture, 0, 2);
                            System.arraycopy(vtList.get(faceVerts.get(i + 1)[1]), 0, p.texture, 2,
                                             2);
                            System.arraycopy(vtList.get(faceVerts.get(i + 2)[1]), 0, p.texture, 4,
                                             2);
                        }
                        if (((int[]) faceVerts.get(0))[2] >= 0)
                        {
                            p.normal = new float[9];
                            System.arraycopy(vnList.get(faceVerts.get(0)[2]), 0, p.normal, 0, 3);
                            System
                                .arraycopy(vnList.get(faceVerts.get(i + 1)[2]), 0, p.normal, 3, 3);
                            System
                                .arraycopy(vnList.get(faceVerts.get(i + 2)[2]), 0, p.normal, 6, 3);
                        }
                        p.faceNormal = faceNormal;
                        polygons.add(p);
                    }
                }
            }
        } catch (NumberFormatException e)
        {
            System.out.println("Error reading OBJ file: Expected number could not be found");
            e.printStackTrace();
            for (int i = 1; i < 1000000000; i++)
                ;
            return false;
        } catch (IOException e)
        {
            System.err.println("Error reading OBJ file: " + e.getMessage());
            e.printStackTrace();
            for (int i = 1; i < 1000000000; i++)
                ;
            return false;
        }
        return true;
    }

    public void dump()
    {
        System.out.println(polygons.size() + " Polygons");
        for (int i = 0; i < polygons.size(); i++)
        {
            Polygon3D p = polygons.get(i);
            System.out.println("#" + i + ", Material " + p.materialName + " = "
                               + p.materialName);
            System.out.println("Vertices:");
            System.out.println("   (" + p.vertices[0] + "," + p.vertices[1] + "," + p.vertices[2]
                               + ")");
            System.out.println("   (" + p.vertices[3] + "," + p.vertices[4] + "," + p.vertices[5]
                               + ")");
            System.out.println("   (" + p.vertices[6] + "," + p.vertices[7] + "," + p.vertices[8]
                               + ")");
            System.out.println("Texture Coords:");
            if (p.texture == null)
                System.out.println("(none)");
            else
            {
                System.out.println("   (" + p.texture[0] + "," + p.texture[1] + ")");
                System.out.println("   (" + p.texture[2] + "," + p.texture[3] + ")");
                System.out.println("   (" + p.texture[4] + "," + p.texture[5] + ")");
            }
            System.out.println("Normal Vectors:");
            if (p.normal == null)
                System.out.println("(none)");
            else
            {
                System.out.println("   (" + p.normal[0] + "," + p.normal[1] + "," + p.normal[2]
                                   + ")");
                System.out.println("   (" + p.normal[3] + "," + p.normal[4] + "," + p.normal[5]
                                   + ")");
                System.out.println("   (" + p.normal[6] + "," + p.normal[7] + "," + p.normal[8]
                                   + ")");
            }
        }
    }

    public ArrayList<Polygon3D> getPolygons()
    {
        return polygons;
    }
}
