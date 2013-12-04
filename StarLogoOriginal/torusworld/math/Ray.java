package torusworld.math;

/** @modelguid {31599FF7-6338-41A1-AB7B-1C98E93BCBBD} */
public final class Ray {
    /** @modelguid {1696F503-1696-48BB-9416-4D873F3EB7D3} */
    private Vector3f origin;
    /** @modelguid {297F0691-8B98-41BF-B296-52CD1DC0EF00} */
    private Vector3f direction;
    
    /** @modelguid {A6687790-EFE5-4582-9817-268329504C0B} */
    public Ray() {
        origin = new Vector3f();
        direction = new Vector3f();
    }
    
    /** @modelguid {636165FC-A803-4BA9-98F4-93811692EFC1} */
    public Ray(Vector3f origin, Vector3f direction) {
        this.origin = origin;
        this.direction = direction;
    }

    /** @modelguid {CF551447-CD1B-45EE-91E6-3111B365DAFC} */
    public Vector3f getOrigin() {
        return origin;
    }
    
    /** @modelguid {2C40B6A6-8267-4885-98DA-36999F1F9F25} */
    public void setOrigin(Vector3f origin) {
        this.origin = origin;
    }
    
    /** @modelguid {D0F1C420-8790-447C-A4D5-C121A2D026A9} */
    public Vector3f getDirection() {
        return direction;
    }
    
    /** @modelguid {29A278F7-0B33-4129-967E-535BFFF6EE81} */
    public void setDirection(Vector3f direction) {
        this.direction = direction;
    }
}
