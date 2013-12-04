
package torusworld.math;

/** @modelguid {31DBBD77-4AD4-44B6-960D-FE39F2BABE99} */
public class Line {
    /** @modelguid {1BC8BA87-0E1B-48AB-802C-A262BE62C52F} */
    private Vector3f origin;
    /** @modelguid {CA1CC7F6-BDC9-4596-9302-1B5D2CD99424} */
    private Vector3f direction;
    
    /** @modelguid {461A45DC-9520-4A46-BDEE-190BFFAD7A26} */
    public Line() {
        origin = new Vector3f();
        direction = new Vector3f();
    }
    
    /** @modelguid {4F319693-7352-47A6-A078-B91D5F1534E3} */
    public Line(Vector3f origin, Vector3f direction) {
        this.origin = origin;
        this.direction = direction;
    }
    
    /** @modelguid {AD3B10A2-8A4B-4DAE-9E96-878C81DBD259} */
    public Vector3f getOrigin() {
        return origin;
    }
    
    /** @modelguid {5A6310E9-6C13-4C0E-B62B-B36692BF392B} */
    public void setOrigin(Vector3f origin) {
        this.origin = origin;
    }
    
    /** @modelguid {14116EEC-72CF-4045-BEFD-FAF445588597} */
    public Vector3f getDirection() {
        return direction;
    }
    
    /** @modelguid {4464B684-BD53-4A69-9D53-011AD2797D56} */
    public void setDirection(Vector3f direction) {
        this.direction = direction;
    }
    
    /** @modelguid {08847A89-0B33-45FE-AD2D-9F7BD7730F0C} */
    public Vector3f random() {
        Vector3f result = new Vector3f();
        float rand = (float)Math.random();
        
        result.x = (origin.x * (1 - rand)) + (direction.x * rand);
        result.y = (origin.y * (1 - rand)) + (direction.y * rand);
        result.z = (origin.z * (1 - rand)) + (direction.z * rand);
        
        return result;
    }
}
