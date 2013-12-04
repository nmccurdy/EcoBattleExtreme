package torusworld.model.md3;

import torusworld.math.CompactNormal;
import torusworld.math.Cylinder;
import torusworld.math.Vector3f;
import torusworld.math.Matrix4f;
import torusworld.Texture;
import java.nio.ByteBuffer;
import javax.media.opengl.GL;

class Md3Surface
{
    public Md3SurfaceHeader header;

    private Md3Skin[] Skins; // numSkins
    private int[] Triangles; // 3 * numTriangles
    private float[] TexCoords; // 2 * numVertices
    private short[] Vertices; // 4 * numVertices * numFrames

    private static final float MD3_SCALE = 1.f / 64.f;

    public Texture texture = null;
    
    public Md3Surface(ByteBuffer buffer)
    {
        readFromBuffer(buffer);
    }
    
    void readFromBuffer(ByteBuffer buffer)
    {
        int i, pos = buffer.position();
        header = new Md3SurfaceHeader(buffer);

        buffer.position(pos + header.ofsSkins);
        Skins = new Md3Skin[header.numSkins];
        for (i = 0; i < header.numSkins; i++)
            Skins[i] = new Md3Skin(buffer);

        buffer.position(pos + header.ofsTriangles);
        Triangles = new int[3 * header.numTriangles];
        buffer.asIntBuffer().get(Triangles);

        buffer.position(pos + header.ofsTexCoords);
        TexCoords = new float[2 * header.numVertices];
        buffer.asFloatBuffer().get(TexCoords);

        buffer.position(pos + header.ofsVertices);
        Vertices = new short[4 * header.numVertices * header.numFrames];
        buffer.asShortBuffer().get(Vertices);

        buffer.position(pos + header.ofsEnd);
    }
    
    void unload()
    {
        header = null;
        Triangles = null;
        TexCoords = null;
        Vertices = null;
    }
    
    /* render a frame of the surface (in immediate mode) */
    void renderFrame(GL gl, int frame1, int frame2, float t)
    {
        int i, j, v, v1, v2;
        if (texture == null) return; /* nodraw */
        texture.bind();
        
        gl.glBegin(GL.GL_TRIANGLES);
        for (i = 0; i < header.numTriangles; i++)
            for (j = 0; j < 3; j++)
            {
                v = Triangles[3 * i + j];
                gl.glTexCoord2f(TexCoords[2 * v], TexCoords[2 * v + 1]);
                
                v1 = v + header.numVertices * frame1;
                v2 = v + header.numVertices * frame2;
                
                gl.glNormal3f(
                     (1-t) * CompactNormal.DecodeNormal(Vertices[4 * v1 + 3], 0) +
                         t * CompactNormal.DecodeNormal(Vertices[4 * v2 + 3], 0),
                     (1-t) * CompactNormal.DecodeNormal(Vertices[4 * v1 + 3], 1) +
                         t * CompactNormal.DecodeNormal(Vertices[4 * v2 + 3], 1),
                     (1-t) * CompactNormal.DecodeNormal(Vertices[4 * v1 + 3], 2) +
                         t * CompactNormal.DecodeNormal(Vertices[4 * v2 + 3], 2));
                
                gl.glVertex3f(MD3_SCALE * ((1.f - t) * Vertices[4 * v1] + t * Vertices[4 * v2]),
                              MD3_SCALE * ((1.f - t) * Vertices[4 * v1 + 1] + t * Vertices[4 * v2 + 1]),
                              MD3_SCALE * ((1.f - t) * Vertices[4 * v1 + 2] + t * Vertices[4 * v2 + 2]));
            }
        gl.glEnd();
    }
    
    public Cylinder computeBoundingCylinderForFrame(Matrix4f transformation, int frame)
    {
        float radius = 0, top = -1e10f, bottom = 1e10f;
        Vector3f point = new Vector3f();
        int v;

        if (texture == null) return new Cylinder(radius, top, bottom); /* nodraw */
        
        for (int i = 0; i < header.numTriangles; i++)
            for (int k = 0; k < 3; k++)
            {
                v = Triangles[3 * i + k] + header.numVertices * frame;
                point.x = MD3_SCALE * Vertices[4 * v];
                point.y = MD3_SCALE * Vertices[4 * v + 1];
                point.z = MD3_SCALE * Vertices[4 * v + 2];
                
                transformation.transform(point);
                
                top = Math.max(top, point.y);
                bottom = Math.min(bottom, point.y);
                point.y = 0;

                radius = Math.max(radius, point.length());
            }

        return new Cylinder(radius, top, bottom);
    }
    
    public void flipTexCoords()
    {
        int i;
        for (i = 0; i < header.numVertices; i++)
            TexCoords[2*i + 1] = 1.f - TexCoords[2*i + 1];
    }
}
