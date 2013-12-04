package torusworld.math;

/** @modelguid {DE0A1FE3-F312-48DD-8626-9E8827C31861} */
public final class Plane {
    /**
     * NO_SIDE represents the plane itself.
     * @modelguid {76053C28-4DAB-44E7-BB8A-B0265F40C5DE}
     */
    public static final int NO_SIDE = 0;
    /**
     * POSITIVE_SIDE represents a point on the side the normal points.
     * @modelguid {13E1645F-7E63-4FA5-868D-F84826AEDC7C}
     */
    public static final int POSITIVE_SIDE = 1;
    /**
     * NEGATIVE_SIDE represents a point on the opposite side the normal points.
     * @modelguid {E3FE6D42-C32B-45AC-B717-799ABBC69F88}
     */
    public static final int NEGATIVE_SIDE = 2;

    //attributes of the plane.
    /** @modelguid {83232E2C-C3BD-4396-99A2-DC8F8215D7A6} */
    private Vector3f normal;
    /** @modelguid {3EDFABEE-8DD9-45FF-9BA5-BB17ADC540D7} */
    private float constant;
    

    /** @modelguid {C4B5F862-45EF-449A-9A68-226B346AB7C2} */
    public Plane() {
        normal = new Vector3f();
    }
    
    /** @modelguid {DEB6D557-586E-412D-8A22-EB10B17E6E1C} */
    public Plane(Vector3f normal, float constant) {
        if(normal == null) {
            System.out.println(" created default normal.");
            normal = new Vector3f();
        }
        this.normal = normal;
        this.constant = constant;
    }
    

    /** @modelguid {3EEA1DA2-D142-4A0E-BA70-474E7A72D2A3} */
    public void setNormal(Vector3f normal) {
        if(normal == null) {
            System.out.println(" created default normal.");
            normal = new Vector3f();
        }
        this.normal = normal;
    }
    
    /** @modelguid {DA6F232D-D783-45A0-8774-72C9C06EC465} */
    public Vector3f getNormal() {
        return normal;
    }
    

    /** @modelguid {03DBBDF0-7DEB-4791-86B4-11E0FFB77544} */
    public void setConstant(float constant) {
        this.constant = constant;
    }
    

    /** @modelguid {6CD5E1EB-FF4A-45D9-AC16-D168064311C7} */
    public float getConstant() {
        return constant;
    }
    

    /** @modelguid {7FDB36B1-3586-4DDA-8335-E24AB6B640C5} */
    public float pseudoDistance(Vector3f point) {
        return Vector3f.dot(normal, point) - constant;
    }
    

    /** @modelguid {9F8CE3E6-9158-4DAA-A30B-2EAE184A4151} */
    public int whichSide(Vector3f point) {
        float dis = pseudoDistance(point);
        if(dis < 0) {
            return NEGATIVE_SIDE;
        } else if (dis > 0) {
            return POSITIVE_SIDE;
        } else {
            return NO_SIDE;
        }
    }
    

    /** @modelguid {B158F9AD-D358-4353-ABB3-07DA6CA832C4} */
    public String toString() {
        return "torusworld.math.Plane [Normal: " + normal + " - Constant: " 
                + constant + "]";
    }
}
