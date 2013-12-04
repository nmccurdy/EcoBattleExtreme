package torusworld.math;

/** @modelguid {DD5E74E3-ED86-4D36-B91A-3B981E2CDA46} */
public class Triangle {
    /** @modelguid {D259DB2B-4F72-46FC-BED0-62D6874C9B28} */
    private Vector3f[] points;
    
    /** @modelguid {EF605F13-CEAB-48F3-92EC-D93FD303E4FC} */
    public Triangle(Vector3f p1, Vector3f p2, Vector3f p3) {
        points = new Vector3f[3];
        points[0] = p1;
        points[1] = p2;
        points[2] = p3;
    }
    
    /** @modelguid {EA363D9E-F649-4E23-9947-C894B26E24C2} */
    public Vector3f get(int i) {
        return points[i];
    }

    /** @modelguid {875870F1-D55B-4AB8-9A0D-0E167ADA322B} */
    public void set(int i, Vector3f point) {
        points[i] = point;
    }
}
